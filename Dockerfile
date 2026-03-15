FROM alpine:latest

WORKDIR /app

RUN echo "Hello from Docker container"

CMD ["sh"]