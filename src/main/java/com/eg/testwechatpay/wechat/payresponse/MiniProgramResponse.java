package com.eg.testwechatpay.wechat.payresponse;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class MiniProgramResponse {
    private String timeStamp;
    private String nonceStr;
    @JSONField(name = "package")
    private String packageStr;
    private String signType;
    private String paySign;

    //上面都是小程序提交的信息，但是这个是我的订单号
    private String orderId;
}
