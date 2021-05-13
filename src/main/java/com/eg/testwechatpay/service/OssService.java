package com.eg.testwechatpay.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;

@Service
public class OssService {
    private final String endpoint = "https://oss-cn-beijing.aliyuncs.com";
    private final String accessKeyId = System.getenv(
            "test_wechat_pay_aliyun_oss_accessKeyId");
    private final String accessKeySecret = System.getenv(
            "test_wechat_pay_aliyun_oss_accessKeySecret");
    private final String bucket = "test-wechat-pay";

    private final String baseUrl = "https://test-wechat-pay.oss-cn-beijing.aliyuncs.com";

    private OSS ossClient;

    public String getBucket() {
        return bucket;
    }

    private OSS getOssClient() {
        if (ossClient == null) {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        }
        return ossClient;
    }

    /**
     * 上传文件
     *
     * @param file
     * @param object
     * @return
     */
    public String upload(File file, String object) {
        PutObjectRequest request = new PutObjectRequest(bucket, object, file);
        getOssClient().putObject(request);
        return baseUrl + "/" + object;
    }

    /**
     * 生成预签名url，默认有效期20分钟
     *
     * @param object
     * @return
     */
    public String generatePreSignedUrl(String object) {
        Date expiration = new Date(new Date().getTime() + 20 * 60 * 1000);
        return getOssClient().generatePresignedUrl(bucket, object, expiration).toString();
    }

    /**
     * 生成预签名url
     *
     * @param object
     * @param time
     * @return
     */
    public String generatePreSignedUrl(String object, long time) {
        Date expiration = new Date(new Date().getTime() + time);
        return getOssClient().generatePresignedUrl(bucket, object, expiration).toString();
    }
}
