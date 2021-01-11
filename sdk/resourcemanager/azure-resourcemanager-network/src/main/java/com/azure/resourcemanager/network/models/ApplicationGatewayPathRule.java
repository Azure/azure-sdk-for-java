// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.models.ApplicationGatewayPathRuleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import java.util.List;

/** A client-side representation of an application gateway's URL path map. */
@Fluent
public interface ApplicationGatewayPathRule
    extends HasInnerModel<ApplicationGatewayPathRuleInner>, ChildResource<ApplicationGatewayUrlPathMap> {

    /** @return backend address pool resource of URL path map path rule */
    ApplicationGatewayBackend backend();

    /** @return backend http settings resource of URL path map path rule */
    ApplicationGatewayBackendHttpConfiguration backendHttpConfiguration();

    /** @return redirect configuration resource of URL path map path rule */
    ApplicationGatewayRedirectConfiguration redirectConfiguration();

    /** @return paths for URL path map rule. */
    List<String> paths();

    /** Grouping of application gateway URL path map definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway URL path map definition.
         *
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface Blank<ReturnT> extends WithBackendHttpConfiguration<ReturnT> {
        }

        /**
         * The stage of an application gateway path rule definition allowing to specify the the paths to associate with
         * path rule.
         *
         * @param <ReturnT> the stage of the application gateway URL path map definition to return to after attaching
         *     this definition
         */
        interface WithPath<ReturnT> {
            /**
             * @param path for the path rule
             * @return the next stage of the definition
             */
            WithAttach<ReturnT> withPath(String path);

            /**
             * @param paths for the path rule
             * @return the next stage of the definition
             */
            WithAttach<ReturnT> withPaths(String... paths);
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to specify the backend HTTP
         * settings configuration to associate the routing rule with.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendHttpConfiguration<ParentT> {
            /**
             * Associates the specified backend HTTP settings configuration with this path rule.
             *
             * <p>If the backend configuration does not exist yet, it must be defined in the optional part of the
             * application gateway definition. The request routing rule references it only by name.
             *
             * @param name the name of a backend HTTP settings configuration
             * @return the next stage of the definition
             */
            WithBackend<ParentT> toBackendHttpConfiguration(String name);
        }

        /**
         * The stage of an application gateway request routing rule definition allowing to specify the backend to
         * associate the routing rule with.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackend<ParentT> {
            /**
             * Associates the path rule with a backend on this application gateway.
             *
             * <p>If the backend does not yet exist, it will be automatically created.
             *
             * @param name the name of an existing backend
             * @return the next stage of the definition
             */
            WithPath<ParentT> toBackend(String name);
        }

        /**
         * The stage of path rule of URL path map definition allowing to associate the rule with a redirect
         * configuration.
         *
         * @param <ParentT> the stage of the application gateway URL path map definition to return to after attaching
         *     this definition
         */
        interface WithRedirectConfig<ParentT> {
            /**
             * Associates the specified redirect configuration with this path rule.
             *
             * @param name the name of a redirect configuration on this application gateway
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRedirectConfiguration(String name);
        }

        /**
         * The final stage of a path rule of URL path map definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the definition can be attached to the
         * parent application gateway definition.
         *
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithAttach<ReturnT>
            extends Attachable.InDefinition<ReturnT>, WithPath<ReturnT>, WithRedirectConfig<ReturnT> {
        }
    }

    /**
     * The entirety of a path rule of URL path map definition.
     *
     * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
     *     definition
     */
    interface Definition<ReturnT>
        extends DefinitionStages.Blank<ReturnT>,
            DefinitionStages.WithBackendHttpConfiguration<ReturnT>,
            DefinitionStages.WithBackend<ReturnT>,
            DefinitionStages.WithAttach<ReturnT> {
    }

    /** The entirety of path rule of URL path map update as part of an application gateway update. */
    interface Update extends Settable<ApplicationGatewayUrlPathMap.Update> {
    }

    /** Grouping of path rule of URL path map definition stages applicable as part of an application gateway update. */
    interface UpdateDefinitionStages {
        /**
         * The first stage of path rule of URL path map definition.
         *
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface Blank<ReturnT> extends WithBackendHttpConfiguration<ReturnT> {
        }

        /**
         * The stage of path rule of URL path map allowing to specify the backend HTTP settings configuration to
         * associate the path rule with.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithBackendHttpConfiguration<ParentT> {
            /**
             * Associates the specified backend HTTP settings configuration with this path rule.
             *
             * <p>If the backend configuration does not exist yet, it must be defined in the optional part of the
             * application gateway definition. The path rule references it only by name.
             *
             * @param name the name of a backend HTTP settings configuration
             * @return the next stage of the definition
             */
            WithBackend<ParentT> toBackendHttpConfiguration(String name);
        }

        /**
         * The stage of an application gateway path rule definition allowing to specify the backend to associate with
         * path rule.
         *
         * @param <ParentT> the stage of the application gateway URL path map definition to return to after attaching
         *     this definition
         */
        interface WithBackend<ParentT> {
            /**
             * Associates the path rule with a backend on this application gateway.
             *
             * <p>If the backend does not yet exist, it will be automatically created.
             *
             * @param name the name of an existing backend
             * @return the next stage of the definition
             */
            WithPath<ParentT> toBackend(String name);
        }

        /**
         * The stage of an application gateway path rule definition allowing to specify the the paths to associate with
         * path rule.
         *
         * @param <ReturnT> the stage of the application gateway URL path map definition to return to after attaching
         *     this definition
         */
        interface WithPath<ReturnT> {
            /**
             * @param path for the path rule
             * @return the next stage of the definition
             */
            WithAttach<ReturnT> withPath(String path);

            /**
             * @param paths for the path rule
             * @return the next stage of the definition
             */
            WithAttach<ReturnT> withPaths(String... paths);
        }

        /**
         * The stage of path rule of URL path map definition allowing to associate the rule with a redirect
         * configuration.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithRedirectConfig<ParentT> {
            /**
             * Associates the specified redirect configuration with this path rule.
             *
             * @param name the name of a redirect configuration on this application gateway
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRedirectConfiguration(String name);
        }

        /**
         * The final stage of path rule of URL path map definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the definition can be attached to the
         * parent application gateway definition.
         *
         * @param <ReturnT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithAttach<ReturnT>
            extends Attachable.InUpdate<ReturnT>, UpdateDefinitionStages.WithRedirectConfig<ReturnT> {
        }
    }

    /**
     * The entirety of path rule of URL path map definition as part of an application gateway update.
     *
     * @param <ReturnT> the stage of the parent application gateway URL path map definition to return to after attaching
     *     this definition
     */
    interface UpdateDefinition<ReturnT>
        extends UpdateDefinitionStages.Blank<ReturnT>,
            UpdateDefinitionStages.WithBackendHttpConfiguration<ReturnT>,
            UpdateDefinitionStages.WithBackend<ReturnT>,
            UpdateDefinitionStages.WithPath<ReturnT>,
            UpdateDefinitionStages.WithAttach<ReturnT> {
    }
}
