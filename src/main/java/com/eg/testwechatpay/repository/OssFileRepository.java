package com.eg.testwechatpay.repository;

import com.eg.testwechatpay.bean.OssFile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OssFileRepository extends MongoRepository<OssFile,String> {
}
