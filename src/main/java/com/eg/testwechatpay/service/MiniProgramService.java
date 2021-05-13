package com.eg.testwechatpay.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eg.testwechatpay.bean.payresponse.MiniProgramResponse;
import com.eg.testwechatpay.bean.qrcode.QRCodeRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class MiniProgramService {
    @Value("${wechat.appid}")
    private String appid;

    @Resource
    private SecretService secretService;
    @Resource
    private WechatPayService wechatPayService;

    /**
     * 生成小程序码
     *
     * @return
     */
    public String getQRCode() {
        String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token="
                + secretService.getAccessToken();
        QRCodeRequest request = new QRCodeRequest();
        String scene = RandomStringUtils.randomAlphanumeric(16);
        System.out.println(scene);
        request.setScene(scene);
        HttpResponse response = HttpRequest.post(url)
                .body(JSON.toJSONString(request))
                .execute();
        File file = new File(SystemUtils.getUserHome()
                + "/Downloads/" + System.currentTimeMillis() + ".jpg");
        try {
            FileUtils.writeByteArrayToFile(file, response.bodyBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 登录
     *
     * @param js_code
     * @return
     */
    public JSONObject login(String js_code) {
        String json = HttpUtil.get("https://api.weixin.qq.com/sns/jscode2session?appid=" + appid
                + "&secret=" + secretService.getAppSecret() + "&js_code=" + js_code
                + "&grant_type=authorization_code");
        JSONObject jsonObject = JSONObject.parseObject(json);
        String openid = jsonObject.getString("openid");
        log.info("小程序登陆, js_code = {}, openid = {}", js_code, openid);
        return jsonObject;
    }

    /**
     * 路由
     *
     * @param queryScene
     * @return
     */
    public String route(String queryScene) {
        //TODO 查询数据库
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("version", 1);
        jsonObject.put("cmd", "pay");
        return jsonObject.toJSONString();
    }

    /**
     * 创建订单
     *
     * @param openid
     * @param queryScene
     * @return
     */
    public String createOrder(String openid, String queryScene) {
        String orderId = wechatPayService.getOrderId();
        //TODO 保存数据库
        log.info("创建订单, orderId = {}", orderId);
        String description = "在线捐款" + RandomUtil.randomNumbers(4);
        int amountTotal = 1;
        String prepay_id = wechatPayService.getJsapiPrepayId(
                openid, description, amountTotal, orderId);
        MiniProgramResponse miniProgramResponse
                = wechatPayService.getMiniProgramResponse(prepay_id, orderId);
        return JSON.toJSONString(miniProgramResponse);
    }
}
