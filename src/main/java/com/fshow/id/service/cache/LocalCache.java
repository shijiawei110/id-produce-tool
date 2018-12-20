package com.fshow.id.service.cache;

/**
 * @author shijiawei
 * @version LocalCache.java, v 0.1
 * @date 2018/12/18
 * 本地二级缓存
 */
public class LocalCache {

    private Long cacheTime = -1L;
    private int currentSequence = -1;
    private int endSequence = -1;


    public Long getCacheTime() {
        return cacheTime;
    }

    public void setCacheTime(Long cacheTime) {
        this.cacheTime = cacheTime;
    }


    public int getEndSequence() {
        return endSequence;
    }

    public void setEndSequence(int endSequence) {
        this.endSequence = endSequence;
    }

    public int getCurrentSequence() {
        return currentSequence;
    }

    public void setCurrentSequence(int currentSequence) {
        this.currentSequence = currentSequence;
    }
}
