// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.management.exception.ManagementError;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.AcceptedImpl;
import com.azure.resourcemanager.resources.models.DebugSetting;
import com.azure.resourcemanager.resources.models.Dependency;
import com.azure.resourcemanager.resources.models.Deployment;
import com.azure.resourcemanager.resources.models.DeploymentExportResult;
import com.azure.resourcemanager.resources.models.DeploymentMode;
import com.azure.resourcemanager.resources.models.DeploymentOperations;
import com.azure.resourcemanager.resources.models.DeploymentProperties;
import com.azure.resourcemanager.resources.models.DeploymentWhatIf;
import com.azure.resourcemanager.resources.models.DeploymentWhatIfProperties;
import com.azure.resourcemanager.resources.models.DeploymentWhatIfSettings;
import com.azure.resourcemanager.resources.models.OnErrorDeployment;
import com.azure.resourcemanager.resources.models.OnErrorDeploymentType;
import com.azure.resourcemanager.resources.models.ParametersLink;
import com.azure.resourcemanager.resources.models.Provider;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.models.ResourceReference;
import com.azure.resourcemanager.resources.models.TemplateLink;
import com.azure.resourcemanager.resources.models.WhatIfOperationResult;
import com.azure.resourcemanager.resources.models.WhatIfResultFormat;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.fluent.models.DeploymentExtendedInner;
import com.azure.resourcemanager.resources.fluent.models.DeploymentInner;
import com.azure.resourcemanager.resources.fluent.models.ProviderInner;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    private final ClientLogger logger = new ClientLogger(DeploymentImpl.class);

    private final ResourceManager resourceManager;
    private String resourceGroupName;
    private Creatable<ResourceGroup> creatableResourceGroup;
    private ObjectMapper objectMapper;
    private DeploymentWhatIf deploymentWhatIf;
    private DeploymentInner deploymentCreateUpdateParameters;

    DeploymentImpl(DeploymentExtendedInner innerModel, String name, final ResourceManager resourceManager) {
        super(name, innerModel);
        this.resourceGroupName = ResourceUtils.groupFromResourceId(innerModel.id());
        this.resourceManager = resourceManager;
        this.objectMapper = new ObjectMapper();
        this.deploymentWhatIf = new DeploymentWhatIf();
        this.deploymentCreateUpdateParameters = new DeploymentInner();
    }

    @Override
    public String resourceGroupName() {
        return this.resourceGroupName;
    }

    @Override
    public String provisioningState() {
        if (this.innerModel().properties() == null) {
            return null;
        }
        return this.innerModel().properties().provisioningState().toString();
    }

    @Override
    public String correlationId() {
        if (this.innerModel().properties() == null) {
            return null;
        }
        return this.innerModel().properties().correlationId();
    }

    @Override
    public OffsetDateTime timestamp() {
        if (this.innerModel().properties() == null) {
            return null;
        }
        return this.innerModel().properties().timestamp();
    }

    @Override
    public Object outputs() {
        if (this.innerModel().properties() == null) {
            return null;
        }
        return this.innerModel().properties().outputs();
    }

    @Override
    public List<Provider> providers() {
        if (this.innerModel().properties() == null) {
            return null;
        }
        List<Provider> providers = new ArrayList<>();
        for (ProviderInner providerInner : this.innerModel().properties().providers()) {
            providers.add(new ProviderImpl(providerInner));
        }
        return providers;
    }

    @Override
    public List<Dependency> dependencies() {
        if (this.innerModel().properties() == null) {
            return null;
        }
        return this.innerModel().properties().dependencies();
    }

    @Override
    public String templateHash() {
        if (this.innerModel().properties() == null) {
            return null;
        }
        return this.innerModel().properties().templateHash();
    }

    @Override
    public TemplateLink templateLink() {
        if (this.innerModel().properties() == null) {
            return null;
        }
        return this.innerModel().properties().templateLink();
    }

    @Override
    public Object parameters() {
        if (this.innerModel().properties() == null) {
            return null;
        }
        return this.innerModel().properties().parameters();
    }

    @Override
    public ParametersLink parametersLink() {
        if (this.innerModel().properties() == null) {
            return null;
        }
        return this.innerModel().properties().parametersLink();
    }

    @Override
    public DeploymentMode mode() {
        if (this.innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().mode();
    }

    @Override
    public ManagementError error() {
        if (this.innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().error();
    }

    @Override
    public List<ResourceReference> outputResources() {
        if (this.innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().outputResources();
    }

    @Override
    public DeploymentOperations deploymentOperations() {
        return new DeploymentOperationsImpl(this.manager().serviceClient().getDeploymentOperations(), this);
    }

    @Override
    public void cancel() {
        this.cancelAsync().block();
    }

    @Override
    public Mono<Void> cancelAsync() {
        return this.manager().serviceClient().getDeployments().cancelAsync(resourceGroupName, name());
    }


    @Override
    public DeploymentExportResult exportTemplate() {
        return this.exportTemplateAsync().block();
    }

    @Override
    public Mono<DeploymentExportResult> exportTemplateAsync() {
        return this.manager().serviceClient().getDeployments().exportTemplateAsync(resourceGroupName(), name())
            .map(DeploymentExportResultImpl::new);
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
        if (this.deploymentCreateUpdateParameters.properties() == null) {
            this.deploymentCreateUpdateParameters.withProperties(new DeploymentProperties());
        }
        this.deploymentCreateUpdateParameters.properties().withTemplate(template);
        this.deploymentCreateUpdateParameters.properties().withTemplateLink(null);
        return this;
    }

    @Override
    public DeploymentImpl withTemplate(String templateJson) throws IOException {
        return withTemplate(objectMapper.readTree(templateJson));
    }

    @Override
    public DeploymentImpl withTemplateLink(String uri, String contentVersion) {
        if (this.deploymentCreateUpdateParameters.properties() == null) {
            this.deploymentCreateUpdateParameters.withProperties(new DeploymentProperties());
        }
        this.deploymentCreateUpdateParameters.properties().withTemplateLink(
            new TemplateLink().withUri(uri).withContentVersion(contentVersion));
        this.deploymentCreateUpdateParameters.properties().withTemplate(null);
        return this;
    }

    @Override
    public DeploymentImpl withMode(DeploymentMode mode) {
        if (this.deploymentCreateUpdateParameters.properties() == null) {
            this.deploymentCreateUpdateParameters.withProperties(new DeploymentProperties());
        }
        this.deploymentCreateUpdateParameters.properties().withMode(mode);
        return this;
    }

    @Override
    public DeploymentImpl withParameters(Object parameters) {
        if (this.deploymentCreateUpdateParameters.properties() == null) {
            this.deploymentCreateUpdateParameters.withProperties(new DeploymentProperties());
        }
        this.deploymentCreateUpdateParameters.properties().withParameters(parameters);
        this.deploymentCreateUpdateParameters.properties().withParametersLink(null);
        return this;
    }

    @Override
    public DeploymentImpl withParameters(String parametersJson) throws IOException {
        return withParameters(objectMapper.readTree(parametersJson));
    }

    @Override
    public DeploymentImpl withParametersLink(String uri, String contentVersion) {
        if (this.deploymentCreateUpdateParameters.properties() == null) {
            this.deploymentCreateUpdateParameters.withProperties(new DeploymentProperties());
        }
        this.deploymentCreateUpdateParameters.properties().withParametersLink(
            new ParametersLink().withUri(uri).withContentVersion(contentVersion));
        this.deploymentCreateUpdateParameters.properties().withParameters(null);
        return this;
    }

    @Override
    public Accepted<Deployment> beginCreate() {
        return AcceptedImpl.newAccepted(logger,
            this.manager().serviceClient().getHttpPipeline(),
            this.manager().serviceClient().getDefaultPollInterval(),
            () -> this.manager().serviceClient().getDeployments()
                .createOrUpdateWithResponseAsync(resourceGroupName(), name(), deploymentCreateUpdateParameters).block(),
            inner -> new DeploymentImpl(inner, inner.name(), resourceManager),
            DeploymentExtendedInner.class,
            () -> {
                if (this.creatableResourceGroup != null) {
                    this.creatableResourceGroup.create();
                }
            },
            inner -> {
                setInner(inner);
                prepareForUpdate(inner);
            });
    }

    @Override
    public Mono<Deployment> beginCreateAsync() {
        return Mono.just(creatableResourceGroup)
                .flatMap(resourceGroupCreatable -> {
                    if (resourceGroupCreatable != null) {
                        return creatableResourceGroup.createAsync();
                    } else {
                        return Mono.just((Indexable) DeploymentImpl.this);
                    }
                })
                .flatMap(indexable -> manager().serviceClient().getDeployments()
                    .createOrUpdateWithResponseAsync(resourceGroupName(), name(), deploymentCreateUpdateParameters))
                .flatMap(activationResponse -> FluxUtil.collectBytesInByteBufferStream(activationResponse.getValue()))
                .map(response -> {
                    try {
                        return (DeploymentExtendedInner) SerializerFactory.createDefaultManagementSerializerAdapter()
                            .deserialize(new String(response, StandardCharsets.UTF_8),
                                DeploymentExtendedInner.class, SerializerEncoding.JSON);
                    } catch (IOException ioe) {
                        throw logger.logExceptionAsError(
                            new IllegalStateException("Failed to deserialize activation response body", ioe));
                    }
                })
                .map(deploymentExtendedInner -> {
                    prepareForUpdate(deploymentExtendedInner);
                    return deploymentExtendedInner;
                })
                .map(innerToFluentMap(this));
    }

    @Override
    public Mono<Deployment> createResourceAsync() {
        return this.manager().serviceClient().getDeployments()
            .createOrUpdateAsync(resourceGroupName(), name(), deploymentCreateUpdateParameters)
            .map(deploymentExtendedInner -> {
                prepareForUpdate(deploymentExtendedInner);
                return deploymentExtendedInner;
            })
            .map(innerToFluentMap(this));
    }

    private void prepareForUpdate(DeploymentExtendedInner inner) {
        deploymentCreateUpdateParameters = new DeploymentInner();
        deploymentCreateUpdateParameters.withLocation(inner.location());
        deploymentCreateUpdateParameters.withTags(inner.tags());
        if (inner.properties() != null) {
            deploymentCreateUpdateParameters.withProperties(new DeploymentProperties());
            deploymentCreateUpdateParameters.properties().withDebugSetting(inner.properties().debugSetting());
            deploymentCreateUpdateParameters.properties().withMode(inner.properties().mode());
            deploymentCreateUpdateParameters.properties().withParameters(inner.properties().parameters());
            deploymentCreateUpdateParameters.properties().withParametersLink(inner.properties().parametersLink());
            deploymentCreateUpdateParameters.properties().withTemplateLink(inner.properties().templateLink());
            if (inner.properties().onErrorDeployment() != null) {
                deploymentCreateUpdateParameters.properties().withOnErrorDeployment(new OnErrorDeployment());
                deploymentCreateUpdateParameters.properties().onErrorDeployment().withDeploymentName(
                    inner.properties().onErrorDeployment().deploymentName());
                deploymentCreateUpdateParameters.properties().onErrorDeployment().withType(
                    inner.properties().onErrorDeployment().type());
            }
        }
    }

    @Override
    public Mono<Deployment> applyAsync() {
        return updateResourceAsync();
    }

    @Override
    protected Mono<DeploymentExtendedInner> getInnerAsync() {
        return this.manager().serviceClient().getDeployments()
            .getAtManagementGroupScopeAsync(resourceGroupName(), name());
    }

    @Override
    public boolean isInCreateMode() {
        return this.innerModel().id() == null;
    }

    @Override
    public ResourceManager manager() {
        return this.resourceManager;
    }

    @Override
    public String id() {
        return innerModel().id();
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
        return this.manager().serviceClient().getDeployments()
            .whatIfAsync(resourceGroupName(), name(), deploymentWhatIf)
            .map(WhatIfOperationResultImpl::new);
    }


    @Override
    public WhatIfOperationResult whatIfAtSubscriptionScope() {
        return this.whatIfAtSubscriptionScopeAsync().block();
    }

    @Override
    public Mono<WhatIfOperationResult> whatIfAtSubscriptionScopeAsync() {
        return this.manager().serviceClient().getDeployments().whatIfAtSubscriptionScopeAsync(name(), deploymentWhatIf)
            .map(WhatIfOperationResultImpl::new);
    }
}
