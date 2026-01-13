import chromadb
from chromadb.config import Settings
from chromadb.utils import embedding_functions
from groq import Groq
from dotenv import load_dotenv
import os

env_path = "/home/jeethan/Desktop/Revakh/Revakh-AI-assisted-Financial-App/AI-service/app/.env"


load_dotenv(dotenv_path=env_path)

# 1. Initialize Persistent Storage
# This ensures your 'finance_memory' folder is created and used
client = chromadb.PersistentClient(path="./finance_memory")

groq_client = Groq(api_key=os.getenv("GROQ_API_KEY"))

# 2. Define the Embedding Function 
# (Default uses 'all-MiniLM-L6-v2', which is great for local development)
default_ef = embedding_functions.DefaultEmbeddingFunction()

def get_finance_collection():
    """
    Creates or retrieves the collection with a specific schema.
    We use 'cosine' distance because it's superior for comparing 
    natural language narratives.
    """
    return client.get_or_create_collection(
        name="finance_events",
        embedding_function=default_ef,
        metadata={"hnsw:space": "cosine"} # The 'Schema' part: using Cosine Similarity
    )

def generate_narrative(message_json,event_type):
    """
    Converts a raw transaction and budgets JSON into a rich, semantic narrative.
    """
    if "budget" in event_type:
        amount = message_json.get('limitAmount', 0)
        cat = message_json.get('category', 'Global')
        period = message_json.get('period', 'Monthly')
        
        prompt = f"""
        Convert this Budget Goal into a 3-sentence financial memory:
        Budget: {amount} for {cat} ({period}). 
        Context: The user has set a spending limit or a budget to stay financially disciplined on a particular category.
        Output only the narrative.
        """
    else:
        amount = message_json.get('amount')
        desc = message_json.get('description')
        cat = message_json.get('category')
        m_type = message_json.get('type')
        date = message_json.get('ocuredAt')
    
    # The "Secret Sauce": The Prompt
        prompt = f"""
        You are a professional financial data analyst for a personal finance RAG system.
        Task: Convert the transaction below into a 3-sentence semantic narrative for long-term memory.
    
        Transaction: {amount} {message_json.get('currency')} for "{desc}" (Category: {cat}, Type: {m_type}, Date: {date})
    
        Requirements:
        Guidelines:
        1. Identify the likely purpose of the transaction (e.g., 'dining out', 'essential grocery', 'transportation', 'salary' etc).
        2. Mention the amount, currency,category and date clearly.
        3. Add a professional financial context.
        4. Do not use curly braces or code in the output.
        5. Make sure the senetences you make are factual and human readable , don't halucinate
        6. The currency is always indian rupees
        Output only the narrative. No JSON, no preamble.
      """

    # Extract data for the prompt
    

    try:
        response = groq_client.chat.completions.create(
            messages=[{"role": "user", "content": prompt}],
            model="openai/gpt-oss-120b",
            temperature=0.2 # Keep it consistent and factual
        )
        return response.choices[0].message.content.strip()
    except Exception as e:
        print(f"Error generating narrative: {e}")
        # Fallback to a basic string if LLM fails
        return f"{m_type} of {amount} for {desc} in {cat}."

def store_event(message_json, narrative):
    collection = get_finance_collection()
    
    # Identify the event type (transaction vs budget)
    event_type = message_json.get('type', 'UNKNOWN').lower()
    
    # 1. Start with common fields
    metadata = {
        "userId": int(message_json.get('userId', 0)),
        "category": str(message_json.get('category', 'GENERAL')),
        "type": event_type,
        "timestamp": str(message_json.get('createdAt') or message_json.get('occurredAt') or "N/A")
    }

    # 2. Add Conditional Fields (Null-Safe)
    if "budget" in event_type:
        metadata.update({
            "budgetId": int(message_json.get('budgetId', 0)),
            "limitAmount": float(message_json.get('limitAmount', 0.0)),
            "period": str(message_json.get('period', 'MONTHLY')),
            "active": bool(message_json.get('active', True))
        })
    else:
        metadata.update({
            "transactionId": str(message_json.get('transactionId', 'N/A')),
            "amount": float(message_json.get('amount', 0.0))
        })

    # 3. Add to Collection
    collection.add(
        documents=[narrative],
        metadatas=[metadata],
        ids=[str(message_json.get('eventId'))]
    )
    print(f" [v] {event_type.upper()} stored for user {metadata['userId']}")


"""

- we have to protect our chatbot api, its currently exposed
- as the RAG gives top 5 so it might forgetr some old transactions and old budgets
- also the thing is if we update our budget, the old budget is still in the db, so the RAG might be confused which is the actual budget
- also we can create documents which summarise the old data, like weekly sumary and monthly summary, daily summary

"""