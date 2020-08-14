## Guide for migrating to `com.azure.resourcemanager.**` from `com.microsoft.azure.management.**`

This document is intended for users that are familiar with an older version of the Java SDK for managment libraries (`com.microsoft.azure.management.**`) and wish to migrate their application 
to the next version of Azure resource management libraries (`com.azure.resourcemanager.**`)

For users new to the Java SDK for resource management libraries, please see the [README for 'com.azure.resourcemanager.*`](http://aka.ms/azure-sdk-java-mgmt)

## Table of contents

* [Prerequisites](#prerequisites)
* [Updated Maven depedencies](#updated-maven-dependencies)
* [General Changes](#general-changes)
  * [Authentication](#authentication)
  * [Customized Policy](#customized-policy)
  * [Custom HTTP Client](#custom-http-client)
  * [Error Handling](#error-handling)
  * [rxJava -> Reactor](#rxjava-to-reactor)
* [Additional Samples](#additional-samples)

## Prerequisites

Java Development Kit (JDK) with version 8 or above.

## Updated Maven dependencies

The latest dependencies for resource management libraries are [available here](https://azure.github.io/azure-sdk/releases/latest/all/java.html). Please look for packages that contains "azure-resourcemanager" in the namespace.

## General Changes

The latest Azure Java SDK for management libraries is a result of our efforts to create a resource management client library that is user-friendly and idiomatic to the Java ecosystem.

Apart from redesigns resulting from the [new Azure SDK Design Guidelines for Java](DESIGN.md), the latest version improves on several areas from old version.

While conforming to the new guideline, we have tried our best to minimize the breaking changes. Most of the interfaces / classes / methods have stayed the same to offer user an easier migration experience.

The important breaking changes are listed in the following sections:

### Authentication

In old version (`com.microsoft.azure.management.**`), ApplicationTokenCredentials is created with all the credential parameters.

In new version (`com.azure.resourcemanager.**`), in order to provide an unified authentication based on Azure Identity for all Azure Java SDKs, the authentication mechanism has been re-designed and improved to offer a simpler interface. 

To the show the code snippets for the change:

**In old version (`com.microsoft.azure.management.**`)**

```java
ApplicationTokenCredential = new ApplicationTokenCredentials("<ClientId>", "<TenantId>", "<ClientSecret>", AzureEnvironment.AZURE)
    .withDefaultSubscriptionId("<SubscriptionId>");
```        

**Equivalent in new version (`com.azure.resourcemanager.**`)**

```java
TokenCredential credential = new ClientSecretCredentialBuilder()
    .clientId("<ClientId>")
    .clientSecret("<ClientSecret>")
    .tenantId("<TenantId>")
    .build();
AzureProfile profile = new AzureProfile("<TenantId>", "<SubscriptionId>", AzureEnvironment.AZURE);
``` 

In addition to this change, the **support for using auth file has been removed**. In old version, the user can choose to authenticate via the auth file, like this:

**In old version (`com.microsoft.azure.management.**`)**

```java
Azure azure = Azure.authenticate(new File("my.azureauth")).withDefaultSubscription();
```
**In new version, this feature has been removed.** If this creates concern on your side, please file an issue to let us know.

For detailed information on the benefits of using the new authentication classes, please refer to [this page](AUTH.md)

## Customized Policy

Because of adopting Azure Core which is a shared library across all Azure SDKs, there is also a minor change regarding how customized policy in configured. 

In old version (`com.microsoft.azure.management.**`), we use `withInterceptor` and pass the customized interceptor class to the Azure object 

In new version (`com.azure.resourcemanager.**`), we use `WithPolicy` instead and pass the customized policy to the Azure object. It's also worth mentioning that the implementation of `HttpPipelinePolicy` is different from that of `Interceptor` from okhttp. 

So:

**In old version (`com.microsoft.azure.management.**`)**

```java
Azure azure = Azure.configure()
    .withInterceptor(new CustomizedInterceptor())
    .authenticate(credential)
    .withDefaultSubscription();
```

**Equivalent in new version (`com.azure.resourcemanager.**`)**

```java
Azure azure = Azure.configure()
    .withPolicy(new CustomizedPolicy())
    .authenticate(credential, profile)
    .withDefaultSubscription();
```

## Custom HTTP Client

Similar to the customized policy, there are changes regarding how the custom HTTP client is configured as well. The re-designed HTTP client builder in the new version is more flexible and the user can choose their own implementation of HTTP client and plug in what they need into the configuration.

**In old version (`com.microsoft.azure.management.**`)**

```java
OkHttpClient.Builder builder = new OkHttpClient.Builder().proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8888)));
RestClient client = new RestClient.Builder(builder, new Retrofit.Builder())
    .withCredentials(credential)
    .build();

Azure azure = Azure.authenticate(client, "<TenantId>")
    .withDefaultSubscription();
```

**Equivalent in new version (`com.azure.resourcemanager.**`)**

```java
HttpClient client = new OkHttpAsyncHttpClientBuilder()
    .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("127.0.0.1", 8888)))
    .build();

Azure azure = Azure.configure()
    .withHttpClient(client)
    .authenticate(credential, profile)
    .withDefaultSubscription();
```    
    
## Error Handling

There is a minor namespace change in the exception class. To be specific, the previous `CloudException` has been re-named to `ManagementException`. 

**In old version (`com.microsoft.azure.management.**`)**

```java
final String resourceGroupName = "invalid resource group name";
try {
    azure.resourceGroups().define(resourceGroupName)
        .withRegion(Region.US_WEST2)
        .create();
} catch (CloudException e) {
    System.err.printf("Response code: %s%n", e.body().code());
    System.err.printf("Response message: %s%n", e.body().message());
}
```

**Equivalent in new version (`com.azure.resourcemanager.**`)**

```java
final String resourceGroupName = "invalid resource group name";
try {
    azure.resourceGroups().define(resourceGroupName)
        .withRegion(Region.US_WEST2)
        .create();
} catch (ManagementException e) {
    System.err.printf("Response code: %s%n", e.getValue().getCode());
    System.err.printf("Response message: %s%n", e.getValue().getMessage());
}
```

## rxJava to Reactor

In old version (`com.microsoft.azure.management.**`), `rxJava` is used for non-blocking applications

In new version (`com.azure.resourcemanager.**`), we have adopted `Reactor` as the main library in replacement of `rxJava` due to the Azure Core adoption.

**In old version (`com.microsoft.azure.management.**`)**

```java
azure.publicIPAddresses()
    .define(publicIpName)
    .withRegion(region)
    .withExistingResourceGroup(rgName)
    .withLeafDomainLabel(publicIpName)
    .createAsync().flatMap(new Func1 < Indexable, Observable < Indexable >> () {
        @Override
        public Observable < Indexable > call(Indexable indexable) {
            if (indexable instanceof PublicIPAddress) {
                PublicIPAddress publicIp = (PublicIPAddress) indexable;
                //=============================================================
                // Create an Internet facing load balancer with
                // One frontend IP address
                // Two backend address pools which contain network interfaces for the virtual
                //  machines to receive HTTP and HTTPS network traffic from the load balancer
                // Two load balancing rules for HTTP and HTTPS to map public ports on the load
                //  balancer to ports in the backend address pool
                // Two probes which contain HTTP and HTTPS health probes used to check availability
                //  of virtual machines in the backend address pool
                // Three inbound NAT rules which contain rules that map a public port on the load
                //  balancer to a port for a specific virtual machine in the backend address pool
                //  - this provides direct VM connectivity for SSH to port 22 and TELNET to port 23
                
                return Observable.merge(
                    Observable.just(indexable), azure.loadBalancers().define(loadBalancerName1).withRegion(region).withExistingResourceGroup(rgName)
                    // Add two rules that uses above backend and probe     
                    .defineLoadBalancingRule(httpLoadBalancingRule).withProtocol(TransportProtocol.TCP).fromFrontend(frontendName).fromFrontendPort(80).toBackend(backendPoolName1).withProbe(httpProbe).attach().defineLoadBalancingRule(httpsLoadBalancingRule).withProtocol(TransportProtocol.TCP).fromFrontend(frontendName).fromFrontendPort(443).toBackend(backendPoolName2).withProbe(httpsProbe).attach()
                    // Add nat pools to enable direct VM connectivity for
                    // SSH to port 22 and TELNET to port 23

                    .defineInboundNatPool(natPool50XXto22).withProtocol(TransportProtocol.TCP).fromFrontend(frontendName).fromFrontendPortRange(5000, 5099).toBackendPort(22).attach().defineInboundNatPool(natPool60XXto23).withProtocol(TransportProtocol.TCP).fromFrontend(frontendName).fromFrontendPortRange(6000, 6099).toBackendPort(23).attach()

                    // Explicitly define the frontend
                    .definePublicFrontend(frontendName).withExistingPublicIPAddress(publicIp).attach

                    // Add two probes one per rule
                    .defineHttpProbe(httpProbe).withRequestPath("/").withPort(80).attach().defineHttpProbe(httpsProbe).withRequestPath("/").withPort(443).attach()
                    .createAsync());
            }
            return Observable.just(indexable);
        }
    })).toBlocking().subscribe(new Action1 < Indexable > () {
    @Override
    public void call(Indexable indexable) {
        createdResources.add(indexable);
    }
});
``` 

[**Link to full sample**](https://github.com/Azure/azure-libraries-for-java/blob/master/azure-samples/src/main/java/com/microsoft/azure/management/compute/samples/ManageVirtualMachineScaleSetAsync.java#L100)
      

**Equivalent in new version (`com.azure.resourcemanager.**`)**

```java
final List < Indexable > createdResources = new ArrayList < > ();
azure.resourceGroups().define(rgName).withRegion(region).create();
Flux.merge(
    azure.networks()
    .define(vnetName)
    .withRegion(region)
    .withExistingResourceGroup(rgName)
    .withAddressSpace("172.16.0.0/16")
    .defineSubnet("Front-end")
    .withAddressPrefix("172.16.1.0/24")
    .attach()
    .createAsync(),
    azure.publicIpAddresses()
    .define(publicIpName)
    .withRegion(region)
    .withExistingResourceGroup(rgName)
    .withLeafDomainLabel(publicIpName)
    .createAsync().flatMap(indexable - > {
        if (indexable instanceof PublicIpAddress) {
            PublicIpAddress publicIp = (PublicIpAddress) indexable;
            //=============================================================
            // Create an Internet facing load balancer with
            // One frontend IP address
            // Two backend address pools which contain network interfaces for the virtual
            //  machines to receive HTTP and HTTPS network traffic from the load balancer
            // Two load balancing rules for HTTP and HTTPS to map public ports on the load
            //  balancer to ports in the backend address pool
            // Two probes which contain HTTP and HTTPS health probes used to check availability
            //  of virtual machines in the backend address pool
            // Three inbound NAT rules which contain rules that map a public port on the load
            //  balancer to a port for a specific virtual machine in the backend address pool
            //  - this provides direct VM connectivity for SSH to port 22 and TELNET to port 23
            
            return Flux.merge(
                Flux.just(indexable), azure.loadBalancers().define(loadBalancerName1).withRegion(region).withExistingResourceGroup(rgName)
                // Add two rules that uses above backend and probe
                .defineLoadBalancingRule(httpLoadBalancingRule).withProtocol(TransportProtocol.TCP).fromFrontend(frontendName).fromFrontendPort(80).toBackend(backendPoolName1).withProbe(httpProbe).attach().defineLoadBalancingRule(httpsLoadBalancingRule).withProtocol(TransportProtocol.TCP).fromFrontend(frontendName).fromFrontendPort(443).toBackend(backendPoolName2).withProbe(httpsProbe).attach()
                // Add nat pools to enable direct VM connectivity for
                //  SSH to port 22 and TELNET to port 23
                .defineInboundNatPool(natPool50XXto22).withProtocol(TransportProtocol.TCP).fromFrontend(frontendName).fromFrontendPortRange(5000, 5099).toBackendPort(22).attach().defineInboundNatPool(natPool60XXto23).withProtocol(TransportProtocol.TCP).fromFrontend(frontendName).fromFrontendPortRange(6000, 6099).toBackendPort(23).attach()

                // Explicitly define the frontend
                .definePublicFrontend(frontendName).withExistingPublicIpAddress(publicIp).attach()

                // Add two probes one per rule
                .defineHttpProbe(httpProbe).withRequestPath("/").withPort(80).attach().defineHttpProbe(httpsProbe).withRequestPath("/").withPort(443).attach().createAsync());
        }
        return Flux.just(indexable);
    })).flatMap(indexable - > {
    createdResources.add(indexable);
    return Flux.just(indexable);
}).last().block();
```

[**Link to full sample**](https://github.com/Azure/azure-sdk-for-java/blob/7beda69/sdk/management/samples/src/main/java/com/azure/resourcemanager/compute/samples/ManageVirtualMachineScaleSetAsync.java#L98)

## Additional Samples 

More samples can be found at :
- [README for new version of SDK](http://aka.ms/azure-sdk-java-mgmt)
- [Code Samples for Resource Management Libraries](SAMPLE.md)
- [Authentication Documentation](AUTH.md)

## Need help?

If you have encountered an issue during migration, please file an issue via [Github Issues](https://github.com/Azure/azure-sdk-for-java/issues) and make sure you add the "Preview" label to the issue
