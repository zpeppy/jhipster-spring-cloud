package com.example.microservice.web.rest;

import com.example.microservice.config.KafkaProperties;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Kafka 接口
 *
 * @author peppy
 */
@Api(value = "Kafka", tags = "Kafka")
@RestController
@RequestMapping("/api/microservice-kafka")
public class MicroserviceKafkaResource {

    private final Logger log = LoggerFactory.getLogger(MicroserviceKafkaResource.class);

    private final KafkaProperties kafkaProperties;
    private KafkaProducer<String, String> producer;
    private ExecutorService sseExecutorService = Executors.newCachedThreadPool();

    public MicroserviceKafkaResource(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
        this.producer = new KafkaProducer<>(kafkaProperties.getProducerProps());
    }

    @ApiOperation(value = "发送消息", tags = "Kafka")
    @PostMapping("/publish/{topic}")
    public PublishResult publish(@PathVariable String topic, @RequestParam String message, @RequestParam(required = false) String key) throws ExecutionException, InterruptedException {
        log.debug("REST request to send to Kafka topic {} with key {} the message : {}", topic, key, message);
        RecordMetadata metadata = producer.send(new ProducerRecord<>(topic, key, message)).get();
        return new PublishResult(metadata.topic(), metadata.partition(), metadata.offset(), Instant.ofEpochMilli(metadata.timestamp()));
    }

    @ApiOperation(value = "接收消息", tags = "Kafka")
    @GetMapping("/consume")
    public SseEmitter consume(@RequestParam("topic") List<String> topics, @RequestParam Map<String, String> consumerParams) {
        log.debug("REST request to consume records from Kafka topics {}", topics);
        Map<String, Object> consumerProps = kafkaProperties.getConsumerProps();
        consumerProps.putAll(consumerParams);
        consumerProps.remove("topic");

        SseEmitter emitter = new SseEmitter(0L);
        sseExecutorService.execute(() -> {
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
            emitter.onCompletion(consumer::close);
            consumer.subscribe(topics);
            boolean exitLoop = false;
            while (!exitLoop) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
                    for (ConsumerRecord<String, String> record : records) {
                        emitter.send(record.value());
                    }
                    emitter.send(SseEmitter.event().comment(""));
                } catch (Exception ex) {
                    log.trace("Complete with error {}", ex.getMessage(), ex);
                    emitter.completeWithError(ex);
                    exitLoop = true;
                }
            }
            consumer.close();
            emitter.complete();
        });
        return emitter;
    }

    @ApiModel(value = "发送结果", description = "发送结果")
    @AllArgsConstructor
    private static class PublishResult implements Serializable {
        private static final long serialVersionUID = -8342165324944221619L;

        @ApiModelProperty("主题")
        public final String topic;

        @ApiModelProperty("分区")
        public final int partition;

        @ApiModelProperty("偏移量")
        public final long offset;

        @ApiModelProperty("时间戳")
        public final Instant timestamp;
    }
}
