package java.com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.EasmClient;
import com.azure.analytics.defender.easm.EasmClientBuilder;
import com.azure.analytics.defender.easm.models.DiscoGroup;
import com.azure.analytics.defender.easm.models.DiscoGroupData;
import com.azure.analytics.defender.easm.models.DiscoTemplate;
import com.azure.core.util.Configuration;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
import com.azure.resourcemanager.defendereasm.EASMClient;
import com.azure.resourcemanager.defendereasm.EASMClientBuilder;
import com.azure.resourcemanager.defendereasm.models.DiscoGroupRequest;
import com.azure.resourcemanager.defendereasm.models.DiscoGroupResult;
import com.azure.resourcemanager.defendereasm.models.DiscoTemplateResult;

import java.util.Scanner;

public class DiscoTemplateSample {
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
                .credential(new InteractiveBrowserCredentialBuilder().build())
                .buildClient();

        // The discoveryTemplatesList method can be used to find a discovery template using a filter.
        // The endpoint will return templates based on a partial match on the name field.

        String partialName = Configuration.getGlobalConfiguration().get("PARTIAL_NAME");
        System.out.println("Partial name is " + partialName);
        easmClient.listDiscoTemplate(partialName, 0, 25)
                .forEach((discoTemplateResult -> {
                    System.out.println(discoTemplateResult.getId() + ": " + discoTemplateResult.getDisplayName());
                }));

        // To get more detail about a disco template, we can use the discoveryTemplatesGet method.
        // From here, we can see the names and seeds which would be used in a discovery run.

        System.out.println("Enter the templateid:");

        Scanner scanner = new Scanner(System.in);
        String templateId = scanner.nextLine();

        DiscoTemplate discoTemplateResult = easmClient.getDiscoTemplate(templateId);

        System.out.println("Chosen template id: " + discoTemplateResult.getId());
        System.out.println("The following names will be used:");
        discoTemplateResult.getNames().forEach(System.out::println);
        System.out.println("The following seeds will be used:");
        discoTemplateResult.getSeeds().forEach(discoSource -> {
            System.out.println(discoSource.getKind() + ", " + discoSource.getName());
        });

        String groupName = "Sample discovery group";
        DiscoGroupData discoGroupRequest = new DiscoGroupData().setTemplateId(templateId);

        DiscoGroup discoGroupResult = easmClient.putDiscoGroup(groupName, discoGroupRequest);

        easmClient.runDiscoGroup(groupName);


    }
}
