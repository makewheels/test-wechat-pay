package com.example.testwechatpay.test;

import com.alibaba.fastjson.JSON;
import com.example.testwechatpay.MyConfig;
import com.github.wxpay.sdk.WXPay;

import java.util.HashMap;
import java.util.Map;

public class WXPayExample {
    public static void main(String[] args) throws Exception {

        MyConfig config = new MyConfig();
        WXPay wxpay = new WXPay(config);

        Map<String, String> data = new HashMap<>();

        data.put("body", "腾讯充值中心-QQ会员充值");
        data.put("out_trade_no", System.currentTimeMillis() + "");
        data.put("total_fee", "1");
        data.put("notify_url", "http://www.example.com/wxpay/notify");
        data.put("trade_type", "JSAPI");
        data.put("product_id", "12");
        data.put("openid", "ooJ4W5bJCy6ZtXIsyXbKdxXwoHwI");

        try {
            Map<String, String> resp = wxpay.unifiedOrder(data);
            System.out.println(JSON.toJSONString(resp));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
