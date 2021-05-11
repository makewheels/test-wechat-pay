package com.eg.testwechatpay.wechatpay.bean.jsapi.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class Response {
    private String timeStamp;
    private String nonceStr;
    @JSONField(name = "package")
    private String packageStr;
    private String signType;
    private String paySign;
}
