// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.azure.communication.rooms.models.CommunicationRoom;
import com.azure.communication.rooms.models.RoomParticipant;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class ReadmeSamples {

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

    public RoomsClient createRoomsClientWithConnectionString() {
        // You can find your connection string from your resource in the Azure Portal
        String connectionString = "https://<resource-name>.communication.azure.com/;<access-key>";

        RoomsClient roomsClient = new RoomsClientBuilder().connectionString(connectionString).buildClient();

        return roomsClient;
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
        OffsetDateTime validFrom = OffsetDateTime.of(2021, 8, 1, 5, 30, 20, 10, ZoneOffset.UTC);
        OffsetDateTime validUntil = OffsetDateTime.of(2021, 9, 1, 5, 30, 20, 10, ZoneOffset.UTC);
        List<RoomParticipant> participants = new ArrayList<>();
        // Add two participants
        participants.add(new RoomParticipant("<ACS User MRI identity 1>", "Prebuilt Role Name"));
        participants.add(new RoomParticipant("<ACS User MRI identity 2>", "Prebuilt Role Name"));

        RoomsClient roomsClient = createRoomsClientWithConnectionString();
        CommunicationRoom roomResult = roomsClient.createRoom(validFrom, validUntil, participants);
        System.out.println("Room Id: " + roomResult.getRoomId());
    }

    public void updateRoomWithRoomId() {
        OffsetDateTime validFrom = OffsetDateTime.of(2021, 8, 1, 5, 30, 20, 10, ZoneOffset.UTC);
        OffsetDateTime validUntil = OffsetDateTime.of(2021, 9, 1, 5, 30, 20, 10, ZoneOffset.UTC);
        List<RoomParticipant> participants = new ArrayList<>();
        participants.add(new RoomParticipant("<ACS User MRI identity 1>", "Prebuilt Role Name"));
        // Delete one participant
        participants.add(new RoomParticipant("<ACS User MRI identity 2>", "Prebuilt Role Name"));

        RoomsClient roomsClient = createRoomsClientWithConnectionString();

        try {
            CommunicationRoom roomResult = roomsClient.updateRoom("<Room Id in String>", validFrom, validUntil);
            System.out.println("Room Id: " + roomResult.getRoomId());

        } catch (RuntimeException ex) {
            System.out.println(ex);
        }
    }

    public void getRoomWithRoomId() {
        RoomsClient roomsClient = createRoomsClientWithConnectionString();
        try {
            CommunicationRoom roomResult = roomsClient.getRoom("<Room Id in String>");
            System.out.println("Room Id: " + roomResult.getRoomId());
        } catch (RuntimeException ex) {
            System.out.println(ex);
        }

    }

    public void deleteRoomWithRoomId() {
        RoomsClient roomsClient = createRoomsClientWithConnectionString();
        try {
            roomsClient.deleteRoomWithResponse("<Room Id in String>", Context.NONE);
        } catch (RuntimeException ex) {
            System.out.println(ex);
        }
    }

    public void addRoomParticipantsWithRoomId() {
        RoomParticipant user1 = new RoomParticipant("8:acs:b6372803-0c35-4ec0-833b-c19b798cef1d_0000000e-3240-55cf-9806-113a0d001dd9", "Presenter");
        RoomParticipant user2 = new RoomParticipant("8:acs:b6372803-0c35-4ec0-833b-c19b798cef2d_0000000e-3240-55cf-9806-113a0d001dd9", "Attendee");
        RoomParticipant user3 = new RoomParticipant("8:acs:b6372803-0c35-4ec0-833b-c19b798cef3d_0000000e-3240-55cf-9806-113a0d001dd9", "Organizer");

        List<RoomParticipant> participants = new ArrayList<RoomParticipant>(Arrays.asList(user1, user2, user3));
        RoomsClient roomsClient = createRoomsClientWithConnectionString();

        try {
            CommunicationRoom addedParticipantRoom =  roomsClient.addParticipants("<Room Id>", participants);
            System.out.println("Room Id: " + addedParticipantRoom.getRoomId());

        } catch (RuntimeException ex) {
            System.out.println(ex);
        }
    }

    public void removeRoomParticipantsWithRoomId() {
        RoomParticipant user1 = new RoomParticipant("8:acs:b6372803-0c35-4ec0-833b-c19b798cef1d_0000000e-3240-55cf-9806-113a0d001dd9", "Presenter");
        RoomParticipant user2 = new RoomParticipant("8:acs:b6372803-0c35-4ec0-833b-c19b798cef2d_0000000e-3240-55cf-9806-113a0d001dd9", "Attendee");

        List<RoomParticipant> participants = new ArrayList<RoomParticipant>(Arrays.asList(user1, user2));
        RoomsClient roomsClient = createRoomsClientWithConnectionString();

        try {
            CommunicationRoom removedParticipantRoom =  roomsClient.removeParticipants("<Room Id>", participants);
            System.out.println("Room Id: " + removedParticipantRoom.getRoomId());

        } catch (RuntimeException ex) {
            System.out.println(ex);
        }
    }

    public void deleteAllParticipantsWithEmptyPayload() {
        RoomsClient roomsClient = createRoomsClientWithConnectionString();
        try {
            CommunicationRoom deleteAllParticipantsRoom =  roomsClient.removeAllParticipants("<Room Id>");
            System.out.println("Room Id: " + deleteAllParticipantsRoom.getRoomId());
        } catch (RuntimeException ex) {
            System.out.println(ex);
        }
    }

    public void createRoomTroubleShooting() {
        RoomsClient roomsClient = createRoomsClientWithConnectionString();
        try {
            OffsetDateTime validFrom = OffsetDateTime.of(2021, 9, 1, 5, 30, 20, 10, ZoneOffset.UTC);
            OffsetDateTime validUntil = OffsetDateTime.of(2021, 8, 1, 5, 30, 20, 10, ZoneOffset.UTC);
            List<RoomParticipant> participants = new ArrayList<RoomParticipant>();

            Response<CommunicationRoom> roomResult = roomsClient.createRoomWithResponse(validFrom, validUntil, participants, null);

            if (roomResult.getStatusCode() == 201) {
                System.out.println("Successfully create the room: " + roomResult.getValue().getRoomId());
            } else {
                System.out.println("Error Happened at create room request: " + roomResult.getStatusCode());
            }
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
