package finance_service.revakh.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import finance_service.revakh.events.UserCreatedEvent;
import finance_service.revakh.events.UserDeletedEvent;
import finance_service.revakh.queues.RabbitMqQueues;
import finance_service.revakh.service.FinanceUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventListeners {
    private final FinanceUserService financeUserService;
    private final ObjectMapper objectMapper;
    @RabbitListener(queues = RabbitMqQueues.USER_CREATED_QUEUE)
    public void handleUserCreated(String message){
        try {
            UserCreatedEvent event = objectMapper.readValue(message, UserCreatedEvent.class);
            System.out.println("User created event received : " + event);
            financeUserService.userCreate(event); //creating a user
        }catch (JsonProcessingException e){
            System.out.println("Problem jsonfying in finance : "+e);
        }
    }

    @RabbitListener(queues = RabbitMqQueues.USER_DELETED_QUEUE)
    public void handleUserDeleted(String message){
        try {
            UserDeletedEvent event = objectMapper.readValue(message, UserDeletedEvent.class);
            System.out.println("User deleted event received : " + event);
            financeUserService.userDelete(event);// delete user
        }catch (JsonProcessingException e){
            System.out.println("Problem jsonfying in finance : "+e);
        }
    }

}
