// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.verticals.agrifood.farming;

import com.azure.core.http.ProxyOptions;
import com.azure.core.http.llc.RequestOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.PollResult;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.util.Random;

/**
 * Sample for creating a farm with FarmsBaseClient.
 */
public class CreateFarms {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(final String[] args) throws Exception {
        String farmerId = "iscai-farmer";
        Random random = new Random();
        String farmId = "jianghao-farm-" + random.nextInt(999);

        FarmsClient client = new FarmBeatsClientBuilder()
                .endpoint("https://iscai-sdk.farmbeats-dogfood.azure.net")
                .credential(new DefaultAzureCredentialBuilder().authorityHost("https://login.windows-ppe.net").build())
                .httpClient(new NettyAsyncHttpClientBuilder().proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("jianghlu.redmond.corp.microsoft.com", 8888))).build())
                .buildFarmsClient();

        JsonObject farm = Json.createObjectBuilder()
                .add("name", farmId)
                .add("description", "A sample farm.")
                .build();

        System.out.println("Creating farm " + farmId + "...");
        SyncPoller<PollResult, BinaryData> res = client.beginCreateOrUpdateWithResponse(farmerId, farmId,
                new RequestOptions().setBody(BinaryData.fromString(farm.toString())));

        BinaryData result = res.getFinalResult();

        JsonReader jsonReader = Json.createReader(new StringReader(result.toString()));
        JsonObject obj = jsonReader.readObject();
        System.out.println("New farm " + farmId + " successfully created at " + obj.getString("createdDateTime"));
    }
}
