package com.example.testwechatpay;

import com.github.wxpay.sdk.WXPayConfig;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class MyConfig implements WXPayConfig {

    private final byte[] certData;

    public MyConfig() throws Exception {
        String certPath = "D:\\Programming\\SoftTopics\\2021.05.06微信支付\\证书\\apiclient_cert.pem";
        File file = new File(certPath);
        InputStream certStream = new FileInputStream(file);
        this.certData = new byte[(int) file.length()];
        certStream.read(this.certData);
        certStream.close();
    }

    public String getAppID() {
        return "wx2703e24708e3d2ca";
    }

    public String getMchID() {
        return "1609202393";
    }

    public String getKey() {
        return null;
    }

    public InputStream getCertStream() {
        return new ByteArrayInputStream(this.certData);
    }

    public int getHttpConnectTimeoutMs() {
        return 8000;
    }

    public int getHttpReadTimeoutMs() {
        return 10000;
    }
}
