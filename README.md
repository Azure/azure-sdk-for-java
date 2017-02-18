[![Build Status](https://travis-ci.org/Azure/autorest-clientruntime-for-java.svg?branch=javavnext)](https://travis-ci.org/Azure/autorest-clientruntime-for-java)

# AutoRest Client Runtimes for Java
The runtime libraries for [AutoRest](https://github.com/azure/autorest) generated Java clients. 

## Usage

### Prerequisites

- JDK 1.7

### Download

```xml
<dependency>
  <groupId>com.microsoft.rest</groupId>
  <artifactId>client-runtime</artifactId>
  <version>1.0.0</version>
</dependency>

<dependency>
  <groupId>com.microsoft.azure</groupId>
  <artifactId>azure-client-runtime</artifactId>
  <version>1.0.0</version>
</dependency>

<dependency>
  <groupId>com.microsoft.azure</groupId>
  <artifactId>azure-client-authentication</artifactId>
  <version>1.0.0-beta6-SNAPSHOT</version>
</dependency>
```

### Create a RestClient

```java
// For Java genenerator
RestClient simpleClient = new RestClient.Builder()
    .withBaseUrl("http://localhost")
    .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
  	.withSerializerAdapter(new JacksonAdapter())
  	.build();
AutoRestJavaClient client1 = new AutoRestJavaClientImpl(simpleClient);

// For Azure.Java generator
RestClient azureClient = new RestClient.Builder()
    .withBaseUrl(AzureEnvironment.Azure, Endpoint.RESOURCE_MANAGER)
    .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
  	.withSerializerAdapter(new AzureJacksonAdapter())
    .withCredentials(AzureCliCredentials.create())
  	.build();
FooServiceClient client2 = new FooServiceClientImpl(azureClient);

// For Azure SDK users
Azure azure = Azure.authenticate(azureClient).withDefaultSubscription();
```

## Components

### client-runtime
This is the generic runtime. Add this package as a dependency if you are using `Java` generator in AutoRest. This package depends on [Retrofit](https://github.com/square/retrofit), [OkHttp](https://github.com/square/okhttp), [Jackson](http://wiki.fasterxml.com/JacksonHome), [RxJava](https://github.com/ReactiveX/RxJava) for making and processing REST requests.

### azure-client-runtime
This is the runtime with Azure specific customizations. Add this package as a dependency if you are using `Azure.Java` or `Azure.Java.Fluent` generator in AutoRest.

This combination provides a set of Azure specific behaviors, including long running operations, special handling of HEAD operations, and paginated `list()` calls.

### azure-client-authentication (beta)
This package provides access to Active Directory authentication on JDK using OrgId or application ID / secret combinations. There are currently 3 types of authentication provided:

- Service principal authentication: `ApplicationTokenCredentials`
- Username / password login without multi-factor auth: `UserTokenCredentials`
- Use the credentials logged in [Azure CLI](https://github.com/azure/azure-cli): `AzureCliCredentials`

### azure-android-client-authentication (beta)
This package provides access to Active Directory authentication on Android. You can login with Microsoft accounts, OrgId, with or without multi-factor auth.

## Build
To build this repository, you will need maven 2.0+ and gradle 1.6+.
Maven is used for [Java SDK](https://github.com/Azure/azure-sdk-for-java) when it's used as a submodule in there. Gradle is used for [AutoRest](https://github.com/Azure/autorest) when it's used as a submodule in there.

## Contributing
This repository is for runtime & authentication specifically. For issues in the generated code, please report in [AutoRest](https://github.com/Azure/autorest). For bugs in the Azure SDK, please report in [Azure SDK for Java](https://github.com/Azure/azure-sdk-for-java). If you are unsure, please file here and state that clearly in the issue. Pull requests are welcomed with clear Javadocs.
