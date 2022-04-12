// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.core.management.polling.PollResult;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.models.BuildInner;
import com.azure.resourcemanager.appplatform.fluent.models.BuildResultInner;
import com.azure.resourcemanager.appplatform.fluent.models.DeploymentResourceInner;
import com.azure.resourcemanager.appplatform.fluent.models.LogFileUrlResponseInner;
import com.azure.resourcemanager.appplatform.models.BuildProperties;
import com.azure.resourcemanager.appplatform.models.BuildResultProvisioningState;
import com.azure.resourcemanager.appplatform.models.BuildResultUserSourceInfo;
import com.azure.resourcemanager.appplatform.models.DeploymentInstance;
import com.azure.resourcemanager.appplatform.models.DeploymentResourceProperties;
import com.azure.resourcemanager.appplatform.models.DeploymentResourceStatus;
import com.azure.resourcemanager.appplatform.models.DeploymentSettings;
import com.azure.resourcemanager.appplatform.models.JarUploadedUserSourceInfo;
import com.azure.resourcemanager.appplatform.models.NetCoreZipUploadedUserSourceInfo;
import com.azure.resourcemanager.appplatform.models.ResourceRequests;
import com.azure.resourcemanager.appplatform.models.ResourceUploadDefinition;
import com.azure.resourcemanager.appplatform.models.RuntimeVersion;
import com.azure.resourcemanager.appplatform.models.Sku;
import com.azure.resourcemanager.appplatform.models.SourceUploadedUserSourceInfo;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringAppDeployment;
import com.azure.resourcemanager.appplatform.models.UploadedUserSourceInfo;
import com.azure.resourcemanager.appplatform.models.UserSourceInfo;
import com.azure.resourcemanager.appplatform.models.UserSourceType;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClientBuilder;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SpringAppDeploymentImpl
    extends ExternalChildResourceImpl<SpringAppDeployment, DeploymentResourceInner, SpringAppImpl, SpringApp>
    implements SpringAppDeployment,
        SpringAppDeployment.Definition<SpringAppImpl, SpringAppDeploymentImpl>,
        SpringAppDeployment.Update {

    private final ClientLogger logger = new ClientLogger(SpringAppDeploymentImpl.class);

    SpringAppDeploymentImpl(String name, SpringAppImpl parent, DeploymentResourceInner innerObject) {
        super(name, parent, innerObject);
    }

    @Override
    public String appName() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().name();
    }

    @Override
    public DeploymentSettings settings() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().deploymentSettings();
    }

    @Override
    public DeploymentResourceStatus status() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().status();
    }

    @Override
    public boolean isActive() {
        if (innerModel().properties() == null) {
            return false;
        }
        return innerModel().properties().active();
    }

    @Override
    public List<DeploymentInstance> instances() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().instances();
    }

    @Override
    public void start() {
        startAsync().block();
    }

    @Override
    public Mono<Void> startAsync() {
        return manager().serviceClient().getDeployments().startAsync(
            parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), name()
        );
    }

    @Override
    public void stop() {
        stopAsync().block();
    }

    @Override
    public Mono<Void> stopAsync() {
        return manager().serviceClient().getDeployments().stopAsync(
            parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), name()
        );
    }

    @Override
    public void restart() {
        restartAsync().block();
    }

    @Override
    public Mono<Void> restartAsync() {
        return manager().serviceClient().getDeployments().restartAsync(
            parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), name()
        );
    }

    @Override
    public String getLogFileUrl() {
        return getLogFileUrlAsync().block();
    }

    @Override
    public Mono<String> getLogFileUrlAsync() {
        return manager().serviceClient().getDeployments().getLogFileUrlAsync(
            parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), name()
        )
            .map(LogFileUrlResponseInner::url);
    }

    private void ensureDeploySettings() {
        if (innerModel().properties() == null) {
            innerModel().withProperties(new DeploymentResourceProperties());
        }
        if (innerModel().properties().deploymentSettings() == null) {
            innerModel().properties().withDeploymentSettings(new DeploymentSettings());
        }
        if (innerModel().properties().deploymentSettings().resourceRequests() == null) {
            innerModel().properties().deploymentSettings().withResourceRequests(new ResourceRequests());
        }
    }

    private void ensureSource() {
        ensureSource(null);
    }

    private void ensureSource(UserSourceType type) {
        if (innerModel().properties() == null) {
            innerModel().withProperties(new DeploymentResourceProperties());
        }
        if (innerModel().properties().source() == null) {
            if (type == UserSourceType.JAR) {
                innerModel().properties().withSource(new JarUploadedUserSourceInfo());
            } else if (type == UserSourceType.SOURCE) {
                innerModel().properties().withSource(new SourceUploadedUserSourceInfo());
            } else if (type == UserSourceType.NET_CORE_ZIP) {
                innerModel().properties().withSource(new NetCoreZipUploadedUserSourceInfo());
            } else if (type == UserSourceType.BUILD_RESULT) {
                innerModel().properties().withSource(new BuildResultUserSourceInfo());
            } else {
                innerModel().properties().withSource(new UserSourceInfo());
            }
        }
    }

    // private File compressSource(File sourceFolder) throws IOException {
    //     File compressFile = File.createTempFile("java_package", "tar.gz");
    //     compressFile.deleteOnExit();
    //     try (TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(
    //              new GZIPOutputStream(new FileOutputStream(compressFile)))) {
    //         tarArchiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

    //         for (Path sourceFile : Files.walk(sourceFolder.toPath()).collect(Collectors.toList())) {
    //             String relativePath = sourceFolder.toPath().relativize(sourceFile).toString();
    //             TarArchiveEntry entry = new TarArchiveEntry(sourceFile.toFile(), relativePath);
    //             if (sourceFile.toFile().isFile()) {
    //                 try (InputStream inputStream = new FileInputStream(sourceFile.toFile())) {
    //                     tarArchiveOutputStream.putArchiveEntry(entry);
    //                     IOUtils.copy(inputStream, tarArchiveOutputStream);
    //                     tarArchiveOutputStream.closeArchiveEntry();
    //                 }
    //             } else {
    //                 tarArchiveOutputStream.putArchiveEntry(entry);
    //                 tarArchiveOutputStream.closeArchiveEntry();
    //             }
    //         }
    //     }
    //     return compressFile;
    // }

    private ShareFileAsyncClient createShareFileAsyncClient(ResourceUploadDefinition option) {
        return new ShareFileClientBuilder()
            .endpoint(option.uploadUrl())
            .httpClient(manager().httpPipeline().getHttpClient())
            .buildFileAsyncClient();
    }

    private Mono<Void> uploadToStorage(File source, ResourceUploadDefinition option) {
        try {
            ShareFileAsyncClient shareFileAsyncClient = createShareFileAsyncClient(option);
            return shareFileAsyncClient.create(source.length())
                .flatMap(fileInfo -> shareFileAsyncClient.uploadFromFile(source.getAbsolutePath()))
                .then(Mono.empty());
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private Mono<String> uploadAndBuild(File source, ResourceUploadDefinition option) {
        AppPlatformManagementClientImpl client = (AppPlatformManagementClientImpl) manager().serviceClient();
        return uploadToStorage(source, option)
            .then(
                // manually create lro
                new PollerFlux<>(
                    manager().serviceClient().getDefaultPollInterval(),
                    context -> enqueueBuild(option, context),
                    this::waitForBuild,
                    (pollResultPollingContext, pollResultPollResponse) -> Mono.error(new RuntimeException("build canceled")), // throw if build canceled
                    this::getBuildResult)
                    .last()
                    .flatMap(client::getLroFinalResultOrError)
                    .flatMap((Function<Object, Mono<String>>) o -> {
                        BuildResultInner result = (BuildResultInner) o;
                        return Mono.just(result.id());
                    })
            );
    }

    private Mono<BuildResultInner> getBuildResult(PollingContext<PollResult<BuildInner>> context) {
        return manager().serviceClient().getBuildServices()
            .getBuildResultAsync(
                service().resourceGroupName(),
                service().name(),
                Constants.DEFAULT_TANZU_COMPONENT_NAME,
                parent().name(),
                ResourceUtils.nameFromResourceId(context.getData("buildId")));
    }

    private Mono<PollResponse<PollResult<BuildInner>>> waitForBuild(PollingContext<PollResult<BuildInner>> context) {
        return getBuildResult(context)
            .flatMap((Function<BuildResultInner, Mono<PollResponse<PollResult<BuildInner>>>>) buildResultInner -> {
                BuildResultProvisioningState state = buildResultInner.properties().provisioningState();
                PollResult<BuildInner> result = new PollResult<>(new BuildInner().withProperties(new BuildProperties()));
                if (state == BuildResultProvisioningState.SUCCEEDED) {
                    return Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, result));
                } else if (state == BuildResultProvisioningState.FAILED || state == BuildResultProvisioningState.DELETING) {
                    return Mono.error(new RuntimeException("build failed"));
                } else if (state == BuildResultProvisioningState.QUEUING) {
                    return Mono.just(new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, result));
                }
                return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, result));
            });
    }

    private Mono<PollResult<BuildInner>> enqueueBuild(ResourceUploadDefinition option, PollingContext<PollResult<BuildInner>> context) {
        return manager().serviceClient().getBuildServices()
            // This method enqueues the build request, response with provision state "Succeeded" only means the build is enqueued.
            // Attempting to continue deploying without waiting for the build to complete will result in failure.
            .createOrUpdateBuildAsync(
                service().resourceGroupName(),
                service().name(),
                Constants.DEFAULT_TANZU_COMPONENT_NAME,
                app().name(),
                new BuildInner().withProperties(
                    new BuildProperties()
                        .withBuilder(String.format("%s/buildservices/default/builders/%s", service().id(), Constants.DEFAULT_TANZU_COMPONENT_NAME))
                        .withAgentPool(String.format("%s/buildservices/default/agentPools/default", service().id()))
                        .withRelativePath(option.relativePath())
                ))
            .map(inner -> {
                context.setData("buildId", inner.properties().triggeredBuildResult().id());
                return new PollResult<>(inner);
            });
    }

    @Override
    public SpringAppDeploymentImpl withJarFile(File jar) {
        if (service().isEnterpriseTier()) {
            List<String> configFilePatterns = new ArrayList<>();
            configFilePatterns.add(app().name());
            withArtifact(jar, configFilePatterns);
        } else {
            ensureSource(UserSourceType.JAR);
            this.addDependency(
                context -> parent().getResourceUploadUrlAsync()
                    .flatMap(option -> {
                        UploadedUserSourceInfo uploadedUserSourceInfo = (UploadedUserSourceInfo) innerModel().properties().source();
                        uploadedUserSourceInfo.withRelativePath(option.relativePath());
                        return uploadToStorage(jar, option)
                            .then(context.voidMono());
                    })
            );
        }
        return this;
    }

    // @Override
    // public SpringAppDeploymentImpl withSourceCodeFolder(File sourceCodeFolder) {
    //     ensureSource();
    //     inner().properties().source().withType(UserSourceType.SOURCE);
    //     this.addDependency(
    //         context -> parent().getResourceUploadUrlAsync()
    //             .flatMap(option -> {
    //                 try {
    //                     return uploadToStorage(compressSource(sourceCodeFolder), option);
    //                 } catch (Exception e) {
    //                     return Mono.error(e);
    //                 }
    //             })
    //             .then(context.voidMono())
    //     );
    //     return this;
    // }

    @Override
    public SpringAppDeploymentImpl withExistingSource(UserSourceType type, String relativePath) {
        if (isEnterpriseTier()) {
            ensureSource(UserSourceType.BUILD_RESULT);
            UserSourceInfo sourceInfo = innerModel().properties().source();
            if (sourceInfo instanceof BuildResultUserSourceInfo) {
                BuildResultUserSourceInfo userSourceInfo = (BuildResultUserSourceInfo) sourceInfo;
                userSourceInfo.withBuildResultId(relativePath);
            }
        } else {
            ensureSource(type);
            UserSourceInfo userSourceInfo = innerModel().properties().source();
            if (userSourceInfo instanceof UploadedUserSourceInfo) {
                UploadedUserSourceInfo uploadedUserSourceInfo = (UploadedUserSourceInfo) userSourceInfo;
                uploadedUserSourceInfo.withRelativePath(relativePath);
            }
        }
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withArtifact(File artifact, List<String> configFilePatterns) {
        ensureSource(UserSourceType.fromString(Constants.BUILD_RESULT_SOURCE_TYPE));
        this.addDependency(
            context -> app().getResourceUploadUrlAsync()
                .flatMap(option ->
                    uploadAndBuild(artifact, option)
                        .flatMap(buildId -> {
                            BuildResultUserSourceInfo userSourceInfo = (BuildResultUserSourceInfo) innerModel().properties().source();
                            userSourceInfo.withBuildResultId(buildId);
                            // set config file patterns
                            ensureAddonConfigs();
                            Map<String, Map<String, Object>> addonConfigs = innerModel().properties().deploymentSettings().addonConfigs();
                            addonConfigs.computeIfAbsent("applicationConfigurationService", s -> {
                                Map<String, Object> config = new HashMap<>();
                                config.put(
                                    "configFilePatterns",
                                    configFilePatterns == null ? new ArrayList<>() : String.join(",", configFilePatterns));
                                return config;
                            });
                            return Mono.empty();
                        }).then(context.voidMono()))
        );
        return this;
    }

    private boolean isEnterpriseTier() {
        return service().isEnterpriseTier();
    }

    @Override
    public SpringAppDeploymentImpl withSourceCodeTarGzFile(File sourceCodeTarGz) {
        ensureSource(UserSourceType.SOURCE);
        this.addDependency(
            context -> parent().getResourceUploadUrlAsync()
                .flatMap(option -> uploadToStorage(sourceCodeTarGz, option)
                    .then(context.voidMono()))
        );
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withTargetModule(String moduleName) {
        ensureSource(UserSourceType.SOURCE);
        UserSourceInfo userSourceInfo = innerModel().properties().source();
        if (userSourceInfo instanceof SourceUploadedUserSourceInfo) {
            SourceUploadedUserSourceInfo sourceUploadedUserSourceInfo = (SourceUploadedUserSourceInfo) userSourceInfo;
            sourceUploadedUserSourceInfo.withArtifactSelector(moduleName);
        }
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withSingleModule() {
        ensureSource(UserSourceType.SOURCE);
        UserSourceInfo userSourceInfo = innerModel().properties().source();
        if (userSourceInfo instanceof SourceUploadedUserSourceInfo) {
            SourceUploadedUserSourceInfo sourceUploadedUserSourceInfo = (SourceUploadedUserSourceInfo) userSourceInfo;
            sourceUploadedUserSourceInfo.withArtifactSelector(null);
        }
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withInstance(int count) {
        if (innerModel().sku() == null) {
            innerModel().withSku(service().sku());
        }
        if (innerModel().sku() == null) {
            innerModel().withSku(new Sku().withName("B0"));
        }
        innerModel().sku().withCapacity(count);
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withCpu(int cpuCount) {
        ensureDeploySettings();
        innerModel().properties().deploymentSettings().resourceRequests().withCpu(String.valueOf(cpuCount));
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withMemory(int sizeInGB) {
        ensureDeploySettings();
        innerModel().properties().deploymentSettings().resourceRequests().withMemory(String.format("%dGi", sizeInGB));
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withRuntime(RuntimeVersion version) {
        UserSourceInfo userSourceInfo = innerModel().properties().source();
        if (userSourceInfo instanceof JarUploadedUserSourceInfo) {
            JarUploadedUserSourceInfo uploadedUserSourceInfo = (JarUploadedUserSourceInfo) userSourceInfo;
            uploadedUserSourceInfo.withRuntimeVersion(version.toString());
        } else if (userSourceInfo instanceof NetCoreZipUploadedUserSourceInfo) {
            NetCoreZipUploadedUserSourceInfo uploadedUserSourceInfo = (NetCoreZipUploadedUserSourceInfo) userSourceInfo;
            uploadedUserSourceInfo.withRuntimeVersion(version.toString());
        } else if (userSourceInfo instanceof SourceUploadedUserSourceInfo) {
            SourceUploadedUserSourceInfo uploadedUserSourceInfo = (SourceUploadedUserSourceInfo) userSourceInfo;
            uploadedUserSourceInfo.withRuntimeVersion(version.toString());
        }
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withJvmOptions(String jvmOptions) {
        if (isEnterpriseTier()) {
            withEnvironment("JAVA_OPTS", jvmOptions);
        } else {
            ensureSource(UserSourceType.JAR);
            JarUploadedUserSourceInfo uploadedUserSourceInfo = (JarUploadedUserSourceInfo) innerModel().properties().source();
            uploadedUserSourceInfo.withJvmOptions(jvmOptions);
        }
        return this;
    }

    private void ensureEnvironments() {
        ensureDeploySettings();
        if (innerModel().properties().deploymentSettings().environmentVariables() == null) {
            innerModel().properties().deploymentSettings().withEnvironmentVariables(new HashMap<>());
        }
    }

    private void ensureAddonConfigs() {
        ensureDeploySettings();
        if (innerModel().properties().deploymentSettings().addonConfigs() == null) {
            innerModel().properties().deploymentSettings().withAddonConfigs(new HashMap<>());
        }
    }

    @Override
    public SpringAppDeploymentImpl withEnvironment(String key, String value) {
        ensureEnvironments();
        innerModel().properties().deploymentSettings().environmentVariables().put(key, value);
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withoutEnvironment(String key) {
        ensureEnvironments();
        innerModel().properties().deploymentSettings().environmentVariables().remove(key);
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withVersionName(String versionName) {
        ensureSource();
        innerModel().properties().source().withVersion(versionName);
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withActivation() {
        this.addPostRunDependent(
            context -> parent().update().withActiveDeployment(name()).applyAsync()
                .map(Function.identity())
        );
        return this;
    }

    @Override
    public Mono<SpringAppDeployment> createResourceAsync() {
        return manager().serviceClient().getDeployments().createOrUpdateAsync(
            parent().parent().resourceGroupName(), parent().parent().name(),
            parent().name(), name(), innerModel()
        )
            .map(inner -> {
                setInner(inner);
                return this;
            });
    }

    @Override
    public Mono<SpringAppDeployment> updateResourceAsync() {
        return manager().serviceClient().getDeployments().updateAsync(
            parent().parent().resourceGroupName(), parent().parent().name(),
            parent().name(), name(), innerModel()
        )
            .map(inner -> {
                setInner(inner);
                return this;
            });
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return manager().serviceClient().getDeployments().deleteAsync(
            parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), name()
        );
    }

    @Override
    protected Mono<DeploymentResourceInner> getInnerAsync() {
        return manager().serviceClient().getDeployments().getAsync(
            parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), name()
        );
    }

    @Override
    public String id() {
        return innerModel().id();
    }

    @Override
    public SpringAppDeploymentImpl update() {
        prepareUpdate();
        return this;
    }

    private AppPlatformManager manager() {
        return parent().manager();
    }

    @Override
    public SpringAppImpl attach() {
        return parent().addActiveDeployment(this);
    }

    private SpringAppImpl app() {
        return parent();
    }

    private SpringServiceImpl service() {
        return parent().parent();
    }
}
