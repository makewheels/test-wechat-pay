/**
  * Copyright 2021 json.cn 
  */
package com.eg.testwechatpay.wechat.transaction;
import java.util.List;
import java.util.Date;

/**
 * Auto-generated: 2021-05-12 14:4:23
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
public class TransactionResult {

    private Amount amount;
    private String appid;
    private String attach;
    private String bank_type;
    private String mchid;
    private String out_trade_no;
    private Payer payer;
    private List<String> promotion_detail;
    private Date success_time;
    private String trade_state;
    private String trade_state_desc;
    private String trade_type;
    private String transaction_id;
    public void setAmount(Amount amount) {
         this.amount = amount;
     }
     public Amount getAmount() {
         return amount;
     }

    public void setAppid(String appid) {
         this.appid = appid;
     }
     public String getAppid() {
         return appid;
     }

    public void setAttach(String attach) {
         this.attach = attach;
     }
     public String getAttach() {
         return attach;
     }

    public void setBank_type(String bank_type) {
         this.bank_type = bank_type;
     }
     public String getBank_type() {
         return bank_type;
     }

    public void setMchid(String mchid) {
         this.mchid = mchid;
     }
     public String getMchid() {
         return mchid;
     }

    public void setOut_trade_no(String out_trade_no) {
         this.out_trade_no = out_trade_no;
     }
     public String getOut_trade_no() {
         return out_trade_no;
     }

    public void setPayer(Payer payer) {
         this.payer = payer;
     }
     public Payer getPayer() {
         return payer;
     }

    public void setPromotion_detail(List<String> promotion_detail) {
         this.promotion_detail = promotion_detail;
     }
     public List<String> getPromotion_detail() {
         return promotion_detail;
     }

    public void setSuccess_time(Date success_time) {
         this.success_time = success_time;
     }
     public Date getSuccess_time() {
         return success_time;
     }

    public void setTrade_state(String trade_state) {
         this.trade_state = trade_state;
     }
     public String getTrade_state() {
         return trade_state;
     }

    public void setTrade_state_desc(String trade_state_desc) {
         this.trade_state_desc = trade_state_desc;
     }
     public String getTrade_state_desc() {
         return trade_state_desc;
     }

    public void setTrade_type(String trade_type) {
         this.trade_type = trade_type;
     }
     public String getTrade_type() {
         return trade_type;
     }

    public void setTransaction_id(String transaction_id) {
         this.transaction_id = transaction_id;
     }
     public String getTransaction_id() {
         return transaction_id;
     }

}