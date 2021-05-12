package com.eg.testwechatpay.controller;

import com.alibaba.fastjson.JSONObject;
import com.eg.testwechatpay.service.MiniProgramService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("miniProgram")
public class MiniProgramController {
    @Resource
    private MiniProgramService miniProgramService;

    @RequestMapping("getQRCode")
    public String getQRCode() {
        return miniProgramService.getQRCode();
    }

    @RequestMapping("login")
    public String login(@RequestParam String js_code) {
        String json = miniProgramService.login(js_code);
        JSONObject jsonObject = JSONObject.parseObject(json);
        JSONObject result = new JSONObject();
        result.put("openid", jsonObject.getString("openid"));
        return result.toJSONString();
    }

}
