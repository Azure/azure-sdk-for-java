package java.com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.EasmClient;
import com.azure.analytics.defender.easm.EasmClientBuilder;
import com.azure.analytics.defender.easm.models.AssetUpdateData;
import com.azure.analytics.defender.easm.models.AssetUpdateState;
import com.azure.analytics.defender.easm.models.SavedFilter;
import com.azure.analytics.defender.easm.models.SavedFilterData;
import com.azure.core.util.Configuration;
import com.azure.identity.InteractiveBrowserCredentialBuilder;

public class SavedFilterSample {

    /*
        A sample asset list call that could be used to monitor the assets:
     */
    private static void monitor(SavedFilter response){
        // your monitor logic here
    }


    public static void main(String[] args) {
        String subscriptionId = Configuration.getGlobalConfiguration().get("SUBSCRIPTIONID");
        String workspaceName = Configuration.getGlobalConfiguration().get("WORKSPACENAME");
        String resourceGroupName = Configuration.getGlobalConfiguration().get("RESOURCEGROUPNAME");
        String endpoint = Configuration.getGlobalConfiguration().get("ENDPOINT");

        EasmClient easmClient = new EasmClientBuilder()
            .endpoint(endpoint)
            .subscriptionId(subscriptionId)
            .workspaceName(workspaceName)
            .resourceGroupName(resourceGroupName)
            // For the purposes of this demo, I've chosen the InteractiveBrowserCredential but any credential will work.
            .credential(new InteractiveBrowserCredentialBuilder().build())
            .buildClient();

        String savedFilterName = "Sample saved filter";
        SavedFilterData savedFilterRequest = new SavedFilterData("IP Address = 1.1.1.1", "Monitored Addresses");

        easmClient.putSavedFilter(savedFilterName, savedFilterRequest);

        String monitorFilter = easmClient.getSavedFilter(savedFilterName).getFilter();

        easmClient.listSavedFilter(monitorFilter, 0, 10)
                .forEach(SavedFilterSample::monitor);

        AssetUpdateData assetUpdateRequest = new AssetUpdateData().setState(AssetUpdateState.CONFIRMED);
        easmClient.updateAssets(monitorFilter, assetUpdateRequest);

        // Should your needs change, the filter can be updated with no need to update the scripts it's used in
        // Simply submit a new `savedFiltersPut` request to replace the old description and filter with a new set

        SavedFilterData newSavedFilterRequest = new SavedFilterData("IP Address = 0.0.0.0", "Monitoring Addresses");

        easmClient.putSavedFilter(savedFilterName, newSavedFilterRequest);

    }
}
