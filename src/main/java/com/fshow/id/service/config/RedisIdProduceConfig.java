package com.fshow.id.service.config;

import com.fshow.id.service.expection.IdProduceClientException;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.JedisPool;

/**
 * @author shijiawei
 * @version RedisIdProduceConfig.java, v 0.1
 * @date 2018/12/17
 * redis com.fshow.id.service.cache id生成客户端配置
 */
public class RedisIdProduceConfig {

    private static final int DEFAULT_SECOND_EXPIRE = 15;
    private static final int MAX_SECOND_EXPIRE = 60;
    private static final int DEFAULT_SHARDING_LENGTH = 4;
    private static final int MAX_SHARDING_LENGTH = 10;
    private static final int DEFAULT_OBLIGATE_LENGTH = 4;
    private static final int MAX_OBLIGATE_LENGTH = 10;
    private static final int DEFAULT_BUSINESS_LENGTH = 2;
    private static final int MAX_BUSINESS_LENGTH = 10;
    private static final int DEFAULT_MS_SEQUENCE_LENGTH = 4;
    private static final int MAX_MS_SEQUENCE_LENGTH = 6;
    private static final int MIN_MS_SEQUENCE_LENGTH = 2;

    public RedisIdProduceConfig(){}

    /**
     * redis缓存key的标识
     **/
    private String cacheKeyBase;
    /**
     * redis 客户端池
     **/
    private JedisPool jedisPool;

    /**
     * 秒级key在redis的过期时间 范围是 1-60秒  默认15秒
     */
    private Integer secondExpire;

    /**
     * 分库分表位数 0位就代表没有 范围0-10 默认为4位
     */
    private Integer shardingLength;

    /**
     * 预留位数 0就代表不预留  范围 0-10 默认为4位
     */
    private Integer obligateLength;

    /**
     * 业务码位数 0代表无 范围 0-10 默认为2位
     */
    private Integer businessLength;

    /**
     * 毫秒自增位数 范围 2-6 默认为4位
     */
    private Integer msSequenceLength;

    /**
     * 毫秒自增序列本地二级缓存 范围 1-毫秒自增位数的最大值(3位就是999)  默认为(位数最大值+1)/20
     */
    private Integer msSequenceLocalSize;


    /**
     * build
     */
    public RedisIdProduceConfig build() {
        //首先检查必填字段
        checkMustParams();
        //检查可空字段范围
        checkParamsRange();
        return this;
    }

    public String getCacheKeyBase() {
        return cacheKeyBase;
    }

    public void setCacheKeyBase(String cacheKeyBase) {
        this.cacheKeyBase = cacheKeyBase;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public Integer getSecondExpire() {
        return secondExpire;
    }

    public void setSecondExpire(Integer secondExpire) {
        this.secondExpire = secondExpire;
    }

    public Integer getShardingLength() {
        return shardingLength;
    }

    public void setShardingLength(Integer shardingLength) {
        this.shardingLength = shardingLength;
    }

    public Integer getObligateLength() {
        return obligateLength;
    }

    public void setObligateLength(Integer obligateLength) {
        this.obligateLength = obligateLength;
    }

    public Integer getBusinessLength() {
        return businessLength;
    }

    public void setBusinessLength(Integer businessLength) {
        this.businessLength = businessLength;
    }

    public Integer getMsSequenceLength() {
        return msSequenceLength;
    }

    public void setMsSequenceLength(Integer msSequenceLength) {
        this.msSequenceLength = msSequenceLength;
    }

    public Integer getMsSequenceLocalSize() {
        return msSequenceLocalSize;
    }

    public void setMsSequenceLocalSize(Integer msSequenceLocalSize) {
        this.msSequenceLocalSize = msSequenceLocalSize;
    }


    private void checkMustParams() {
        if (StringUtils.isBlank(cacheKeyBase)) {
            throw IdProduceClientException.CLIENT_INIT_PARAMS_ERROR;
        }
        if (jedisPool == null) {
            throw IdProduceClientException.CLIENT_INIT_JEDIS_ERROR;
        }
        if (jedisPool.isClosed()) {
            throw IdProduceClientException.CLIENT_INIT_JEDIS_ERROR;
        }
    }

    private void checkParamsRange() {
        if (null == secondExpire) {
            secondExpire = DEFAULT_SECOND_EXPIRE;
        } else if (secondExpire <= 0 || secondExpire > MAX_SECOND_EXPIRE) {
            throw IdProduceClientException.SECOND_EXPIRE_OUT_OF_MAX;
        }

        if (null == shardingLength) {
            shardingLength = DEFAULT_SHARDING_LENGTH;
        } else if (shardingLength < 0 || shardingLength > MAX_SHARDING_LENGTH) {
            throw IdProduceClientException.CLIENT_INIT_PARAMS_ERROR;
        }

        if (null == obligateLength) {
            obligateLength = DEFAULT_OBLIGATE_LENGTH;
        } else if (obligateLength < 0 || obligateLength > MAX_OBLIGATE_LENGTH) {
            throw IdProduceClientException.CLIENT_INIT_PARAMS_ERROR;
        }

        if (null == businessLength) {
            businessLength = DEFAULT_BUSINESS_LENGTH;
        } else if (businessLength < 0 || businessLength > MAX_BUSINESS_LENGTH) {
            throw IdProduceClientException.CLIENT_INIT_PARAMS_ERROR;
        }

        if (null == msSequenceLength) {
            msSequenceLength = DEFAULT_MS_SEQUENCE_LENGTH;
        } else if (msSequenceLength <= MIN_MS_SEQUENCE_LENGTH || msSequenceLength > MAX_MS_SEQUENCE_LENGTH) {
            throw IdProduceClientException.CLIENT_INIT_PARAMS_ERROR;
        }

        //计算 每毫秒自增序号的 最大值
        int base = 1;
        for (int i = 0; i < msSequenceLength; i++) {
            base *= 10;
        }
        int maxSequence = base - 1;

        if (null == msSequenceLocalSize) {
            msSequenceLocalSize = base / 20;
        } else if (msSequenceLocalSize <= 0 || msSequenceLocalSize > maxSequence) {
            throw IdProduceClientException.CLIENT_INIT_PARAMS_ERROR;
        }
    }

}
