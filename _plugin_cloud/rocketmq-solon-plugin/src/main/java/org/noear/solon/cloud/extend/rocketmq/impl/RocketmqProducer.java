package org.noear.solon.cloud.extend.rocketmq.impl;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.noear.solon.Utils;
import org.noear.solon.cloud.CloudProps;
import org.noear.solon.cloud.extend.rocketmq.RocketmqProps;
import org.noear.solon.cloud.model.Event;

import java.util.Properties;

/**
 * @author noear
 * @since 1.3
 */
public class RocketmqProducer {
    RocketmqConfig cfg;
    DefaultMQProducer producer;

    public RocketmqProducer(RocketmqConfig config) {
        cfg = config;
    }

    private void init(CloudProps cloudProps) throws MQClientException {
        if (producer != null) {
            return;
        }

        synchronized (this) {
            if (producer != null) {
                return;
            }


            producer = new DefaultMQProducer();
            //服务地址
            producer.setNamesrvAddr(cfg.server);
            //生产组
            producer.setProducerGroup(cfg.producerGroup);
            //命名空间
            if (Utils.isNotEmpty(cfg.namespace)) {
                producer.setNamespace(cfg.namespace);
            }

            //发送超时时间，默认3000 单位ms
            if (cfg.timeout > 0) {
                producer.setSendMsgTimeout((int) cfg.timeout);
            }
            //失败后重试2次
            producer.setRetryTimesWhenSendFailed(2);

            //绑定定制属性
            Properties props = cloudProps.getEventProducerProps();
            if (props.size() > 0) {
                Utils.injectProperties(producer, props);
            }

            producer.start();
        }
    }

    public boolean publish(CloudProps cloudProps, Event event, String topic) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        init(cloudProps);

        Message message = MessageUtil.buildNewMeaage(event, topic);

        SendResult send = producer.send(message);

        if (send.getSendStatus().equals(SendStatus.SEND_OK)) {
            return true;
        } else {
            return false;
        }
    }
}
