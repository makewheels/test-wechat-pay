package com.eg.testwechatpay.repository;

import com.eg.testwechatpay.bean.QRCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QRCodeRepository extends MongoRepository<QRCode, String> {
    QRCode findByQueryScene(String queryScene);

}
