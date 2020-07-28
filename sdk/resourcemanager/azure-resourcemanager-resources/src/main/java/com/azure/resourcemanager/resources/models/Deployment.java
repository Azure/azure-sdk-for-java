// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluent.inner.DeploymentExtendedInner;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * An immutable client-side representation of an Azure deployment.
 */
@Fluent
public interface Deployment extends
        Indexable,
        Refreshable<Deployment>,
        Updatable<Deployment.Update>,
        HasInner<DeploymentExtendedInner>,
        HasManager<ResourceManager>,
        HasName,
        HasId {

    /**
     * @return the name of this deployment's resource group
     */
    String resourceGroupName();

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
    OffsetDateTime timestamp();

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
     * @return the hash produced for the template
     */
    String templateHash();

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
     * Get array of provisioned resources.
     *
     * @return the outputResources value
     */
    List<ResourceReference> outputResources();

    /**
     * @return the operations related to this deployment
     */
    DeploymentOperations deploymentOperations();

    /**
     * Cancel a currently running template deployment.
     */
    void cancel();

    /**
     * Cancel a currently running template deployment asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> cancelAsync();


    /**
     * Exports a deployment template.
     *
     * @return the export result
     */
    DeploymentExportResult exportTemplate();

    /**
     * Exports a deployment template asynchronously.
     *
     * @return a representation of the deferred computation of this call returning the export result
     */
    Mono<DeploymentExportResult> exportTemplateAsync();

    /**
     * Prepares a What-if operation.
     *
     * @return the What-if execution.
     */
    Execution prepareWhatIf();

    /**
     * Container interface for all the deployment definitions.
     */
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
             *
             * @param name the name of the new group
             * @param region the region to create the resource group in
             * @return the next stage of the definition
             */
            WithTemplate withNewResourceGroup(String name, Region region);

            /**
             * Creates a new resource group to put the resource in, based on the definition specified.
             *
             * @param groupDefinition a creatable definition for a new resource group
             * @return the next stage of the definition
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
             * @return the next stage of the definition
             */
            WithParameters withTemplate(Object template);

            /**
             * Specifies the template as a JSON string.
             *
             * @param templateJson the JSON string
             * @return the next stage of the definition
             * @throws IOException exception thrown from serialization/deserialization
             */
            WithParameters withTemplate(String templateJson) throws IOException;

            /**
             * Specifies the template as a URL.
             *
             * @param uri the location of the remote template file
             * @param contentVersion the version of the template file
             * @return the next stage of the definition
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
             * @return the next stage of the definition
             */
            WithMode withParameters(Object parameters);

            /**
             * Specifies the parameters as a JSON string.
             *
             * @param parametersJson the JSON string
             * @return the next stage of the definition
             * @throws IOException exception thrown from serialization/deserialization
             */
            WithMode withParameters(String parametersJson) throws IOException;

            /**
             * Specifies the parameters as a URL.
             *
             * @param uri the location of the remote parameters file
             * @param contentVersion the version of the parameters file
             * @return the next stage of the definition
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
             * @return the next stage of the definition
             */
            WithCreate withMode(DeploymentMode mode);
        }

        /**
         * A deployment definition with sufficient inputs to create a new
         * deployment in the cloud, but exposing additional optional inputs to specify.
         */
        interface WithCreate extends Creatable<Deployment> {
            /**
             * Begins creating the deployment resource.
             *
             * @return the accepted create operation
             */
            Accepted<Deployment> beginCreate();

            Mono<Deployment> beginCreateAsync();
        }
    }

    /**
     * Grouping of all the deployment updates stages.
     */
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
    interface Update extends
            Appliable<Deployment>,
            UpdateStages.WithTemplate,
            UpdateStages.WithParameters,
            UpdateStages.WithMode {
    }

    /**
     * Container interface for all the deployment execution.
     */
    interface Execution extends
            ExecutionStages.Blank,
            ExecutionStages.WithExecute,
            ExecutionStages.WithWhatIf,
            ExecutionStages.WithWhatIfDeploymentMode,
            ExecutionStages.WithWhatIfLocation,
            ExecutionStages.WithWhatIfOnErrorDeploymentType,
            ExecutionStages.WithWhatIfParameter,
            ExecutionStages.WithWhatIfResultFormat,
            ExecutionStages.WithWhatIfTemplate {
    }

    /**
     * Grouping of all the deployment execution stages.
     */
    interface ExecutionStages {
        /**
         * The first stage of deployment execution.
         */
        interface Blank {
        }

        /**
         * A deployment execution allowing What-if parameters to be specified.
         */
        interface WithWhatIf extends
                WithExecute,
                WithWhatIfDeploymentMode,
                WithWhatIfLocation,
                WithWhatIfOnErrorDeploymentType,
                WithWhatIfParameter,
                WithWhatIfResultFormat,
                WithWhatIfTemplate {
            /**
             * Specifies the type of information to log for debugging.
             *
             * @param detailedLevel the detailed value to set.
             * @return the next stage of the execution.
             */
            WithWhatIf withDetailedLevel(String detailedLevel);

            /**
             * Specifies the deployment name to be used on error cases.
             *
             * @param deploymentName the deployment name to set.
             * @return the next stage of the execution.
             */
            WithWhatIf withDeploymentName(String deploymentName);
        }

        /**
         * A deployment execution allowing data storage location to be specified.
         */
        interface WithWhatIfLocation {
            /**
             * Specifies the location to store the deployment data.
             *
             * @param location the location value to set.
             * @return the next stage of the execution.
             */
            WithWhatIf withLocation(String location);
        }

        /**
         * A deployment execution allowing deployment mode to be specified.
         */
        interface WithWhatIfDeploymentMode {
            /**
             * Specifies the mode with value of 'INCREMENTAL' in deployment properties.
             *
             * @return the next stage of the execution.
             */
            WithWhatIf withIncrementalMode();

            /**
             * Specifies the mode with value of 'COMPLETE' in deployment properties.
             *
             * @return the next stage of the execution.
             */
            WithWhatIf withCompleteMode();
        }

        /**
         * A deployment execution allowing result format to be specified.
         */
        interface WithWhatIfResultFormat {
            /**
             * Specifies the result format with value of 'FULL_RESOURCE_PAYLOADS'
             * in What-if settings of deployment properties.
             *
             * @return the next stage of the execution.
             */
            WithWhatIf withFullResourcePayloadsResultFormat();

            /**
             * Specifies the result format with value of 'RESOURCE_ID_ONLY'
             * in What-if settings of deployment properties.
             *
             * @return the next stage of the execution.
             */
            WithWhatIf withResourceIdOnlyResultFormat();
        }

        /**
         * A deployment execution allowing template to be specified.
         */
        interface WithWhatIfTemplate {
            /**
             * Specifies the template content.
             *
             * @param template the template value to set.
             * @return the next stage of the execution.
             */
            WithWhatIf withWhatIfTemplate(Object template);

            /**
             * Specifies the uri and content version of template.
             *
             * @param uri the uri value to set.
             * @param contentVersion the content version value to set.
             * @return the next stage of the execution.
             */
            WithWhatIf withWhatIfTemplateLink(String uri, String contentVersion);
        }

        /**
         * A deployment execution allowing parameter to be specified.
         */
        interface WithWhatIfParameter {
            /**
             * Specifies the name and value pairs that define the deployment parameters for the template.
             *
             * @param parameters the parameters value to set.
             * @return the next stage of the execution.
             */
            WithWhatIf withWhatIfParameters(Object parameters);

            /**
             * Specifies the uri and content version of parameters file.
             *
             * @param uri the uri value to set.
             * @param contentVersion the content version value to set.
             * @return the next stage of the execution.
             */
            WithWhatIf withWhatIfParametersLink(String uri, String contentVersion);
        }

        /**
         * A deployment execution allowing on error deployment type to be specified.
         */
        interface WithWhatIfOnErrorDeploymentType {
            /**
             * Specifies the What-if deployment on error behavior type with value of 'LAST_SUCCESSFUL'.
             *
             * @return the next stage of the execution.
             */
            WithWhatIf withLastSuccessfulOnErrorDeployment();

            /**
             * Specifies the What-if deployment on error behavior type with value of 'SPECIFIC_DEPLOYMENT'.
             *
             * @return the next stage of the execution.
             */
            WithWhatIf withSpecialDeploymentOnErrorDeployment();
        }

        interface WithExecute {
            /**
             * Gets changes that will be made by the deployment if executed at the scope of the resource group.
             *
             * @return the next stage of the execution.
             */
            WhatIfOperationResult whatIf();

            /**
             * Gets changes that will be made by the deployment
             * if executed at the scope of the resource group asynchronously.
             *
             * @return the next stage of the execution.
             */
            Mono<WhatIfOperationResult> whatIfAsync();


            /**
             * Gets changes that will be made by the deployment if executed at the scope of the subscription.
             *
             * @return the next stage of the execution.
             */
            WhatIfOperationResult whatIfAtSubscriptionScope();

            /**
             * Gets changes that will be made by the deployment
             * if executed at the scope of the subscription asynchronously.
             *
             * @return the next stage of the execution.
             */
            Mono<WhatIfOperationResult> whatIfAtSubscriptionScopeAsync();
        }
    }
}
