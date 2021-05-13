package com.eg.testwechatpay.controller;

import com.eg.testwechatpay.service.OssService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Map;

@Controller
public class IndexController {
    @Resource
    private OssService ossService;

    @RequestMapping("/")
    public String index(Map<String, String> map) {
        String preSignedUrl = ossService.generatePreSignedUrl("qrcodes/frsntzf97irg.jpg");
        map.put("imageUrl", preSignedUrl);
        return "index";
    }
}
