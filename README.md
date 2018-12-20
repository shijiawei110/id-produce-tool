# id-produce-tool
一款分布式id生成 小插件工具
需要依赖redis实现一级缓存
本地缓存作为二级缓存

可以在多机集群毫秒级并发情况下生成唯一的分布式id

##联系方式
如有bug和改进指导意见请告知我哦
wechat : shijiawei110

##客户端注册,可以通过@Bean方式注册单例(或者多例到系统)

        RedisIdProduceConfig config = new RedisIdProduceConfig();
        /**设置缓存的key的基础标识**/
        config.setCacheKeyBase("fshows.test.");
        /**如果需要使用缓存功能,需要设置jedisPool实例**/
        config.setJedisPool(MyJedisPool.getJedisPool());
        /**设置每秒redis缓存的过期时间(单位秒) : 不设置为默认15s**/
        config.setSecondExpire(60);
        /**设置分库分表位数 0位就代表没有 范围0-10 不设置默认为4位**/
        config.setShardingLength(4);
        /**设置预留位数 0就代表不预留  范围 0-10 不设置默认为4位**/
        config.setObligateLength(4);
        /**设置业务码位数 0代表无 范围 0-10 不默认为2位**/
        config.setBusinessLength(2);
        /**设置毫秒自增位数 范围 2-6 默认为4位 必须设置**/
        config.setMsSequenceLength(4);

        /**生成实例**/
        IdProduceClient client = new RedisIdProduceClient(config);  
        
        
##使用api  

        /**99为你的业务码,必须和你设置的长度一致,不然会报错**/    
        /**分库分表码和预留码会填充为默认的字符0**/    
        client.getId("99");
        
        /**依次为分库分表码,预留码和业务码**/ 
        client.getId("1","2","3");
