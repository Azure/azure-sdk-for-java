// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.io.File;
import java.io.InputStream;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure Web App. */
@Fluent
public interface WebApp extends WebAppBase, Refreshable<WebApp>, Updatable<WebApp.Update> {
    /** @return the entry point to deployment slot management API under the web app */
    DeploymentSlots deploymentSlots();

    /**
     * Deploys a WAR file onto the Azure specialized Tomcat on this web app.
     *
     * @param warFile the WAR file to upload
     */
    void warDeploy(File warFile);

    /**
     * Deploys a WAR file onto the Azure specialized Tomcat on this web app.
     *
     * @param warFile the WAR file to upload
     * @return a completable of the operation
     */
    Mono<Void> warDeployAsync(File warFile);

    /**
     * Deploys a WAR file onto the Azure specialized Tomcat on this web app.
     *
     * @param warFile the WAR file to upload
     */
    void warDeploy(InputStream warFile);

    /**
     * Deploys a WAR file onto the Azure specialized Tomcat on this web app.
     *
     * @param warFile the WAR file to upload
     * @return a completable of the operation
     */
    Mono<Void> warDeployAsync(InputStream warFile);

    /**
     * Deploys a WAR file onto the Azure specialized Tomcat on this web app.
     *
     * @param warFile the WAR file to upload
     * @param appName the name of the app, default to "ROOT" when not provided
     */
    void warDeploy(File warFile, String appName);

    /**
     * Deploys a WAR file onto the Azure specialized Tomcat on this web app.
     *
     * @param warFile the WAR file to upload
     * @param appName the name of the app, default to "ROOT" when not provided
     * @return a completable of the operation
     */
    Mono<Void> warDeployAsync(File warFile, String appName);

    /**
     * Deploys a WAR file onto the Azure specialized Tomcat on this web app.
     *
     * @param warFile the WAR file to upload
     * @param appName the name of the app, default to "ROOT" when not provided
     */
    void warDeploy(InputStream warFile, String appName);

    /**
     * Deploys a WAR file onto the Azure specialized Tomcat on this web app.
     *
     * @param warFile the WAR file to upload
     * @param appName the name of the app, default to "ROOT" when not provided
     * @return a completable of the operation
     */
    Mono<Void> warDeployAsync(InputStream warFile, String appName);

    /**************************************************************
     * Fluent interfaces to provision a Web App
     **************************************************************/

    /** Container interface for all the definitions that need to be implemented. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.NewAppServicePlanWithGroup,
            DefinitionStages.WithNewAppServicePlan,
            DefinitionStages.WithDockerContainerImage,
            DefinitionStages.WithCredentials,
            DefinitionStages.WithStartUpCommand,
            DefinitionStages.WithCreate {
    }

    /** Grouping of all the web app definition stages. */
    interface DefinitionStages {
        /** The first stage of the web app definition. */
        interface Blank extends DefinitionWithRegion<NewAppServicePlanWithGroup> {
            /**
             * Uses an existing app service plan for the web app.
             *
             * @param appServicePlan the existing app service plan
             * @return the next stage of the definition
             */
            ExistingWindowsPlanWithGroup withExistingWindowsPlan(AppServicePlan appServicePlan);

            /**
             * Uses an existing app service plan for the web app.
             *
             * @param appServicePlan the existing app service plan
             * @return the next stage of the definition
             */
            ExistingLinuxPlanWithGroup withExistingLinuxPlan(AppServicePlan appServicePlan);
        }

        /** A web app definition allowing resource group to be specified when an existing app service plan is used. */
        interface NewAppServicePlanWithGroup
            extends GroupableResource.DefinitionStages.WithGroup<WithNewAppServicePlan> {
        }

        /**
         * A web app definition allowing resource group to be specified when a new app service plan is to be created.
         */
        interface ExistingWindowsPlanWithGroup {
            /**
             * Associates the resource with an existing resource group.
             *
             * @param groupName the name of an existing resource group to put this resource in.
             * @return the next stage of the definition
             */
            WithWindowsRuntimeStack withExistingResourceGroup(String groupName);

            /**
             * Associates the resource with an existing resource group.
             *
             * @param group an existing resource group to put the resource in
             * @return the next stage of the definition
             */
            WithWindowsRuntimeStack withExistingResourceGroup(ResourceGroup group);

            /**
             * Creates a new resource group to put the resource in.
             *
             * <p>The group will be created in the same location as the resource.
             *
             * @param name the name of the new group
             * @return the next stage of the definition
             */
            WithWindowsRuntimeStack withNewResourceGroup(String name);

            /**
             * Creates a new resource group to put the resource in.
             *
             * <p>The group will be created in the same location as the resource. The group's name is automatically
             * derived from the resource's name.
             *
             * @return the next stage of the definition
             */
            WithWindowsRuntimeStack withNewResourceGroup();

            /**
             * Creates a new resource group to put the resource in, based on the definition specified.
             *
             * @param groupDefinition a creatable definition for a new resource group
             * @return the next stage of the definition
             */
            WithWindowsRuntimeStack withNewResourceGroup(Creatable<ResourceGroup> groupDefinition);
        }

        /**
         * A web app definition allowing resource group to be specified when a new app service plan is to be created.
         */
        interface ExistingLinuxPlanWithGroup {
            /**
             * Associates the resource with an existing resource group.
             *
             * @param groupName the name of an existing resource group to put this resource in.
             * @return the next stage of the definition
             */
            WithDockerContainerImage withExistingResourceGroup(String groupName);

            /**
             * Associates the resource with an existing resource group.
             *
             * @param group an existing resource group to put the resource in
             * @return the next stage of the definition
             */
            WithDockerContainerImage withExistingResourceGroup(ResourceGroup group);

            /**
             * Creates a new resource group to put the resource in.
             *
             * <p>The group will be created in the same location as the resource.
             *
             * @param name the name of the new group
             * @return the next stage of the definition
             */
            WithDockerContainerImage withNewResourceGroup(String name);

            /**
             * Creates a new resource group to put the resource in.
             *
             * <p>The group will be created in the same location as the resource. The group's name is automatically
             * derived from the resource's name.
             *
             * @return the next stage of the definition
             */
            WithDockerContainerImage withNewResourceGroup();

            /**
             * Creates a new resource group to put the resource in, based on the definition specified.
             *
             * @param groupDefinition a creatable definition for a new resource group
             * @return the next stage of the definition
             */
            WithDockerContainerImage withNewResourceGroup(Creatable<ResourceGroup> groupDefinition);
        }

        /** A web app definition allowing app service plan to be set. */
        interface WithNewAppServicePlan {
            /**
             * Creates a new free app service plan. This will fail if there are 10 or more free plans in the current
             * subscription.
             *
             * @return the next stage of the definition
             */
            WithWindowsRuntimeStack withNewFreeAppServicePlan();

            /**
             * Creates a new shared app service plan.
             *
             * @return the next stage of the definition
             */
            WithWindowsRuntimeStack withNewSharedAppServicePlan();

            /**
             * Creates a new app service plan to use.
             *
             * @param pricingTier the sku of the app service plan
             * @return the next stage of the definition
             */
            WithWindowsRuntimeStack withNewWindowsPlan(PricingTier pricingTier);

            /**
             * Creates a new app service plan to use.
             *
             * @param appServicePlanName the name of the new app service plan
             * @param pricingTier the sku of the app service plan
             * @return the next stage of the definition
             */
            WithWindowsRuntimeStack withNewWindowsPlan(String appServicePlanName, PricingTier pricingTier);

            /**
             * Creates a new app service plan to use.
             *
             * @param appServicePlanCreatable the new app service plan creatable
             * @return the next stage of the definition
             */
            WithWindowsRuntimeStack withNewWindowsPlan(Creatable<AppServicePlan> appServicePlanCreatable);

            /**
             * Creates a new app service plan to use.
             *
             * @param pricingTier the sku of the app service plan
             * @return the next stage of the definition
             */
            WithDockerContainerImage withNewLinuxPlan(PricingTier pricingTier);

            /**
             * Creates a new app service plan to use.
             *
             * @param appServicePlanName the name of the new app service plan
             * @param pricingTier the sku of the app service plan
             * @return the next stage of the definition
             */
            WithDockerContainerImage withNewLinuxPlan(String appServicePlanName, PricingTier pricingTier);

            /**
             * Creates a new app service plan to use.
             *
             * @param appServicePlanCreatable the new app service plan creatable
             * @return the next stage of the definition
             */
            WithDockerContainerImage withNewLinuxPlan(Creatable<AppServicePlan> appServicePlanCreatable);
        }

        /** A web app definition allowing docker image source to be specified. */
        interface WithDockerContainerImage {
            /**
             * Specifies the docker container image to be a built in one.
             *
             * @param runtimeStack the runtime stack installed on the image
             * @return the next stage of the definition
             */
            WithCreate withBuiltInImage(RuntimeStack runtimeStack);

            /**
             * Specifies the docker container image to be one from Docker Hub.
             *
             * @param imageAndTag image and optional tag (eg 'image:tag')
             * @return the next stage of the definition
             */
            WithStartUpCommand withPublicDockerHubImage(String imageAndTag);

            /**
             * Specifies the docker container image to be one from Docker Hub.
             *
             * @param imageAndTag image and optional tag (eg 'image:tag')
             * @return the next stage of the definition
             */
            WithCredentials withPrivateDockerHubImage(String imageAndTag);

            /**
             * Specifies the docker container image to be one from a private registry.
             *
             * @param imageAndTag image and optional tag (eg 'image:tag')
             * @param serverUrl the URL to the private registry server
             * @return the next stage of the definition
             */
            WithCredentials withPrivateRegistryImage(String imageAndTag, String serverUrl);
        }

        /** A web app definition allowing docker registry credentials to be set. */
        interface WithCredentials {
            /**
             * Specifies the username and password for Docker Hub or the docker registry.
             *
             * @param username the username for Docker Hub or the docker registry
             * @param password the password for Docker Hub or the docker registry
             * @return the next stage of the definition
             */
            WithStartUpCommand withCredentials(String username, String password);
        }

        /**
         * A web app definition allowing docker startup command to be specified. This will replace the "CMD" section in
         * the Dockerfile.
         */
        interface WithStartUpCommand extends WithCreate {
            /**
             * Specifies the startup command.
             *
             * @param startUpCommand startup command to replace "CMD" in Dockerfile
             * @return the next stage of the definition
             */
            WithCreate withStartUpCommand(String startUpCommand);
        }

        /** A web app definition allowing runtime stack on Windows operating system to be specified. */
        interface WithWindowsRuntimeStack extends WithCreate {
            /**
             * Specifies the runtime stack for the web app on Windows operating system.
             *
             * @param runtimeStack the runtime stack for web app
             * @return the next stage of the definition
             */
            WithCreate withRuntimeStack(WebAppRuntimeStack runtimeStack);
        }

        /**
         * A site definition with sufficient inputs to create a new web app / deployments slot in the cloud, but
         * exposing additional optional inputs to specify.
         */
        interface WithCreate extends Creatable<WebApp>, WebAppBase.DefinitionStages.WithCreate<WebApp> {
        }
    }

    /** Grouping of all the web app update stages. */
    interface UpdateStages {
        /** A web app update allowing app service plan to be set. */
        interface WithAppServicePlan {
            /**
             * Creates a new free app service plan. This will fail if there are 10 or more free plans in the current
             * subscription.
             *
             * @return the next stage of the web app update
             */
            Update withNewFreeAppServicePlan();

            /**
             * Creates a new shared app service plan.
             *
             * @return the next stage of the web app update
             */
            Update withNewSharedAppServicePlan();

            /**
             * Creates a new app service plan to use.
             *
             * @param pricingTier the sku of the app service plan
             * @return the next stage of the web app update
             */
            Update withNewAppServicePlan(PricingTier pricingTier);

            /**
             * Creates a new app service plan to use.
             *
             * @param appServicePlanName the name of the new app service plan
             * @param pricingTier the sku of the app service plan
             * @return the next stage of the web app update
             */
            Update withNewAppServicePlan(String appServicePlanName, PricingTier pricingTier);

            /**
             * Creates a new app service plan to use.
             *
             * @param appServicePlanCreatable the new app service plan creatable
             * @return the next stage of the web app update
             */
            Update withNewAppServicePlan(Creatable<AppServicePlan> appServicePlanCreatable);

            /**
             * Uses an existing app service plan for the web app.
             *
             * @param appServicePlan the existing app service plan
             * @return the next stage of the web app update
             */
            Update withExistingAppServicePlan(AppServicePlan appServicePlan);
        }

        /** A web app update allowing docker image source to be specified. */
        interface WithDockerContainerImage {
            /**
             * Specifies the docker container image to be a built in one.
             *
             * @param runtimeStack the runtime stack installed on the image
             * @return the next stage of the web app update
             */
            Update withBuiltInImage(RuntimeStack runtimeStack);

            /**
             * Specifies the docker container image to be one from Docker Hub.
             *
             * @param imageAndTag image and optional tag (eg 'image:tag')
             * @return the next stage of the web app update
             */
            WithStartUpCommand withPublicDockerHubImage(String imageAndTag);

            /**
             * Specifies the docker container image to be one from Docker Hub.
             *
             * @param imageAndTag image and optional tag (eg 'image:tag')
             * @return the next stage of the web app update
             */
            WithCredentials withPrivateDockerHubImage(String imageAndTag);

            /**
             * Specifies the docker container image to be one from a private registry.
             *
             * @param imageAndTag image and optional tag (eg 'image:tag')
             * @param serverUrl the URL to the private registry server
             * @return the next stage of the web app update
             */
            WithCredentials withPrivateRegistryImage(String imageAndTag, String serverUrl);
        }

        /** A web app update allowing docker hub credentials to be set. */
        interface WithCredentials {
            /**
             * Specifies the username and password for Docker Hub.
             *
             * @param username the username for Docker Hub
             * @param password the password for Docker Hub
             * @return the next stage of the web app update
             */
            WithStartUpCommand withCredentials(String username, String password);
        }

        /**
         * A web app update allowing docker startup command to be specified. This will replace the "CMD" section in the
         * Dockerfile.
         */
        interface WithStartUpCommand extends Update {
            /**
             * Specifies the startup command.
             *
             * @param startUpCommand startup command to replace "CMD" in Dockerfile
             * @return the next stage of the web app update
             */
            Update withStartUpCommand(String startUpCommand);
        }

        /** A web app update allowing runtime stack on Windows operating system to be specified. */
        interface WithWindowsRuntimeStack {
            /**
             * Specifies the runtime stack for the web app on Windows operating system.
             *
             * @param runtimeStack the runtime stack for web app
             * @return the next stage of the web app update
             */
            Update withRuntimeStack(WebAppRuntimeStack runtimeStack);
        }
    }

    /** The template for a web app update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<WebApp>,
            UpdateStages.WithAppServicePlan,
            UpdateStages.WithWindowsRuntimeStack,
            WebAppBase.Update<WebApp>,
            UpdateStages.WithDockerContainerImage {
    }
}
