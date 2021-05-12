/**
  * Copyright 2021 json.cn 
  */
package com.eg.testwechatpay.bean.transaction.query;

/**
 * Auto-generated: 2021-05-12 14:4:23
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
public class Amount {

    private String currency;
    private String payer_currency;
    private int payer_total;
    private int total;
    public void setCurrency(String currency) {
         this.currency = currency;
     }
     public String getCurrency() {
         return currency;
     }

    public void setPayer_currency(String payer_currency) {
         this.payer_currency = payer_currency;
     }
     public String getPayer_currency() {
         return payer_currency;
     }

    public void setPayer_total(int payer_total) {
         this.payer_total = payer_total;
     }
     public int getPayer_total() {
         return payer_total;
     }

    public void setTotal(int total) {
         this.total = total;
     }
     public int getTotal() {
         return total;
     }

}