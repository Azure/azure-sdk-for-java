/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangMethodDefinition;
import com.microsoft.azure.management.apigeneration.LangMethodDefinition.LangMethodType;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.DeploymentExtendedInner;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;

/**
 * An immutable client-side representation of an Azure deployment.
 */
@LangDefinition(ContainerName = "~/")
public interface Deployment extends
        Refreshable<Deployment>,
        Updatable<Deployment.Update>,
        Wrapper<DeploymentExtendedInner> {

    /**
     * @return the name of this deployment's resource group
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
    @LangMethodDefinition(AsType = LangMethodType.Property)
    DeploymentOperations deploymentOperations();

    /**
     * Cancel a currently running template deployment.
     */
    void cancel();

    /**
     * Exports a deployment template.
     *
     * @return the export result
     */
    DeploymentExportResult exportTemplate();

    /**
     * Container interface for all the deployment definitions.
     */
    @LangDefinition(ContainerName = "~/Deployment.Definition")
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithTemplate,
        DefinitionStages.WithParameters,
        DefinitionStages.WithMode,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the deployment definition stages.
     */
    @LangDefinition(ContainerName = "~/Deployment.Definition", ContainerFileName = "IDefinition", IsContainerOnly = true)
    interface DefinitionStages {
        /**
         * The first stage of deployment definition.
         */
        interface Blank extends DefinitionStages.WithGroup {
        }

        /**
         * A deployment definition allowing resource group to be specified.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithExistingResourceGroup<WithTemplate> {
            /**
             * Creates a new resource group to put the deployment in.
             * @param name the name of the new group
             * @param region the region to create the resource group in
             * @return the next stage of the deployment definition
             */
            WithTemplate withNewResourceGroup(String name, Region region);

            /**
             * Creates a new resource group to put the resource in, based on the definition specified.
             * @param groupDefinition a creatable definition for a new resource group
             * @return the next stage of the deployment definition
             */
            WithTemplate withNewResourceGroup(Creatable<ResourceGroup> groupDefinition);
        }

        /**
         * A deployment definition allowing the template to be specified.
         */
        interface WithTemplate {
            /**
             * Specifies the template as a Java object.
             *
             * @param template the Java object
             * @return the next stage of the deployment definition
             */
            WithParameters withTemplate(Object template);

            /**
             * Specifies the template as a JSON string.
             *
             * @param templateJson the JSON string
             * @return the next stage of the deployment definition
             * @throws IOException exception thrown from serialization/deserialization
             */
            WithParameters withTemplate(String templateJson) throws IOException;

            /**
             * Specifies the template as a URL.
             *
             * @param uri the location of the remote template file
             * @param contentVersion the version of the template file
             * @return the next stage of the deployment definition
             */
            WithParameters withTemplateLink(String uri, String contentVersion);
        }

        /**
         * A deployment definition allowing the parameters to be specified.
         */
        interface WithParameters {
            /**
             * Specifies the parameters as a Java object.
             *
             * @param parameters the Java object
             * @return the next stage of the deployment definition
             */
            WithMode withParameters(Object parameters);

            /**
             * Specifies the parameters as a JSON string.
             * @param parametersJson the JSON string
             * @return the next stage of the deployment definition
             * @throws IOException exception thrown from serialization/deserialization
             */
            WithMode withParameters(String parametersJson) throws IOException;

            /**
             * Specifies the parameters as a URL.
             *
             * @param uri the location of the remote parameters file
             * @param contentVersion the version of the parameters file
             * @return the next stage of the deployment definition
             */
            WithMode withParametersLink(String uri, String contentVersion);
        }

        /**
         * A deployment definition allowing the deployment mode to be specified.
         */
        interface WithMode {
            /**
             * Specifies the deployment mode.
             *
             * @param mode the mode of the deployment
             * @return the next stage of the deployment definition
             */
            WithCreate withMode(DeploymentMode mode);
        }

        /**
         * A deployment definition with sufficient inputs to create a new
         * deployment in the cloud, but exposing additional optional inputs to specify.
         */
        interface WithCreate extends Creatable<Deployment> {
            Deployment beginCreate();
        }
    }

    /**
     * Grouping of all the deployment updates stages.
     */
    @LangDefinition(ContainerName = "~/Deployment.Update", ContainerFileName = "IUpdate", IsContainerOnly = true)
    interface UpdateStages {
        /**
         * A deployment update allowing to change the deployment mode.
         */
        interface WithMode {
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
        interface WithTemplate {
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
             * @throws IOException exception thrown from serialization/deserialization
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
        interface WithParameters {
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
             * @throws IOException exception thrown from serialization/deserialization
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
    }

    /**
     * The template for a deployment update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the deployment in Azure.
     */
    @LangDefinition(ContainerName = "~/Deployment.Update")
    interface Update extends
            Appliable<Deployment>,
            UpdateStages.WithTemplate,
            UpdateStages.WithParameters,
            UpdateStages.WithMode {
    }
}
