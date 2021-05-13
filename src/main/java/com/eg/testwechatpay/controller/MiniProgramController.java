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
        JSONObject login = miniProgramService.login(js_code);
        String openid = login.getString("openid");

        JSONObject result = new JSONObject();
        result.put("openid", openid);
        return result.toJSONString();
    }

    /**
     * 传入小程序码中的场景值，这里相当于是个路由
     */
    @RequestMapping("router")
    public String router(@RequestParam String queryScene) {
        return miniProgramService.route(queryScene);
    }
}
