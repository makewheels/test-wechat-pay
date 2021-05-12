package com.eg.testwechatpay.controller;

import com.alibaba.fastjson.JSON;
import com.eg.testwechatpay.bean.transaction.query.TransactionResult;
import com.eg.testwechatpay.service.WechatPayService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("wechatPay")
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
    public String createOrder(@RequestParam String openid) {
        String orderId = wechatPayService.getOrderId();
        String prepay_id = wechatPayService.getJsapiPrepayId(
                openid, "的大范围", 1, orderId);
        return JSON.toJSONString(wechatPayService.getMiniProgramResponse(prepay_id));
    }

    @RequestMapping("callback")
    public String callback(@RequestBody String body) {
        System.out.println("WechatPayController.callback " + body);
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
