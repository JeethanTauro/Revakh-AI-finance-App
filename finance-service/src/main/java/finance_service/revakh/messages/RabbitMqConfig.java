package finance_service.revakh.messages;

import finance_service.revakh.queues.RabbitMqQueues;
import org.hibernate.sql.model.ast.builder.ColumnValueBindingBuilder;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;

@Configuration
public class RabbitMqConfig {

    public static final String FINANCE_EXCHANGE = "app.global.exhange";

    @Bean
    public Queue userCreatedQueue(){
        return new Queue(RabbitMqQueues.USER_CREATED_QUEUE,true);
    }

    @Bean
    public Queue userDeletedQueue(){
        return new Queue(RabbitMqQueues.USER_DELETED_QUEUE, true);
    }

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(FINANCE_EXCHANGE, true, false);
    }

    @Bean
    public Binding bindingUserCreated(){
        return BindingBuilder.bind(userCreatedQueue())
                .to(topicExchange())
                .with(RabbitMqQueues.USER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingUserDeleted(){
        return BindingBuilder.bind(userDeletedQueue())
                .to(topicExchange())
                .with(RabbitMqQueues.USER_DELETED_ROUTING_KEY);
    }
}
