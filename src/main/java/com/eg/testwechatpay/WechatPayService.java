package com.eg.testwechatpay;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eg.testwechatpay.wechatpay.bean.jsapi.prepareid.Amount;
import com.eg.testwechatpay.wechatpay.bean.jsapi.prepareid.Payer;
import com.eg.testwechatpay.wechatpay.bean.jsapi.prepareid.PrepareIdRequest;
import com.eg.testwechatpay.util.UUIDUtil;
import com.eg.testwechatpay.wechatpay.bean.jsapi.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Service
@Slf4j
public class WechatPayService {
    @Value("${wechat.pay.mchid}")
    private String mchid;

    @Value("${wechat.pay.serial_no}")
    private String serial_no;

    @Value("${wechat.pay.notify_url}")
    private String notify_url;

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.pay.privateKeyPath}")
    private String privateKeyPath;

    private PrivateKey privateKey;

    public String getOrderId() {
        return UUIDUtil.getUUID();
    }

    private PrivateKey getPrivateKey() {
        if (privateKey != null)
            return privateKey;
        File file = new File(privateKeyPath);
        try {
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            String privateKeyString = content.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            privateKey = KeyFactory.getInstance("RSA").generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString)));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("当前Java环境不支持RSA", e);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            throw new RuntimeException("无效的密钥格式");
        }
        return privateKey;
    }

    private String sign(String text) {
        PrivateKey privateKey = getPrivateKey();
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(text.getBytes());
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取签名头
     *
     * @param method GET, POST
     * @param url    相对地址，例如 /v3/pay/transactions/jsapi
     * @param body
     * @return
     */
    private String getAuthorizationHeader(String method, String url, String body) {
        long timestamp = System.currentTimeMillis() / 1000;
        String nonce_str = RandomStringUtils.randomAlphanumeric(16);
        String signText = method + "\n"
                + url + "\n"
                + timestamp + "\n"
                + nonce_str + "\n"
                + body + "\n";

        String signature = sign(signText);

        //  mchid="1609202393",
        //  serial_no="2358F61A1A50BE22DC036A2FF06D437777AA5DE6",
        //  nonce_str="5EBBA407F99B95194B944F9A7ED0F726",
        //  timestamp="1620700133",
        //  signature="PACZ0q68Fw=="
        return "WECHATPAY2-SHA256-RSA2048 "
                + "mchid=\"" + mchid + "\","
                + "serial_no=\"" + serial_no + "\","
                + "timestamp=\"" + timestamp + "\","
                + "nonce_str=\"" + nonce_str + "\","
                + "signature=\"" + signature + "\"";
    }

    /**
     * 第一步
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
    public String getJsapiPrepayId(
            String openid, String description, int amountTotal, String orderId) {
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

        String authorizationHeader = getAuthorizationHeader(
                "POST", "/v3/pay/transactions/jsapi", body);

        String json = HttpRequest.post(url)
                .auth(authorizationHeader)
                .body(body)
                .execute().body();

        JSONObject jsonObject = JSONObject.parseObject(json);
        log.info("创建预付订单，微信返回: {}", jsonObject);
        return jsonObject.getString("prepay_id");
    }

    /**
     * 第二步，根据预付订单id，获取小程序发起支付请求，所需信息
     * wx.requestPayment({
     * timeStamp: '1620705237',
     * nonceStr: '5K8264ILTKCH16CQ2502SI8ZNMTM67VS',
     * package: 'prepay_id=wx11114818772442358df66b6e1322c60000',
     * signType: 'RSA',
     * paySign: 'XfX0cJ9+ko4LdcGP+Z==',
     * success (res) { },
     * fail (res) { }
     * })
     *
     * @param prepay_id
     * @return
     */
    public Response getMiniProgramResponse(String prepay_id) {
        Response response = new Response();
        long timeStamp = System.currentTimeMillis() / 1000;
        String nonceStr = RandomStringUtils.randomAlphanumeric(16);
        String packageStr = "prepay_id=" + prepay_id;
        String signText = appid + "\n"
                + timeStamp + "\n"
                + nonceStr + "\n"
                + packageStr + "\n";

        String paySign = sign(signText);
        response.setTimeStamp(timeStamp + "");
        response.setNonceStr(nonceStr);
        response.setPackageStr(packageStr);
        response.setSignType("RSA");
        response.setPaySign(paySign);
        log.info("小程序支付所需提交信息: " + JSON.toJSONString(response));
        return response;
    }

}
