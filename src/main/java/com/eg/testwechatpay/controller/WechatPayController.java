package com.eg.testwechatpay.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eg.testwechatpay.wechat.transaction.Transaction;
import com.eg.testwechatpay.service.WechatPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("wechatPay")
@Slf4j
public class WechatPayController {
    @Resource
    private WechatPayService wechatPayService;

    /**
     * 测试接口不开放
     *
     * @return
     */
    @RequestMapping("getJsapiPrepayId")
    public String getJsapiPrepayId() {
        String orderId = wechatPayService.getOrderId();
        return wechatPayService.getJsapiPrepayId(
                "ooJ4W5bJCy6ZtXIsyXbKdxXwoHwI", "的大范围", 1, orderId);
    }

    /**
     * 不对外开放，仅测试接口
     *
     * @param openid
     * @param queryScene
     * @return
     */
    @RequestMapping("createOrder")
    public String createOrder(@RequestParam String openid, @RequestParam String queryScene) {
        String orderId = wechatPayService.getOrderId();
        log.info("创建订单：orderId = " + orderId);
        String prepay_id = wechatPayService.getJsapiPrepayId(
                openid, "的大范围", 1, orderId);
        if (prepay_id == null) {
            log.warn("创建预付订单失败，不再生成小程序支付所需提交信息");
            return null;
        }
        return JSON.toJSONString(wechatPayService.getMiniProgramResponse(prepay_id, orderId));
    }

    @RequestMapping("callback")
    public String callback(@RequestBody JSONObject jsonObject) {
        System.out.println("WechatPayController.callback = " + jsonObject);
        return "{\"code\": \"SUCCESS\",\"message\": \"成功\"}";
    }

    /**
     * 测试接口不开放
     *
     * @param out_trade_no
     * @return
     */
    @RequestMapping("queryTransactionByOutTradeNo")
    public String queryTransactionByOutTradeNo(@RequestParam String out_trade_no) {
        return wechatPayService.queryTransactionByOutTradeNo(out_trade_no);
    }

    /**
     * 测试接口不开放
     *
     * @param transaction_id
     * @return
     */
    @RequestMapping("queryTransactionByWechatTransactionId")
    public String queryTransactionByWechatTransactionId(@RequestParam String transaction_id) {
        String json = wechatPayService.queryTransactionByWechatTransactionId(transaction_id);
        Transaction transaction = JSON.parseObject(json, Transaction.class);
        System.out.println("transactionResult.getTrade_state() = " + transaction.getTrade_state());
        return transaction.getTransaction_id();
    }
}
