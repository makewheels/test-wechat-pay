package com.eg.testwechatpay.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eg.testwechatpay.bean.Order;
import com.eg.testwechatpay.bean.OssFile;
import com.eg.testwechatpay.bean.QRCode;
import com.eg.testwechatpay.repository.OrderRepository;
import com.eg.testwechatpay.repository.OssFileRepository;
import com.eg.testwechatpay.repository.QRCodeRepository;
import com.eg.testwechatpay.wechat.payresponse.PayResponse;
import com.eg.testwechatpay.wechat.qrcode.QRCodeRequest;
import com.eg.testwechatpay.wechat.transaction.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
    @Resource
    private OssFileRepository ossFileRepository;
    @Resource
    private OrderRepository orderRepository;

    @Resource
    private OssService ossService;

    /**
     * 生成queryScene
     *
     * @return
     */
    private String getQueryScene() {
        String queryScene;
        QRCode find;
        do {
            queryScene = RandomUtil.randomString(12);
            find = qrCodeRepository.findByQueryScene(queryScene);
        } while (find != null);
        return queryScene;
    }

    /**
     * 批量创建小程序码
     *
     * @param amount
     * @return
     */
    public List<QRCode> batchCreateQRCode(int amount) {
        List<QRCode> qrCodes = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            QRCode qrCode = createQRCode();
            qrCodes.add(qrCode);
        }
        return qrCodes;
    }

    /**
     * 生成单个小程序码
     */
    public QRCode createQRCode() {
        String getwxacodeunlimitUrl = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token="
                + secretService.getAccessToken();
        String queryScene = getQueryScene();
        QRCodeRequest request = new QRCodeRequest();
        request.setScene(queryScene);
        HttpResponse response = HttpRequest.post(getwxacodeunlimitUrl)
                .body(JSON.toJSONString(request))
                .execute();
        File folder = SystemUtils.getJavaIoTmpDir();
        if (!folder.exists())
            folder.mkdirs();
        File imageFile = new File(folder,
                System.currentTimeMillis() + "-" + queryScene + ".jpg");
        log.info("生成小程序码，queryScene = {}, 下载文件: {}", queryScene, imageFile.getPath());
        //下载文件
        try {
            FileUtils.writeByteArrayToFile(imageFile, response.bodyBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //上传对象存储
        String object = "qrcodes/" + queryScene + ".jpg";
        log.info("上传对象存储 object = {}", object);
        String url = ossService.upload(imageFile, object);
        OssFile ossFile = new OssFile();
        ossFile.setBucket(ossService.getBucket());
        ossFile.setObject(object);
        ossFile.setUrl(url);
        ossFile.setSize(imageFile.length());
        ossFile.setMd5(SecureUtil.md5(imageFile));
        ossFile.setCreateTime(new Date());
        ossFileRepository.save(ossFile);
        log.info("保存ossFile到数据库: {}", JSON.toJSONString(ossFile));
        //删除本地文件
        boolean deleteResult = imageFile.delete();
        log.info("删除本地文件: 删除结果: {}, 文件路径: {}", deleteResult, imageFile.getPath());
        //返回对象
        QRCode qrCode = new QRCode();
        qrCode.setCreateTime(new Date());
        qrCode.setQueryScene(queryScene);
        qrCode.setIsUsed(false);
        qrCode.setOssFileId(ossFile.get_id());
        qrCodeRepository.save(qrCode);
        log.info("保存qrCode到数据库: {}", JSON.toJSONString(qrCode));
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
     * 请求支付
     *
     * @param openid
     * @param queryScene
     * @return
     */
    public String requestPay(String openid, String queryScene) {
        //先根据queryScene查出订单
        Order order = orderRepository.findByQueryScene(queryScene);
        //如果没找到这个订单
        if (order == null)
            return null;
        String orderId = order.getOrderId();
        String prepay_id = order.getPrepay_id();
        //如果订单没有生成过预付订单号，那就生成，并且更新保存数据库
        if (StringUtils.isEmpty(prepay_id)) {
            prepay_id = wechatPayService.getJsapiPrepayId(
                    openid, order.getName(), order.getPrice(), orderId);
            order.setOpenid(openid);
            order.setPrepay_id(prepay_id);
            orderRepository.save(order);
        } else {
            log.info("微信预付订单号已存在, prepay_id = {}", prepay_id);
        }
        PayResponse payResponse = wechatPayService
                .getMiniProgramResponse(prepay_id, orderId);
        return JSON.toJSONString(payResponse);
    }

    /**
     * 当订单支付完成时
     *
     * @param openid
     * @param queryScene
     * @param orderId
     * @return
     */
    public String onPaySuccess(String openid, String queryScene, String orderId) {
        String json = wechatPayService.queryTransactionByOutTradeNo(orderId);
        log.info("向微信查询订单, 商户订单号: {}, 微信返回结果为: {}", orderId, json);

        Transaction transaction = JSON.parseObject(json, Transaction.class);
        //判断微信返回的支付状态
        if (!transaction.getTrade_state().equals("SUCCESS"))
            return null;
        //更新订单状态
        Order order = orderRepository.findByOrderId(orderId);
        //判断queryScene是否匹配
        if (!order.getQueryScene().equals(queryScene))
            return null;
        //判断openid是否匹配
        if (!order.getOpenid().equals(openid))
            return null;

        String transaction_id = transaction.getTransaction_id();
        log.info("订单支付成功, 微信订单号: {}, 商户订单号: {}, 支付时间: {}",
                transaction_id, orderId, transaction.getSuccess_time());

        order.setIsPaid(true);
        order.setTransaction_id(transaction_id);
        order.setTransaction(transaction);
        orderRepository.save(order);
        log.info("更新订单状态: {}", JSON.toJSONString(order));
        return json;
    }
}
