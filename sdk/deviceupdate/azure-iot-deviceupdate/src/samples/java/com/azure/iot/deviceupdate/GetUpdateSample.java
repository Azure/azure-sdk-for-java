// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.iot.deviceupdate;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetUpdateSample {
    public static void main(String[] args) {
        // BEGIN: com.azure.iot.deviceupdate.DeviceUpdateClient.instantiate
        DeviceUpdateClient client = new DeviceUpdateClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_ACCOUNT_ENDPOINT"))
            .instanceId(Configuration.getGlobalConfiguration().get("AZURE_INSTANCE_ID"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
        // END: com.azure.iot.deviceupdate.DeviceUpdateClient.instantiate

        try {
            String updateProvider = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_UPDATE_PROVIDER");
            String updateName = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_UPDATE_NAME");
            String updateVersion = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_UPDATE_VERSION");

            System.out.println("Get update data for provider '" + updateProvider + "', name '" + updateName + "' and version '" + updateVersion + "'...");
            // BEGIN: com.azure.iot.deviceupdate.DeviceUpdateClient.GetUpdate
            System.out.println("Update:");
            Response<BinaryData> updateResponse = client.getUpdateWithResponse(updateProvider, updateName, updateVersion, null);
            ObjectMapper updateMapper = new ObjectMapper();
            JsonNode updateJsonNode = updateMapper.readTree(updateResponse.getValue().toBytes());
            System.out.println("  Provider: " + updateJsonNode.get("updateId").get("provider").asText());
            System.out.println("  Name: " + updateJsonNode.get("updateId").get("name").asText());
            System.out.println("  Version: " + updateJsonNode.get("updateId").get("version").asText());
            System.out.println("Metadata:");
            System.out.println(updateResponse.getValue());
            // END: com.azure.iot.deviceupdate.DeviceUpdateClient.GetUpdate

            System.out.println("\nEnumerate update files...");
            // BEGIN: com.azure.iot.deviceupdate.DeviceUpdateClient.EnumerateUpdateFiles
            PagedIterable<BinaryData> items = client.listFiles(updateProvider, updateName, updateVersion, null);
            List<String> fileIds = new ArrayList<String>();
            ObjectMapper fileIdMapper = new ObjectMapper();
            for (BinaryData i: items) {
                String fileId = fileIdMapper.readTree(i.toBytes()).asText();
                System.out.println(fileId);
                fileIds.add(fileId);
            }
            // END: com.azure.iot.deviceupdate.DeviceUpdateClient.EnumerateUpdateFiles

            System.out.println("\nGet file data...");
            // BEGIN: com.azure.iot.deviceupdate.DeviceUpdateClient.GetFiles
            PagedIterable<BinaryData> files = client.listFiles(updateProvider, updateName, updateVersion, null);
            ObjectMapper fileMapper = new ObjectMapper();
            for (String fileId: fileIds) {
                System.out.println("File:");
                Response<BinaryData> fileResponse  = client.getFileWithResponse(updateProvider, updateName, updateVersion, fileId, null);
                JsonNode fileJsonNode = fileMapper.readTree(fileResponse.getValue().toBytes());
                System.out.println("  FileId: " + fileJsonNode.get("fileId").asText());
                System.out.println("Metadata:");
                System.out.println(fileResponse.getValue());
            }
            // END: com.azure.iot.deviceupdate.DeviceUpdateClient.GetFiles

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == 404) {
                // update does not exist
                System.out.println("update does not exist");
            }
        } catch (IOException e) {
            System.out.println("no response");
        }
    }
}
