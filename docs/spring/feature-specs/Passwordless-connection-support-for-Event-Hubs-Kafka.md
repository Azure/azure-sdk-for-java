# Goal
To support all Azure Identity supported credential types and configuration options for Event Hubs Kafka when using Spring Cloud Azure support.

# Configuration Properties

1. Spring Boot Kafka Common configuration
    ```properties
    spring.kafka.properties.azure.credential.client-certificate-password
    spring.kafka.properties.azure.credential.client-certificate-path
    spring.kafka.properties.azure.credential.client-id
    spring.kafka.properties.azure.credential.client-secret
    spring.kafka.properties.azure.credential.managed-identity-enabled
    spring.kafka.properties.azure.credential.password
    spring.kafka.properties.azure.credential.username
    spring.kafka.properties.azure.profile.cloud-type
    spring.kafka.properties.azure.profile.environment.active-directory-endpoint
    spring.kafka.properties.azure.profile.environment.active-directory-graph-api-version
    spring.kafka.properties.azure.profile.environment.active-directory-graph-endpoint
    spring.kafka.properties.azure.profile.environment.active-directory-resource-id
    spring.kafka.properties.azure.profile.environment.azure-application-insights-endpoint
    spring.kafka.properties.azure.profile.environment.azure-data-lake-analytics-catalog-and-job-endpoint-suffix
    spring.kafka.properties.azure.profile.environment.azure-data-lake-store-file-system-endpoint-suffix
    spring.kafka.properties.azure.profile.environment.azure-log-analytics-endpoint
    spring.kafka.properties.azure.profile.environment.data-lake-endpoint-resource-id
    spring.kafka.properties.azure.profile.environment.gallery-endpoint
    spring.kafka.properties.azure.profile.environment.key-vault-dns-suffix
    spring.kafka.properties.azure.profile.environment.management-endpoint
    spring.kafka.properties.azure.profile.environment.microsoft-graph-endpoint
    spring.kafka.properties.azure.profile.environment.portal
    spring.kafka.properties.azure.profile.environment.publishing-profile
    spring.kafka.properties.azure.profile.environment.resource-manager-endpoint
    spring.kafka.properties.azure.profile.environment.sql-management-endpoint
    spring.kafka.properties.azure.profile.environment.sql-server-hostname-suffix
    spring.kafka.properties.azure.profile.environment.storage-endpoint-suffix
    spring.kafka.properties.azure.profile.subscription-id
    spring.kafka.properties.azure.profile.tenant-id
    ```

2. Spring Boot Kafka consumer configuration

    Set either Spring Boot Kafka common configuration or below ones:
    
   ```properties
    spring.kafka.consumer.properties.azure.credential.client-certificate-password
    spring.kafka.consumer.properties.azure.credential.client-certificate-path
    spring.kafka.consumer.properties.azure.credential.client-id
    spring.kafka.consumer.properties.azure.credential.client-secret
    spring.kafka.consumer.properties.azure.credential.managed-identity-enabled
    spring.kafka.consumer.properties.azure.credential.password
    spring.kafka.consumer.properties.azure.credential.username
    spring.kafka.consumer.properties.azure.profile.cloud-type
    spring.kafka.consumer.properties.azure.profile.environment.active-directory-endpoint
    spring.kafka.consumer.properties.azure.profile.environment.active-directory-graph-api-version
    spring.kafka.consumer.properties.azure.profile.environment.active-directory-graph-endpoint
    spring.kafka.consumer.properties.azure.profile.environment.active-directory-resource-id
    spring.kafka.consumer.properties.azure.profile.environment.azure-application-insights-endpoint
    spring.kafka.consumer.properties.azure.profile.environment.azure-data-lake-analytics-catalog-and-job-endpoint-suffix
    spring.kafka.consumer.properties.azure.profile.environment.azure-data-lake-store-file-system-endpoint-suffix
    spring.kafka.consumer.properties.azure.profile.environment.azure-log-analytics-endpoint
    spring.kafka.consumer.properties.azure.profile.environment.data-lake-endpoint-resource-id
    spring.kafka.consumer.properties.azure.profile.environment.gallery-endpoint
    spring.kafka.consumer.properties.azure.profile.environment.key-vault-dns-suffix
    spring.kafka.consumer.properties.azure.profile.environment.management-endpoint
    spring.kafka.consumer.properties.azure.profile.environment.microsoft-graph-endpoint
    spring.kafka.consumer.properties.azure.profile.environment.portal
    spring.kafka.consumer.properties.azure.profile.environment.publishing-profile
    spring.kafka.consumer.properties.azure.profile.environment.resource-manager-endpoint
    spring.kafka.consumer.properties.azure.profile.environment.sql-management-endpoint
    spring.kafka.consumer.properties.azure.profile.environment.sql-server-hostname-suffix
    spring.kafka.consumer.properties.azure.profile.environment.storage-endpoint-suffix
    spring.kafka.consumer.properties.azure.profile.subscription-id
    spring.kafka.consumer.properties.azure.profile.tenant-id
    ```

3. Spring Boot Kafka producer configuration

    Set either Spring Boot Kafka common configuration or below ones:

    ```properties
    spring.kafka.producer.properties.azure.credential.client-certificate-password
    spring.kafka.producer.properties.azure.credential.client-certificate-path
    spring.kafka.producer.properties.azure.credential.client-id
    spring.kafka.producer.properties.azure.credential.client-secret
    spring.kafka.producer.properties.azure.credential.managed-identity-enabled
    spring.kafka.producer.properties.azure.credential.password
    spring.kafka.producer.properties.azure.credential.username
    spring.kafka.producer.properties.azure.profile.cloud-type
    spring.kafka.producer.properties.azure.profile.environment.active-directory-endpoint
    spring.kafka.producer.properties.azure.profile.environment.active-directory-graph-api-version
    spring.kafka.producer.properties.azure.profile.environment.active-directory-graph-endpoint
    spring.kafka.producer.properties.azure.profile.environment.active-directory-resource-id
    spring.kafka.producer.properties.azure.profile.environment.azure-application-insights-endpoint
    spring.kafka.producer.properties.azure.profile.environment.azure-data-lake-analytics-catalog-and-job-endpoint-suffix
    spring.kafka.producer.properties.azure.profile.environment.azure-data-lake-store-file-system-endpoint-suffix
    spring.kafka.producer.properties.azure.profile.environment.azure-log-analytics-endpoint
    spring.kafka.producer.properties.azure.profile.environment.data-lake-endpoint-resource-id
    spring.kafka.producer.properties.azure.profile.environment.gallery-endpoint
    spring.kafka.producer.properties.azure.profile.environment.key-vault-dns-suffix
    spring.kafka.producer.properties.azure.profile.environment.management-endpoint
    spring.kafka.producer.properties.azure.profile.environment.microsoft-graph-endpoint
    spring.kafka.producer.properties.azure.profile.environment.portal
    spring.kafka.producer.properties.azure.profile.environment.publishing-profile
    spring.kafka.producer.properties.azure.profile.environment.resource-manager-endpoint
    spring.kafka.producer.properties.azure.profile.environment.sql-management-endpoint
    spring.kafka.producer.properties.azure.profile.environment.sql-server-hostname-suffix
    spring.kafka.producer.properties.azure.profile.environment.storage-endpoint-suffix
    spring.kafka.producer.properties.azure.profile.subscription-id
    spring.kafka.producer.properties.azure.profile.tenant-id
    ```

4. Spring Boot Kafka admin configuration

    Set either Spring Boot Kafka common configuration or below ones:

    ```properties
    spring.kafka.admin.properties.azure.credential.client-certificate-password
    spring.kafka.admin.properties.azure.credential.client-certificate-path
    spring.kafka.admin.properties.azure.credential.client-id
    spring.kafka.admin.properties.azure.credential.client-secret
    spring.kafka.admin.properties.azure.credential.managed-identity-enabled
    spring.kafka.admin.properties.azure.credential.password
    spring.kafka.admin.properties.azure.credential.username
    spring.kafka.admin.properties.azure.profile.cloud-type
    spring.kafka.admin.properties.azure.profile.environment.active-directory-endpoint
    spring.kafka.admin.properties.azure.profile.environment.active-directory-graph-api-version
    spring.kafka.admin.properties.azure.profile.environment.active-directory-graph-endpoint
    spring.kafka.admin.properties.azure.profile.environment.active-directory-resource-id
    spring.kafka.admin.properties.azure.profile.environment.azure-application-insights-endpoint
    spring.kafka.admin.properties.azure.profile.environment.azure-data-lake-analytics-catalog-and-job-endpoint-suffix
    spring.kafka.admin.properties.azure.profile.environment.azure-data-lake-store-file-system-endpoint-suffix
    spring.kafka.admin.properties.azure.profile.environment.azure-log-analytics-endpoint
    spring.kafka.admin.properties.azure.profile.environment.data-lake-endpoint-resource-id
    spring.kafka.admin.properties.azure.profile.environment.gallery-endpoint
    spring.kafka.admin.properties.azure.profile.environment.key-vault-dns-suffix
    spring.kafka.admin.properties.azure.profile.environment.management-endpoint
    spring.kafka.admin.properties.azure.profile.environment.microsoft-graph-endpoint
    spring.kafka.admin.properties.azure.profile.environment.portal
    spring.kafka.admin.properties.azure.profile.environment.publishing-profile
    spring.kafka.admin.properties.azure.profile.environment.resource-manager-endpoint
    spring.kafka.admin.properties.azure.profile.environment.sql-management-endpoint
    spring.kafka.admin.properties.azure.profile.environment.sql-server-hostname-suffix
    spring.kafka.admin.properties.azure.profile.environment.storage-endpoint-suffix
    spring.kafka.admin.properties.azure.profile.subscription-id
    spring.kafka.admin.properties.azure.profile.tenant-id    
    ```

5. Spring Cloud Stream Kafka Binder common configuration

    Set either Spring Cloud Stream Kafka Binder common configuration as below ones:

    ```properties
    spring.cloud.stream.kafka.binder.configuration.azure.credential.client-certificate-password
    spring.cloud.stream.kafka.binder.configuration.azure.credential.client-certificate-path
    spring.cloud.stream.kafka.binder.configuration.azure.credential.client-id
    spring.cloud.stream.kafka.binder.configuration.azure.credential.client-secret
    spring.cloud.stream.kafka.binder.configuration.azure.credential.managed-identity-enabled
    spring.cloud.stream.kafka.binder.configuration.azure.credential.password
    spring.cloud.stream.kafka.binder.configuration.azure.credential.username
    spring.cloud.stream.kafka.binder.configuration.azure.profile.cloud-type
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.active-directory-endpoint
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.active-directory-graph-api-version
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.active-directory-graph-endpoint
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.active-directory-resource-id
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.azure-application-insights-endpoint
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.azure-data-lake-analytics-catalog-and-job-endpoint-suffix
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.azure-data-lake-store-file-system-endpoint-suffix
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.azure-log-analytics-endpoint
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.data-lake-endpoint-resource-id
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.gallery-endpoint
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.key-vault-dns-suffix
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.management-endpoint
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.microsoft-graph-endpoint
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.portal
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.publishing-profile
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.resource-manager-endpoint
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.sql-management-endpoint
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.sql-server-hostname-suffix
    spring.cloud.stream.kafka.binder.configuration.azure.profile.environment.storage-endpoint-suffix
    spring.cloud.stream.kafka.binder.configuration.azure.profile.subscription-id
    spring.cloud.stream.kafka.binder.configuration.azure.profile.tenant-id
    ```

6. Spring Cloud Stream Kafka Binder consumer configuration

    Set either Spring Boot Kafka common/consumer configuration or below ones:

    ```properties
    spring.cloud.stream.kafka.binder.consumer-properties.azure.credential.client-certificate-password
    spring.cloud.stream.kafka.binder.consumer-properties.azure.credential.client-certificate-path
    spring.cloud.stream.kafka.binder.consumer-properties.azure.credential.client-id
    spring.cloud.stream.kafka.binder.consumer-properties.azure.credential.client-secret
    spring.cloud.stream.kafka.binder.consumer-properties.azure.credential.managed-identity-enabled
    spring.cloud.stream.kafka.binder.consumer-properties.azure.credential.password
    spring.cloud.stream.kafka.binder.consumer-properties.azure.credential.username
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.cloud-type
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.active-directory-endpoint
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.active-directory-graph-api-version
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.active-directory-graph-endpoint
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.active-directory-resource-id
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.azure-application-insights-endpoint
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.azure-data-lake-analytics-catalog-and-job-endpoint-suffix
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.azure-data-lake-store-file-system-endpoint-suffix
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.azure-log-analytics-endpoint
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.data-lake-endpoint-resource-id
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.gallery-endpoint
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.key-vault-dns-suffix
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.management-endpoint
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.microsoft-graph-endpoint
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.portal
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.publishing-profile
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.resource-manager-endpoint
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.sql-management-endpoint
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.sql-server-hostname-suffix
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.environment.storage-endpoint-suffix
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.subscription-id
    spring.cloud.stream.kafka.binder.consumer-properties.azure.profile.tenant-id
    ```

7. Spring Cloud Stream Kafka Binder producer configuration

    Set either Spring Boot Kafka common/producer configuration or below ones:

    ```properties
    spring.cloud.stream.kafka.binder.producer-properties.azure.credential.client-certificate-password
    spring.cloud.stream.kafka.binder.producer-properties.azure.credential.client-certificate-path
    spring.cloud.stream.kafka.binder.producer-properties.azure.credential.client-id
    spring.cloud.stream.kafka.binder.producer-properties.azure.credential.client-secret
    spring.cloud.stream.kafka.binder.producer-properties.azure.credential.managed-identity-enabled
    spring.cloud.stream.kafka.binder.producer-properties.azure.credential.password
    spring.cloud.stream.kafka.binder.producer-properties.azure.credential.username
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.cloud-type
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.active-directory-endpoint
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.active-directory-graph-api-version
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.active-directory-graph-endpoint
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.active-directory-resource-id
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.azure-application-insights-endpoint
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.azure-data-lake-analytics-catalog-and-job-endpoint-suffix
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.azure-data-lake-store-file-system-endpoint-suffix
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.azure-log-analytics-endpoint
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.data-lake-endpoint-resource-id
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.gallery-endpoint
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.key-vault-dns-suffix
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.management-endpoint
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.microsoft-graph-endpoint
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.portal
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.publishing-profile
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.resource-manager-endpoint
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.sql-management-endpoint
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.sql-server-hostname-suffix
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.environment.storage-endpoint-suffix
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.subscription-id
    spring.cloud.stream.kafka.binder.producer-properties.azure.profile.tenant-id
    ```

7. Spring Cloud Stream Kafka Binder admin configuration

    Not supported by SCS Kafka Binder, should use Spring Boot Kafka common or admin configuration, or Spring Cloud Stream Kafka Binder common configuration.