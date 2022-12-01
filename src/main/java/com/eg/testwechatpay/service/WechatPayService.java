package com.eg.testwechatpay.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eg.testwechatpay.wechat.payresponse.PayResponse;
import com.eg.testwechatpay.wechat.prepayid.Amount;
import com.eg.testwechatpay.wechat.prepayid.Payer;
import com.eg.testwechatpay.wechat.prepayid.PrepareIdRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
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

    public static final String wechatPayBaseUrl = "https://api.mch.weixin.qq.com";

    public String getOrderId() {
        String uuid = IdUtil.simpleUUID();
        return new BigInteger(uuid, 16).toString(36);
    }

    /**
     * 读取本地私钥
     *
     * @return
     */
    private PrivateKey getPrivateKey() {
        if (privateKey != null)
            return privateKey;
        File file = new File(privateKeyPath);
        try {
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            String privateKeyString = content
                    .replace("-----BEGIN PRIVATE KEY-----", "")
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

    /**
     * 签名
     *
     * @param text
     * @return
     */
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
        String nonce_str = RandomStringUtils.randomAlphanumeric(20);

        if (body == null)
            body = "";
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
     * 发送GET请求
     *
     * @param relativeUrl
     * @return
     */
    private String getRequest(String relativeUrl) {
        String authorizationHeader = getAuthorizationHeader("GET", relativeUrl, null);
        return HttpRequest.get(wechatPayBaseUrl + relativeUrl)
                .auth(authorizationHeader)
                .execute().body();
    }

    /**
     * 发送POST请求
     *
     * @param relativeUrl
     * @param body
     * @return
     */
    private String postRequest(String relativeUrl, String body) {
        String authorizationHeader = getAuthorizationHeader("POST", relativeUrl, body);
        return HttpRequest.post(wechatPayBaseUrl + relativeUrl)
                .auth(authorizationHeader)
                .body(body)
                .execute().body();
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
        String json = postRequest("/v3/pay/transactions/jsapi", body);

        JSONObject jsonObject = JSONObject.parseObject(json);
        String prepay_id = jsonObject.getString("prepay_id");
        if (StringUtils.isEmpty(prepay_id))
            log.error("创建预付订单出错，微信返回的结果中没有prepay_id, 微信返回: {}", jsonObject);
        else
            log.info("创建预付订单成功，微信返回: {}", jsonObject);
        return prepay_id;
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
    public PayResponse getMiniProgramResponse(String prepay_id, String orderId) {
        long timeStamp = System.currentTimeMillis() / 1000;
        String nonceStr = RandomStringUtils.randomAlphanumeric(20);
        String packageStr = "prepay_id=" + prepay_id;
        String signText = appid + "\n"
                + timeStamp + "\n"
                + nonceStr + "\n"
                + packageStr + "\n";
        String paySign = sign(signText);

        PayResponse payResponse = new PayResponse();
        payResponse.setOrderId(orderId);
        payResponse.setTimeStamp(timeStamp + "");
        payResponse.setNonceStr(nonceStr);
        payResponse.setPackageStr(packageStr);
        payResponse.setSignType("RSA");
        payResponse.setPaySign(paySign);
        log.info("小程序支付所需提交信息: " + JSON.toJSONString(payResponse));
        return payResponse;
    }

    /**
     * 查询订单: 根据商户订单号
     * <p>
     * https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no
     * /43138c3f00e947e7b10bfaa7bb854b35?mchid=1609202393
     * <p>
     * {
     * "amount": {
     * "currency": "CNY",
     * "payer_currency": "CNY",
     * "payer_total": 1,
     * "total": 1
     * },
     * "appid": "wx2703e24708e3d2ca",
     * "attach": "",
     * "bank_type": "OTHERS",
     * "mchid": "1609202393",
     * "out_trade_no": "43138c3f00e947e7b10bfaa7bb854b35",
     * "payer": {
     * "openid": "ooJ4W5bJCy6ZtXIsyXbKdxXwoHwI"
     * },
     * "promotion_detail": [],
     * "success_time": "2021-05-12T00:19:52+08:00",
     * "trade_state": "SUCCESS",
     * "trade_state_desc": "支付成功",
     * "trade_type": "JSAPI",
     * "transaction_id": "4200001025202105128641360838"
     * }
     */
    public String queryTransactionByOutTradeNo(String out_trade_no) {
        String relativeUrl = "/v3/pay/transactions/out-trade-no/" + out_trade_no
                + "?mchid=" + mchid;
        return getRequest(relativeUrl);
    }

    /**
     * 查询订单: 根据微信订单号
     * <p>
     * https://api.mch.weixin.qq.com/v3/pay/transactions/id/1217752501201407033233368018
     */
    public String queryTransactionByWechatTransactionId(String transaction_id) {
        String relativeUrl = "/v3/pay/transactions/id/" + transaction_id + "?mchid=" + mchid;
        return getRequest(relativeUrl);
    }

    public String createNative() {
        // {
        //     "mchid": "1900006XXX",
        //     "out_trade_no": "native12177525012014070332333",
        //     "appid": "wxdace645e0bc2cXXX",
        //     "description": "Image形象店-深圳腾大-QQ公仔",
        //     "notify_url": "https://weixin.qq.com/",
        //     "amount": {
        //         "total": 1,
        //         "currency": "CNY"
        //     }
        // }

        JSONObject body = new JSONObject();
        body.put("mchid", mchid);
        body.put("out_trade_no", getOrderId());
        body.put("appid", appid);
        body.put("description", "description-" + IdUtil.simpleUUID());
        body.put("notify_url", "https://weixin.qq.com/");

        JSONObject amount = new JSONObject();
        amount.put("total", 1);
        body.put("amount", amount);
        log.info("创建微信支付native订单，参数为：{}", body.toJSONString());
        String response = postRequest("/v3/pay/transactions/native", body.toJSONString());
        log.info("微信返回创建native订单结果：{}", response);
        return JSON.parseObject(response).getString("code_url");
    }
}
