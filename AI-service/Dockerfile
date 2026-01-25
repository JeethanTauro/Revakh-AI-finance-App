# 1. Use an official, lightweight Python runtime
FROM python:3.11-slim

# 2. Set the working directory inside the container
WORKDIR /app

# 3. Prevent Python from writing .pyc files and enable real-time logging
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# 4. Install system-level dependencies (needed for some AI/RabbitMQ libs)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# 5. Copy only requirements first to leverage Docker's cache
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 6. Copy the entire project into the container
COPY . .

# 7. Expose the port your FastAPI app runs on
EXPOSE 8082

# 8. Start the FastAPI application
# We use 'uvicorn' as the production-ready server
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8082"]