package com.netease.comment.dto;


import com.netease.cloud.auth.BasicCredentials;
import com.netease.cloud.auth.Credentials;
import com.netease.cloud.services.nos.NosClient;
import com.netease.cloud.services.nos.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zhaofeng01 on 2018/1/24.
 */
@Slf4j
@Service
public class NosProxy {


    private String endpoint = "http://nos.netease.com";

    private String bucket = "newsfeed";

    private String accessKey = "9550480b889e43c9abb26987141d2e82";

    private String secretKey = "9520f48df0b648b38807131305c66898";

    private Credentials credentials;

    // nosClient 是线程安全的对象，可以在并发情况下使用
    private NosClient nosClient;

    public synchronized void init() {
        if (nosClient != null) {
            return;
        }
        credentials = new BasicCredentials(accessKey, secretKey);
        nosClient = new NosClient(credentials);
        nosClient.setEndpoint(endpoint);
    }

    public NosProxy() {}

    public NosProxy(String endpoint, String bucket, String accessKey, String secretKey) {
        this.endpoint = endpoint;
        this.bucket = bucket;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    /**
     * 获取该endpoint下面的所有的bucket的名称
     * @return
     */
    public List<String> listBucket() {
        List<String> bucketName = nosClient.listBuckets().stream()
                .map(bucket -> bucket.getName())
                .collect(Collectors.toList());
        return bucketName;
    }

    /**
     * 判断这个桶是否存在
     * @param bucketName
     * @return
     */
    public boolean doesBucketExist(String bucketName) {
        return nosClient.doesBucketExist(bucketName);
    }

    public PutObjectResult uploadObject(String filePath, String fileName) {
        return uploadObject(filePath, fileName, null);
    }

    /**
     * 上传文件到nos，本地普通上传
     * @param filePath
     * @param fileName
     * @param bucketName
     */
    public PutObjectResult uploadObject(String filePath, String fileName, String bucketName) {
        PutObjectResult result;
        try {
            if (bucketName == null) {
                result = nosClient.putObject(bucket, fileName, new File(filePath));
            } else {
                result = nosClient.putObject(bucketName, fileName, new File(filePath));
            }
            return result;
        } catch (Exception e) {
            log.error("上传文件到nos出错", e);
        }
        return null;
    }

    /**
     * 生成nos的地址
     * @param fileName
     * @return
     */
    public String getUploadAddress(String fileName) {
        return this.endpoint + "/" + this.bucket + "/" + fileName;
    }

}
