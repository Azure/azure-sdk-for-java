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
import com.azure.communication.rooms.models.AddOrUpdateParticipantsResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class ReadmeSamples {

    RoomParticipant participant1;
    RoomParticipant participant2;

    public RoomsClient createRoomsClientUsingAzureKeyCredential() {
        // BEGIN: readme-sample-createRoomsClientUsingAzureKeyCredential
        // Find your endpoint and access key from your resource in the Azure
        String endpoint = "https://<resource-name>.communication.azure.com";
        AzureKeyCredential azureKeyCredential = new AzureKeyCredential("<access-key>");

        RoomsClient roomsClient = new RoomsClientBuilder().endpoint(endpoint).credential(azureKeyCredential)
                .buildClient();
        // END: readme-sample-createRoomsClientUsingAzureKeyCredential
        return roomsClient;
    }


    public RoomsAsyncClient createRoomsAsyncClientUsingAzureKeyCredential() {
        // BEGIN: readme-sample-createRoomsAsyncClientUsingAzureKeyCredential
        // Find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<resource-name>.communication.azure.com";
        AzureKeyCredential azureKeyCredential = new AzureKeyCredential("<access-key>");

        RoomsAsyncClient roomsClient = new RoomsClientBuilder().endpoint(endpoint).credential(azureKeyCredential)
                .buildAsyncClient();
        // END: readme-sample-createRoomsAsyncClientUsingAzureKeyCredential
        return roomsClient;
    }

    public RoomsClient createRoomsClientWithConnectionString() {
        // BEGIN: readme-sample-createRoomsClientWithConnectionString
        // Find your connection string from your resource in the Azure Portal
        String connectionString = "https://<resource-name>.communication.azure.com/;<access-key>";

        RoomsClient roomsClient = new RoomsClientBuilder().connectionString(connectionString).buildClient();
        // END: readme-sample-createRoomsClientWithConnectionString
        return roomsClient;
    }

    public RoomsClientBuilder createRoomsClientBuilder() {
        // BEGIN: readme-sample-createRoomsCLientBuilder
        RoomsClientBuilder builder = new RoomsClientBuilder();
        // END: readme-sample-createRoomsCLientBuilder
        return builder;
    }

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

    public void createRoomWithValidInput() {
        RoomsClient roomsClient = createRoomsClientWithConnectionString();

        // BEGIN: readme-sample-createRoomWithValidInput
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

        CommunicationRoom roomResult = roomsClient.createRoom(roomOptions);
        // END: readme-sample-createRoomWithValidInput
        System.out.println("Room Id: " + roomResult.getRoomId());
    }

    public void updateRoomWithRoomId() {
        RoomsClient roomsClient = createRoomsClientWithConnectionString();
        // BEGIN: readme-sample-updateRoomWithRoomId
        OffsetDateTime validFrom = OffsetDateTime.now();
        OffsetDateTime validUntil = validFrom.plusDays(30);

        // Update Room options
        UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions()
                .setValidFrom(validFrom)
                .setValidUntil(validUntil);

        try {
            CommunicationRoom roomResult = roomsClient.updateRoom("<Room Id>", updateRoomOptions);
            System.out.println("Room Id: " + roomResult.getRoomId());
        } catch (RuntimeException ex) {
            System.out.println(ex);
        }
        // END: readme-sample-updateRoomWithRoomId
    }

    public void getRoomWithRoomId() {
        RoomsClient roomsClient = createRoomsClientWithConnectionString();
        // BEGIN: readme-sample-getRoomWithRoomId
        try {
            CommunicationRoom roomResult = roomsClient.getRoom("<Room Id>");
            System.out.println("Room Id: " + roomResult.getRoomId());
        } catch (RuntimeException ex) {
            System.out.println(ex);
        }
        // END: readme-sample-getRoomWithRoomId
    }

    public void deleteRoomWithRoomId() {
        RoomsClient roomsClient = createRoomsClientWithConnectionString();

        // BEGIN: readme-sample-deleteRoomWithRoomId
        try {
            roomsClient.deleteRoom("<Room Id>");
        } catch (RuntimeException ex) {
            System.out.println(ex);
        }
        // END: readme-sample-deleteRoomWithRoomId
    }


    public void listRooms() {
        RoomsClient roomsClient = createRoomsClientWithConnectionString();

        // BEGIN: readme-sample-listRooms
        try {
            PagedIterable<CommunicationRoom> rooms = roomsClient.listRooms();

            for (CommunicationRoom room : rooms) {
                System.out.println("Room ID: " + room.getRoomId());
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        // END: readme-sample-listRooms
    }


    public void addOrUpdateRoomParticipantsWithRoomId() {

        RoomsClient roomsClient = createRoomsClientWithConnectionString();

        // BEGIN: readme-sample-addOrUpdateRoomParticipantsWithRoomId
        List<RoomParticipant> participantsToaddOrUpdate = new ArrayList<>();

        // New participant to add
        RoomParticipant participantToAdd = new RoomParticipant(new CommunicationUserIdentifier("<ACS User MRI identity 3>")).setRole(ParticipantRole.ATTENDEE);

        // Existing participant to update, assume participant2 is part of the room as a
        // consumer
        participant2 = new RoomParticipant(new CommunicationUserIdentifier("<ACS User MRI identity 2>")).setRole(ParticipantRole.ATTENDEE);

        participantsToaddOrUpdate.add(participantToAdd); // Adding new participant to room
        participantsToaddOrUpdate.add(participant2); // Update participant from Consumer -> Attendee

        try {
            AddOrUpdateParticipantsResult addOrUpdateResult = roomsClient.addOrUpdateParticipants("<Room Id>", participantsToaddOrUpdate);
        } catch (RuntimeException ex) {
            System.out.println(ex);
        }
        // END: readme-sample-addOrUpdateRoomParticipantsWithRoomId
    }

    public void removeRoomParticipantsWithRoomId() {
        RoomsClient roomsClient = createRoomsClientWithConnectionString();

        // BEGIN: readme-sample-removeRoomParticipantsWithRoomId
        List<CommunicationIdentifier> participantsToRemove = new ArrayList<>();

        participantsToRemove.add(participant1.getCommunicationIdentifier());
        participantsToRemove.add(participant2.getCommunicationIdentifier());

        try {
            RemoveParticipantsResult removeResult = roomsClient.removeParticipants("<Room Id>", participantsToRemove);
        } catch (RuntimeException ex) {
            System.out.println(ex);
        }
        // END: readme-sample-removeRoomParticipantsWithRoomId
    }

    public void listRoomParticipantsWithRoomId() {
        RoomsClient roomsClient = createRoomsClientWithConnectionString();
        // BEGIN: readme-sample-listRoomParticipantsWithRoomId
        try {
            PagedIterable<RoomParticipant> allParticipants = roomsClient.listParticipants("<Room Id>");
            for (RoomParticipant participant : allParticipants) {
                System.out.println(participant.getCommunicationIdentifier().getRawId() + " (" + participant.getRole() + ")");
            }
        } catch (RuntimeException ex) {
            System.out.println(ex);
        }
        // END: readme-sample-listRoomParticipantsWithRoomId
    }
}
