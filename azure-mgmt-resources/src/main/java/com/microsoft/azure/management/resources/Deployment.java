package com.microsoft.azure.management.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.api.Dependency;
import com.microsoft.azure.management.resources.implementation.api.DeploymentExtendedInner;
import com.microsoft.azure.management.resources.implementation.api.DeploymentMode;
import com.microsoft.azure.management.resources.implementation.api.ParametersLink;
import com.microsoft.azure.management.resources.implementation.api.TemplateLink;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Defines the interface for accessing a deployment in Azure.
 */
public interface Deployment extends
        Refreshable<Deployment>,
        Wrapper<DeploymentExtendedInner> {

    /**
     * The resource group where the deployment is.
     *
     * @return the name of the resource group.
     */
    String resourceGroupName();

    /**
     * The name of the deployment.
     *
     * @return the name of the deployment.
     */
    String name();

    /**
     * Get the state of the provisioning.
     *
     * @return the state of the provisioning.
     */
    String provisioningState();

    /**
     * Get the correlation ID of the deployment.
     *
     * @return the correlation ID of the deployment.
     */
    String correlationid();

    /**
     * Get the timestamp of the template deployment.
     *
     * @return the timestamp of the template deployment.
     */
    DateTime timestamp();

    /**
     * Get key/value pairs that represent deployment output.
     *
     * @return key/value pairs that represent deployment output.
     */
    Object outputs();

    /**
     * Get the list of resource providers needed for the deployment.
     *
     * @return the list of resource providers needed for the deployment.
     */
    List<Provider> providers();

    /**
     * Get the list of deployment dependencies.
     *
     * @return the list of deployment dependencies.
     */
    List<Dependency> dependencies();

    /**
     * Get the template content.
     *
     * @return the template content.
     */
    Object template();

    /**
     * Get the URI referencing the template.
     *
     * @return the URI referencing the template.
     */
    TemplateLink templateLink();

    /**
     * Get the deployment parameters.
     *
     * @return the deployment parameters.
     */
    Object parameters();

    /**
     * Get the URI referencing the parameters.
     *
     * @return the URI referencing the parameters.
     */
    ParametersLink parametersLink();

    /**
     * Get the deployment mode. Possible values include:
     * 'Incremental', 'Complete'.
     *
     * @return the deployment mode.
     */
    DeploymentMode mode();

    /**
     * Get the operations related to this deployment.
     *
     * @return the {@link DeploymentOperations} operation.
     */
    DeploymentOperations deploymentOperations();

    /**
     * A deployment definition allowing resource group to be specified.
     */
    interface DefinitionBlank {
        DefinitionWithTemplate withNewResourceGroup(String resourceGroupName, Region location) throws Exception;
        DefinitionWithTemplate withExistingResourceGroup(String resourceGroupName);
    }

    /**
     * A deployment definition allowing template to be specified.
     */
    interface DefinitionWithTemplate {
        DefinitionWithParameters withTemplate(Object template);
        DefinitionWithParameters withTemplate(JsonNode template);
        DefinitionWithParameters withTemplateLink(String uri, String contentVersion);
    }

    /**
     * A deployment definition allowing parameters to be specified.
     */
    interface DefinitionWithParameters {
        DefinitionWithMode withParameters(Object parameters);
        DefinitionWithMode withParameters(JsonNode parameters);
        DefinitionWithMode withParametersLink(String uri, String contentVersion);
    }

    /**
     * A deployment definition allowing deployment mode to be specified.
     */
    interface DefinitionWithMode {
        DefinitionCreatable withMode(DeploymentMode mode);
    }

    /**
     * A deployment definition with sufficient inputs to create a new
     * deployment in the cloud, but exposing additional optional inputs to
     * specify.
     */
    interface DefinitionCreatable extends Creatable<Deployment> {
    }
}
