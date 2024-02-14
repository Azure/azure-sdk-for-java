# Build the Cosmos DB Connectors on top of the Kafka Connect image
FROM confluentinc/cp-kafka-connect:7.5.0

# Install datagen connector
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-datagen:latest

COPY connectors/ /etc/kafka-connect/jars
