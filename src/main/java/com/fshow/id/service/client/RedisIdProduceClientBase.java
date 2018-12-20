package com.fshow.id.service.client;


import com.fshow.id.service.cache.LocalCache;
import com.fshow.id.service.config.RedisIdProduceConfig;
import com.fshow.id.service.expection.IdProduceClientException;
import com.fshow.id.service.pojo.TimeSeqResult;
import com.fshow.id.service.utils.SystemClock;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author shijiawei
 * @version RedisIdProduceClient.java, v 0.1
 * @date 2018/12/17
 */
public class RedisIdProduceClientBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisIdProduceClientBase.class);

    /**
     * 空填充位符号
     **/
    private static final String DEFAULT_REND_FLAG = "0";
    /**
     * 补位填充符号
     */
    private static final String SUPPLEMENT_REND_FLAG = "0";
    /**
     * 空字符填充
     */
    private static final String EMPTY = "";

    /**
     * 二级缓存 复用缓存 的毫秒偏差 (1000ms)
     */
    private static final int LOCAL_CACHE_MS_DEVIATION = 1000;

    RedisIdProduceClientBase(RedisIdProduceConfig config) {
        //check 合法性
        config.build();
        //渲染参数
        cacheKeyBase = config.getCacheKeyBase();
        jedisPool = config.getJedisPool();
        secondExpire = config.getSecondExpire();

        shardingLength = config.getShardingLength();
        if (shardingLength == 0) {
            isShardind = false;
        }
        obligateLength = config.getObligateLength();
        if (obligateLength == 0) {
            isObligate = false;
        }
        businessLength = config.getBusinessLength();
        if (businessLength == 0) {
            isBusiness = false;
        }
        msSequenceLength = config.getMsSequenceLength();
        msSequenceLocalSize = config.getMsSequenceLocalSize();
        int max = 1;
        for (int i = 0; i < msSequenceLength; i++) {
            max *= 10;
        }
        msSequenceMax = max;
        //生成默认填充
        renderDefaultLocation();
        //生成本地缓存
        localCache = new LocalCache();
    }


    private LocalCache localCache;

    private String defaultShardingCode;
    private String defaultObligateCode;
    private String defaultBusinessCode;

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
    private boolean isShardind = true;

    /**
     * 预留位数 0就代表不预留  范围 0-10 默认为4位
     */
    private Integer obligateLength;
    private boolean isObligate = true;

    /**
     * 业务码位数 0代表无 范围 0-10 默认为2位
     */
    private Integer businessLength;
    private boolean isBusiness = true;

    /**
     * 毫秒自增位数 范围 2-6 默认为4位
     */
    private Integer msSequenceLength;
    /**
     * 毫秒自增最大值
     */
    private Integer msSequenceMax;

    /**
     * 毫秒自增序列本地二级缓存 范围 0-毫秒自增位数的最大值(3位就是999)  默认为(位数最大值+1)/20
     * 如果为0的话就关闭二级缓存
     */
    private Integer msSequenceLocalSize;

    /**
     * 生成默认的填充
     */
    private void renderDefaultLocation() {
        if (isShardind) {
            defaultShardingCode = getRenderDefault(shardingLength);
        }
        if (isObligate) {
            defaultObligateCode = getRenderDefault(obligateLength);
        }
        if (isBusiness) {
            defaultBusinessCode = getRenderDefault(businessLength);
        }
    }

    private String getRenderDefault(int size) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            builder.append(DEFAULT_REND_FLAG);
        }
        return builder.toString();
    }

    /**
     * 获取分库分表码
     */
    String getShardingCode(String currentShardingCode) {
        if (isShardind) {
            if (StringUtils.isBlank(currentShardingCode)) {
                return defaultShardingCode;
            }
            //校验位数是否合法
            if (currentShardingCode.length() != shardingLength) {
                throw IdProduceClientException.SHARDIND_PARAM_LENGTH_ERROR;
            }
            return currentShardingCode;
        } else {
            return EMPTY;
        }
    }

    /**
     * 获取预留位码
     */
    String getObligateCode(String currentObligateCode) {
        if (isObligate) {
            if (StringUtils.isBlank(currentObligateCode)) {
                return defaultObligateCode;
            }
            //校验位数是否合法
            if (currentObligateCode.length() != obligateLength) {
                throw IdProduceClientException.OBLIGATE_PARAM_LENGTH_ERROR;
            }
            return currentObligateCode;
        } else {
            return EMPTY;
        }
    }

    /**
     * 获取业务码
     */
    String getBusinessCode(String currentBusinessCode) {
        if (isBusiness) {
            if (StringUtils.isBlank(currentBusinessCode)) {
                return defaultBusinessCode;
            }
            //校验位数是否合法
            if (currentBusinessCode.length() != businessLength) {
                throw IdProduceClientException.BUSINESS_PARAM_LENGTH_ERROR;
            }
            return currentBusinessCode;
        } else {
            return EMPTY;
        }
    }

    /**
     * 根据当前毫秒时从2级缓存中试图拉取序号
     */
    synchronized void getSequenceByCache(TimeSeqResult timeSeqResult) {
        //首先尝试拉取本地缓存
        boolean flag = tryGetSeqByLocalCache(timeSeqResult);
        if (flag) {
            return;
        }
        //本地缓存失效或者不存在 拉取redis缓存并且更新本地缓存
        reGetCacheFromRedis(timeSeqResult);
    }

    /**
     * 尝试获取本地缓存 如果失败,则重新拉取redis缓存
     *
     * @return
     */
    private boolean tryGetSeqByLocalCache(TimeSeqResult timeSeqResult) {
        long currentTime = timeSeqResult.getTimeStamp();
        int currentSeq = localCache.getCurrentSequence();
        //校验是否超过一批次的限制,超过需要重新拉取redis缓存
        if (currentSeq >= localCache.getEndSequence()) {
            return false;
        }
        //如在偏差值内 则继续使用二级缓存
        long dif = currentTime - localCache.getCacheTime();
        if (dif >= 0 && dif <= LOCAL_CACHE_MS_DEVIATION) {
            localCache.setCurrentSequence(currentSeq + 1);
            //重置时间
            timeSeqResult.setTimeStamp(localCache.getCacheTime());
            timeSeqResult.setSequence(currentSeq);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 拉取redis缓存并且更新本地缓存
     */
    private void reGetCacheFromRedis(TimeSeqResult timeSeqResult) {
        long currentTime = timeSeqResult.getTimeStamp();
        //获取当前秒
        long second = currentTime / 1000;
        //获取当前截取的三位毫秒
        long millsecond = currentTime % 1000;
        String hash = String.valueOf(millsecond);
        //拼接redis key
        String key = cacheKeyBase + second;
        Jedis jedis = jedisPool.getResource();
        try {
            //获取缓存值
            long currentRedisEnd = jedis.hincrBy(key, hash, msSequenceLocalSize);
            //计算当前开始序号
            int startSeq = (int) currentRedisEnd - msSequenceLocalSize;
            //如果超过最大的序号
            if (startSeq >= msSequenceMax) {
                throw IdProduceClientException.OUT_OF_MAX_SEQUENCE;
            }
            //重置本地缓存
            localCache.setCacheTime(currentTime);
            localCache.setCurrentSequence(startSeq + 1);
            localCache.setEndSequence((int) currentRedisEnd);
            timeSeqResult.setSequence(startSeq);
        } catch (Exception e) {
            LOGGER.error("id produce com.fshow.id.service.client reGetCacheFromRedis error e={},stack={}", e.getMessage(), ExceptionUtils.getStackTrace(e));
            throw e;
        } finally {
            if (jedis != null) {
                jedis.expire(key, secondExpire);
                jedis.close();
            } else {
                //补偿操作 重试
                Jedis jedisR = jedisPool.getResource();
                jedisR.expire(key, secondExpire);
                jedisR.close();
            }
        }
    }


    /**
     * 补位
     */
    String supplyRender(int sequence) {
        String sequenceStr = String.valueOf(sequence);
        int length = sequenceStr.length();
        if (length < msSequenceLength) {
            int dif = msSequenceLength - length;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < dif; i++) {
                builder.append(SUPPLEMENT_REND_FLAG);
            }
            builder.append(sequenceStr);
            return builder.toString();
        }
        return sequenceStr;
    }


}
