// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.models.GenericResource;
import com.azure.resourcemanager.resources.models.PolicyAssignment;
import com.azure.resourcemanager.resources.models.PolicyDefinition;
import com.azure.resourcemanager.resources.models.PolicyType;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PolicyTests extends TestBase {
    protected ResourceManager resourceManager;
    private String policyRule = "{\"if\":{\"not\":{\"field\":\"location\",\"in\":[\"southcentralus\",\"westeurope\"]}},\"then\":{\"effect\":\"deny\"}}";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        resourceManager = ResourceManager
                .authenticate(httpPipeline, profile)
                .withSdkContext(sdkContext)
                .withDefaultSubscription();
    }

    @Override
    protected void cleanUpResources() {

    }

    @Test
    public void canCRUDPolicyDefinition() throws Exception {
        String policyName = generateRandomResourceName("policy", 15);
        String displayName = generateRandomResourceName("mypolicy", 15);
        try {
            // Create
            PolicyDefinition definition = resourceManager.policyDefinitions().define(policyName)
                    .withPolicyRuleJson(policyRule)
                    .withPolicyType(PolicyType.CUSTOM)
                    .withDisplayName(displayName)
                    .withDescription("This is my policy")
                    .create();
            Assertions.assertEquals(policyName, definition.name());
            Assertions.assertEquals(PolicyType.CUSTOM, definition.policyType());
            Assertions.assertEquals(displayName, definition.displayName());
            Assertions.assertEquals("This is my policy", definition.description());
            // List
            PagedIterable<PolicyDefinition> definitions = resourceManager.policyDefinitions().list();
            boolean found = false;
            for (PolicyDefinition def : definitions) {
                if (definition.id().equalsIgnoreCase(def.id())) {
                    found = true;
                }
            }
            Assertions.assertTrue(found);
            // Get
            definition = resourceManager.policyDefinitions().getByName(policyName);
            Assertions.assertNotNull(definition);
            Assertions.assertEquals(displayName, definition.displayName());
        } finally {
            // Delete
            resourceManager.policyDefinitions().deleteByName(policyName);
        }
    }

    @Test
    public void canCRUDPolicyAssignment() throws Exception {
        String policyName = generateRandomResourceName("policy", 15);
        String displayName = generateRandomResourceName("mypolicy", 15);
        String rgName = generateRandomResourceName("javarg", 15);
        String assignmentName1 = generateRandomResourceName("assignment1", 15);
        String assignmentName2 = generateRandomResourceName("assignment2", 15);
        String resourceName = generateRandomResourceName("webassignment", 15);
        try {
            // Create definition
            PolicyDefinition definition = resourceManager.policyDefinitions().define(policyName)
                    .withPolicyRuleJson(policyRule)
                    .withPolicyType(PolicyType.CUSTOM)
                    .withDisplayName(displayName)
                    .withDescription("This is my policy")
                    .create();
            // Create assignment
            ResourceGroup group = resourceManager.resourceGroups().define(rgName)
                    .withRegion(Region.UK_WEST)
                    .create();
            PolicyAssignment assignment1 = resourceManager.policyAssignments().define(assignmentName1)
                    .forResourceGroup(group)
                    .withPolicyDefinition(definition)
                    .withDisplayName("My Assignment")
                    .create();

            Assertions.assertNotNull(assignment1);
            Assertions.assertEquals("My Assignment", assignment1.displayName());

            GenericResource resource = resourceManager.genericResources().define(resourceName)
                    .withRegion(Region.US_SOUTH_CENTRAL)
                    .withExistingResourceGroup(group)
                    .withResourceType("sites")
                    .withProviderNamespace("Microsoft.Web")
                    .withoutPlan()
                    .withApiVersion("2015-08-01")
                    .withParentResourcePath("")
                    .withProperties(new ObjectMapper().readTree("{\"SiteMode\":\"Limited\",\"ComputeMode\":\"Shared\"}"))
                    .create();

            PolicyAssignment assignment2 = resourceManager.policyAssignments().define(assignmentName2)
                    .forResource(resource)
                    .withPolicyDefinition(definition)
                    .withDisplayName("My Assignment 2")
                    .create();

            Assertions.assertNotNull(assignment2);
            Assertions.assertEquals("My Assignment 2", assignment2.displayName());

            PagedIterable<PolicyAssignment> assignments = resourceManager.policyAssignments().listByResourceGroup(rgName);
            Assertions.assertTrue(TestUtilities.getSize(assignments) >= 2);

            boolean foundAssignment1 = false;
            boolean foundAssignment2 = false;
            for (PolicyAssignment policyAssignment : assignments) {
                if (policyAssignment.id().equalsIgnoreCase(assignment1.id())) {
                    foundAssignment1 = true;
                } else if (policyAssignment.id().equalsIgnoreCase(assignment2.id())) {
                    foundAssignment2 = true;
                }
            }
            Assertions.assertTrue(foundAssignment1);
            Assertions.assertTrue(foundAssignment2);

            // Delete
            resourceManager.policyAssignments().deleteById(assignment1.id());
            resourceManager.policyAssignments().deleteById(assignment2.id());
            resourceManager.policyDefinitions().deleteByName(policyName);
        } finally {
            resourceManager.resourceGroups().deleteByName(rgName);
        }
    }
}
