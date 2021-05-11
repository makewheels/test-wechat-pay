package com.eg.testwechatpay;

import com.alibaba.fastjson.JSON;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public String createOrder() {
        String orderId = wechatPayService.getOrderId();
        String prepay_id = wechatPayService.getJsapiPrepayId(
                "ooJ4W5bJCy6ZtXIsyXbKdxXwoHwI", "的大范围", 1, orderId);
        return JSON.toJSONString(wechatPayService.getMiniProgramResponse(prepay_id));
    }

    @RequestMapping("callback")
    public String callback() {
        return null;
    }
}
