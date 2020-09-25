// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appservice.fluent.models.SiteAuthSettingsInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;

/** A web app authentication configuration in a web app. */
@Fluent
public interface WebAppAuthentication extends HasInnerModel<SiteAuthSettingsInner>, Indexable, HasParent<WebAppBase> {

    /**
     * The entirety of a web app authentication definition.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithDefaultAuthenticationProvider<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of web app authentication definition stages applicable as part of a web app creation. */
    interface DefinitionStages {
        /**
         * The first stage of a web app authentication definition as part of a definition of a web app.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface Blank<ParentT> extends WithDefaultAuthenticationProvider<ParentT> {
        }

        /**
         * A web app authentication definition allowing the default authentication provider to be set.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithDefaultAuthenticationProvider<ParentT> {
            /**
             * Does not require login by default.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAnonymousAuthentication();

            /**
             * Specifies the default authentication provider.
             *
             * @param provider the default authentication provider
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withDefaultAuthenticationProvider(BuiltInAuthenticationProvider provider);
        }

        /**
         * A web app authentication definition allowing detailed provider information to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAuthenticationProvider<ParentT> {
            /**
             * Specifies the provider to be Active Directory and its client ID and issuer URL.
             *
             * @param clientId the AAD app's client ID
             * @param issuerUrl the token issuer URL in the format of https://sts.windows.net/(tenantId)
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withActiveDirectory(String clientId, String issuerUrl);

            /**
             * Specifies the provider to be Facebook and its app ID and app secret.
             *
             * @param appId the Facebook app ID
             * @param appSecret the Facebook app secret
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFacebook(String appId, String appSecret);

            /**
             * Specifies the provider to be Google and its client ID and client secret.
             *
             * @param clientId the Google app's client ID
             * @param clientSecret the Google app's client secret
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withGoogle(String clientId, String clientSecret);

            /**
             * Specifies the provider to be Twitter and its API key and API secret.
             *
             * @param apiKey the Twitter app's API key
             * @param apiSecret the Twitter app's API secret
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTwitter(String apiKey, String apiSecret);

            /**
             * Specifies the provider to be Microsoft and its client ID and client secret.
             *
             * @param clientId the Microsoft app's client ID
             * @param clientSecret the Microsoft app's client secret
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withMicrosoft(String clientId, String clientSecret);
        }

        /**
         * A web app authentication definition allowing token store to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithTokenStore<ParentT> {
            /**
             * Specifies if token store should be enabled.
             *
             * @param enabled true if token store should be enabled
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTokenStore(boolean enabled);
        }

        /**
         * A web app authentication definition allowing branch to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithExternalRedirectUrls<ParentT> {
            /**
             * Adds an external redirect URL.
             *
             * @param url the external redirect URL
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAllowedExternalRedirectUrl(String url);
        }

        /**
         * The final stage of the web app authentication definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the web app authentication definition
         * can be attached to the parent web app definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinition<ParentT>,
                WithAuthenticationProvider<ParentT>,
                WithTokenStore<ParentT>,
                WithExternalRedirectUrls<ParentT> {
        }
    }

    /**
     * The entirety of a web app authentication definition as part of a web app update.
     *
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT>,
            UpdateDefinitionStages.WithDefaultAuthenticationProvider<ParentT>,
            UpdateDefinitionStages.WithAuthenticationProvider<ParentT>,
            UpdateDefinitionStages.WithTokenStore<ParentT>,
            UpdateDefinitionStages.WithExternalRedirectUrls<ParentT> {
    }

    /** Grouping of web app authentication definition stages applicable as part of a web app update. */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a web app authentication definition as part of a definition of a web app.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface Blank<ParentT> extends WithDefaultAuthenticationProvider<ParentT> {
        }

        /**
         * A web app authentication definition allowing the default authentication provider to be set.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithDefaultAuthenticationProvider<ParentT> {
            /**
             * Does not require login by default.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAnonymousAuthentication();

            /**
             * Specifies the default authentication provider.
             *
             * @param provider the default authentication provider
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withDefaultAuthenticationProvider(BuiltInAuthenticationProvider provider);
        }

        /**
         * A web app authentication definition allowing detailed provider information to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAuthenticationProvider<ParentT> {
            /**
             * Specifies the provider to be Active Directory and its client ID and issuer URL.
             *
             * @param clientId the AAD app's client ID
             * @param issuerUrl the token issuer URL in the format of https://sts.windows.net/(tenantId)
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withActiveDirectory(String clientId, String issuerUrl);

            /**
             * Specifies the provider to be Facebook and its app ID and app secret.
             *
             * @param appId the Facebook app ID
             * @param appSecret the Facebook app secret
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFacebook(String appId, String appSecret);

            /**
             * Specifies the provider to be Google and its client ID and client secret.
             *
             * @param clientId the Google app's client ID
             * @param clientSecret the Google app's client secret
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withGoogle(String clientId, String clientSecret);

            /**
             * Specifies the provider to be Twitter and its API key and API secret.
             *
             * @param apiKey the Twitter app's API key
             * @param apiSecret the Twitter app's API secret
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTwitter(String apiKey, String apiSecret);

            /**
             * Specifies the provider to be Microsoft and its client ID and client secret.
             *
             * @param clientId the Microsoft app's client ID
             * @param clientSecret the Microsoft app's client secret
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withMicrosoft(String clientId, String clientSecret);
        }

        /**
         * A web app authentication definition allowing token store to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithTokenStore<ParentT> {
            /**
             * Specifies if token store should be enabled.
             *
             * @param enabled true if token store should be enabled
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withTokenStore(boolean enabled);
        }

        /**
         * A web app authentication definition allowing branch to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithExternalRedirectUrls<ParentT> {
            /**
             * Adds an external redirect URL.
             *
             * @param url the external redirect URL
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAllowedExternalRedirectUrl(String url);
        }

        /**
         * The final stage of the web app authentication definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the web app authentication definition
         * can be attached to the parent web app update using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT>
            extends Attachable.InUpdate<ParentT>,
                WithAuthenticationProvider<ParentT>,
                WithTokenStore<ParentT>,
                WithExternalRedirectUrls<ParentT> {
        }
    }
}
