package com.timegate.kubeproducer.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class TopicUtil {
    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${topic.name}")
    private String topicName;

    @Value("${topic.partitions-num}")
    private Integer topicPartition;

    @Value("${topic.replication-factor}")
    private Integer topicReplicationFactor;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic topicCreate() {
        return TopicBuilder.name(topicName)
                .partitions(topicPartition)
                .replicas(topicReplicationFactor)
                .build();
    }

    public void topicCreate(String topicName, Integer topicPartition) {
        NewTopic newTopic = TopicBuilder.name(topicName)
                .partitions(topicPartition)
                .replicas(topicReplicationFactor)
                .build();
        kafkaAdmin().createOrModifyTopics(newTopic);
        log.info("Create topic {}", newTopic);
    }

    public boolean topicExists(String topicName) {
        try {
            Map<String, TopicDescription> result = kafkaAdmin().describeTopics(topicName);
            return !result.isEmpty();
        } catch (UnknownTopicOrPartitionException e) {
            return false;
        } catch (Exception e) {
            log.error("토픽 조회 실패: {}", topicName, e);
            return false;
        }
    }
}
