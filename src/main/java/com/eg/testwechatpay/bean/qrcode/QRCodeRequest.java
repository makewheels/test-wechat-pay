package com.eg.testwechatpay.bean.qrcode;

import lombok.Data;

@Data
public class QRCodeRequest {
    private String scene;
    private String page;
    private Integer width;
    private Boolean auto_color;
    private RGB line_color;
    private Boolean is_hyaline;

}
