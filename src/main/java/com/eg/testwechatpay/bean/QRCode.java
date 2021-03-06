package com.eg.testwechatpay.bean;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.File;
import java.util.Date;

@Data
@Document
public class QRCode {
    @Id
    private String _id;

    @Indexed
    private String queryScene;
    private Boolean isUsed;
    private Date createTime;

    private String ossFileId;

}
