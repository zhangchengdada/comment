package com.netease.comment.utils.http;


import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by zhaofeng01 on 2018/1/17.
 */
@Slf4j
public class HttpPoolManager {


    private static CloseableHttpClient closeableHttpClient;

    private static PoolingHttpClientConnectionManager cm;

    private static HttpRequestRetryHandler retryHandler;

    private static int maxConn = 300;

    private static int defaultMaxPerRoute = 15;

    private static int soTimeout = 5000;

    private static int connTimeout = 3000;

    private static int retryCount = 5;

    private static int keepAliveDuration = 10 * 1000;

    public static HttpClient getHttpClient() {
        if (closeableHttpClient == null) {
            init();
        }
        return closeableHttpClient;
    }

    private static synchronized void init() {
        if (closeableHttpClient != null) {
            return;
        }

        LayeredConnectionSocketFactory sslsf = null;

        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            SSLContext sslContext = SSLContexts.custom().setProtocol("TLS").loadTrustMaterial(trustStore,
                    (TrustStrategy) (chain, authType) -> true).build();

//            sslsf = new SSLConnectionSocketFactory(sslContext,
//                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // 同时支持http，https协议
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", sslsf)
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connTimeout)
                .setSocketTimeout(soTimeout)
                .setConnectionRequestTimeout(connTimeout)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();

        // 连接keepAlive策略设置
        ConnectionKeepAliveStrategy keepAliveStrategy = new DefaultConnectionKeepAliveStrategy() {

            @Override
            public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {
                long keepAlive = super.getKeepAliveDuration(httpResponse, httpContext);
                if (keepAlive == -1) {
                    keepAlive = keepAliveDuration;
                }
                return keepAlive;
            }
        };

        // 创建，配置连接池属性
        cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        cm.setMaxTotal(maxConn);
        cm.setDefaultMaxPerRoute(defaultMaxPerRoute);
        cm.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(soTimeout).build());

        if (retryHandler == null) {
            createRetryHandler();
        }

        // 创建可以复用的http客户端
        closeableHttpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(retryHandler)
                .setKeepAliveStrategy(keepAliveStrategy)
                .build();
    }

    private static void createRetryHandler() {
        if (retryHandler != null) {
            return;
        }
        retryHandler = new HttpRequestRetryHandler() {
            public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
                // 如果重试次数大于 retryCount 直接放弃
                if (i > retryCount) {
                    return false;
                }
                // 如果是服务器丢掉了链接，直接重试
                if (e instanceof NoHttpResponseException) {
                    return true;
                }
                // ssl的捂手异常不需要重试
                if (e instanceof SSLHandshakeException) {
                    return false;
                }
                // 链接被中断
                if (e instanceof InterruptedIOException) {
                    return true;
                }
                // 目标服务器不可达
                if (e instanceof UnknownHostException) {
                    return false;
                }
                // 超时
                if (e instanceof ConnectTimeoutException) {
                    return true;
                }
                // SSL握手异常
                if (e instanceof SSLException) {
                    return false;
                }
                HttpClientContext clientContext = HttpClientContext.adapt(httpContext);
                HttpRequest request = clientContext.getRequest();
                // 如果请求是幂等的，就再次尝试
                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    return true;
                }
                return false;
            }
        };
    }


}
