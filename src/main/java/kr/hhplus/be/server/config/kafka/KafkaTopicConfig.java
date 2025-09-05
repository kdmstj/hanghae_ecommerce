package kr.hhplus.be.server.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder
                .name("order.created")
                .partitions(3)
                .build();
    }

    @Bean
    public NewTopic couponIssueTopic(){
        return TopicBuilder
                .name("coupon.issue")
                .partitions(3)
                .build();
    }
}

