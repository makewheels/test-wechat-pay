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
        return JSON.toJSONString(wechatPayService.getMiniProgramResponse(prepay_id, orderId));
    }

    @RequestMapping("callback")
    public String callback(@RequestBody JSONObject jsonObject) {
        log.info("WechatPayController.callback = " + jsonObject);
        return "{\"code\": \"SUCCESS\",\"message\": \"成功\"}";
    }

    @RequestMapping("queryTransactionByOutTradeNo")
    public JSONObject queryTransactionByOutTradeNo(@RequestParam String out_trade_no) {
        return wechatPayService.queryTransactionByOutTradeNo(out_trade_no);
    }

    @RequestMapping("queryTransactionByWechatTransactionId")
    public JSONObject queryTransactionByWechatTransactionId(@RequestParam String transactionId) {
        return wechatPayService.queryTransactionByWechatTransactionId(transactionId);
    }

    @RequestMapping("createNative")
    public JSONObject createNative() {
        return wechatPayService.createNative();
    }

    @RequestMapping("close")
    public void close(@RequestParam String outTradeNo) {
        wechatPayService.close(outTradeNo);
    }

    @RequestMapping("refund")
    public String refund(@RequestParam String outTradeNo) {
        return wechatPayService.refund(outTradeNo);
    }

    @RequestMapping("queryRefund")
    public JSONObject queryRefund(@RequestParam String outRefundNo) {
        return wechatPayService.queryRefund(outRefundNo);
    }

    @RequestMapping("tradebill")
    public JSONObject tradebill(@RequestParam String billDate) {
        return wechatPayService.tradebill(billDate);
    }
}
