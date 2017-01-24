package com.microsoft.azure.management.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.credentials.UserTokenCredentials;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.fail;

public class PolicyTests extends TestBase {
    protected static ResourceManager resourceManager;
    private String policyRule = "{\"if\":{\"not\":{\"field\":\"location\",\"in\":[\"northeurope\",\"westeurope\"]}},\"then\":{\"effect\":\"deny\"}}";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(defaultSubscription);
    }

    @Override
    protected void cleanUpResources() {

    }

    @Test
    @Ignore("Not authorized for scope - 'Microsoft.Authorization/policydefinitions/write'")
    public void canCRUDPolicyDefinition() throws Exception {
        // Create
        PolicyDefinition definition = resourceManager.policyDefinitions().define("policy1")
                .withPolicyRuleJson(policyRule)
                .withPolicyType(PolicyType.CUSTOM)
                .withDisplayName("My Policy")
                .withDescription("This is my policy")
                .create();
        Assert.assertEquals("policy1", definition.name());
        Assert.assertEquals(PolicyType.CUSTOM, definition.policyType());
        Assert.assertEquals("My Policy", definition.displayName());
        Assert.assertEquals("This is my policy", definition.description());
        // List
        List<PolicyDefinition> definitions = resourceManager.policyDefinitions().list();
        boolean found = false;
        for (PolicyDefinition def : definitions) {
            if (definition.id().equalsIgnoreCase(def.id())) {
                found = true;
            }
        }
        Assert.assertTrue(found);
        // Get
        definition = resourceManager.policyDefinitions().getByName("policy1");
        Assert.assertNotNull(definition);
        Assert.assertEquals("My Policy", definition.displayName());
        // Delete
        resourceManager.policyDefinitions().deleteById(definition.id());
    }

    @Test
    @Ignore("Not authorized for scope - 'Microsoft.Authorization/policydefinitions/write'")
    public void canCRUDPolicyAssignment() throws Exception {
        // Create definition
        PolicyDefinition definition = resourceManager.policyDefinitions().define("policy1")
                .withPolicyRuleJson(policyRule)
                .withPolicyType(PolicyType.CUSTOM)
                .withDisplayName("My Policy")
                .withDescription("This is my policy")
                .create();
        // Create assignment
        ResourceGroup group = resourceManager.resourceGroups().define("rgassignment115095")
                .withRegion(Region.UK_WEST)
                .create();
        PolicyAssignment assignment = resourceManager.policyAssignments().define("assignment1")
                .forResourceGroup(group)
                .withPolicyDefinition(definition)
                .withDisplayName("My Assignment")
                .create();
        // Verify
        try {
            GenericResource resource = resourceManager.genericResources().define("webassignment115095")
                    .withRegion(Region.US_SOUTH_CENTRAL)
                    .withExistingResourceGroup(group)
                    .withResourceType("sites")
                    .withProviderNamespace("Microsoft.Web")
                    .withoutPlan()
                    .withApiVersion("2015-08-01")
                    .withParentResourcePath("")
                    .withProperties(new ObjectMapper().readTree("{\"SiteMode\":\"Limited\",\"ComputeMode\":\"Shared\"}"))
                    .create();
            fail();
        } catch (CloudException ce) {
            // expected
            Assert.assertTrue(ce.getMessage().contains("disallowed"));
        }
        // Delete
        resourceManager.resourceGroups().define(group.name());
        resourceManager.policyAssignments().deleteById(assignment.id());
        resourceManager.policyDefinitions().deleteById(definition.id());
    }
}
