/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.implementation;

import com.azure.management.resources.DebugSetting;
import com.azure.management.resources.Dependency;
import com.azure.management.resources.Deployment;
import com.azure.management.resources.DeploymentExportResult;
import com.azure.management.resources.DeploymentMode;
import com.azure.management.resources.DeploymentOperations;
import com.azure.management.resources.DeploymentProperties;
import com.azure.management.resources.DeploymentPropertiesExtended;
import com.azure.management.resources.DeploymentWhatIf;
import com.azure.management.resources.DeploymentWhatIfProperties;
import com.azure.management.resources.DeploymentWhatIfSettings;
import com.azure.management.resources.OnErrorDeployment;
import com.azure.management.resources.OnErrorDeploymentType;
import com.azure.management.resources.ParametersLink;
import com.azure.management.resources.Provider;
import com.azure.management.resources.ResourceGroup;
import com.azure.management.resources.TemplateLink;
import com.azure.management.resources.WhatIfOperationResult;
import com.azure.management.resources.WhatIfResultFormat;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.model.Indexable;
import com.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.management.resources.models.DeploymentExtendedInner;
import com.azure.management.resources.models.DeploymentInner;
import com.azure.management.resources.models.ProviderInner;
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
        this.resourceGroupName = ResourceUtils.groupFromResourceId(innerModel.getId());
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
        if (this.inner().getProperties() == null) {
            return null;
        }
        return this.inner().getProperties().getProvisioningState();
    }

    @Override
    public String correlationId() {
        if (this.inner().getProperties() == null) {
            return null;
        }
        return this.inner().getProperties().getCorrelationId();
    }

    @Override
    public OffsetDateTime timestamp() {
        if (this.inner().getProperties() == null) {
            return null;
        }
        return this.inner().getProperties().getTimestamp();
    }

    @Override
    public Object outputs() {
        if (this.inner().getProperties() == null) {
            return null;
        }
        return this.inner().getProperties().getOutputs();
    }

    @Override
    public List<Provider> providers() {
        if (this.inner().getProperties() == null) {
            return null;
        }
        List<Provider> providers = new ArrayList<>();
        for (ProviderInner providerInner : this.inner().getProperties().getProviders()) {
            providers.add(new ProviderImpl(providerInner));
        }
        return providers;
    }

    @Override
    public List<Dependency> dependencies() {
        if (this.inner().getProperties() == null) {
            return null;
        }
        return this.inner().getProperties().getDependencies();
    }

    @Override
    public Object template() {
        if (this.inner().getProperties() == null) {
            return null;
        }
        return this.inner().getProperties().getTemplate();
    }

    @Override
    public TemplateLink templateLink() {
        if (this.inner().getProperties() == null) {
            return null;
        }
        return this.inner().getProperties().getTemplateLink();
    }

    @Override
    public Object parameters() {
        if (this.inner().getProperties() == null) {
            return null;
        }
        return this.inner().getProperties().getParameters();
    }

    @Override
    public ParametersLink parametersLink() {
        if (this.inner().getProperties() == null) {
            return null;
        }
        return this.inner().getProperties().getParametersLink();
    }

    @Override
    public DeploymentMode mode() {
        if (this.inner().getProperties() == null) {
            return null;
        }
        return inner().getProperties().getMode();
    }

    @Override
    public DeploymentOperations deploymentOperations() {
        return new DeploymentOperationsImpl(this.manager().inner().deploymentOperations(), this);
    }

    @Override
    public void cancel() {
        this.cancelAsync().block();
    }

    @Override
    public Mono<Void> cancelAsync() {
        return this.manager().inner().deployments().cancelAsync(resourceGroupName, name());
    }


    @Override
    public DeploymentExportResult exportTemplate() {
        return this.exportTemplateAsync().block();
    }

    @Override
    public Mono<DeploymentExportResult> exportTemplateAsync() {
        return this.manager().inner().deployments().exportTemplateAsync(resourceGroupName(), name()).map(deploymentExportResultInner -> new DeploymentExportResultImpl(deploymentExportResultInner));
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
        if (this.inner().getProperties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().getProperties().setTemplate(template);
        this.inner().getProperties().setTemplateLink(null);
        return this;
    }

    @Override
    public DeploymentImpl withTemplate(String templateJson) throws IOException {
        return withTemplate(objectMapper.readTree(templateJson));
    }

    @Override
    public DeploymentImpl withTemplateLink(String uri, String contentVersion) {
        if (this.inner().getProperties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().getProperties().setTemplateLink(new TemplateLink().setUri(uri).setContentVersion(contentVersion));
        this.inner().getProperties().setTemplate(null);
        return this;
    }

    @Override
    public DeploymentImpl withMode(DeploymentMode mode) {
        if (this.inner().getProperties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().getProperties().setMode(mode);
        return this;
    }

    @Override
    public DeploymentImpl withParameters(Object parameters) {
        if (this.inner().getProperties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().getProperties().setParameters(parameters);
        this.inner().getProperties().setParametersLink(null);
        return this;
    }

    @Override
    public DeploymentImpl withParameters(String parametersJson) throws IOException {
        return withParameters(objectMapper.readTree(parametersJson));
    }

    @Override
    public DeploymentImpl withParametersLink(String uri, String contentVersion) {
        if (this.inner().getProperties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().getProperties().setParametersLink(new ParametersLink().setUri(uri).setContentVersion(contentVersion));
        this.inner().getProperties().setParameters(null);
        return this;
    }

    private DeploymentInner createRequestFromInner() {
        DeploymentInner inner = new DeploymentInner()
                .setProperties(new DeploymentProperties());
        inner.getProperties().setMode(mode());
        inner.getProperties().setTemplate(template());
        inner.getProperties().setTemplateLink(templateLink());
        inner.getProperties().setParameters(parameters());
        inner.getProperties().setParametersLink(parametersLink());
        return inner;
    }

    @Override
    public DeploymentImpl beginCreate() {
        if (this.creatableResourceGroup != null) {
            this.creatableResourceGroup.create();
        }
        setInner(this.manager().inner().deployments().beginCreateOrUpdate(resourceGroupName(), name(), createRequestFromInner()));
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
                .flatMap(indexable -> manager().inner().deployments().beginCreateOrUpdateAsync(resourceGroupName(), name(), createRequestFromInner()))
                .map(innerToFluentMap(this));
    }

    @Override
    public Mono<Deployment> createResourceAsync() {
        return this.manager().inner().deployments().createOrUpdateAsync(resourceGroupName(), name(), createRequestFromInner())
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
        return this.manager().inner().deployments().getAtManagementGroupScopeAsync(resourceGroupName(), name());
    }

    @Override
    public boolean isInCreateMode() {
        return this.inner().getId() == null;
    }

    @Override
    public ResourceManager manager() {
        return this.resourceManager;
    }

    @Override
    public String id() {
        return inner().getId();
    }

    @Override
    public DeploymentImpl withDetailedLevel(String detailedLevel) {
        if (deploymentWhatIf.getProperties() == null) {
            deploymentWhatIf.setProperties(new DeploymentWhatIfProperties());
        }
        deploymentWhatIf.getProperties().setDebugSetting(new DebugSetting().setDetailLevel(detailedLevel));
        return this;
    }

    @Override
    public DeploymentImpl withDeploymentName(String deploymentName) {
        if (deploymentWhatIf.getProperties() == null) {
            deploymentWhatIf.setProperties(new DeploymentWhatIfProperties());
        }
        if (deploymentWhatIf.getProperties().getOnErrorDeployment() == null) {
            deploymentWhatIf.getProperties().setOnErrorDeployment(new OnErrorDeployment());
        }
        deploymentWhatIf.getProperties().getOnErrorDeployment().setDeploymentName(deploymentName);
        return this;
    }

    @Override
    public DeploymentImpl withLocation(String location) {
        this.deploymentWhatIf.setLocation(location);
        return this;
    }

    @Override
    public DeploymentImpl withIncrementalMode() {
        if (deploymentWhatIf.getProperties() == null) {
            deploymentWhatIf.setProperties(new DeploymentWhatIfProperties());
        }
        deploymentWhatIf.getProperties().setMode(DeploymentMode.INCREMENTAL);
        return this;
    }

    @Override
    public DeploymentImpl withCompleteMode() {
        if (deploymentWhatIf.getProperties() == null) {
            deploymentWhatIf.setProperties(new DeploymentWhatIfProperties());
        }
        deploymentWhatIf.getProperties().setMode(DeploymentMode.COMPLETE);
        return this;
    }

    @Override
    public DeploymentImpl withFullResourcePayloadsResultFormat() {
        if (deploymentWhatIf.getProperties() == null) {
            deploymentWhatIf.setProperties(new DeploymentWhatIfProperties());
        }
        if (deploymentWhatIf.getProperties().getWhatIfSettings() == null) {
            deploymentWhatIf.getProperties().setWhatIfSettings(new DeploymentWhatIfSettings());
        }
        deploymentWhatIf.getProperties().getWhatIfSettings().setResultFormat(WhatIfResultFormat.FULL_RESOURCE_PAYLOADS);
        return this;
    }

    @Override
    public DeploymentImpl withResourceIdOnlyResultFormat() {
        if (deploymentWhatIf.getProperties() == null) {
            deploymentWhatIf.setProperties(new DeploymentWhatIfProperties());
        }
        if (deploymentWhatIf.getProperties().getWhatIfSettings() == null) {
            deploymentWhatIf.getProperties().setWhatIfSettings(new DeploymentWhatIfSettings());
        }
        deploymentWhatIf.getProperties().getWhatIfSettings().setResultFormat(WhatIfResultFormat.RESOURCE_ID_ONLY);
        return this;
    }

    @Override
    public DeploymentImpl withLastSuccessfulOnErrorDeployment() {
        if (deploymentWhatIf.getProperties() == null) {
            deploymentWhatIf.setProperties(new DeploymentWhatIfProperties());
        }
        if (deploymentWhatIf.getProperties().getOnErrorDeployment() == null) {
            deploymentWhatIf.getProperties().setOnErrorDeployment(new OnErrorDeployment());
        }
        deploymentWhatIf.getProperties().getOnErrorDeployment().setType(OnErrorDeploymentType.LAST_SUCCESSFUL);
        return this;
    }

    @Override
    public DeploymentImpl withSpecialDeploymentOnErrorDeployment() {
        if (deploymentWhatIf.getProperties() == null) {
            deploymentWhatIf.setProperties(new DeploymentWhatIfProperties());
        }
        if (deploymentWhatIf.getProperties().getOnErrorDeployment() == null) {
            deploymentWhatIf.getProperties().setOnErrorDeployment(new OnErrorDeployment());
        }
        deploymentWhatIf.getProperties().getOnErrorDeployment().setType(OnErrorDeploymentType.SPECIFIC_DEPLOYMENT);
        return this;
    }

    @Override
    public DeploymentImpl withWhatIfTemplate(Object template) {
        if (deploymentWhatIf.getProperties() == null) {
            deploymentWhatIf.setProperties(new DeploymentWhatIfProperties());
        }
        deploymentWhatIf.getProperties().setTemplate(template);
        return this;
    }

    @Override
    public DeploymentImpl withWhatIfTemplateLink(String uri, String contentVersion) {
        if (deploymentWhatIf.getProperties() == null) {
            deploymentWhatIf.setProperties(new DeploymentWhatIfProperties());
        }
        deploymentWhatIf.getProperties().setTemplateLink(new TemplateLink().setUri(uri).setContentVersion(contentVersion));
        return this;
    }

    @Override
    public DeploymentImpl withWhatIfParameters(Object parameters) {
        if (deploymentWhatIf.getProperties() == null) {
            deploymentWhatIf.setProperties(new DeploymentWhatIfProperties());
        }
        deploymentWhatIf.getProperties().setParameters(parameters);
        return this;
    }

    @Override
    public DeploymentImpl withWhatIfParametersLink(String uri, String contentVersion) {
        if (deploymentWhatIf.getProperties() == null) {
            deploymentWhatIf.setProperties(new DeploymentWhatIfProperties());
        }
        deploymentWhatIf.getProperties().setParametersLink(new ParametersLink().setUri(uri).setContentVersion(contentVersion));
        return this;
    }

    @Override
    public WhatIfOperationResult whatIf() {
        return this.whatIfAsync().block();
    }

    @Override
    public Mono<WhatIfOperationResult> whatIfAsync() {
        return this.manager().inner().deployments().whatIfAsync(resourceGroupName(), name(), deploymentWhatIf)
                .map(whatIfOperationResultInner -> new WhatIfOperationResultImpl(whatIfOperationResultInner));
    }


    @Override
    public WhatIfOperationResult whatIfAtSubscriptionScope() {
        return this.whatIfAtSubscriptionScopeAsync().block();
    }

    @Override
    public Mono<WhatIfOperationResult> whatIfAtSubscriptionScopeAsync() {
        return this.manager().inner().deployments().whatIfAtSubscriptionScopeAsync(name(), deploymentWhatIf)
                .map(whatIfOperationResultInner -> new WhatIfOperationResultImpl(whatIfOperationResultInner));
    }
}
