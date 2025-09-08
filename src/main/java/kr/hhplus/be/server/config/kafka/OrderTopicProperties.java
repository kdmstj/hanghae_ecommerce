package kr.hhplus.be.server.config.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.topics.order")
public record OrderTopicProperties(
        String createdName,
        String createdGroupId,
        int createdPartitions,
        int createdReplicas
) {
}
