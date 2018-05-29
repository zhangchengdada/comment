package com.netease.comment.utils.http;


import com.netease.comment.utils.http.HttpPoolManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhaofeng01 on 2018/1/17.
 */
@Slf4j
@Component
public class HttpUtil {


    private static String charset = "UTF-8";
    private static final int TIMEOUT_IN_MILLIONS = 5000;
    //private static final String imgType = "image/*";
    private static final Pattern pattern = Pattern.compile("image/*");

    public  String postMethod(String url, List<NameValuePair> pairs) throws IOException {
        return postMethod(url, pairs, MediaType.APPLICATION_JSON_UTF8_VALUE);
    }


    public interface CallBack {
        void onRequestComplete(String result);
    }

    /**
     * Get请求，获得返回数据
     *
     * @param urlStr
     * @return
     * @throws Exception
     */
    public  String doGet(String urlStr) {
        URL url = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIMEOUT_IN_MILLIONS);
            conn.setConnectTimeout(TIMEOUT_IN_MILLIONS);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            if (conn.getResponseCode() == 200) {
                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                int len = -1;
                byte[] buf = new byte[128];

                while ((len = is.read(buf)) != -1) {
                    baos.write(buf, 0, len);
                }
                baos.flush();
                return baos.toString();
            } else {
                throw new RuntimeException(" responseCode is not 200 ... ");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            }
            try {
                if (baos != null)
                    baos.close();
            } catch (IOException e) {
            }
            conn.disconnect();
        }

        return null;

    }


    /**
     * @param url
     * @param pairs
     * @param mediaType
     * @return
     */
    public  String postMethod(String url, List<NameValuePair> pairs, String mediaType) throws IOException {
        HttpClient httpClient = HttpPoolManager.getHttpClient();
        HttpPost post = new HttpPost(url);
        UrlEncodedFormEntity se = new UrlEncodedFormEntity(pairs, charset);
        se.setContentType(mediaType);
        post.setEntity(se);
        HttpResponse response = httpClient.execute(post, HttpClientContext.create());
        StatusLine sl = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        String ret = EntityUtils.toString(entity, charset);
        if (HttpStatus.SC_OK == sl.getStatusCode()) {
            return ret;
        } else {
            log.error("reqesut url failed. url {}, return code {}, content {}", url, sl.getStatusCode(), ret);
        }
        return null;
    }

    /**
     * 根据url, String的requestBody获取post链接
     *
     * @param url
     * @param content
     * @return
     * @throws IOException
     */
    public  String postMethod(String url, String content) throws IOException {
        return postMethod(url, content, MediaType.APPLICATION_JSON_UTF8_VALUE);
    }

    /**
     * @param url
     * @param body
     * @param mediaType
     * @return
     */
    public  String postMethod(String url, String body, String mediaType) throws IOException {
        HttpClient httpClient = HttpPoolManager.getHttpClient();
        HttpPost post = new HttpPost(url);
        StringEntity se = new StringEntity(body, charset);
        se.setContentType(mediaType);
        post.setEntity(se);
        HttpResponse response = httpClient.execute(post, HttpClientContext.create());
        StatusLine sl = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        String ret = EntityUtils.toString(entity, charset);
        if (HttpStatus.SC_OK == sl.getStatusCode()) {
            return ret;
        } else {
            log.error("reqesut url failed. url {}, return code {}, content {}", url, sl.getStatusCode(), ret);
        }
        return null;
    }

    public  String postMethodWithHeaders(String url, List<Header> httpHead, String body) throws IOException {
        return postMethodWithHeaders(url, httpHead, body, MediaType.APPLICATION_JSON_UTF8_VALUE);
    }

    /**
     * 可以指定http头
     *
     * @param url
     * @param httpHead
     * @param body
     * @param mediaType
     * @return
     * @throws IOException
     */
    public  String postMethodWithHeaders(String url, List<Header> httpHead, String body, String mediaType) throws IOException {
        HttpClient httpClient = HttpPoolManager.getHttpClient();
        HttpPost post = new HttpPost(url);
        StringEntity se = new StringEntity(body, charset);
        se.setContentType(mediaType);
        post.setEntity(se);
        post.setHeaders(httpHead.toArray(new Header[0]));
        HttpResponse response = httpClient.execute(post, HttpClientContext.create());
        StatusLine sl = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        String ret = EntityUtils.toString(entity, charset);
        if (HttpStatus.SC_OK == sl.getStatusCode()) {
            return ret;
        } else {
            log.error("reqesut url failed. url {}, return code {}, content {}", url, sl.getStatusCode(), ret);
        }
        return null;
    }

    /**
     * 发送get请求, 返回一个String类型的返回值
     *
     * @param url
     * @return
     * @throws IOException
     */
    public  String getMethod(String url) throws IOException {
        HttpClient httpClient = HttpPoolManager.getHttpClient();
        HttpGet get = new HttpGet(url);
        HttpResponse response = httpClient.execute(get, HttpClientContext.create());
        StatusLine sl = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        String ret = EntityUtils.toString(entity, charset);
        if (HttpStatus.SC_OK == sl.getStatusCode()) {
            return ret;
        } else {
            log.error("reqesut url failed. url {}, return code {}, content {}", url, sl.getStatusCode(), ret);
        }
        return null;
    }

    /**
     * 直接返回HttpEntity,注意，业务逻辑必须要自己consume掉这个httpEntity，否则会占用链接，阻塞整个http服务
     *
     * @param url
     * @return
     * @throws IOException
     */
    public  HttpEntity getMethodResEntity(String url) throws IOException {
        HttpClient httpClient = HttpPoolManager.getHttpClient();
        HttpGet get = new HttpGet(url);
        HttpResponse response = httpClient.execute(get, HttpClientContext.create());
        StatusLine sl = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        //String ret = EntityUtils.toString(entity, charset);
        if (HttpStatus.SC_OK == sl.getStatusCode()) {
            return entity;
        } else {
            EntityUtils.consume(entity);
            log.error("reqesut url failed. url {}, return code {}", url, sl.getStatusCode());
        }
        return null;
    }

    /**
     * 通过post请求下载资源，返回一个byte数组
     *
     * @param url
     * @return
     * @throws IOException
     */
    public  byte[] downloadByPost(String url) throws IOException {
        HttpClient httpClient = HttpPoolManager.getHttpClient();
        HttpPost post = new HttpPost(url);
        HttpResponse response = httpClient.execute(post, HttpClientContext.create());
        StatusLine sl = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        if (HttpStatus.SC_OK == sl.getStatusCode()) {
            return EntityUtils.toByteArray(entity);
        } else {
            EntityUtils.consume(entity);
            log.error("reqesut url failed. url {}, return code {}", url, sl.getStatusCode());
            return null;
        }
    }

    /**
     * 通过get请求下载资源，返回一个byte数组
     *
     * @param url
     * @return
     * @throws IOException
     */
    public  byte[] downloadByGet(String url) throws IOException {
        HttpClient httpClient = HttpPoolManager.getHttpClient();
        HttpGet post = new HttpGet(url);
        HttpResponse response = httpClient.execute(post, HttpClientContext.create());
        StatusLine sl = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        if (HttpStatus.SC_OK == sl.getStatusCode()) {
            return EntityUtils.toByteArray(entity);
        } else {
            EntityUtils.consume(entity);
            log.error("reqesut url failed. url {}, return code {}", url, sl.getStatusCode());
            return null;
        }
    }

    /**
     * 通过get请求下载资源，返回一个byte数组
     *
     * @param url
     * @return
     * @throws IOException
     */
    public  HttpResponse downloadPictureByGet(String url, List<Header> httpHead) throws IOException {
        HttpClient httpClient = HttpPoolManager.getHttpClient();
        HttpGet post = new HttpGet(url);
        if (!CollectionUtils.isEmpty(httpHead)) {
            post.setHeaders(httpHead.toArray(new Header[0]));
        }
        HttpResponse response = httpClient.execute(post, HttpClientContext.create());
        StatusLine sl = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        if (HttpStatus.SC_OK == sl.getStatusCode() && verifyImg(entity.getContentType())) {
//            Header[] head = response.getHeaders(HttpHeaders.CONTENT_TYPE);
//            if (head.length >= 1) {
//                contentType = head[0].getValue();
//            }
//            return EntityUtils.toByteArray(entity);
            return response;
        } else {
            EntityUtils.consume(entity);
            log.error("reqesut url failed. url {}, return code {}", url, sl.getStatusCode());
            return null;
        }
    }

    public  boolean verifyImg(Header contentType) {
        String type = contentType.getValue();
        Matcher matcher = pattern.matcher(type);
        return matcher.find();
    }

}
