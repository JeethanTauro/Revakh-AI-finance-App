import os
from groq import Groq
from dotenv import load_dotenv
from app.database import get_finance_collection # Import your function from database.py

# Load env variables
env_path = "/home/jeethan/Desktop/Revakh/Revakh-AI-assisted-Financial-App/AI-service/app/.env"
load_dotenv(dotenv_path=env_path)
groq_client = Groq(api_key=os.getenv("GROQ_API_KEY"))

def ask_finance_ai(user_id: int, query: str):
    """
    RAG Logic:
    1. Retrieve similar semantic narratives from ChromaDB (Filtered by user_id).
    2. Build a context string from the retrieved documents.
    3. Ask Groq to answer the query based on that context.
    """
    collection = get_finance_collection()
    
    # 1. RETRIEVAL (The 'R' in RAG)
    # We use 'where' for metadata filtering so User A doesn't see User B's data
    results = collection.query(
        query_texts=[query],
        n_results=5,
        where={"userId": user_id} 
    )

    # 2. CONTEXT BUILDING
    # results['documents'] is a list of lists (one per query_text)
    retrieved_docs = results.get("documents", [[]])[0]
    
    if not retrieved_docs:
        return "I don't have any financial records for you yet. Try adding some transactions first!"

    context = "\n---\n".join(retrieved_docs)

    # 3. GENERATION (The 'G' in RAG)
    system_prompt = f"""
    You are 'Revakh AI', a professional financial assistant. 
    Use the following transaction narratives to answer the user's question accurately.
    
    Context from User's History:
    {context}
    
    Guidelines:
    - If the answer isn't in the context, say you don't have enough data.
    - Be concise and professional.
    - Treat all numbers as factual.
    - Currency is always indian rupees
    """

    try:
        chat_completion = groq_client.chat.completions.create(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": query}
            ],
            model="openai/gpt-oss-120b",
            temperature=0.2 # Low temperature for financial accuracy
        )
        return chat_completion.choices[0].message.content.strip()
    
    except Exception as e:
        print(f"Error in generation: {e}")
        return "Sorry, I encountered an error while analyzing your finances."