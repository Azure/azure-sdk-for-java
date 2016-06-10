/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.api.Dependency;
import com.microsoft.azure.management.resources.implementation.api.DeploymentExtendedInner;
import com.microsoft.azure.management.resources.implementation.api.DeploymentMode;
import com.microsoft.azure.management.resources.implementation.api.ParametersLink;
import com.microsoft.azure.management.resources.implementation.api.TemplateLink;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;

/**
 * An immutable client-side representation of an Azure deployment.
 */
public interface Deployment extends
        Refreshable<Deployment>,
        Updatable<Deployment.Update>,
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
    String correlationId();

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
     * Cancel a currently running template deployment.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     */
    void cancel() throws CloudException, IOException;

    /**
     * Exports a deployment template.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @return the export result
     */
    DeploymentExportResult exportTemplate() throws CloudException, IOException;

    /**
     * A deployment definition allowing resource group to be specified.
     */
    interface DefinitionBlank {
        /**
         * Creates a new resource group for the deployment.
         *
         * @param resourceGroupName the name of the resource group
         * @param region the region for the resource group
         * @return the next stage of the deployment definition
         */
        DefinitionWithTemplate withNewResourceGroup(String resourceGroupName, Region region);

        /**
         * Specifies the name of an existing resource group for the deployment.
         *
         * @param resourceGroupName the name of the resource group
         * @return the next stage of the deployment definition
         */
        DefinitionWithTemplate withExistingResourceGroup(String resourceGroupName);
    }

    /**
     * A deployment definition allowing template to be specified.
     */
    interface DefinitionWithTemplate {
        /**
         * Specifies the template as a Java object.
         *
         * @param template the Java object
         * @return the next stage of the deployment definition
         */
        DefinitionWithParameters withTemplate(Object template);

        /**
         * Specifies the template as a JSON string.
         *
         * @param templateJson the JSON string
         * @return the next stage of the deployment definition
         */
        DefinitionWithParameters withTemplate(String templateJson) throws IOException;

        /**
         * Specifies the template as a URL.
         *
         * @param uri the location of the remote template file
         * @param contentVersion the version of the template file
         * @return the next stage of the deployment definition
         */
        DefinitionWithParameters withTemplateLink(String uri, String contentVersion);
    }

    /**
     * A deployment definition allowing parameters to be specified.
     */
    interface DefinitionWithParameters {
        /**
         * Specifies the parameters as a Java object.
         *
         * @param parameters the Java object
         * @return the next stage of the deployment definition
         */
        DefinitionWithMode withParameters(Object parameters);

        /**
         * Specifies the parameters as a JSON string.
         *
         * @param parametersJson the JSON string
         * @return the next stage of the deployment definition
         */
        DefinitionWithMode withParameters(String parametersJson) throws IOException;

        /**
         * Specifies the parameters as a URL.
         *
         * @param uri the location of the remote parameters file
         * @param contentVersion the version of the parameters file
         * @return the next stage of the deployment definition
         */
        DefinitionWithMode withParametersLink(String uri, String contentVersion);
    }

    /**
     * A deployment definition allowing deployment mode to be specified.
     */
    interface DefinitionWithMode {
        /**
         * Specifies the deployment mode.
         *
         * @param mode the mode of the deployment
         * @return the next stage of the deployment definition
         */
        DefinitionCreatable withMode(DeploymentMode mode);
    }

    /**
     * A deployment definition with sufficient inputs to create a new
     * deployment in the cloud, but exposing additional optional inputs to
     * specify.
     */
    interface DefinitionCreatable extends Creatable<Deployment> {
        Deployment beginCreate() throws Exception;
    }

    /**
     * A deployment update allowing to change the deployment mode.
     */
    interface UpdateWithDeploymentMode {
        /**
         * Specifies the deployment mode.
         *
         * @param mode the mode of the deployment
         * @return the next stage of the deployment update
         */
        Update withMode(DeploymentMode mode);
    }

    /**
     * A deployment update allowing to change the template.
     */
    interface UpdateWithTemplate {
        /**
         * Specifies the template as a Java object.
         *
         * @param template the Java object
         * @return the next stage of the deployment update
         */
        Update withTemplate(Object template);

        /**
         * Specifies the template as a JSON string.
         *
         * @param templateJson the JSON string
         * @return the next stage of the deployment update
         */
        Update withTemplate(String templateJson) throws IOException;

        /**
         * Specifies the template as a URL.
         *
         * @param uri the location of the remote template file
         * @param contentVersion the version of the template file
         * @return the next stage of the deployment update
         */
        Update withTemplateLink(String uri, String contentVersion);
    }

    /**
     * A deployment update allowing to change the parameters.
     */
    interface UpdateWithParameters {
        /**
         * Specifies the parameters as a Java object.
         *
         * @param parameters the Java object
         * @return the next stage of the deployment update
         */
        Update withParameters(Object parameters);

        /**
         * Specifies the parameters as a JSON string.
         *
         * @param parametersJson the JSON string
         * @return the next stage of the deployment update
         */
        Update withParameters(String parametersJson) throws IOException;

        /**
         * Specifies the parameters as a URL.
         *
         * @param uri the location of the remote parameters file
         * @param contentVersion the version of the parameters file
         * @return the next stage of the deployment update
         */
        Update withParametersLink(String uri, String contentVersion);
    }

    /**
     * The template for a deployment update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the deployment in Azure.
     */
    interface Update extends
            Appliable<Deployment>,
            UpdateWithTemplate,
            UpdateWithParameters,
            UpdateWithDeploymentMode {
    }
}
