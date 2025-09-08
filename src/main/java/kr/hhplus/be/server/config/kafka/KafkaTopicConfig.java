package kr.hhplus.be.server.config.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableConfigurationProperties({OrderTopicProperties.class, CouponTopicProperties.class})
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final OrderTopicProperties orderTopicProperties;
    private final CouponTopicProperties couponTopicProperties;

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(orderTopicProperties.createdName())
                .partitions(orderTopicProperties.createdPartitions())
                .replicas(orderTopicProperties.createdReplicas())
                .build();
    }

    @Bean
    public NewTopic couponIssueTopic() {
        return TopicBuilder
                .name(couponTopicProperties.issueName())
                .partitions(couponTopicProperties.issuePartitions())
                .replicas(couponTopicProperties.issueReplicas())
                .build();
    }
}

