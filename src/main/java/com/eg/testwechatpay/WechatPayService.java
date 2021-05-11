package com.eg.testwechatpay;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eg.testwechatpay.bean.wechat.pay.jsapi.prepareid.Amount;
import com.eg.testwechatpay.bean.wechat.pay.jsapi.prepareid.Payer;
import com.eg.testwechatpay.bean.wechat.pay.jsapi.prepareid.PrepareIdRequest;
import com.eg.testwechatpay.util.UUIDUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Service
public class WechatPayService {
    @Value("${wechat.pay.mchid}")
    private String mchid;
    @Value("${wechat.pay.serial_no}")
    private String serial_no;
    @Value("${wechat.pay.appid}")
    private String appid;
    @Value("${wechat.pay.notify_url}")
    private String notify_url;
    @Value("${wechat.pay.privateKeyPath}")
    private String privateKeyPath;

    public String getOrderId() {
        return UUIDUtil.getUUID();
    }

    private PrivateKey getPrivateKey() {
        File file = new File(privateKeyPath);
        try {
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            String privateKey = content.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            return KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("当前Java环境不支持RSA", e);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            throw new RuntimeException("无效的密钥格式");
        }
        return null;
    }

    private String sign(String text) {
        PrivateKey privateKey = getPrivateKey();
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(privateKey);
            sign.update(text.getBytes());
            return Base64.getEncoder().encodeToString(sign.sign());
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * JSAPI统一下单
     * {
     * "appid":"wx2703e24708e3d2ca",
     * "mchid":"1609202393",
     * "description":"Image形象店-深圳腾大-QQ公仔",
     * "out_trade_no":"1217752501201407033233368318",
     * "time_expire":"",
     * "attach":"",
     * "notify_url":"https://weixin.qq.com/",
     * "amount":{
     * "total":1,
     * "currency":"CNY"
     * },
     * "payer":{
     * "openid":"ooJ4W5bJCy6ZtXIsyXbKdxXwoHwI"
     * }
     * }
     *
     * @return <br>
     * {
     * "prepay_id": "wx26112221580621e9b071c00d9e093b0000"
     * }
     */
    public String getJsapiPrepayId(String openid, String description, int amountTotal, String orderId) {
        String url = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";
        PrepareIdRequest request = new PrepareIdRequest();
        request.setAppid(appid);
        request.setMchid(mchid);
        request.setDescription(description);
        request.setOut_trade_no(orderId);
        request.setNotify_url(notify_url);
        Amount amount = new Amount();
        amount.setTotal(amountTotal);
        request.setAmount(amount);
        Payer payer = new Payer();
        payer.setOpenid(openid);
        request.setPayer(payer);
        String body = JSON.toJSONString(request);

        long timestamp = System.currentTimeMillis() / 1000;
        String nonce_str = RandomStringUtils.randomAlphanumeric(16);
        String signText = "POST\n"
                + "/v3/pay/transactions/jsapi\n"
                + timestamp + "\n"
                + nonce_str + "\n"
                + body + "\n";

        String signature = sign(signText);

        //  mchid="1609202393",
        //  serial_no="2358F61A1A50BE22DC036A2FF06D437777AA5DE6",
        //  nonce_str="5EBBA407F99B95194B944F9A7ED0F726",
        //  timestamp="1620700133",
        //  signature="PACZ0q68Fw=="
        String authorization = "WECHATPAY2-SHA256-RSA2048 "
                + "mchid=\"" + mchid + "\","
                + "serial_no=\"" + serial_no + "\","
                + "timestamp=\"" + timestamp + "\","
                + "nonce_str=\"" + nonce_str + "\","
                + "signature=\"" + signature + "\"";

        String json = HttpRequest.post(url)
                .auth(authorization)
                .body(body)
                .execute().body();

        JSONObject jsonObject = JSONObject.parseObject(json);
        System.out.println(jsonObject);
        return jsonObject.getString("prepay_id");
    }

}
