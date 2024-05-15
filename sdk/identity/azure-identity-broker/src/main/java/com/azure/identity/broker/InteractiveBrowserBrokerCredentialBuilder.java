// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.broker;


import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.AuthenticationRecord;
import com.azure.identity.BrowserCustomizationOptions;
import com.azure.identity.InteractiveBrowserCredential;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
import com.azure.identity.TokenCachePersistenceOptions;
import com.azure.identity.implementation.CredentialBuilderBaseHelper;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

/**
 * Fluent credential builder for instantiating a {@link InteractiveBrowserCredential} configured to use a secure broker.
 *
 * <p><strong>Sample: Construct a {@link InteractiveBrowserCredential} for brokered authentication</strong></p>
 *
 * <p>the following code sample shows to use this type:</p>
 * <!-- src_embed com.azure.identity.broker.interactivebrowserbrokercredentialbuilder.construct -->
 * <pre>
 * InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder&#40;&#41;;
 * InteractiveBrowserCredential credential = builder.build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.broker.interactivebrowserbrokercredentialbuilder.construct -->
 */
public class InteractiveBrowserBrokerCredentialBuilder extends InteractiveBrowserCredentialBuilder {
    /**
     * Sets the parent window handle used by the broker. For use on Windows only.
     *
     * @param windowHandle The window handle of the current application, or 0 for a console application.
     * @return An updated instance of this builder with the interactive browser broker configured.
     */
    public InteractiveBrowserBrokerCredentialBuilder setWindowHandle(long windowHandle) {
        CredentialBuilderBaseHelper.getClientOptions(this).setBrokerWindowHandle(windowHandle);
        return this;
    }

    /**
     * Enables Microsoft Account (MSA) pass-through. This allows the user to sign in with a Microsoft Account (MSA)
     * instead of a work or school account.
     *
     * @return An updated instance of this builder with enable Legacy MSA Passthrough set to true.
     */
    public InteractiveBrowserBrokerCredentialBuilder enableLegacyMsaPassthrough() {
        CredentialBuilderBaseHelper.getClientOptions(this).setEnableLegacyMsaPassthrough(true);
        return this;
    }

    /**
     * Enables automatically using the default broker account for authentication instead
     * of prompting the user with an account picker.
     *
     * @return An updated instance of this builder with useDefaultBrokerAccount set.
     */
    public InteractiveBrowserCredentialBuilder useDefaultBrokerAccount() {
        CredentialBuilderBaseHelper.getClientOptions(this).setUseDefaultBrokerAccount(true);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder clientOptions(ClientOptions clientOptions) {
        super.clientOptions(clientOptions);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder addPolicy(HttpPipelinePolicy policy) {
        super.addPolicy(policy);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder port(int port) {
        super.port(port);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder additionallyAllowedTenants(String... additionallyAllowedTenants) {
        super.additionallyAllowedTenants(additionallyAllowedTenants);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder authenticationRecord(AuthenticationRecord authenticationRecord) {
        super.authenticationRecord(authenticationRecord);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder browserCustomizationOptions(BrowserCustomizationOptions browserCustomizationOptions) {
        super.browserCustomizationOptions(browserCustomizationOptions);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder clientId(String clientId) {
        super.clientId(clientId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder configuration(Configuration configuration) {
        super.configuration(configuration);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder authorityHost(String authorityHost) {
        super.authorityHost(authorityHost);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder disableAutomaticAuthentication() {
        super.disableAutomaticAuthentication();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder disableInstanceDiscovery() {
        super.disableInstanceDiscovery();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder enableAccountIdentifierLogging() {
        super.enableAccountIdentifierLogging();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder enableUnsafeSupportLogging() {
        super.enableUnsafeSupportLogging();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder executorService(ExecutorService executorService) {
        super.executorService(executorService);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder httpClient(HttpClient client) {
        super.httpClient(client);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder httpLogOptions(HttpLogOptions logOptions) {
        super.httpLogOptions(logOptions);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder loginHint(String loginHint) {
        super.loginHint(loginHint);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder pipeline(HttpPipeline pipeline) {
        super.pipeline(pipeline);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder maxRetry(int maxRetry) {
        super.maxRetry(maxRetry);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder redirectUrl(String redirectUrl) {
        super.redirectUrl(redirectUrl);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder additionallyAllowedTenants(List<String> additionallyAllowedTenants) {
        super.additionallyAllowedTenants(additionallyAllowedTenants);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder retryOptions(RetryOptions retryOptions) {
        super.retryOptions(retryOptions);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder retryPolicy(RetryPolicy retryPolicy) {
        super.retryPolicy(retryPolicy);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder tenantId(String tenantId) {
        super.tenantId(tenantId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder retryTimeout(Function<Duration, Duration> retryTimeout) {
        super.retryTimeout(retryTimeout);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder tokenCachePersistenceOptions(TokenCachePersistenceOptions tokenCachePersistenceOptions) {
        super.tokenCachePersistenceOptions(tokenCachePersistenceOptions);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder httpPipeline(HttpPipeline httpPipeline) {
        super.httpPipeline(httpPipeline);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveBrowserBrokerCredentialBuilder proxyOptions(ProxyOptions proxyOptions) {
        super.proxyOptions(proxyOptions);
        return this;
    }
}
