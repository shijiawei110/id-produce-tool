/**
 * fshows.com
 * Copyright (C) 2013-2018 All Rights Reserved.
 */
package com.fshow.id.service.expection;

import java.text.MessageFormat;

/**
 * @author shijw
 * @version IdProduceClientException.java, v 0.1 2018-12-12 20:05 shijw
 * id生成器异常类
 */
public class IdProduceClientException extends RuntimeException {
    /**
     * 参数异常
     */
    public static final IdProduceClientException CLIENT_INIT_PARAMS_ERROR = new IdProduceClientException(90001, "id生成客户端初始化参数错误");
    public static final IdProduceClientException CLIENT_INIT_JEDIS_ERROR = new IdProduceClientException(90002, "id生成客户端无法初始化jedis-pool");
    public static final IdProduceClientException SHARDIND_PARAM_LENGTH_ERROR = new IdProduceClientException(90003, "分库分表码长度和设置长度不一致");
    public static final IdProduceClientException OBLIGATE_PARAM_LENGTH_ERROR = new IdProduceClientException(90004, "预留位长度和设置长度不一致");
    public static final IdProduceClientException BUSINESS_PARAM_LENGTH_ERROR = new IdProduceClientException(90005, "业务码长度和设置长度不一致");
    public static final IdProduceClientException SECOND_EXPIRE_OUT_OF_MAX = new IdProduceClientException(90006, "超过最大客户端每秒redis-key过期限制");
    public static final IdProduceClientException OUT_OF_MAX_SEQUENCE = new IdProduceClientException(90999, "已经超过单毫秒最大的序号值");


    /**
     * 异常信息
     */
    protected String msg;

    /**
     * 具体异常码
     */
    protected int code;

    /**
     * 异常构造器
     *
     * @param code      代码
     * @param msgFormat 消息模板,内部会用MessageFormat拼接，模板类似：userid={0},message={1},date{2}
     * @param args      具体参数的值
     */
    private IdProduceClientException(int code, String msgFormat, Object... args) {
        super(MessageFormat.format(msgFormat, args));
        this.code = code;
        this.msg = MessageFormat.format(msgFormat, args);
    }

    /**
     * 默认构造器
     */
    private IdProduceClientException() {
        super();
    }

    /**
     * 异常构造器
     *
     * @param message
     * @param cause
     */
    private IdProduceClientException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 异常构造器
     *
     * @param cause
     */
    private IdProduceClientException(Throwable cause) {
        super(cause);
    }

    /**
     * 异常构造器
     *
     * @param message
     */
    private IdProduceClientException(String message) {
        super(message);
    }

    /**
     * 实例化异常
     *
     * @return 异常类
     */
    public IdProduceClientException newInstance(String msgFormat, Object... args) {
        return new IdProduceClientException(this.code, msgFormat, args);
    }


}
