// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.implementation.annotation.Beta;
import com.azure.resourcemanager.appplatform.fluent.inner.DeploymentResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.List;

/** An immutable client-side representation of an Azure deployment Deployment request. */
@Fluent
@Beta
public interface SpringAppDeployment
    extends ExternalChildResource<SpringAppDeployment, SpringApp>,
        HasInner<DeploymentResourceInner>,
        Updatable<SpringAppDeployment.Update> {
    /** @return the app name of the deployment */
    String appName();

    /** @return the deploy settings of the deployment */
    DeploymentSettings settings();

    /** @return the status of the deployment */
    DeploymentResourceStatus status();

    /** @return whether the deployment is active */
    boolean isActive();

    /** @return the creation time of the deployment */
    OffsetDateTime createdTime();

    /** @return all the instances of the deployment */
    List<DeploymentInstance> instances();

    /** @return the log file url of the deployment */
    String getLogFileUrl();

    /** @return the log file url of the deployment */
    Mono<String> getLogFileUrlAsync();

    /** Container interface for all the definitions that need to be implemented. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithSource,
            DefinitionStages.WithModule,
            DefinitionStages.WithPredefinedSettings,
            DefinitionStages.WithSettingsAndCreate { }

    /** Grouping of all the deployment definition stages. */
    interface DefinitionStages {
        /** The first stage of the deployment definition. */
        interface Blank extends WithSource { }

        /** The stage of a deployment definition allowing to specify the source code or package. */
        interface WithSource {
            /**
             * Specifies the jar package for the deployment.
             * @param jar the file of the jar
             * @return the next stage of deployment definition
             */
            WithPredefinedSettings withJarPath(File jar);

            /**
             * Specifies the jar package for the deployment.
             * @param jar the content of the jar
             * @return the next stage of deployment definition
             */
            WithPredefinedSettings withJarFile(byte[] jar);

            /**
             * Specifies the source code for the deployment.
             * @param sourceCode the folder of the source code
             * @return the next stage of deployment definition
             */
            WithModule withSourceCodeFolder(File sourceCode);

            /**
             * Specifies the source code for the deployment.
             * @param sourceCodeTarGz a tar.gz file of the source code
             * @return the next stage of deployment definition
             */
            WithModule withSourceCodeTarGzFile(File sourceCodeTarGz);

            /**
             * Specifies the a existing source in the cloud storage.
             * @param type the source type in previous upload
             * @param relativePath the relative path gotten from getResourceUploadUrl
             * @return the next stage of deployment definition
             */
            WithPredefinedSettings withExistingSource(UserSourceType type, String relativePath);
        }

        /** The stage of a deployment definition allowing to specify the module of the source code. */
        interface WithModule {
            /**
             * Specifies the module of the source code.
             * @param moduleName the target module of the multi-module source code
             * @return the next stage of deployment definition
             */
            WithPredefinedSettings withTargetModule(String moduleName);

            /**
             * Specifies the only module of the source code.
             * @return the next stage of deployment definition
             */
            WithPredefinedSettings withSingleModule();
        }

        /** The stage of a deployment definition allowing to specify predefined settings. */
        interface WithPredefinedSettings {
            /**
             * Specifies the settings from the app active deployment.
             * @return the next stage of deployment definition
             */
            WithCreate withSettingsFromActiveDeployment();

            /**
             * Specifies the settings from another deployment.
             * @param deployment the deployment object
             * @return the next stage of deployment definition
             */
            WithCreate withSettingsFromDeployment(SpringAppDeployment deployment);

            /**
             * Specifies the settings from another deployment.
             * @param deploymentName the name of the deployment
             * @return the next stage of deployment definition
             */
            WithCreate withSettingsFromDeployment(String deploymentName);

            /**
             * Customizes settings of the deployment.
             * @return the next stage of deployment definition
             */
            WithSettingsAndCreate withCustomSetting();
        }

        /** The stage of a deployment definition allowing to specify deployment settings. */
        interface WithSettings {
            /**
             * Specifies the instance number of the deployment.
             * @param count the number of the instance
             * @return the next stage of deployment definition
             */
            WithSettingsAndCreate withInstance(int count);

            /**
             * Specifies the cpu number of the deployment.
             * @param cpuCount the number of the cpu
             * @return the next stage of deployment definition
             */
            WithSettingsAndCreate withCpu(int cpuCount);

            /**
             * Specifies the memory of the deployment.
             * @param sizeInGB the size of the memory in GB
             * @return the next stage of deployment definition
             */
            WithSettingsAndCreate withMemory(int sizeInGB);

            /**
             * Specifies the runtime version of the deployment.
             * @param version the runtime version of Java
             * @return the next stage of deployment definition
             */
            WithSettingsAndCreate withRuntime(RuntimeVersion version);

            /**
             * Specifies the jvm options of the deployment.
             * @param jvmOptions the argument of jvm
             * @return the next stage of deployment definition
             */
            WithSettingsAndCreate withJvmOptions(String jvmOptions);

            /**
             * Specifies a environment variable of the deployment.
             * @param key the key of the environment
             * @param value the value of the environment
             * @return the next stage of deployment definition
             */
            WithSettingsAndCreate withEnvironment(String key, String value);

            /**
             * Specifies the version of the deployment.
             * @param versionName the version name of the deployment
             * @return the next stage of deployment definition
             */
            WithSettingsAndCreate withVersionName(String versionName);

            /**
             * Activates of the deployment after definition.
             * @return the next stage of deployment definition
             */
            WithSettingsAndCreate activate();
        }

        interface WithBaseSettings {
            /**
             * Specifies the version of the deployment.
             * @param versionName the version name of the deployment
             * @return the next stage of deployment definition
             */
            WithCreate withVersionName(String versionName);


            /**
             * Activates of the deployment after definition.
             * @return the next stage of deployment definition
             */
            WithCreate activate();
        }

        /**
         * The stage of the definition which contains all required inputs for the resource to be created.
         */
        interface WithCreate
            extends Creatable<SpringAppDeployment>,
                WithBaseSettings { }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created,
         * but also allows for any other optional settings to be specified.
         */
        interface WithSettingsAndCreate
            extends WithCreate,
                WithSettings { }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<SpringAppDeployment>,
            UpdateStages.WithSettings { }

    /** Grouping of deployment update stages. */
    interface UpdateStages {
        /** The stage of a deployment update allowing to specify deployment settings. */
        interface WithSettings {
            /**
             * Specifies the instance number of the deployment.
             * @param count the number of the instance
             * @return the next stage of deployment update
             */
            Update withInstance(int count);

            /**
             * Specifies the cpu number of the deployment.
             * @param cpuCount the number of the cpu
             * @return the next stage of deployment update
             */
            Update withCpu(int cpuCount);

            /**
             * Specifies the memory of the deployment.
             * @param sizeInGB the size of the memory in GB
             * @return the next stage of deployment update
             */
            Update withMemory(int sizeInGB);

            /**
             * Specifies the runtime version of the deployment.
             * @param version the runtime version of Java
             * @return the next stage of deployment update
             */
            Update withRuntime(RuntimeVersion version);

            /**
             * Specifies the jvm options of the deployment.
             * @param jvmOptions the argument of jvm
             * @return the next stage of deployment update
             */
            Update withJvmOptions(String jvmOptions);

            /**
             * Specifies a environment variable of the deployment.
             * @param key the key of the environment
             * @param value the value of the environment
             * @return the next stage of deployment update
             */
            Update withEnvironment(String key, String value);

            /**
             * Removes a environment variable of the deployment.
             * @param key the key of the environment
             * @return the next stage of deployment update
             */
            Update withoutEnvironment(String key);
        }
    }
}
