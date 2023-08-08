package java.com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.DiscoveryGroupsClient;
import com.azure.analytics.defender.easm.EasmDefenderClientBuilder;
import com.azure.analytics.defender.easm.models.DiscoGroupData;
import com.azure.analytics.defender.easm.models.DiscoRunPageResponse;
import com.azure.analytics.defender.easm.models.DiscoSource;
import com.azure.analytics.defender.easm.models.DiscoSourceKind;
import com.azure.core.util.Configuration;
import com.azure.identity.InteractiveBrowserCredentialBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiscoveryRunsSample {
    public static void main(String[] args) {

        // To create an easmClient, you need your subscription ID, endpoint, and some sort of credential.
        String subscriptionId = Configuration.getGlobalConfiguration().get("SUBSCRIPTIONID");
        String workspaceName = Configuration.getGlobalConfiguration().get("WORKSPACENAME");
        String resourceGroupName = Configuration.getGlobalConfiguration().get("RESOURCEGROUPNAME");
        String endpoint = Configuration.getGlobalConfiguration().get("ENDPOINT");
        String discoveryGroupName = "Sample Disco";
        String discoveryGroupDescription = "This is a sample description for a discovery group";

        EasmDefenderClientBuilder easmDefenderClientBuilder = new EasmDefenderClientBuilder()
            .endpoint(endpoint)
            .subscriptionId(subscriptionId)
            .workspaceName(workspaceName)
            .resourceGroupName(resourceGroupName)
            .credential(new InteractiveBrowserCredentialBuilder().build());

        DiscoveryGroupsClient discoveryGroupsClient = easmDefenderClientBuilder.buildDiscoveryGroupsClient();

        // In order to start discovery runs, we must first create a discovery group, which is a collection of known assets that we can pivot off of.
        // These are created using the discoveryGroupsPut method

        List<DiscoSource> seeds = Stream.concat(
                Arrays.stream(Configuration.getGlobalConfiguration().get("HOSTS").split(","))
                        .map((host) -> new DiscoSource().setName(host).setKind(DiscoSourceKind.HOST)),
                Arrays.stream(Configuration.getGlobalConfiguration().get("DOMAINS").split(","))
                        .map((domain) -> new DiscoSource().setName(domain).setKind(DiscoSourceKind.DOMAIN))
        ).collect(Collectors.toList());

        DiscoGroupData discoGroupRequest = new DiscoGroupData().setName(discoveryGroupName).setDescription(discoveryGroupDescription).setSeeds(seeds);

        discoveryGroupsClient.put(discoveryGroupName, discoGroupRequest);

        // Discovery groups created through the API's `put` method aren't run automatically, so we need to start the run ourselves.
        discoveryGroupsClient.run(discoveryGroupName);

        discoveryGroupsClient.list()
                .getValue()
                .forEach((discoGroupResponse -> {
                    System.out.println(discoGroupResponse.getName());
                    DiscoRunPageResponse discoRunPageResponse = discoveryGroupsClient.listRuns(discoGroupResponse.getName());
                    long elementsToPrint = discoRunPageResponse.getTotalElements() > 5 ? 5 : discoRunPageResponse.getTotalElements();
                    discoRunPageResponse.getValue().subList(0, (int) elementsToPrint).forEach(discoRunResponse -> {
                        System.out.println(" - started: " + discoRunResponse.getStartedDate()
                                            + ", finished: " + discoRunResponse.getCompletedDate()
                                            + ", assets found: " + discoRunResponse.getTotalAssetsFoundCount());
                    });
                }));
    }
}
