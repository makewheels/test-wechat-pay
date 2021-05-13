package com.eg.testwechatpay.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eg.testwechatpay.bean.payresponse.MiniProgramResponse;
import com.eg.testwechatpay.bean.transaction.query.TransactionResult;
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

    @RequestMapping("getJsapiPrepayId")
    public String getJsapiPrepayId() {
        String orderId = wechatPayService.getOrderId();
        return wechatPayService.getJsapiPrepayId(
                "ooJ4W5bJCy6ZtXIsyXbKdxXwoHwI", "的大范围", 1, orderId);
    }

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
        return JSON.toJSONString(wechatPayService.getMiniProgramResponse(prepay_id));
    }

    @RequestMapping("callback")
    public String callback(@RequestBody JSONObject jsonObject) {
        System.out.println("WechatPayController.callback " + jsonObject);
        return "{\"code\": \"SUCCESS\",\"message\": \"成功\"}";
    }

    @RequestMapping("queryTransactionByOutTradeNo")
    public String queryTransactionByOutTradeNo(@RequestParam String out_trade_no) {
        return wechatPayService.queryTransactionByOutTradeNo(out_trade_no);
    }

    @RequestMapping("queryTransactionByWechatTransactionId")
    public String queryTransactionByWechatTransactionId(@RequestParam String transaction_id) {
        String json = wechatPayService.queryTransactionByWechatTransactionId(transaction_id);
        TransactionResult transactionResult = JSON.parseObject(json, TransactionResult.class);
        System.out.println("transactionResult.getTrade_state() = " + transactionResult.getTrade_state());
        return transactionResult.getTransaction_id();
    }
}
