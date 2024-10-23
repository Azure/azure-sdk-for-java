// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.http.HttpResponse;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.appservice.fluent.models.HostKeysInner;
import com.azure.resourcemanager.appservice.fluent.models.SitePatchResourceInner;
import com.azure.resourcemanager.appservice.models.CsmDeploymentStatus;
import com.azure.resourcemanager.appservice.models.DeployOptions;
import com.azure.resourcemanager.appservice.models.DeployType;
import com.azure.resourcemanager.appservice.models.DeploymentSlotBase;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.FunctionDeploymentSlot;
import com.azure.resourcemanager.appservice.fluent.models.SiteConfigResourceInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteLogsConfigInner;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.azure.resourcemanager.appservice.models.KuduDeploymentResult;
import reactor.core.publisher.Mono;

/** The implementation for FunctionDeploymentSlot. */
class FunctionDeploymentSlotImpl
    extends DeploymentSlotBaseImpl<
        FunctionDeploymentSlot,
        FunctionDeploymentSlotImpl,
        FunctionAppImpl,
        FunctionDeploymentSlot.DefinitionStages.WithCreate,
        DeploymentSlotBase<FunctionDeploymentSlot>>
    implements FunctionDeploymentSlot, FunctionDeploymentSlot.Definition {

    private static final ClientLogger LOGGER = new ClientLogger(FunctionDeploymentSlotImpl.class);

    private Boolean appServicePlanIsFlexConsumption;

    FunctionDeploymentSlotImpl(
        String name,
        SiteInner innerObject,
        SiteConfigResourceInner siteConfig,
        SiteLogsConfigInner logConfig,
        FunctionAppImpl parent) {
        super(name, innerObject, siteConfig, logConfig, parent);
    }

    @Override
    public FunctionDeploymentSlot.DefinitionStages.WithCreate withConfigurationFromParent() {
        return withConfigurationFromFunctionApp(this.parent());
    }

    @Override
    public FunctionDeploymentSlot.DefinitionStages.WithCreate withConfigurationFromFunctionApp(FunctionApp app) {
        this.siteConfig = ((WebAppBaseImpl) app).siteConfig;
        configurationSource = app;
        return this;
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
        return kuduClient.zipDeployAsync(zipFile, length);
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
    Mono<SiteInner> submitSite(final SiteInner site) {
        return submitSiteWithoutSiteConfig(site);
    }

    @Override
    Mono<SiteInner> submitSite(final SitePatchResourceInner siteUpdate) {
        // PATCH does not work for function app slot
        return submitSiteWithoutSiteConfig(this.innerModel());
    }

    @Override
    public String getMasterKey() {
        return this.getMasterKeyAsync().block();
    }

    @Override
    public Mono<String> getMasterKeyAsync() {
        return this.manager().serviceClient().getWebApps().listHostKeysSlotAsync(
            this.resourceGroupName(), this.parent().name(), this.name()).map(HostKeysInner::masterKey);
    }

    @Override
    public void deploy(DeployType type, File file) {
        deployAsync(type, file).block();
    }

    @Override
    public Mono<Void> deployAsync(DeployType type, File file) {
        return deployAsync(type, file, null);
    }

    @Override
    public void deploy(DeployType type, File file, DeployOptions deployOptions) {
        deployAsync(type, file, null).block();
    }

    @Override
    public Mono<Void> deployAsync(DeployType type, File file, DeployOptions deployOptions) {
        return this.pushDeployAsync(type, file, null)
            .flatMap(result -> kuduClient.pollDeploymentStatus(result, manager().serviceClient().getDefaultPollInterval()));
    }

    @Override
    public void deploy(DeployType type, InputStream file, long length) {
        deployAsync(type, file, length).block();
    }

    @Override
    public Mono<Void> deployAsync(DeployType type, InputStream file, long length) {
        return deployAsync(type, file, length, null);
    }

    @Override
    public void deploy(DeployType type, InputStream file, long length, DeployOptions deployOptions) {
        deployAsync(type, file, length, null).block();
    }

    @Override
    public Mono<Void> deployAsync(DeployType type, InputStream file, long length, DeployOptions deployOptions) {
        return this.pushDeployAsync(type, file, length, null)
            .flatMap(result -> kuduClient.pollDeploymentStatus(result, manager().serviceClient().getDefaultPollInterval()));
    }

    @Override
    public KuduDeploymentResult pushDeploy(DeployType type, File file, DeployOptions deployOptions) {
        return pushDeployAsync(type, file, deployOptions).block();
    }

    @Override
    public Mono<KuduDeploymentResult> pushDeployAsync(DeployType type, File file, DeployOptions deployOptions) {
        // If tier of the AppServicePlan is "FlexConsumption", use /api/publish; else, use /api/zipdeploy

        // deployOptions is ignored
        if (type != DeployType.ZIP) {
            return Mono.error(new IllegalArgumentException("Deployment to Function App supports ZIP package."));
        }
        return getAppServicePlanIsFlexConsumptionMono().flatMap(appServiceIsFlexConsumptionPlan -> {
            try {
                if (appServiceIsFlexConsumptionPlan) {
                    return kuduClient.pushDeployFlexConsumptionAsync(file);
                } else {
                    return kuduClient.pushZipDeployAsync(file)
                        .then(Mono.just(new KuduDeploymentResult("latest")));
                }
            } catch (IOException e) {
                return Mono.error(e);
            }
        });
    }

    private Mono<KuduDeploymentResult> pushDeployAsync(DeployType type, InputStream file, long length,
                                                       DeployOptions deployOptions) {
        // deployOptions is ignored
        if (type != DeployType.ZIP) {
            return Mono.error(new IllegalArgumentException("Deployment to Function App supports ZIP package."));
        }
        return getAppServicePlanIsFlexConsumptionMono().flatMap(appServiceIsFlexConsumptionPlan -> {
            try {
                if (appServiceIsFlexConsumptionPlan) {
                    return kuduClient.pushDeployFlexConsumptionAsync(file, length);
                } else {
                    return kuduClient.pushZipDeployAsync(file, length)
                        .then(Mono.just(new KuduDeploymentResult("latest")));
                }
            } catch (IOException e) {
                return Mono.error(e);
            }
        });
    }

    private Mono<Boolean> getAppServicePlanIsFlexConsumptionMono() {
        Mono<Boolean> updateAppServicePlan = Mono.justOrEmpty(appServicePlanIsFlexConsumption);
        if (appServicePlanIsFlexConsumption == null) {
            updateAppServicePlan = Mono.defer(
                () -> manager().appServicePlans()
                    .getByIdAsync(this.appServicePlanId())
                    .map(appServicePlan -> {
                        appServicePlanIsFlexConsumption = "FlexConsumption".equals(appServicePlan.pricingTier().toSkuDescription().tier());
                        return appServicePlanIsFlexConsumption;
                    }));
        }
        return updateAppServicePlan;
    }

    @Override
    public CsmDeploymentStatus getDeploymentStatus(String deploymentId) {
        return getDeploymentStatusAsync(deploymentId).block();
    }

    @Override
    public Mono<CsmDeploymentStatus> getDeploymentStatusAsync(String deploymentId) {
        // "GET" LRO is not supported in azure-core
        SerializerAdapter serializerAdapter = SerializerFactory.createDefaultManagementSerializerAdapter();
        return this.manager().serviceClient().getWebApps()
            .getSlotSiteDeploymentStatusSlotWithResponseAsync(this.resourceGroupName(), this.parent().name(), this.name(), deploymentId)
            .flatMap(fluxResponse -> {
                HttpResponse response = new HttpFluxBBResponse(fluxResponse);
                return response.getBodyAsString()
                    .flatMap(bodyString -> {
                        CsmDeploymentStatus status;
                        try {
                            status = serializerAdapter.deserialize(bodyString, CsmDeploymentStatus.class, SerializerEncoding.JSON);
                        } catch (IOException e) {
                            return Mono.error(new ManagementException("Deserialize failed for response body.", response));
                        }
                        return Mono.justOrEmpty(status);
                    }).doFinally(ignored -> response.close());
            });
    }
}
