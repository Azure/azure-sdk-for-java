// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.models.DeploymentResourceInner;
import com.azure.resourcemanager.appplatform.fluent.models.LogFileUrlResponseInner;
import com.azure.resourcemanager.appplatform.models.DeploymentInstance;
import com.azure.resourcemanager.appplatform.models.DeploymentResourceProperties;
import com.azure.resourcemanager.appplatform.models.DeploymentResourceStatus;
import com.azure.resourcemanager.appplatform.models.DeploymentSettings;
import com.azure.resourcemanager.appplatform.models.ResourceUploadDefinition;
import com.azure.resourcemanager.appplatform.models.RuntimeVersion;
import com.azure.resourcemanager.appplatform.models.Sku;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringAppDeployment;
import com.azure.resourcemanager.appplatform.models.UserSourceInfo;
import com.azure.resourcemanager.appplatform.models.UserSourceType;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClientBuilder;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;

public class SpringAppDeploymentImpl
    extends ExternalChildResourceImpl<SpringAppDeployment, DeploymentResourceInner, SpringAppImpl, SpringApp>
    implements SpringAppDeployment,
        SpringAppDeployment.Definition<SpringAppImpl, SpringAppDeploymentImpl>,
        SpringAppDeployment.Update {

    SpringAppDeploymentImpl(String name, SpringAppImpl parent, DeploymentResourceInner innerObject) {
        super(name, parent, innerObject);
    }

    @Override
    public String appName() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().appName();
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
    public OffsetDateTime createdTime() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().createdTime();
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
    }

    private void ensureSource() {
        if (innerModel().properties() == null) {
            innerModel().withProperties(new DeploymentResourceProperties());
        }
        if (innerModel().properties().source() == null) {
            innerModel().properties().withSource(new UserSourceInfo());
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
        innerModel().properties().source().withRelativePath(option.relativePath());
        try {
            ShareFileAsyncClient shareFileAsyncClient = createShareFileAsyncClient(option);
            return shareFileAsyncClient.create(source.length())
                .flatMap(fileInfo -> shareFileAsyncClient.uploadFromFile(source.getAbsolutePath()))
                .then(Mono.empty());
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    @Override
    public SpringAppDeploymentImpl withJarFile(File jar) {
        ensureSource();
        innerModel().properties().source().withType(UserSourceType.JAR);
        this.addDependency(
            context -> parent().getResourceUploadUrlAsync()
                .flatMap(option -> uploadToStorage(jar, option)
                    .then(context.voidMono()))
        );
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
        ensureSource();
        innerModel().properties().source().withType(type);
        innerModel().properties().source().withRelativePath(relativePath);
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withSourceCodeTarGzFile(File sourceCodeTarGz) {
        ensureSource();
        innerModel().properties().source().withType(UserSourceType.SOURCE);
        this.addDependency(
            context -> parent().getResourceUploadUrlAsync()
                .flatMap(option -> uploadToStorage(sourceCodeTarGz, option)
                    .then(context.voidMono()))
        );
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withTargetModule(String moduleName) {
        ensureSource();
        innerModel().properties().source().withArtifactSelector(moduleName);
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withSingleModule() {
        ensureSource();
        innerModel().properties().source().withArtifactSelector(null);
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withInstance(int count) {
        if (innerModel().sku() == null) {
            innerModel().withSku(parent().parent().sku());
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
        innerModel().properties().deploymentSettings().withCpu(cpuCount);
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withMemory(int sizeInGB) {
        ensureDeploySettings();
        innerModel().properties().deploymentSettings().withMemoryInGB(sizeInGB);
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withRuntime(RuntimeVersion version) {
        ensureDeploySettings();
        innerModel().properties().deploymentSettings().withRuntimeVersion(version);
        return this;
    }

    @Override
    public SpringAppDeploymentImpl withJvmOptions(String jvmOptions) {
        ensureDeploySettings();
        innerModel().properties().deploymentSettings().withJvmOptions(jvmOptions);
        return this;
    }

    private void ensureEnvironments() {
        ensureDeploySettings();
        if (innerModel().properties().deploymentSettings().environmentVariables() == null) {
            innerModel().properties().deploymentSettings().withEnvironmentVariables(new HashMap<>());
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
                .map(app -> (Indexable) app)
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
}
