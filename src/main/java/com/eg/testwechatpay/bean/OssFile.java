package com.eg.testwechatpay.bean;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document
public class OssFile {
    @Id
    private String _id;

    private String bucket;
    private String object;
    private String url;
    private Long size;
    private String md5;
    private Date createTime;

}
