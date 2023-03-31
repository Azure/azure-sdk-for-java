// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

// package com.azure.communication.rooms;

// import java.time.OffsetDateTime;
// import java.time.ZoneOffset;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.List;

// import com.azure.communication.common.CommunicationUserIdentifier;
// import com.azure.communication.rooms.models.ParticipantsCollection;
// import com.azure.communication.rooms.models.CommunicationRoom;
// import com.azure.communication.rooms.models.RoleType;
// import com.azure.communication.rooms.models.RoomJoinPolicy;
// import com.azure.communication.rooms.models.RoomParticipant;
// import com.azure.core.credential.AzureKeyCredential;
// import com.azure.core.credential.TokenCredential;
// import com.azure.core.util.Context;
// import com.azure.identity.DefaultAzureCredentialBuilder;
// import com.azure.core.http.rest.Response;

// public class ReadmeSamples {

//     public RoomsClient createRoomsClientUsingAzureKeyCredential() {
//         // You can find your endpoint and access key from your resource in the Azure
//         // Portal
//         String endpoint = "https://<resource-name>.communication.azure.com";
//         AzureKeyCredential azureKeyCredential = new AzureKeyCredential("<access-key>");

//         RoomsClient roomsClient = new RoomsClientBuilder().endpoint(endpoint).credential(azureKeyCredential)
//                 .buildClient();

//         return roomsClient;
//     }

//     public RoomsAsyncClient createRoomsAsyncClientUsingAzureKeyCredential() {
//         // You can find your endpoint and access key from your resource in the Azure
//         // Portal
//         String endpoint = "https://<resource-name>.communication.azure.com";
//         AzureKeyCredential azureKeyCredential = new AzureKeyCredential("<access-key>");

//         RoomsAsyncClient roomsClient = new RoomsClientBuilder().endpoint(endpoint).credential(azureKeyCredential)
//                 .buildAsyncClient();

//         return roomsClient;
//     }

//     // BEGIN: readme-sample-createRoomsClientWithConnectionString
//     public RoomsClient createRoomsClientWithConnectionString() {
//         // You can find your connection string from your resource in the Azure Portal
//         String connectionString = "https://<resource-name>.communication.azure.com/;<access-key>";

//         RoomsClient roomsClient = new RoomsClientBuilder().connectionString(connectionString).buildClient();

//         return roomsClient;
//     }
//     // END: readme-sample-createRoomsClientWithConnectionString

//     public RoomsClient createRoomsClientWithAAD() {
//         // You can find your endpoint and access key from your resource in the Azure
//         // Portal
//         String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

//         RoomsClient roomsClient = new RoomsClientBuilder().endpoint(endpoint)
//                 .credential(new DefaultAzureCredentialBuilder().build()).buildClient();

//         return roomsClient;
//     }

//     public RoomsClient createSyncClientUsingTokenCredential() {
//         TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
//         // You can find your endpoint and access key from your resource in the Azure
//         // Portal
//         String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

//         RoomsClient roomsClient = new RoomsClientBuilder().endpoint(endpoint).credential(tokenCredential).buildClient();
//         return roomsClient;
//     }

//     // BEGIN: readme-sample-createRoomWithValidInput
//     public void createRoomWithValidInput() {
//         OffsetDateTime validFrom = OffsetDateTime.of(2021, 8, 1, 5, 30, 20, 10, ZoneOffset.UTC);
//         OffsetDateTime validUntil = OffsetDateTime.of(2021, 9, 1, 5, 30, 20, 10, ZoneOffset.UTC);
//         List<RoomParticipant> participants = new ArrayList<>();
//         // Add two participants
//         participants.add(new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("<ACS User MRI identity 1>")).setRole(RoleType.ATTENDEE));
//         participants.add(new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("<ACS User MRI identity 2>")).setRole(RoleType.CONSUMER));

//         RoomsClient roomsClient = createRoomsClientWithConnectionString();
//         CommunicationRoom roomResult = roomsClient.createRoom(validFrom, validUntil, RoomJoinPolicy.INVITE_ONLY, participants);
//         System.out.println("Room Id: " + roomResult.getRoomId());
//     }
//     // END: readme-sample-createRoomWithValidInput

//     // BEGIN: readme-sample-createOpenRoomWithValidInput
//     public void createOpenRoomWithValidInput() {
//         OffsetDateTime validFrom = OffsetDateTime.of(2021, 8, 1, 5, 30, 20, 10, ZoneOffset.UTC);
//         OffsetDateTime validUntil = OffsetDateTime.of(2021, 9, 1, 5, 30, 20, 10, ZoneOffset.UTC);

//         RoomsClient roomsClient = createRoomsClientWithConnectionString();
//         CommunicationRoom roomResult = roomsClient.createRoom(validFrom, validUntil, RoomJoinPolicy.INVITE_ONLY, null);
//         System.out.println("Room Id: " + roomResult.getRoomId());
//     }
//     // END: readme-sample-createOpenRoomWithValidInput


//     public void createRoomWithParticipants() {
//         OffsetDateTime validFrom = OffsetDateTime.of(2022, 8, 1, 5, 30, 20, 10, ZoneOffset.UTC);
//         OffsetDateTime validUntil = OffsetDateTime.of(2022, 9, 1, 5, 30, 20, 10, ZoneOffset.UTC);
//         List<RoomParticipant> participants = new ArrayList<>();
//         // Add participants
//         participants.add(new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("<ACS User MRI identity 1>")).setRole(RoleType.ATTENDEE));
//         participants.add(new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("<ACS User MRI identity 2>")).setRole(RoleType.CONSUMER));
//         participants.add(new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("<ACS User MRI identity 3>")).setRole(RoleType.ATTENDEE));

//         RoomsClient roomsClient = createRoomsClientWithConnectionString();
//         CommunicationRoom roomResult = roomsClient.createRoom(validFrom, validUntil, RoomJoinPolicy.INVITE_ONLY, participants);
//         System.out.println("Room Id: " + roomResult.getRoomId());
//     }

//     public void createOpenRoomWithoutParticipants() {
//         OffsetDateTime validFrom = OffsetDateTime.of(2021, 8, 1, 5, 30, 20, 10, ZoneOffset.UTC);
//         OffsetDateTime validUntil = OffsetDateTime.of(2021, 9, 1, 5, 30, 20, 10, ZoneOffset.UTC);

//         RoomsClient roomsClient = createRoomsClientWithConnectionString();
//         CommunicationRoom roomResult = roomsClient.createRoom(validFrom, validUntil, RoomJoinPolicy.COMMUNICATION_SERVICE_USERS, null);
//         System.out.println("Room Id: " + roomResult.getRoomId());
//     }

//     // BEGIN: readme-sample-updateRoomWithRoomId
//     public void updateRoomWithRoomId() {
//         OffsetDateTime validFrom = OffsetDateTime.of(2021, 8, 1, 5, 30, 20, 10, ZoneOffset.UTC);
//         OffsetDateTime validUntil = OffsetDateTime.of(2021, 9, 1, 5, 30, 20, 10, ZoneOffset.UTC);
//         List<RoomParticipant> participants = new ArrayList<>();
//         participants.add(new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("<ACS User MRI identity 1>")).setRole(RoleType.ATTENDEE));
//         participants.add(new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("<ACS User MRI identity 2>")).setRole(RoleType.CONSUMER));

//         RoomsClient roomsClient = createRoomsClientWithConnectionString();

//         try {
//             CommunicationRoom roomResult = roomsClient.updateRoom("<Room Id in String>", validFrom, validUntil, null, participants);
//             System.out.println("Room Id: " + roomResult.getRoomId());

//         } catch (RuntimeException ex) {
//             System.out.println(ex);
//         }
//     }
//     // END: readme-sample-updateRoomWithRoomId

//     // BEGIN: readme-sample-getRoomWithRoomId
//     public void getRoomWithRoomId() {
//         RoomsClient roomsClient = createRoomsClientWithConnectionString();
//         try {
//             CommunicationRoom roomResult = roomsClient.getRoom("<Room Id in String>");
//             System.out.println("Room Id: " + roomResult.getRoomId());
//         } catch (RuntimeException ex) {
//             System.out.println(ex);
//         }
//     }
//     // END: readme-sample-getRoomWithRoomId

//     // BEGIN: readme-sample-deleteRoomWithRoomId
//     public void deleteRoomWithRoomId() {
//         RoomsClient roomsClient = createRoomsClientWithConnectionString();
//         try {
//             roomsClient.deleteRoomWithResponse("<Room Id in String>", Context.NONE);
//         } catch (RuntimeException ex) {
//             System.out.println(ex);
//         }
//     }
//     // END: readme-sample-deleteRoomWithRoomId

//     // BEGIN: readme-sample-addRoomParticipantsWithRoomId
//     public void addRoomParticipantsWithRoomId() {
//         RoomParticipant user1 = new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("8:acs:b6372803-0c35-4ec0-833b-c19b798cef2d_0000000e-3240-55cf-9806-113a0d001dd9")).setRole(RoleType.ATTENDEE);
//         RoomParticipant user2 = new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("8:acs:b6372803-0c35-4ec0-833b-c19b798cef2d_0000000e-3240-55cf-9806-113a0d001dd7")).setRole(RoleType.PRESENTER);
//         RoomParticipant user3 = new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("8:acs:b6372803-0c35-4ec0-833b-c19b798cef2d_0000000e-3240-55cf-9806-113a0d001dd5")).setRole(RoleType.CONSUMER);

//         List<RoomParticipant> participants = new ArrayList<RoomParticipant>(Arrays.asList(user1, user2, user3));
//         RoomsClient roomsClient = createRoomsClientWithConnectionString();

//         try {
//             ParticipantsCollection roomParticipants =  roomsClient.addParticipants("<Room Id>", participants);
//             System.out.println("No. of Participants in Room: " + roomParticipants.getParticipants().size());

//         } catch (RuntimeException ex) {
//             System.out.println(ex);
//         }
//     }
//     // END: readme-sample-addRoomParticipantsWithRoomId

//     // BEGIN: readme-sample-removeRoomParticipantsWithRoomId
//     public void removeRoomParticipantsWithRoomId() {
//         RoomParticipant user1 = new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("8:acs:b6372803-0c35-4ec0-833b-c19b798cef2d_0000000e-3240-55cf-9806-113a0d001dd9")).setRole(RoleType.ATTENDEE);
//         RoomParticipant user2 = new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("8:acs:b6372803-0c35-4ec0-833b-c19b798cef2d_0000000e-3240-55cf-9806-113a0d001dd7")).setRole(RoleType.PRESENTER);

//         List<RoomParticipant> participants = new ArrayList<RoomParticipant>(Arrays.asList(user1, user2));
//         RoomsClient roomsClient = createRoomsClientWithConnectionString();

//         try {
//             ParticipantsCollection roomParticipants =  roomsClient.removeParticipants("<Room Id>", participants);
//             System.out.println("Room Id: " + roomParticipants.getParticipants().size());

//         } catch (RuntimeException ex) {
//             System.out.println(ex);
//         }
//     }
//     // END: readme-sample-removeRoomParticipantsWithRoomId

//     public void updateRoomParticipantsWithRoomId() {
//         RoomParticipant user1 = new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("8:acs:b6372803-0c35-4ec0-833b-c19b798cef2d_0000000e-3240-55cf-9806-113a0d001dd9")).setRole(RoleType.PRESENTER);

//         List<RoomParticipant> participants = new ArrayList<RoomParticipant>(Arrays.asList(user1));
//         RoomsClient roomsClient = createRoomsClientWithConnectionString();

//         try {
//             ParticipantsCollection roomParticipants =  roomsClient.updateParticipants("<Room Id>", participants);
//             System.out.println("Room Id: " + roomParticipants.getParticipants().size());

//         } catch (RuntimeException ex) {
//             System.out.println(ex);
//         }
//     }

//     public void getRoomParticipants() {

//         // Create Room
//         OffsetDateTime validFrom = OffsetDateTime.of(2022, 8, 1, 5, 30, 20, 10, ZoneOffset.UTC);
//         OffsetDateTime validUntil = OffsetDateTime.of(2022, 9, 1, 5, 30, 20, 10, ZoneOffset.UTC);
//         List<RoomParticipant> participants = new ArrayList<>();
//         // Add participants
//         participants.add(new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("<ACS User MRI identity 1>")).setRole(RoleType.ATTENDEE));
//         participants.add(new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("<ACS User MRI identity 2>")).setRole(RoleType.CONSUMER));
//         participants.add(new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("<ACS User MRI identity 3>")).setRole(RoleType.ATTENDEE));

//         RoomsClient roomsClient = createRoomsClientWithConnectionString();
//         CommunicationRoom roomResult = roomsClient.createRoom(validFrom, validUntil, RoomJoinPolicy.INVITE_ONLY, participants);
//         String roomId = roomResult.getRoomId();
//         System.out.println("Room Id: " + roomResult.getRoomId());


//         try {
//             ParticipantsCollection roomParticipants =  roomsClient.getParticipants(roomId);
//             System.out.println("No. of Participants in room: " + roomParticipants.getParticipants().size());
//         } catch (RuntimeException ex) {
//             System.out.println(ex);
//         }
//     }

//     public void createRoomTroubleShooting() {
//         RoomsClient roomsClient = createRoomsClientWithConnectionString();
//         try {
//             OffsetDateTime validFrom = OffsetDateTime.of(2021, 9, 1, 5, 30, 20, 10, ZoneOffset.UTC);
//             OffsetDateTime validUntil = OffsetDateTime.of(2021, 8, 1, 5, 30, 20, 10, ZoneOffset.UTC);
//             List<RoomParticipant> participants = new ArrayList<RoomParticipant>();

//             Response<CommunicationRoom> roomResult = roomsClient.createRoomWithResponse(validFrom, validUntil, RoomJoinPolicy.INVITE_ONLY, participants, null);

//             if (roomResult.getStatusCode() == 201) {
//                 System.out.println("Successfully create the room: " + roomResult.getValue().getRoomId());
//             } else {
//                 System.out.println("Error Happened at create room request: " + roomResult.getStatusCode());
//             }
//         } catch (RuntimeException ex) {
//             System.out.println(ex.getMessage());
//         }
//     }
// }
