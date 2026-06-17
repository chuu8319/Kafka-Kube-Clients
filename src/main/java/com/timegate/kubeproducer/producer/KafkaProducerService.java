package com.timegate.kubeproducer.producer;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.FlashMapManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final AtomicLong idCounter = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicBoolean> runningMap = new ConcurrentHashMap<>();
    private final FlashMapManager flashMapManager;

    @Value("${topic.name}")
    private String TOPIC;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(@Nullable String topic, String message) {
        if(topic == null || topic.isEmpty()) {
            topic = this.TOPIC;
        }

        kafkaTemplate.send(topic, String.valueOf(idCounter), message);
        log.info("Message sent to {}: {}: {}", topic, idCounter, message);

        idCounter.incrementAndGet();
    }

    public void sendMessage(@Nullable String topic, long messageCount, long delayTime, String message) throws InterruptedException {
        if(topic == null || topic.isEmpty()) {
            topic = this.TOPIC;
        }

        for(int i = 0; i < messageCount; i++) {
            kafkaTemplate.send(topic, String.valueOf(idCounter), message);
            log.info("Message sent to {}: {}: {}", topic, idCounter, message);

            idCounter.incrementAndGet();
            Thread.sleep(delayTime * 1000);
        }
    }

    @Async
    public void startMessage(@Nullable String topic, long delayTime, String message) {
        if(topic == null || topic.isEmpty()) {
            topic = this.TOPIC;
        }

        runningMap.putIfAbsent(topic, new AtomicBoolean(false));
        AtomicBoolean running = runningMap.get(topic);

        if (!running.compareAndSet(false, true)) {
            log.error("{} is already running", topic);
            return;
        }
        try {
            while (running.get()) {
                kafkaTemplate.send(topic, String.valueOf(idCounter), message);
                log.info("Message sent to {}: {}: {}", topic, idCounter, message);

                Thread.sleep(delayTime * 1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            running.set(false);
            log.info("{} stopped", topic);
        }
    }

    public void stopMessage(@Nullable String topic) {
        if(topic == null || topic.isEmpty()) {
            topic = this.TOPIC;
        }

        AtomicBoolean running = runningMap.get(topic);
        if(running != null) {
            running.set(false);
        } else log.warn("{} is not running", topic);
    }

    public void stopAllMessages() {
        runningMap.values().forEach(flag -> flag.set(false));
    }

    public boolean isRunning(@Nullable String topic) {
        if(topic == null || topic.isEmpty()) {
            topic = this.TOPIC;
        }

        AtomicBoolean running = runningMap.get(topic);
        return running != null && running.get();
    }

    public List<String> statusMessage() {
        return runningMap.entrySet().stream()
                .filter(e -> e.getValue().get())
                .map(Map.Entry::getKey)
                .toList();
    }
}