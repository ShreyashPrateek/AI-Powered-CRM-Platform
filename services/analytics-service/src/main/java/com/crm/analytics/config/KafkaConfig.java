package com.crm.analytics.config;

import com.crm.analytics.event.DealEventMessage;
import com.crm.analytics.event.LeadEventMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> baseConsumerProps(String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.crm.*");
        return props;
    }

    // ── Deal ──────────────────────────────────────────────────────────────────

    @Bean
    public ConsumerFactory<String, DealEventMessage> dealConsumerFactory() {
        Map<String, Object> props = baseConsumerProps("analytics-deal-group");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(),
                new JsonDeserializer<>(DealEventMessage.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DealEventMessage> dealKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DealEventMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(dealConsumerFactory());
        return factory;
    }

    // ── Lead ──────────────────────────────────────────────────────────────────

    @Bean
    public ConsumerFactory<String, LeadEventMessage> leadConsumerFactory() {
        Map<String, Object> props = baseConsumerProps("analytics-lead-group");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(),
                new JsonDeserializer<>(LeadEventMessage.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LeadEventMessage> leadKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, LeadEventMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(leadConsumerFactory());
        return factory;
    }
}
