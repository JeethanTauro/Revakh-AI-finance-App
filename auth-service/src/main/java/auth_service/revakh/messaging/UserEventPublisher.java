package auth_service.revakh.messaging;

import auth_service.revakh.events.UserCreatedEvent;
import auth_service.revakh.events.UserDeletedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserEventPublisher {

    //converts event into json and then sends it to exchange with a routing key so it sends to the correct queue
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE  = RabbitMqConfig.AUTH_EXCHANGE;
    private static final String ROUTING_KEY_CREATED = "user.created";
    private static final String ROUTING_KEY_DELETED = "user.deleted";

    public void userCreatedEventPublisher(UserCreatedEvent userCreatedEvent){
        try{
            String message = objectMapper.writeValueAsString(userCreatedEvent);
            rabbitTemplate.convertAndSend(EXCHANGE,ROUTING_KEY_CREATED,message,msg -> {
                msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return msg;
            });
            System.out.println("Published event: " + message);


        }catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
    public void userDeletedEventPublisher(UserDeletedEvent userDeletedEvent){
        try{
            String message = objectMapper.writeValueAsString(userDeletedEvent);
            rabbitTemplate.convertAndSend(EXCHANGE,ROUTING_KEY_DELETED,message,msg -> {
                msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return msg;
            });
            System.out.println("Published event : "+message);
        }catch (JsonProcessingException e){
            throw new RuntimeException("Failed to serialize event",e);
        }
    }
}
