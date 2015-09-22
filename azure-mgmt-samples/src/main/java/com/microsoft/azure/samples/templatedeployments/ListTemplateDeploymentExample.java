package com.microsoft.azure.samples.templatedeployments;

import com.microsoft.azure.management.resources.DeploymentOperations;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementService;
import com.microsoft.azure.management.resources.models.DeploymentExtended;
import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.utility.AuthHelper;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample showing listing of template deployments across all resource groups within a subscription using a
 * service principal for authentication. To use the sample please set following environment variables or simply
 * replace the getenv call with actual values:
 *
 *      management.uri                  = https://management.core.windows.net/
 *      arm.url                         = https://management.azure.com/
 *      arm.aad.url                     = https://login.windows.net/
 *      arm.clientid                    = [your service principal client id]
 *      arm.clientkey                   = [your service principal client key]
 *      arm.tenant                      = [your service principal tenant]
 *      management.subscription.id      = [your subscription id (GUID)]
 */
public class ListTemplateDeploymentExample {
    public static void main(String[] args) {
        try {
            ResourceManagementClient resourceManagementClient = createResourceManagementClient();

            // list all resource groups
            List<ResourceGroupExtended> resourceGroups = getResourceGroups(resourceManagementClient);

            // list template deployments for each resource group
            DeploymentOperations deploymentOperations = resourceManagementClient.getDeploymentsOperations();
            for(ResourceGroupExtended resourceGroup : resourceGroups) {
                ArrayList<DeploymentExtended> deployments = deploymentOperations.list(resourceGroup.getName(), null).getDeployments();
                System.out.printf("%s\n", resourceGroup.getName());
                for(DeploymentExtended deployment : deployments) {
                    System.out.printf("    %s\n", deployment.getName());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches a list of resource groups that have been created in the given subscription.
     * @param resourceManagementClient A valid {@link ResourceManagementClient} object.
     * @return A list of {@link ResourceGroupExtended} objects representing each resource group.
     * @throws ServiceException
     * @throws IOException
     * @throws URISyntaxException
     */
    protected static List<ResourceGroupExtended> getResourceGroups(ResourceManagementClient resourceManagementClient) throws ServiceException, IOException, URISyntaxException {
        return resourceManagementClient.getResourceGroupsOperations().list(null).getResourceGroups();
    }

    /**
     * Use the ResourceManagementService factory helper method to create a client based on the management config.
     *
     * @return ResourceManagementClient a client to be used to make authenticated requests to the ARM REST API
     * @throws Exception all of the exceptions
     */
    protected static ResourceManagementClient createResourceManagementClient() throws Exception {
        Configuration config = createConfiguration();
        return ResourceManagementService.create(config);
    }

    /**
     * Create configuration builds the management configuration needed for creating the clients.
     * The config contains the baseURI which is the base of the ARM REST service, the subscription id as the context for
     * the ResourceManagementService and the AAD token required for the HTTP Authorization header.
     *
     * @return Configuration the generated configuration
     * @throws Exception all of the exceptions!!
     */
    public static Configuration createConfiguration() throws Exception {
        String baseUri = System.getenv("arm.url");

        return ManagementConfiguration.configure(
                null,
                baseUri != null ? new URI(baseUri) : null,
                System.getenv(ManagementConfiguration.SUBSCRIPTION_ID),
                AuthHelper.getAccessTokenFromServicePrincipalCredentials(
                        System.getenv(ManagementConfiguration.URI), System.getenv("arm.aad.url"),
                        System.getenv("arm.tenant"), System.getenv("arm.clientid"),
                        System.getenv("arm.clientkey"))
                        .getAccessToken());
    }
}
