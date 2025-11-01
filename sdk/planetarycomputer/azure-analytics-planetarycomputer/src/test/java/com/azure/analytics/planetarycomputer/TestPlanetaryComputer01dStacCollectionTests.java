// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.RenderOption;
import com.azure.analytics.planetarycomputer.models.RenderOptionType;
import com.azure.core.exception.HttpResponseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for STAC Collection operations (Group 01d: Tests 13-14a).
 * Ported from TestPlanetaryComputer01dStacCollectionTests.cs
 */
@Tag("STAC")
@Tag("RenderOptions")
public class TestPlanetaryComputer01dStacCollectionTests extends PlanetaryComputerTestBase {

    /**
     * Test getting a specific render option.
     * Python equivalent: test_13_get_render_option
     * Java method: getRenderOption(collectionId, renderOptionId)
     */
    @Test
    public void test01_13_GetRenderOption() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getRenderOption for collection: " + collectionId);

        // Act
        RenderOption renderOption = stacClient.getRenderOption(collectionId, "test-natural-color");

        // Assert
        assertNotNull(renderOption, "Render option should not be null");
        assertEquals("test-natural-color", renderOption.getId(), "ID should match");
        assertNotNull(renderOption.getName(), "Name should not be null");

        System.out.println("Successfully retrieved render option: " + renderOption.getId());
    }

    /**
     * Test replacing a render option.
     * Python equivalent: test_14_replace_render_option
     * Java method: replaceRenderOption(collectionId, renderOptionId, RenderOption)
     */
    @Test
    @Tag("Mutation")
    public void test01_14_ReplaceRenderOption() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing replaceRenderOption for collection: " + collectionId);

        // Create updated render option
        RenderOption renderOption = new RenderOption("test-natural-color", "Test Natural color updated");
        renderOption.setDescription("RGB from visual assets - updated");
        renderOption.setType(RenderOptionType.RASTER_TILE);
        renderOption.setOptions("assets=image&asset_bidx=image|1,2,3");
        renderOption.setMinZoom(6);

        // Act
        RenderOption updatedOption = stacClient.replaceRenderOption(collectionId, "test-natural-color", renderOption);

        // Assert
        assertNotNull(updatedOption, "Updated render option should not be null");
        assertEquals("test-natural-color", updatedOption.getId(), "ID should match");
        assertEquals("RGB from visual assets - updated", updatedOption.getDescription(),
            "Description should be updated");

        System.out.println("Successfully replaced render option: " + updatedOption.getId());
    }

    /**
     * Test deleting a render option.
     * Python equivalent: test_14a_delete_render_option
     * Java method: deleteRenderOption(collectionId, renderOptionId)
     */
    @Test
    @Tag("Mutation")
    public void test01_14a_DeleteRenderOption() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing deleteRenderOption for collection: " + collectionId);

        // Create a render option to be deleted
        RenderOption renderOption = new RenderOption("test-render-opt-delete", "Test Render Option To Be Deleted");
        renderOption.setType(RenderOptionType.RASTER_TILE);
        renderOption.setOptions("assets=image&asset_bidx=image|1,2,3");
        renderOption.setMinZoom(6);

        System.out.println("Creating render option for deletion");
        stacClient.createRenderOption(collectionId, renderOption);

        // Verify it exists
        RenderOption existingOption = stacClient.getRenderOption(collectionId, "test-render-opt-delete");
        assertNotNull(existingOption, "Render option should exist before deletion");

        // Act - Delete it
        stacClient.deleteRenderOption(collectionId, "test-render-opt-delete");

        System.out.println("Render option deleted successfully");

        // Verify deletion - should throw 404
        HttpResponseException exception = assertThrows(HttpResponseException.class,
            () -> stacClient.getRenderOption(collectionId, "test-render-opt-delete"),
            "Getting deleted render option should have failed");

        assertEquals(404, exception.getResponse().getStatusCode(), "Should return 404 for deleted resource");
        System.out.println("Verified render option was deleted");
    }
}
