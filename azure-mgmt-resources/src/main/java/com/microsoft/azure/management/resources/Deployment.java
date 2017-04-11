/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.LangMethodDefinition;
import com.microsoft.azure.management.apigeneration.LangMethodDefinition.LangMethodType;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.implementation.DeploymentExtendedInner;
import com.microsoft.azure.management.resources.implementation.ResourceManager;

import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import org.joda.time.DateTime;
import rx.Completable;
import rx.Observable;

import java.io.IOException;
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
        HasName {

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
    @Method
    void cancel();

    /**
     * Cancel a currently running template deployment asynchronously.
     * @return a representation of the deferred computation of this call
     */
    @Beta
    @Method
    Completable cancelAsync();

    /**
     * Cancel a currently running template deployment asynchronously.
     *
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    @Beta
    @Method
    ServiceFuture<Void> cancelAsync(ServiceCallback<Void> callback);

    /**
     * Exports a deployment template.
     *
     * @return the export result
     */
    @Method
    DeploymentExportResult exportTemplate();

    /**
     * Exports a deployment template asynchronously.
     *
     * @return observable to the export result
     */
    @Method
    @Beta
    Observable<DeploymentExportResult> exportTemplateAsync();

    /**
     * Exports a deployment template asynchronously.
     *
     * @param callback the callback to call on success or failure with export result as parameter
     * @return a handle to cancel the request
     */
    @Method
    @Beta
    ServiceFuture<DeploymentExportResult> exportTemplateAsync(ServiceCallback<DeploymentExportResult> callback);

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
             * @param name the name of the new group
             * @param region the region to create the resource group in
             * @return the next stage of the definition
             */
            WithTemplate withNewResourceGroup(String name, Region region);

            /**
             * Creates a new resource group to put the resource in, based on the definition specified.
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
            @Method
            Deployment beginCreate();
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
}
