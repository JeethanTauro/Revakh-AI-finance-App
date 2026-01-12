package finance_service.revakh.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import finance_service.revakh.events.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventPublisher {

    private final static String TRANSACTION_CREATED_KEY = "finance.transaction.created";
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public void transactionCreatedPublisher(TransactionCreatedEvent transactionCreatedEvent){
        try{
            String message = objectMapper.writeValueAsString(transactionCreatedEvent);
            rabbitTemplate.convertAndSend(RabbitMqConfig.FINANCE_EXCHANGE, TRANSACTION_CREATED_KEY,message,msg -> {
                msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return msg;
            });
            System.out.println("Published event: " + message);
        }catch (JsonProcessingException e){
            // 6. Log ONLY. Do not crash the user's request.
            log.error("❌ Failed to publish transaction event to RabbitMQ", e);
        }

    }
}
