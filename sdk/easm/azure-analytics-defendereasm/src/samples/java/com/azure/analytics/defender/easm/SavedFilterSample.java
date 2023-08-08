package java.com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.AssetsClient;
import com.azure.analytics.defender.easm.EasmDefenderClientBuilder;
import com.azure.analytics.defender.easm.SavedFiltersClient;
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

        EasmDefenderClientBuilder easmDefenderClientBuilder = new EasmDefenderClientBuilder()
            .endpoint(endpoint)
            .subscriptionId(subscriptionId)
            .workspaceName(workspaceName)
            .resourceGroupName(resourceGroupName)
            .credential(new InteractiveBrowserCredentialBuilder().build());

        SavedFiltersClient savedFiltersClient = easmDefenderClientBuilder.buildSavedFiltersClient();
        AssetsClient assetsClient = easmDefenderClientBuilder.buildAssetsClient();

        String savedFilterName = "Sample saved filter";
        SavedFilterData savedFilterData = new SavedFilterData("IP Address = 1.1.1.1", "Monitored Addresses");

        savedFiltersClient.put(savedFilterName, savedFilterData);

        String monitorFilter = savedFiltersClient.get(savedFilterName).getFilter();

        savedFiltersClient.list(monitorFilter, 0, 10)
                .getValue()
                .forEach(SavedFilterSample::monitor);

        AssetUpdateData assetUpdateData = new AssetUpdateData().setState(AssetUpdateState.CONFIRMED);
        assetsClient.update(monitorFilter, assetUpdateData);

        // Should your needs change, the filter can be updated with no need to update the scripts it's used in
        // Simply submit a new `savedFiltersPut` request to replace the old description and filter with a new set

        SavedFilterData newSavedFilterData = new SavedFilterData("IP Address = 0.0.0.0", "Monitoring Addresses");

        savedFiltersClient.put(savedFilterName, newSavedFilterData);

    }
}
