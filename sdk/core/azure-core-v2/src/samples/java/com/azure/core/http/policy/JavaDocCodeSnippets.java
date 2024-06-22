// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings("unused")
public class JavaDocCodeSnippets {

    /**
     * Code snippets for using {@link AddHeadersPolicy}.
     */
    @SuppressWarnings("deprecation")
    public void createAddHeaderPolicy() {

        // BEGIN: com.azure.core.http.policy.AddHeaderPolicy.constructor
        HttpHeaders headers = new HttpHeaders();
        headers.put("User-Agent", "MyApp/1.0");
        headers.put("Content-Type", "application/json");

        new AddHeadersPolicy(headers);
        // END: com.azure.core.http.policy.AddHeaderPolicy.constructor
    }

    /**
     * Code snippets for using {@link AzureKeyCredentialPolicy}.
     */
    public void createAzureKeyCredentialPolicy() {

        // BEGIN: com.azure.core.http.policy.AzureKeyCredentialPolicy.constructor
        AzureKeyCredential credential = new AzureKeyCredential("my_key");
        AzureKeyCredentialPolicy policy = new AzureKeyCredentialPolicy("my_header", credential);
        // END: com.azure.core.http.policy.AzureKeyCredentialPolicy.constructor
    }

    /**
     * Code snippets for using {@link AzureSasCredentialPolicy}.
     */
    public void createAzureSasCredentialPolicy() {

        // BEGIN: com.azure.core.http.policy.AzureSasCredentialPolicy.constructor
        AzureSasCredential credential = new AzureSasCredential("my_sas");
        AzureSasCredentialPolicy policy = new AzureSasCredentialPolicy(credential);
        // END: com.azure.core.http.policy.AzureSasCredentialPolicy.constructor
    }

    /**
     * Code snippets for using {@link BearerTokenAuthenticationPolicy}.
     */
    public void createBearerTokenAuthenticationPolicy() {

        // BEGIN: com.azure.core.http.policy.BearerTokenAuthenticationPolicy.constructor
        TokenCredential credential = new BasicAuthenticationCredential("username", "password");
        BearerTokenAuthenticationPolicy policy = new BearerTokenAuthenticationPolicy(credential,
            "https://management.azure.com/.default");
        // END: com.azure.core.http.policy.BearerTokenAuthenticationPolicy.constructor
    }

    /**
     * Code snippets for using {@link CookiePolicy}.
     */
    public void createCookiePolicy() {

        // BEGIN: com.azure.core.http.policy.CookiePolicy.constructor
        CookiePolicy cookiePolicy = new CookiePolicy();
        // END: com.azure.core.http.policy.CookiePolicy.constructor
    }

    /**
     * Code snippets for using {@link RedirectPolicy}.
     */
    public void createRedirectPolicy() {

        // BEGIN: com.azure.core.http.policy.RedirectPolicy.constructor
        RedirectPolicy redirectPolicy = new RedirectPolicy();
        // END: com.azure.core.http.policy.RedirectPolicy.constructor
    }

    /**
     * Code snippets for using {@link DefaultRedirectStrategy}.
     */
    public void createDefaultRetryStrategy() {

        // BEGIN: com.azure.core.http.policy.DefaultRedirectStrategy.constructor
        DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy(3, "Location",
            EnumSet.of(HttpMethod.GET, HttpMethod.HEAD));
        RedirectPolicy redirectPolicy = new RedirectPolicy(redirectStrategy);
        // END: com.azure.core.http.policy.DefaultRedirectStrategy.constructor
    }

    /**
     * Code snippets for using {@link ExponentialBackoff}.
     */
    public void createExponentialBackoff() {

        // BEGIN: com.azure.core.http.policy.ExponentialBackoff.constructor
        ExponentialBackoff retryStrategy = new ExponentialBackoff();
        RetryPolicy policy = new RetryPolicy(retryStrategy);
        // END: com.azure.core.http.policy.ExponentialBackoff.constructor
    }

    /**
     * Code snippets for using {@link ExponentialBackoffOptions}.
     */
    public void createExponentialBackoffOptions() {

        // BEGIN: com.azure.core.http.policy.ExponentialBackoffOptions.constructor
        ExponentialBackoffOptions options = new ExponentialBackoffOptions().setMaxRetries(5)
            .setBaseDelay(Duration.ofSeconds(1))
            .setMaxDelay(Duration.ofSeconds(10));

        ExponentialBackoff retryStrategy = new ExponentialBackoff(options);
        // END: com.azure.core.http.policy.ExponentialBackoffOptions.constructor
    }

    /**
     * Code snippets for using {@link FixedDelay}.
     */
    public void createFixedDelay() {

        // BEGIN: com.azure.core.http.policy.FixedDelay.constructor
        FixedDelay retryStrategy = new FixedDelay(3, Duration.ofSeconds(1));
        RetryPolicy policy = new RetryPolicy(retryStrategy);
        // END: com.azure.core.http.policy.FixedDelay.constructor
    }

    /**
     * Code snippets for using {@link FixedDelayOptions}.
     */
    public void createFixedDelayOptions() {

        // BEGIN: com.azure.core.http.policy.FixedDelayOptions.constructor
        FixedDelayOptions options = new FixedDelayOptions(3, Duration.ofSeconds(1));
        FixedDelay retryStrategy = new FixedDelay(options);
        // END: com.azure.core.http.policy.FixedDelayOptions.constructor
    }

    /**
     * Code snippets for using {@link HostPolicy}.
     */
    public void createHostPolicy() {

        // BEGIN: com.azure.core.http.policy.HostPolicy.constructor
        HostPolicy hostPolicy = new HostPolicy("www.example.com");
        // END: com.azure.core.http.policy.HostPolicy.constructor
    }

    /**
     * Code snippets for using {@link HttpLogDetailLevel}.
     */
    public void createHttpLogDetailLevel() {
        // BEGIN: com.azure.core.http.policy.HttpLogDetailLevel.constructor
        HttpLogOptions logOptions = new HttpLogOptions();
        logOptions.setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS);
        HttpLoggingPolicy loggingPolicy = new HttpLoggingPolicy(logOptions);
        // END: com.azure.core.http.policy.HttpLogDetailLevel.constructor
    }

    /**
     * Code snippets for using {@link HttpLoggingPolicy}.
     */
    public void createHttpLoggingPolicy() {
        // BEGIN: com.azure.core.http.policy.HttpLoggingPolicy.constructor
        HttpLogOptions logOptions = new HttpLogOptions();
        logOptions.setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS);
        HttpLoggingPolicy loggingPolicy = new HttpLoggingPolicy(logOptions);
        // END: com.azure.core.http.policy.HttpLoggingPolicy.constructor
    }

    /**
     * Code snippets for using {@link HttpLogOptions}.
     */
    public void createHttpLogOptions() {
        // BEGIN: com.azure.core.http.policy.HttpLogOptions.constructor
        HttpLogOptions logOptions = new HttpLogOptions();
        logOptions.setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS);
        logOptions.setAllowedHeaderNames(new HashSet<>(Arrays.asList("Date", "x-ms-request-id")));
        logOptions.setAllowedQueryParamNames(new HashSet<>(Arrays.asList("api-version")));
        logOptions.setPrettyPrintBody(true);
        HttpLoggingPolicy loggingPolicy = new HttpLoggingPolicy(logOptions);
        // END: com.azure.core.http.policy.HttpLogOptions.constructor
    }

    /**
     * Code snippets for using {@link HttpPolicyProviders}.
     */
    public void createHttpPolicyProviders() {
        // BEGIN: com.azure.core.http.policy.HttpPolicyProviders.usage
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        // Add policies that should be executed before the retry policy
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        // Add the retry policy
        policies.add(new RetryPolicy());
        // Add policies that should be executed after the retry policy
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        // END: com.azure.core.http.policy.HttpPolicyProviders.usage
    }

    /**
     * Code snippets for using {@link KeyCredentialPolicy}.
     */
    public void createKeyCredentialPolicy() {
        // BEGIN: com.azure.core.http.policy.KeyCredentialPolicy.constructor
        KeyCredential credential = new KeyCredential("my_key");
        KeyCredentialPolicy policy = new KeyCredentialPolicy("my_header", credential);
        // END: com.azure.core.http.policy.KeyCredentialPolicy.constructor
    }

    /**
     * Code snippets for using {@link PortPolicy}.
     */
    public void createPortPolicy() {
        // BEGIN: com.azure.core.http.policy.PortPolicy.constructor
        PortPolicy portPolicy = new PortPolicy(8080, true);
        // END: com.azure.core.http.policy.PortPolicy.constructor
    }

    /**
     * Code snippets for using {@link ProtocolPolicy}.
     */
    public void createProtocolPolicy() {
        // BEGIN: com.azure.core.http.policy.ProtocolPolicy.constructor
        ProtocolPolicy protocolPolicy = new ProtocolPolicy("https", true);
        // END: com.azure.core.http.policy.ProtocolPolicy.constructor
    }

    /**
     * Code snippets for using {@link RequestIdPolicy}.
     */
    public void createRequestIdPolicy() {
        // BEGIN: com.azure.core.http.policy.RequestIdPolicy.constructor
        // Using the default header name
        RequestIdPolicy defaultPolicy = new RequestIdPolicy();
        // Using a custom header name
        RequestIdPolicy customRequestIdPolicy = new RequestIdPolicy("x-ms-my-custom-request-id");
        // END: com.azure.core.http.policy.RequestIdPolicy.constructor
    }

    /**
     * Code snippets for using {@link HttpRetryPolicy}.
     */
    public void createRetryPolicy() {
        // BEGIN: com.azure.core.http.policy.RetryPolicy.constructor
        RetryPolicy retryPolicy = new RetryPolicy();
        // END: com.azure.core.http.policy.RetryPolicy.constructor
    }

    /**
     * Code snippets for using {@link UserAgentPolicy}.
     */
    public void createUserAgentPolicy() {
        // BEGIN: com.azure.core.http.policy.UserAgentPolicy.constructor
        UserAgentPolicy userAgentPolicy = new UserAgentPolicy("MyApp/1.0");
        // END: com.azure.core.http.policy.UserAgentPolicy.constructor
    }
}
