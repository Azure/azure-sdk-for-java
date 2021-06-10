package com.azure.maps.service;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.azure.maps.service.models.GetCopyrightCaptionResult;
import com.azure.maps.service.models.GetCopyrightForTileResult;
import com.azure.maps.service.models.GetCopyrightForWorldResult;
import com.azure.maps.service.models.GetCopyrightFromBoundingBoxResult;
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
import com.fasterxml.jackson.core.JsonProcessingException;

public class RenderSamples {

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("Usage RenderSamples.java <statesetId>");
			return;
		}
		String statesetId = args[0];
		Renders render = MapsCommon.createMapsClient().getRenders();
		RenderV2s renderV2 = MapsCommon.createMapsClient().getRenderV2s();

		getCopyrightCaption(render);
		getCopyrightForTile(render);
		getCopyrightForWorld(render);
		getCopyrightFromBoundingBox(render);
		getMapImageryTile(render);
		getMapStateTilePreview(render, statesetId);
		getMapStaticImage(render);
		getMapTile(render);

		getMapTilePreviewV2(renderV2);
	}
	
	public static void getCopyrightCaption(Renders render) throws JsonProcessingException {
		GetCopyrightCaptionResult result = render.getCopyrightCaption(TextFormat.JSON);
		MapsCommon.print(result);
	}
	
	public static void getCopyrightForTile(Renders render) throws JsonProcessingException {
		GetCopyrightForTileResult result = render.getCopyrightForTile(TextFormat.JSON, 6, 9, 22);
		MapsCommon.print(result);
	}
	
	public static void getCopyrightForWorld(Renders render) throws JsonProcessingException {
		GetCopyrightForWorldResult result = render.getCopyrightForWorld(TextFormat.JSON);
		MapsCommon.print(result);
	}
	
	public static void getCopyrightFromBoundingBox(Renders render) throws JsonProcessingException {
		GetCopyrightFromBoundingBoxResult result = render.getCopyrightFromBoundingBox(TextFormat.JSON, "52.41064,4.84228", "52.41072,4.84239", IncludeText.YES);
		MapsCommon.print(result);
	}

	public static void getMapImageryTile(Renders render) throws IOException {
		InputStream result = render.getMapImageryTile(RasterTileFormat.PNG, MapImageryStyle.SATELLITE, 6, 10, 22);
		File file = File.createTempFile("imagery", ".png");
		Files.copy(result, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    Desktop.getDesktop().open(file);
	}
	
	public static void getMapStateTilePreview(Renders render, String statesetId) throws IOException {
		InputStream result = render.getMapStateTilePreview(6, 10, 22, statesetId);
		File file = File.createTempFile("stateTile", ".png");
		Files.copy(result, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    Desktop.getDesktop().open(file);
	}
	
	public static void getMapStaticImage(Renders render) throws IOException {
		InputStream result = render.getMapStaticImage(RasterTileFormat.PNG, StaticMapLayer.BASIC, MapImageStyle.DARK, 2, null, "1.355233,42.982261,24.980233,56.526017", null, null, null, null, null, null);
		File file = File.createTempFile("static_image", ".png");
		Files.copy(result, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    Desktop.getDesktop().open(file);
	}
	
	
	public static void getMapTile(Renders render) throws IOException {
		InputStream result = render.getMapTile(TileFormat.PNG, MapTileLayer.BASIC, MapTileStyle.MAIN, 6, 10, 22, MapTileSize.FIVE_HUNDRED_TWELVE, null, null);
		File file = File.createTempFile("tile", ".png");
		Files.copy(result, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    Desktop.getDesktop().open(file);
	}
	
	
	public static void getMapTilePreviewV2(RenderV2s renderV2) throws IOException {
		InputStream result = renderV2.getMapTilePreview(TilesetID.MICROSOFT_BASE, 6, 10, 22, null, TileSize.FIVE_HUNDRED_TWELVE, null, null);
		File file = File.createTempFile("tile_v2", ".vec");
		Files.copy(result, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    Desktop.getDesktop().open(file);
	}

}
