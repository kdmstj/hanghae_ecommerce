package kr.hhplus.be.server.config.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.topics.coupon")
public record CouponTopicProperties(
        String issueName,
        String issueGroupId,
        int issuePartitions,
        int issueReplicas
) {
}
