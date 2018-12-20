package com.fshow.id.service.client;

/**
 * @author shijiawei
 * @version IdProduceClient.java, v 0.1
 * @date 2018/12/17
 * id生成客户端
 */
public interface IdProduceClient {

    /**
     * 获取分布式机器唯一id
     * 参数如果没有就传null
     *
     * @return
     */
    String getId(String shardingCode, String obligateCode, String businessCode);

    String getId(String businessCode);
}
