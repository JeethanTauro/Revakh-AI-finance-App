package finance_service.revakh.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import finance_service.revakh.events.BudgetCreatedEvent;
import finance_service.revakh.events.BudgetUpdatedEvent;
import finance_service.revakh.events.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class BudgetEventPublisher {
    private final static String BUDGET_CREATED_KEY = "finance.budget.created";
    private final static String BUDGET_UPDATED_KEY = "finance.budget.updated";
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public void budgetCreatedPublisher(BudgetCreatedEvent budgetCreatedEvent){
        try{
            String message = objectMapper.writeValueAsString(budgetCreatedEvent);
            rabbitTemplate.convertAndSend(RabbitMqConfig.FINANCE_EXCHANGE, BUDGET_CREATED_KEY,message,msg -> {
                msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return msg;
            });
            System.out.println("Published event: " + message);
        }catch (JsonProcessingException e){
            // 6. Log ONLY. Do not crash the user's request.
            log.error("❌ Failed to publish transaction event to RabbitMQ", e);
        }
    }

    public void budgetUpdatedPublisher(BudgetUpdatedEvent budgetUpdatedEvent){
        try{
            String message = objectMapper.writeValueAsString(budgetUpdatedEvent);
            rabbitTemplate.convertAndSend(RabbitMqConfig.FINANCE_EXCHANGE, BUDGET_UPDATED_KEY,message,msg -> {
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
