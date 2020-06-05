// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.DebugSetting;
import com.azure.resourcemanager.resources.models.Dependency;
import com.azure.resourcemanager.resources.models.Deployment;
import com.azure.resourcemanager.resources.models.DeploymentExportResult;
import com.azure.resourcemanager.resources.models.DeploymentMode;
import com.azure.resourcemanager.resources.models.DeploymentOperations;
import com.azure.resourcemanager.resources.models.DeploymentProperties;
import com.azure.resourcemanager.resources.models.DeploymentPropertiesExtended;
import com.azure.resourcemanager.resources.models.DeploymentWhatIf;
import com.azure.resourcemanager.resources.models.DeploymentWhatIfProperties;
import com.azure.resourcemanager.resources.models.DeploymentWhatIfSettings;
import com.azure.resourcemanager.resources.models.OnErrorDeployment;
import com.azure.resourcemanager.resources.models.OnErrorDeploymentType;
import com.azure.resourcemanager.resources.models.ParametersLink;
import com.azure.resourcemanager.resources.models.Provider;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.models.TemplateLink;
import com.azure.resourcemanager.resources.models.WhatIfOperationResult;
import com.azure.resourcemanager.resources.models.WhatIfResultFormat;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.fluent.inner.DeploymentExtendedInner;
import com.azure.resourcemanager.resources.fluent.inner.DeploymentInner;
import com.azure.resourcemanager.resources.fluent.inner.ProviderInner;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of {@link Deployment} and its nested interfaces.
 */
public final class DeploymentImpl extends
        CreatableUpdatableImpl<Deployment, DeploymentExtendedInner, DeploymentImpl>
        implements
        Deployment,
        Deployment.Definition,
        Deployment.Update,
        Deployment.Execution {

    private final ResourceManager resourceManager;
    private String resourceGroupName;
    private Creatable<ResourceGroup> creatableResourceGroup;
    private ObjectMapper objectMapper;
    private DeploymentWhatIf deploymentWhatIf;

    DeploymentImpl(DeploymentExtendedInner innerModel, String name, final ResourceManager resourceManager) {
        super(name, innerModel);
        this.resourceGroupName = ResourceUtils.groupFromResourceId(innerModel.id());
        this.resourceManager = resourceManager;
        this.objectMapper = new ObjectMapper();
        this.deploymentWhatIf = new DeploymentWhatIf();
    }

    @Override
    public String resourceGroupName() {
        return this.resourceGroupName;
    }

    @Override
    public String provisioningState() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().provisioningState();
    }

    @Override
    public String correlationId() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().correlationId();
    }

    @Override
    public OffsetDateTime timestamp() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().timestamp();
    }

    @Override
    public Object outputs() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().outputs();
    }

    @Override
    public List<Provider> providers() {
        if (this.inner().properties() == null) {
            return null;
        }
        List<Provider> providers = new ArrayList<>();
        for (ProviderInner providerInner : this.inner().properties().providers()) {
            providers.add(new ProviderImpl(providerInner));
        }
        return providers;
    }

    @Override
    public List<Dependency> dependencies() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().dependencies();
    }

    @Override
    public Object template() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().template();
    }

    @Override
    public TemplateLink templateLink() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().templateLink();
    }

    @Override
    public Object parameters() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().parameters();
    }

    @Override
    public ParametersLink parametersLink() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().parametersLink();
    }

    @Override
    public DeploymentMode mode() {
        if (this.inner().properties() == null) {
            return null;
        }
        return inner().properties().mode();
    }

    @Override
    public DeploymentOperations deploymentOperations() {
        return new DeploymentOperationsImpl(this.manager().inner().getDeploymentOperations(), this);
    }

    @Override
    public void cancel() {
        this.cancelAsync().block();
    }

    @Override
    public Mono<Void> cancelAsync() {
        return this.manager().inner().getDeployments().cancelAsync(resourceGroupName, name());
    }


    @Override
    public DeploymentExportResult exportTemplate() {
        return this.exportTemplateAsync().block();
    }

    @Override
    public Mono<DeploymentExportResult> exportTemplateAsync() {
        return this.manager().inner().getDeployments().exportTemplateAsync(resourceGroupName(), name())
            .map(deploymentExportResultInner -> new DeploymentExportResultImpl(deploymentExportResultInner));
    }

    @Override
    public DeploymentImpl prepareWhatIf() {
        return this;
    }

    // Withers

    @Override
    public DeploymentImpl withNewResourceGroup(String resourceGroupName, Region region) {
        this.creatableResourceGroup = this.resourceManager.resourceGroups()
                .define(resourceGroupName)
                .withRegion(region);
        this.addDependency(this.creatableResourceGroup);
        this.resourceGroupName = resourceGroupName;
        return this;
    }

    @Override
    public DeploymentImpl withNewResourceGroup(Creatable<ResourceGroup> resourceGroupDefinition) {
        this.resourceGroupName = resourceGroupDefinition.name();
        this.addDependency(resourceGroupDefinition);
        this.creatableResourceGroup = resourceGroupDefinition;
        return this;
    }

    @Override
    public DeploymentImpl withExistingResourceGroup(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
        return this;
    }

    @Override
    public DeploymentImpl withExistingResourceGroup(ResourceGroup resourceGroup) {
        this.resourceGroupName = resourceGroup.name();
        return this;
    }

    @Override
    public DeploymentImpl withTemplate(Object template) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().withTemplate(template);
        this.inner().properties().withTemplateLink(null);
        return this;
    }

    @Override
    public DeploymentImpl withTemplate(String templateJson) throws IOException {
        return withTemplate(objectMapper.readTree(templateJson));
    }

    @Override
    public DeploymentImpl withTemplateLink(String uri, String contentVersion) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().withTemplateLink(new TemplateLink().withUri(uri).withContentVersion(contentVersion));
        this.inner().properties().withTemplate(null);
        return this;
    }

    @Override
    public DeploymentImpl withMode(DeploymentMode mode) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().withMode(mode);
        return this;
    }

    @Override
    public DeploymentImpl withParameters(Object parameters) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().withParameters(parameters);
        this.inner().properties().withParametersLink(null);
        return this;
    }

    @Override
    public DeploymentImpl withParameters(String parametersJson) throws IOException {
        return withParameters(objectMapper.readTree(parametersJson));
    }

    @Override
    public DeploymentImpl withParametersLink(String uri, String contentVersion) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().withParametersLink(
            new ParametersLink().withUri(uri).withContentVersion(contentVersion));
        this.inner().properties().withParameters(null);
        return this;
    }

    private DeploymentInner createRequestFromInner() {
        DeploymentInner inner = new DeploymentInner()
                .withProperties(new DeploymentProperties());
        inner.properties().withMode(mode());
        inner.properties().withTemplate(template());
        inner.properties().withTemplateLink(templateLink());
        inner.properties().withParameters(parameters());
        inner.properties().withParametersLink(parametersLink());
        return inner;
    }

    @Override
    public DeploymentImpl beginCreate() {
        if (this.creatableResourceGroup != null) {
            this.creatableResourceGroup.create();
        }
        setInner(this.manager().inner().getDeployments()
            .beginCreateOrUpdateWithoutPolling(resourceGroupName(), name(), createRequestFromInner()));
        return this;
    }

    @Override
    public Mono<Deployment> beginCreateAsync() {
        return Mono.just(creatableResourceGroup)
                .flatMap(resourceGroupCreatable -> {
                    if (resourceGroupCreatable != null) {
                        return creatableResourceGroup.createAsync().last();
                    } else {
                        return Mono.just((Indexable) DeploymentImpl.this);
                    }
                })
                .flatMap(indexable -> manager().inner().getDeployments()
                    .beginCreateOrUpdateWithoutPollingAsync(resourceGroupName(), name(), createRequestFromInner()))
                .map(innerToFluentMap(this));
    }

    @Override
    public Mono<Deployment> createResourceAsync() {
        return this.manager().inner().getDeployments()
            .createOrUpdateAsync(resourceGroupName(), name(), createRequestFromInner())
            .map(innerToFluentMap(this));
    }

    @Override
    public Mono<Deployment> applyAsync() {
        return updateResourceAsync();
    }

    @Override
    public Mono<Deployment> updateResourceAsync() {
        try {
            if (this.templateLink() != null && this.template() != null) {
                this.withTemplate(null);
            }
            if (this.parametersLink() != null && this.parameters() != null) {
                this.withParameters(null);
            }
        } catch (IOException e) {
            return Mono.error(e);
        }
        return createResourceAsync();
    }

    @Override
    protected Mono<DeploymentExtendedInner> getInnerAsync() {
        return this.manager().inner().getDeployments().getAtManagementGroupScopeAsync(resourceGroupName(), name());
    }

    @Override
    public boolean isInCreateMode() {
        return this.inner().id() == null;
    }

    @Override
    public ResourceManager manager() {
        return this.resourceManager;
    }

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public DeploymentImpl withDetailedLevel(String detailedLevel) {
        if (deploymentWhatIf.properties() == null) {
            deploymentWhatIf.withProperties(new DeploymentWhatIfProperties());
        }
        deploymentWhatIf.properties().withDebugSetting(new DebugSetting().withDetailLevel(detailedLevel));
        return this;
    }

    @Override
    public DeploymentImpl withDeploymentName(String deploymentName) {
        if (deploymentWhatIf.properties() == null) {
            deploymentWhatIf.withProperties(new DeploymentWhatIfProperties());
        }
        if (deploymentWhatIf.properties().onErrorDeployment() == null) {
            deploymentWhatIf.properties().withOnErrorDeployment(new OnErrorDeployment());
        }
        deploymentWhatIf.properties().onErrorDeployment().withDeploymentName(deploymentName);
        return this;
    }

    @Override
    public DeploymentImpl withLocation(String location) {
        this.deploymentWhatIf.withLocation(location);
        return this;
    }

    @Override
    public DeploymentImpl withIncrementalMode() {
        if (deploymentWhatIf.properties() == null) {
            deploymentWhatIf.withProperties(new DeploymentWhatIfProperties());
        }
        deploymentWhatIf.properties().withMode(DeploymentMode.INCREMENTAL);
        return this;
    }

    @Override
    public DeploymentImpl withCompleteMode() {
        if (deploymentWhatIf.properties() == null) {
            deploymentWhatIf.withProperties(new DeploymentWhatIfProperties());
        }
        deploymentWhatIf.properties().withMode(DeploymentMode.COMPLETE);
        return this;
    }

    @Override
    public DeploymentImpl withFullResourcePayloadsResultFormat() {
        if (deploymentWhatIf.properties() == null) {
            deploymentWhatIf.withProperties(new DeploymentWhatIfProperties());
        }
        if (deploymentWhatIf.properties().whatIfSettings() == null) {
            deploymentWhatIf.properties().withWhatIfSettings(new DeploymentWhatIfSettings());
        }
        deploymentWhatIf.properties().whatIfSettings().withResultFormat(WhatIfResultFormat.FULL_RESOURCE_PAYLOADS);
        return this;
    }

    @Override
    public DeploymentImpl withResourceIdOnlyResultFormat() {
        if (deploymentWhatIf.properties() == null) {
            deploymentWhatIf.withProperties(new DeploymentWhatIfProperties());
        }
        if (deploymentWhatIf.properties().whatIfSettings() == null) {
            deploymentWhatIf.properties().withWhatIfSettings(new DeploymentWhatIfSettings());
        }
        deploymentWhatIf.properties().whatIfSettings().withResultFormat(WhatIfResultFormat.RESOURCE_ID_ONLY);
        return this;
    }

    @Override
    public DeploymentImpl withLastSuccessfulOnErrorDeployment() {
        if (deploymentWhatIf.properties() == null) {
            deploymentWhatIf.withProperties(new DeploymentWhatIfProperties());
        }
        if (deploymentWhatIf.properties().onErrorDeployment() == null) {
            deploymentWhatIf.properties().withOnErrorDeployment(new OnErrorDeployment());
        }
        deploymentWhatIf.properties().onErrorDeployment().withType(OnErrorDeploymentType.LAST_SUCCESSFUL);
        return this;
    }

    @Override
    public DeploymentImpl withSpecialDeploymentOnErrorDeployment() {
        if (deploymentWhatIf.properties() == null) {
            deploymentWhatIf.withProperties(new DeploymentWhatIfProperties());
        }
        if (deploymentWhatIf.properties().onErrorDeployment() == null) {
            deploymentWhatIf.properties().withOnErrorDeployment(new OnErrorDeployment());
        }
        deploymentWhatIf.properties().onErrorDeployment().withType(OnErrorDeploymentType.SPECIFIC_DEPLOYMENT);
        return this;
    }

    @Override
    public DeploymentImpl withWhatIfTemplate(Object template) {
        if (deploymentWhatIf.properties() == null) {
            deploymentWhatIf.withProperties(new DeploymentWhatIfProperties());
        }
        deploymentWhatIf.properties().withTemplate(template);
        return this;
    }

    @Override
    public DeploymentImpl withWhatIfTemplateLink(String uri, String contentVersion) {
        if (deploymentWhatIf.properties() == null) {
            deploymentWhatIf.withProperties(new DeploymentWhatIfProperties());
        }
        deploymentWhatIf.properties().withTemplateLink(
            new TemplateLink().withUri(uri).withContentVersion(contentVersion));
        return this;
    }

    @Override
    public DeploymentImpl withWhatIfParameters(Object parameters) {
        if (deploymentWhatIf.properties() == null) {
            deploymentWhatIf.withProperties(new DeploymentWhatIfProperties());
        }
        deploymentWhatIf.properties().withParameters(parameters);
        return this;
    }

    @Override
    public DeploymentImpl withWhatIfParametersLink(String uri, String contentVersion) {
        if (deploymentWhatIf.properties() == null) {
            deploymentWhatIf.withProperties(new DeploymentWhatIfProperties());
        }
        deploymentWhatIf.properties().withParametersLink(
            new ParametersLink().withUri(uri).withContentVersion(contentVersion));
        return this;
    }

    @Override
    public WhatIfOperationResult whatIf() {
        return this.whatIfAsync().block();
    }

    @Override
    public Mono<WhatIfOperationResult> whatIfAsync() {
        return this.manager().inner().getDeployments().whatIfAsync(resourceGroupName(), name(), deploymentWhatIf)
                .map(whatIfOperationResultInner -> new WhatIfOperationResultImpl(whatIfOperationResultInner));
    }


    @Override
    public WhatIfOperationResult whatIfAtSubscriptionScope() {
        return this.whatIfAtSubscriptionScopeAsync().block();
    }

    @Override
    public Mono<WhatIfOperationResult> whatIfAtSubscriptionScopeAsync() {
        return this.manager().inner().getDeployments().whatIfAtSubscriptionScopeAsync(name(), deploymentWhatIf)
                .map(whatIfOperationResultInner -> new WhatIfOperationResultImpl(whatIfOperationResultInner));
    }
}
