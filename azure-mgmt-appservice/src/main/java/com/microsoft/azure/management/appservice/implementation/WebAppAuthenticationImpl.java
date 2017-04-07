/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.BuiltInAuthenticationProvider;
import com.microsoft.azure.management.appservice.UnauthenticatedClientAction;
import com.microsoft.azure.management.appservice.WebAppAuthentication;
import com.microsoft.azure.management.appservice.WebAppBase;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;

import java.util.ArrayList;

/**
 *  Implementation for WebAppAuthentication and its create and update interfaces.
 *  @param <FluentT> the fluent interface of the parent web app
 *  @param <FluentImplT> the fluent implementation of the parent web app
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class WebAppAuthenticationImpl<
        FluentT extends WebAppBase,
        FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
    extends IndexableWrapperImpl<SiteAuthSettingsInner>
    implements
        WebAppAuthentication,
        WebAppAuthentication.Definition<WebAppBase.DefinitionStages.WithCreate<FluentT>>,
        WebAppAuthentication.UpdateDefinition<WebAppBase.Update<FluentT>>,
        WebAppAuthentication.Update<WebAppBase.Update<FluentT>> {

    private final WebAppBaseImpl<FluentT, FluentImplT> parent;

    WebAppAuthenticationImpl(SiteAuthSettingsInner inner, WebAppBaseImpl<FluentT, FluentImplT> parent) {
        super(inner);
        this.parent = parent;
        inner.withLocation(parent.regionName());
        inner.withTokenStoreEnabled(true);
    }

    @Override
    public FluentImplT attach() {
        parent.withAuthentication(this);
        return parent();
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT parent() {
        parent.withAuthentication(this);
        return (FluentImplT) this.parent;
    }

    @Override
    public WebAppAuthenticationImpl<FluentT, FluentImplT> withAnonymousAuthentication() {
        inner().withUnauthenticatedClientAction(UnauthenticatedClientAction.ALLOW_ANONYMOUS);
        return this;
    }

    @Override
    public WebAppAuthenticationImpl<FluentT, FluentImplT> withDefaultAuthenticationProvider(BuiltInAuthenticationProvider provider) {
        inner().withUnauthenticatedClientAction(UnauthenticatedClientAction.REDIRECT_TO_LOGIN_PAGE).withDefaultProvider(provider);
        return this;
    }

    @Override
    public WebAppAuthenticationImpl<FluentT, FluentImplT> withActiveDirectory(String clientId, String issuerUrl) {
        inner().withClientId(clientId).withIssuer(issuerUrl);
        return this;
    }

    @Override
    public WebAppAuthenticationImpl<FluentT, FluentImplT> withFacebook(String appId, String appSecret) {
        inner().withFacebookAppId(appId).withFacebookAppSecret(appSecret);
        return this;
    }

    @Override
    public WebAppAuthenticationImpl<FluentT, FluentImplT> withGoogle(String clientId, String clientSecret) {
        inner().withGoogleClientId(clientId).withGoogleClientSecret(clientSecret);
        return this;
    }

    @Override
    public WebAppAuthenticationImpl<FluentT, FluentImplT> withTwitter(String apiKey, String apiSecret) {
        inner().withTwitterConsumerKey(apiKey).withTwitterConsumerSecret(apiSecret);
        return this;
    }

    @Override
    public WebAppAuthenticationImpl<FluentT, FluentImplT> withMicrosoft(String clientId, String clientSecret) {
        inner().withMicrosoftAccountClientId(clientId).withMicrosoftAccountClientSecret(clientSecret);
        return this;
    }

    @Override
    public WebAppAuthenticationImpl<FluentT, FluentImplT> withTokenStore(boolean enabled) {
        inner().withTokenStoreEnabled(enabled);
        return this;
    }

    @Override
    public WebAppAuthenticationImpl<FluentT, FluentImplT> withAllowedExternalRedirectUrl(String url) {
        if (inner().allowedExternalRedirectUrls() == null) {
            inner().withAllowedExternalRedirectUrls(new ArrayList<String>());
        }
        inner().allowedExternalRedirectUrls().add(url);
        return this;
    }
}
