# Azure ProgrammableConnectivity client library for Java

Azure ProgrammableConnectivity client library for Java.

This package contains Microsoft Azure ProgrammableConnectivity client library.

## Documentation

Various documentation is available to help you get started

- [Source code](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/communication/azure-communication-programmableconnectivity/src) 
- Package (NuGet): `https://www.nuget.org/packages/Azure.Communication.ProgrammableConnectivity` 
- [API reference documentation](https://azure.github.io/azure-sdk-for-java) 
- [APC Product documentation](https://learn.microsoft.com/en-us/azure/programmable-connectivity/)


## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-communication-programmableconnectivity;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-communication-programmableconnectivity</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

[Azure Identity][azure_identity] package provides the default implementation for authenticating the client.

## Key concepts

## Examples

```java com.azure.communication.programmableconnectivity.readme
// Initialize the Programmable Connectivity client
String endpoint = "https://your-resource-name.communication.azure.com";
String gatewayId = "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/your-resource-group/providers/Private.programmableconnectivity/gateways/your-gateway";

// Example 1: Verify device location
DeviceLocationClient deviceLocationClient = new ProgrammableConnectivityClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint(endpoint)
    .buildDeviceLocationClient();

// Prepare the device information
LocationDevice device = new LocationDevice().setPhoneNumber("+12065550100");

// Create network identifier (usually NetworkCode, IPv4, or IPv6)
NetworkIdentifier networkId = new NetworkIdentifier("NetworkCode", "YourOperatorNetwork");

// Create location verification content with coordinates
DeviceLocationVerificationContent verificationContent = new DeviceLocationVerificationContent(
    networkId,    // Network identifier
    47.6062,      // Latitude
    -122.3321,    // Longitude
    50,           // Accuracy in meters
    device        // Device information
);

// Execute the verification request
DeviceLocationVerificationResult locationResult = deviceLocationClient.verify(gatewayId, verificationContent);
System.out.println("Location verification result: " + locationResult.isVerificationResult());

// Example 1 (Async): Verify device location asynchronously
DeviceLocationAsyncClient deviceLocationAsyncClient = new ProgrammableConnectivityClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint(endpoint)
    .buildDeviceLocationAsyncClient();
    
// Execute the verification request asynchronously
deviceLocationAsyncClient.verify(gatewayId, verificationContent)
    .subscribe(asyncResult -> {
        System.out.println("Async location verification result: " + asyncResult.isVerificationResult());
    }, error -> {
        System.err.println("Async location verification failed: " + error.getMessage());
    });

// Example 2: Retrieve device network information
DeviceNetworkClient deviceNetworkClient = new ProgrammableConnectivityClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint(endpoint)
    .buildDeviceNetworkClient();

// Create a network identifier (IPv4, IPv6, or NetworkCode)
NetworkIdentifier ipNetworkId = new NetworkIdentifier("IPv4", "203.0.113.45");

// Retrieve network information
NetworkRetrievalResult networkInfo = deviceNetworkClient.retrieve(gatewayId, ipNetworkId);

// Process the network information
if (networkInfo != null && networkInfo.getNetworkCode() != null) {
    System.out.println("Network code: " + networkInfo.getNetworkCode());
}

// Example 2 (Async): Retrieve device network information asynchronously
DeviceNetworkAsyncClient deviceNetworkAsyncClient = new ProgrammableConnectivityClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint(endpoint)
    .buildDeviceNetworkAsyncClient();
    
// Retrieve network information asynchronously
deviceNetworkAsyncClient.retrieve(gatewayId, ipNetworkId)
    .subscribe(asyncNetworkInfo -> {
        if (asyncNetworkInfo != null && asyncNetworkInfo.getNetworkCode() != null) {
            System.out.println("Async network code: " + asyncNetworkInfo.getNetworkCode());
        }
    }, error -> {
        System.err.println("Async network retrieval failed: " + error.getMessage());
    });

// Example 3: SIM Swap verification
SimSwapClient simSwapClient = new ProgrammableConnectivityClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint(endpoint)
    .buildSimSwapClient();
    
// Set up verification with phone number and max age (hours)
SimSwapVerificationContent verifyContent = new SimSwapVerificationContent(networkId)
    .setPhoneNumber("12065550100")
    .setMaxAgeHours(120);
    
// Execute verification request
SimSwapVerificationResult verifyResult = simSwapClient.verify(gatewayId, verifyContent);
System.out.println("SIM swap verification result: " + verifyResult.isVerificationResult());

// Example 3 (Async): SIM Swap verification asynchronously
SimSwapAsyncClient simSwapAsyncClient = new ProgrammableConnectivityClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint(endpoint)
    .buildSimSwapAsyncClient();
    
// Execute verification request asynchronously
simSwapAsyncClient.verify(gatewayId, verifyContent)
    .subscribe(asyncVerifyResult -> {
        System.out.println("Async SIM swap verification result: " + asyncVerifyResult.isVerificationResult());
    }, error -> {
        System.err.println("Async SIM swap verification failed: " + error.getMessage());
    });

// Example 4: Retrieve SIM Swap information
SimSwapRetrievalContent retrieveContent = new SimSwapRetrievalContent(networkId)
    .setPhoneNumber("12065550100");
    
// Execute retrieval request
SimSwapRetrievalResult swapResult = simSwapClient.retrieve(gatewayId, retrieveContent);

// Process the SIM swap date information
OffsetDateTime swapDate = swapResult.getDate();
if (swapDate != null) {
    String formattedDate = swapDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    System.out.println("SIM swap date: " + formattedDate);
} else {
    System.out.println("No SIM swap date available");
}

// Example 4 (Async): Retrieve SIM Swap information asynchronously
simSwapAsyncClient.retrieve(gatewayId, retrieveContent)
    .subscribe(asyncSwapResult -> {
        // Process the SIM swap date information
        OffsetDateTime asyncSwapDate = asyncSwapResult.getDate();
        if (asyncSwapDate != null) {
            String formattedDate = asyncSwapDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            System.out.println("Async SIM swap date: " + formattedDate);
        } else {
            System.out.println("Async - No SIM swap date available");
        }
    }, error -> {
        System.err.println("Async SIM swap retrieval failed: " + error.getMessage());
    });

// Example 5: Number verification with authentication flow
NumberVerificationClient numberVerificationClient = new ProgrammableConnectivityClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint(endpoint)
    .buildNumberVerificationClient();
    
// Start the verification flow (this initiates a redirect to operator authentication)
String redirectUri = "https://your-app.example.com/callback";
NumberVerificationWithoutCodeContent initialContent = new NumberVerificationWithoutCodeContent(
    networkId, redirectUri).setPhoneNumber("12065550100");
    
// Create options to prevent auto-following redirects
RequestOptions requestOptions = new RequestOptions();

// Make the initial request (in a real app, this would redirect to operator)
Response<Void> initialResponse = numberVerificationClient.verifyWithoutCodeWithResponse(
    gatewayId, BinaryData.fromObject(initialContent), requestOptions);
    
// After user authenticates with operator, your callback endpoint receives a code
// You then complete verification using that code:
String apcCode = "apc_received_from_operator"; // This would come from your callback handler

// Final verification step with the code
NumberVerificationWithCodeContent finalContent = new NumberVerificationWithCodeContent(apcCode);
NumberVerificationResult numberResult = numberVerificationClient.verifyWithCode(gatewayId, finalContent);

System.out.println("Number verification result: " + numberResult.isVerificationResult());

// Example 5 (Async): Number verification with authentication flow asynchronously
NumberVerificationAsyncClient numberVerificationAsyncClient = new ProgrammableConnectivityClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint(endpoint)
    .buildNumberVerificationAsyncClient();
    
// Make the initial async request (in a real app, this would redirect to operator)
Mono<Response<Void>> initialResponseMono = numberVerificationAsyncClient.verifyWithoutCodeWithResponse(
    gatewayId, BinaryData.fromObject(initialContent), requestOptions);
    
initialResponseMono.subscribe(asyncInitialResponse -> {
    System.out.println("Async initial response status: " + asyncInitialResponse.getStatusCode());
    
    // In a real application, you would handle the redirect and auth callback flow
    // Then, once you have the APC code from the callback:
    
    // Example of the final verification step (would be in a separate method/callback in real app)
    String asyncApcCode = "apc_received_from_operator"; // This would come from your callback handler
    NumberVerificationWithCodeContent asyncFinalContent = new NumberVerificationWithCodeContent(asyncApcCode);
    
    numberVerificationAsyncClient.verifyWithCode(gatewayId, asyncFinalContent)
        .subscribe(asyncNumberResult -> {
            System.out.println("Async number verification result: " + asyncNumberResult.isVerificationResult());
        }, error -> {
            System.err.println("Async final verification failed: " + error.getMessage());
        });
}, error -> {
    System.err.println("Async initial verification failed: " + error.getMessage());
});

// Example of using block() for testing or synchronous code paths (not recommended for production)
// This is just to show how you might use async clients in a synchronous context if needed
try {
    NumberVerificationResult blockingResult = numberVerificationAsyncClient
        .verifyWithCode(gatewayId, finalContent)
        .block(); // Blocks until the operation completes - only for demonstration
    
    System.out.println("Blocking result from async client: " + blockingResult.isVerificationResult());
} catch (Exception e) {
    System.err.println("Error in blocking operation: " + e.getMessage());
}
```

### Service API versions

The client library targets the latest service API version by default.
The service client builder accepts an optional service API version parameter to specify which API version to communicate.

#### Select a service API version

You have the flexibility to explicitly select a supported service API version when initializing a service client via the service client builder.
This ensures that the client can communicate with services using the specified API version.

When selecting an API version, it is important to verify that there are no breaking changes compared to the latest API version.
If there are significant differences, API calls may fail due to incompatibility.

Always ensure that the chosen API version is fully supported and operational for your specific use case and that it aligns with the service's versioning policy.

## Troubleshooting

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[product_documentation]: https://azure.microsoft.com/services/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
