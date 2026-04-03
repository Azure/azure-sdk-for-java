// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.cdn.fluent.models.RouteInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

import java.util.List;

/**
 * An immutable client-side representation of an Azure Front Door (AFD) route that lives under an
 * {@link AfdEndpoint}.
 */
@Fluent
public interface Route extends ExternalChildResource<Route, AfdEndpoint>, HasInnerModel<RouteInner> {

    /**
     * Gets the name of the endpoint which contains this route.
     *
     * @return the endpoint name
     */
    String endpointName();

    /**
     * Gets the resource ID of the origin group associated with this route.
     *
     * @return the origin group resource ID
     */
    String originGroupResourceId();

    /**
     * Gets the path to use when routing to the origin.
     *
     * @return the origin path
     */
    String originPath();

    /**
     * Gets the resource IDs of the rule sets associated with this route.
     *
     * @return the rule set resource IDs
     */
    List<String> ruleSetResourceIds();

    /**
     * Gets the supported protocols for this route.
     *
     * @return the supported protocols
     */
    List<AfdEndpointProtocols> supportedProtocols();

    /**
     * Gets the patterns to match for this route.
     *
     * @return the patterns to match
     */
    List<String> patternsToMatch();

    /**
     * Gets the cache configuration for this route.
     *
     * @return the cache configuration
     */
    AfdRouteCacheConfiguration cacheConfiguration();

    /**
     * Gets the forwarding protocol for this route.
     *
     * @return the forwarding protocol
     */
    ForwardingProtocol forwardingProtocol();

    /**
     * Gets whether this route is linked to the default endpoint domain.
     *
     * @return the link-to-default-domain setting
     */
    LinkToDefaultDomain linkToDefaultDomain();

    /**
     * Gets the HTTPS redirect configuration for this route.
     *
     * @return the HTTPS redirect setting
     */
    HttpsRedirect httpsRedirect();

    /**
     * Gets the enabled state of this route.
     *
     * @return the enabled state
     */
    EnabledState enabledState();

    /**
     * Gets the gRPC state for this route.
     *
     * @return the gRPC state
     */
    AfdRouteGrpcState grpcState();

    /**
     * Gets the provisioning state reported by the service.
     *
     * @return the provisioning state
     */
    AfdProvisioningState provisioningState();

    /**
     * Gets the deployment status for the route.
     *
     * @return the deployment status
     */
    DeploymentStatus deploymentStatus();

    /**
     * Grouping of route definition stages as part of a parent {@link AfdEndpoint} definition.
     */
    interface DefinitionStages {
        /**
         * The first stage of a route definition.
         *
         * @param <ParentT> the stage of the parent endpoint definition to return to after attaching
         */
        interface Blank<ParentT> extends WithOriginGroup<ParentT> {
        }

        /**
         * The stage of a route definition requiring the origin group to be specified.
         *
         * @param <ParentT> the stage of the parent endpoint definition to return to after attaching
         */
        interface WithOriginGroup<ParentT> {
            /**
             * Specifies the resource ID of the origin group for this route.
             *
             * @param originGroupResourceId the origin group resource ID
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withOriginGroupResourceId(String originGroupResourceId);
        }

        /**
         * The stage of the definition containing optional settings prior to attachment.
         *
         * @param <ParentT> the stage of the parent endpoint definition to return to after attaching
         */
        interface WithAttach<ParentT> extends Attachable<ParentT> {
            /**
             * Specifies the path to use when routing to the origin.
             *
             * @param originPath the origin path
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withOriginPath(String originPath);

            /**
             * Specifies the resource IDs of rule sets for this route.
             *
             * @param ruleSetResourceIds the rule set resource IDs
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRuleSetResourceIds(List<String> ruleSetResourceIds);

            /**
             * Specifies the supported protocols for this route.
             *
             * @param supportedProtocols the supported protocols
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSupportedProtocols(List<AfdEndpointProtocols> supportedProtocols);

            /**
             * Specifies the patterns to match for this route.
             *
             * @param patternsToMatch the patterns to match
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPatternsToMatch(List<String> patternsToMatch);

            /**
             * Specifies the cache configuration for this route.
             *
             * @param cacheConfiguration the cache configuration
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withCacheConfiguration(AfdRouteCacheConfiguration cacheConfiguration);

            /**
             * Specifies the forwarding protocol for this route.
             *
             * @param forwardingProtocol the forwarding protocol
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withForwardingProtocol(ForwardingProtocol forwardingProtocol);

            /**
             * Specifies whether this route should be linked to the default endpoint domain.
             *
             * @param linkToDefaultDomain the link-to-default-domain setting
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withLinkToDefaultDomain(LinkToDefaultDomain linkToDefaultDomain);

            /**
             * Specifies the HTTPS redirect configuration for this route.
             *
             * @param httpsRedirect the HTTPS redirect setting
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withHttpsRedirect(HttpsRedirect httpsRedirect);

            /**
             * Specifies the enabled state of this route.
             *
             * @param enabledState the enabled state
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withEnabledState(EnabledState enabledState);

            /**
             * Specifies the gRPC state for this route.
             *
             * @param grpcState the gRPC state
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withGrpcState(AfdRouteGrpcState grpcState);
        }

        /**
         * The final stage of a route definition.
         *
         * @param <ParentT> the stage of the parent endpoint definition to return to after attaching
         */
        interface Attachable<ParentT> {
            /**
             * Attaches the defined route to the parent endpoint.
             *
             * @return the next stage of the parent definition
             */
            ParentT attach();
        }
    }

    /**
     * The entirety of a route definition.
     *
     * @param <ParentT> the stage of the parent endpoint definition to return to after attaching
     */
    interface Definition<ParentT> extends DefinitionStages.Blank<ParentT>, DefinitionStages.WithOriginGroup<ParentT>,
        DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of route definition stages that run as part of an {@link AfdEndpoint.Update} flow.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a route definition inside an endpoint update.
         *
         * @param <ParentT> the stage of the parent endpoint update to return to after attaching
         */
        interface Blank<ParentT> extends WithOriginGroup<ParentT> {
        }

        /**
         * The stage of a route update-definition requiring the origin group to be specified.
         *
         * @param <ParentT> the stage of the parent endpoint update to return to after attaching
         */
        interface WithOriginGroup<ParentT> {
            /**
             * Specifies the resource ID of the origin group for this route.
             *
             * @param originGroupResourceId the origin group resource ID
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withOriginGroupResourceId(String originGroupResourceId);
        }

        /**
         * The stage of the definition containing optional settings prior to attachment.
         *
         * @param <ParentT> the stage of the parent endpoint update to return to after attaching
         */
        interface WithAttach<ParentT> extends Attachable<ParentT> {
            /**
             * Specifies the path to use when routing to the origin.
             *
             * @param originPath the origin path
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withOriginPath(String originPath);

            /**
             * Specifies the resource IDs of rule sets for this route.
             *
             * @param ruleSetResourceIds the rule set resource IDs
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRuleSetResourceIds(List<String> ruleSetResourceIds);

            /**
             * Specifies the supported protocols for this route.
             *
             * @param supportedProtocols the supported protocols
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSupportedProtocols(List<AfdEndpointProtocols> supportedProtocols);

            /**
             * Specifies the patterns to match for this route.
             *
             * @param patternsToMatch the patterns to match
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPatternsToMatch(List<String> patternsToMatch);

            /**
             * Specifies the cache configuration for this route.
             *
             * @param cacheConfiguration the cache configuration
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withCacheConfiguration(AfdRouteCacheConfiguration cacheConfiguration);

            /**
             * Specifies the forwarding protocol for this route.
             *
             * @param forwardingProtocol the forwarding protocol
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withForwardingProtocol(ForwardingProtocol forwardingProtocol);

            /**
             * Specifies whether this route should be linked to the default endpoint domain.
             *
             * @param linkToDefaultDomain the link-to-default-domain setting
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withLinkToDefaultDomain(LinkToDefaultDomain linkToDefaultDomain);

            /**
             * Specifies the HTTPS redirect configuration for this route.
             *
             * @param httpsRedirect the HTTPS redirect setting
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withHttpsRedirect(HttpsRedirect httpsRedirect);

            /**
             * Specifies the enabled state of this route.
             *
             * @param enabledState the enabled state
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withEnabledState(EnabledState enabledState);

            /**
             * Specifies the gRPC state for this route.
             *
             * @param grpcState the gRPC state
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withGrpcState(AfdRouteGrpcState grpcState);
        }

        /**
         * The final stage of a route definition inside an endpoint update.
         *
         * @param <ParentT> the stage of the parent endpoint update to return to after attaching
         */
        interface Attachable<ParentT> {
            /**
             * Attaches the defined route to the parent endpoint update.
             *
             * @return the next stage of the parent update
             */
            ParentT attach();
        }
    }

    /**
     * The entirety of a route update inside an {@link AfdEndpoint.Update} flow.
     */
    interface Update extends Settable<AfdEndpoint.Update> {
        /**
         * Specifies the resource ID of the origin group for this route.
         *
         * @param originGroupResourceId the origin group resource ID
         * @return the next stage of the update
         */
        Update withOriginGroupResourceId(String originGroupResourceId);

        /**
         * Specifies the path to use when routing to the origin.
         *
         * @param originPath the origin path
         * @return the next stage of the update
         */
        Update withOriginPath(String originPath);

        /**
         * Specifies the resource IDs of rule sets for this route.
         *
         * @param ruleSetResourceIds the rule set resource IDs
         * @return the next stage of the update
         */
        Update withRuleSetResourceIds(List<String> ruleSetResourceIds);

        /**
         * Specifies the supported protocols for this route.
         *
         * @param supportedProtocols the supported protocols
         * @return the next stage of the update
         */
        Update withSupportedProtocols(List<AfdEndpointProtocols> supportedProtocols);

        /**
         * Specifies the patterns to match for this route.
         *
         * @param patternsToMatch the patterns to match
         * @return the next stage of the update
         */
        Update withPatternsToMatch(List<String> patternsToMatch);

        /**
         * Specifies the cache configuration for this route.
         *
         * @param cacheConfiguration the cache configuration
         * @return the next stage of the update
         */
        Update withCacheConfiguration(AfdRouteCacheConfiguration cacheConfiguration);

        /**
         * Specifies the forwarding protocol for this route.
         *
         * @param forwardingProtocol the forwarding protocol
         * @return the next stage of the update
         */
        Update withForwardingProtocol(ForwardingProtocol forwardingProtocol);

        /**
         * Specifies whether this route should be linked to the default endpoint domain.
         *
         * @param linkToDefaultDomain the link-to-default-domain setting
         * @return the next stage of the update
         */
        Update withLinkToDefaultDomain(LinkToDefaultDomain linkToDefaultDomain);

        /**
         * Specifies the HTTPS redirect configuration for this route.
         *
         * @param httpsRedirect the HTTPS redirect setting
         * @return the next stage of the update
         */
        Update withHttpsRedirect(HttpsRedirect httpsRedirect);

        /**
         * Specifies the enabled state of this route.
         *
         * @param enabledState the enabled state
         * @return the next stage of the update
         */
        Update withEnabledState(EnabledState enabledState);

        /**
         * Specifies the gRPC state for this route.
         *
         * @param grpcState the gRPC state
         * @return the next stage of the update
         */
        Update withGrpcState(AfdRouteGrpcState grpcState);
    }
}
