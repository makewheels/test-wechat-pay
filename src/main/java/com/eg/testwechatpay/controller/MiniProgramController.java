package com.eg.testwechatpay.controller;

import com.alibaba.fastjson.JSONObject;
import com.eg.testwechatpay.bean.QRCode;
import com.eg.testwechatpay.service.MiniProgramService;
import com.eg.testwechatpay.service.WechatPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("miniProgram")
@Slf4j
public class MiniProgramController {
    @Resource
    private MiniProgramService miniProgramService;
    @Resource
    private WechatPayService wechatPayService;

    /**
     * 批量创建小程序码：接口不对外
     *
     * @param amount
     * @return
     */
    @RequestMapping("batchCreateQRCode")
    public List<QRCode> batchCreateQRCode(@RequestParam int amount) {
        return miniProgramService.batchCreateQRCode(amount);
    }

    /**
     * 小程序登录
     *
     * @param js_code
     * @return
     */
    @RequestMapping("login")
    public String login(@RequestParam String js_code) {
        JSONObject login = miniProgramService.login(js_code);
        String openid = login.getString("openid");

        JSONObject result = new JSONObject();
        result.put("openid", openid);
        return result.toJSONString();
    }

    /**
     * 传入小程序码中的场景值，这里相当于是个路由
     */
    @RequestMapping("router")
    public String router(@RequestParam String queryScene) {
        return miniProgramService.route(queryScene);
    }

    /**
     * 请求支付
     *
     * @param openid
     * @param queryScene
     * @return
     */
    @RequestMapping("requestPay")
    public String requestPay(@RequestParam String openid, @RequestParam String queryScene) {
        return miniProgramService.createOrder(openid, queryScene);
    }

    /**
     * 当小程序支付成功时
     */
    @RequestMapping("onPaySuccess")
    public String onPaySuccess(@RequestParam String openid,
                               @RequestParam String queryScene,
                               @RequestParam String orderId) {
        String json = wechatPayService.queryTransactionByOutTradeNo(orderId);
        log.info("查询订单: orderId = {}", orderId);
        log.info("结果为：{}", json);
        return json;
    }
}
