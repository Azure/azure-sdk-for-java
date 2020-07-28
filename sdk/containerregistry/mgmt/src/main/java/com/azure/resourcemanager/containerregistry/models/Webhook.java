// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.containerregistry.fluent.inner.WebhookInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.util.Collection;
import java.util.Map;
import reactor.core.publisher.Mono;

/** An object that represents a webhook for a container registry. */
@Fluent
public interface Webhook
    extends ExternalChildResource<Webhook, Registry>,
        Resource,
        HasInner<WebhookInner>,
        Refreshable<Webhook>,
        Updatable<Webhook.Update> {

    /** @return the status of the webhook */
    boolean isEnabled();

    /**
     * @return the scope of repositories where the event can be triggered
     *     <p>For example: - 'foo:*' means events for all tags under repository 'foo' - 'foo:bar' means events for
     *     'foo:bar' only - 'foo' is equivalent to 'foo:latest' - empty means all events
     */
    String scope();

    /** @return the service URI for the webhook to post notifications */
    String serviceUri();

    /** @return the Custom headers that will be added to the webhook notifications */
    Map<String, String> customHeaders();

    /** @return the list of actions that trigger the webhook to post notifications */
    Collection<WebhookAction> triggers();

    /** @return the provisioning state of the webhook */
    ProvisioningState provisioningState();

    /** @return the webhook parent ID */
    String parentId();

    /** Changes the status of the webhook to "enabled". */
    void enable();

    /**
     * Changes the status of the webhook to "enabled".
     *
     * @return a representation of the future computation of this call
     */
    Mono<Void> enableAsync();

    /** Changes the status of the webhook to "disabled". */
    void disable();

    /**
     * Changes the status of the webhook to "disabled".
     *
     * @return a representation of the future computation of this call
     */
    Mono<Void> disableAsync();

    /** @return the id on an event info resource */
    String ping();

    /** @return a representation of the future computation of this call, returning the id on an event info resource */
    Mono<String> pingAsync();

    /** @return the list of event info object */
    PagedIterable<WebhookEventInfo> listEvents();

    /** @return a representation of the future computation of this call, returning the list of event info object */
    PagedFlux<WebhookEventInfo> listEventsAsync();

    /** Grouping of webhook definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of the webhook definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithTriggerWhen<ParentT> {
        }

        /**
         * The stage of the webhook definition allowing to specify the actions that will trigger the webhook
         * notifications.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithTriggerWhen<ParentT> {
            /**
             * Specifies the actions that will trigger the webhook notifications.
             *
             * @param webhookActions the webhook actions
             * @return the next stage of the definition
             */
            WithServiceUri<ParentT> withTriggerWhen(WebhookAction... webhookActions);
        }

        /**
         * The stage of the webhook definition allowing to specify the service URI for post notifications.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithServiceUri<ParentT> {
            /**
             * Specifies the service URI for post notifications.
             *
             * @param serviceUri the service URI for the post notifications
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withServiceUri(String serviceUri);
        }

        /**
         * The stage of the webhook definition allowing to specify the custom headers that will be added to the
         * notifications.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithCustomHeaders<ParentT> {
            /**
             * Specifies a custom header that will be added to notifications.
             *
             * <p>Consecutive calls to this method will add additional headers.
             *
             * @param name of the optional header
             * @param value of the optional header
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withCustomHeader(String name, String value);

            /**
             * Specifies the custom headers that will be added to the notifications.
             *
             * @param customHeaders the "Name=Value" custom headers.
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withCustomHeaders(Map<String, String> customHeaders);
        }

        /**
         * The stage of the webhook definition allowing to specify the scope of repositories where the event can be
         * triggered.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithRepositoriesScope<ParentT> {
            /**
             * Specifies the scope of repositories where the event can be triggered.
             *
             * <p>For example, 'foo:*' means events for all tags under repository 'foo'. 'foo:bar' means events for
             * 'foo:bar' only. 'foo' is equivalent to 'foo:latest', empty means all events.
             *
             * @param repositoriesScope the scope of repositories where the event can be triggered; empty means all
             *     events
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRepositoriesScope(String repositoriesScope);
        }

        /**
         * The stage of the webhook definition allowing to specify the default status of the webhook after being
         * created.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDefaultStatus<ParentT> {
            /**
             * Specifies the default status of the webhook; default is "enabled".
             *
             * @param defaultStatus indicates whether the webhook is enabled or disabled after being created
             * @return the next stage of the definition
             */
            WithAttach<ParentT> enabled(boolean defaultStatus);
        }

        /**
         * The final stage of the webhook definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the webhook definition can be attached
         * to the parent container group definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends WithCustomHeaders<ParentT>,
                WithRepositoriesScope<ParentT>,
                WithDefaultStatus<ParentT>,
                DefinitionWithTags<WithAttach<ParentT>>,
                Attachable.InDefinition<ParentT> {
        }
    }

    /**
     * Grouping of the container register webhook definitions.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface WebhookDefinition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithTriggerWhen<ParentT>,
            DefinitionStages.WithServiceUri<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of webhook update definition stages. */
    interface UpdateDefinitionStages {
        /**
         * The first stage of the webhook definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithTriggerWhen<ParentT> {
        }

        /**
         * The stage of the webhook definition allowing to specify the actions that will trigger the webhook
         * notifications.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithTriggerWhen<ParentT> {
            /**
             * Specifies the actions that will trigger the webhook notifications.
             *
             * @param webhookActions the webhook actions
             * @return the next stage of the definition
             */
            WithServiceUri<ParentT> withTriggerWhen(WebhookAction... webhookActions);
        }

        /**
         * The stage of the webhook definition allowing to specify the service URI for post notifications.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithServiceUri<ParentT> {
            /**
             * Specifies the service URI for post notifications.
             *
             * @param serviceUri the service URI for the post notifications
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withServiceUri(String serviceUri);
        }

        /**
         * The stage of the webhook definition allowing to specify the custom headers that will be added to the
         * notifications.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithCustomHeaders<ParentT> {
            /**
             * Specifies a custom header that will be added to notifications.
             *
             * <p>Consecutive calls to this method will add additional headers.
             *
             * @param name of the optional header
             * @param value of the optional header
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withCustomHeader(String name, String value);

            /**
             * Specifies the custom headers that will be added to the notifications.
             *
             * @param customHeaders the "Name=Value" custom headers.
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withCustomHeaders(Map<String, String> customHeaders);
        }

        /**
         * The stage of the webhook definition allowing to specify the scope of repositories where the event can be
         * triggered.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithRepositoriesScope<ParentT> {
            /**
             * Specifies the scope of repositories where the event can be triggered.
             *
             * <p>For example, 'foo:*' means events for all tags under repository 'foo'. 'foo:bar' means events for
             * 'foo:bar' only. 'foo' is equivalent to 'foo:latest', empty means all events.
             *
             * @param repositoriesScope the scope of repositories where the event can be triggered; empty means all
             *     events
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRepositoriesScope(String repositoriesScope);
        }

        /**
         * The stage of the webhook definition allowing to specify the default status of the webhook after being
         * created.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDefaultStatus<ParentT> {
            /**
             * Specifies the default status of the webhook; default is "enabled".
             *
             * @param defaultStatus indicates whether the webhook is enabled or disabled after being created
             * @return the next stage of the definition
             */
            WithAttach<ParentT> enabled(boolean defaultStatus);
        }

        /**
         * The stage of the webhook definition allowing to specify the tags.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithTags<ParentT> {
            /**
             * Specifies tags for the webhook.
             *
             * @param tags a {@link Map} of tags
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTags(Map<String, String> tags);

            /**
             * Adds a tag to the webhook.
             *
             * @param key the key for the tag
             * @param value the value for the tag
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTag(String key, String value);
        }

        /**
         * The final stage of the webhook definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the webhook definition can be attached
         * to the parent container group definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends WithCustomHeaders<ParentT>,
                WithRepositoriesScope<ParentT>,
                WithDefaultStatus<ParentT>,
                WithTags<ParentT>,
                Attachable.InUpdate<ParentT> {
        }
    }

    /**
     * Grouping of the container registry's webhook update definition.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithTriggerWhen<ParentT>,
            UpdateDefinitionStages.WithServiceUri<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /** The entirety of a webhook update. */
    interface Update
        extends UpdateStages.WithTriggerWhen,
            UpdateStages.WithServiceUri,
            UpdateStages.WithCustomHeaders,
            UpdateStages.WithRepositoriesScope,
            UpdateStages.WithDefaultStatus,
            Resource.UpdateWithTags<Update>,
            Appliable<Webhook> {
    }

    /** Grouping of webhook update stages. */
    interface UpdateStages {
        interface WithTriggerWhen {
            /**
             * Specifies the actions that will trigger the webhook notifications.
             *
             * @param webhookActions the webhook actions
             * @return the next stage of the resource update
             */
            Update withTriggerWhen(WebhookAction... webhookActions);
        }

        /** The stage of the webhook definition allowing to specify the service URI for post notifications. */
        interface WithServiceUri {
            /**
             * Specifies the service URI for post notifications.
             *
             * @param serviceUri the service URI for the post notifications
             * @return the next stage of the resource update
             */
            Update withServiceUri(String serviceUri);
        }

        /**
         * The stage of the webhook definition allowing to specify the custom headers that will be added to the
         * notifications.
         */
        interface WithCustomHeaders {
            /**
             * Specifies a custom header that will be added to notifications.
             *
             * <p>Consecutive calls to this method will add additional headers.
             *
             * @param name of the optional header
             * @param value of the optional header
             * @return the next stage of the resource update
             */
            Update withCustomHeader(String name, String value);

            /**
             * Specifies custom headers that will be added to the notifications.
             *
             * @param customHeaders the "Name=Value" custom headers.
             * @return the next stage of the resource update
             */
            Update withCustomHeaders(Map<String, String> customHeaders);
        }

        /**
         * The stage of the webhook definition allowing to specify the scope of repositories where the event can be
         * triggered.
         */
        interface WithRepositoriesScope {
            /**
             * Specifies the scope of repositories where the event can be triggered.
             *
             * <p>For example, 'foo:*' means events for all tags under repository 'foo'. 'foo:bar' means events for
             * 'foo:bar' only. 'foo' is equivalent to 'foo:latest', empty means all events.
             *
             * @param repositoriesScope the scope of repositories where the event can be triggered; empty means all
             *     events
             * @return the next stage of the resource update
             */
            Update withRepositoriesScope(String repositoriesScope);
        }

        /**
         * The stage of the webhook definition allowing to specify the default status of the webhook after being
         * created.
         */
        interface WithDefaultStatus {
            /**
             * Specifies the default status of the webhook; default is "enabled".
             *
             * @param defaultStatus indicates whether the webhook is enabled or disabled after being created
             * @return the next stage of the resource update
             */
            Update enabled(boolean defaultStatus);
        }
    }

    /**
     * The entirety of a webhook resource update as part of a container registry update.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface UpdateResource<ParentT>
        extends UpdateResourceStages.Blank<ParentT>, UpdateResourceStages.WithAttach<ParentT> {
    }

    /** Grouping of webhook update stages applicable as part of a container registry update. */
    interface UpdateResourceStages {
        /**
         * The first stage of the webhook definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this update definition
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        interface WithTriggerWhen<ParentT> {
            /**
             * Specifies the actions that will trigger the webhook notifications.
             *
             * @param webhookActions the webhook actions
             * @return the next stage of the resource update
             */
            WithAttach<ParentT> withTriggerWhen(WebhookAction... webhookActions);
        }

        /**
         * The stage of the webhook definition allowing to specify the service URI for post notifications.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithServiceUri<ParentT> {
            /**
             * Specifies the service URI for post notifications.
             *
             * @param serviceUri the service URI for the post notifications
             * @return the next stage of the resource update
             */
            WithAttach<ParentT> withServiceUri(String serviceUri);
        }

        /**
         * The stage of the webhook definition allowing to specify the custom headers that will be added to the
         * notifications.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithCustomHeaders<ParentT> {
            /**
             * Specifies a custom header that will be added to notifications.
             *
             * <p>Consecutive calls to this method will add additional headers.
             *
             * @param name of the optional header
             * @param value of the optional header
             * @return the next stage of the resource update
             */
            WithAttach<ParentT> withCustomHeader(String name, String value);

            /**
             * Specifies the custom headers that will be added to the notifications.
             *
             * @param customHeaders the "Name=Value" custom headers.
             * @return the next stage of the resource update
             */
            WithAttach<ParentT> withCustomHeaders(Map<String, String> customHeaders);
        }

        /**
         * The stage of the webhook definition allowing to specify the scope of repositories where the event can be
         * triggered.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithRepositoriesScope<ParentT> {
            /**
             * Updates the scope of repositories where the event can be triggered.
             *
             * <p>For example, 'foo:*' means events for all tags under repository 'foo'. 'foo:bar' means events for
             * 'foo:bar' only. 'foo' is equivalent to 'foo:latest', empty means all events.
             *
             * @param repositoriesScope the scope of repositories where the event can be triggered; empty means all
             *     events
             * @return the next stage of the resource update
             */
            WithAttach<ParentT> withRepositoriesScope(String repositoriesScope);
        }

        /**
         * The stage of the webhook definition allowing to specify the default status of the webhook after being
         * created.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDefaultStatus<ParentT> {
            /**
             * Updates the default status of the webhook.
             *
             * @param defaultStatus indicates whether the webhook is enabled or disabled after being created
             * @return the next stage of the resource update
             */
            WithAttach<ParentT> enabled(boolean defaultStatus);
        }

        /**
         * The stage of the webhook definition allowing to specify the tags.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithOrWithoutTags<ParentT> {
            /**
             * Specifies tags for the webhook.
             *
             * @param tags a {@link Map} of tags
             * @return the next stage of the resource update
             */
            WithAttach<ParentT> withTags(Map<String, String> tags);

            /**
             * Adds a tag to the webhook.
             *
             * @param key the key for the tag
             * @param value the value for the tag
             * @return the next stage of the resource update
             */
            WithAttach<ParentT> withTag(String key, String value);

            /**
             * Removes a tag from the resource.
             *
             * @param key the key of the tag to remove
             * @return the next stage of the resource update
             */
            WithAttach<ParentT> withoutTag(String key);
        }

        /**
         * The final stage of the webhook definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the webhook definition can be attached
         * to the parent container registry definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends WithTriggerWhen<ParentT>,
                WithServiceUri<ParentT>,
                WithCustomHeaders<ParentT>,
                WithRepositoriesScope<ParentT>,
                WithDefaultStatus<ParentT>,
                WithOrWithoutTags<ParentT>,
                Settable<ParentT> {
        }
    }
}
