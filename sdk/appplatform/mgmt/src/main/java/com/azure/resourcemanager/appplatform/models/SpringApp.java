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

import java.io.File;
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

    /** @return the temporary disk of the app */
    TemporaryDisk temporaryDisk();

    /** @return the persistent disk of the app */
    PersistentDisk persistentDisk();

    /** @return the identity property of the app */
    ManagedIdentityProperties identity();

    /** @return the creation time of the app */
    OffsetDateTime createdTime();

    /** @return the active deployment name */
    String activeDeployment();

    /** @return the entry point of the spring app deployment */
    SpringAppDeployments deployments();

    /** @return the blob url to upload deployment */
    Mono<ResourceUploadDefinition> getResourceUploadUrlAsync();

    /** @return the blob url to upload deployment */
    ResourceUploadDefinition getResourceUploadUrl();

    /** Container interface for all the definitions that need to be implemented. */
    interface Definition
        extends DefinitionStages.Blank { }

    /** Grouping of all the spring app definition stages. */
    interface DefinitionStages {
        /** The first stage of the spring app definition. */
        interface Blank extends WithCreate { }

        /** The stage of a spring app definition allowing to specify the endpoint. */
        interface WithEndpoint {
            /**
             * Enables the public endpoint for the spring app.
             * @return the next stage of spring app definition
             */
            WithCreate withPublicEndpoint();

            /**
             * Specifies the custom domain for the spring app.
             * @param domain the domain name
             * @return the next stage of spring app definition
             */
            WithCreate withCustomDomain(String domain);

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

        /**
         * The stage of a spring app definition allowing to specify an simple active deployment.
         * for more operations, use {@link #deployments()}
         */
        interface WithDeployment {
            /**
             * Deploys the jar package for the spring app with default scale.
             * @param name the name of the deployment
             * @param jarFile the file of the jar
             * @return the next stage of spring app definition
             */
            WithCreate deployJar(String name, File jarFile);

            /**
             * Deploys the source code for the spring app with default scale.
             * @param name the name of the deployment
             * @param sourceCodeFolder the source code folder
             * @param targetModule the target module of the source code
             * @return the next stage of spring app definition
             */
            WithCreate deploySource(String name, File sourceCodeFolder, String targetModule);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created,
         * but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<SpringApp>,
                DefinitionStages.WithEndpoint,
                DefinitionStages.WithDisk,
                DefinitionStages.WithDeployment { }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<SpringApp>,
        UpdateStages.WithEndpoint,
        UpdateStages.WithDisk,
        UpdateStages.WithDeployment { }

    /** Grouping of spring app update stages. */
    interface UpdateStages {
        /** The stage of a spring app update allowing to specify the endpoint. */
        interface WithEndpoint {
            /**
             * Enables the public endpoint for the spring app.
             * @return the next stage of spring app update
             */
            Update withPublicEndpoint();

            /**
             * Disables the public endpoint for the spring app.
             * @return the next stage of spring app update
             */
            Update withoutPublicEndpoint();

            /**
             * Specifies the custom domain for the spring app.
             * @param domain the domain name
             * @return the next stage of spring app update
             */
            Update withCustomDomain(String domain);

            /**
             * Removes the custom domain for the spring app.
             * @return the next stage of spring app update
             */
            Update withoutCustomDomain();

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
             * Removes the temporary disk for the spring app.
             * @return the next stage of spring app update
             */
            Update withoutTemporaryDisk();

            /**
             * Specifies the persistent disk for the spring app.
             * @param sizeInGB the size of the disk
             * @param mountPath the mount path of the disk
             * @return the next stage of spring app update
             */
            Update withPersistentDisk(int sizeInGB, String mountPath);

            /**
             * Removes the persistent disk for the spring app.
             * @return the next stage of spring app update
             */
            Update withoutPersistentDisk();
        }


        /**
         * The stage of a spring app update allowing to specify an simple active deployment.
         * for more operations, use {@link #deployments()}
         */
        interface WithDeployment {
            /**
             * Deploys the jar package for the spring app with default scale.
             * @param name the name of the deployment
             * @param jarFile the file of the jar
             * @return the next stage of spring app update
             */
            Update deployJar(String name, File jarFile);

            /**
             * Deploys the source code for the spring app with default scale.
             * @param name the name of the deployment
             * @param sourceCodeFolder the source code folder
             * @param targetModule the target module of the source code
             * @return the next stage of spring app update
             */
            Update deploySource(String name, File sourceCodeFolder, String targetModule);

            /**
             * Specifies active deployment for the spring app.
             * @param name the name of the deployment
             * @return the next stage of spring app update
             */
            Update withActiveDeployment(String name);

            /**
             * Removes a deployment for the spring app.
             * @param name the name of the deployment
             * @return the next stage of spring app update
             */
            Update withoutDeployment(String name);
        }
    }
}
