# askmydocs

Simple RAG in Java and Spring Boot

## Run it on your machine

You need Java 21 Docker and [Ollama](https://ollama.com) or a gemini api key

```bash
ollama pull nomic-embed-text     # embedding model
ollama pull llama3.2             # generation model
docker compose up -d
./mvnw spring-boot:run
```

Open http://localhost:8080 upload a PDF or a text file and ask it something...

Uploads are capped at 10 MB the database keeps it's data in a Docker volume...

## Run the tests

```bash
./mvnw test
```

The tests start their own throwaway Postgres so Docker needs to be running...

## Put it on your own AWS account

This deploys small Amazon Linux machine that runs the app and the database
side by side using Docker. It uses Gemini rather than Ollama because the 
machine is too small to run a model itself (1GB)

```bash
./mvnw package -DskipTests                    # builds the file the deploy uploads
cd infra
cdk bootstrap
GEMINI_API_KEY=your-key-here cdk deploy
```

Give the machine a couple of minutes after that it is still downloading Docker and building the app on first boot...

To take it down:

```bash
cdk destroy
```

The key you pass on the command line ends up in your shell history and in a file on the server...
