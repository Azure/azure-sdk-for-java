package com.azure.maps.service;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.maps.service.models.IncludeText;
import com.azure.maps.service.models.MapImageStyle;
import com.azure.maps.service.models.MapImageryStyle;
import com.azure.maps.service.models.MapTileLayer;
import com.azure.maps.service.models.MapTileSize;
import com.azure.maps.service.models.MapTileStyle;
import com.azure.maps.service.models.RasterTileFormat;
import com.azure.maps.service.models.StaticMapLayer;
import com.azure.maps.service.models.TextFormat;
import com.azure.maps.service.models.TileFormat;
import com.azure.maps.service.models.TileSize;
import com.azure.maps.service.models.TilesetID;

public class RenderSamples {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage RenderSamples.java <statesetId>");
            return;
        }
        String statesetId = args[0];
        HttpPipelinePolicy policy = new AzureKeyInQueryPolicy("subscription-key",
                new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY")));
        MapsClient client = new MapsClientBuilder().addPolicy(policy).buildClient();

        System.out.println("Get copyright for caption");
        MapsCommon.print(client.getRenders().getCopyrightCaption(TextFormat.JSON));

        System.out.println("Get copyright for tile");
        MapsCommon.print(client.getRenders().getCopyrightForTile(TextFormat.JSON, 6, 9, 22));

        System.out.println("Get copyright for world");
        MapsCommon.print(client.getRenders().getCopyrightForWorld(TextFormat.JSON));

        System.out.println("Get copyright from bounding box");
        MapsCommon.print(client.getRenders().getCopyrightFromBoundingBox(TextFormat.JSON, "52.41064,4.84228",
                "52.41072,4.84239", IncludeText.YES));

        System.out.println("Get map imagery tile");
        openImageFile(
                client.getRenders().getMapImageryTile(RasterTileFormat.PNG, MapImageryStyle.SATELLITE, 6, 10, 22));

        System.out.println("Get map satelite tile");
        openImageFile(client.getRenders().getMapStateTilePreview(6, 10, 22, statesetId));

        System.out.println("Get map static image");
        openImageFile(
                client.getRenders().getMapStaticImage(RasterTileFormat.PNG, StaticMapLayer.BASIC, MapImageStyle.DARK, 2,
                        null, "1.355233,42.982261,24.980233,56.526017", null, null, null, null, null, null));

        System.out.println("Get map tile");
        openImageFile(client.getRenders().getMapTile(TileFormat.PNG, MapTileLayer.BASIC, MapTileStyle.MAIN, 6, 10, 22,
                MapTileSize.FIVE_HUNDRED_TWELVE, null, null));

        System.out.println("Get map tile for V2");
        openImageFile(client.getRenderV2s().getMapTilePreview(TilesetID.MICROSOFT_BASE, 6, 10, 22, null,
                TileSize.FIVE_HUNDRED_TWELVE, null, null));
    }

    public static void openImageFile(InputStream stream) throws IOException {
        File file = File.createTempFile("image", ".png");
        Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Desktop.getDesktop().open(file);
    }
}
