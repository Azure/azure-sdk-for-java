/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerregistry.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.containerregistry.ProvisioningState;
import com.microsoft.azure.management.containerregistry.Registry;
import com.microsoft.azure.management.containerregistry.Webhook;
import com.microsoft.azure.management.containerregistry.WebhookAction;
import com.microsoft.azure.management.containerregistry.WebhookEventInfo;
import com.microsoft.azure.management.containerregistry.WebhookStatus;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation for Webhook.
 */
@LangDefinition
public class WebhookImpl
    extends ExternalChildResourceImpl<Webhook, WebhookInner, RegistryImpl, Registry>
    implements
        Webhook,
        Webhook.WebhookDefinition<Registry.DefinitionStages.WithCreate>,
        Webhook.UpdateDefinition<Registry.Update>,
        Webhook.UpdateResource<Registry.Update>,
        Webhook.Update {

    private WebhookCreateParametersInner webhookCreateParametersInner;
    private WebhookUpdateParametersInner webhookUpdateParametersInner;

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
     * @param name        the name of this external child resource
     * @param parent      reference to the parent of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param containerRegistryManager reference to the container registry manager that accesses web hook operations
     */
    WebhookImpl(String name, RegistryImpl parent, WebhookInner innerObject, ContainerRegistryManager containerRegistryManager) {
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
     * @param name        the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param containerRegistryManager reference to the container registry manager that accesses web hook operations
     */
    WebhookImpl(String resourceGroupName, String registryName, String name, WebhookInner innerObject, ContainerRegistryManager containerRegistryManager) {
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
        Map<String, String> tags = this.inner().getTags();
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
        this.update()
            .withDefaultStatus(WebhookStatus.ENABLED)
            .apply();
    }

    @Override
    public Completable enableAsync() {
        return this.update()
            .withDefaultStatus(WebhookStatus.ENABLED)
            .applyAsync().toCompletable();
    }

    @Override
    public void disable() {
        this.update()
            .withDefaultStatus(WebhookStatus.DISABLED)
            .apply();
    }

    @Override
    public Completable disableAsync() {
        return this.update()
            .withDefaultStatus(WebhookStatus.DISABLED)
            .applyAsync().toCompletable();
    }

    @Override
    public String ping() {
        return this.containerRegistryManager.inner().webhooks()
            .ping(this.resourceGroupName, this.registryName, name()).id();
    }

    @Override
    public Observable<String> pingAsync() {
        return this.containerRegistryManager.inner().webhooks()
            .pingAsync(this.resourceGroupName, this.registryName, name())
            .map(new Func1<EventInfoInner, String>() {
                @Override
                public String call(EventInfoInner eventInfoInner) {
                    return eventInfoInner.id();
                }
            });
    }

    @Override
    public PagedList<WebhookEventInfo> listEvents() {
        final WebhookImpl self = this;
        final PagedListConverter<EventInner, WebhookEventInfo> converter = new PagedListConverter<EventInner, WebhookEventInfo>() {
            @Override
            public WebhookEventInfo typeConvert(EventInner inner) {
                return new WebhookEventInfoImpl(inner);
            }
        };
        return converter.convert(this.containerRegistryManager.inner().webhooks()
            .listEvents(self.resourceGroupName, self.registryName, self.name()));
    }

    @Override
    public Observable<WebhookEventInfo> listEventsAsync() {
        final WebhookImpl self = this;

        return this.containerRegistryManager.inner().webhooks()
            .listEventsAsync(self.resourceGroupName, self.registryName, self.name())
            .flatMap(new Func1<Page<EventInner>, Observable<EventInner>>() {
                @Override
                public Observable<EventInner> call(Page<EventInner> eventInnerPage) {
                    return Observable.from(eventInnerPage.items());
                }
            }).map(new Func1<EventInner, WebhookEventInfo>() {
                @Override
                public WebhookEventInfo call(EventInner inner) {
                    return new WebhookEventInfoImpl(inner);
                }
            });
    }

    @Override
    public Observable<Webhook> createAsync() {
        final WebhookImpl self = this;
        if (webhookCreateParametersInner != null) {
            return this.containerRegistryManager.inner().webhooks()
                .createAsync(self.resourceGroupName,
                    this.registryName,
                    this.name(),
                    this.webhookCreateParametersInner)
                .map(new Func1<WebhookInner, WebhookImpl>() {
                    @Override
                    public WebhookImpl call(WebhookInner inner) {
                        self.webhookCreateParametersInner = null;
                        self.setInner(inner);
                        return self;
                    }
                }).flatMap(new Func1<WebhookImpl, Observable<Webhook>>() {
                    @Override
                    public Observable<Webhook> call(WebhookImpl webhook) {
                        return self.setCallbackConfigAsync();
                    }
                });
        } else {
            return Observable.just(this).map(new Func1<WebhookImpl, Webhook>() {
                @Override
                public Webhook call(WebhookImpl webhook) {
                    return webhook;
                }
            });
        }
    }

    WebhookImpl setCallbackConfig(CallbackConfigInner callbackConfigInner) {
        this.serviceUri = callbackConfigInner.serviceUri();
        this.customHeaders = callbackConfigInner.customHeaders() != null ? callbackConfigInner.customHeaders() : new HashMap<String, String>();
        return this;
    }

    Observable<Webhook> setCallbackConfigAsync() {
        final WebhookImpl self = this;

        return this.containerRegistryManager.inner().webhooks()
            .getCallbackConfigAsync(self.resourceGroupName, self.registryName, self.name())
            .map(new Func1<CallbackConfigInner, Webhook>() {
                @Override
                public Webhook call(CallbackConfigInner callbackConfigInner) {
                    setCallbackConfig(callbackConfigInner);
                    return self;
                }
            });
    }

    @Override
    public Observable<Webhook> updateAsync() {
        final WebhookImpl self = this;
        if (webhookUpdateParametersInner != null) {
            return this.containerRegistryManager.inner().webhooks()
                .updateAsync(self.resourceGroupName,
                    self.registryName,
                    self.name(),
                    self.webhookUpdateParametersInner)
                .map(new Func1<WebhookInner, WebhookImpl>() {
                    @Override
                    public WebhookImpl call(WebhookInner inner) {
                        self.setInner(inner);
                        self.webhookUpdateParametersInner = null;
                        return self;
                    }
                }).flatMap(new Func1<WebhookImpl, Observable<Webhook>>() {
                    @Override
                    public Observable<Webhook> call(WebhookImpl webhook) {
                        return self.setCallbackConfigAsync();
                    }
                });
        } else {
            return Observable.just(this).map(new Func1<WebhookImpl, Webhook>() {
                @Override
                public Webhook call(WebhookImpl webhook) {
                    return webhook;
                }
            });
        }
    }

    @Override
    public Observable<Void> deleteAsync() {
        return this.containerRegistryManager.inner().webhooks()
            .deleteAsync(this.resourceGroupName,
            this.registryName,
            this.name());
    }

    @Override
    protected Observable<WebhookInner> getInnerAsync() {
        final WebhookImpl self = this;
        final WebhooksInner webhooksInner = this.containerRegistryManager.inner().webhooks();
        return webhooksInner.getAsync(this.resourceGroupName,
            this.registryName,
            this.name())
            .flatMap(new Func1<WebhookInner, Observable<CallbackConfigInner>>() {
                @Override
                public Observable<CallbackConfigInner> call(WebhookInner webhookInner) {
                    self.setInner(webhookInner);
                    return webhooksInner.getCallbackConfigAsync(self.resourceGroupName, self.registryName, self.name());
                }
            }).map(new Func1<CallbackConfigInner, WebhookInner>() {
                @Override
                public WebhookInner call(CallbackConfigInner callbackConfigInner) {
                    return setCallbackConfig(callbackConfigInner).inner();
                }
            });
    }

    @Override
    public Webhook apply() {
        return this.applyAsync().toBlocking().last();
    }

    @Override
    public Observable<Webhook> applyAsync() {
        return this.updateAsync();
    }

    @Override
    public ServiceFuture<Webhook> applyAsync(ServiceCallback<Webhook> callback) {
        return ServiceFuture.fromBody(this.updateAsync(), callback);
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
            this.webhookCreateParametersInner = new WebhookCreateParametersInner().withLocation(parent().regionName());
        } else {
            this.webhookUpdateParametersInner = new WebhookUpdateParametersInner();
        }

        return this;
    }


    @Override
    public WebhookImpl withTags(Map<String, String> tags) {
        if (tags != null) {
            this.tags = null;
            ensureValidTags();
            for (Map.Entry<String, String> entry : inner().getTags().entrySet()) {
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
            for (Map.Entry<String, String> entry : inner().getTags().entrySet()) {
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
    public WebhookImpl withDefaultStatus(WebhookStatus defaultStatus) {
        if (defaultStatus != null) {
            if (this.isInCreateMode) {
                ensureWebhookCreateParametersInner().withStatus(defaultStatus);
            } else {
                ensureWebhookUpdateParametersInner().withStatus(defaultStatus);
            }
        }
        return this;
    }

    private WebhookCreateParametersInner ensureWebhookCreateParametersInner() {
        if (this.webhookCreateParametersInner == null && parent() != null) {
            this.webhookCreateParametersInner = new WebhookCreateParametersInner().withLocation(parent().regionName());
        }
        return this.webhookCreateParametersInner;
    }

    private WebhookUpdateParametersInner ensureWebhookUpdateParametersInner() {
        if (this.webhookUpdateParametersInner == null && parent() != null) {
            this.webhookUpdateParametersInner = new WebhookUpdateParametersInner();
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
