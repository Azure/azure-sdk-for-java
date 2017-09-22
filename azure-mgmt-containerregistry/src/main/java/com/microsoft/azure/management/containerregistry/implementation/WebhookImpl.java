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
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;
import rx.functions.Func1;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    private WebhooksInner innerOperations;
    private WebhookCreateParametersInner webhookCreateParametersInner;
    private WebhookUpdateParametersInner webhookUpdateParametersInner;

    private Map<String, String> tags;
    private Map<String, String> customHeaders;
    private String serviceUri;
    private boolean isInCreateMode;


    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name        the name of this external child resource
     * @param parent      reference to the parent of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param innerOperations reference to the inner object that accesses web hook operations
     */
    WebhookImpl(String name, RegistryImpl parent, WebhookInner innerObject, WebhooksInner innerOperations) {
        super(name, parent, innerObject);
        this.innerOperations = innerOperations;
        this.webhookCreateParametersInner = null;
        this.webhookUpdateParametersInner = null;
        this.isInCreateMode = false;
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
        return Collections.unmodifiableList(this.inner().actions());
    }

    @Override
    public ProvisioningState provisioningState() {
        return this.inner().provisioningState();
    }

    @Override
    public Webhook enable() {
        return this.update()
            .withDefaultStatus(WebhookStatus.ENABLED)
            .apply();
    }

    @Override
    public Observable<Webhook> enableAsync() {
        return this.update()
            .withDefaultStatus(WebhookStatus.ENABLED)
            .applyAsync();
    }

    @Override
    public Webhook disable() {
        return this.update()
            .withDefaultStatus(WebhookStatus.DISABLED)
            .apply();
    }

    @Override
    public Observable<Webhook> disableAsync() {
        return this.update()
            .withDefaultStatus(WebhookStatus.DISABLED)
            .applyAsync();
    }

    @Override
    public String ping() {
        return this.innerOperations.ping(parent().resourceGroupName(), parent().name(), name()).id();
    }

    @Override
    public Observable<String> pingAsync() {
        return this.innerOperations.pingAsync(parent().resourceGroupName(), parent().name(), name())
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
        final WebhooksInner webhooksInner = this.innerOperations;
        final PagedListConverter<EventInner, WebhookEventInfo> converter = new PagedListConverter<EventInner, WebhookEventInfo>() {
            @Override
            public WebhookEventInfo typeConvert(EventInner inner) {
                return new WebhookEventInfoImpl(inner);
            }
        };
        return converter.convert(webhooksInner.listEvents(self.parent().resourceGroupName(), self.parent().name(), self.name()));
    }

    @Override
    public Observable<WebhookEventInfo> listEventsAsync() {
        final WebhookImpl self = this;
        final WebhooksInner webhooksInner = this.innerOperations;

        return webhooksInner.listEventsAsync(self.parent().resourceGroupName(), self.parent().name(), self.name())
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
    public String id() {
        return this.inner().id();
    }

    @Override
    public Observable<Webhook> createAsync() {
        final WebhookImpl self = this;
        final WebhooksInner webhooksInner = this.innerOperations;
        if (webhookCreateParametersInner != null) {
            return webhooksInner.createAsync(self.parent().resourceGroupName(),
                    this.parent().name(),
                    this.name(),
                    this.webhookCreateParametersInner)
                .map(new Func1<WebhookInner, Webhook>() {
                    @Override
                    public Webhook call(WebhookInner inner) {
                        self.webhookCreateParametersInner = null;
                        self.setInner(inner);
                        return self;
                    }
                }).flatMap(new Func1<Webhook, Observable<CallbackConfigInner>>() {
                    @Override
                    public Observable<CallbackConfigInner> call(Webhook webhook) {
                        return webhooksInner.getCallbackConfigAsync(self.parent().resourceGroupName(), self.parent().name(), self.name());
                    }
                }).map(new Func1<CallbackConfigInner, Webhook>() {
                    @Override
                    public Webhook call(CallbackConfigInner callbackConfigInner) {
                        self.serviceUri = callbackConfigInner.serviceUri();
                        self.customHeaders = callbackConfigInner.customHeaders() != null ? callbackConfigInner.customHeaders() : new HashMap<String, String>();
                        return self;
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
    public Observable<Webhook> updateAsync() {
        final WebhookImpl self = this;
        if (webhookUpdateParametersInner != null) {
            return this.innerOperations.updateAsync(self.parent().resourceGroupName(),
                    self.parent().name(),
                    self.name(),
                    self.webhookUpdateParametersInner)
                .map(new Func1<WebhookInner, Webhook>() {
                    @Override
                    public Webhook call(WebhookInner inner) {
                        self.setInner(inner);
                        if (webhookUpdateParametersInner.serviceUri() != null) {
                            self.serviceUri = webhookUpdateParametersInner.serviceUri();
                        }
                        self.webhookUpdateParametersInner = null;
                        return self;
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
        return this.innerOperations.deleteAsync(this.parent().resourceGroupName(),
            this.parent().name(),
            this.name());
    }

    @Override
    protected Observable<WebhookInner> getInnerAsync() {
        return this.innerOperations.getAsync(this.parent().resourceGroupName(),
            this.parent().name(),
            this.name());
    }

    @Override
    public Observable<Webhook> refreshAsync() {
        final WebhookImpl self = this;
        final WebhooksInner webhooksInner = this.innerOperations;
        return super.refreshAsync()
            .flatMap(new Func1<Webhook, Observable<CallbackConfigInner>>() {
                @Override
                public Observable<CallbackConfigInner> call(Webhook webhook) {
                    return webhooksInner.getCallbackConfigAsync(self.parent().resourceGroupName(), self.parent().name(), self.name());
                }
            }).map(new Func1<CallbackConfigInner, Webhook>() {
                @Override
                public Webhook call(CallbackConfigInner callbackConfigInner) {
                    self.serviceUri = callbackConfigInner.serviceUri();
                    self.customHeaders = callbackConfigInner.customHeaders() != null ? callbackConfigInner.customHeaders() : new HashMap<String, String>();
                    return self;
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

        if (this.isInCreateMode) {
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
        if (this.webhookCreateParametersInner == null) {
            this.webhookCreateParametersInner = new WebhookCreateParametersInner().withLocation(parent().regionName());
        }
        return this.webhookCreateParametersInner;
    }

    private WebhookUpdateParametersInner ensureWebhookUpdateParametersInner() {
        if (this.webhookUpdateParametersInner == null) {
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
