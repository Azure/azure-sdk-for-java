// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.mediacomposition;

import java.util.HashMap;
import java.util.Map;

import com.azure.communication.mediacomposition.implementation.AzureCommunicationMediaCompositionServiceImpl;
import com.azure.communication.mediacomposition.implementation.AzureCommunicationMediaCompositionServiceImplBuilder;
import com.azure.communication.mediacomposition.implementation.MediaCompositionsImpl;
import com.azure.communication.mediacomposition.implementation.models.CommunicationUserIdentifierModel;
import com.azure.communication.mediacomposition.implementation.models.CompositionStreamState;
import com.azure.communication.mediacomposition.implementation.models.Layout;
import com.azure.communication.mediacomposition.implementation.models.LayoutType;
import com.azure.communication.mediacomposition.implementation.models.MediaCompositionBody;
import com.azure.communication.mediacomposition.implementation.models.MediaInput;
import com.azure.communication.mediacomposition.implementation.models.MediaOutput;
import com.azure.communication.mediacomposition.implementation.models.MediaType;
import com.azure.communication.mediacomposition.implementation.models.Resolution;
import com.azure.communication.mediacomposition.implementation.models.Source;
import com.azure.communication.mediacomposition.implementation.models.SourceType;
import com.azure.communication.mediacomposition.implementation.models.TeamsMeeting;
import com.azure.core.http.HttpPipelineBuilder;

public class ReadmeSamples
{
    public MediaCompositionsImpl createMediaCompositionClient() {
        AzureCommunicationMediaCompositionServiceImplBuilder builder =
        new AzureCommunicationMediaCompositionServiceImplBuilder()
            .host("REPLACE_WITH_SERVICE_URL")
            .pipeline(new HttpPipelineBuilder().build());

        AzureCommunicationMediaCompositionServiceImpl mediaCompositionsClient = builder.buildClient();
        return mediaCompositionsClient.getMediaCompositions();
    }

    public MediaCompositionBody createMediaCompositionBody(String mediaCompositionId) {
        MediaCompositionBody mediaComposition = new MediaCompositionBody().setId(mediaCompositionId);

        // Set Inputs
        Map<String, MediaInput> mediaInputs = new HashMap<>();
        MediaInput watchParty = new MediaInput();
        watchParty
            .setMediaType(MediaType.TEAMS_MEETING)
            .setTeamsMeeting(new TeamsMeeting().setTeamsJoinUrl("REPLACE_WITH_TEAMS_JOIN_URL"));
        mediaInputs.put("watchParty", watchParty);
        mediaComposition.setMediaInputs(mediaInputs);

        // Set Sources
        Map<String, Source> sources = new HashMap<>();
        Source presenter = new Source()
            .setMediaInputId("watchParty")
            .setSourceType(SourceType.PARTICIPANT)
            .setParticipant(new CommunicationUserIdentifierModel().setId("REPLACE_WITH_PARTICIPANT_ID"));

        sources.put("presenter", presenter);
        mediaComposition.setSources(sources);

        // Set Layout
        Layout warholLayout = new Layout()
            .setType(LayoutType.WARHOL)
            .setResolution(new Resolution().setWidth(1920).setHeight(1080));

        mediaComposition.setLayout(warholLayout);

        // Set Outputs
        Map<String, MediaOutput> mediaOutputs = new HashMap<>();
        MediaOutput teams = new MediaOutput()
            .setMediaType(MediaType.TEAMS_MEETING)
            .setTeamsMeeting(new TeamsMeeting().setTeamsJoinUrl("REPLACE_WITH_TEAMS_JOIN_URL"));
        mediaOutputs.put("teams", teams);
        mediaComposition.setMediaOutputs(mediaOutputs);
        return mediaComposition;
    }

    public MediaCompositionBody createMediaComposition(String mediaCompositionId, MediaCompositionBody mediaComposition) {
        MediaCompositionsImpl client = createMediaCompositionClient();
        MediaCompositionBody responseMediaComposition = client.create(mediaCompositionId, mediaComposition);
        return responseMediaComposition;
    }

    public CompositionStreamState startMediaComposition(String mediaCompositionId){
        MediaCompositionsImpl client = createMediaCompositionClient();
        CompositionStreamState streamState = client.start(mediaCompositionId);
        return streamState;
    }

    public CompositionStreamState stopMediaComposition(String mediaCompositionId) {
        MediaCompositionsImpl client = createMediaCompositionClient();
        CompositionStreamState streamState = client.stop(mediaCompositionId);
        return streamState;
    }
}