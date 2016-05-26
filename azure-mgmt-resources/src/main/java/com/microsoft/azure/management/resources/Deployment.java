/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
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
 * An immutable client-side representation of an Azure deployment.
 */
public interface Deployment extends
        Refreshable<Deployment>,
        Wrapper<DeploymentExtendedInner> {

    /**
     * @return the name of the resource group
     */
    String resourceGroupName();

    /**
     * @return the name of the deployment
     */
    String name();

    /**
     * @return the state of the provisioning process of the resources being deployed
     */
    String provisioningState();

    /**
     * @return the correlation ID of the deployment
     */
    String correlationid();

    /**
     * @return the timestamp of the template deployment
     */
    DateTime timestamp();

    /**
     * @return key/value pairs that represent deployment output
     */
    Object outputs();

    /**
     * @return the list of resource providers needed for the deployment
     */
    List<Provider> providers();

    /**
     * @return the list of deployment dependencies
     */
    List<Dependency> dependencies();

    /**
     * @return the template content
     */
    Object template();

    /**
     * @return the URI referencing the template
     */
    TemplateLink templateLink();

    /**
     * @return the deployment parameters
     */
    Object parameters();

    /**
     * @return the URI referencing the parameters
     */
    ParametersLink parametersLink();

    /**
     * @return the deployment mode. Possible values include:
     * 'Incremental', 'Complete'.
     */
    DeploymentMode mode();

    /**
     * @return the operations related to this deployment
     */
    DeploymentOperations deploymentOperations();

    /**
     * A deployment definition allowing resource group to be specified.
     */
    interface DefinitionBlank extends GroupableResource.DefinitionWithGroup<DefinitionWithTemplate> {
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
