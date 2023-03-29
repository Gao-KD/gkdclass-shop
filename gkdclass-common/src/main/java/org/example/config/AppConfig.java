package org.example.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class AppConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private String redisPort;

    @Value("${spring.redis.password}")
    private String redisPwd;



    /**
     * 配置分布式锁
     * @return
     */
    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        //单机模式
        //config.useSingleServer().setPassword("123456Gdclass").setAddress("redis://110.40.152.57:6379");
        config.useSingleServer().setPassword(redisPwd).setAddress("redis://"+redisHost+":"+redisPort);

        //集群模式
        //config.useClusterServers()
        //.setScanInterval(2000)
        //.addNodeAddress("redis://110.40.152.57:6379", "redis://110.40.152.58:6379")
        // .addNodeAddress("redis://127.0.0.1:6379");

        RedissonClient redisson = Redisson.create(config);

        return redisson;
    }
}
