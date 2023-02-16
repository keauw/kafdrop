package kafdrop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/producer")
public class ProducerController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @PostMapping("/{topicName}/messages")
    public ResponseEntity<Void> sendMessage(@PathVariable("topicName") String topicName
            , @RequestParam(required = false) MultiValueMap<String, String> allRequestParams
            , @RequestBody String messageBody) {

        MessageBuilder<String> message = MessageBuilder
                .withPayload(messageBody)
                .setHeader(KafkaHeaders.TOPIC, topicName);

        if (allRequestParams != null) {
            for (Map.Entry<String, List<String>> paramEntry : allRequestParams.entrySet()) {
                if ("messageKey".equalsIgnoreCase(paramEntry.getKey())) {
                    message.setHeader(KafkaHeaders.MESSAGE_KEY, paramEntry.getValue().get(0));
                } else {
                    message.setHeader(paramEntry.getKey(), paramEntry.getValue().get(0));
                }
            }
        }

        this.kafkaTemplate.send(message.build());

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
