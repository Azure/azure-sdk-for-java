package com.microsoft.azure.utility;

import com.google.gson.Gson;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.models.*;
import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.exception.ServiceException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class ResourceHelper {

    /**
     * Helper class to serialize the parameter values in the format expected by Azure.
     */
    private static class ParameterValue {
        String value;
        ParameterValue(String value) {
            this.value = value;
        }
    }

    /**
     * This is a helper routine that creates a template deployment from an ARM template supplied
     * as a HTTP URI. The input parameters to the template are passed in via a {@link Map}.
     * @param resourceManagementClient A valid {@link ResourceManagementClient} object.
     * @param resourceGroupName The name of the resource group in which the deployment is to be created. This
     *                          resource group must exist before this method is called.
     * @param deploymentMode Specifies the mode that is used to deploy resources. This value could be either
     *                       <strong>Incremental</strong> or <strong>Complete</strong>. In Incremental mode,
     *                       resources are deployed without deleting existing resources that are not included in
     *                       the template. In complete mode resources are deployed and existing resources in the
     *                       resource group not included in the template are deleted.
     * @param deploymentName The name of the deployment.
     * @param templateUri A HTTP URI pointing to the location of the ARM template JSON.
     * @param templateContentVersion The version of the ARM template. This value must match the value of the
     *                               <code>contentVersion</code> property in the ARM template.
     * @param templateParameters The input parameters that the template requires.
     * @return A {@link DeploymentExtended} object representing the deployment.
     * @throws URISyntaxException
     * @throws IOException
     * @throws ServiceException
     */
    public static DeploymentExtended createTemplateDeploymentFromURI(
            ResourceManagementClient resourceManagementClient,
            String resourceGroupName,
            DeploymentMode deploymentMode,
            String deploymentName,
            String templateUri,
            String templateContentVersion,
            Map<String, String> templateParameters) throws URISyntaxException, IOException, ServiceException {

        DeploymentProperties deploymentProperties = new DeploymentProperties();
        deploymentProperties.setMode(deploymentMode);

        // set the link to template JSON
        TemplateLink templateLink = new TemplateLink(new URI(templateUri));
        templateLink.setContentVersion(templateContentVersion);
        deploymentProperties.setTemplateLink(templateLink);

        // initialize the parameters for this template
        Map<String, ParameterValue> parameters = new HashMap<String, ParameterValue>();
        for(Map.Entry<String, String> entry : templateParameters.entrySet()) {
            parameters.put(entry.getKey(), new ParameterValue(entry.getValue()));
        }
        deploymentProperties.setParameters(new Gson().toJson(parameters));

        // kick off the deployment
        Deployment deployment = new Deployment();
        deployment.setProperties(deploymentProperties);

        return resourceManagementClient
                .getDeploymentsOperations()
                .createOrUpdate(resourceGroupName, deploymentName, deployment)
                .getDeployment();
    }
}
