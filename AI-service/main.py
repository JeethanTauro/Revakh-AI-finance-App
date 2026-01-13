from fastapi import FastAPI
from contextlib import asynccontextmanager
import threading
import uvicorn
from app.consumer import start_consumer

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

app = FastAPI(lifespan=lifespan)

@app.get("/")
def read_root():
    return {"status": "AI Service is Online", "brain": "Listening to RabbitMQ"}

if __name__ == "__main__":
    uvicorn.run("main:app", host="127.0.0.1", port=8000, reload=True)