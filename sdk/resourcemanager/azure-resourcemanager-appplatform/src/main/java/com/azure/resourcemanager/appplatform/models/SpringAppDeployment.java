// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appplatform.fluent.models.DeploymentResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.List;

/** An immutable client-side representation of an Azure Spring App Deployment. */
@Fluent
public interface SpringAppDeployment extends ExternalChildResource<SpringAppDeployment, SpringApp>,
    HasInnerModel<DeploymentResourceInner>, Updatable<SpringAppDeployment.Update> {
    /**
     * Gets the app name of the deployment.
     *
     * @return the app name of the deployment
     */
    String appName();

    /**
     * Gets the deploy settings of the deployment.
     *
     * @return the deploy settings of the deployment
     */
    DeploymentSettings settings();

    /**
     * Gets the status of the deployment.
     *
     * @return the status of the deployment
     */
    DeploymentResourceStatus status();

    /**
     * Check whether the deployment is active.
     *
     * @return whether the deployment is active
     */
    boolean isActive();

    /**
     * Gets all the instances of the deployment.
     *
     * @return all the instances of the deployment
     */
    List<DeploymentInstance> instances();

    /** Starts the deployment. */
    void start();

    /**
     * Starts the deployment.
     * @return null
     */
    Mono<Void> startAsync();

    /** Stops the deployment. */
    void stop();

    /**
     * Stops the deployment.
     * @return null
     */
    Mono<Void> stopAsync();

    /** Restarts the deployment. */
    void restart();

    /**
     * Restarts the deployment.
     * @return null
     */
    Mono<Void> restartAsync();

    /**
     * Gets the log file url of the deployment.
     *
     * @return the log file url of the deployment
     */
    String getLogFileUrl();

    /**
     * Gets the log file url of the deployment.
     *
     * @return the log file url of the deployment
     */
    Mono<String> getLogFileUrlAsync();

    /**
     * Gets config file patterns.
     *
     * @return (Enterprise Tier Only) config file patterns
     */
    List<String> configFilePatterns();

    /**
     * Gets CPU count.
     *
     * @return CPU count, can be 0.5, 1, 2, etc
     */
    Double cpu();

    /**
     * Gets memory in GB.
     *
     * @return memory in GB, can be 0.5, 1, 2, etc */
    Double memoryInGB();

    /**
     * Gets runtimeVersion of the deployment.
     *
     * @return RuntimeVersion of the deployment, only support Basic/Standard Tier, null for Enterprise Tier
     */
    RuntimeVersion runtimeVersion();

    /**
     * Gets JVM options of the deployment.
     *
     * @return JVM options of the deployment
     */
    String jvmOptions();

    /**
     * Container interface for all the definitions that need to be implemented.
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     * @param <T> The return type of final stage,
     *            usually {@link DefinitionStages.WithCreate} or {@link DefinitionStages.WithAttach}
     */
    interface Definition<ParentT, T> extends DefinitionStages.Blank<T>, DefinitionStages.WithSource<T>,
        DefinitionStages.WithModule<T>, DefinitionStages.WithCreate<T>, DefinitionStages.WithAttach<ParentT, T> {
    }

    /** Grouping of all the deployment definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of the deployment definition.
         * @param <T> The return type of final stage
         */
        interface Blank<T> extends WithSource<T> {
        }

        /**
         * The stage of a deployment definition allowing to specify the source code or package.
         * @param <T> The return type of final stage
         */
        interface WithSource<T> {
            /**
             * Specifies the jar package for the deployment.
             * @param jar the file of the jar
             * @return the next stage of deployment definition
             */
            T withJarFile(File jar);

            /**
             * (Enterprise Tier Only)
             * Specifies the jar package for the deployment.
             * @param jar the file of the jar
             * @param configFilePatterns config file patterns to decide which patterns of Application Configuration Service will be used
             *                           (App has to have a binding to the Configuration Service first in order to read the config files
             *                           {@link com.azure.resourcemanager.appplatform.models.SpringApp.DefinitionStages.WithConfigurationServiceBinding}),
             *                           use null or empty list to clear existing configurations
             * @return the next stage of deployment definition
             */
            T withJarFile(File jar, List<String> configFilePatterns);

            // Remove compression first due to tar.gz needs extern dependency
            // /**
            //  * Specifies the source code for the deployment.
            //  * @param sourceCodeFolder the folder of the source code
            //  * @return the next stage of deployment definition
            //  */
            // WithModule withSourceCodeFolder(File sourceCodeFolder);

            /**
             * Specifies the source code for the deployment.
             * @param sourceCodeTarGz a tar.gz file of the source code
             * @return the next stage of deployment definition
             */
            WithModule<T> withSourceCodeTarGzFile(File sourceCodeTarGz);

            /**
             * (Enterprise Tier Only)
             * Specifies the source code for the deployment.
             * @param sourceCodeTarGz a tar.gz file of the source code
             * @param configFilePatterns config file patterns to decide which patterns of Application Configuration Service will be used
             *                           (App has to have a binding to the Configuration Service first in order to read the config files
             *                           {@link com.azure.resourcemanager.appplatform.models.SpringApp.DefinitionStages.WithConfigurationServiceBinding}),
             *                           use null or empty list to clear existing configurations.
             * @return the next stage of deployment definition
             */
            WithModule<T> withSourceCodeTarGzFile(File sourceCodeTarGz, List<String> configFilePatterns);

            /**
             * Specifies the a existing source in the cloud storage.
             * @param type the source type in previous upload
             * @param relativePath the relative path gotten from getResourceUploadUrl
             * @return the next stage of deployment definition
             */
            T withExistingSource(UserSourceType type, String relativePath);
        }

        /**
         * The stage of a deployment definition allowing to specify the module of the source code.
         * @param <T> The return type of final stage
         */
        interface WithModule<T> {
            /**
             * Specifies the module of the source code.
             * @param moduleName the target module of the multi-module source code
             * @return the next stage of deployment definition
             */
            T withTargetModule(String moduleName);

            /**
             * Specifies the only module of the source code.
             * @return the next stage of deployment definition
             */
            T withSingleModule();
        }

        /**
         * The stage of a deployment definition allowing to specify deployment settings.
         * @param <T> The return type of final stage
         */
        interface WithSettings<T> {
            /**
             * Specifies the instance number of the deployment.
             * @param count the number of the instance
             * @return the next stage of deployment definition
             */
            T withInstance(int count);

            /**
             * Specifies the cpu number of the deployment.
             * @param cpuCount the number of the cpu
             * @return the next stage of deployment definition
             */
            T withCpu(int cpuCount);

            /**
             * Specifies the cpu number of the deployment.
             * @param cpuCount the number of the cpu, can be 0.5, 1, 2, etc
             * @return the next stage of deployment definition
             */
            T withCpu(double cpuCount);

            /**
             * Specifies the memory of the deployment.
             * @param sizeInGB the size of the memory in GB
             * @return the next stage of deployment definition
             */
            T withMemory(int sizeInGB);

            /**
             * Specifies the memory of the deployment.
             * @param sizeInGB the size of the memory in GB, can be 0.5, 1, 2, etc
             * @return the next stage of deployment definition
             */
            T withMemory(double sizeInGB);

            /**
             * Specifies the runtime version of the deployment.
             * @param version the runtime version of Java
             * @return the next stage of deployment definition
             */
            T withRuntime(RuntimeVersion version);

            /**
             * Specifies the jvm options of the deployment.
             * @param jvmOptions the argument of jvm
             * @return the next stage of deployment definition
             */
            T withJvmOptions(String jvmOptions);

            /**
             * Specifies a environment variable of the deployment.
             * @param key the key of the environment
             * @param value the value of the environment
             * @return the next stage of deployment definition
             */
            T withEnvironment(String key, String value);

            /**
             * Specifies the version of the deployment.
             * @param versionName the version name of the deployment
             * @return the next stage of deployment definition
             */
            T withVersionName(String versionName);

            /**
             * Activates of the deployment after definition.
             * @return the next stage of deployment definition
             */
            T withActivation();

            /**
             * Specifies the config file patterns for the deployment.
             * @param configFilePatterns Config file patterns to decide which patterns of Application Configuration Service will be used.
             *                           Use null or empty list to clear existing configurations.
             * @return the next stage of deployment definition
             */
            T withConfigFilePatterns(List<String> configFilePatterns);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created,
         * but also allows for any other optional settings to be specified.
         * @param <T> The return type of final stage
         */
        interface Final<T> extends WithSettings<T> {
        }

        /**
         * The final stage of the definition allowing to create a deployment
         * @param <T> The return type of final stage
         */
        interface WithCreate<T> extends Creatable<SpringAppDeployment>, Final<T> {
        }

        /**
         * The final stage of the definition allowing to attach a deployment to its parent
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         * @param <T> The return type of final stage
         */
        interface WithAttach<ParentT, T> extends Attachable<ParentT>, Final<T> {
        }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update extends Appliable<SpringAppDeployment>, UpdateStages.WithSource, UpdateStages.WithModule,
        UpdateStages.WithSettings {
    }

    /** Grouping of deployment update stages. */
    interface UpdateStages {
        /** The stage of a deployment update allowing to specify deployment settings. */
        interface WithSettings {
            /**
             * Specifies the instance number of the deployment.
             * @param count the number of the instance
             * @return the next stage of deployment definition
             */
            Update withInstance(int count);

            /**
             * Specifies the cpu number of the deployment.
             * @param cpuCount the number of the cpu
             * @return the next stage of deployment update
             */
            Update withCpu(int cpuCount);

            /**
             * Specifies the cpu number of the deployment.
             * @param cpuCount the number of the cpu, can be 0.5, 1, 2, etc
             * @return the next stage of deployment update
             */
            Update withCpu(double cpuCount);

            /**
             * Specifies the memory of the deployment.
             * @param sizeInGB the size of the memory in GB
             * @return the next stage of deployment update
             */
            Update withMemory(int sizeInGB);

            /**
             * Specifies the memory of the deployment.
             * @param sizeInGB the size of the memory, can be 0.5, 1, 2, etc
             * @return the next stage of deployment update
             */
            Update withMemory(double sizeInGB);

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

            /**
             * Specifies the version of the deployment.
             * @param versionName the version name of the deployment
             * @return the next stage of deployment update
             */
            Update withVersionName(String versionName);

            /**
             * Activates of the deployment after update.
             * @return the next stage of deployment update
             */
            Update withActivation();

            /**
             * Specifies the config file patterns for the deployment.
             * @param configFilePatterns Config file patterns to decide which patterns of Application Configuration Service will be used.
             *                           Use null or empty list to clear existing configurations.
             * @return the next stage of deployment update
             */
            Update withConfigFilePatterns(List<String> configFilePatterns);
        }

        /** The stage of a deployment update allowing to specify the source code or package. */
        interface WithSource {
            /**
             * Specifies the jar package for the deployment.
             * @param jar the file of the jar
             * @return the next stage of deployment update
             */
            Update withJarFile(File jar);

            /**
             * (Enterprise Tier Only)
             * Specifies the jar package for the deployment.
             * @param jar the file of the jar
             * @param configFilePatterns config file patterns to decide which patterns of Application Configuration Service will be used
             *                           (App has to have a binding to the Configuration Service first in order to read the config files
             *                            {@link com.azure.resourcemanager.appplatform.models.SpringApp.DefinitionStages.WithConfigurationServiceBinding})
             *                           use null or empty list to clear existing configurations
             * @return the next stage of deployment update
             */
            Update withJarFile(File jar, List<String> configFilePatterns);

            // /**
            //  * Specifies the source code for the deployment.
            //  * @param sourceCodeFolder the folder of the source code
            //  * @return the next stage of deployment update
            //  */
            // WithModule withSourceCodeFolder(File sourceCodeFolder);

            /**
             * Specifies the source code for the deployment.
             * @param sourceCodeTarGz a tar.gz file of the source code
             * @return the next stage of deployment update
             */
            WithModule withSourceCodeTarGzFile(File sourceCodeTarGz);

            /**
             * (Enterprise Tier Only)
             * Specifies the source code for the deployment.
             * @param sourceCodeTarGz a tar.gz file of the source code
             * @param configFilePatterns config file patterns to decide which patterns of Application Configuration Service will be used
             *                           (App has to have a binding to the Configuration Service first in order to read the config files
             *                            {@link com.azure.resourcemanager.appplatform.models.SpringApp.DefinitionStages.WithConfigurationServiceBinding})
             *                           use null or empty list to clear existing configurations.
             * @return the next stage of deployment update
             */
            WithModule withSourceCodeTarGzFile(File sourceCodeTarGz, List<String> configFilePatterns);

            /**
             * Specifies the a existing source in the cloud storage.
             * @param type the source type in previous upload
             * @param relativePath the relative path gotten from getResourceUploadUrl
             * @return the next stage of deployment update
             */
            Update withExistingSource(UserSourceType type, String relativePath);
        }

        /** The stage of a deployment update allowing to specify the module of the source code. */
        interface WithModule {
            /**
             * Specifies the module of the source code.
             * @param moduleName the target module of the multi-module source code
             * @return the next stage of deployment update
             */
            Update withTargetModule(String moduleName);

            /**
             * Specifies the only module of the source code.
             * @return the next stage of deployment update
             */
            Update withSingleModule();
        }
    }
}
