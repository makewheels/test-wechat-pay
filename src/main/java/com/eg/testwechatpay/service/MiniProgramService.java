package com.eg.testwechatpay.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.eg.testwechatpay.bean.qrcode.QRCodeRequest;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

@Service
public class MiniProgramService {
    @Value("${wechat.appid}")
    private String appid;

    @Resource
    private SecretService secretService;

    public String getQRCode() {
        String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token="
                + secretService.getAccessToken();
        QRCodeRequest request = new QRCodeRequest();
        request.setScene("v=1&cmd=pay&id=16185322516");
        HttpResponse response = HttpRequest.post(url)
                .body(JSON.toJSONString(request))
                .execute();
        File file = new File("C:\\Users\\binqiao\\Downloads\\" + System.currentTimeMillis() + ".jpg");
        try {
            FileUtils.writeByteArrayToFile(file, response.bodyBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String login(String js_code) {
        return HttpUtil.get("https://api.weixin.qq.com/sns/jscode2session?appid=" + appid
                + "&secret=" + secretService.getAppSecret() + "&js_code=" + js_code
                + "&grant_type=authorization_code");
    }
}
