package com.eg.testwechatpay.repository;

import com.eg.testwechatpay.bean.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    Order findByOrderId(String orderId);

    Order findByQueryScene(String queryScene);
}
