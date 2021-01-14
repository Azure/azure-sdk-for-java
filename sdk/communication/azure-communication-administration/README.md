# Azure Communication Administration client library for Java

The administration package is used for managing users and tokens for Azure Communication Services. This package also provides capabilities for Phone Number Administration.

Acquired phone numbers can come with many capabilities, depending on the country, number type and phone plan. Examples of capabilities are SMS inbound and outbound usage, PSTN inbound and outbound usage. Phone numbers can also be assigned to a bot via a webhook URL.

[Source code][source] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][product_docs]
## Getting started

### Prerequisites

- An Azure account with an active subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).
- A deployed Communication Services resource. You can use the [Azure Portal](https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource?tabs=windows&pivots=platform-azp) or the [Azure PowerShell](https://docs.microsoft.com/powershell/module/az.communication/new-azcommunicationservice) to set it up.

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-communication-administration;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-communication-administration</artifactId>
  <version>1.0.0-beta.3</version>
</dependency>
```

## Key concepts

There are two forms of authentication to use the Administration SDK:

### Azure Active Directory Token Authentication
Currently, Azure Active Directory (AAD) authentication is only possible for CommunicationIdentityClient.
The `DefaultAzureCredential` object must be passed to the `CommunicationIdentityClientBuilder` via
the credential() funtion. Endpoint and httpClient must also be set
via the endpoint() and httpClient() functions respectively.

`AZURE_CLIENT_SECRET`, `AZURE_CLIENT_ID` and `AZURE_TENANT_ID` environment variables 
are needed to create a DefaultAzureCredential object.

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L377-L389 -->
```java
    String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

    // Create an HttpClient builder of your choice and customize it
    HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

    CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
        .endpoint(endpoint)
        .credential(new DefaultAzureCredentialBuilder().build())
        .httpClient(httpClient)
        .buildClient();

    return communicationIdentityClient;
}
```

### Access Key Authentication
Administration uses HMAC authentication with the resource access key.
The access key must be provided to the `CommunicationIdentityClientBuilder`
or the `PhoneNumberClientBuilder` via the accessKey() function. Endpoint and httpClient must also be set
via the endpoint() and httpClient() functions respectively.

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L37-L48 -->
```java
// You can find your endpoint and access key from your resource in the Azure Portal
String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
String accessKey = "SECRET";

// Create an HttpClient builder of your choice and customize it
HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
    .endpoint(endpoint)
    .accessKey(accessKey)
    .httpClient(httpClient)
    .buildClient();
```

Alternatively, you can provide the entire connection string using the connectionString() function instead of providing the endpoint and access key. 
<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L62-L68 -->
```java
// Your can find your connection string from your resource in the Azure Portal
String connectionString = "<connection_string>";

CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
    .connectionString(connectionString)
    .httpClient(httpClient)
    .buildClient();
```
### Initializing Phone Number Client

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L128-L139 -->
```java
// You can find your endpoint and access token from your resource in the Azure Portal
String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
String accessKey = "SECRET";

// Create an HttpClient builder of your choice and customize it
HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

PhoneNumberClient phoneNumberClient = new PhoneNumberClientBuilder()
    .endpoint(endpoint)
    .accessKey(accessKey)
    .httpClient(httpClient)
    .buildClient();
```
Alternatively, you can provide the entire connection string using the connectionString() function of the PhoneNumberClientBuilder instead of providing the endpoint and access key. 

### Phone plans overview

Phone plans come in two types; Geographic and Toll-Free. Geographic phone plans are phone plans associated with a location, whose phone numbers' area codes are associated with the area code of a geographic location. Toll-Free phone plans are phone plans not associated location. For example, in the US, toll-free numbers can come with area codes such as 800 or 888.

All geographic phone plans within the same country are grouped into a phone plan group with a Geographic phone number type. All Toll-Free phone plans within the same country are grouped into a phone plan group.

### Searching and Acquiring numbers

Phone numbers search can be search through the search creation API by providing a phone plan id, an area code and quantity of phone numbers. The provided quantity of phone numbers will be reserved for ten minutes. This search of phone numbers can either be cancelled or purchased. If the search is cancelled, then the phone numbers will become available to others. If the search is purchased, then the phone numbers are acquired for the Azure resources.

### Configuring / Assigning numbers

Phone numbers can be assigned to a callback URL via the configure number API. As part of the configuration, you will need an acquired phone number, callback URL and application id.

## Examples

### Creating a new user
Use the `createUser` function to create a new user. `user.getId()` gets the
unique ID of the user that was created.

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L80-L81 -->
```java
CommunicationUserIdentifier user = communicationIdentityClient.createUser();
System.out.println("User id: " + user.getId());
```

### Issuing or Refreshing a token for an existing user
Use the `issueToken` function to issue or refresh a token for an existing user. The function
also takes in a list of communication token scopes. Scope options include:
- `chat` (Chat)
- `pstn` (Public switched telephone network)
- `voip` (Voice over IP)

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L93-L96 -->
```java
List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
CommunicationUserToken userToken = communicationIdentityClient.issueToken(user, scopes);
System.out.println("Token: " + userToken.getToken());
System.out.println("Expires On: " + userToken.getExpiresOn());
```

### Revoking all tokens for an existing user
Use the `revokeTokens` function to revoke all the issued tokens of a user.

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L108-L109 -->
```java
// revoke tokens issued for the user prior to now
communicationIdentityClient.revokeTokens(user, OffsetDateTime.now());
```

### Deleting a user
Use the `deleteUser` function to delete a user.

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L118-L119 -->
```java
// delete a previously created user
communicationIdentityClient.deleteUser(user);
```

### Get Countries

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L151-L160 -->
```java
PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

PagedIterable<PhoneNumberCountry> phoneNumberCountries = phoneNumberClient
    .listAllSupportedCountries(locale);

for (PhoneNumberCountry phoneNumberCountry
    : phoneNumberCountries) {
    System.out.println("Phone Number Country Code: " + phoneNumberCountry.getCountryCode());
    System.out.println("Phone Number Country Name: " + phoneNumberCountry.getLocalizedName());
}
```

### Get Phone Plan Groups

Phone plan groups come in two types, Geographic and Toll-Free.

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L193-L202 -->
```java
PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

PagedIterable<PhonePlanGroup> phonePlanGroups = phoneNumberClient
    .listPhonePlanGroups(countryCode, locale, true);

for (PhonePlanGroup phonePlanGroup
    : phonePlanGroups) {
    System.out.println("Phone Plan GroupId: " + phonePlanGroup.getPhonePlanGroupId());
    System.out.println("Phone Plan NumberType: " + phonePlanGroup.getPhoneNumberType());
}
```

### Get Phone Plans

Unlike Toll-Free phone plans, area codes for Geographic Phone Plans are empty. Area codes are found in the Area Codes API.

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L216-L227 -->
```java
PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

PagedIterable<PhonePlan> phonePlans = phoneNumberClient
    .listPhonePlans(countryCode, phonePlanGroupId, locale);

for (PhonePlan phonePlan
    : phonePlans) {
    System.out.println("Phone Plan Id: " + phonePlan.getPhonePlanId());
    System.out.println("Phone Plan Name: " + phonePlan.getLocalizedName());
    System.out.println("Phone Plan Capabilities: " + phonePlan.getCapabilities());
    System.out.println("Phone Plan Area Codes: " + phonePlan.getAreaCodes());
}
```

### Get Location Options

For Geographic phone plans, you can query the available geographic locations. The locations options are structured like the geographic hierarchy of a country. For example, the US has states and within each state are cities.

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L242-L260 -->
```java
PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

LocationOptions locationOptions = phoneNumberClient
    .getPhonePlanLocationOptions(countryCode, phonePlanGroupId, phonePlanId, locale)
    .getLocationOptions();

System.out.println("Getting LocationOptions for: " + locationOptions.getLabelId());
for (LocationOptionsDetails locationOptionsDetails
    : locationOptions.getOptions()) {
    System.out.println(locationOptionsDetails.getValue());
    for (LocationOptions locationOptions1
        : locationOptionsDetails.getLocationOptions()) {
        System.out.println("Getting LocationOptions for: " + locationOptions1.getLabelId());
        for (LocationOptionsDetails locationOptionsDetails1
            : locationOptions1.getOptions()) {
            System.out.println(locationOptionsDetails1.getValue());
        }
    }
}
```

### Get Area Codes

Fetching area codes for geographic phone plans will require the the location options queries set. You must include the chain of geographic locations traversing down the location options object returned by the GetLocationOptions API.

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L284-L292 -->
```java
PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

AreaCodes areaCodes = phoneNumberClient
    .getAllAreaCodes("selection", countryCode, phonePlanId, locationOptions);

for (String areaCode
    : areaCodes.getPrimaryAreaCodes()) {
    System.out.println(areaCode);
}
```

### Configure Phone Number

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L306-L306 -->
```java
phoneNumberClient.configureNumber(phoneNumber, pstnConfiguration);
```

## Long Running Operations

The Phone Number Client supports a variety of long running operations that allow indefinite polling time to the functions listed down below.

### Create Search

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L313-L337 -->
```java
String phonePlanId = "PHONE_PLAN_ID";

List<String> phonePlanIds = new ArrayList<>();
phonePlanIds.add(phonePlanId);

CreateReservationOptions createReservationOptions = new CreateReservationOptions();
createReservationOptions
    .setAreaCode("AREA_CODE_FOR_RESERVATION")
    .setDescription("DESCRIPTION_FOR_RESERVATION")
    .setDisplayName("NAME_FOR_RESERVATION")
    .setPhonePlanIds(phonePlanIds)
    .setQuantity(2);

Duration duration = Duration.ofSeconds(1);
PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

SyncPoller<PhoneNumberReservation, PhoneNumberReservation> res =
    phoneNumberClient.beginCreateReservation(createReservationOptions, duration);
res.waitForCompletion();
PhoneNumberReservation result = res.getFinalResult();

System.out.println("Reservation Id: " + result.getReservationId());
for (String phoneNumber: result.getPhoneNumbers()) {
    System.out.println("Phone Number: " + phoneNumber);
}
```

### Purchase Search
<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L344-L350 -->
```java
Duration duration = Duration.ofSeconds(1);
String phoneNumberReservationId = "RESERVATION_ID_TO_PURCHASE";
PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

SyncPoller<Void, Void> res =
    phoneNumberClient.beginPurchaseReservation(phoneNumberReservationId, duration);
res.waitForCompletion();
```

### Release Phone Numbers
<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L357-L367 -->
```java
Duration duration = Duration.ofSeconds(1);
PhoneNumberIdentifier phoneNumber = new PhoneNumberIdentifier("PHONE_NUMBER_TO_RELEASE");
List<PhoneNumberIdentifier> phoneNumbers = new ArrayList<>();
phoneNumbers.add(phoneNumber);
PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

SyncPoller<PhoneNumberRelease, PhoneNumberRelease> res =
    phoneNumberClient.beginReleasePhoneNumbers(phoneNumbers, duration);
res.waitForCompletion();
PhoneNumberRelease result = res.getFinalResult();
System.out.println("Phone number release status: " + result.getStatus());
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
[package]: https://search.maven.org/artifact/com.azure/azure-communication-administration
[api_documentation]: https://aka.ms/java-docs
[source]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/communication/azure-communication-administration/src



![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcommunication%2Fazure-communication-administration%2FREADME.png)
