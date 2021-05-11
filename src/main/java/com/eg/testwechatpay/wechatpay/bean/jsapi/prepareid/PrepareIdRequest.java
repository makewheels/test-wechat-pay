/**
  * Copyright 2021 json.cn 
  */
package com.eg.testwechatpay.wechatpay.bean.jsapi.prepareid;

/**
 * Auto-generated: 2021-05-11 22:37:2
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
public class PrepareIdRequest {

    private String appid;
    private String mchid;
    private String description;
    private String out_trade_no;
    private String time_expire;
    private String attach;
    private String notify_url;
    private Amount amount;
    private Payer payer;
    public void setAppid(String appid) {
         this.appid = appid;
     }
     public String getAppid() {
         return appid;
     }

    public void setMchid(String mchid) {
         this.mchid = mchid;
     }
     public String getMchid() {
         return mchid;
     }

    public void setDescription(String description) {
         this.description = description;
     }
     public String getDescription() {
         return description;
     }

    public void setOut_trade_no(String out_trade_no) {
         this.out_trade_no = out_trade_no;
     }
     public String getOut_trade_no() {
         return out_trade_no;
     }

    public void setTime_expire(String time_expire) {
         this.time_expire = time_expire;
     }
     public String getTime_expire() {
         return time_expire;
     }

    public void setAttach(String attach) {
         this.attach = attach;
     }
     public String getAttach() {
         return attach;
     }

    public void setNotify_url(String notify_url) {
         this.notify_url = notify_url;
     }
     public String getNotify_url() {
         return notify_url;
     }

    public void setAmount(Amount amount) {
         this.amount = amount;
     }
     public Amount getAmount() {
         return amount;
     }

    public void setPayer(Payer payer) {
         this.payer = payer;
     }
     public Payer getPayer() {
         return payer;
     }

}