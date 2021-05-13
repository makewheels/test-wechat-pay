package com.eg.testwechatpay.bean;

import com.eg.testwechatpay.wechat.transaction.Transaction;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document
public class Order {
    @Id
    private String _id;

    @Indexed
    private String orderId;
    @Indexed
    private String transaction_id;

    private String openid;
    @Indexed
    private String prepay_id;

    private String name;
    private Integer price;
    private Date createTime;
    private Boolean isPaid;

    @Indexed
    private String queryScene;

    private Transaction transaction;

}
