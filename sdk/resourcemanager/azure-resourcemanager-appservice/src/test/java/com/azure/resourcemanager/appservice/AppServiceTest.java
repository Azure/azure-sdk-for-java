// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.appservice.models.AppServiceCertificateOrder;
import com.azure.resourcemanager.appservice.models.AppServiceDomain;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.msi.MsiManager;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryIsoCode;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryPhoneCode;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.test.ResourceManagerTestProxyTestBase;
import com.azure.resourcemanager.test.policy.HttpDebugLoggingPolicy;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeoutException;

/** The base for app service tests. */
public class AppServiceTest extends ResourceManagerTestProxyTestBase {

    static final ClientLogger LOGGER = new ClientLogger(AppServiceTest.class);

    protected ResourceManager resourceManager;
    protected KeyVaultManager keyVaultManager;
    protected AppServiceManager appServiceManager;
    protected MsiManager msiManager;

    protected AppServiceDomain domain;
    protected AppServiceCertificateOrder certificateOrder;
    protected String rgName = "";

    @Override
    protected HttpPipeline buildHttpPipeline(
        TokenCredential credential,
        AzureProfile profile,
        HttpLogOptions httpLogOptions,
        List<HttpPipelinePolicy> policies,
        HttpClient httpClient) {
        return HttpPipelineProvider.buildHttpPipeline(
            credential,
            profile,
            null,
            httpLogOptions,
            null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS),
            policies,
            httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 20);
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        ResourceManagerUtils.InternalRuntimeContext internalContext = new ResourceManagerUtils.InternalRuntimeContext();
        internalContext.setIdentifierFunction(name -> new TestIdentifierProvider(testResourceNamer));

        keyVaultManager = buildManager(KeyVaultManager.class, httpPipeline, profile);
        appServiceManager = buildManager(AppServiceManager.class, httpPipeline, profile);
        msiManager = buildManager(MsiManager.class, httpPipeline, profile);
        resourceManager = appServiceManager.resourceManager();
        setInternalContext(internalContext, appServiceManager, msiManager);

        if (!interceptorManager.isLiveMode()) {
            // Disable `$..appkey` sanitizer for these tests
            interceptorManager.removeSanitizers("AZSDK3466");
        }
        // useExistingDomainAndCertificate();
        // createNewDomainAndCertificate();
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    private void useExistingDomainAndCertificate() {
        String rgName = "rgnemv24d683784f51d";
        String certOrder = "wild2crt8b42374211";
        String domainName = "jsdk79877.com";
        if (System.getenv("appservice-group") != null) {
            rgName = System.getenv("appservice-group");
        }
        if (System.getenv("appservice-domain") != null) {
            domainName = System.getenv("appservice-domain");
        }
        if (System.getenv("appservice-certificateorder") != null) {
            certOrder = System.getenv("appservice-certificateorder");
        }

        domain = appServiceManager.domains().getByResourceGroup(rgName, domainName);
        certificateOrder = appServiceManager.certificateOrders().getByResourceGroup(rgName, certOrder);
    }

    private void createNewDomainAndCertificate() {
        domain =
            appServiceManager
                .domains()
                .define(System.getenv("appservice-domain"))
                .withExistingResourceGroup(System.getenv("appservice-group"))
                .defineRegistrantContact()
                .withFirstName("Jon")
                .withLastName("Doe")
                .withEmail("jondoe@contoso.com")
                .withAddressLine1("123 4th Ave")
                .withCity("Redmond")
                .withStateOrProvince("WA")
                .withCountry(CountryIsoCode.UNITED_STATES)
                .withPostalCode("98052")
                .withPhoneCountryCode(CountryPhoneCode.UNITED_STATES)
                .withPhoneNumber("4258828080")
                .attach()
                .withDomainPrivacyEnabled(true)
                .withAutoRenewEnabled(true)
                .create();
        certificateOrder =
            appServiceManager
                .certificateOrders()
                .define(System.getenv("appservice-certificateorder"))
                .withExistingResourceGroup(System.getenv("appservice-group"))
                .withHostName("*." + domain.name())
                .withWildcardSku()
                .withDomainVerification(domain)
                .withNewKeyVault("graphvault", Region.US_WEST)
                .withValidYears(1)
                .create();
    }

    protected static Response<String> curl(String urlString) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, urlString);
        Mono<Response<String>> response =
            stringResponse(HTTP_PIPELINE.send(request)
                .flatMap(response1 -> {
                    int code = response1.getStatusCode();
                    if (code == 200 || code == 400 || code == 404) {
                        return Mono.just(response1);
                    } else {
                        return Mono.error(new HttpResponseException(response1));
                    }
                })
                .retryWhen(Retry
                    .fixedDelay(3, Duration.ofSeconds(30))
                    .filter(t -> t instanceof TimeoutException)));
        return response.block();
    }

    protected static String post(String urlString, String body) {
        try {
            HttpRequest request = new HttpRequest(HttpMethod.POST, urlString).setBody(body);
            Mono<Response<String>> response =
                stringResponse(HTTP_PIPELINE.send(request)
                    .flatMap(response1 -> {
                        int code = response1.getStatusCode();
                        if (code == 200 || code == 400 || code == 404) {
                            return Mono.just(response1);
                        } else {
                            return Mono.error(new HttpResponseException(response1));
                        }
                    })
                    .retryWhen(Retry
                        .fixedDelay(3, Duration.ofSeconds(30))
                        .filter(t -> t instanceof TimeoutException)));
            Response<String> ret = response.block();
            return ret == null ? null : ret.getValue();
        } catch (Exception e) {
            LOGGER.logThrowableAsError(e);
            return null;
        }
    }

    protected void assertAppRunning(String hostname) {
        if (!isPlaybackMode()) {
            Response<String> response = null;

            final int retryMax = 10;
            for (int retryCount = 0; retryCount <= retryMax; ++retryCount) {
                // wait
                ResourceManagerUtils.sleep(Duration.ofMinutes(1));
                response = curl("https://" + hostname);

                if (response.getStatusCode() == 200) {
                    break;
                }
            }

            Assertions.assertEquals(200, response.getStatusCode());
            Assertions.assertNotNull(response.getValue());
            Assertions.assertTrue(response.getValue().contains("Hello world from linux 4"));
        }
    }

    protected Response<String> getAppResponse(String hostname) {
        Response<String> response = null;
        final int retryMax = 10;
        for (int retryCount = 0; retryCount <= retryMax; ++retryCount) {
            // wait
            ResourceManagerUtils.sleep(Duration.ofMinutes(1));
            response = curl("https://" + hostname);

            if (response.getStatusCode() == 200) {
                break;
            }
        }

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(response.getValue());
        return response;
    }

    private static Mono<Response<String>> stringResponse(Mono<HttpResponse> responseMono) {
        return responseMono.flatMap(response -> response.getBodyAsString()
            .map(str -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), str)));
    }

    private static final HttpPipeline HTTP_PIPELINE = new HttpPipelineBuilder()
        .policies(
            new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC)),
            new RetryPolicy(new FixedDelay(3, Duration.ofSeconds(30)), "Retry-After", ChronoUnit.SECONDS),
            new HttpDebugLoggingPolicy())
        .build();
}
