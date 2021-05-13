package com.eg.testwechatpay.controller;

import com.eg.testwechatpay.bean.Order;
import com.eg.testwechatpay.service.PageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.Map;

@Controller
@RequestMapping("page")
public class PageController {
    @Resource
    private PageService pageService;

    @RequestMapping("home")
    public String home() {
        return "index";
    }

    @RequestMapping("createOrder")
    public String createOrder(@RequestParam String itemId) {
        Order order = pageService.createOrder(itemId);
        return "redirect:/page/pay?orderId=" + order.getOrderId();
    }

    @RequestMapping("pay")
    public String buy(@RequestParam String orderId, Map<String, String> map) {
        Order order = pageService.findOrderByOrderId(orderId);
        if (order==null)
            return null;
        String qrCodeUrl = pageService.getQRCodeUrlByOrder(order);
        map.put("name", order.getName());
        map.put("price", order.getPrice() + "");
        map.put("orderId", order.getOrderId());
        map.put("qrCodeUrl", qrCodeUrl);
        if (order.getIsPaid()) {
            return "success";
        } else {
            return "pay";
        }
    }
}
