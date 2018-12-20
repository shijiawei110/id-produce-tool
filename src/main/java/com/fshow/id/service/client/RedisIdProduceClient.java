package com.fshow.id.service.client;


import com.fshow.id.service.config.RedisIdProduceConfig;
import com.fshow.id.service.pojo.TimeSeqResult;
import com.fshow.id.service.utils.SystemClock;

/**
 * @author shijiawei
 * @version RedisIdProduceClient.java, v 0.1
 * @date 2018/12/17
 * 使用redis cache的 com.fshow.id.service.client
 * 协议码：毫秒时间戳 + 分库分表码 + 预留码 + 业务码 + 自增序列
 */
public class RedisIdProduceClient extends RedisIdProduceClientBase implements IdProduceClient {

    public RedisIdProduceClient(RedisIdProduceConfig config) {
        super(config);
    }

    @Override
    public String getId(String shardingCode, String obligateCode, String businessCode) {
        //获取分库分表码
        String finalShardingCode = getShardingCode(shardingCode);
        String finalObligateCode = getObligateCode(obligateCode);
        String finalBusinessCode = getBusinessCode(businessCode);
        //获取当前毫秒时
        long currentTime = SystemClock.millisClock().now();
        //获取sequence(2级缓存)
        TimeSeqResult timeSeqResult = new TimeSeqResult();
        timeSeqResult.setTimeStamp(currentTime);
        getSequenceByCache(timeSeqResult);
        String finalCurrentTime = String.valueOf(timeSeqResult.getTimeStamp());
        //补全位数
        return buildId(finalCurrentTime, finalShardingCode, finalObligateCode, finalBusinessCode, timeSeqResult.getSequence());
    }

    @Override
    public String getId(String businessCode) {
        return getId(null, null, businessCode);
    }

    private String buildId(String timeStamp, String shardingCode, String obligateCode, String businessCode, int sequence) {
        StringBuilder builder = new StringBuilder();
        builder.append(timeStamp);
        builder.append(shardingCode);
        builder.append(obligateCode);
        builder.append(businessCode);
        builder.append(supplyRender(sequence));
        return builder.toString();
    }
}
