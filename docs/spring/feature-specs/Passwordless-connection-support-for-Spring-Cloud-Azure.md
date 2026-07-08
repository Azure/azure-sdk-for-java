# Problem statement

Support for passwordless connection has become a fundamental principle for Azure, providing a security baseline at the individual service level. We believe aligning on those guidelines will also benefit Spring developers at large.

We do not support passwordless connection for any 3rd party libraries today, and for 1st party libraries where we already have support, documentation and samples are lacking to guide Spring developers to complete their journey. Lack of samples has been one of the top challenges from the Spring Cloud Azure users.

# Scope
The scope to describe passwordless connection support can be divided into 4 dimensions: 
- Passwordless connection approaches
- Target Azure services to connect to
- Hosting environments
- Developing frameworks in use

The credential free support should cover all user scenarios combined with the above four dimensions, because a developer may want to use a certain Java framework to connect one Azure service without credentials, and he/she may deploy the application on a certain environment for production requirements.

## Passwordless connections approaches
To connect to an Azure service in a passwordless way, the possible approaches include:
- Use **Managed Identities** to obtain Azure AD tokens and then connect to the Azure service with those tokens, the types of Managed Identities to support include system assigned ones and user assigned ones.
- Store credentials in **Key Vault** and use Key Vault as a property source to load credentials within the application.

## Azure Services
The target Azure services that we should support passwordless connection to include:

| Priority | Azure Service |
|:--:|:--:|
| 0 | SQL Server |
| 0 | MySQL |
| 0 | PostgreSQL |
| 1 | Maria DB |
| 0 | Cosmos DB - SQL |
| 0 | Cosmos DB - MongoDB |
| 0 | Cosmos DB - Cassandra |
| 0 | Redis - JEDIS  |
| 0 | Redis - Lettuce  |
| 0 | Event Hubs - Kafka |
| 0 | Event Hubs - Native |
| 0 | Service Bus - Native |
| 0 | Storage Blobs |
| 0 | Storage Files |
| 0 | Storage Queues |
| 0 | Azure AD |
| 1 | Azure Functions |
| 1 | Event Grid |
| 1 | Elasticsearch |

## Hosting environments
The hosting environments that an application deploys on can be vaiours, we should support configuration of passwordless connection in below environments for each target service.
- Azure VM/VMSS
- Azure Kubernetes Service
- Azure App Services​
- Azure Container Instances
- Azure Spring Cloud​
- Azure Functions

## Developing frameworks
The Java frameworks to support are scoped with Spring eco-system.

# User scenarios
To achieve passwordless connection, developers can use approaches of **Managed Identities** and **Key Vault** as claimed above, the user stories are introduced on the basis of passwordless approaches.

## Managed Identities
For the approach of Managed Identities, the user scenarios should cover all combinations with the above developing frameworks, hosting environments and Azure services that support Azure AD authentication. For each case, developers are able to know:
1. How to create and configure Managed Identities for different hosting environments:
   
   Operations to create and configure Managed Identities can be different towards the Azure hosting services and the identity types. Each hosting service has published its own MS documentation for guidance, which is placed separately. When developers use the managed-identity passwordless approach, they should be able to find the managed-identity guidance entrances of all hosting services from our passwordless reference documentation.

2. How to configure Managed Identities for their applications which is built with various spring eco-system frameworks and then connect to an Azure service:

   To achieve passwordless connection with Managed Identities, developers should be able to configure the Managed Identities to applications which can be built with various spring frameworks towards different Azure services. The user scenarios for each service should cover below cases:
   - With a Spring Cloud Azure Managed Identities OAuth library, developers can use any of the supported spring projects to configure Managed Identities and connect to the associated Azure service.
   - Developers can find reference documentations of the above library for guidance of feature introduction, working principle and supported configuration.
   - Developers can find samples to experience how to use the above library to set up an application connecting to Azure services with Managed Identities. 
   - Developers can find quick-start documentations helping remove credentials from an existing application, and transform to passwordless application via Managed Identities.

## Key Vault
For passwordless connection with Key Vault, the user scenarios should cover all combinations with the above developing frameworks, hosting environments and Azure services. For each case, developers can find `samples` to learn about:
1. how they should store credentials in Key Vault, including what credentials and secret names should be set towards different spring projects and target services
2. how they can create and configure Managed Identities used for Key Vault connection in different hosting environments
3. how they can leverage Managed Identities to connect to Key Vault via `spring-cloud-azure-starter-keyvault-secrets`

Besides, when developers need to remove credentials from their existing applications, they should be able to refer to `quick-start` documentations to help transform to passwordless applications via Key Vault.

# Exit criteria
In a nutshell, we need to provide below resources to guide developers for each user scenario:
1. Managed Identities:
   - a collection of managed-identity configuration documentations from all the supported hosting services
   - a collection of Spring Cloud Azure libraries enabling Managed Identities OAuth to connect to different Azure services
   - a collection of reference documentations introducing the feature description, working principle and supported configuration of the above libraries
   - a collection of quick-start documentations to guide how to remove Azure services' credentials from applications building with various spring projects and migrate to using Managed Identities
   - a collection of sample projects leveraging the above libraries to set up applications connecting to the target services using Managed Identities based on different spring projects 
2. Key Vault:
   - a collection of quick-start documentations to guide how to remove Azure services' credentials from applications building with various spring projects and migrate to using Key Vault
   - a collection of sample projects setting up applications leveraging Key Vault to connect the target services 

## Delivery Resource Lists

The final deliveries should include:
### 1. Library:
<table>
    <tr>
        <td>Resource type</td>
        <td>Description</td>
    </tr>
    <tr>
        <td rowspan="4">Spring Cloud Azrue Library</td>
        <td>A spring cloud azure library capable of enabling Managed Identities OAuth for spring ecosystems for Kafka</td>
    </tr>
    <tr>
        <td>A spring cloud azure library capable of enabling Managed Identities OAuth for MySQL</td>
    </tr>
    <tr>
        <td>A spring cloud azure library capable of enabling Managed Identities OAuth for PostgreSQL</td>
    </tr>
    <tr>
        <td>A spring cloud azure library capable of enabling Managed Identities OAuth for Azure AD</td>
    </tr>
</table>

### 2. Reference documentation:
<table>
    <tr>
        <td>Resource type</td>
        <td>Description</td>
    </tr>
    <tr>
        <td rowspan="4">Reference documentation</td>
        <td>A documentation introducing the spring cloud azure library of Managed Identities OAuth Kafka, including feature description, working principle, supported configuration</td>
    </tr>
    <tr>
        <td>A documentation introducing the spring cloud azure library of Managed Identities OAuth MySQL, including feature description, working principle, supported configuration</td>
    </tr>
    <tr>
        <td>A documentation introducing the spring cloud azure library of Managed Identities OAuth PostgreSQL, including feature description, working principle, supported configuration</td>
    </tr>
    <tr>
        <td>A documentation introducing the spring cloud azure library of Managed Identities OAuth Azure AD, including feature description, working principle, supported configuration</td>
    </tr>
</table>

### 3. [Thematic Documentation](https://dynalist.io/d/hiM5D9x76saJTBZxCVVW-p15#z=D86YB8IOuXwEejwAWqSQqT1A)

![image](https://user-images.githubusercontent.com/63028776/171127962-abb4cad5-31c4-4f99-af45-85bad7fc8cc5.png)

### 4. Quick-start documentation:
<table>
    <tr>
        <td>Resource type</td>
        <td>Approach</td>
        <td>Description</td>
    </tr>
    <tr>
        <td rowspan="23">Quick-start documentation</td>
        <td rowspan="8">Managed Identities</td>
        <td>A quick-start introducing how to migrate a spring/spring integration/spring cloud stream binder Kafka application to using Managed Identities from using connection string to connect to Event Hubs</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring boot/spring integration/spring cloud stream binder Event Hubs application to using Managed Identities from using connection string to connect to Event Hubs</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring boot/spring integration/spring cloud stream binder Service Bus application to using Managed Identities from using connection string to connect to Service Bus</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring boot/spring resource Storage Blobs application to using Managed Identities from using connection string to connect to Storage Blobs</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring boot/spring integration Storage Queues application to using Managed Identities from using connection string to connect to Storage Queues</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring data jdbc/spring data jpa application to using Managed Identities from using username/password to connect to MySQL</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring data jdbc/spring data jpa application to using Managed Identities from using username/password to connect to PostgreSQL</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring security application to using Managed Identities from using client id/secret to connect to Azure AD</td>
    </tr>
    <tr>
        <td rowspan="15">Key Vault</td>
        <td>A quick-start introducing how to migrate a spring/spring integration/spring cloud stream binder kafka application to using Key Vault from using connection string to connect to Event Hubs</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring boot/spring integration/spring cloud stream binder Event Hubs application to using Key Vault from using connection string to connect to Event Hubs</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring boot/spring integration/spring cloud stream binder Service Bus application to using Key Vault from using connection string to connect to Service Bus</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring boot/spring resource Storage Blobs application to using Key Vault from using connection string to connect to Storage Blobs</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring boot/spring integration Storage Queues application to using Key Vault from using connection string to connect to Storage Queues</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring boot/spring resource Storage Files application to using Key Vault from using connection string to connect to Storage Files</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring data jdbc/spring data jpa application to using Key Vault from using username/password to connect to SQL Server</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring data jdbc/spring data jpa application to using Key Vault from using username/password to connect to MySQL</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring data jdbc/spring data jpa application to using Key Vault from using username/password to connect to PostgreSQL</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring data jdbc/spring data jpa application to using Key Vault from using primary key to connect to Cosmos DB for SQL</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring data MongoDB application to using Key Vault from using connection string to connect to Cosmos DB for MongoDB</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring data Cassandra application to using Key Vault from using username/password to connect to Cosmos DB for Cassandra</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring data Redis application to using Key Vault from using access key to connect to Redis for JEDIS</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring data Redis application to using Key Vault from using access key to connect to Redis for Lettuce</td>
    </tr>
    <tr>
        <td>A quick-start introducing how to migrate a spring security application to using Key Vault from using client secret to connect to Azure AD</td>
    </tr>
</table>

### 5. Samples:
We need to establish two sample projects to illustrate the usage of Managed Identities and Key Vault for passwordless connection to all the suppported Azure services with various spring projects.
   1. For Managed Identities usage:
      - The sample project lists the links of each hosting service's Managed Identities guidance documentation
      - The sample project lists the links of each hosting service's deployment guidance documentation
      - The sample project imports the dependencies of our Spring Cloud Azure Managed Identities OAuth libraries and configure the necessary Managed Identities.
      - The sample interacts with Event Hubs/Service Bus from 3 mentioned spring Kafka projects, 3 mentioned Spring Cloud Azure Event Hubs projects and 3 mentioned Spring Cloud Azure Service Bus projects
      - The sample interacts with Storage Blobs/Storage Queues from 2 mentioned Spring Cloud Azure Storage Blobs projects and 2 mentioned Spring Cloud Azure Storage Queues projects
      - The sample interacts with one of MySQL/PostgreSQL databases from 2 mentioned spring data projects, which we will use maven profiles and spring profiles to enable developers for selection
      - The sample authenticates with Azure AD from spring security framework
   2. For Key Vault usage:
      - The sample project lists the links of each hosting service's Managed Identities guidance documentation
      - The sample project lists the links of each hosting service's deployment guidance documentation
      - The sample interacts with Event Hubs/Service Bus from 3 mentioned spring Kafka projects, 3 mentioned Spring Cloud Azure Event Hubs projects and 3 mentioned Spring Cloud Azure Service Bus projects
      - The sample interacts with Storage Blobs/Storage Queues from 2 mentioned Spring Cloud Azure Storage Blobs projects and 2 mentioned Spring Cloud Azure Storage Queues projects
      - The sample interacts with one of SQL Server/MySQL/PostgreSQL/Cosmos DB for SQL/Cosmos DB for MongoDB/Cosmos DB for Cassandra/Redis for JEDIS/Redis for Lettuce database from all mentioned spring data projects, which we will use maven profiles and spring profiles to enable developers for selection
      - The sample authenticates with Azure AD from spring security framework

## Delivery Resource Status

<table>
    <tr>
        <td>Passwordless Approaches</td>
        <td>Spring Projects</td>
        <td>Azure Services</td>
        <td>Library Ready</td>
        <td>Reference Doc Ready</td>
        <td>Thematic Doc Ready</td>
        <td>Quick-start Doc Ready</td>
        <td>Sample Ready</td>
    </tr>
    <tr>
        <td rowspan="8">Managed Identities</td>
        <td>Spring Kafka/Spring Integration/Spring Cloud Stream</td>
        <td>Event Hubs for Kafka</td>
        <td>No</td>
        <td>No</td>
        <td>No</td>
        <td>No</td>
        <td rowspan="8">No</td>
    </tr>
    <tr>
        <td>Spring Boot Autoconfiguration/Spring Integration/Spring Cloud Stream</td>
        <td>Event Hubs</td>
        <td>Yes</td>
        <td>Yes</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Boot Autoconfiguration/Spring Integration/Spring Cloud Stream</td>
        <td>Service Bus</td>
        <td>Yes</td>
        <td>Yes</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Boot Autoconfiguration/Spring Resource</td>
        <td>Storage Blobs</td>
        <td>Yes</td>
        <td>Yes</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Boot Autoconfiguration/Spring Integration</td>
        <td>Storage Queues</td>
        <td>Yes</td>
        <td>Yes</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Data JDBC/Spring Data JPA</td>
        <td>MySQL</td>
        <td>No</td>
        <td>No</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Data JDBC/Spring Data JPA</td>
        <td>PostgreSQL</td>
        <td>No</td>
        <td>No</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Security</td>
        <td>Azure AD</td>
        <td>No</td>
        <td>No</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td rowspan="15">Key Vault</td>
        <td>Spring Kafka/Spring Integration/Spring Cloud Stream</td>
        <td>Event Hubs for Kafka</td>
        <td>N/A</td>
        <td>N/A</td>
        <td>No</td>
        <td>No</td>
        <td rowspan="15">No</td>
    </tr>
    <tr>
        <td>Spring Boot Autoconfiguration/Spring Integration/Spring Cloud Stream</td>
        <td>Event Hubs</td>
        <td>N/A</td>
        <td>N/A</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Boot Autoconfiguration/Spring Integration/Spring Cloud Stream</td>
        <td>Service Bus</td>
        <td>N/A</td>
        <td>N/A</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Boot Autoconfiguration/Spring Resource</td>
        <td>Storage Blobs</td>
        <td>N/A</td>
        <td>N/A</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Boot Autoconfiguration/Spring Resource</td>
        <td>Storage Files</td>
        <td>N/A</td>
        <td>N/A</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Boot Autoconfiguration/Spring Integration</td>
        <td>Storage Queues</td>
        <td>N/A</td>
        <td>N/A</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Data JDBC/Spring Data JPA</td>
        <td>SQL Server</td>
        <td>N/A</td>
        <td>N/A</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Data JDBC/Spring Data JPA</td>
        <td>MySQL</td>
        <td>N/A</td>
        <td>N/A</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Data JDBC/Spring Data JPA</td>
        <td>PostgreSQL </td>
        <td>N/A</td>
        <td>N/A</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Data JDBC/Spring Data JPA</td>
        <td>Cosmos DB for SQL</td>
        <td>N/A</td>
        <td>N/A</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Data MongoDB</td>
        <td>Cosmos DB for MongoDB</td>
        <td>N/A</td>
        <td>N/A</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Data Cassandra</td>
        <td>Cosmos DB for Cassandra</td>
        <td>N/A</td>
        <td>N/A</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Data Redis</td>
        <td>Redis for JEDIS</td>
        <td>N/A</td>
        <td>N/A</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Data Redis</td>
        <td>Redis for Lettuce</td>
        <td>N/A</td>
        <td>N/A</td>
        <td>No</td>
        <td>No</td>
    </tr>
    <tr>
        <td>Spring Security</td>
        <td>Azure AD</td>
        <td>N/A</td>
        <td>N/A</td>
        <td>No</td>
        <td>No</td>
    </tr>
</table>

# Roadmap
|Milestone|ETA|
|:--:|:--:|
|Kafka Managed Identities support|June 24th|
|MySQL Managed Identities support|June 24th|
|Postgre Managed Identities support|July 29th|
|Azure AD Managed Identities support|July 29th|
|Key Vault sample&quickstart&thematic doc support|Aug 26th|
|The rest of MSI sample&quickstart&thematic doc support|Sep 23th|

Note: the Managed Identities support for a third party service includes library ready, reference doc ready, quick-start doc ready and thematic doc ready.