package java.com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.AssetsClient;
import com.azure.analytics.defender.easm.EasmDefenderClientBuilder;
import com.azure.analytics.defender.easm.TasksClient;
import com.azure.analytics.defender.easm.models.AssetUpdateData;
import com.azure.analytics.defender.easm.models.Task;
import com.azure.core.util.Configuration;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
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

        EasmDefenderClientBuilder easmDefenderClientBuilder = new EasmDefenderClientBuilder()
            .endpoint(endpoint)
            .subscriptionId(subscriptionId)
            .workspaceName(workspaceName)
            .resourceGroupName(resourceGroupName)
            .credential(new InteractiveBrowserCredentialBuilder().build());

        AssetsClient assetsClient = easmDefenderClientBuilder.buildAssetsClient();
        TasksClient tasksClient = easmDefenderClientBuilder.buildTasksClient();

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
            Task task = assetsClient.update(filter, assetUpdateRequest);
            updateIds.add(task.getId());
        });

        // We can view the progress of each update using the `get` method of the tasks client
        updateIds.forEach(id -> {
            Task task = tasksClient.get(id);
            System.out.println(task.getId() + ": " + task.getState());
        });

        // The updates can be viewed using the `list` method of the assets client by creating a filter that matches on each
        // external id using an `in` query
        String assetFilter = "External ID in ".concat(String.join(", ", externalIds));

        assetsClient.list(assetFilter, "lastSeen", 0, 25, null)
                .getValue()
                .forEach(assetResponse -> {
                    System.out.println(assetResponse.getExternalId() + ", " + assetResponse.getName());
                });
    }
}
