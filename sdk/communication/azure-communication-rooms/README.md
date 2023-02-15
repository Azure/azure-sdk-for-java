# Azure Communications Rooms Service client library for Java

Azure Communication Rooms is used to operate on rooms.

[Source code][source] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][product_docs]

## Getting started

### Prerequisites

- An Azure account with an active subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).
- A deployed Communication Services resource. You can use the [Azure Portal](https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource?tabs=windows&pivots=platform-azp) or the [Azure PowerShell](https://docs.microsoft.com/powershell/module/az.communication/new-azcommunicationservice) to set it up.

### Include the package

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-communication-rooms;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-communication-rooms</artifactId>
  <version>1.0.0-beta.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Authenticate the client

### Azure Active Directory Token Authentication
A `DefaultAzureCredential` object must be passed to the `RoomsClientBuilder` via the credential() function. Endpoint and httpClient must also be set via the endpoint() and httpClient() functions respectively.

`AZURE_CLIENT_SECRET`, `AZURE_CLIENT_ID` and `AZURE_TENANT_ID` environment variables
are needed to create a DefaultAzureCredential object.

Alternatively, you can provide the entire connection string using the connectionString() function instead of providing the endpoint and access key.

```java readme-sample-createRoomsClientWithConnectionString
public RoomsClient createRoomsClientWithConnectionString() {
    // You can find your connection string from your resource in the Azure Portal
    String connectionString = "https://<resource-name>.communication.azure.com/;<access-key>";

    RoomsClient roomsClient = new RoomsClientBuilder().connectionString(connectionString).buildClient();

    return roomsClient;
}
```

## Key concepts

There are four operations to interact with the Azure Communication Rooms Service.

## Examples

### Create a new room
Use the `createRoom`  function to create a new Room on Azure Communication Service.

```java readme-sample-createRoomWithValidInput
public void createRoomWithValidInput() {
    OffsetDateTime validFrom = OffsetDateTime.of(2021, 8, 1, 5, 30, 20, 10, ZoneOffset.UTC);
    OffsetDateTime validUntil = OffsetDateTime.of(2021, 9, 1, 5, 30, 20, 10, ZoneOffset.UTC);
    List<RoomParticipant> participants = new ArrayList<>();
    // Add two participants
    participants.add(new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("<ACS User MRI identity 1>")).setRole(RoleType.ATTENDEE));
    participants.add(new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("<ACS User MRI identity 2>")).setRole(RoleType.CONSUMER));

    RoomsClient roomsClient = createRoomsClientWithConnectionString();
    CommunicationRoom roomResult = roomsClient.createRoom(validFrom, validUntil, RoomJoinPolicy.INVITE_ONLY, participants);
    System.out.println("Room Id: " + roomResult.getRoomId());
}
```

### Create a new open room
Use the `createRoom`  function to create a new Open Room on Azure Communication Service.

```java readme-sample-createOpenRoomWithValidInput
public void createOpenRoomWithValidInput() {
    OffsetDateTime validFrom = OffsetDateTime.of(2021, 8, 1, 5, 30, 20, 10, ZoneOffset.UTC);
    OffsetDateTime validUntil = OffsetDateTime.of(2021, 9, 1, 5, 30, 20, 10, ZoneOffset.UTC);

    RoomsClient roomsClient = createRoomsClientWithConnectionString();
    CommunicationRoom roomResult = roomsClient.createRoom(validFrom, validUntil, RoomJoinPolicy.INVITE_ONLY, null);
    System.out.println("Room Id: " + roomResult.getRoomId());
}
```

### Update an existing room
Use the `updateRoom`  function to create a new Room on Azure Communication Service.

```java readme-sample-updateRoomWithRoomId
public void updateRoomWithRoomId() {
    OffsetDateTime validFrom = OffsetDateTime.of(2021, 8, 1, 5, 30, 20, 10, ZoneOffset.UTC);
    OffsetDateTime validUntil = OffsetDateTime.of(2021, 9, 1, 5, 30, 20, 10, ZoneOffset.UTC);
    List<RoomParticipant> participants = new ArrayList<>();
    participants.add(new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("<ACS User MRI identity 1>")).setRole(RoleType.ATTENDEE));
    participants.add(new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("<ACS User MRI identity 2>")).setRole(RoleType.CONSUMER));

    RoomsClient roomsClient = createRoomsClientWithConnectionString();

    try {
        CommunicationRoom roomResult = roomsClient.updateRoom("<Room Id in String>", validFrom, validUntil, null, participants);
        System.out.println("Room Id: " + roomResult.getRoomId());

    } catch (RuntimeException ex) {
        System.out.println(ex);
    }
}
```

### Get an existing room
Use the `getRoom`  function to get an existing Room on Azure Communication Service.

```java readme-sample-getRoomWithRoomId
public void getRoomWithRoomId() {
    RoomsClient roomsClient = createRoomsClientWithConnectionString();
    try {
        CommunicationRoom roomResult = roomsClient.getRoom("<Room Id in String>");
        System.out.println("Room Id: " + roomResult.getRoomId());
    } catch (RuntimeException ex) {
        System.out.println(ex);
    }
}
```

### Delete an existing room
Use the `deleteRoomWithResponse`  function to delete an existing Room on Azure Communication Service.

```java readme-sample-deleteRoomWithRoomId
public void deleteRoomWithRoomId() {
    RoomsClient roomsClient = createRoomsClientWithConnectionString();
    try {
        roomsClient.deleteRoomWithResponse("<Room Id in String>", Context.NONE);
    } catch (RuntimeException ex) {
        System.out.println(ex);
    }
}
```

### Add participants an existing room
Use the `addParticipants`  function to add participants to an existing Room on Azure Communication Service.

```java readme-sample-addRoomParticipantsWithRoomId
public void addRoomParticipantsWithRoomId() {
    RoomParticipant user1 = new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("8:acs:b6372803-0c35-4ec0-833b-c19b798cef2d_0000000e-3240-55cf-9806-113a0d001dd9")).setRole(RoleType.ATTENDEE);
    RoomParticipant user2 = new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("8:acs:b6372803-0c35-4ec0-833b-c19b798cef2d_0000000e-3240-55cf-9806-113a0d001dd7")).setRole(RoleType.PRESENTER);
    RoomParticipant user3 = new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("8:acs:b6372803-0c35-4ec0-833b-c19b798cef2d_0000000e-3240-55cf-9806-113a0d001dd5")).setRole(RoleType.CONSUMER);

    List<RoomParticipant> participants = new ArrayList<RoomParticipant>(Arrays.asList(user1, user2, user3));
    RoomsClient roomsClient = createRoomsClientWithConnectionString();

    try {
        ParticipantsCollection roomParticipants =  roomsClient.addParticipants("<Room Id>", participants);
        System.out.println("No. of Participants in Room: " + roomParticipants.getParticipants().size());

    } catch (RuntimeException ex) {
        System.out.println(ex);
    }
}
```

### Remove participants an existing room
Use the `removeParticipants`  function to remove participants from an existing Room on Azure Communication Service.

```java readme-sample-removeRoomParticipantsWithRoomId
public void removeRoomParticipantsWithRoomId() {
    RoomParticipant user1 = new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("8:acs:b6372803-0c35-4ec0-833b-c19b798cef2d_0000000e-3240-55cf-9806-113a0d001dd9")).setRole(RoleType.ATTENDEE);
    RoomParticipant user2 = new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("8:acs:b6372803-0c35-4ec0-833b-c19b798cef2d_0000000e-3240-55cf-9806-113a0d001dd7")).setRole(RoleType.PRESENTER);

    List<RoomParticipant> participants = new ArrayList<RoomParticipant>(Arrays.asList(user1, user2));
    RoomsClient roomsClient = createRoomsClientWithConnectionString();

    try {
        ParticipantsCollection roomParticipants =  roomsClient.removeParticipants("<Room Id>", participants);
        System.out.println("Room Id: " + roomParticipants.getParticipants().size());

    } catch (RuntimeException ex) {
        System.out.println(ex);
    }
}
```

## Troubleshooting

1. If creating a client fails, verify if you have the right connection string.
2. For room creation failures the communication error should in most case give a brief description of the issue.
3. For participants update failures, make sure the participants are present in the room using the get participants.

## Next steps

- [Read more about Rooms in Azure Communication Services][next_steps]

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/communication/azure-communication-rooms/src
[package]: https://search.maven.org/artifact/com.azure/azure-communication-rooms
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[product_docs]: https://docs.microsoft.com/azure/communication-services/
[api_documentation]: https://aka.ms/java-docs
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fappconfiguration%2Fazure-data-appconfiguration%2FREADME.png)
