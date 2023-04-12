package org.example.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.example.model.CouponRecordMessage;
import org.example.service.CouponRecordService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RabbitListener(queues = "${mqconfig.coupon_release_queue}")
public class CouponMQListener {

    @Autowired
    private CouponRecordService couponRecordService;

    @Autowired
    private RedissonClient redissonClient;

    @RabbitHandler
    public void releaseCouponRecord(CouponRecordMessage recordMessage, Message message, Channel channel) throws IOException {

        log.info("监听到消息:releaseCouponRecord消息内容:{}", recordMessage);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        boolean flag = couponRecordService.releaseCouponRecord(recordMessage);

//        RLock lock = redissonClient.getLock("lock:coupon_record_release" + recordMessage.getTaskId());
//        //加锁
//        lock.lock();

        try {
            if (flag) {
                //消费成功，则确认
                channel.basicAck(deliveryTag, false);
            }else {
                log.error("释放优惠券失败 flag=false:{}",recordMessage);
                //消费失败是否重新入队 true为是
                channel.basicReject(deliveryTag,true);
            }

        } catch (IOException e) {
            log.error("释放优惠券记录异常:{},msg:{}",e,recordMessage);
            channel.basicReject(deliveryTag,true);
            throw new RuntimeException(e);
        }
//        finally {
//            //解锁
//            lock.unlock();
//        }
    }


}
