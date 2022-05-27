// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.models.BuildInner;
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
import com.azure.resourcemanager.resources.fluentcore.dag.FunctionalTaskItem;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClientBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class SpringAppDeploymentImpl
    extends ExternalChildResourceImpl<SpringAppDeployment, DeploymentResourceInner, SpringAppImpl, SpringApp>
    implements SpringAppDeployment,
        SpringAppDeployment.Definition<SpringAppImpl, SpringAppDeploymentImpl>,
        SpringAppDeployment.Update {
    private static final Duration MAX_BUILD_TIMEOUT = Duration.ofHours(1);
    private BuildServiceTask buildServiceTask;

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

    @Override
    public List<String> configFilePatterns() {
        Map<String, Map<String, Object>> addonConfigs = this.innerModel().properties().deploymentSettings().addonConfigs();
        if (addonConfigs == null) {
            return Collections.emptyList();
        }
        Map<String, Object> configurationConfigs = addonConfigs.get(Constants.APPLICATION_CONFIGURATION_SERVICE_KEY);
        if (configurationConfigs == null) {
            return Collections.emptyList();
        }
        if (configurationConfigs.get(Constants.CONFIG_FILE_PATTERNS_KEY) instanceof String) {
            String patterns = (String) configurationConfigs.get(Constants.CONFIG_FILE_PATTERNS_KEY);
            return Collections.unmodifiableList(Arrays.asList(patterns.split(",")));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Double cpu() {
        return Utils.fromCpuString(innerModel().properties().deploymentSettings().resourceRequests().cpu());
    }

    @Override
    public Double memoryInGB() {
        return Utils.fromMemoryString(innerModel().properties().deploymentSettings().resourceRequests().memory());
    }

    @Override
    public RuntimeVersion runtimeVersion() {
        if (isEnterpriseTier() || innerModel().properties() == null) {
            return null;
        }
        UserSourceInfo userSourceInfo = innerModel().properties().source();
        if (userSourceInfo instanceof JarUploadedUserSourceInfo) {
            JarUploadedUserSourceInfo uploadedUserSourceInfo = (JarUploadedUserSourceInfo) userSourceInfo;
            return RuntimeVersion.fromString(uploadedUserSourceInfo.runtimeVersion());
        } else if (userSourceInfo instanceof NetCoreZipUploadedUserSourceInfo) {
            NetCoreZipUploadedUserSourceInfo uploadedUserSourceInfo = (NetCoreZipUploadedUserSourceInfo) userSourceInfo;
            return RuntimeVersion.fromString(uploadedUserSourceInfo.runtimeVersion());
        } else if (userSourceInfo instanceof SourceUploadedUserSourceInfo) {
            SourceUploadedUserSourceInfo uploadedUserSourceInfo = (SourceUploadedUserSourceInfo) userSourceInfo;
            return RuntimeVersion.fromString(uploadedUserSourceInfo.runtimeVersion());
        }
        return null;
    }

    @Override
    public String jvmOptions() {
        if (innerModel().properties() == null) {
            return null;
        }
        String jvmOptions = null;
        if (isEnterpriseTier() && innerModel().properties().deploymentSettings() != null) {
            Map<String, String> environment = innerModel().properties().deploymentSettings().environmentVariables();
            if (environment != null) {
                jvmOptions = environment.get(Constants.JAVA_OPTS);
            }
        } else if (innerModel().properties().source() != null) {
            UserSourceInfo userSourceInfo = innerModel().properties().source();
            if (userSourceInfo instanceof JarUploadedUserSourceInfo) {
                JarUploadedUserSourceInfo uploadedUserSourceInfo = (JarUploadedUserSourceInfo) userSourceInfo;
                jvmOptions = uploadedUserSourceInfo.jvmOptions();
            }
        }
        return jvmOptions;
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

    @Override
    public SpringAppDeploymentImpl withJarFile(File jar) {
        if (service().isEnterpriseTier()) {
            return withJarFile(jar, null);
        } else {
            ensureSource(UserSourceType.JAR);
            this.addDependency(
                context -> parent().getResourceUploadUrlAsync()
                    .flatMap(option -> {
                        UploadedUserSourceInfo uploadedUserSourceInfo = (UploadedUserSourceInfo) innerModel().properties().source();
                        uploadedUserSourceInfo.withRelativePath(option.relativePath());
                        return uploadToStorageAsync(jar, option)
                            .then(context.voidMono());
                    })
            );
            return this;
        }
    }

    private ShareFileAsyncClient createShareFileAsyncClient(ResourceUploadDefinition option) {
        return new ShareFileClientBuilder()
            .endpoint(option.uploadUrl())
            .httpClient(manager().httpPipeline().getHttpClient())
            .buildFileAsyncClient();
    }

    private Mono<Void> uploadToStorageAsync(File source, ResourceUploadDefinition option) {
        try {
            ShareFileAsyncClient shareFileAsyncClient = createShareFileAsyncClient(option);
            return shareFileAsyncClient.create(source.length())
                .flatMap(fileInfo -> shareFileAsyncClient.uploadFromFile(source.getAbsolutePath()))
                .then(Mono.empty());
        } catch (Exception e) {
            return Mono.error(e);
        }
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
    public SpringAppDeploymentImpl withJarFile(File jar, List<String> configFilePatterns) {
        ensureSource(UserSourceType.BUILD_RESULT);
        this.buildServiceTask = new BuildServiceTask(jar, configFilePatterns);
        return this;
    }

    private boolean isEnterpriseTier() {
        return service().isEnterpriseTier();
    }

    @Override
    public SpringAppDeploymentImpl withSourceCodeTarGzFile(File sourceCodeTarGz) {
        return withSourceCodeTarGzFile(sourceCodeTarGz, null);
    }

    @Override
    public SpringAppDeploymentImpl withSourceCodeTarGzFile(File sourceCodeTarGz, List<String> configFilePatterns) {
        if (isEnterpriseTier()) {
            ensureSource(UserSourceType.BUILD_RESULT);
            this.buildServiceTask = new BuildServiceTask(sourceCodeTarGz, configFilePatterns, true);
        } else {
            ensureSource(UserSourceType.SOURCE);
            this.addDependency(
                context -> parent().getResourceUploadUrlAsync()
                    .flatMap(option -> {
                        UploadedUserSourceInfo uploadedUserSourceInfo = (UploadedUserSourceInfo) innerModel().properties().source();
                        uploadedUserSourceInfo.withRelativePath(option.relativePath());
                        return uploadToStorageAsync(sourceCodeTarGz, option)
                            .then(context.voidMono());
                    })
            );
        }
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withTargetModule(String moduleName) {
        if (isEnterpriseTier()) {
            ensureSource(UserSourceType.BUILD_RESULT);
            this.buildServiceTask.module = moduleName;
        } else {
            ensureSource(UserSourceType.SOURCE);
            UserSourceInfo userSourceInfo = innerModel().properties().source();
            if (userSourceInfo instanceof SourceUploadedUserSourceInfo) {
                SourceUploadedUserSourceInfo sourceUploadedUserSourceInfo = (SourceUploadedUserSourceInfo) userSourceInfo;
                sourceUploadedUserSourceInfo.withArtifactSelector(moduleName);
            }
        }
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withSingleModule() {
        return withTargetModule(null);
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
        return withCpu((double) cpuCount);
    }

    @Override
    public SpringAppDeploymentImpl withCpu(double cpuCount) {
        ensureDeploySettings();
        innerModel().properties().deploymentSettings().resourceRequests().withCpu(Utils.toCpuString(cpuCount));
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withMemory(int sizeInGB) {
        return withMemory((double) sizeInGB);
    }

    @Override
    public SpringAppDeploymentImpl withMemory(double sizeInGB) {
        ensureDeploySettings();
        innerModel().properties().deploymentSettings().resourceRequests().withMemory(Utils.toMemoryString(sizeInGB));
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
            withEnvironment(Constants.JAVA_OPTS, jvmOptions);
        } else {
            ensureSource(UserSourceType.JAR);
            UserSourceInfo userSourceInfo = innerModel().properties().source();
            if (userSourceInfo instanceof JarUploadedUserSourceInfo) {
                JarUploadedUserSourceInfo uploadedUserSourceInfo = (JarUploadedUserSourceInfo) userSourceInfo;
                uploadedUserSourceInfo.withJvmOptions(jvmOptions);
            }
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
    public SpringAppDeploymentImpl withConfigFilePatterns(List<String> configFilePatterns) {
        ensureAddonConfigs();
        Map<String, Map<String, Object>> addonConfigs = innerModel().properties().deploymentSettings().addonConfigs();
        addonConfigs.computeIfAbsent(Constants.APPLICATION_CONFIGURATION_SERVICE_KEY, s -> {
            Map<String, Object> config = new HashMap<>();
            config.put(
                Constants.CONFIG_FILE_PATTERNS_KEY,
                CoreUtils.isNullOrEmpty(configFilePatterns) ? "" : String.join(",", configFilePatterns));
            return config;
        });
        return this;
    }

    @Override
    public void beforeGroupCreateOrUpdate() {
        super.beforeGroupCreateOrUpdate();
        if (this.buildServiceTask != null) {
            this.addDependency(this.buildServiceTask);
            this.buildServiceTask = null;
        }
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

    private class BuildServiceTask implements FunctionalTaskItem {
        private final File file;
        private final boolean sourceCodeTarGz;
        private final List<String> configFilePatterns;
        private String module;

        BuildServiceTask(File file, List<String> configFilePatterns) {
            this(file, configFilePatterns, false);
        }

        BuildServiceTask(File file, List<String> configFilePatterns, boolean sourceCodeTarGz) {
            this.file = file;
            this.configFilePatterns = configFilePatterns;
            this.sourceCodeTarGz = sourceCodeTarGz;
        }

        @Override
        public Mono<Indexable> apply(Context context) {
            return app().getResourceUploadUrlAsync()
                .flatMap(option ->
                    uploadAndBuildAsync(file, option)
                        .flatMap(buildId -> {
                            BuildResultUserSourceInfo userSourceInfo = (BuildResultUserSourceInfo) innerModel().properties().source();
                            userSourceInfo.withBuildResultId(buildId);
                            withConfigFilePatterns(this.configFilePatterns);
                            return Mono.empty();
                        }).then(context.voidMono()));
        }

        private Mono<String> uploadAndBuildAsync(File source, ResourceUploadDefinition option) {
            AtomicLong pollCount = new AtomicLong();
            Duration pollDuration = manager().serviceClient().getDefaultPollInterval();
            return uploadToStorageAsync(source, option)
                .then(enqueueBuildAsync(option))
                .flatMap(buildId ->
                    manager().serviceClient().getBuildServices()
                        .getBuildResultWithResponseAsync(
                            service().resourceGroupName(),
                            service().name(),
                            Constants.DEFAULT_TANZU_COMPONENT_NAME,
                            parent().name(),
                            ResourceUtils.nameFromResourceId(buildId))
                        .flatMap(response -> {
                            if (pollDuration.multipliedBy(pollCount.get()).compareTo(MAX_BUILD_TIMEOUT) < 0) {
                                BuildResultProvisioningState state = response.getValue().properties().provisioningState();
                                if (state == BuildResultProvisioningState.SUCCEEDED) {
                                    return Mono.just(buildId);
                                } else if (state == BuildResultProvisioningState.QUEUING || state == BuildResultProvisioningState.BUILDING) {
                                    return Mono.empty();
                                } else {
                                    AppPlatformManagementClientImpl client = (AppPlatformManagementClientImpl) manager().serviceClient();
                                    return Mono.error(new ManagementException(String.format("Build failed for file: %s, buildId: %s",
                                        file.getName(), buildId),
                                        new HttpResponseImpl<>(response, client.getSerializerAdapter())));
                                }
                            } else {
                                return Mono.error(new ManagementException(String.format("Build timeout for file: %s, buildId: %s",
                                    file.getName(), buildId), null));
                            }
                        }).repeatWhenEmpty(
                            longFlux ->
                                longFlux
                                    .flatMap(
                                        index -> {
                                            pollCount.set(index);
                                            return Mono.delay(ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(pollDuration));
                                        })));
        }

        private Mono<String> enqueueBuildAsync(ResourceUploadDefinition option) {
            BuildProperties buildProperties = new BuildProperties()
                .withBuilder(String.format("%s/buildservices/%s/builders/%s", service().id(), Constants.DEFAULT_TANZU_COMPONENT_NAME, Constants.DEFAULT_TANZU_COMPONENT_NAME))
                .withAgentPool(String.format("%s/buildservices/%s/agentPools/%s", service().id(), Constants.DEFAULT_TANZU_COMPONENT_NAME, Constants.DEFAULT_TANZU_COMPONENT_NAME))
                .withRelativePath(option.relativePath());
            // source code
            if (this.sourceCodeTarGz) {
                Map<String, String> buildEnv = buildProperties.env() == null ? new HashMap<>() : buildProperties.env();
                buildProperties.withEnv(buildEnv);
                if (module != null) {
                    // for now, only support maven project
                    buildEnv.put("BP_MAVEN_BUILT_MODULE", module);
                }
            }
            return manager().serviceClient().getBuildServices()
                // This method enqueues the build request, response with provision state "Succeeded" only means the build is enqueued.
                // Attempting to continue deploying without waiting for the build to complete will result in failure.
                .createOrUpdateBuildAsync(
                    service().resourceGroupName(),
                    service().name(),
                    Constants.DEFAULT_TANZU_COMPONENT_NAME,
                    app().name(),
                    new BuildInner().withProperties(buildProperties))
                .map(inner -> inner.properties().triggeredBuildResult().id());
        }

        @SuppressWarnings("BlockingMethodInNonBlockingContext")
        private class HttpResponseImpl<T> extends HttpResponse {
            private final Response<T> response;
            private final SerializerAdapter serializerAdapter;

            protected HttpResponseImpl(Response<T> response, SerializerAdapter serializerAdapter) {
                super(response.getRequest());
                this.response = response;
                this.serializerAdapter = serializerAdapter;
            }

            @Override
            public int getStatusCode() {
                return response.getStatusCode();
            }

            @Override
            public String getHeaderValue(String header) {
                return response.getHeaders().getValue(header);
            }

            @Override
            public HttpHeaders getHeaders() {
                return response.getHeaders();
            }

            @Override
            public Flux<ByteBuffer> getBody() {
                try {
                    return Flux.just(ByteBuffer.wrap(serializerAdapter.serializeToBytes(response.getValue(), SerializerEncoding.JSON)));
                } catch (IOException e) {
                    return Flux.empty();
                }
            }

            @Override
            public Mono<byte[]> getBodyAsByteArray() {
                try {
                    return Mono.just(serializerAdapter.serializeToBytes(response.getValue(), SerializerEncoding.JSON));
                } catch (IOException e) {
                    return Mono.empty();
                }
            }

            @Override
            public Mono<String> getBodyAsString() {
                return Mono.just(serializerAdapter.serializeRaw(response.getValue()));
            }

            @Override
            public Mono<String> getBodyAsString(Charset charset) {
                return getBodyAsString();
            }
        }
    }
}
