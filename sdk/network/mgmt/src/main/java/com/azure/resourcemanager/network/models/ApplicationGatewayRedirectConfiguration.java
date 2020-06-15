// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.inner.ApplicationGatewayRedirectConfigurationInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import java.util.Map;

/** A client-side representation of an application gateway's redirect configuration. */
@Fluent()
public interface ApplicationGatewayRedirectConfiguration
    extends HasInner<ApplicationGatewayRedirectConfigurationInner>, ChildResource<ApplicationGateway> {

    /** @return the type of redirection. */
    ApplicationGatewayRedirectType type();

    /** @return the target listener on this application network traffic is redirected to */
    ApplicationGatewayListener targetListener();

    /** @return the target URL network traffic is redirected to */
    String targetUrl();

    /** @return request routing rules on this application referencing this redirect configuration, indexed by name */
    Map<String, ApplicationGatewayRequestRoutingRule> requestRoutingRules();

    /** @return true if the path is included in the redirected URL, otherwise false */
    boolean isPathIncluded();

    /** @return true if the query string is included in the redirected URL, otherwise false */
    boolean isQueryStringIncluded();

    /** Grouping of application gateway redirect configuration configuration stages. */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway redirect configuration.
         *
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface Blank<ReturnT> extends WithType<ReturnT> {
        }

        /**
         * The stage of an application gateway redirect configuration allowing to specify the target URL or listener for
         * the redirection.
         *
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithTarget<ReturnT> {
            /**
             * Specifies the URL to redirect to.
             *
             * @param url a URL
             * @return the next stage of the definition
             */
            WithAttach<ReturnT> withTargetUrl(String url);

            /**
             * Specifies the listener on this application gateway to redirect to.
             *
             * @param name the name of a listener on this application gateway
             * @return the next stage of the definition
             */
            WithAttachAndPath<ReturnT> withTargetListener(String name);
        }

        /**
         * The stage of an application gateway redirect configuration allowing to specify the type of the redirection.
         *
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithType<ReturnT> {
            /**
             * Specifies the redirection type.
             *
             * @param redirectType a redirection type
             * @return the next stage of the definition
             */
            WithTarget<ReturnT> withType(ApplicationGatewayRedirectType redirectType);
        }

        /**
         * The stage of an application gateway redirect configuration allowing to specify whether the path should be
         * included in the redirected URL.
         *
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithPathIncluded<ReturnT> {
            /**
             * Specifies that the path should be included in the redirected URL.
             *
             * <p>Note that this setting is valid only when the target of the redirection is a listener, not a URL.
             *
             * @return the next stage of the definition
             */
            WithAttach<ReturnT> withPathIncluded();
        }

        /**
         * The stage of an application gateway redirect configuration allowing to specify whether the query string
         * should be included in the redirected URL.
         *
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithQueryStringIncluded<ReturnT> {
            /**
             * Specifies that the query string should be included in the redirected URL.
             *
             * @return the next stage of the definition
             */
            WithAttach<ReturnT> withQueryStringIncluded();
        }

        /**
         * The stage of an application gateway redirect configuration allowing to specify whether the query string
         * should be included in the redirected URL or other optional settings.
         *
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithAttachAndPath<ReturnT> extends WithAttach<ReturnT>, WithPathIncluded<ReturnT> {
        }

        /**
         * The final stage of an application gateway redirect configuration.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the definition can be attached to the
         * parent application gateway definition.
         *
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithAttach<ReturnT> extends Attachable.InDefinition<ReturnT>, WithQueryStringIncluded<ReturnT> {
        }
    }

    /**
     * The entirety of an application gateway redirect configuration definition.
     *
     * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
     *     definition
     */
    interface Definition<ReturnT>
        extends DefinitionStages.Blank<ReturnT>,
            DefinitionStages.WithAttach<ReturnT>,
            DefinitionStages.WithAttachAndPath<ReturnT>,
            DefinitionStages.WithTarget<ReturnT>,
            DefinitionStages.WithType<ReturnT> {
    }

    /** Grouping of application gateway redirect configuration update stages. */
    interface UpdateStages {
        /**
         * The stage of an application gateway redirect configuration allowing to specify the target URL or listener for
         * the redirection.
         */
        interface WithTarget {
            /**
             * Specifies the URL to redirect to.
             *
             * @param url a URL
             * @return the next stage of the update
             */
            Update withTargetUrl(String url);

            /**
             * Specifies the listener on this application gateway to redirect to.
             *
             * @param name the name of a listener on this application gateway
             * @return the next stage of the update
             */
            Update withTargetListener(String name);

            /**
             * Removes the reference to the target listener.
             *
             * @return the next stage of the update
             */
            Update withoutTargetListener();

            /**
             * Removes the reference to the target URL.
             *
             * @return the next stage of the update
             */
            Update withoutTargetUrl();
        }

        /**
         * The stage of an application gateway redirect configuration allowing to specify the type of the redirection.
         */
        interface WithType {
            /**
             * Specifies the redirection type.
             *
             * @param redirectType a redirection type
             * @return the next stage of the update
             */
            Update withType(ApplicationGatewayRedirectType redirectType);
        }

        /**
         * The stage of an application gateway redirect configuration allowing to specify whether the path should be
         * included in the redirected URL.
         */
        interface WithPathIncluded {
            /**
             * Specifies that the path should be included in the redirected URL.
             *
             * <p>Note that this setting is valid only when the target of the redirection is a listener, not a URL.
             *
             * @return the next stage of the update
             */
            Update withPathIncluded();

            /**
             * Specifies that the path should not be included in the redirected URL.
             *
             * @return the next stage of the update
             */
            Update withoutPathIncluded();
        }

        /**
         * The stage of an application gateway redirect configuration allowing to specify whether the query string
         * should be included in the redirected URL.
         */
        interface WithQueryStringIncluded {
            /**
             * Specifies that the query string should be included in the redirected URL.
             *
             * @return the next stage of the update
             */
            Update withQueryStringIncluded();

            /**
             * Specifies that the query string should not be included in the redirected URL.
             *
             * @return the next stage of the update
             */
            Update withoutQueryStringIncluded();
        }
    }

    /**
     * The entirety of an application gateway redirect configuration update as part of an application gateway update.
     */
    interface Update
        extends Settable<ApplicationGateway.Update>,
            UpdateStages.WithTarget,
            UpdateStages.WithType,
            UpdateStages.WithPathIncluded,
            UpdateStages.WithQueryStringIncluded {
    }

    /**
     * Grouping of application gateway redirect configuration definition stages applicable as part of an application
     * gateway update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an application gateway redirect configuration configuration definition.
         *
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface Blank<ReturnT> extends WithType<ReturnT> {
        }

        /**
         * The stage of an application gateway redirect configuration allowing to specify the target URL or listener for
         * the redirection.
         *
         * @param <ReturnT> the stage of the parent application gateway update to return to after attaching this
         *     definition
         */
        interface WithTarget<ReturnT> {
            /**
             * Specifies the URL to redirect to.
             *
             * @param url a URL
             * @return the next stage of the definition
             */
            WithAttach<ReturnT> withTargetUrl(String url);

            /**
             * Specifies the listener on this application gateway to redirect to.
             *
             * @param name the name of a listener on this application gateway
             * @return the next stage of the definition
             */
            WithAttachAndPath<ReturnT> withTargetListener(String name);
        }

        /**
         * The stage of an application gateway redirect configuration allowing to specify the type of the redirection.
         *
         * @param <ReturnT> the stage of the parent application gateway update to return to after attaching this
         *     definition
         */
        interface WithType<ReturnT> {
            /**
             * Specifies the redirection type.
             *
             * @param redirectType a redirection type
             * @return the next stage of the definition
             */
            WithTarget<ReturnT> withType(ApplicationGatewayRedirectType redirectType);
        }

        /**
         * The stage of an application gateway redirect configuration allowing to specify whether the path should be
         * included in the redirected URL.
         *
         * @param <ReturnT> the stage of the parent application gateway update to return to after attaching this
         *     definition
         */
        interface WithPathIncluded<ReturnT> {
            /**
             * Specifies that the path should be included in the redirected URL.
             *
             * <p>Note that this setting is valid only when the target of the redirection is a listener, not a URL.
             *
             * @return the next stage of the definition
             */
            WithAttach<ReturnT> withPathIncluded();
        }

        /**
         * The stage of an application gateway redirect configuration allowing to specify whether the query string
         * should be included in the redirected URL.
         *
         * @param <ReturnT> the stage of the parent application gateway update to return to after attaching this
         *     definition
         */
        interface WithQueryStringIncluded<ReturnT> {
            /**
             * Specifies that the query string should be included in the redirected URL.
             *
             * @return the next stage of the definition
             */
            WithAttach<ReturnT> withQueryStringIncluded();
        }

        /**
         * The stage of an application gateway redirect configuration allowing to specify whether the query string
         * should be included in the redirected URL or other optional settings.
         *
         * @param <ReturnT> the stage of the parent application gateway update to return to after attaching this
         *     definition
         */
        interface WithAttachAndPath<ReturnT> extends WithAttach<ReturnT>, WithPathIncluded<ReturnT> {
        }

        /**
         * The final stage of an application gateway redirect configuration definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the definition can be attached to the
         * parent application gateway definition.
         *
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithAttach<ReturnT> extends Attachable.InUpdate<ReturnT>, WithQueryStringIncluded<ReturnT> {
        }
    }

    /**
     * The entirety of an application gateway redirect configuration definition as part of an application gateway
     * update.
     *
     * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
     *     definition
     */
    interface UpdateDefinition<ReturnT>
        extends UpdateDefinitionStages.Blank<ReturnT>,
            UpdateDefinitionStages.WithAttach<ReturnT>,
            UpdateDefinitionStages.WithAttachAndPath<ReturnT>,
            UpdateDefinitionStages.WithTarget<ReturnT>,
            UpdateDefinitionStages.WithType<ReturnT> {
    }
}
