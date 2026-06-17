package com.timegate.kubeproducer.controller;

import com.timegate.kubeproducer.producer.KafkaProducerService;
import com.timegate.kubeproducer.util.TopicUtil;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class KafkaController {
    private final KafkaProducerService kafkaProducerService;
    private final TopicUtil topicUtil;

    @PostMapping(path = "/pub", params = {"!count"})
    public void sendMessage(@RequestParam(value = "topic", required = false) String topic, @RequestBody() String message) {
        if(topic == null || topic.isEmpty()) {
            if (message == null || message.isEmpty()) {
                log.error("Empty Input Data");
            } else {
                log.info("Input Data: name: {}", message);
                kafkaProducerService.sendMessage(topic, message);
            }
        } else {
            if (message == null || message.isEmpty()) {
                log.error("Empty Input Data");
            }
            if (!topicUtil.topicExists(topic)) {
                topicUtil.topicCreate(topic, 3);
                log.info("Create Topic: name: {}", topic);
            } else log.info("Exist Topic: name: {}", topic);

            log.info("Input Data: name: {}", message);
            kafkaProducerService.sendMessage(topic, message);
        }
    }

    @PostMapping(path = "/pub", params = {"count", "delay"})
    public void sendMessage(@RequestParam(value = "topic", required = false) String topic, @RequestParam("count") long messageCount, @RequestParam("delay") long delayTime, @RequestBody() String message) throws InterruptedException {
        if(topic == null || topic.isEmpty()) {
                    if (message == null || message.isEmpty()) {
                        log.error("Empty Input Data");
                    } else {
                        log.info("Input Data: name: {}", message);
                        kafkaProducerService.sendMessage(topic, messageCount, delayTime, message);
            }
        } else {
            if (message == null || message.isEmpty()) {
                log.error("Empty Input Data");
            }
            if (!topicUtil.topicExists(topic)) {
                topicUtil.topicCreate(topic, 3);
                log.info("Create Topic: name: {}", topic);
            } else log.info("Exist Topic: name: {}", topic);

            log.info("Input Data: name: {}", message);
            kafkaProducerService.sendMessage(topic, messageCount, delayTime, message);
        }
    }

    @PostMapping("/topic")
    public void createTopic(@RequestParam("topic") String topicName,@RequestParam(value = "partition", required = false) Integer partition) {
        if(topicUtil.topicExists(topicName)) {
            log.error("Exist Topic: name: {}", topicName);
        }
        else {
            if(partition == null || partition.equals(0)) {
                topicUtil.topicCreate(topicName, 3);
            } else {
                topicUtil.topicCreate(topicName, partition);
            }
        }
    }

    @PostMapping("/pub/start")
    public ResponseEntity<String> startMessage(@RequestParam(value = "topic", required = false) String topicName, @RequestParam("delay") long delayTime, @RequestBody() String message) {
        if(kafkaProducerService.isRunning(topicName)) {
           log.warn("{} is already running", topicName);
           return ResponseEntity.badRequest().body("already running");
        }

        if (!topicUtil.topicExists(topicName)) {
            topicUtil.topicCreate(topicName, 3);
            log.info("Create Topic: name: {}", topicName);
        } else log.info("Exist Topic: name: {}", topicName);

        kafkaProducerService.startMessage(topicName, delayTime, message);
        return ResponseEntity.ok("start");
    }

    @PostMapping("/pub/stop")
    public ResponseEntity<String> stopMessage(@RequestParam(value = "topic", required = false) String topicName) {
        if(topicName == null || topicName.isEmpty()) {
            kafkaProducerService.stopAllMessages();
            return ResponseEntity.ok("stop all messages");
        } else {
            if(!kafkaProducerService.isRunning(topicName)) {
                log.warn("{} is not running", topicName);
                return ResponseEntity.badRequest().body("not running");
            }

            kafkaProducerService.stopMessage(topicName);
            return ResponseEntity.ok("stop");
        }
    }

    @PostMapping("/pub/status")
    public ResponseEntity<List<String>> statusMessage() {
        return ResponseEntity.ok(kafkaProducerService.statusMessage());
    }
}
