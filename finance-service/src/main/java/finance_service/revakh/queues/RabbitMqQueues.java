package finance_service.revakh.queues;

import org.springframework.stereotype.Repository;


public class RabbitMqQueues {

     public static final String USER_CREATED_QUEUE = "finance.user.created";
     public static final String USER_DELETED_QUEUE = "finance.user.deleted";

     public static final String USER_CREATED_ROUTING_KEY = "user.created";
     public static final String USER_DELETED_ROUTING_KEY = "user.deleted";

}
