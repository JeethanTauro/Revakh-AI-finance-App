
import pika
import json
import os
import sys
from dotenv import load_dotenv
from app.database import generate_narrative, store_event

env_path = "/home/jeethan/Desktop/Revakh/Revakh-AI-assisted-Financial-App/AI-service/app/.env"
load_dotenv(dotenv_path=env_path)
RABBIT_USER = os.getenv("RABBIT_USER")
RABBIT_PASS = os.getenv("RABBIT_PASS")

RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "localhost")
EXCHANGE_NAME = "app.global.exchange"
QUEUE_NAME = "ai.service.queue"


#This is the fucntion to connect to the rabbit mq using the credentials
#here it creates the queues, and then binds it to the respective routing keys
def connect_rabbitmq():
    if not RABBIT_USER or not RABBIT_PASS:
        print("Error: RABBIT_USER or RABBIT_PASS environment variables are not set!")
        return None, None
    try:
        credentials = pika.PlainCredentials(RABBIT_USER, RABBIT_PASS) # or your username/password
        parameters = pika.ConnectionParameters(
            host=RABBITMQ_HOST,
            virtual_host='myapp_vhost',
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

#Basically this method runs whenever the queue receives a message/event   
def callback(ch, method, properties, body):
   
    routing_key = method.routing_key
    print(f" [x] RECEIVED EVENT: {routing_key}")
    
    try:
        # 1. Parse the incoming bytes to JSON
        message_json = json.loads(body)
        
        # 2. TAG THE EVENT TYPE (The fix for Budgets)
        # We check the routing key to decide if it's a budget or transaction
        if "budget" in routing_key:
            message_json['type'] = "budget"
        else:
            message_json['type'] = "transaction"
        
        print(f"\npayload : \n {message_json}")

        # 3. Generate the Narrative (using the new type)
        narrative = generate_narrative(message_json, message_json['type'])
        
        # 4. Store in ChromaDB
        store_event(message_json, narrative)
        
        # Acknowledge the message was processed
        ch.basic_ack(delivery_tag=method.delivery_tag)
        
    except Exception as e:
        print(f"Error processing message: {e}")
        # Optionally nack the message so it stays in the queue
        ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)


#this is the start consumer thread
#so it starts the connection, and then once the channel and connection is established it runs the consume function witht the callback
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