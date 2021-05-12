package com.eg.testwechatpay.bean.login;

import lombok.Data;

@Data
public class LoginResponse {
    private String session_key;
    private String openid;
}
