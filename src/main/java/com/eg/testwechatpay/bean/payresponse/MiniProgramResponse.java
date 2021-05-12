package com.eg.testwechatpay.bean.payresponse;

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
}
