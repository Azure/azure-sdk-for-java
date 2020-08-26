// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.implementation.annotation.Beta;
import com.azure.resourcemanager.appplatform.fluent.inner.AppResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/** An immutable client-side representation of an Azure Spring App. */
@Fluent
@Beta
public interface SpringApp
    extends ExternalChildResource<SpringApp, SpringService>,
        HasInner<AppResourceInner>,
        Updatable<SpringApp.Update> {
    /** @return whether the app exposes public endpoint */
    boolean isPublic();

    /** @return whether only https is allowed for the app */
    boolean isHttpsOnly();

    /** @return the url of the app */
    String url();

    /** @return the fully qualified domain name (FQDN) of the app */
    String fqdn();

    /** @return the temporary disk of the app */
    TemporaryDisk temporaryDisk();

    /** @return the persistent disk of the app */
    PersistentDisk persistentDisk();

    /** @return the identity property of the app */
    ManagedIdentityProperties identity();

    /** @return the creation time of the app */
    OffsetDateTime createdTime();

    /** @return the active deployment name */
    String activeDeploymentName();

    /** @return the active deployment */
    SpringAppDeployment getActiveDeployment();

    /** @return the active deployment */
    Mono<SpringAppDeployment> getActiveDeploymentAsync();

    /**
     * @param <T> derived type of {@link SpringAppDeployment.DefinitionStages.WithCreate}
     * @return the entry point of the spring app deployment
     */
    <T extends SpringAppDeployment.DefinitionStages.WithCreate<T>> SpringAppDeployments<T> deployments();

    /** @return the entry point of the spring app service binding */
    SpringAppServiceBindings serviceBindings();

    /** @return the entry point of the spring app custom domain */
    SpringAppDomains customDomains();

    /** @return the blob url to upload deployment */
    Mono<ResourceUploadDefinition> getResourceUploadUrlAsync();

    /** @return the blob url to upload deployment */
    ResourceUploadDefinition getResourceUploadUrl();

    /** Container interface for all the definitions that need to be implemented. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithCreate { }

    /** Grouping of all the spring app definition stages. */
    interface DefinitionStages {
        /** The first stage of the spring app definition. */
        interface Blank extends WithDeployment { }

        /**
         * The stage of a spring app definition allowing to specify an active deployment.
         */
        interface WithDeployment {
            /**
             * Deploys a default package for the spring app with default scale.
             * @return the next stage of spring app definition
             */
            WithCreate withDefaultActiveDeployment();

            /**
             * Starts the definition of the active deployment for the spring app.
             * @param name the name of the deployment
             * @param <T> derived type of {@link SpringAppDeployment.DefinitionStages.WithAttach}
             * @return the first stage of spring app deployment definition
             */
            <T extends SpringAppDeployment.DefinitionStages.WithAttach
                <? extends SpringApp.DefinitionStages.WithCreate, T>>
                SpringAppDeployment.DefinitionStages.Blank<T> defineActiveDeployment(String name);
        }

        /** The stage of a spring app definition allowing to specify the endpoint. */
        interface WithEndpoint {
            /**
             * Enables the default public endpoint for the spring app.
             * @return the next stage of spring app definition
             */
            WithCreate withDefaultPublicEndpoint();

            /**
             * Specifies the custom domain for the spring app.
             * @param domain the domain name
             * @return the next stage of spring app definition
             */
            WithCreate withCustomDomain(String domain);

            /**
             * Specifies the custom domain for the spring app.
             * @param domain the domain name
             * @param certThumbprint the thumbprint of certificate for https
             * @return the next stage of spring app update
             */
            WithCreate withCustomDomain(String domain, String certThumbprint);

            /**
             * Enables https only for the spring app.
             * @return the next stage of spring app definition
             */
            WithCreate withHttpsOnly();
        }

        /** The stage of a spring app definition allowing to specify the disk. */
        interface WithDisk {
            /**
             * Specifies the temporary disk for the spring app.
             * @param sizeInGB the size of the disk
             * @param mountPath the mount path of the disk
             * @return the next stage of spring app definition
             */
            WithCreate withTemporaryDisk(int sizeInGB, String mountPath);

            /**
             * Specifies the persistent disk for the spring app.
             * @param sizeInGB the size of the disk
             * @param mountPath the mount path of the disk
             * @return the next stage of spring app definition
             */
            WithCreate withPersistentDisk(int sizeInGB, String mountPath);
        }

        /** The stage of a spring app update allowing to specify the service binding. */
        interface WithServiceBinding {
            /**
             * Specifies a service binding for the spring app.
             * @param name the service binding name
             * @param bindingProperties the property for the service binding
             * @return the next stage of spring app update
             */
            WithCreate withServiceBinding(String name, BindingResourceProperties bindingProperties);

            /**
             * Removes a service binding for the spring app.
             * @param name the service binding name
             * @return the next stage of spring app update
             */
            WithCreate withoutServiceBinding(String name);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created,
         * but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<SpringApp>,
                DefinitionStages.WithEndpoint,
                DefinitionStages.WithDisk,
                DefinitionStages.WithDeployment,
                DefinitionStages.WithServiceBinding { }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<SpringApp>,
        UpdateStages.WithEndpoint,
        UpdateStages.WithDisk,
        UpdateStages.WithDeployment,
        UpdateStages.WithServiceBinding { }

    /** Grouping of spring app update stages. */
    interface UpdateStages {
        /** The stage of a spring app update allowing to specify the endpoint. */
        interface WithEndpoint {
            /**
             * Enables the default public endpoint for the spring app.
             * @return the next stage of spring app update
             */
            Update withDefaultPublicEndpoint();

            /**
             * Disables the default public endpoint for the spring app.
             * @return the next stage of spring app update
             */
            Update withoutDefaultPublicEndpoint();

            /**
             * Specifies the custom domain for the spring app.
             * @param domain the domain name
             * @return the next stage of spring app update
             */
            Update withCustomDomain(String domain);

            /**
             * Specifies the custom domain for the spring app.
             * @param domain the domain name
             * @param certThumbprint the thumbprint of certificate for https
             * @return the next stage of spring app update
             */
            Update withCustomDomain(String domain, String certThumbprint);

            /**
             * Removes the custom domain for the spring app.
             * @param domain the domain name
             * @return the next stage of spring app update
             */
            Update withoutCustomDomain(String domain);

            /**
             * Enables https only for the spring app.
             * @return the next stage of spring app update
             */
            Update withHttpsOnly();

            /**
             * Disables https only for the spring app.
             * @return the next stage of spring app update
             */
            Update withoutHttpsOnly();
        }

        /** The stage of a spring app update allowing to specify the disk. */
        interface WithDisk {
            /**
             * Specifies the temporary disk for the spring app.
             * @param sizeInGB the size of the disk
             * @param mountPath the mount path of the disk
             * @return the next stage of spring app update
             */
            Update withTemporaryDisk(int sizeInGB, String mountPath);

            /**
             * Specifies the persistent disk for the spring app.
             * @param sizeInGB the size of the disk
             * @param mountPath the mount path of the disk
             * @return the next stage of spring app update
             */
            Update withPersistentDisk(int sizeInGB, String mountPath);
        }

        /**
         * The stage of a spring app update allowing to specify an simple active deployment.
         * for more operations, use {@link #deployments()}
         */
        interface WithDeployment {
            /**
             * Specifies active deployment for the spring app.
             * @param name the name of the deployment
             * @return the next stage of spring app update
             */
            Update withActiveDeployment(String name);
        }

        /** The stage of a spring app update allowing to specify the service binding. */
        interface WithServiceBinding {
            /**
             * Specifies a service binding for the spring app.
             * @param name the service binding name
             * @param bindingProperties the property for the service binding
             * @return the next stage of spring app update
             */
            Update withServiceBinding(String name, BindingResourceProperties bindingProperties);

            /**
             * Removes a service binding for the spring app.
             * @param name the service binding name
             * @return the next stage of spring app update
             */
            Update withoutServiceBinding(String name);
        }
    }
}
