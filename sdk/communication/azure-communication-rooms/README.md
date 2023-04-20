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
  <version>1.0.0-beta.3</version>
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

### Rooms
- Create room
- Update room
- Get room
- Delete room
- List all rooms

### Participants
- Add or Update Participants
- Remove Participants
- List all participants

## Examples

### Create a new room
Use the `createRoom` function to create a new Room on Azure Communication Service.

```java readme-sample-createRoomWithValidInput
public void createRoomWithValidInput() {
    OffsetDateTime validFrom = OffsetDateTime.now();
    OffsetDateTime validUntil = validFrom.plusDays(30);
    List<RoomParticipant> participants = new ArrayList<>();

    // Add two participants
    participant1 = new RoomParticipant(new CommunicationUserIdentifier("<ACS User MRI identity 1>")).setRole(ParticipantRole.ATTENDEE);
    participant2 = new RoomParticipant(new CommunicationUserIdentifier("<ACS User MRI identity 2>")).setRole(ParticipantRole.CONSUMER);

    participants.add(participant1);
    participants.add(participant2);

    // Create Room options
    CreateRoomOptions roomOptions = new CreateRoomOptions()
            .setValidFrom(validFrom)
            .setValidUntil(validUntil)
            .setParticipants(participants);

    RoomsClient roomsClient = createRoomsClientWithConnectionString();

    CommunicationRoom roomResult = roomsClient.createRoom(roomOptions);
    System.out.println("Room Id: " + roomResult.getRoomId());
}
```

### Update an existing room
Use the `updateRoom` function to update an existing Room on Azure Communication Service.

```java readme-sample-updateRoomWithRoomId
public void updateRoomWithRoomId() {
    OffsetDateTime validFrom = OffsetDateTime.now();
    OffsetDateTime validUntil = validFrom.plusDays(30);

    // Update Room options
    UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions()
            .setValidFrom(validFrom)
            .setValidUntil(validUntil);

    RoomsClient roomsClient = createRoomsClientWithConnectionString();

    try {
        CommunicationRoom roomResult = roomsClient.updateRoom("<Room Id in String>", updateRoomOptions);
        System.out.println("Room Id: " + roomResult.getRoomId());
    } catch (RuntimeException ex) {
        System.out.println(ex);
    }
}
```

### Get an existing room
Use the `getRoom` function to get an existing Room on Azure Communication Service.

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
Use the `deleteRoom` function to delete an existing Room on Azure Communication Service.

```java readme-sample-deleteRoomWithRoomId
public void deleteRoomWithRoomId() {
    RoomsClient roomsClient = createRoomsClientWithConnectionString();

    try {
        roomsClient.deleteRoom("<Room Id in String>");
    } catch (RuntimeException ex) {
        System.out.println(ex);
    }
}
```

### Upsert participants an existing room
Use the `addOrUpdateParticipants` function to add or update participants in an existing Room on Azure Communication Service.

```java readme-sample-addOrUpdateRoomParticipantsWithRoomId
public void addOrUpdateRoomParticipantsWithRoomId() {
    List<RoomParticipant> participantsToaddOrUpdate = new ArrayList<>();

    // New participant to add
    RoomParticipant participantToAdd = new RoomParticipant(new CommunicationUserIdentifier("<ACS User MRI identity 3>")).setRole(ParticipantRole.ATTENDEE);

    // Existing participant to update, assume participant2 is part of the room as a
    // consumer
    participant2 = new RoomParticipant(new CommunicationUserIdentifier("<ACS User MRI identity 2>")).setRole(ParticipantRole.ATTENDEE);

    participantsToaddOrUpdate.add(participantToAdd); // Adding new participant to room
    participantsToaddOrUpdate.add(participant2); // Update participant from Consumer -> Attendee

    RoomsClient roomsClient = createRoomsClientWithConnectionString();

    try {
        AddOrUpdateParticipantsResult addOrUpdateResult = roomsClient.addOrUpdateParticipants("<Room Id>", participantsToaddOrUpdate);
    } catch (RuntimeException ex) {
        System.out.println(ex);
    }
}
```

### Remove participants from an existing room
Use the `removeParticipants` function to remove participants from an existing Room on Azure Communication Service.

```java readme-sample-removeRoomParticipantsWithRoomId
public void removeRoomParticipantsWithRoomId() {
    List<CommunicationIdentifier> participantsToRemove = new ArrayList<>();

    participantsToRemove.add(participant1.getCommunicationIdentifier());
    participantsToRemove.add(participant2.getCommunicationIdentifier());

    RoomsClient roomsClient = createRoomsClientWithConnectionString();

    try {
        RemoveParticipantsResult removeResult = roomsClient.removeParticipants("<Room Id>", participantsToRemove);
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
