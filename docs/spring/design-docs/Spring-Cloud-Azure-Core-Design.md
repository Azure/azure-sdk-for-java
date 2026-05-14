## Overview

Spring Cloud Azure 4.0 will organize our projects by different Spring abstractions, such as Spring Boot, Spring Data and etc. But all these different abstractions will sit on top of different kinds of Azure SDKs and if they talk directly to the SDKs it will bring us challenges of dealing with heterogeneous SDKs. This urges us to bring in an `Spring Cloud Azure Core` project which to provide an abstraction layer between upper Azure Spring projects and Azure SDKs. Since all Azure SDKs build on top of the `Azure Core` project, and `Spring` projects on top of the `Spring Framework` project, so our `Spring Cloud Azure Core` project will take dependency on these two projects.

## Goal  
The goals of this project are:  
- Provide a common template of configuring all the Azure SDK clients, no matter which protocol it uses, HTTP or AMQP.
- Provide the ability to configure every single configuration item the Azure SDK clients expose. To be more specific:
  * All the authentication methods each Service or SDK supports.
  * Proxy configurations.
  * Retry configurations.
  * Logging configurations.
  * Underlying client configurations, for example, HttpClient.
  * Other sdk configurations.
- Provide an extensible way for replacing Azure SDK provided implementations with Spring common implementations, such as using WebClient as HttpClient and configuring loggings with Spring properties.
- Provide abstraction support for upper-layer functions, such as:
  * `spring-retry` support
  * micrometer
  * tracing

## Dependency

The project will take dependency on `spring-core`, `spring-context`, `azure-core`, `azure-identity`, and `azure-core-management`.

![dependency-mermaid]

## Implementation

### Common Properties Apply to All SDKs
Define an `AzureProperties` class that contains all the common properties Azure SDK clients use.
[[/spring/design-docs/resources/AzureProperties.png]]

For each specific client define a `service properties class` extends the `AzureProperties`, such as `StorageBlobProperties`.

### Properties Apply to Some SDKs
Define a couple of `Aware` interfaces, when a specific `service properties class` implements these interfaces it indicates this service has such ability. For example the Storage Blob could configure SAS token and connection string, the `StorageBlobProperties` could implement `SasTokenAware` and `ConnectionStringAware`.

[[/spring/design-docs/resources/Aware.png]]

### Builder Factory
Since the Azure SDKs use a builder pattern to build all clients, so it's natural for us to provide a template to configure the builders. So we define the top interface:
```java
public interface AzureServiceClientBuilderFactory<T> {

    T build();
}
```
The output of this factory is a client builder.

#### The AbstractAzureServiceClientBuilderFactory
Creating an abstract implementation of the `AzureServiceClientBuilderFactory` to provide the template for configuring client builders for all SDK clients.

```java
public abstract class AbstractAzureServiceClientBuilderFactory<T> implements AzureServiceClientBuilderFactory<T> {

    public T build() {
        T builder = createBuilderInstance();
        configureCore(builder);
        configureService(builder);
        customizeBuilder(builder);
        return builder;
    }

    protected void configureCore(T builder) {
        configureApplicationId(builder);
        configureAzureEnvironment(builder);
        configureRetry(builder);
        configureProxy(builder);
        configureCredential(builder);
        configureConnectionString(builder);
        configureDefaultCredential(builder);
    }
}
```

![[/spring/design-docs/resources/BuilderFactory.png]]

### Authentication
Each Service/SDK supports a bunch of authentication methods, such as use `TokenCredential` as a bearer token, `SasCredential` as a shared access signature, and etc. We need to provide a way to let each builder factory define the supported authentication methods, the way to resolve such credentials from `AzureProperties`, and at last how to set the credential to the builder.

To achieve this, we define an interface called `AuthenticationDescriptor` describing the three elements mentioned above.
```java
public interface AuthenticationDescriptor<T extends AzureCredentialProvider<?>> {

    AzureCredentialType azureCredentialType();

    AzureCredentialResolver<T> azureCredentialResolver();

    Consumer<T> consumer();
}
```
Each builder factory provides a list of `AuthenticationDescriptor` so the builder factory will try to resolve the first available authentication credential and set it to the builder.
```java
protected void configureCredential(T builder) {
        List<AuthenticationDescriptor<?>> descriptors = getAuthenticationDescriptors(builder);
        AzureCredentialProvider<?> azureCredentialProvider = resolveAzureCredential(getAzureProperties(), descriptors);
        final Consumer consumer = descriptors.stream()
                                             .filter(d -> d.azureCredentialType() == azureCredentialProvider.getType())
                                             .map(AuthenticationDescriptor::consumer)
                                             .findFirst()
                                             .orElseThrow(
                                                 () -> new IllegalArgumentException("Consumer should not be null"));


        consumer.accept(azureCredentialProvider.getCredential());
    }
```


[dependency-mermaid]:https://mermaid.ink/img/eyJjb2RlIjoiZmxvd2NoYXJ0IFREIFxuXG5zcHJpbmctY2xvdWQtYXp1cmUtY29yZSAtLT4gYXp1cmUtY29yZVxuc3ByaW5nLWNsb3VkLWF6dXJlLWNvcmUgLS0-IGF6dXJlLWNvcmUtbWFuYWdlbWVudFxuc3ByaW5nLWNsb3VkLWF6dXJlLWNvcmUgLS0-IGF6dXJlLWlkZW50aXR5XG5zcHJpbmctY2xvdWQtYXp1cmUtY29yZSAtLT4gc3ByaW5nLWNvcmVcbnNwcmluZy1jbG91ZC1henVyZS1jb3JlIC0tPiBzcHJpbmctY29udGV4dFxuXG4lJT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PVxuY2xhc3NEZWYgZ3JlZW4gIGZpbGw6IzlmNlxuY2xhc3NEZWYgcGluayAgIGZpbGw6I0ZGNjlCNFxuY2xhc3NEZWYgeWVsbG93IGZpbGw6I2ZmMFxuXG5jbGFzcyBhenVyZS1jb3JlLW1hbmFnZW1lbnQsYXp1cmUtY29yZSxhenVyZS1pZGVudGl0eSB5ZWxsb3dcbmNsYXNzIHNwcmluZy1jb250ZXh0LHNwcmluZy1jb3JlIGdyZWVuXG4iLCJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJ1cGRhdGVFZGl0b3IiOmZhbHNlLCJhdXRvU3luYyI6dHJ1ZSwidXBkYXRlRGlhZ3JhbSI6ZmFsc2V9
[dependency-mermaid-live-editor]:https://mermaid-js.github.io/mermaid-live-editor/edit#eyJjb2RlIjoiZmxvd2NoYXJ0IFREIFxuXG5zcHJpbmctY2xvdWQtYXp1cmUtY29yZSAtLT4gYXp1cmUtY29yZVxuc3ByaW5nLWNsb3VkLWF6dXJlLWNvcmUgLS0-IGF6dXJlLWNvcmUtbWFuYWdlbWVudFxuc3ByaW5nLWNsb3VkLWF6dXJlLWNvcmUgLS0-IGF6dXJlLWlkZW50aXR5XG5zcHJpbmctY2xvdWQtYXp1cmUtY29yZSAtLT4gc3ByaW5nLWNvcmVcbnNwcmluZy1jbG91ZC1henVyZS1jb3JlIC0tPiBzcHJpbmctY29udGV4dFxuXG4lJT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PVxuY2xhc3NEZWYgZ3JlZW4gIGZpbGw6IzlmNlxuY2xhc3NEZWYgcGluayAgIGZpbGw6I0ZGNjlCNFxuY2xhc3NEZWYgeWVsbG93IGZpbGw6I2ZmMFxuXG5jbGFzcyBhenVyZS1jb3JlLW1hbmFnZW1lbnQsYXp1cmUtY29yZSxhenVyZS1pZGVudGl0eSB5ZWxsb3dcbmNsYXNzIHNwcmluZy1jb250ZXh0LHNwcmluZy1jb3JlIGdyZWVuXG4iLCJtZXJtYWlkIjoie1xuICBcInRoZW1lXCI6IFwiZGVmYXVsdFwiXG59IiwidXBkYXRlRWRpdG9yIjpmYWxzZSwiYXV0b1N5bmMiOnRydWUsInVwZGF0ZURpYWdyYW0iOmZhbHNlfQ