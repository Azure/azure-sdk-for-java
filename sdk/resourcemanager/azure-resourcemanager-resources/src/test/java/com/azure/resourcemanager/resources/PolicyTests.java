// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.resourcemanager.resources.models.GenericResource;
import com.azure.resourcemanager.resources.models.PolicyAssignment;
import com.azure.resourcemanager.resources.models.PolicyDefinition;
import com.azure.resourcemanager.resources.models.PolicyType;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.azure.core.management.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PolicyTests extends ResourceManagementTest {
    private String policyRule = "{\"if\":{\"not\":{\"field\":\"location\",\"in\":[\"southcentralus\",\"westeurope\"]}},\"then\":{\"effect\":\"deny\"}}";

    @Override
    protected void cleanUpResources() {

    }

    @Test
    public void canCRUDPolicyDefinition() throws Exception {
        String policyName = generateRandomResourceName("policy", 15);
        String displayName = generateRandomResourceName("mypolicy", 15);
        try {
            // Create
            PolicyDefinition definition = resourceClient.policyDefinitions().define(policyName)
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
            PagedIterable<PolicyDefinition> definitions = resourceClient.policyDefinitions().list();
            boolean found = false;
            for (PolicyDefinition def : definitions) {
                if (definition.id().equalsIgnoreCase(def.id())) {
                    found = true;
                }
            }
            Assertions.assertTrue(found);
            // Get
            definition = resourceClient.policyDefinitions().getByName(policyName);
            Assertions.assertNotNull(definition);
            Assertions.assertEquals(displayName, definition.displayName());
        } finally {
            // Delete
            resourceClient.policyDefinitions().deleteByName(policyName);
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
            PolicyDefinition definition = resourceClient.policyDefinitions().define(policyName)
                    .withPolicyRuleJson(policyRule)
                    .withPolicyType(PolicyType.CUSTOM)
                    .withDisplayName(displayName)
                    .withDescription("This is my policy")
                    .create();
            // Create assignment
            ResourceGroup group = resourceClient.resourceGroups().define(rgName)
                    .withRegion(Region.UK_WEST)
                    .create();
            PolicyAssignment assignment1 = resourceClient.policyAssignments().define(assignmentName1)
                    .forResourceGroup(group)
                    .withPolicyDefinition(definition)
                    .withDisplayName("My Assignment")
                    .create();

            Assertions.assertNotNull(assignment1);
            Assertions.assertEquals("My Assignment", assignment1.displayName());

            GenericResource resource = resourceClient.genericResources().define(resourceName)
                    .withRegion(Region.US_SOUTH_CENTRAL)
                    .withExistingResourceGroup(group)
                    .withResourceType("sites")
                    .withProviderNamespace("Microsoft.Web")
                    .withoutPlan()
                    .withApiVersion("2015-08-01")
                    .withParentResourcePath("")
                    .withProperties(new ObjectMapper().readTree("{\"SiteMode\":\"Limited\",\"ComputeMode\":\"Shared\"}"))
                    .create();

            PolicyAssignment assignment2 = resourceClient.policyAssignments().define(assignmentName2)
                    .forResource(resource)
                    .withPolicyDefinition(definition)
                    .withDisplayName("My Assignment 2")
                    .create();

            Assertions.assertNotNull(assignment2);
            Assertions.assertEquals("My Assignment 2", assignment2.displayName());

            PagedIterable<PolicyAssignment> assignments = resourceClient.policyAssignments().listByResourceGroup(rgName);
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
            resourceClient.policyAssignments().deleteById(assignment1.id());
            resourceClient.policyAssignments().deleteById(assignment2.id());
            resourceClient.policyDefinitions().deleteByName(policyName);
        } finally {
            resourceClient.resourceGroups().deleteByName(rgName);
        }
    }
}
