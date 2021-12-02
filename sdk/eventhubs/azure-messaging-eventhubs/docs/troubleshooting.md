# Troubleshooting

## Logging

Logging expedites debugging failures in the Azure Event Hubs client library.  [Configure logging in the Azure SDK for Java][logging] shows how to enable logging in the SDK. In addition to enabling logging, setting `AZURE_LOG_LEVEL` to `VERBOSE` provides insights to the state of the library.  We have an Event Hubs specific logging configuration that filters out log messages. Below are examples for Log4J2 and logback.

### Configuring Log4J 2

1. Add the following dependencies in your pom.xml.
    ```xml
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-api</artifactId>
        <version>2.14.1</version>
    </dependency>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-core</artifactId>
        <version>2.14.1</version>
    </dependency>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-slf4j-impl</artifactId>
        <version>2.14.1</version>
    </dependency>
    <dependency>
        <groupId>org.codehaus.groovy</groupId>
        <artifactId>groovy-jsr223</artifactId>
        <version>3.0.9</version>
        <scope>runtime</scope>
    </dependency>
    ```
2. Add [log4j2.xml][log4j2] to your `src/main/resources`.
3. Add `AZURE_LOG_LEVEL=1` to enable VERBOSE logging for the Azure SDK.

### Configuring logback

1. Add the following dependencies in your pom.xml.
    ```xml
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.2.6</version>
    </dependency>
    <dependency>
        <groupId>org.codehaus.janino</groupId>
        <artifactId>janino</artifactId>
        <version>3.1.6</version>
    </dependency>
    ```
1. Add [logback.xml][logback] to your `src/main/resources`.
1. Add `AZURE_LOG_LEVEL=1` to enable logging for the Azure SDK.

<!-- Links --->
[logging]: https://docs.microsoft.com/azure/developer/java/sdk/logging-overview
[log4j2]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventhubs/azure-messaging-eventhubs/docs/log4j2.xml
[logback]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventhubs/azure-messaging-eventhubs/docs/logback.xml
