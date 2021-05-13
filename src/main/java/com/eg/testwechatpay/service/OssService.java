package com.eg.testwechatpay.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.internal.OSSHeaders;
import com.aliyun.oss.model.*;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class OssService {
    private final String endpoint = "https://oss-cn-beijing.aliyuncs.com";
    private final String accessKeyId = "LTAI5t6n9vK3GSeMBrBpX8Uh";
    private final String accessKeySecret = "";
    private final String bucketName = "test-wechat-pay";

    private final String baseUrl = "https://test-wechat-pay.oss-cn-beijing.aliyuncs.com";

    private OSS ossClient;

    private OSS getOssClient() {
        if (ossClient == null) {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        }
        return ossClient;
    }

    public String upload(File file, String objectName) {
        PutObjectRequest request = new PutObjectRequest(bucketName, objectName, file);
        PutObjectResult result = getOssClient().putObject(request);
        return baseUrl + "/" + objectName;
    }
}
