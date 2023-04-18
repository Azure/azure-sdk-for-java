// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.rooms.models.CommunicationRoom;
import com.azure.communication.rooms.models.CreateRoomOptions;
import com.azure.communication.rooms.models.RoomParticipant;
import com.azure.communication.rooms.models.ParticipantRole;
import com.azure.communication.rooms.models.RemoveParticipantsResult;
import com.azure.communication.rooms.models.UpdateRoomOptions;
import com.azure.communication.rooms.models.UpsertParticipantsResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class ReadmeSamples {

    RoomParticipant participant1;
    RoomParticipant participant2;

    public RoomsClient createRoomsClientUsingAzureKeyCredential() {
        // You can find your endpoint and access key from your resource in the Azure
        // Portal
        String endpoint = "https://<resource-name>.communication.azure.com";
        AzureKeyCredential azureKeyCredential = new AzureKeyCredential("<access-key>");

        RoomsClient roomsClient = new RoomsClientBuilder().endpoint(endpoint).credential(azureKeyCredential)
                .buildClient();

        return roomsClient;
    }

    public RoomsAsyncClient createRoomsAsyncClientUsingAzureKeyCredential() {
        // You can find your endpoint and access key from your resource in the Azure
        // Portal
        String endpoint = "https://<resource-name>.communication.azure.com";
        AzureKeyCredential azureKeyCredential = new AzureKeyCredential("<access-key>");

        RoomsAsyncClient roomsClient = new RoomsClientBuilder().endpoint(endpoint).credential(azureKeyCredential)
                .buildAsyncClient();

        return roomsClient;
    }

    // BEGIN: readme-sample-createRoomsClientWithConnectionString
    public RoomsClient createRoomsClientWithConnectionString() {
        // You can find your connection string from your resource in the Azure Portal
        String connectionString = "https://<resource-name>.communication.azure.com/;<access-key>";

        RoomsClient roomsClient = new RoomsClientBuilder().connectionString(connectionString).buildClient();

        return roomsClient;
    }
    // END: readme-sample-createRoomsClientWithConnectionString

    public RoomsClient createRoomsClientWithAAD() {
        // You can find your endpoint and access key from your resource in the Azure
        // Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

        RoomsClient roomsClient = new RoomsClientBuilder().endpoint(endpoint)
                .credential(new DefaultAzureCredentialBuilder().build()).buildClient();

        return roomsClient;
    }

    public RoomsClient createSyncClientUsingTokenCredential() {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        // You can find your endpoint and access key from your resource in the Azure
        // Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

        RoomsClient roomsClient = new RoomsClientBuilder().endpoint(endpoint).credential(tokenCredential).buildClient();
        return roomsClient;
    }

    // BEGIN: readme-sample-createRoomWithValidInput
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
    // END: readme-sample-createRoomWithValidInput

    // BEGIN: readme-sample-updateRoomWithRoomId
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
    // END: readme-sample-updateRoomWithRoomId

    // BEGIN: readme-sample-getRoomWithRoomId
    public void getRoomWithRoomId() {
        RoomsClient roomsClient = createRoomsClientWithConnectionString();

        try {
            CommunicationRoom roomResult = roomsClient.getRoom("<Room Id in String>");
            System.out.println("Room Id: " + roomResult.getRoomId());
        } catch (RuntimeException ex) {
            System.out.println(ex);
        }
    }
    // END: readme-sample-getRoomWithRoomId

    // BEGIN: readme-sample-deleteRoomWithRoomId
    public void deleteRoomWithRoomId() {
        RoomsClient roomsClient = createRoomsClientWithConnectionString();

        try {
            roomsClient.deleteRoom("<Room Id in String>");
        } catch (RuntimeException ex) {
            System.out.println(ex);
        }
    }
    // END: readme-sample-deleteRoomWithRoomId

    // BEGIN: readme-sample-upsertRoomParticipantsWithRoomId
    public void upsertRoomParticipantsWithRoomId() {
        List<RoomParticipant> participantsToUpsert = new ArrayList<>();

        // New participant to add
        RoomParticipant participantToAdd = new RoomParticipant(new CommunicationUserIdentifier("<ACS User MRI identity 3>")).setRole(ParticipantRole.ATTENDEE);

        // Existing participant to update, assume participant2 is part of the room as a
        // consumer
        participant2 = new RoomParticipant(new CommunicationUserIdentifier("<ACS User MRI identity 2>")).setRole(ParticipantRole.ATTENDEE);

        participantsToUpsert.add(participantToAdd); // Adding new participant to room
        participantsToUpsert.add(participant2); // Update participant from Consumer -> Attendee

        RoomsClient roomsClient = createRoomsClientWithConnectionString();

        try {
            UpsertParticipantsResult upsertResult = roomsClient.upsertParticipants("<Room Id>", participantsToUpsert);
        } catch (RuntimeException ex) {
            System.out.println(ex);
        }
    }
    // END: readme-sample-upsertRoomParticipantsWithRoomId

    // BEGIN: readme-sample-removeRoomParticipantsWithRoomId
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
    // END: readme-sample-removeRoomParticipantsWithRoomId
}
