package com.eg.testwechatpay.service;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.eg.testwechatpay.bean.qrcode.QRCodeRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MiniProgramService {
    @Resource
    private AccessTokenService accessTokenService;

    public String getQRCode() {
        String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token="
                + accessTokenService.getAccessToken();
        QRCodeRequest qrCodeRequest = new QRCodeRequest();
        qrCodeRequest.setScene("fawe");
        System.out.println("qrCodeRequest = " + JSON.toJSONString(qrCodeRequest));
        String response = HttpRequest.post(url)
                .body(JSON.toJSONString(qrCodeRequest))
                .execute().body();
        System.out.println("response = " + response);
        return response;
    }
}
