
import pika
import json
import os
import sys
from dotenv import load_dotenv

env_path = "/home/jeethan/Desktop/Revakh/Revakh-AI-assisted-Financial-App/AI-service/app/.env"
load_dotenv(dotenv_path=env_path)
RABBIT_USER = os.getenv("RABBIT_USER")
RABBIT_PASS = os.getenv("RABBIT_PASS")

RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "localhost")
EXCHANGE_NAME = "app.global.exchange"
QUEUE_NAME = "ai.service.queue"

def connect_rabbitmq():
    if not RABBIT_USER or not RABBIT_PASS:
        print("Error: RABBIT_USER or RABBIT_PASS environment variables are not set!")
        return None, None
    try:
        credentials = pika.PlainCredentials(RABBIT_USER, RABBIT_PASS) # or your username/password
        parameters = pika.ConnectionParameters(
            host=RABBITMQ_HOST,
            virtual_host='myapp_vhost', # THIS MUST MATCH THE IMAGE
            credentials=credentials
        )
        #connecting to rabbitmq server
        connection = pika.BlockingConnection(parameters=parameters)
        channel = connection.channel()

        #declaring the exchange
        channel.exchange_declare(exchange=EXCHANGE_NAME, exchange_type='topic', durable=True)

        #declaring the queue
        channel.queue_declare(queue=QUEUE_NAME, durable=True)

        #now we need keys to bind to the queues
        routing_keys = [
            "finance.transaction.created",
            "finance.budget.created",
            "finance.budget.updated"
        ]

        for key in routing_keys:
            channel.queue_bind(exchange=EXCHANGE_NAME, queue=QUEUE_NAME, routing_key=key)
            print(f" [v] Bound {QUEUE_NAME} to {key}")
        print(" [*] AI Ear is listening for Finance Events...")
        return channel, connection
    
    except Exception as e:
        print(f"Error connecting to RabbitMQ: {e}")
        return None, None
    
def callback(ch, method, properties, body):
    """
    This function runs AUTOMATICALLY whenever a message arrives.
    """
    try:
        # 1. Decode the bytes to JSON
        message = json.loads(body)
        routing_key = method.routing_key
        
        print(f"\n [x] RECEIVED EVENT: {routing_key}")
        print(f"     Payload: {json.dumps(message, indent=2)}")
        
        # TODO: Vectorize this data and save to ChromaDB (We will add this next)
        
        # 2. Acknowledge (Tell RabbitMQ: "I processed it, you can delete it")
        ch.basic_ack(delivery_tag=method.delivery_tag)
        
    except Exception as e:
        print(f"Error processing message: {e}")
        # If we crash, tell RabbitMQ to NOT requeue it (prevent infinite loops)
        ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)


def start_consumer():
    """
    The main loop that keeps the ear open.
    """
    channel, connection = connect_rabbitmq()
    if channel:
        # Tell RabbitMQ to send messages to our 'callback' function
        channel.basic_consume(queue=QUEUE_NAME, on_message_callback=callback)
        try:
            channel.start_consuming()
        except KeyboardInterrupt:
            channel.stop_consuming()
            connection.close()