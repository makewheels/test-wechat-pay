package com.eg.testwechatpay.service;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.eg.testwechatpay.bean.Order;
import com.eg.testwechatpay.bean.OssFile;
import com.eg.testwechatpay.bean.QRCode;
import com.eg.testwechatpay.repository.OrderRepository;
import com.eg.testwechatpay.repository.OssFileRepository;
import com.eg.testwechatpay.repository.QRCodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.BooleanOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PageService {
    @Resource
    private WechatPayService wechatPayService;
    @Resource
    private MiniProgramService miniProgramService;
    @Resource
    private OssService ossService;

    @Resource
    private OrderRepository orderRepository;
    @Resource
    private QRCodeRepository qrCodeRepository;
    @Resource
    private OssFileRepository ossFileRepository;
    @Resource
    private MongoTemplate mongoTemplate;


    public Order createOrder(String itemId) {
        Order order = new Order();
        order.setOrderId(wechatPayService.getOrderId());
        order.setCreateTime(new Date());
        order.setIsPaid(false);

        switch (itemId) {
            case "1":
                order.setName("荧光棒-" + RandomUtil.randomString(4));
                order.setPrice(1);
                break;
            case "2":
                order.setName("糖豆车-" + RandomUtil.randomString(4));
                order.setPrice(2);
                break;
            case "3":
                order.setName("火箭-" + RandomUtil.randomString(4));
                order.setPrice(3);
                break;
        }
        //拿到一个小程序码，先从数据库中查没有使用过的小程序码
        Query query = Query.query(Criteria.where("isUsed").is(false)).limit(1);
        QRCode qrCode = mongoTemplate.findOne(query, QRCode.class);
        //如果没有，那就生成
        if (qrCode == null)
            qrCode = miniProgramService.createQRCode();
        //更新为已使用
        qrCode.setIsUsed(true);
        qrCodeRepository.save(qrCode);
        order.setQrCodeId(qrCode.get_id());
        orderRepository.save(order);
        log.info("创建订单: {}", JSON.toJSONString(order));
        return order;
    }

    public Order findOrderByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    public String getQRCodeUrlByOrder(Order order) {
        //根据订单里的qr id查到qr
        QRCode qrCode = qrCodeRepository.findById(order.getQrCodeId()).get();
        //再根据qr里的oss文件id查到文件objectName
        OssFile ossFile = ossFileRepository.findById(qrCode.getOssFileId()).get();
        //再生成预签名的地址
        return ossService.generatePreSignedUrl(ossFile.getObject());
    }
}
