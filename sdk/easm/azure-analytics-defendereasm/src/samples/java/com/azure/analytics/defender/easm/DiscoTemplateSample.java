package java.com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.DiscoveryGroupsClient;
import com.azure.analytics.defender.easm.DiscoveryTemplatesClient;
import com.azure.analytics.defender.easm.EasmDefenderClientBuilder;
import com.azure.analytics.defender.easm.models.DiscoGroup;
import com.azure.analytics.defender.easm.models.DiscoGroupData;
import com.azure.analytics.defender.easm.models.DiscoTemplate;
import com.azure.core.util.Configuration;
import com.azure.identity.InteractiveBrowserCredentialBuilder;

import java.util.Scanner;

public class DiscoTemplateSample {
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

        // We initialize the discovery templates and group clients
        DiscoveryTemplatesClient discoveryTemplatesClient = easmDefenderClientBuilder.buildDiscoveryTemplatesClient();
        DiscoveryGroupsClient discoveryGroupsClient = easmDefenderClientBuilder.buildDiscoveryGroupsClient();

        // The discoveryTemplatesList method can be used to find a discovery template using a filter.
        // The endpoint will return templates based on a partial match on the name field.

        String partialName = Configuration.getGlobalConfiguration().get("PARTIAL_NAME");
        System.out.println("Partial name is " + partialName);
        discoveryTemplatesClient.list(partialName, 0, 25)
                .getValue()
                .forEach((discoTemplateResponse -> {
                    System.out.println(discoTemplateResponse.getId() + ": " + discoTemplateResponse.getDisplayName());
                }));

        // To get more detail about a disco template, we can use the discoveryTemplatesGet method.
        // From here, we can see the names and seeds which would be used in a discovery run.

        System.out.println("Enter the templateid:");

        Scanner scanner = new Scanner(System.in);
        String templateId = scanner.nextLine();

        DiscoTemplate discoTemplateResponse = discoveryTemplatesClient.get(templateId);

        System.out.println("Chosen template id: " + discoTemplateResponse.getId());
        System.out.println("The following names will be used:");
        discoTemplateResponse.getNames().forEach(System.out::println);
        System.out.println("The following seeds will be used:");
        discoTemplateResponse.getSeeds().forEach(discoSource -> {
            System.out.println(discoSource.getKind() + ", " + discoSource.getName());
        });

        String groupName = "Sample discovery group";
        DiscoGroupData discoGroupData = new DiscoGroupData().setTemplateId(templateId);

        DiscoGroup discoGroup = discoveryGroupsClient.put(groupName, discoGroupData);

        discoveryGroupsClient.run(groupName);


    }
}
