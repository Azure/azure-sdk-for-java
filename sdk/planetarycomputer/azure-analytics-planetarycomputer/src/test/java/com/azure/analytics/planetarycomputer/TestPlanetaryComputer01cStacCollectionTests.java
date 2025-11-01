// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.RenderOption;
import com.azure.analytics.planetarycomputer.models.RenderOptionType;
import com.azure.analytics.planetarycomputer.models.StacCollection;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for STAC Collection operations (Group 01c: Tests 11-12).
 * Ported from TestPlanetaryComputer01cStacCollectionTests.cs
 */
@Tag("STAC")
public class TestPlanetaryComputer01cStacCollectionTests extends PlanetaryComputerTestBase {

    /**
     * Test getting collection thumbnail.
     * Python equivalent: test_11_get_collection_thumbnail
     * Java method: getCollectionThumbnailWithResponse(collectionId, requestOptions)
     * Returns streaming binary data
     */
    @Test
    @Tag("Thumbnail")
    public void test01_11_GetCollectionThumbnail() throws Exception {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getCollectionThumbnail for collection: " + collectionId);

        // First check if collection has thumbnail asset
        StacCollection collection = stacClient.getCollection(collectionId, null, null);

        if (collection.getAssets() == null || !collection.getAssets().containsKey("thumbnail")) {
            System.out.println("Collection does not have a thumbnail asset, skipping test");
            // Skip test if no thumbnail
            return;
        }

        // Act - Get thumbnail as streaming response
        Response<BinaryData> response
            = stacClient.getCollectionThumbnailWithResponse(collectionId, "image/png", new RequestOptions());

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCode(), "Expected successful response");

        // Read the streaming content
        BinaryData contentData = response.getValue();
        assertNotNull(contentData, "Content data should not be null");

        byte[] thumbnailBytes = contentData.toBytes();

        System.out.println("Thumbnail size: " + thumbnailBytes.length + " bytes");
        assertTrue(thumbnailBytes.length > 0, "Thumbnail bytes should not be empty");
        assertTrue(thumbnailBytes.length > 100, "Thumbnail should be substantial");

        // Check for common image format magic bytes
        boolean isPng = thumbnailBytes.length >= 8
            && thumbnailBytes[0] == (byte) 0x89
            && thumbnailBytes[1] == (byte) 0x50
            && thumbnailBytes[2] == (byte) 0x4E
            && thumbnailBytes[3] == (byte) 0x47;

        boolean isJpeg = thumbnailBytes.length >= 3
            && thumbnailBytes[0] == (byte) 0xFF
            && thumbnailBytes[1] == (byte) 0xD8
            && thumbnailBytes[2] == (byte) 0xFF;

        assertTrue(isPng || isJpeg, "Thumbnail should be either PNG or JPEG format");

        if (isPng) {
            System.out.println("Thumbnail format: PNG");
        } else if (isJpeg) {
            System.out.println("Thumbnail format: JPEG");
        }

        System.out.println("Successfully retrieved collection thumbnail");
    }

    /**
     * Test creating a render option for a collection.
     * Python equivalent: test_12_create_render_option
     * Java method: createRenderOption(collectionId, RenderOption)
     */
    @Test
    @Tag("RenderOptions")
    @Tag("Mutation")
    public void test01_12_CreateRenderOption() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing createRenderOption for collection: " + collectionId);

        // Check if render option already exists and delete it
        try {
            stacClient.deleteRenderOption(collectionId, "test-natural-color");
            System.out.println("Deleted existing test render option");
        } catch (Exception e) {
            // Ignore if it doesn't exist
        }

        // Create render option
        RenderOption renderOption = new RenderOption("test-natural-color", "Test Natural color");
        renderOption.setType(RenderOptionType.RASTER_TILE);
        renderOption.setOptions("assets=image&asset_bidx=image|1,2,3");
        renderOption.setMinZoom(6);

        // Act
        RenderOption createdOption = stacClient.createRenderOption(collectionId, renderOption);

        // Assert
        assertNotNull(createdOption, "Created render option should not be null");
        assertEquals("test-natural-color", createdOption.getId(), "ID should match");
        assertEquals("Test Natural color", createdOption.getName(), "Name should match");

        System.out.println("Successfully created render option: " + createdOption.getId());
    }
}
