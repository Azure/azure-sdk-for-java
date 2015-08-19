package com.microsoft.azure.samples.templatedeployments;

import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementService;
import com.microsoft.azure.management.resources.models.DeploymentExtended;
import com.microsoft.azure.management.resources.models.DeploymentMode;
import com.microsoft.azure.utility.AuthHelper;
import com.microsoft.azure.utility.ComputeHelper;
import com.microsoft.azure.utility.ResourceContext;
import com.microsoft.azure.utility.ResourceHelper;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sample showing creation of a new template deployment within a subscription using a service principal for
 * authentication. This sample uses the template JSON from the following location:
 *
 *      https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-simple-linux-vm/azuredeploy.json
 *
 * This template requires passing in a number of parameters. Those parameters can be supplied to this sample
 * via environment variables as documented below. All of them have a default value that will be used in case one
 * is not supplied.
 *
 * To use the sample please set following environment variables or simply replace the getenv call with
 * actual values:
 *
 *      group.name                      = [the name of the resource group to create; defaults to a randomly
 *                                        generated name]
 *      group.location                  = [the location where the resource should be created; for e.g. "West US";
 *                                        defaults to "West US"]
 *      deployment.name                 = [name of the deployment; defaults to a randomly generated name]
 *      storage.name                    = [name of the storage account to create for this deployment; defaults to
 *                                        a randomly generated name]
 *      admin.userName                  = [name of the admin user for the VM created from the template; defaults to
 *                                        'azureuser']
 *      admin.password                  = [password for the admin user for the VM created from the template;
 *                                        defaults to Password@123 !!!! DO NOT USE THIS PASSWORD IN PRODUCTION !!!!]
 *      dns.name                        = [unique DNS Name for the Public IP used to access the VM; defauls to a
 *                                        randomly generated name]
 *
 *      management.uri                  = https://management.core.windows.net/
 *      arm.url                         = https://management.azure.com/
 *      arm.aad.url                     = https://login.windows.net/
 *      arm.clientid                    = [your service principal client id]
 *      arm.clientkey                   = [your service principal client key]
 *      arm.tenant                      = [your service principal tenant]
 *      management.subscription.id      = [your subscription id (GUID)]
 */
public class CreateTemplateDeploymentExample {

    private static final String TEMPLATE_URI = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-simple-linux-vm/azuredeploy.json";

    public static void main(String[] args) {
        try {
            String resourceGroupName = getInput("group.name", generateRandomName("group"));
            String resourceGroupLocation = getInput("group.location", "West US");
            String deploymentName = getInput("depoloyment.name", generateRandomName("deployment"));

            // initialize input parameters for the template
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("newStorageAccountName", getInput("storage.name",
                    UUID.randomUUID().toString().replace("-", "").substring(0, 20)));
            parameters.put("adminUsername", getInput("admin.userName", "azureuser"));
            parameters.put("adminPassword", getInput("admin.password", "Password@123"));
            parameters.put("dnsNameForPublicIP", getInput("dns.name", generateRandomName("vm")));

            // create a new resource group
            ResourceManagementClient resourceManagementClient = createResourceManagementClient();
            ResourceContext resourceContext = new ResourceContext(
                    resourceGroupLocation, resourceGroupName,
                    System.getenv(ManagementConfiguration.SUBSCRIPTION_ID), false);
            ComputeHelper.createOrUpdateResourceGroup(resourceManagementClient, resourceContext);

            // create the template deployment
            DeploymentExtended deployment = ResourceHelper.createTemplateDeploymentFromURI(
                    resourceManagementClient,
                    resourceGroupName,
                    DeploymentMode.INCREMENTAL,
                    deploymentName,
                    TEMPLATE_URI,
                    "1.0.0.0",
                    parameters);

            System.out.printf("Created new deployment - %s in resource group - %s\n",
                    deployment.getName(), resourceGroupName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static String generateRandomName(String prefix) {
        return "azure-sample-" + prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    protected static String getInput(String envName, String defaultValue) {
        String val = System.getenv(envName);
        return (val == null || val.trim().isEmpty()) ? defaultValue : envName;
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
                        System.getenv(ManagementConfiguration.URI),
                        System.getenv("arm.aad.url"),
                        System.getenv("arm.tenant"),
                        System.getenv("arm.clientid"),
                        System.getenv("arm.clientkey"))
                        .getAccessToken());
    }
}
