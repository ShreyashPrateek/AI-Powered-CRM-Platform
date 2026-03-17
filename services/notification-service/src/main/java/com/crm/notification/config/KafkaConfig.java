package com.crm.notification.config;

import com.crm.notification.event.DealEventMessage;
import com.crm.notification.event.LeadEventMessage;
import com.crm.notification.event.UserEventMessage;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ── Topic declarations (idempotent — created only if absent) ─────────────

    @Bean public NewTopic leadEventsTopic() {
        return TopicBuilder.name("lead-events").partitions(3).replicas(1).build();
    }

    @Bean public NewTopic dealEventsTopic() {
        return TopicBuilder.name("deal-events").partitions(3).replicas(1).build();
    }

    @Bean public NewTopic userEventsTopic() {
        return TopicBuilder.name("user-events").partitions(3).replicas(1).build();
    }

    // ── Shared consumer factory ───────────────────────────────────────────────

    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-service");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> factory(Class<T> targetType) {
        Map<String, Object> props = baseConsumerProps();
        JsonDeserializer<T> deserializer = new JsonDeserializer<>(targetType);
        deserializer.addTrustedPackages("com.crm.notification.event");
        deserializer.setUseTypeHeaders(false);

        ConsumerFactory<String, T> cf = new DefaultKafkaConsumerFactory<>(
            props, new StringDeserializer(), deserializer);

        ConcurrentKafkaListenerContainerFactory<String, T> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        factory.setConcurrency(3);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LeadEventMessage> kafkaListenerContainerFactory() {
        return factory(LeadEventMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DealEventMessage> dealKafkaListenerContainerFactory() {
        return factory(DealEventMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserEventMessage> userKafkaListenerContainerFactory() {
        return factory(UserEventMessage.class);
    }
}
