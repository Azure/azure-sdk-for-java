# Build the Cosmos DB Connectors on top of the Kafka Connect image
FROM confluentinc/cp-kafka-connect:8.2.5

# Install datagen connector
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-datagen:0.6.6

COPY connectors/ /etc/kafka-connect/jars/
COPY resources/connect-log4j2.properties /etc/kafka/connect-log4j2.properties
