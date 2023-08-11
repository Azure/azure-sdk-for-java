package java.com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.EasmClient;
import com.azure.analytics.defender.easm.EasmClientBuilder;
import com.azure.analytics.defender.easm.models.AssetUpdateData;
import com.azure.analytics.defender.easm.models.Task;
import com.azure.core.util.Configuration;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
import com.azure.resourcemanager.defendereasm.EASMClient;
import com.azure.resourcemanager.defendereasm.EASMClientBuilder;
import com.azure.resourcemanager.defendereasm.models.AssetUpdateRequest;
import com.azure.resourcemanager.defendereasm.models.TaskResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class ManagingExternalIdsSample {
    public static void main(String[] args) throws JsonProcessingException {
        String subscriptionId = Configuration.getGlobalConfiguration().get("SUBSCRIPTIONID");
        String workspaceName = Configuration.getGlobalConfiguration().get("WORKSPACENAME");
        String resourceGroupName = Configuration.getGlobalConfiguration().get("RESOURCEGROUPNAME");
        String endpoint = Configuration.getGlobalConfiguration().get("ENDPOINT");
        String externalIdMapping = Configuration.getGlobalConfiguration().get("MAPPING");

        EasmClient easmClient = new EasmClientBuilder()
            .endpoint(endpoint)
            .subscriptionId(subscriptionId)
            .workspaceName(workspaceName)
            .resourceGroupName(resourceGroupName)
            // For the purposes of this demo, I've chosen the InteractiveBrowserCredential but any credential will work.
            .credential(new InteractiveBrowserCredentialBuilder().build())
            .buildClient();

        // We can update each asset and append the tracking id of the update to our update ID list, so that
        // we can keep track of the progress on each update later
        List<String> updateIds = new ArrayList<>();
        List<String> externalIds = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode mapping = objectMapper.readTree(externalIdMapping);

        mapping.elements().forEachRemaining((mappingInstance) -> {

            externalIds.add(mappingInstance.get("externalId").toString());

            AssetUpdateData assetUpdateRequest = new AssetUpdateData()
                    .setExternalId(mappingInstance.get("externalId").toString());
            String filter = "kind = " +
                    mappingInstance.get("kind") +
                    " AND name = " +
                    mappingInstance.get("name");
            Task taskResponse = easmClient.updateAssets(filter, assetUpdateRequest);
            updateIds.add(taskResponse.getId());
        });

        // We can view the progress of each update using the tasksGet method
        updateIds.forEach(id -> {
            Task taskResponse = easmClient.getTask(id);
            System.out.println(taskResponse.getId() + ": " + taskResponse.getState());
        });

        // The updates can be viewed using the `assetsList` method by creating a filter that matches on each
        // external id using an `in` query
        String assetFilter = "External ID in ".concat(String.join(", ", externalIds));

        easmClient.listAssetResource(assetFilter, "lastSeen", 0, 25, null)
                .forEach(assetResponse -> {
                    System.out.println(assetResponse.getExternalId() + ", " + assetResponse.getName());
                });
    }
}
