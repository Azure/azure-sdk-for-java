# Azure Communication Phone Numbers client library for Java

The phone numbers package provides capabilities for phone number management.

Purchased phone numbers can come with many capabilities, depending on the country, number type and phone plan. Examples of capabilities are SMS inbound and outbound usage, calling inbound and outbound usage. Phone numbers can also be assigned to a bot via a webhook URL.

[Source code][source] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][product_docs]
## Getting started

### Prerequisites

- An Azure account with an active subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).
- A deployed Communication Services resource. You can use the [Azure Portal](https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource?tabs=windows&pivots=platform-azp) or the [Azure PowerShell](https://docs.microsoft.com/powershell/module/az.communication/new-azcommunicationservice) to set it up.

### Include the package
#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on the General Availability (GA) version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
and then include the direct dependency in the dependencies section without the version tag.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-communication-phonenumbers</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.


[//]: # ({x-version-update-start;com.azure:azure-communication-phonenumbers;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-communication-phonenumbers</artifactId>
  <version>1.0.4</version>
</dependency>
```

## Key concepts

### Initializing Phone Number Client
The PhoneNumberClientBuilder is enabled to use Azure Active Directory Authentication
<!-- embedme ./src/samples/java/com/azure/communication/phonenumbers/ReadmeSamples.java#L54-L64 -->
```java
// You can find your endpoint and access key from your resource in the Azure Portal
String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

// Create an HttpClient builder of your choice and customize it
HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

PhoneNumbersClient phoneNumberClient = new PhoneNumbersClientBuilder()
    .endpoint(endpoint)
    .credential(new DefaultAzureCredentialBuilder().build())
    .httpClient(httpClient)
    .buildClient();
```

Using the endpoint and access key from the communication resource to authenticate is also posible.
<!-- embedme ./src/samples/java/com/azure/communication/phonenumbers/ReadmeSamples.java#L32-L43 -->
```java
// You can find your endpoint and access token from your resource in the Azure Portal
String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
AzureKeyCredential keyCredential = new AzureKeyCredential("SECRET");

// Create an HttpClient builder of your choice and customize it
HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

PhoneNumbersClient phoneNumberClient = new PhoneNumbersClientBuilder()
    .endpoint(endpoint)
    .credential(keyCredential)
    .httpClient(httpClient)
    .buildClient();
```
Alternatively, you can provide the entire connection string using the connectionString() function of the PhoneNumberClientBuilder instead of providing the endpoint and access key.

### Phone Number Types overview

Phone numbers come in two types; Geographic and Toll-Free. Geographic phone plans are phone plans associated with a location, whose phone numbers' area codes are associated with the area code of a geographic location. Toll-Free phone plans are phone plans not associated location. For example, in the US, toll-free numbers can come with area codes such as 800 or 888.

### Searching and Purchasing and Releasing numbers

Phone numbers can be searched through the search creation API by providing an area code, quantity of phone numbers, application type, phone number type, and capabilities. The provided quantity of phone numbers will be reserved for ten minutes and can be purchased within this time. If the search is not purchased, the phone numbers will become available to others after ten minutes. If the search is purchased, then the phone numbers are purchased for the Azure resources.

Phone numbers can also be released using the release API.

## Examples

### Get Purchased Phone Number
Gets the specified purchased phone number.

<!-- embedme ./src/samples/java/com/azure/communication/phonenumbers/ReadmeSamples.java#L76-L78 -->
```java
PurchasedPhoneNumber phoneNumber = phoneNumberClient.getPurchasedPhoneNumber("+18001234567");
System.out.println("Phone Number Value: " + phoneNumber.getPhoneNumber());
System.out.println("Phone Number Country Code: " + phoneNumber.getCountryCode());
```

### Get All Purchased Phone Numbers
Lists all the purchased phone numbers.

<!-- embedme ./src/samples/java/com/azure/communication/phonenumbers/ReadmeSamples.java#L88-L91 -->
```java
PagedIterable<PurchasedPhoneNumber> phoneNumbers = createPhoneNumberClient().listPurchasedPhoneNumbers(Context.NONE);
PurchasedPhoneNumber phoneNumber = phoneNumbers.iterator().next();
System.out.println("Phone Number Value: " + phoneNumber.getPhoneNumber());
System.out.println("Phone Number Country Code: " + phoneNumber.getCountryCode());
```

## Long Running Operations

The Phone Number Client supports a variety of long running operations that allow indefinite polling time to the functions listed down below.

### Search for Available Phone Numbers
Search for available phone numbers by providing the area code, assignment type, phone number capabilities, phone number type, and quantity. The result of the search can then be used to purchase the numbers. Note that for the toll-free phone number type, providing the area code is optional.

<!-- embedme ./src/samples/java/com/azure/communication/phonenumbers/ReadmeSamples.java#L100-L116-->
```java
PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities()
    .setCalling(PhoneNumberCapabilityType.INBOUND)
    .setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND);
PhoneNumberSearchOptions searchOptions = new PhoneNumberSearchOptions().setAreaCode("800").setQuantity(1);

SyncPoller<PhoneNumberOperation, PhoneNumberSearchResult> poller = phoneNumberClient
    .beginSearchAvailablePhoneNumbers("US", PhoneNumberType.TOLL_FREE, PhoneNumberAssignmentType.APPLICATION, capabilities, searchOptions, Context.NONE);
PollResponse<PhoneNumberOperation> response = poller.waitForCompletion();
String searchId = "";

if (LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus()) {
    PhoneNumberSearchResult searchResult = poller.getFinalResult();
    searchId = searchResult.getSearchId();
    System.out.println("Searched phone numbers: " + searchResult.getPhoneNumbers());
    System.out.println("Search expires by: " + searchResult.getSearchExpiresBy());
    System.out.println("Phone number costs:" + searchResult.getCost().getAmount());
}
```

### Purchase Phone Numbers
The result of searching for phone numbers is a `PhoneNumberSearchResult`. This can be used to get the numbers' details and purchase numbers by passing in the `searchId` to the purchase number API.

<!-- embedme ./src/samples/java/com/azure/communication/phonenumbers/ReadmeSamples.java#L118-L120 -->
```java
PollResponse<PhoneNumberOperation> purchaseResponse =
    phoneNumberClient.beginPurchasePhoneNumbers(searchId, Context.NONE).waitForCompletion();
System.out.println("Purchase phone numbers is complete: " + purchaseResponse.getStatus());
```

### Release Phone Number
Releases a purchased phone number.

<!-- embedme ./src/samples/java/com/azure/communication/phonenumbers/ReadmeSamples.java#L128-L130 -->
```java
PollResponse<PhoneNumberOperation> releaseResponse =
    phoneNumberClient.beginReleasePhoneNumber("+18001234567", Context.NONE).waitForCompletion();
System.out.println("Release phone number is complete: " + releaseResponse.getStatus());
```

### Updating Phone Number Capabilities
Updates Phone Number Capabilities for Calling and SMS to one of:
- `PhoneNumberCapabilityValue.NONE`
- `PhoneNumberCapabilityValue.INBOUND`
- `PhoneNumberCapabilityValue.OUTBOUND`
- `PhoneNumberCapabilityValue.INBOUND_OUTBOUND`

<!-- embedme ./src/samples/java/com/azure/communication/phonenumbers/ReadmeSamples.java#L138-L150 -->
```java
PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
capabilities
    .setCalling(PhoneNumberCapabilityType.INBOUND)
    .setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND);

SyncPoller<PhoneNumberOperation, PurchasedPhoneNumber> poller = phoneNumberClient.beginUpdatePhoneNumberCapabilities("+18001234567", capabilities, Context.NONE);
PollResponse<PhoneNumberOperation> response = poller.waitForCompletion();

if (LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus()) {
    PurchasedPhoneNumber phoneNumber = poller.getFinalResult();
    System.out.println("Phone Number Calling capabilities: " + phoneNumber.getCapabilities().getCalling()); //Phone Number Calling capabilities: inbound
    System.out.println("Phone Number SMS capabilities: " + phoneNumber.getCapabilities().getSms()); //Phone Number SMS capabilities: inbound+outbound
}
```

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.


## Troubleshooting

In progress.

## Next steps

Check out other client libraries for Azure communication service

<!-- LINKS -->
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[product_docs]: https://docs.microsoft.com/azure/communication-services/
[package]: https://search.maven.org/artifact/com.azure/azure-communication-phonenumbers
[api_documentation]: https://aka.ms/java-docs
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/communication/azure-communication-phonenumbers/src



![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcommunication%2Fazure-communication-phonenumbers%2FREADME.png)
