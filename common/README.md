[![Build Status](https://travis-ci.org/Azure/autorest-clientruntime-for-java.svg?branch=v2)](https://travis-ci.org/Azure/autorest-clientruntime-for-java)

# AutoRest Client Runtimes for Java
The runtime libraries for [AutoRest](https://github.com/azure/autorest) generated Java clients. 

## Usage

### Prerequisites

- JDK 1.8

### Download

```xml
<dependencies>
    <!-- For generic, non-Azure Resource Management users --> 
    <dependency>
      <groupId>com.microsoft.rest.v3</groupId>
      <artifactId>client-runtime</artifactId>
      <version>2.0.0-beta4</version>
    </dependency>
    
    <!-- For Azure Resource Management users -->
    <dependency>
      <groupId>com.microsoft.azure.v3</groupId>
      <artifactId>azure-client-runtime</artifactId>
      <version>2.0.0-beta4</version>
    </dependency>
    
    <dependency>
      <groupId>com.microsoft.azure.v3</groupId>
      <artifactId>azure-client-authentication</artifactId>
      <version>2.0.0-beta4</version>
    </dependency>
    
    <!-- Below are optional high-performance native dependencies  -->

    <!-- Available on Windows/Mac/Linux x86_64 -->
    <dependency>
      <groupId>io.netty</groupId>
      <artifactId>netty-tcnative-boringssl-static</artifactId>
      <version>2.0.8.Final</version>
      <classifier>${os.detected.classifier}</classifier>
    </dependency>

    <!-- Only available on Linux -->
    <dependency>
      <groupId>io.netty</groupId>
      <artifactId>netty-transport-native-epoll</artifactId>
      <version>4.1.23.Final</version>
      <classifier>${os.detected.classifier}</classifier>
    </dependency>

    <!-- Only available on macOS/BSD -->
    <dependency>
      <groupId>io.netty</groupId>
      <artifactId>netty-transport-native-kqueue</artifactId>
      <version>4.1.23.Final</version>
      <classifier>${os.detected.classifier}</classifier>
    </dependency>
</dependencies>

<!-- Allows automatic detection of OS for native modules -->
<build>
  <extensions>
    <extension>
      <groupId>kr.motd.maven</groupId>
      <artifactId>os-maven-plugin</artifactId>
      <version>1.6.0</version>
    </extension>
  </extensions>
</build>
```

### Usage

Non-Azure generated clients will have a constructor that takes no arguments for simple scenarios, while Azure generated clients will require a `ServiceClientCredentials` argument at a minimum.

If you want to have more control over configuration, consider using HttpPipeline. This enables performing transformations on all HTTP messages sent by a client, similar to interceptors or filters in other HTTP clients.

You can build an HttpPipeline out of a sequence of RequestPolicyFactories. These policies will get applied in-order to outgoing requests, and then in reverse order for incoming responses. HttpPipelineBuilder includes convenience methods for adding several built-in RequestPolicyFactories, including policies for credentials, logging, response decoding (deserialization), cookies support, and several others.

```java
// For Java generator
HttpPipeline pipeline = new HttpPipelineBuilder()
    .withHostPolicy("http://localhost")
    .withDecodingPolicy()
    .build();
AutoRestJavaClient client = new AutoRestJavaClientImpl(pipeline);

// For Azure.Java generator
HttpPipeline azurePipeline = new HttpPipelineBuilder()
    .withCredentialsPolicy(AzureCliCredentials.create())
    .withHttpLoggingPolicy(HttpLogDetailLevel.HEADERS)
    .withDecodingPolicy()
    .build();
FooServiceClient azureClient = new FooServiceClientImpl(azurePipeline);
```

## Components

### client-runtime
This is the generic runtime. Add this package as a dependency if you are using `Java` generator in AutoRest. This package depends on [Netty](https://github.com/netty/netty), [Jackson](http://wiki.fasterxml.com/JacksonHome), and [RxJava](https://github.com/ReactiveX/RxJava) for making and processing REST requests.

### azure-client-runtime
This is the runtime with Azure Resource Management customizations. Add this package as a dependency if you are using `--azure-arm` or `--azure-arm --fluent` generator flags in AutoRest.

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

## Contributing
This repository is for runtime & authentication specifically. For issues in the generated code, please report in [AutoRest](https://github.com/Azure/autorest). For bugs in the Azure SDK, please report in [Azure SDK for Java](https://github.com/Azure/azure-sdk-for-java). If you are unsure, please file here and state that clearly in the issue. Pull requests are welcomed with clear Javadocs.
