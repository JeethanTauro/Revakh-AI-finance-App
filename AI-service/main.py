from fastapi import FastAPI,HTTPException
from contextlib import asynccontextmanager
from pydantic import BaseModel
import threading
import uvicorn
from app.consumer import start_consumer
from fastapi.middleware.cors import CORSMiddleware
from app.ai_service import ask_finance_ai



# This is the "Lifespan" event. 
# Code before 'yield' runs on Startup.
# Code after 'yield' runs on Shutdown.
@asynccontextmanager
async def lifespan(app: FastAPI):
    
    print("Starting AI Service...")
    
    # Start the RabbitMQ Consumer in a background thread
    # daemon=True means this thread will die when the main program dies
    consumer_thread = threading.Thread(target=start_consumer, daemon=True)
    consumer_thread.start()
    
    yield
    
    print("Shutting down AI Service...")

app = FastAPI(title="Revakh AI Assistant API", lifespan=lifespan)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # In production, replace with your frontend URL
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
def read_root():
    return {"status": "AI Service is Online", "brain": "Listening to RabbitMQ"}

class ChatRequest(BaseModel):
    user_id: int
    query: str

class ChatResponse(BaseModel):
    answer: str

@app.post("/api/AI/chat", response_model=ChatResponse)
async def chat_endpoint(request: ChatRequest):
    try:
        # Call the RAG function we built earlier
        answer = ask_finance_ai(request.user_id, request.query)
        return ChatResponse(answer=answer)
    except Exception as e:
        # Proper error handling so the frontend knows what went wrong
        raise HTTPException(status_code=500, detail=str(e))
    
if __name__ == "__main__":
    uvicorn.run("main:app", host="127.0.0.1", port=8082, reload=True)