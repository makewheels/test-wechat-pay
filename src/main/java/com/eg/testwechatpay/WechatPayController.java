package com.eg.testwechatpay;

import com.alibaba.fastjson.JSON;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public String createOrder(@RequestParam String openid) {
        String orderId = wechatPayService.getOrderId();
        String prepay_id = wechatPayService.getJsapiPrepayId(
                openid, "的大范围", 1, orderId);
        return JSON.toJSONString(wechatPayService.getMiniProgramResponse(prepay_id));
    }

    @RequestMapping("callback")
    public String callback() {
        System.out.println("WechatPayController.callback");
        return "{\"code\": \"SUCCESS\",\"message\": \"成功\"}";
    }
}
