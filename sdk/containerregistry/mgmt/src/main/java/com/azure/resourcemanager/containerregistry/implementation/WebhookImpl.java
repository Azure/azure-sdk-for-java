// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.containerregistry.ContainerRegistryManager;
import com.azure.resourcemanager.containerregistry.fluent.WebhooksClient;
import com.azure.resourcemanager.containerregistry.fluent.inner.CallbackConfigInner;
import com.azure.resourcemanager.containerregistry.fluent.inner.WebhookInner;
import com.azure.resourcemanager.containerregistry.models.ProvisioningState;
import com.azure.resourcemanager.containerregistry.models.Registry;
import com.azure.resourcemanager.containerregistry.models.Webhook;
import com.azure.resourcemanager.containerregistry.models.WebhookAction;
import com.azure.resourcemanager.containerregistry.models.WebhookCreateParameters;
import com.azure.resourcemanager.containerregistry.models.WebhookEventInfo;
import com.azure.resourcemanager.containerregistry.models.WebhookStatus;
import com.azure.resourcemanager.containerregistry.models.WebhookUpdateParameters;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/** Implementation for Webhook. */
public class WebhookImpl extends ExternalChildResourceImpl<Webhook, WebhookInner, RegistryImpl, Registry>
    implements Webhook,
        Webhook.WebhookDefinition<Registry.DefinitionStages.WithCreate>,
        Webhook.UpdateDefinition<Registry.Update>,
        Webhook.UpdateResource<Registry.Update>,
        Webhook.Update {

    private WebhookCreateParameters webhookCreateParametersInner;
    private WebhookUpdateParameters webhookUpdateParametersInner;

    private Map<String, String> tags;
    private Map<String, String> customHeaders;
    private String serviceUri;
    private boolean isInCreateMode;

    private ContainerRegistryManager containerRegistryManager;
    private String resourceGroupName;
    private String registryName;

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name the name of this external child resource
     * @param parent reference to the parent of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param containerRegistryManager reference to the container registry manager that accesses web hook operations
     */
    WebhookImpl(
        String name, RegistryImpl parent, WebhookInner innerObject, ContainerRegistryManager containerRegistryManager) {
        super(name, parent, innerObject);
        this.containerRegistryManager = containerRegistryManager;
        if (parent != null) {
            this.resourceGroupName = parent.resourceGroupName();
            this.registryName = parent.name();
        }

        this.initCreateUpdateParams();
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param resourceGroupName the resource group name
     * @param registryName the registry name
     * @param name the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param containerRegistryManager reference to the container registry manager that accesses web hook operations
     */
    WebhookImpl(
        String resourceGroupName,
        String registryName,
        String name,
        WebhookInner innerObject,
        ContainerRegistryManager containerRegistryManager) {
        super(name, null, innerObject);
        this.containerRegistryManager = containerRegistryManager;
        this.resourceGroupName = resourceGroupName;
        this.registryName = registryName;

        this.initCreateUpdateParams();
    }

    private void initCreateUpdateParams() {
        this.webhookCreateParametersInner = null;
        this.webhookUpdateParametersInner = null;
        this.isInCreateMode = false;
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String type() {
        return this.inner().type();
    }

    @Override
    public String regionName() {
        return this.inner().location();
    }

    @Override
    public Region region() {
        return Region.findByLabelOrName(this.regionName());
    }

    @Override
    public Map<String, String> tags() {
        Map<String, String> tags = this.inner().tags();
        if (tags == null) {
            tags = new TreeMap<>();
        }
        return Collections.unmodifiableMap(tags);
    }

    @Override
    public boolean isEnabled() {
        return this.inner().status().equals(WebhookStatus.ENABLED);
    }

    @Override
    public String scope() {
        return this.inner().scope();
    }

    @Override
    public String serviceUri() {
        return this.serviceUri;
    }

    @Override
    public Map<String, String> customHeaders() {
        return Collections.unmodifiableMap(this.customHeaders);
    }

    @Override
    public Collection<WebhookAction> triggers() {
        return Collections.unmodifiableCollection(this.inner().actions());
    }

    @Override
    public ProvisioningState provisioningState() {
        return this.inner().provisioningState();
    }

    @Override
    public String parentId() {
        return ResourceUtils.parentResourceIdFromResourceId(this.id());
    }

    @Override
    public void enable() {
        this.update().enabled(true).apply();
    }

    @Override
    public Mono<Void> enableAsync() {
        return this.update().enabled(true).applyAsync().then(Mono.empty());
    }

    @Override
    public void disable() {
        this.update().enabled(false).apply();
    }

    @Override
    public Mono<Void> disableAsync() {
        return this.update().enabled(false).applyAsync().then(Mono.empty());
    }

    @Override
    public String ping() {
        return this
            .containerRegistryManager
            .inner()
            .getWebhooks()
            .ping(this.resourceGroupName, this.registryName, name())
            .id();
    }

    @Override
    public Mono<String> pingAsync() {
        return this
            .containerRegistryManager
            .inner()
            .getWebhooks()
            .pingAsync(this.resourceGroupName, this.registryName, name())
            .map(eventInfoInner -> eventInfoInner.id());
    }

    @Override
    public PagedIterable<WebhookEventInfo> listEvents() {
        return new PagedIterable<>(this.listEventsAsync());
    }

    @Override
    public PagedFlux<WebhookEventInfo> listEventsAsync() {
        final WebhookImpl self = this;

        return this
            .containerRegistryManager
            .inner()
            .getWebhooks()
            .listEventsAsync(self.resourceGroupName, self.registryName, self.name())
            .mapPage(inner -> new WebhookEventInfoImpl(inner));
    }

    @Override
    public Mono<Webhook> createResourceAsync() {
        final WebhookImpl self = this;
        if (webhookCreateParametersInner != null) {
            return this
                .containerRegistryManager
                .inner()
                .getWebhooks()
                .createAsync(self.resourceGroupName, this.registryName, this.name(), this.webhookCreateParametersInner)
                .map(
                    inner -> {
                        self.webhookCreateParametersInner = null;
                        self.setInner(inner);
                        return self;
                    })
                .flatMap(webhook -> self.setCallbackConfigAsync());
        } else {
            return Mono.just(this);
        }
    }

    WebhookImpl setCallbackConfig(CallbackConfigInner callbackConfigInner) {
        this.serviceUri = callbackConfigInner.serviceUri();
        this.customHeaders =
            callbackConfigInner.customHeaders() != null
                ? callbackConfigInner.customHeaders()
                : new HashMap<String, String>();
        return this;
    }

    Mono<Webhook> setCallbackConfigAsync() {
        final WebhookImpl self = this;

        return this
            .containerRegistryManager
            .inner()
            .getWebhooks()
            .getCallbackConfigAsync(self.resourceGroupName, self.registryName, self.name())
            .map(
                callbackConfigInner -> {
                    setCallbackConfig(callbackConfigInner);
                    return self;
                });
    }

    @Override
    public Mono<Webhook> updateResourceAsync() {
        final WebhookImpl self = this;
        if (webhookUpdateParametersInner != null) {
            return this
                .containerRegistryManager
                .inner()
                .getWebhooks()
                .updateAsync(self.resourceGroupName, self.registryName, self.name(), self.webhookUpdateParametersInner)
                .map(
                    inner -> {
                        self.setInner(inner);
                        self.webhookUpdateParametersInner = null;
                        return self;
                    })
                .flatMap(webhook -> self.setCallbackConfigAsync());
        } else {
            return Mono.just(this);
        }
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this
            .containerRegistryManager
            .inner()
            .getWebhooks()
            .deleteAsync(this.resourceGroupName, this.registryName, this.name());
    }

    @Override
    protected Mono<WebhookInner> getInnerAsync() {
        final WebhookImpl self = this;
        final WebhooksClient webhooksInner = this.containerRegistryManager.inner().getWebhooks();
        return webhooksInner
            .getAsync(this.resourceGroupName, this.registryName, this.name())
            .flatMap(
                webhookInner -> {
                    self.setInner(webhookInner);
                    return webhooksInner.getCallbackConfigAsync(self.resourceGroupName, self.registryName, self.name());
                })
            .map(callbackConfigInner -> setCallbackConfig(callbackConfigInner).inner());
    }

    @Override
    public Webhook apply() {
        return this.applyAsync().block();
    }

    @Override
    public Mono<Webhook> applyAsync() {
        return this.updateResourceAsync();
    }

    @Override
    public WebhookImpl update() {
        setCreateMode(false);

        return this;
    }

    @Override
    public RegistryImpl attach() {
        return this.parent();
    }

    WebhookImpl setCreateMode(boolean isInCreateMode) {
        this.isInCreateMode = isInCreateMode;

        if (this.isInCreateMode && parent() != null) {
            this.webhookCreateParametersInner = new WebhookCreateParameters().withLocation(parent().regionName());
        } else {
            this.webhookUpdateParametersInner = new WebhookUpdateParameters();
        }

        return this;
    }

    @Override
    public WebhookImpl withTags(Map<String, String> tags) {
        if (tags != null) {
            this.tags = null;
            ensureValidTags();
            for (Map.Entry<String, String> entry : inner().tags().entrySet()) {
                this.tags.put(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    public WebhookImpl withTag(String key, String value) {
        if (key != null && value != null) {
            ensureValidTags().put(key, value);
        }
        return this;
    }

    @Override
    public WebhookImpl withoutTag(String key) {
        if (key != null && this.tags != null) {
            this.tags.remove(key);
        }
        return this;
    }

    @Override
    public WebhookImpl withTriggerWhen(WebhookAction... webhookActions) {
        if (webhookActions != null) {
            if (this.isInCreateMode) {
                ensureWebhookCreateParametersInner().withActions(Arrays.asList(webhookActions));
            } else {
                ensureWebhookUpdateParametersInner().withActions(Arrays.asList(webhookActions));
            }
        }
        return this;
    }

    @Override
    public WebhookImpl withServiceUri(String serviceUri) {
        if (serviceUri != null) {
            if (this.isInCreateMode) {
                ensureWebhookCreateParametersInner().withServiceUri(serviceUri);
            } else {
                ensureWebhookUpdateParametersInner().withServiceUri(serviceUri);
            }
        }
        return this;
    }

    @Override
    public WebhookImpl withCustomHeader(String name, String value) {
        if (name != null && value != null) {
            ensureValidCustomHeaders().put(name, value);
        }
        return this;
    }

    @Override
    public WebhookImpl withCustomHeaders(Map<String, String> customHeaders) {
        if (customHeaders != null) {
            this.customHeaders = null;
            ensureValidCustomHeaders();
            for (Map.Entry<String, String> entry : inner().tags().entrySet()) {
                this.customHeaders.put(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    public WebhookImpl withRepositoriesScope(String repositoriesScope) {
        if (repositoriesScope != null) {
            if (this.isInCreateMode) {
                ensureWebhookCreateParametersInner().withScope(repositoriesScope);
            } else {
                ensureWebhookUpdateParametersInner().withScope(repositoriesScope);
            }
        }
        return this;
    }

    @Override
    public WebhookImpl enabled(boolean defaultStatus) {
        WebhookStatus status = defaultStatus ? WebhookStatus.ENABLED : WebhookStatus.DISABLED;
        if (this.isInCreateMode) {
            ensureWebhookCreateParametersInner().withStatus(status);
        } else {
            ensureWebhookUpdateParametersInner().withStatus(status);
        }
        return this;
    }

    private WebhookCreateParameters ensureWebhookCreateParametersInner() {
        if (this.webhookCreateParametersInner == null && parent() != null) {
            this.webhookCreateParametersInner = new WebhookCreateParameters().withLocation(parent().regionName());
        }
        return this.webhookCreateParametersInner;
    }

    private WebhookUpdateParameters ensureWebhookUpdateParametersInner() {
        if (this.webhookUpdateParametersInner == null && parent() != null) {
            this.webhookUpdateParametersInner = new WebhookUpdateParameters();
        }
        return this.webhookUpdateParametersInner;
    }

    private Map<String, String> ensureValidTags() {
        if (this.tags == null) {
            this.tags = new HashMap<>();
            if (this.isInCreateMode) {
                this.ensureWebhookCreateParametersInner().withTags(this.tags);
            } else {
                this.ensureWebhookUpdateParametersInner().withTags(this.tags);
            }
        }
        return this.tags;
    }

    private Map<String, String> ensureValidCustomHeaders() {
        if (this.customHeaders == null) {
            this.customHeaders = new HashMap<>();
            if (this.isInCreateMode) {
                this.ensureWebhookCreateParametersInner().withCustomHeaders(this.customHeaders);
            } else {
                this.ensureWebhookUpdateParametersInner().withCustomHeaders(this.customHeaders);
            }
        }
        return this.customHeaders;
    }
}
