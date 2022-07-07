// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.http.HttpResponse;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.appservice.fluent.models.SiteConfigResourceInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteLogsConfigInner;
import com.azure.resourcemanager.appservice.models.CsmDeploymentStatus;
import com.azure.resourcemanager.appservice.models.DeployOptions;
import com.azure.resourcemanager.appservice.models.DeployType;
import com.azure.resourcemanager.appservice.models.DeploymentSlot;
import com.azure.resourcemanager.appservice.models.DeploymentSlotBase;
import com.azure.resourcemanager.appservice.models.KuduDeploymentResult;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/** The implementation for DeploymentSlot. */
class DeploymentSlotImpl
    extends DeploymentSlotBaseImpl<
        DeploymentSlot,
        DeploymentSlotImpl,
        WebAppImpl,
        DeploymentSlot.DefinitionStages.WithCreate,
        DeploymentSlotBase.Update<DeploymentSlot>>
    implements DeploymentSlot, DeploymentSlot.Definition {
    private static final Duration MAX_DEPLOYMENT_STATUS_TIMEOUT = Duration.ofMinutes(5);

    DeploymentSlotImpl(
        String name,
        SiteInner innerObject,
        SiteConfigResourceInner siteConfig,
        SiteLogsConfigInner logConfig,
        WebAppImpl parent) {
        super(name, innerObject, siteConfig, logConfig, parent);
    }

    @Override
    public DeploymentSlotImpl withConfigurationFromParent() {
        return withConfigurationFromWebApp(this.parent());
    }

    @Override
    public DeploymentSlotImpl withConfigurationFromWebApp(WebApp webApp) {
        this.siteConfig = ((WebAppBaseImpl) webApp).siteConfig;
        configurationSource = webApp;
        return this;
    }

    @Override
    public Mono<Void> warDeployAsync(File warFile) {
        return warDeployAsync(warFile, null);
    }

    @Override
    public void warDeploy(File warFile) {
        warDeployAsync(warFile).block();
    }

    @Override
    public Mono<Void> warDeployAsync(InputStream warFile, long length) {
        return warDeployAsync(warFile, length, null);
    }

    @Override
    public void warDeploy(InputStream warFile, long length) {
        warDeployAsync(warFile, length).block();
    }

    @Override
    public Mono<Void> warDeployAsync(File warFile, String appName) {
        try {
            return kuduClient.warDeployAsync(warFile, appName);
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    @Override
    public void warDeploy(File warFile, String appName) {
        warDeployAsync(warFile, appName).block();
    }

    @Override
    public Mono<Void> warDeployAsync(InputStream warFile, long length, String appName) {
        return kuduClient.warDeployAsync(warFile, length, appName);
    }

    @Override
    public void warDeploy(InputStream warFile, long length, String appName) {
        warDeployAsync(warFile, length, appName).block();
    }

    @Override
    public void zipDeploy(File zipFile) {
        zipDeployAsync(zipFile).block();
    }

    @Override
    public void zipDeploy(InputStream zipFile, long length) {
        zipDeployAsync(zipFile, length).block();
    }

    @Override
    public Mono<Void> zipDeployAsync(InputStream zipFile, long length) {
        return kuduClient.zipDeployAsync(zipFile, length).then(stopAsync()).then(startAsync());
    }

    @Override
    public Mono<Void> zipDeployAsync(File zipFile) {
        try {
            return kuduClient.zipDeployAsync(zipFile);
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    @Override
    public void deploy(DeployType type, File file) {
        deployAsync(type, file).block();
    }

    @Override
    public Mono<Void> deployAsync(DeployType type, File file) {
        return deployAsync(type, file, new DeployOptions());
    }

    @Override
    public void deploy(DeployType type, File file, DeployOptions deployOptions) {
        deployAsync(type, file, deployOptions).block();
    }

    @Override
    public Mono<Void> deployAsync(DeployType type, File file, DeployOptions deployOptions) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(file);
        if (deployOptions == null) {
            deployOptions = new DeployOptions();
        }
        try {
            return kuduClient.deployAsync(type, file,
                deployOptions.path(), deployOptions.restartSite(), deployOptions.cleanDeployment());
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    @Override
    public void deploy(DeployType type, InputStream file, long length) {
        deployAsync(type, file, length).block();
    }

    @Override
    public Mono<Void> deployAsync(DeployType type, InputStream file, long length) {
        return deployAsync(type, file, length, new DeployOptions());
    }

    @Override
    public void deploy(DeployType type, InputStream file, long length, DeployOptions deployOptions) {
        deployAsync(type, file, length, deployOptions).block();
    }

    @Override
    public Mono<Void> deployAsync(DeployType type, InputStream file, long length, DeployOptions deployOptions) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(file);
        if (deployOptions == null) {
            deployOptions = new DeployOptions();
        }
        return kuduClient.deployAsync(type, file, length,
            deployOptions.path(), deployOptions.restartSite(), deployOptions.cleanDeployment());
    }

    @Override
    public KuduDeploymentResult pushDeploy(DeployType type, File file, DeployOptions deployOptions) {
        return pushDeployAsync(type, file, deployOptions).block();
    }

    @Override
    public Mono<KuduDeploymentResult> pushDeployAsync(DeployType type, File file, DeployOptions deployOptions) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(file);
        if (deployOptions == null) {
            deployOptions = new DeployOptions();
        }
        try {
            return kuduClient.pushDeployAsync(type, file,
                deployOptions.path(), deployOptions.restartSite(), deployOptions.cleanDeployment(),
                deployOptions.trackDeployment());
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    @Override
    public CsmDeploymentStatus getDeploymentStatus(String deploymentId) {
        return getDeploymentStatusAsync(deploymentId).block();
    }

    @Override
    public Mono<CsmDeploymentStatus> getDeploymentStatusAsync(String deploymentId) {
        // "GET" LRO is not supported in azure-core
        AtomicLong pollCount = new AtomicLong();
        Duration pollDuration = manager().serviceClient().getDefaultPollInterval();
        return this.manager().serviceClient().getWebApps()
            .getProductionSiteDeploymentStatusWithResponseAsync(this.resourceGroupName(), this.name(), deploymentId)
            .flatMap(fluxResponse -> {
                if (pollDuration.multipliedBy(pollCount.get()).compareTo(MAX_DEPLOYMENT_STATUS_TIMEOUT) < 0) {
                    HttpResponse response = new HttpFluxBBResponse(fluxResponse);
                    if (response.getStatusCode() / 100 != 2) {
                        return Mono.error(new ManagementException("Service responds with a non-20x response.", response));
                    }
                    return response.getBodyAsString()
                        .flatMap(bodyString -> {
                            SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
                            CsmDeploymentStatus status;
                            try {
                                status = serializerAdapter.deserialize(bodyString, CsmDeploymentStatus.class, SerializerEncoding.JSON);
                            } catch (IOException e) {
                                return Mono.error(new ManagementException("Deserialize failed for response body.", response));
                            }
                            if (status == null) {
                                return Mono.empty();
                            }
                            return Mono.just(status);
                        });
                } else {
                    return Mono.error(new ManagementException("Timeout getting deployment status for deploymentId: " + deploymentId, null));
                }
            }).repeatWhenEmpty(
                longFlux ->
                    longFlux
                        .flatMap(
                            index -> {
                                pollCount.set(index);
                                return Mono.delay(ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(pollDuration));
                            }));
    }
}
