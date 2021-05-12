package com.eg.testwechatpay.bean.qrcode;

import lombok.Data;

@Data
public class QRCodeRequest {
    private String scene;
    private String page;
    private int width;
    private boolean auto_color;
    private RGB line_color;
    private boolean is_hyaline;

}
