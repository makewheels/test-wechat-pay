package com.eg.testwechatpay.bean;

import com.eg.testwechatpay.wechat.transaction.TransactionResult;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Order {
    @Id
    private String _id;

    @Indexed
    private String orderId;
    private String createTime;
    private Boolean isPaid;

    private TransactionResult transaction;

}
