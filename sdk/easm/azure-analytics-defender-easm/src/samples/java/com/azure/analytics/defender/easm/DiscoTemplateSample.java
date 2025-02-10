// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.models.DiscoGroup;
import com.azure.analytics.defender.easm.models.DiscoGroupData;
import com.azure.analytics.defender.easm.models.DiscoTemplate;
import com.azure.core.util.Configuration;
import com.azure.identity.InteractiveBrowserCredentialBuilder;


import java.util.Scanner;

/**
 * This sample shows you how to use the discovery_groups module to create discovery groups using templates provided by the discovery_templates module of the EasmClient
 *
 * Set the following environment variables before running the sample:
 *     1) SUBSCRIPTION_ID - the subscription id for your resource
 *     2) WORKSPACENAME - the workspace name for your resource
 *     3) RESOURCEGROUPNAME - the resource group for your resource
 *     4) REGION - the azure region your resource is in
 *     5) PARTIAL_NAME - the search term for the templates. used for a case insensitive "contains" search
 */
public class DiscoTemplateSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("ENDPOINT");

        EasmClient easmClient = new EasmClientBuilder()
            .endpoint(endpoint)
            .credential(new InteractiveBrowserCredentialBuilder().build())
            .buildClient();

        // The discoveryTemplatesList method can be used to find a discovery template using a filter.
        // The endpoint will return templates based on a partial match on the name field.

        String partialName = Configuration.getGlobalConfiguration().get("PARTIAL_NAME");
        System.out.println("Partial name is " + partialName);
        easmClient.listDiscoTemplate(partialName, 0)
            .forEach((discoTemplateResult -> {
                System.out.println(discoTemplateResult.getId() + ": " + discoTemplateResult.getDisplayName());
            }));

        // To get more detail about a disco template, we can use the discoveryTemplatesGet method.
        // From here, we can see the names and seeds which would be used in a discovery run.

        System.out.println("Enter the template id:");

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

        DiscoGroup discoGroupResult = easmClient.createOrReplaceDiscoGroup(groupName, discoGroupRequest);

        easmClient.runDiscoGroup(discoGroupResult.getName());
    }
}
