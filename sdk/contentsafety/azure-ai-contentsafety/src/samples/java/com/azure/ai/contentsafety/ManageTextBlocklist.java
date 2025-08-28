// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentsafety;

import com.azure.ai.contentsafety.models.AddOrUpdateTextBlocklistItemsOptions;
import com.azure.ai.contentsafety.models.AddOrUpdateTextBlocklistItemsResult;
import com.azure.ai.contentsafety.models.AnalyzeTextOptions;
import com.azure.ai.contentsafety.models.AnalyzeTextResult;
import com.azure.ai.contentsafety.models.RemoveTextBlocklistItemsOptions;
import com.azure.ai.contentsafety.models.TextBlocklist;
import com.azure.ai.contentsafety.models.TextBlocklistItem;
import com.azure.ai.contentsafety.models.TextBlocklistMatch;
import com.azure.core.credential.KeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class ManageTextBlocklist {
    public static void main(String[] args) {
        // BEGIN:com.azure.ai.contentsafety.createblocklistclient
        String endpoint = Configuration.getGlobalConfiguration().get("CONTENT_SAFETY_ENDPOINT");
        String key = Configuration.getGlobalConfiguration().get("CONTENT_SAFETY_KEY");

        BlocklistClient blocklistClient = new BlocklistClientBuilder()
            .credential(new KeyCredential(key))
            .endpoint(endpoint).buildClient();
        // END:com.azure.ai.contentsafety.createblocklistclient

        // BEGIN:com.azure.ai.contentsafety.createtextblocklist
        String blocklistName = "TestBlocklist";
        Map<String, String> description = new HashMap<>();
        description.put("description", "Test Blocklist");
        BinaryData resource = BinaryData.fromObject(description);
        RequestOptions requestOptions = new RequestOptions();
        Response<BinaryData> response =
            blocklistClient.createOrUpdateTextBlocklistWithResponse(blocklistName, resource, requestOptions);
        if (response.getStatusCode() == 201) {
            System.out.println("\nBlocklist " + blocklistName + " created.");
        } else if (response.getStatusCode() == 200) {
            System.out.println("\nBlocklist " + blocklistName + " updated.");
        }
        // END:com.azure.ai.contentsafety.createtextblocklist

        // BEGIN:com.azure.ai.contentsafety.addblockitems
        String blockItemText1 = "k*ll";
        String blockItemText2 = "h*te";
        List<TextBlocklistItem> blockItems = Arrays.asList(new TextBlocklistItem(blockItemText1).setDescription("Kill word"),
            new TextBlocklistItem(blockItemText2).setDescription("Hate word"));
        AddOrUpdateTextBlocklistItemsResult addedBlockItems = blocklistClient.addOrUpdateBlocklistItems(blocklistName,
            new AddOrUpdateTextBlocklistItemsOptions(blockItems));
        if (addedBlockItems != null && addedBlockItems.getBlocklistItems() != null) {
            System.out.println("\nBlockItems added:");
            for (TextBlocklistItem addedBlockItem : addedBlockItems.getBlocklistItems()) {
                System.out.println("BlockItemId: " + addedBlockItem.getBlocklistItemId() + ", Text: " + addedBlockItem.getText() + ", Description: " + addedBlockItem.getDescription());
            }
        }
        // END:com.azure.ai.contentsafety.addblockitems


        // BEGIN:com.azure.ai.contentsafety.analyzetextwithblocklist
        // After you edit your blocklist, it usually takes effect in 5 minutes, please wait some time before analyzing with blocklist after editing.
        ContentSafetyClient contentSafetyClient = new ContentSafetyClientBuilder()
            .credential(new KeyCredential(key))
            .endpoint(endpoint).buildClient();
        AnalyzeTextOptions request = new AnalyzeTextOptions("I h*te you and I want to k*ll you");
        request.setBlocklistNames(Arrays.asList(blocklistName));
        request.setHaltOnBlocklistHit(true);

        AnalyzeTextResult analyzeTextResult;
        try {
            analyzeTextResult = contentSafetyClient.analyzeText(request);
        } catch (HttpResponseException ex) {
            System.out.println("Analyze text failed.\nStatus code: " + ex.getResponse().getStatusCode() + ", Error message: " + ex.getMessage());
            throw ex;
        }

        if (analyzeTextResult.getBlocklistsMatch() != null) {
            System.out.println("\nBlocklist match result:");
            for (TextBlocklistMatch matchResult : analyzeTextResult.getBlocklistsMatch()) {
                System.out.println("BlocklistName: " + matchResult.getBlocklistName() + ", BlockItemId: " + matchResult.getBlocklistItemId() + ", BlockItemText: " + matchResult.getBlocklistItemText());
            }
        }
        // END:com.azure.ai.contentsafety.analyzetextwithblocklist

        // BEGIN:com.azure.ai.contentsafety.listtextblocklists
        PagedIterable<TextBlocklist> allTextBlocklists = blocklistClient.listTextBlocklists();
        System.out.println("\nList Blocklist:");
        for (TextBlocklist blocklist : allTextBlocklists) {
            System.out.println("Blocklist: " + blocklist.getName() + ", Description: " + blocklist.getDescription());
        }
        // END:com.azure.ai.contentsafety.listtextblocklists

        // BEGIN:com.azure.ai.contentsafety.gettextblocklist
        TextBlocklist getBlocklist = blocklistClient.getTextBlocklist(blocklistName);
        if (getBlocklist != null) {
            System.out.println("\nGet blocklist:");
            System.out.println("BlocklistName: " + getBlocklist.getName() + ", Description: " + getBlocklist.getDescription());
        }
        // END:com.azure.ai.contentsafety.gettextblocklist

        // BEGIN:com.azure.ai.contentsafety.listtextblocklistitems
        PagedIterable<TextBlocklistItem> allBlockitems = blocklistClient.listTextBlocklistItems(blocklistName);
        System.out.println("\nList BlockItems:");
        for (TextBlocklistItem blocklistItem : allBlockitems) {
            System.out.println("BlockItemId: " + blocklistItem.getBlocklistItemId() + ", Text: " + blocklistItem.getText() + ", Description: " + blocklistItem.getDescription());
        }
        // END:com.azure.ai.contentsafety.listtextblocklistitems

        // BEGIN:com.azure.ai.contentsafety.gettextblocklistitem
        String getBlockItemId = addedBlockItems.getBlocklistItems().get(0).getBlocklistItemId();
        TextBlocklistItem getBlockItem = blocklistClient.getTextBlocklistItem(blocklistName, getBlockItemId);
        System.out.println("\nGet BlockItem:");
        System.out.println("BlockItemId: " + getBlockItem.getBlocklistItemId() + ", Text: " + getBlockItem.getText() + ", Description: " + getBlockItem.getDescription());
        // END:com.azure.ai.contentsafety.gettextblocklistitem

        // BEGIN:com.azure.ai.contentsafety.removeblockitems
        String removeBlockItemId = addedBlockItems.getBlocklistItems().get(0).getBlocklistItemId();
        List<String> removeBlockItemIds = new ArrayList<>();
        removeBlockItemIds.add(removeBlockItemId);
        blocklistClient.removeBlocklistItems(blocklistName, new RemoveTextBlocklistItemsOptions(removeBlockItemIds));
        // END:com.azure.ai.contentsafety.removeblockitems

        // BEGIN:com.azure.ai.contentsafety.deletetextblocklist
        blocklistClient.deleteTextBlocklist(blocklistName);
        // END:com.azure.ai.contentsafety.deletetextblocklist

    }
}
