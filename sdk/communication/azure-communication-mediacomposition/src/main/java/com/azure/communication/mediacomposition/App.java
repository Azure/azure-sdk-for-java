package com.azure.communication.mediacomposition;

import java.util.HashMap;
import java.util.Map;

import com.azure.communication.mediacomposition.implementation.AzureCommunicationMediaCompositionServiceImpl;
import com.azure.communication.mediacomposition.implementation.AzureCommunicationMediaCompositionServiceImplBuilder;
import com.azure.communication.mediacomposition.implementation.MediaCompositionsImpl;
import com.azure.communication.mediacomposition.implementation.models.CommunicationUserIdentifierModel;
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

public class App
{
    public static void main( String[] args )
    {
        System.out.println("Creating media composition.");
        String mediaCompositionId = "warholMediaComposition";
        AzureCommunicationMediaCompositionServiceImplBuilder builder =
            new AzureCommunicationMediaCompositionServiceImplBuilder()
                .host("http://localhost:57105")
                .pipeline(new HttpPipelineBuilder().build());

        AzureCommunicationMediaCompositionServiceImpl mediaCompositionsClient = builder.buildClient();
        MediaCompositionsImpl client = mediaCompositionsClient.getMediaCompositions();
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
            .setParticipant(new CommunicationUserIdentifierModel().setId("f3ba9014-6dca-4456-8ec0-fa03cfa2b7b7"));

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
            .setTeamsMeeting(new TeamsMeeting().setTeamsJoinUrl("https://teams.microsoft.com/l/meetup-join/19%3ameeting_OTYyYjVhNGItOWU5MS00MjFlLTgwMjQtOTM3NjRlMmIwZjA2%40thread.v2/0?context=%7b%22Tid%22%3a%2221fa45a2-67de-4731-b654-ccf5c9c0d577%22%2c%22Oid%22%3a%22f3ba9014-6dca-4456-8ec0-fa03cfa2b7b7%22%7d"));
        mediaOutputs.put("teams", teams);
        mediaComposition.setMediaOutputs(mediaOutputs);

        client.create("warholMediaComposition", mediaComposition);
        client.start("warholMediaComposition");
        System.out.println("Started "+ mediaCompositionId);
    }
}