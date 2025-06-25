package com.azure.identity.broker;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.TokenCachePersistenceOptions;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class VisualStudioCodeBrokerCredentialBuilder {

    InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();

    private String tenantId;

    /**
     * Constructs an instance of VisualStudioCodeCredentialBuilder.
     */
    public VisualStudioCodeBrokerCredentialBuilder() {
    }

    /**
     * Sets the tenant id of the user to authenticate through the {@link VisualStudioCodeBrokerCredential}. The default is
     * the tenant the user originally authenticated to via the Visual Studio Code Azure Resources Extension.
     *
     * @param tenantId the tenant ID to set.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    public VisualStudioCodeBrokerCredentialBuilder tenantId(String tenantId) {
        builder.tenantId(tenantId);
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Specifies tenants in addition to the specified tenantId for which the credential may acquire tokens.
     * Add the wildcard value "*" to allow the credential to acquire tokens for any tenant the logged in account can access.
     * If no value is specified for tenantId this option will have no effect, and the credential will acquire tokens
     * for any requested tenant.
     *
     * @param additionallyAllowedTenants the additionally allowed tenants.
     * @return An updated instance of this builder with the additional tenants configured.
     */
    public VisualStudioCodeBrokerCredentialBuilder additionallyAllowedTenants(String... additionallyAllowedTenants) {
        builder.additionallyAllowedTenants(additionallyAllowedTenants);
        return this;
    }

    /**
     * Specifies tenants in addition to the specified tenantId for which the credential may acquire tokens.
     * Add the wildcard value "*" to allow the credential to acquire tokens for any tenant the logged in account can access.
     * If no value is specified for tenantId this option will have no effect, and the credential will acquire tokens
     * for any requested tenant.
     *
     * @param additionallyAllowedTenants the additionally allowed tenants.
     * @return An updated instance of this builder with the additional tenants configured.
     */
    public VisualStudioCodeBrokerCredentialBuilder additionallyAllowedTenants(List<String> additionallyAllowedTenants) {
        builder.additionallyAllowedTenants(additionallyAllowedTenants);
        return this;
    }

    /**
     * Sets the parent window handle used by the broker. For use on Windows only.
     *
     * @param windowHandle The window handle of the current application, or 0 for a console application.
     * @return An updated instance of this builder with the interactive browser broker configured.
     */
    public VisualStudioCodeBrokerCredentialBuilder setWindowHandle(long windowHandle) {
        builder.setWindowHandle(windowHandle);
        return this;
    }

    public VisualStudioCodeBrokerCredentialBuilder clientOptions(ClientOptions clientOptions) {
        builder.clientOptions(clientOptions);
        return this;
    }

    public VisualStudioCodeBrokerCredentialBuilder addPolicy(HttpPipelinePolicy policy) {
        builder.addPolicy(policy);
        return this;
    }

    public VisualStudioCodeBrokerCredentialBuilder configuration(Configuration configuration) {
        builder.configuration(configuration);
        return this;
    }

    public VisualStudioCodeBrokerCredentialBuilder authorityHost(String authorityHost) {
        builder.authorityHost(authorityHost);
        return this;
    }

    public VisualStudioCodeBrokerCredentialBuilder disableInstanceDiscovery() {
        builder.disableInstanceDiscovery();
        return this;
    }

    public VisualStudioCodeBrokerCredentialBuilder enableAccountIdentifierLogging() {
        builder.enableAccountIdentifierLogging();
        return this;
    }

    public VisualStudioCodeBrokerCredentialBuilder enableUnsafeSupportLogging() {
        builder.enableUnsafeSupportLogging();
        return this;
    }

    public VisualStudioCodeBrokerCredentialBuilder executorService(ExecutorService executorService) {
        builder.executorService(executorService);
        return this;
    }

    public VisualStudioCodeBrokerCredentialBuilder httpClient(HttpClient client) {
        builder.httpClient(client);
        return this;
    }

    public VisualStudioCodeBrokerCredentialBuilder httpLogOptions(HttpLogOptions logOptions) {
        builder.httpLogOptions(logOptions);
        return this;
    }

    public VisualStudioCodeBrokerCredentialBuilder pipeline(HttpPipeline pipeline) {
        builder.pipeline(pipeline);
        return this;
    }

    public VisualStudioCodeBrokerCredentialBuilder retryOptions(RetryOptions retryOptions) {
        builder.retryOptions(retryOptions);
        return this;
    }

    public VisualStudioCodeBrokerCredentialBuilder retryPolicy(RetryPolicy retryPolicy) {
        builder.retryPolicy(retryPolicy);
        return this;
    }

    public VisualStudioCodeBrokerCredentialBuilder
        tokenCachePersistenceOptions(TokenCachePersistenceOptions tokenCachePersistenceOptions) {
        builder.tokenCachePersistenceOptions(tokenCachePersistenceOptions);
        return this;
    }

    public VisualStudioCodeBrokerCredentialBuilder httpPipeline(HttpPipeline httpPipeline) {
        builder.httpPipeline(httpPipeline);
        return this;
    }

    public VisualStudioCodeBrokerCredential build() {
        return new VisualStudioCodeBrokerCredential(builder);
    }
}
