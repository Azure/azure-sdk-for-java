// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentsafety;

import com.azure.ai.contentsafety.models.AddOrUpdateTextBlocklistItemsOptions;
import com.azure.ai.contentsafety.models.AddOrUpdateTextBlocklistItemsResult;
import com.azure.ai.contentsafety.models.RemoveTextBlocklistItemsOptions;
import com.azure.ai.contentsafety.models.TextBlocklist;
import com.azure.ai.contentsafety.models.TextBlocklistItem;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class ManageTextBlocklistTests extends ContentSafetyClientTestBase {
    private static String blocklistName = "blocklistTest";
    private static String blocklistItemId = "";

    @Test
    @Order(1)
    public void testCreateOrUpdateTextBlocklistTests() {
        BinaryData resource = BinaryData.fromString("{\"description\":\"Test Blocklist\"}");
        RequestOptions requestOptions = new RequestOptions();
        Response<BinaryData> response
            = blocklistClient.createOrUpdateTextBlocklistWithResponse(blocklistName, resource, requestOptions);
        Assertions.assertEquals(201, response.getStatusCode());
    }

    @Test
    @Order(2)
    public void testGetAllTextBlocklistsTests() {
        // method invocation
        PagedIterable<TextBlocklist> response = blocklistClient.listTextBlocklists();

        // response assertion
        Assertions.assertEquals(200, response.iterableByPage().iterator().next().getStatusCode());
        TextBlocklist firstItem = response.iterator().next();
        Assertions.assertNotNull(firstItem);

        String firstItemBlocklistName = firstItem.getName();
        Assertions.assertEquals(blocklistName, firstItemBlocklistName);
        String firstItemDescription = firstItem.getDescription();
        Assertions.assertEquals("Test Blocklist", firstItemDescription);

    }

    @Test
    @Order(3)
    public void testGetTextBlocklistByBlocklistNameTests() {
        // method invocation
        TextBlocklist response = blocklistClient.getTextBlocklist(blocklistName);

        // response assertion
        Assertions.assertNotNull(response);

        String responseBlocklistName = response.getName();
        Assertions.assertEquals(blocklistName, responseBlocklistName);
        String responseDescription = response.getDescription();
        Assertions.assertEquals("Test Blocklist", responseDescription);
    }

    @Test
    @Order(4)
    public void testAddBlockItemsToTextBlocklistTests() {
        // method invocation
        AddOrUpdateTextBlocklistItemsResult response = blocklistClient.addOrUpdateBlocklistItems(blocklistName,
            new AddOrUpdateTextBlocklistItemsOptions(
                Arrays.asList(new TextBlocklistItem("fuck").setDescription("fuck word"),
                    new TextBlocklistItem("hate").setDescription("hate word"),
                    new TextBlocklistItem("violence").setDescription("violence word"),
                    new TextBlocklistItem("sex").setDescription("sex word"))));

        // response assertion
        Assertions.assertNotNull(response);
        Assertions.assertEquals(4, response.getBlocklistItems().size());

        List<TextBlocklistItem> responseValue = response.getBlocklistItems();
        TextBlocklistItem responseValueFirstItem = responseValue.get(0);

        Assertions.assertNotNull(responseValueFirstItem);
        Assertions.assertEquals("fuck word", responseValueFirstItem.getDescription());
        Assertions.assertEquals("fuck", responseValueFirstItem.getText());
        Assertions.assertNotNull(responseValueFirstItem.getBlocklistItemId());
        blocklistItemId = new String(responseValueFirstItem.getBlocklistItemId());
        System.out.println("debug blocklistItemId: " + blocklistItemId);
    }

    @Test
    @Order(5)
    public void testGetAllBlockItemsByBlocklistNameTests() {
        // method invocation
        PagedIterable<TextBlocklistItem> response = blocklistClient.listTextBlocklistItems(blocklistName, 2, 1);

        // response assertion
        Assertions.assertEquals(2, response.stream().count());
        Optional<TextBlocklistItem> firstItem = response.stream().findFirst();
        Assertions.assertNotNull(firstItem);
        Assertions.assertEquals("violence word", firstItem.get().getDescription());
        Assertions.assertEquals("violence", firstItem.get().getText());
    }

    @Test
    @Order(6)
    public void testGetBlockItemByBlocklistNameAndBlockItemIdTests() {
        // method invocation
        System.out.println("debug blocklistItemId: " + blocklistItemId);
        TextBlocklistItem response = blocklistClient.getTextBlocklistItem(blocklistName, blocklistItemId);

        // response assertion
        Assertions.assertNotNull(response);

        String responseBlockItemId = response.getBlocklistItemId();
        Assertions.assertEquals(blocklistItemId, responseBlockItemId);
        String responseDescription = response.getDescription();
        Assertions.assertEquals("fuck word", responseDescription);
        String responseText = response.getText();
        Assertions.assertEquals("fuck", responseText);
    }

    @Test
    @Order(7)
    public void testRemoveBlockItemsFromTextBlocklistTests() {
        // method invocation
        System.out.println("debug blocklistItemId: " + blocklistItemId);
        blocklistClient.removeBlocklistItems(blocklistName,
            new RemoveTextBlocklistItemsOptions(Arrays.asList(blocklistItemId)));
    }

    @Test
    @Order(8)
    public void testDeleteTextBlocklistByBlocklistNameTests() {
        // method invocation
        blocklistClient.deleteTextBlocklist(blocklistName);
    }
}
