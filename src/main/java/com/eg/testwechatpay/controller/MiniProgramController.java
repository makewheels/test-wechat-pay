package com.eg.testwechatpay.controller;

import com.eg.testwechatpay.service.MiniProgramService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class MiniProgramController {
    @Resource
    private MiniProgramService miniProgramService;

    @RequestMapping("getQRCode")
    public String getQRCode() {
        return miniProgramService.getQRCode();
    }
}
