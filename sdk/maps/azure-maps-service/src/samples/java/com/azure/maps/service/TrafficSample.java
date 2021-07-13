package com.azure.maps.service;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.maps.service.models.TextFormat;
import com.azure.maps.service.models.TileFormat;
import com.azure.maps.service.models.TrafficFlowSegmentStyle;
import com.azure.maps.service.models.TrafficFlowTileStyle;
import com.azure.maps.service.models.TrafficIncidentDetailResult;
import com.azure.maps.service.models.TrafficIncidentDetailStyle;
import com.azure.maps.service.models.TrafficIncidentTileStyle;

public class TrafficSample {
    public static void main(String[] args) throws IOException {
        HttpPipelinePolicy policy = new AzureKeyInQueryPolicy("subscription-key",
                new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY")));
        MapsClient client = new MapsClientBuilder().addPolicy(policy).buildClient();

        System.out.println("Get Traffic Flow Segment");
        MapsCommon.print(client.getTraffics().getTrafficFlowSegment(TextFormat.JSON, TrafficFlowSegmentStyle.ABSOLUTE,
                10, "52.41072,4.84239"));

        InputStream stream = client.getTraffics().getTrafficFlowTile(TileFormat.PNG, TrafficFlowTileStyle.ABSOLUTE, 12,
                2044, 1360);
        System.out.println("Get Traffic Flow Tile");
        openImageFile(stream);

        TrafficIncidentDetailResult trafficIncidentDetailResult = client.getTraffics().getTrafficIncidentDetail(
                TextFormat.JSON, TrafficIncidentDetailStyle.S3,
                "6841263.950712,511972.674418,6886056.049288,582676.925582", 11, "1335294634919");
        System.out.println("Get Traffic Incident Detail");
        MapsCommon.print(trafficIncidentDetailResult);

        stream = client.getTraffics().getTrafficIncidentTile(TileFormat.PNG, TrafficIncidentTileStyle.NIGHT, 10, 175,
                408);
        System.out.println("Get Traffic Incident Tile");
        openImageFile(stream);

        System.out.println("Get Traffic Incident Viewport");
        MapsCommon.print(client.getTraffics().getTrafficIncidentViewport(TextFormat.JSON,
                "-939584.4813015489,-23954526.723651607,14675583.153020501,25043442.895825107", 2,
                "-939584.4813018347,-23954526.723651607,14675583.153020501,25043442.8958229083", 2));
    }

    public static void openImageFile(InputStream stream) throws IOException {
        File file = File.createTempFile("image", ".png");
        Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Desktop.getDesktop().open(file);
    }
}
