package auth_service.revakh.messaging;


import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String AUTH_EXCHANGE = "app.global.events";

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(AUTH_EXCHANGE,true,false);
    }
    //whenever the rabbit mq server will restart the durable and autodelete means the exchange wont be deleted

}
