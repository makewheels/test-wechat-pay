package com.eg.testwechatpay.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eg.testwechatpay.bean.QRCode;
import com.eg.testwechatpay.repository.QRCodeRepository;
import com.eg.testwechatpay.wechat.payresponse.MiniProgramResponse;
import com.eg.testwechatpay.wechat.qrcode.QRCodeRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class MiniProgramService {
    @Value("${wechat.appid}")
    private String appid;

    @Resource
    private SecretService secretService;
    @Resource
    private WechatPayService wechatPayService;

    @Resource
    private QRCodeRepository qrCodeRepository;

    private String getQueryScene() {
        String queryScene;
        QRCode find;
        do {
            queryScene = RandomUtil.randomString(12);
            find = qrCodeRepository.findByQueryScene(queryScene);
        } while (find != null);
        return queryScene;
    }

    public List<QRCode> generateQRCode(int amount) {
        String queryScene = getQueryScene();
        List<QRCode> qrCodes = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            QRCode qrCode = createQRCode(queryScene);
            qrCodeRepository.save(qrCode);
            qrCodes.add(qrCode);
        }
        return qrCodes;
    }

    /**
     * 生成小程序码
     */
    public QRCode createQRCode(String queryScene) {
        String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token="
                + secretService.getAccessToken();
        QRCodeRequest request = new QRCodeRequest();
        request.setScene(queryScene);
        HttpResponse response = HttpRequest.post(url)
                .body(JSON.toJSONString(request))
                .execute();
        File folder = new File(SystemUtils.getUserHome() + "/Downloads/qrcode/");
        if (!folder.exists())
            folder.mkdirs();
        File imageFile = new File(folder,
                System.currentTimeMillis() + "-" + IdUtil.simpleUUID() + ".jpg");
        try {
            FileUtils.writeByteArrayToFile(imageFile, response.bodyBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        QRCode qrCode = new QRCode();
        qrCode.setCreateTime(new Date());
        qrCode.setQueryScene(queryScene);
        qrCode.setIsEnable(false);
        qrCode.setLocalFile(imageFile);
        return qrCode;
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
