// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.implementation;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Patch;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ReturnValueWireType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.azure.security.keyvault.certificates.CertificateServiceVersion;
import com.azure.security.keyvault.certificates.models.CertificateContact;
import com.azure.security.keyvault.certificates.models.CertificateContentType;
import com.azure.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.ImportCertificateOptions;
import com.azure.security.keyvault.certificates.models.IssuerProperties;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.azure.security.keyvault.certificates.models.MergeCertificateOptions;
import reactor.core.publisher.Mono;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

public class CertificateClientImpl {
    private static final ClientLogger LOGGER = new ClientLogger(CertificateAsyncClient.class);
    private static final Duration DEFAULT_POLLING_INTERVAL = Duration.ofSeconds(1);
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";

    private final CertificateService service;
    private final HttpPipeline pipeline;
    private final String apiVersion;
    private final String vaultUrl;

    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String ACCEPT_LANGUAGE = "en-US";
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";

    /**
     * Creates a {@link CertificateClientImpl} instance that uses {@code pipeline} to service requests.
     *
     * @param vaultUrl URL for the Azure KeyVault service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     * @param version {@link CertificateServiceVersion} of the service to be used when making requests.
     */
    public CertificateClientImpl(String vaultUrl, HttpPipeline pipeline, CertificateServiceVersion version) {
        Objects.requireNonNull(vaultUrl, KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED);

        this.vaultUrl = vaultUrl;
        this.service = RestProxy.create(CertificateService.class, pipeline);
        this.pipeline = pipeline;
        apiVersion = version.getVersion();
    }

    /**
     * Get the vault endpoint url to which service requests are sent to.
     *
     * @return the vault endpoint url
     */
    public String getVaultUrl() {
        return vaultUrl;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return this.pipeline;
    }

    public Duration getDefaultPollingInterval() {
        return DEFAULT_POLLING_INTERVAL;
    }

    /**
     * The interface defining all the services for {@link CertificateAsyncClient} to be used
     * by the proxy service to perform REST calls.
     *
     * This is package-private so that these REST calls are transparent to the user.
     */
    @Host("{url}")
    @ServiceInterface(name = "KeyVault")
    public interface CertificateService {

        @Post("certificates/{certificate-name}/create")
        @ExpectedResponses({202})
        @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<CertificateOperation>> createCertificateAsync(@HostParam("url") String url,
                                                                    @PathParam("certificate-name") String certificateName,
                                                                    @QueryParam("api-version") String apiVersion,
                                                                    @HeaderParam("accept-language") String acceptLanguage,
                                                                    @BodyParam("application/json") CertificateRequestParameters parameters,
                                                                    @HeaderParam("Content-Type") String type,
                                                                    Context context);

        @Get("certificates/{certificate-name}/pending")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<CertificateOperation>> getCertificateOperationAsync(@HostParam("url") String url,
                                                                          @PathParam("certificate-name") String certificateName,
                                                                          @QueryParam("api-version") String apiVersion,
                                                                          @HeaderParam("accept-language") String acceptLanguage,
                                                                          @HeaderParam("Content-Type") String type,
                                                                          Context context);

        @Patch("certificates/{certificate-name}/pending")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<CertificateOperation>> updateCertificateOperationAsync(@HostParam("url") String url,
                                                                             @PathParam("certificate-name") String certificateName,
                                                                             @QueryParam("api-version") String apiVersion,
                                                                             @HeaderParam("accept-language") String acceptLanguage,
                                                                             @BodyParam("application/json") CertificateOperationUpdateParameter properties,
                                                                             @HeaderParam("Content-Type") String type,
                                                                             Context context);

        @Delete("certificates/{certificate-name}/pending")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<CertificateOperation>> deleteCertificateOperationAsync(@HostParam("url") String url,
                                                                             @PathParam("certificate-name") String certificateName,
                                                                             @QueryParam("api-version") String apiVersion,
                                                                             @HeaderParam("accept-language") String acceptLanguage,
                                                                             @HeaderParam("Content-Type") String type,
                                                                             Context context);

        @Get("certificates/{certificate-name}/{certificate-version}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(code = {403}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<KeyVaultCertificate>> getCertificateAsync(@HostParam("url") String url,
                                                                @PathParam("certificate-name") String certificateName,
                                                                @PathParam("certificate-version") String certificateVersion,
                                                                @QueryParam("api-version") String apiVersion,
                                                                @HeaderParam("accept-language") String acceptLanguage,
                                                                @HeaderParam("Content-Type") String type,
                                                                Context context);

        @Get("certificates/{certificate-name}/{certificate-version}")
        @ExpectedResponses({200, 404, 403})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<KeyVaultCertificateWithPolicy>> getCertificatePollerAsync(@HostParam("url") String url,
                                                                                @PathParam("certificate-name") String certificateName,
                                                                                @PathParam("certificate-version") String certificateVersion,
                                                                                @QueryParam("api-version") String apiVersion,
                                                                                @HeaderParam("accept-language") String acceptLanguage,
                                                                                @HeaderParam("Content-Type") String type,
                                                                                Context context);

        @Get("certificates/{certificate-name}/{certificate-version}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(code = {403}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<KeyVaultCertificateWithPolicy>> getCertificateWithPolicyAsync(@HostParam("url") String url,
                                                                                    @PathParam("certificate-name") String certificateName,
                                                                                    @PathParam("certificate-version") String certificateVersion,
                                                                                    @QueryParam("api-version") String apiVersion,
                                                                                    @HeaderParam("accept-language") String acceptLanguage,
                                                                                    @HeaderParam("Content-Type") String type,
                                                                                    Context context);


        @Get("certificates")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(CertificatePropertiesPage.class)
        Mono<PagedResponse<CertificateProperties>> getCertificatesAsync(@HostParam("url") String url,
                                                                        @QueryParam("maxresults") Integer maxresults,
                                                                        @QueryParam("includePending") Boolean includePending,
                                                                        @QueryParam("api-version") String apiVersion,
                                                                        @HeaderParam("accept-language") String acceptLanguage,
                                                                        @HeaderParam("Content-Type") String type,
                                                                        Context context);


        @Get("{nextUrl}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(CertificatePropertiesPage.class)
        Mono<PagedResponse<CertificateProperties>> getCertificatesAsync(@HostParam("url") String url,
                                                                        @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                                        @HeaderParam("accept-language") String acceptLanguage,
                                                                        @HeaderParam("Content-Type") String type,
                                                                        Context context);

        @Delete("certificates/{certificate-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<DeletedCertificate>> deleteCertificateAsync(@HostParam("url") String url,
                                                                  @PathParam("certificate-name") String certificateName,
                                                                  @QueryParam("api-version") String apiVersion,
                                                                  @HeaderParam("accept-language") String acceptLanguage,
                                                                  @HeaderParam("Content-Type") String type,
                                                                  Context context);

        @Patch("certificates/{certificate-name}/{certificate-version}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {500}, value = HttpResponseException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<KeyVaultCertificate>> updateCertificateAsync(@HostParam("url") String url,
                                                                   @PathParam("certificate-name") String certificateName,
                                                                   @PathParam("certificate-version") String certificateVersion,
                                                                   @QueryParam("api-version") String apiVersion,
                                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                                   @BodyParam("application/json") CertificateUpdateParameters properties,
                                                                   @HeaderParam("Content-Type") String type,
                                                                   Context context);


        @Get("deletedcertificates/{certificate-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<DeletedCertificate>> getDeletedCertificateAsync(@HostParam("url") String url,
                                                                      @PathParam("certificate-name") String certificateName,
                                                                      @QueryParam("api-version") String apiVersion,
                                                                      @HeaderParam("accept-language") String acceptLanguage,
                                                                      @HeaderParam("Content-Type") String type,
                                                                      Context context);

        @Get("deletedcertificates/{certificate-name}")
        @ExpectedResponses({200, 403, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<DeletedCertificate>> getDeletedCertificatePollerAsync(@HostParam("url") String url,
                                                                            @PathParam("certificate-name") String certificateName,
                                                                            @QueryParam("api-version") String apiVersion,
                                                                            @HeaderParam("accept-language") String acceptLanguage,
                                                                            @HeaderParam("Content-Type") String type,
                                                                            Context context);

        @Delete("deletedcertificates/{certificate-name}")
        @ExpectedResponses({204})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> purgeDeletedCertificateAsync(@HostParam("url") String url,
                                                          @PathParam("certificate-name") String certificateName,
                                                          @QueryParam("api-version") String apiVersion,
                                                          @HeaderParam("accept-language") String acceptLanguage,
                                                          @HeaderParam("Content-Type") String type,
                                                          Context context);

        @Post("deletedcertificates/{certificate-name}/recover")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<KeyVaultCertificateWithPolicy>> recoverDeletedCertificateAsync(@HostParam("url") String url,
                                                                                     @PathParam("certificate-name") String certificateName,
                                                                                     @QueryParam("api-version") String apiVersion,
                                                                                     @HeaderParam("accept-language") String acceptLanguage,
                                                                                     @HeaderParam("Content-Type") String type,
                                                                                     Context context);


        @Post("certificates/{certificate-name}/backup")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<CertificateBackup>> backupCertificateAsync(@HostParam("url") String url,
                                                                 @PathParam("certificate-name") String certificateName,
                                                                 @QueryParam("api-version") String apiVersion,
                                                                 @HeaderParam("accept-language") String acceptLanguage,
                                                                 @HeaderParam("Content-Type") String type,
                                                                 Context context);


        @Post("certificates/restore")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<KeyVaultCertificateWithPolicy>> restoreCertificateAsync(@HostParam("url") String url,
                                                                              @QueryParam("api-version") String apiVersion,
                                                                              @HeaderParam("accept-language") String acceptLanguage,
                                                                              @BodyParam("application/json") CertificateRestoreParameters parameters,
                                                                              @HeaderParam("Content-Type") String type,
                                                                              Context context);

        @Get("deletedcertificates")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(DeletedCertificatePage.class)
        Mono<PagedResponse<DeletedCertificate>> getDeletedCertificatesAsync(@HostParam("url") String url,
                                                                            @QueryParam("maxresults") Integer maxresults,
                                                                            @QueryParam("includePending") Boolean includePending,
                                                                            @QueryParam("api-version") String apiVersion,
                                                                            @HeaderParam("accept-language") String acceptLanguage,
                                                                            @HeaderParam("Content-Type") String type,
                                                                            Context context);

        @Get("{nextUrl}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(DeletedCertificatePage.class)
        Mono<PagedResponse<DeletedCertificate>> getDeletedCertificatesAsync(@HostParam("url") String url,
                                                                            @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                                            @HeaderParam("accept-language") String acceptLanguage,
                                                                            @HeaderParam("Content-Type") String type,
                                                                            Context context);

        @Put("certificates/contacts")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(ContactPage.class)
        Mono<PagedResponse<CertificateContact>> setCertificateContactsAsync(@HostParam("url") String url,
                                                                            @QueryParam("api-version") String apiVersion,
                                                                            @HeaderParam("accept-language") String acceptLanguage,
                                                                            @BodyParam("application/json") Contacts contacts,
                                                                            @HeaderParam("Content-Type") String type,
                                                                            Context context);

        @Get("certificates/contacts")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(ContactPage.class)
        Mono<PagedResponse<CertificateContact>> getCertificateContactsAsync(@HostParam("url") String url,
                                                                            @QueryParam("api-version") String apiVersion,
                                                                            @HeaderParam("accept-language") String acceptLanguage,
                                                                            @HeaderParam("Content-Type") String type,
                                                                            Context context);

        @Delete("certificates/contacts")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(ContactPage.class)
        Mono<PagedResponse<CertificateContact>> deleteCertificateContactsAsync(@HostParam("url") String url,
                                                                               @QueryParam("api-version") String apiVersion,
                                                                               @HeaderParam("accept-language") String acceptLanguage,
                                                                               @HeaderParam("Content-Type") String type,
                                                                               Context context);

        @Put("certificates/issuers/{issuer-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<CertificateIssuer>> setCertificateIssuerAsync(@HostParam("url") String url,
                                                                    @QueryParam("api-version") String apiVersion,
                                                                    @HeaderParam("accept-language") String acceptLanguage,
                                                                    @PathParam("issuer-name") String issuerName,
                                                                    @BodyParam("application/json") CertificateIssuerSetParameters parameter,
                                                                    @HeaderParam("Content-Type") String type,
                                                                    Context context);


        @Get("certificates/issuers/{issuer-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<CertificateIssuer>> getCertificateIssuerAsync(@HostParam("url") String url,
                                                                    @QueryParam("api-version") String apiVersion,
                                                                    @HeaderParam("accept-language") String acceptLanguage,
                                                                    @PathParam("issuer-name") String issuerName,
                                                                    @HeaderParam("Content-Type") String type,
                                                                    Context context);


        @Delete("certificates/issuers/{issuer-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<CertificateIssuer>> deleteCertificateIssuerAsync(@HostParam("url") String url,
                                                                       @PathParam("issuer-name") String issuerName,
                                                                       @QueryParam("api-version") String apiVersion,
                                                                       @HeaderParam("accept-language") String acceptLanguage,
                                                                       @HeaderParam("Content-Type") String type,
                                                                       Context context);

        @Patch("certificates/issuers/{issuer-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {500}, value = HttpResponseException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<CertificateIssuer>> updateCertificateIssuerAsync(@HostParam("url") String url,
                                                                       @PathParam("issuer-name") String issuerName,
                                                                       @QueryParam("api-version") String apiVersion,
                                                                       @HeaderParam("accept-language") String acceptLanguage,
                                                                       @BodyParam("application/json") CertificateIssuerUpdateParameters properties,
                                                                       @HeaderParam("Content-Type") String type,
                                                                       Context context);

        @Get("certificates/Issuers")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(IssuerPropertiesPage.class)
        Mono<PagedResponse<IssuerProperties>> getCertificateIssuersAsync(@HostParam("url") String url,
                                                                         @QueryParam("maxresults") Integer maxresults,
                                                                         @QueryParam("api-version") String apiVersion,
                                                                         @HeaderParam("accept-language") String acceptLanguage,
                                                                         @HeaderParam("Content-Type") String type,
                                                                         Context context);


        @Get("{nextUrl}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(IssuerPropertiesPage.class)
        Mono<PagedResponse<IssuerProperties>> getCertificateIssuersAsync(@HostParam("url") String url,
                                                                         @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                                         @HeaderParam("accept-language") String acceptLanguage,
                                                                         @HeaderParam("Content-Type") String type,
                                                                         Context context);


        @Get("certificates/{certificate-name}/versions")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(CertificatePropertiesPage.class)
        Mono<PagedResponse<CertificateProperties>> getCertificateVersionsAsync(@HostParam("url") String url,
                                                                               @PathParam("certificate-name") String certificateName,
                                                                               @QueryParam("maxresults") Integer maxresults,
                                                                               @QueryParam("api-version") String apiVersion,
                                                                               @HeaderParam("accept-language") String acceptLanguage,
                                                                               @HeaderParam("Content-Type") String type,
                                                                               Context context);


        @Post("certificates/{certificate-name}/import")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<KeyVaultCertificateWithPolicy>> importCertificateAsync(@HostParam("url") String url,
                                                                             @PathParam("certificate-name") String certificateName,
                                                                             @QueryParam("api-version") String apiVersion,
                                                                             @HeaderParam("accept-language") String acceptLanguage,
                                                                             @BodyParam("application/json") CertificateImportParameters parameters,
                                                                             @HeaderParam("Content-Type") String type,
                                                                             Context context);

        @Post("certificates/{certificate-name}/pending/merge")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<KeyVaultCertificateWithPolicy>> mergeCertificateAsync(@HostParam("url") String url,
                                                                            @PathParam("certificate-name") String certificateName,
                                                                            @QueryParam("api-version") String apiVersion,
                                                                            @HeaderParam("accept-language") String acceptLanguage,
                                                                            @BodyParam("application/json") CertificateMergeParameters parameters,
                                                                            @HeaderParam("Content-Type") String type,
                                                                            Context context);

        @Get("certificates/{certificate-name}/policy")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<CertificatePolicy>> getCertificatePolicyAsync(@HostParam("url") String url,
                                                                    @QueryParam("api-version") String apiVersion,
                                                                    @HeaderParam("accept-language") String acceptLanguage,
                                                                    @PathParam("certificate-name") String certificateName,
                                                                    @HeaderParam("Content-Type") String type,
                                                                    Context context);

        @Patch("certificates/{certificate-name}/policy")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<CertificatePolicy>> updateCertificatePolicyAsync(@HostParam("url") String url,
                                                                       @QueryParam("api-version") String apiVersion,
                                                                       @HeaderParam("accept-language") String acceptLanguage,
                                                                       @PathParam("certificate-name") String certificateName,
                                                                       @BodyParam("application/json") CertificatePolicyRequest certificatePolicyRequest,
                                                                       @HeaderParam("Content-Type") String type,
                                                                       Context context);

        @Post("certificates/{certificate-name}/create")
        @ExpectedResponses({202})
        @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<CertificateOperation> createCertificate(@HostParam("url") String url,
                                                         @PathParam("certificate-name") String certificateName,
                                                         @QueryParam("api-version") String apiVersion,
                                                         @HeaderParam("accept-language") String acceptLanguage,
                                                         @BodyParam("application/json") CertificateRequestParameters parameters,
                                                         @HeaderParam("Content-Type") String type,
                                                         Context context);

        @Get("certificates/{certificate-name}/pending")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<CertificateOperation> getCertificateOperation(@HostParam("url") String url,
                                                               @PathParam("certificate-name") String certificateName,
                                                               @QueryParam("api-version") String apiVersion,
                                                               @HeaderParam("accept-language") String acceptLanguage,
                                                               @HeaderParam("Content-Type") String type,
                                                               Context context);

        @Patch("certificates/{certificate-name}/pending")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<CertificateOperation> updateCertificateOperation(@HostParam("url") String url,
                                                                  @PathParam("certificate-name") String certificateName,
                                                                  @QueryParam("api-version") String apiVersion,
                                                                  @HeaderParam("accept-language") String acceptLanguage,
                                                                  @BodyParam("application/json") CertificateOperationUpdateParameter properties,
                                                                  @HeaderParam("Content-Type") String type,
                                                                  Context context);

        @Delete("certificates/{certificate-name}/pending")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<CertificateOperation> deleteCertificateOperation(@HostParam("url") String url,
                                                                  @PathParam("certificate-name") String certificateName,
                                                                  @QueryParam("api-version") String apiVersion,
                                                                  @HeaderParam("accept-language") String acceptLanguage,
                                                                  @HeaderParam("Content-Type") String type,
                                                                  Context context);

        @Get("certificates/{certificate-name}/{certificate-version}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(code = {403}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<KeyVaultCertificate> getCertificate(@HostParam("url") String url,
                                                     @PathParam("certificate-name") String certificateName,
                                                     @PathParam("certificate-version") String certificateVersion,
                                                     @QueryParam("api-version") String apiVersion,
                                                     @HeaderParam("accept-language") String acceptLanguage,
                                                     @HeaderParam("Content-Type") String type,
                                                     Context context);

        @Get("certificates/{certificate-name}/{certificate-version}")
        @ExpectedResponses({200, 404, 403})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<KeyVaultCertificateWithPolicy> getCertificatePoller(@HostParam("url") String url,
                                                                     @PathParam("certificate-name") String certificateName,
                                                                     @PathParam("certificate-version") String certificateVersion,
                                                                     @QueryParam("api-version") String apiVersion,
                                                                     @HeaderParam("accept-language") String acceptLanguage,
                                                                     @HeaderParam("Content-Type") String type,
                                                                     Context context);

        @Get("certificates/{certificate-name}/{certificate-version}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(code = {403}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<KeyVaultCertificateWithPolicy> getCertificateWithPolicy(@HostParam("url") String url,
                                                                         @PathParam("certificate-name") String certificateName,
                                                                         @PathParam("certificate-version") String certificateVersion,
                                                                         @QueryParam("api-version") String apiVersion,
                                                                         @HeaderParam("accept-language") String acceptLanguage,
                                                                         @HeaderParam("Content-Type") String type,
                                                                         Context context);


        @Get("certificates")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(CertificatePropertiesPage.class)
        PagedResponse<CertificateProperties> getCertificates(@HostParam("url") String url,
                                                             @QueryParam("maxresults") Integer maxresults,
                                                             @QueryParam("includePending") Boolean includePending,
                                                             @QueryParam("api-version") String apiVersion,
                                                             @HeaderParam("accept-language") String acceptLanguage,
                                                             @HeaderParam("Content-Type") String type,
                                                             Context context);


        @Get("{nextUrl}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(CertificatePropertiesPage.class)
        PagedResponse<CertificateProperties> getCertificates(@HostParam("url") String url,
                                                             @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                             @HeaderParam("accept-language") String acceptLanguage,
                                                             @HeaderParam("Content-Type") String type,
                                                             Context context);

        @Delete("certificates/{certificate-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<DeletedCertificate> deleteCertificate(@HostParam("url") String url,
                                                       @PathParam("certificate-name") String certificateName,
                                                       @QueryParam("api-version") String apiVersion,
                                                       @HeaderParam("accept-language") String acceptLanguage,
                                                       @HeaderParam("Content-Type") String type,
                                                       Context context);

        @Patch("certificates/{certificate-name}/{certificate-version}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {500}, value = HttpResponseException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<KeyVaultCertificate> updateCertificate(@HostParam("url") String url,
                                                        @PathParam("certificate-name") String certificateName,
                                                        @PathParam("certificate-version") String certificateVersion,
                                                        @QueryParam("api-version") String apiVersion,
                                                        @HeaderParam("accept-language") String acceptLanguage,
                                                        @BodyParam("application/json") CertificateUpdateParameters properties,
                                                        @HeaderParam("Content-Type") String type,
                                                        Context context);


        @Get("deletedcertificates/{certificate-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<DeletedCertificate> getDeletedCertificate(@HostParam("url") String url,
                                                           @PathParam("certificate-name") String certificateName,
                                                           @QueryParam("api-version") String apiVersion,
                                                           @HeaderParam("accept-language") String acceptLanguage,
                                                           @HeaderParam("Content-Type") String type,
                                                           Context context);

        @Get("deletedcertificates/{certificate-name}")
        @ExpectedResponses({200, 403, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<DeletedCertificate> getDeletedCertificatePoller(@HostParam("url") String url,
                                                                 @PathParam("certificate-name") String certificateName,
                                                                 @QueryParam("api-version") String apiVersion,
                                                                 @HeaderParam("accept-language") String acceptLanguage,
                                                                 @HeaderParam("Content-Type") String type,
                                                                 Context context);

        @Delete("deletedcertificates/{certificate-name}")
        @ExpectedResponses({204})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<Void> purgeDeletedCertificate(@HostParam("url") String url,
                                               @PathParam("certificate-name") String certificateName,
                                               @QueryParam("api-version") String apiVersion,
                                               @HeaderParam("accept-language") String acceptLanguage,
                                               @HeaderParam("Content-Type") String type,
                                               Context context);

        @Post("deletedcertificates/{certificate-name}/recover")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<KeyVaultCertificateWithPolicy> recoverDeletedCertificate(@HostParam("url") String url,
                                                                          @PathParam("certificate-name") String certificateName,
                                                                          @QueryParam("api-version") String apiVersion,
                                                                          @HeaderParam("accept-language") String acceptLanguage,
                                                                          @HeaderParam("Content-Type") String type,
                                                                          Context context);


        @Post("certificates/{certificate-name}/backup")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<CertificateBackup> backupCertificate(@HostParam("url") String url,
                                                      @PathParam("certificate-name") String certificateName,
                                                      @QueryParam("api-version") String apiVersion,
                                                      @HeaderParam("accept-language") String acceptLanguage,
                                                      @HeaderParam("Content-Type") String type,
                                                      Context context);


        @Post("certificates/restore")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<KeyVaultCertificateWithPolicy> restoreCertificate(@HostParam("url") String url,
                                                                   @QueryParam("api-version") String apiVersion,
                                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                                   @BodyParam("application/json") CertificateRestoreParameters parameters,
                                                                   @HeaderParam("Content-Type") String type,
                                                                   Context context);

        @Get("deletedcertificates")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(DeletedCertificatePage.class)
        PagedResponse<DeletedCertificate> getDeletedCertificates(@HostParam("url") String url,
                                                                 @QueryParam("maxresults") Integer maxresults,
                                                                 @QueryParam("includePending") Boolean includePending,
                                                                 @QueryParam("api-version") String apiVersion,
                                                                 @HeaderParam("accept-language") String acceptLanguage,
                                                                 @HeaderParam("Content-Type") String type,
                                                                 Context context);

        @Get("{nextUrl}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(DeletedCertificatePage.class)
        PagedResponse<DeletedCertificate> getDeletedCertificates(@HostParam("url") String url,
                                                                 @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                                 @HeaderParam("accept-language") String acceptLanguage,
                                                                 @HeaderParam("Content-Type") String type,
                                                                 Context context);

        @Put("certificates/contacts")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(ContactPage.class)
        PagedResponse<CertificateContact> setCertificateContacts(@HostParam("url") String url,
                                                                 @QueryParam("api-version") String apiVersion,
                                                                 @HeaderParam("accept-language") String acceptLanguage,
                                                                 @BodyParam("application/json") Contacts contacts,
                                                                 @HeaderParam("Content-Type") String type,
                                                                 Context context);

        @Get("certificates/contacts")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(ContactPage.class)
        PagedResponse<CertificateContact> getCertificateContacts(@HostParam("url") String url,
                                                                 @QueryParam("api-version") String apiVersion,
                                                                 @HeaderParam("accept-language") String acceptLanguage,
                                                                 @HeaderParam("Content-Type") String type,
                                                                 Context context);

        @Delete("certificates/contacts")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(ContactPage.class)
        PagedResponse<CertificateContact> deleteCertificateContacts(@HostParam("url") String url,
                                                                    @QueryParam("api-version") String apiVersion,
                                                                    @HeaderParam("accept-language") String acceptLanguage,
                                                                    @HeaderParam("Content-Type") String type,
                                                                    Context context);

        @Put("certificates/issuers/{issuer-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<CertificateIssuer> setCertificateIssuer(@HostParam("url") String url,
                                                         @QueryParam("api-version") String apiVersion,
                                                         @HeaderParam("accept-language") String acceptLanguage,
                                                         @PathParam("issuer-name") String issuerName,
                                                         @BodyParam("application/json") CertificateIssuerSetParameters parameter,
                                                         @HeaderParam("Content-Type") String type,
                                                         Context context);


        @Get("certificates/issuers/{issuer-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<CertificateIssuer> getCertificateIssuer(@HostParam("url") String url,
                                                         @QueryParam("api-version") String apiVersion,
                                                         @HeaderParam("accept-language") String acceptLanguage,
                                                         @PathParam("issuer-name") String issuerName,
                                                         @HeaderParam("Content-Type") String type,
                                                         Context context);


        @Delete("certificates/issuers/{issuer-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<CertificateIssuer> deleteCertificateIssuer(@HostParam("url") String url,
                                                            @PathParam("issuer-name") String issuerName,
                                                            @QueryParam("api-version") String apiVersion,
                                                            @HeaderParam("accept-language") String acceptLanguage,
                                                            @HeaderParam("Content-Type") String type,
                                                            Context context);

        @Patch("certificates/issuers/{issuer-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {500}, value = HttpResponseException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<CertificateIssuer> updateCertificateIssuer(@HostParam("url") String url,
                                                            @PathParam("issuer-name") String issuerName,
                                                            @QueryParam("api-version") String apiVersion,
                                                            @HeaderParam("accept-language") String acceptLanguage,
                                                            @BodyParam("application/json") CertificateIssuerUpdateParameters properties,
                                                            @HeaderParam("Content-Type") String type,
                                                            Context context);

        @Get("certificates/Issuers")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(IssuerPropertiesPage.class)
        PagedResponse<IssuerProperties> getCertificateIssuers(@HostParam("url") String url,
                                                              @QueryParam("maxresults") Integer maxresults,
                                                              @QueryParam("api-version") String apiVersion,
                                                              @HeaderParam("accept-language") String acceptLanguage,
                                                              @HeaderParam("Content-Type") String type,
                                                              Context context);


        @Get("{nextUrl}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(IssuerPropertiesPage.class)
        PagedResponse<IssuerProperties> getCertificateIssuers(@HostParam("url") String url,
                                                              @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                              @HeaderParam("accept-language") String acceptLanguage,
                                                              @HeaderParam("Content-Type") String type,
                                                              Context context);


        @Get("certificates/{certificate-name}/versions")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @ReturnValueWireType(CertificatePropertiesPage.class)
        PagedResponse<CertificateProperties> getCertificateVersions(@HostParam("url") String url,
                                                                    @PathParam("certificate-name") String certificateName,
                                                                    @QueryParam("maxresults") Integer maxresults,
                                                                    @QueryParam("api-version") String apiVersion,
                                                                    @HeaderParam("accept-language") String acceptLanguage,
                                                                    @HeaderParam("Content-Type") String type,
                                                                    Context context);


        @Post("certificates/{certificate-name}/import")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<KeyVaultCertificateWithPolicy> importCertificate(@HostParam("url") String url,
                                                                  @PathParam("certificate-name") String certificateName,
                                                                  @QueryParam("api-version") String apiVersion,
                                                                  @HeaderParam("accept-language") String acceptLanguage,
                                                                  @BodyParam("application/json") CertificateImportParameters parameters,
                                                                  @HeaderParam("Content-Type") String type,
                                                                  Context context);

        @Post("certificates/{certificate-name}/pending/merge")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<KeyVaultCertificateWithPolicy> mergeCertificate(@HostParam("url") String url,
                                                                 @PathParam("certificate-name") String certificateName,
                                                                 @QueryParam("api-version") String apiVersion,
                                                                 @HeaderParam("accept-language") String acceptLanguage,
                                                                 @BodyParam("application/json") CertificateMergeParameters parameters,
                                                                 @HeaderParam("Content-Type") String type,
                                                                 Context context);

        @Get("certificates/{certificate-name}/policy")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<CertificatePolicy> getCertificatePolicy(@HostParam("url") String url,
                                                         @QueryParam("api-version") String apiVersion,
                                                         @HeaderParam("accept-language") String acceptLanguage,
                                                         @PathParam("certificate-name") String certificateName,
                                                         @HeaderParam("Content-Type") String type,
                                                         Context context);

        @Patch("certificates/{certificate-name}/policy")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<CertificatePolicy> updateCertificatePolicy(@HostParam("url") String url,
                                                            @QueryParam("api-version") String apiVersion,
                                                            @HeaderParam("accept-language") String acceptLanguage,
                                                            @PathParam("certificate-name") String certificateName,
                                                            @BodyParam("application/json") CertificatePolicyRequest certificatePolicyRequest,
                                                            @HeaderParam("Content-Type") String type,
                                                            Context context);
    }

    public PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> beginCreateCertificateAsync(String certificateName,
                                                                                                       CertificatePolicy policy,
                                                                                                       Boolean isEnabled,
                                                                                                       Map<String, String> tags) {
        return new PollerFlux<>(getDefaultPollingInterval(),
            activationOperationAsync(certificateName, policy, isEnabled, tags),
            createPollOperationAsync(certificateName),
            cancelOperationAsync(certificateName),
            fetchResultOperationAsync(certificateName));
    }

    public SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> beginCreateCertificate(String certificateName,
                                                                                                  CertificatePolicy policy,
                                                                                                  Boolean isEnabled,
                                                                                                  Map<String, String> tags,
                                                                                                  Context context) {
        return SyncPoller.createPoller(getDefaultPollingInterval(),
            cxt ->
                new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                    activationOperation(certificateName, policy, isEnabled, tags, context).apply(cxt)),
            createPollOperation(certificateName, context),
            cancelOperation(certificateName, context),
            fetchResultOperation(certificateName, context));
    }

    private Function<PollingContext<CertificateOperation>, Mono<CertificateOperation>> activationOperationAsync(String certificateName,
                                                                                                                CertificatePolicy policy,
                                                                                                                boolean enabled,
                                                                                                                Map<String, String> tags) {
        return (pollingContext) ->
            withContext(context -> createCertificateWithResponseAsync(certificateName, policy, enabled, tags, context))
                .flatMap(certificateOperationResponse -> Mono.just(certificateOperationResponse.getValue()));
    }

    private Function<PollingContext<CertificateOperation>, CertificateOperation> activationOperation(String certificateName,
                                                                                                     CertificatePolicy policy,
                                                                                                     boolean enabled,
                                                                                                     Map<String, String> tags,
                                                                                                     Context context) {
        return (pollingContext) ->
            createCertificateWithResponse(certificateName, policy, enabled, tags, context).getValue();
    }

    private BiFunction<PollingContext<CertificateOperation>, PollResponse<CertificateOperation>, Mono<CertificateOperation>> cancelOperationAsync(String certificateName) {
        return (pollingContext, firstResponse) ->
            withContext(context -> cancelCertificateOperationWithResponseAsync(certificateName, context))
                .flatMap(FluxUtil::toMono);
    }
    private BiFunction<PollingContext<CertificateOperation>, PollResponse<CertificateOperation>, CertificateOperation> cancelOperation(String certificateName, Context context) {
        return (pollingContext, firstResponse) ->
            cancelCertificateOperationWithResponse(certificateName, context).getValue();
    }

    private Function<PollingContext<CertificateOperation>,
        Mono<KeyVaultCertificateWithPolicy>> fetchResultOperationAsync(String certificateName) {
        return (pollingContext) ->
            withContext(context -> getCertificateWithResponseAsync(certificateName, "", context))
                .flatMap(certificateResponse -> Mono.just(certificateResponse.getValue()));
    }

    /* Async polling operation to poll on create certificate operation status. */

    private Function<PollingContext<CertificateOperation>, KeyVaultCertificateWithPolicy> fetchResultOperation(String certificateName, Context context) {
        return (pollingContext) -> getCertificateWithResponse(certificateName, "", context).getValue();
    }

    private Function<PollingContext<CertificateOperation>, Mono<PollResponse<CertificateOperation>>> createPollOperationAsync(String certificateName) {
        return (pollingContext) -> {
            try {
                return withContext(context ->
                    service.getCertificateOperationAsync(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
                        CONTENT_TYPE_HEADER_VALUE, context))
                    .map(this::processCertificateOperationResponse);
            } catch (RuntimeException e) {
                return monoError(LOGGER, e);
            }
        };
    }

    /* Sync polling operation to poll on create certificate operation status. */
    private Function<PollingContext<CertificateOperation>, PollResponse<CertificateOperation>> createPollOperation(String certificateName, Context context) {
        return (pollingContext) -> {
            try {
                Context contextToUse = context == null ? Context.NONE : context;
                contextToUse = enableSyncRestProxy(contextToUse);
                Response<CertificateOperation> operationResponse =
                    service.getCertificateOperation(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
                        CONTENT_TYPE_HEADER_VALUE, contextToUse);

                return processCertificateOperationResponse(operationResponse);
            } catch (RuntimeException e) {
                throw LOGGER.logExceptionAsError(e);
            }
        };
    }

    private PollResponse<CertificateOperation> processCertificateOperationResponse(Response<CertificateOperation> certificateOperationResponse) {
        LongRunningOperationStatus status;

        switch (certificateOperationResponse.getValue().getStatus()) {
            case "inProgress":
                status = LongRunningOperationStatus.IN_PROGRESS;

                break;
            case "completed":
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;

                break;
            case "failed":
                status = LongRunningOperationStatus.FAILED;

                break;
            default:
                //should not reach here
                status =
                    LongRunningOperationStatus.fromString(certificateOperationResponse.getValue().getStatus(), true);

                break;
        }

        return new PollResponse<>(status, certificateOperationResponse.getValue());
    }

    public Mono<Response<CertificateOperation>> cancelCertificateOperationWithResponseAsync(String certificateName,
                                                                                            Context context) {
        CertificateOperationUpdateParameter parameter =
            new CertificateOperationUpdateParameter().cancellationRequested(true);

        return service.updateCertificateOperationAsync(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
                parameter, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Cancelling certificate operation - {}", certificateName))
            .doOnSuccess(response ->
                LOGGER.verbose("Cancelled the certificate operation - {}", response.getValue().getStatus()))
            .doOnError(error ->
                LOGGER.warning("Failed to cancel the certificate operation - {}", certificateName, error));
    }

    public Response<CertificateOperation> cancelCertificateOperationWithResponse(String certificateName,
                                                                                 Context context) {
        CertificateOperationUpdateParameter parameter =
            new CertificateOperationUpdateParameter().cancellationRequested(true);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.updateCertificateOperation(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE, parameter,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    private Mono<Response<CertificateOperation>> createCertificateWithResponseAsync(String certificateName,
                                                                                    CertificatePolicy certificatePolicy,
                                                                                    boolean enabled,
                                                                                    Map<String, String> tags,
                                                                                    Context context) {
        CertificateRequestParameters certificateRequestParameters = new CertificateRequestParameters()
            .certificatePolicy(new CertificatePolicyRequest(certificatePolicy))
            .certificateAttributes(new CertificateRequestAttributes().enabled(enabled))
            .tags(tags);

        return service.createCertificateAsync(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
                certificateRequestParameters, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Starting creation of certificate - {}", certificateName))
            .doOnSuccess(response -> LOGGER.verbose("Started creation of certificate - {}", certificateName))
            .doOnError(error -> LOGGER.warning("Failed to create the certificate - {}", certificateName, error));
    }

    private Response<CertificateOperation> createCertificateWithResponse(String certificateName,
                                                                         CertificatePolicy certificatePolicy,
                                                                         boolean enabled,
                                                                         Map<String, String> tags,
                                                                         Context context) {
        CertificateRequestParameters certificateRequestParameters = new CertificateRequestParameters()
            .certificatePolicy(new CertificatePolicyRequest(certificatePolicy))
            .certificateAttributes(new CertificateRequestAttributes().enabled(enabled))
            .tags(tags);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.createCertificate(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
                certificateRequestParameters, CONTENT_TYPE_HEADER_VALUE, context);
    }

    public PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> getCertificateOperationAsync(String certificateName) {
        return new PollerFlux<>(getDefaultPollingInterval(),
            (pollingContext) -> Mono.empty(),
            createPollOperationAsync(certificateName),
            cancelOperationAsync(certificateName),
            fetchResultOperationAsync(certificateName));
    }

    public SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> getCertificateOperation(String certificateName, Context context) {
        return SyncPoller.createPoller(getDefaultPollingInterval(),
            (pollingContext) -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null),
            createPollOperation(certificateName, context),
            cancelOperation(certificateName, context),
            fetchResultOperation(certificateName, context));
    }

    public Mono<Response<KeyVaultCertificateWithPolicy>> getCertificateWithResponseAsync(String certificateName,
                                                                                         String version,
                                                                                         Context context) {
        return service.getCertificateWithPolicyAsync(vaultUrl, certificateName, version, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Retrieving certificate - {}", certificateName))
            .doOnSuccess(response ->
                LOGGER.verbose("Retrieved the certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> LOGGER.warning("Failed to Retrieve the certificate - {}", certificateName, error));
    }

    public Response<KeyVaultCertificateWithPolicy> getCertificateWithResponse(String certificateName, String version,
                                                                              Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getCertificateWithPolicy(vaultUrl, certificateName, version, apiVersion, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE,
            context);
    }

    public Mono<Response<KeyVaultCertificate>> getCertificateVersionWithResponseAsync(String certificateName,
                                                                                      String version, Context context) {
        return service.getCertificateAsync(vaultUrl, certificateName, version, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Retrieving certificate - {}", certificateName))
            .doOnSuccess(response ->
                LOGGER.verbose("Retrieved the certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> LOGGER.warning("Failed to Retrieve the certificate - {}", certificateName, error));
    }

    public Response<KeyVaultCertificate> getCertificateVersionWithResponse(String certificateName, String version,
                                                                           Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getCertificate(vaultUrl, certificateName, version, apiVersion, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context);
    }


    public Mono<Response<KeyVaultCertificate>> updateCertificatePropertiesWithResponseAsync(CertificateProperties properties,
                                                                                            Context context) {
        Objects.requireNonNull(properties, "properties' cannot be null.");

        CertificateUpdateParameters parameters = new CertificateUpdateParameters()
            .tags(properties.getTags())
            .certificateAttributes(new CertificateRequestAttributes(properties));

        return service.updateCertificateAsync(vaultUrl, properties.getName(), properties.getVersion(), apiVersion,
                ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE,
                context)
            .doOnRequest(ignored -> LOGGER.verbose("Updating certificate - {}", properties.getName()))
            .doOnSuccess(response -> LOGGER.verbose("Updated the certificate - {}", properties.getName()))
            .doOnError(error -> LOGGER.warning("Failed to update the certificate - {}", properties.getName(), error));
    }

    public Response<KeyVaultCertificate> updateCertificatePropertiesWithResponse(CertificateProperties properties,
                                                                                 Context context) {
        Objects.requireNonNull(properties, "properties' cannot be null.");

        CertificateUpdateParameters parameters = new CertificateUpdateParameters()
            .tags(properties.getTags())
            .certificateAttributes(new CertificateRequestAttributes(properties));
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.updateCertificate(vaultUrl, properties.getName(), properties.getVersion(), apiVersion,
            ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context);
    }


    public PollerFlux<DeletedCertificate, Void> beginDeleteCertificateAsync(String certificateName) {
        return new PollerFlux<>(getDefaultPollingInterval(),
            activationOperationAsync(certificateName),
            createDeletePollOperationAsync(certificateName),
            (context, firstResponse) -> Mono.empty(),
            (context) -> Mono.empty());
    }

    public SyncPoller<DeletedCertificate, Void> beginDeleteCertificate(String certificateName, Context context) {
        return SyncPoller.createPoller(getDefaultPollingInterval(),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                activationOperation(certificateName, context).apply(cxt)),
            createDeletePollOperation(certificateName, context),
            (pollingContext, firstResponse) -> null,
            (pollingContext) -> null);
    }

    private Function<PollingContext<DeletedCertificate>, Mono<DeletedCertificate>> activationOperationAsync(String certificateName) {
        return (pollingContext) ->
            withContext(context -> deleteCertificateWithResponseAsync(certificateName, context))
                .flatMap(deletedCertificateResponse -> Mono.just(deletedCertificateResponse.getValue()));
    }

    private Function<PollingContext<DeletedCertificate>, DeletedCertificate> activationOperation(String certificateName,
                                                                                                 Context context) {
        return (pollingContext) -> deleteCertificateWithResponse(certificateName, context).getValue();
    }

    /* Async polling operation to poll on create delete certificate operation status. */
    private Function<PollingContext<DeletedCertificate>, Mono<PollResponse<DeletedCertificate>>> createDeletePollOperationAsync(String keyName) {
        return (pollingContext) ->
            withContext(context -> service.getDeletedCertificatePollerAsync(vaultUrl, keyName, apiVersion,
                ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context))
                .flatMap(deletedCertificateResponse -> {
                    if (deletedCertificateResponse.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                        return Mono.defer(() ->
                            Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                                pollingContext.getLatestResponse().getValue())));
                    }

                    if (deletedCertificateResponse.getStatusCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                        return Mono.defer(() ->
                            Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                                pollingContext.getLatestResponse().getValue())));
                    }

                    return Mono.defer(() ->
                        Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                            deletedCertificateResponse.getValue())));
                })
                // This means either vault has soft-delete disabled or permission is not granted for the get deleted
                // certificate operation. In both cases deletion operation was successful when activation operation
                // succeeded before reaching here.
                .onErrorReturn(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue()));
    }

    /* Sync polling operation to poll on create delete certificate operation status. */
    private Function<PollingContext<DeletedCertificate>, PollResponse<DeletedCertificate>> createDeletePollOperation(String keyName, Context context) {
        return (pollingContext) -> {
            try {
                Context contextToUse = context == null ? Context.NONE : context;
                contextToUse = enableSyncRestProxy(contextToUse);
                Response<DeletedCertificate> deletedCertificateResponse =
                    service.getDeletedCertificatePoller(vaultUrl, keyName, apiVersion, ACCEPT_LANGUAGE,
                        CONTENT_TYPE_HEADER_VALUE, contextToUse);

                if (deletedCertificateResponse.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                        pollingContext.getLatestResponse().getValue());
                }

                if (deletedCertificateResponse.getStatusCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                    return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                        pollingContext.getLatestResponse().getValue());
                }

                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    deletedCertificateResponse.getValue());
            } catch (HttpResponseException e) {
                // This means either vault has soft-delete disabled or permission is not granted for the get deleted
                // certificate operation. In both cases deletion operation was successful when activation operation
                // succeeded before reaching here.
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue());
            }
        };
    }

    private Mono<Response<DeletedCertificate>> deleteCertificateWithResponseAsync(String certificateName,
                                                                                  Context context) {
        return service.deleteCertificateAsync(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Deleting certificate - {}", certificateName))
            .doOnSuccess(response -> LOGGER.verbose("Deleted the certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> LOGGER.warning("Failed to delete the certificate - {}", certificateName, error));
    }

    private Response<DeletedCertificate> deleteCertificateWithResponse(String certificateName, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.deleteCertificate(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    public Mono<Response<DeletedCertificate>> getDeletedCertificateWithResponseAsync(String certificateName, Context context) {
        return service.getDeletedCertificateAsync(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context)
            .doOnRequest(ignored -> LOGGER.verbose("Retrieving deleted certificate - {}", certificateName))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved the deleted certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> LOGGER.warning("Failed to Retrieve the deleted certificate - {}", certificateName, error));
    }

    public Response<DeletedCertificate> getDeletedCertificateWithResponse(String certificateName, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getDeletedCertificate(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context);
    }


    public Mono<Response<Void>> purgeDeletedCertificateWithResponseAsync(String certificateName, Context context) {
        return service.purgeDeletedCertificateAsync(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Purging certificate - {}", certificateName))
            .doOnSuccess(response -> LOGGER.verbose("Purged the certificate - {}", response.getStatusCode()))
            .doOnError(error -> LOGGER.warning("Failed to purge the certificate - {}", certificateName, error));
    }

    public Response<Void> purgeDeletedCertificateWithResponse(String certificateName, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.purgeDeletedCertificate(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context);
    }


    public PollerFlux<KeyVaultCertificateWithPolicy, Void> beginRecoverDeletedCertificateAsync(String certificateName) {
        return new PollerFlux<>(getDefaultPollingInterval(),
            recoverActivationOperationAsync(certificateName),
            createRecoverPollOperationAsync(certificateName),
            (context, firstResponse) -> Mono.empty(),
            context -> Mono.empty());
    }

    public SyncPoller<KeyVaultCertificateWithPolicy, Void> beginRecoverDeletedCertificate(String certificateName, Context context) {
        return SyncPoller.createPoller(getDefaultPollingInterval(),
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                recoverActivationOperation(certificateName, context).apply(cxt)),
            createRecoverPollOperation(certificateName, context),
            (pollingContext, firstResponse) -> null,
            (pollingContext) -> null);
    }

    private Function<PollingContext<KeyVaultCertificateWithPolicy>, Mono<KeyVaultCertificateWithPolicy>> recoverActivationOperationAsync(String certificateName) {
        return (pollingContext) ->
            withContext(context -> recoverDeletedCertificateWithResponseAsync(certificateName, context))
                .flatMap(certificateResponse -> Mono.just(certificateResponse.getValue()));
    }

    private Function<PollingContext<KeyVaultCertificateWithPolicy>, KeyVaultCertificateWithPolicy> recoverActivationOperation(String certificateName, Context context) {
        return (pollingContext) -> recoverDeletedCertificateWithResponse(certificateName, context).getValue();
    }

    /* Async polling operation to poll on create recover certificate operation status. */
    private Function<PollingContext<KeyVaultCertificateWithPolicy>, Mono<PollResponse<KeyVaultCertificateWithPolicy>>> createRecoverPollOperationAsync(String keyName) {
        return (pollingContext) ->
            withContext(context -> service.getCertificatePollerAsync(vaultUrl, keyName, "", apiVersion,
                ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context))
                .flatMap(certificateResponse -> {
                    if (certificateResponse.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                        return Mono.defer(() ->
                            Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                                pollingContext.getLatestResponse().getValue())));
                    }

                    if (certificateResponse.getStatusCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                        return Mono.defer(() ->
                            Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                                pollingContext.getLatestResponse().getValue())));
                    }

                    return Mono.defer(() ->
                        Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                            certificateResponse.getValue())));
                })
                // This means permission is not granted for the get deleted key operation.
                // In both cases deletion operation was successful when activation operation succeeded before reaching
                // here.
                .onErrorReturn(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue()));
    }

    /* Sync polling operation to poll on create recover certificate operation status. */
    private Function<PollingContext<KeyVaultCertificateWithPolicy>, PollResponse<KeyVaultCertificateWithPolicy>> createRecoverPollOperation(String keyName, Context context) {
        return (pollingContext) -> {
            try {
                Response<KeyVaultCertificateWithPolicy> certificateResponse =
                    service.getCertificatePoller(vaultUrl, keyName, "", apiVersion, ACCEPT_LANGUAGE,
                        CONTENT_TYPE_HEADER_VALUE, context);

                if (certificateResponse.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                        pollingContext.getLatestResponse().getValue());
                }

                if (certificateResponse.getStatusCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                    return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                        pollingContext.getLatestResponse().getValue());
                }

                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    certificateResponse.getValue());
            } catch (HttpResponseException e) {
                // This means permission is not granted for the get deleted key operation.
                // In both cases deletion operation was successful when activation operation succeeded before reaching
                // here.
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue());
            }
        };
    }

    private Mono<Response<KeyVaultCertificateWithPolicy>> recoverDeletedCertificateWithResponseAsync(String certificateName,
                                                                                                     Context context) {
        return service.recoverDeletedCertificateAsync(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Recovering deleted certificate - {}", certificateName))
            .doOnSuccess(response ->
                LOGGER.verbose("Recovered the deleted certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> LOGGER.warning("Failed to recover the deleted certificate - {}", certificateName, error));
    }

    private Response<KeyVaultCertificateWithPolicy> recoverDeletedCertificateWithResponse(String certificateName,
                                                                                          Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.recoverDeletedCertificate(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    public Mono<Response<byte[]>> backupCertificateWithResponseAsync(String certificateName, Context context) {
        return service.backupCertificateAsync(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Backing up certificate - {}", certificateName))
            .doOnSuccess(response -> LOGGER.verbose("Backed up the certificate - {}", response.getStatusCode()))
            .doOnError(error -> LOGGER.warning("Failed to back up the certificate - {}", certificateName, error))
            .flatMap(certificateBackupResponse ->
                Mono.just(new SimpleResponse<>(certificateBackupResponse.getRequest(),
                    certificateBackupResponse.getStatusCode(), certificateBackupResponse.getHeaders(),
                    certificateBackupResponse.getValue().getValue())));
    }

    public Response<byte[]> backupCertificateWithResponse(String certificateName, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);
        Response<CertificateBackup> certificateBackupResponse = service.backupCertificate(vaultUrl, certificateName,
            apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context);

        return new SimpleResponse<>(certificateBackupResponse.getRequest(), certificateBackupResponse.getStatusCode(),
            certificateBackupResponse.getHeaders(), certificateBackupResponse.getValue().getValue());
    }

    public Mono<Response<KeyVaultCertificateWithPolicy>> restoreCertificateBackupWithResponseAsync(byte[] backup, Context context) {
        CertificateRestoreParameters parameters = new CertificateRestoreParameters().certificateBundleBackup(backup);

        return service.restoreCertificateAsync(vaultUrl, apiVersion, ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Restoring the certificate"))
            .doOnSuccess(response ->
                LOGGER.verbose("Restored the certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> LOGGER.warning("Failed to restore the certificate - {}", error));
    }

    public Response<KeyVaultCertificateWithPolicy> restoreCertificateBackupWithResponse(byte[] backup,
                                                                                        Context context) {
        CertificateRestoreParameters parameters = new CertificateRestoreParameters().certificateBundleBackup(backup);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.restoreCertificate(vaultUrl, apiVersion, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE,
            context);
    }

    public PagedFlux<CertificateProperties> listPropertiesOfCertificatesAsync() {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listCertificatesFirstPageAsync(false, context)),
                continuationToken -> withContext(context -> listCertificatesNextPageAsync(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(LOGGER, ex));
        }
    }

    public PagedFlux<CertificateProperties> listPropertiesOfCertificatesAsync(boolean includePending, Context context) {
        return new PagedFlux<>(
            () -> listCertificatesFirstPageAsync(includePending, context),
            continuationToken -> listCertificatesNextPageAsync(continuationToken, context));
    }

    public PagedFlux<CertificateProperties> listPropertiesOfCertificatesAsync(boolean includePending) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listCertificatesFirstPageAsync(includePending, context)),
                continuationToken -> withContext(context -> listCertificatesNextPageAsync(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(LOGGER, ex));
        }
    }

    /**
     * Gets attributes of all the certificates given by the {@code continuationToken} that was retrieved from a call to
     * {@link CertificateClientImpl#listPropertiesOfCertificates(boolean, Context)}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken()} ()} from a previous, successful call to one of the
     * listCertificates operations.
     *
     * @return A {@link Mono} of {@link PagedResponse} containing {@link CertificateProperties} instances from the next
     * page of results.
     */
    private Mono<PagedResponse<CertificateProperties>> listCertificatesNextPageAsync(String continuationToken,
                                                                                     Context context) {
        try {
            return service.getCertificatesAsync(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                    context)
                .doOnRequest(ignored -> LOGGER.verbose("Listing next certificates page - Page {} ", continuationToken))
                .doOnSuccess(response -> LOGGER.verbose("Listed next certificates page - Page {} ", continuationToken))
                .doOnError(error ->
                    LOGGER.warning("Failed to list next certificates page - Page {} ", continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code DEFAULT_MAX_PAGE_RESULTS}
     * values.
     */
    private Mono<PagedResponse<CertificateProperties>> listCertificatesFirstPageAsync(boolean includePending,
                                                                                      Context context) {
        try {
            return service
                .getCertificatesAsync(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, includePending, apiVersion, ACCEPT_LANGUAGE,
                    CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> LOGGER.verbose("Listing certificates"))
                .doOnSuccess(response -> LOGGER.verbose("Listed certificates"))
                .doOnError(error -> LOGGER.warning("Failed to list certificates", error));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    public PagedIterable<CertificateProperties> listPropertiesOfCertificates() {
        return new PagedIterable<>(
            () -> listCertificatesFirstPage(false, Context.NONE),
            continuationToken -> listCertificatesNextPage(continuationToken, Context.NONE));
    }

    public PagedIterable<CertificateProperties> listPropertiesOfCertificates(boolean includePending) {
        return new PagedIterable<>(
            () -> listCertificatesFirstPage(includePending, Context.NONE),
            continuationToken -> listCertificatesNextPage(continuationToken, Context.NONE));
    }

    public PagedIterable<CertificateProperties> listPropertiesOfCertificates(boolean includePending, Context context) {
        return new PagedIterable<>(
            () -> listCertificatesFirstPage(includePending, context),
            continuationToken -> listCertificatesNextPage(continuationToken, context));
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code DEFAULT_MAX_PAGE_RESULTS}
     * values.
     */
    private PagedResponse<CertificateProperties> listCertificatesFirstPage(boolean includePending, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getCertificates(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, includePending, apiVersion, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    /**
     * Gets attributes of all the certificates given by the {@code continuationToken} that was retrieved from a call to
     * {@link CertificateClientImpl#listPropertiesOfCertificates(boolean, Context)}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken()} from a previous, successful call to one
     * of the listCertificates operations.
     *
     * @return A {@link PagedResponse} containing {@link CertificateProperties} instances from the next page of results.
     */
    private PagedResponse<CertificateProperties> listCertificatesNextPage(String continuationToken, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getCertificates(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context);
    }

    public PagedFlux<DeletedCertificate> listDeletedCertificatesAsync() {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listDeletedCertificatesFirstPageAsync(false, context)),
                continuationToken ->
                    withContext(context -> listDeletedCertificatesNextPageAsync(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(LOGGER, ex));
        }
    }

    public PagedFlux<DeletedCertificate> listDeletedCertificatesAsync(boolean includePending) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listDeletedCertificatesFirstPageAsync(includePending, context)),
                continuationToken ->
                    withContext(context -> listDeletedCertificatesNextPageAsync(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(LOGGER, ex));
        }
    }

    public PagedFlux<DeletedCertificate> listDeletedCertificatesAsync(Boolean includePending, Context context) {
        return new PagedFlux<>(
            () -> listDeletedCertificatesFirstPageAsync(includePending, context),
            continuationToken -> listDeletedCertificatesNextPageAsync(continuationToken, context));
    }

    /**
     * Gets attributes of all the certificates given by the {@code continuationToken}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken()} from a previous, successful call to one
     * of the list operations.
     *
     * @return A {@link Mono} of {@link PagedResponse} containing {@link DeletedCertificate} instances from the next
     * page of results.
     */
    private Mono<PagedResponse<DeletedCertificate>> listDeletedCertificatesNextPageAsync(String continuationToken,
                                                                                         Context context) {
        try {
            return service.getDeletedCertificatesAsync(vaultUrl, continuationToken, ACCEPT_LANGUAGE,
                    CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored ->
                    LOGGER.verbose("Listing next deleted certificates page - Page {} ", continuationToken))
                .doOnSuccess(response ->
                    LOGGER.verbose("Listed next deleted certificates page - Page {} ", continuationToken))
                .doOnError(error ->
                    LOGGER.warning("Failed to list next deleted certificates page - Page {} ", continuationToken,
                        error));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code DEFAULT_MAX_PAGE_RESULTS}
     * values.
     */
    private Mono<PagedResponse<DeletedCertificate>> listDeletedCertificatesFirstPageAsync(boolean includePending,
                                                                                          Context context) {
        try {
            return service.getDeletedCertificatesAsync(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, includePending, apiVersion,
                    ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> LOGGER.verbose("Listing deleted certificates"))
                .doOnSuccess(response -> LOGGER.verbose("Listed deleted certificates"))
                .doOnError(error -> LOGGER.warning("Failed to list deleted certificates", error));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    public PagedIterable<DeletedCertificate> listDeletedCertificates() {
        return new PagedIterable<>(
            () -> listDeletedCertificatesFirstPage(false, Context.NONE),
            continuationToken -> listDeletedCertificatesNextPage(continuationToken, Context.NONE));
    }

    public PagedIterable<DeletedCertificate> listDeletedCertificates(boolean includePending) {
        return new PagedIterable<>(
            () -> listDeletedCertificatesFirstPage(includePending, Context.NONE),
            continuationToken -> listDeletedCertificatesNextPage(continuationToken, Context.NONE));
    }

    public PagedIterable<DeletedCertificate> listDeletedCertificates(Boolean includePending, Context context) {
        return new PagedIterable<>(
            () -> listDeletedCertificatesFirstPage(includePending, context),
            continuationToken -> listDeletedCertificatesNextPage(continuationToken, context));
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code DEFAULT_MAX_PAGE_RESULTS}
     * values.
     */
    private PagedResponse<DeletedCertificate> listDeletedCertificatesFirstPage(boolean includePending,
                                                                               Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getDeletedCertificates(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, includePending, apiVersion,
            ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context);
    }

    /**
     * Gets attributes of all the certificates given by the {@code continuationToken}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken()} from a previous, successful call to one
     * of the list operations.
     *
     * @return A {@link Mono} of {@link PagedResponse} containing {@link DeletedCertificate} instances from the next
     * page of results.
     */
    private PagedResponse<DeletedCertificate> listDeletedCertificatesNextPage(String continuationToken,
                                                                              Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getDeletedCertificates(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context);
    }

    public PagedFlux<CertificateProperties> listPropertiesOfCertificateVersionsAsync(String certificateName) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listCertificateVersionsFirstPageAsync(certificateName, context)),
                continuationToken ->
                    withContext(context -> listCertificateVersionsNextPageAsync(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(LOGGER, ex));
        }
    }

    public PagedFlux<CertificateProperties> listPropertiesOfCertificateVersionsAsync(String certificateName,
                                                                                     Context context) {
        return new PagedFlux<>(
            () -> listCertificateVersionsFirstPageAsync(certificateName, context),
            continuationToken -> listCertificateVersionsNextPageAsync(continuationToken, context));
    }

    private Mono<PagedResponse<CertificateProperties>> listCertificateVersionsFirstPageAsync(String certificateName,
                                                                                             Context context) {
        try {
            return service.getCertificateVersionsAsync(vaultUrl, certificateName, DEFAULT_MAX_PAGE_RESULTS, apiVersion,
                    ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> LOGGER.verbose("Listing certificate versions - {}", certificateName))
                .doOnSuccess(response -> LOGGER.verbose("Listed certificate versions - {}", certificateName))
                .doOnError(error -> LOGGER.warning("Failed to list certificate versions - {}", certificateName, error));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /*
     * Gets attributes of all the certificates given by the {@code continuationToken}.
     */
    private Mono<PagedResponse<CertificateProperties>> listCertificateVersionsNextPageAsync(String continuationToken,
                                                                                            Context context) {
        try {
            return service.getCertificatesAsync(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                    context)
                .doOnRequest(ignored ->
                    LOGGER.verbose("Listing next certificate versions page - Page {} ", continuationToken))
                .doOnSuccess(response ->
                    LOGGER.verbose("Listed next certificate versions page - Page {} ", continuationToken))
                .doOnError(error ->
                    LOGGER.warning("Failed to list next certificate versions page - Page {} ", continuationToken,
                        error));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    public PagedIterable<CertificateProperties> listPropertiesOfCertificateVersions(String certificateName) {
        return new PagedIterable<>(
            () -> listCertificateVersionsFirstPage(certificateName, Context.NONE),
            continuationToken -> listCertificateVersionsNextPage(continuationToken, Context.NONE));
    }

    public PagedIterable<CertificateProperties> listPropertiesOfCertificateVersions(String certificateName,
                                                                                     Context context) {
        return new PagedIterable<>(
            () -> listCertificateVersionsFirstPage(certificateName, context),
            continuationToken -> listCertificateVersionsNextPage(continuationToken, context));
    }

    private PagedResponse<CertificateProperties> listCertificateVersionsFirstPage(String certificateName,
                                                                                  Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getCertificateVersions(vaultUrl, certificateName, DEFAULT_MAX_PAGE_RESULTS, apiVersion,
                ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context);
    }

    /*
     * Gets attributes of all the certificates given by the {@code continuationToken}.
     */
    private PagedResponse<CertificateProperties> listCertificateVersionsNextPage(String continuationToken,
                                                                                 Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getCertificates(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context);
    }

    public Mono<Response<KeyVaultCertificateWithPolicy>> mergeCertificateWithResponseAsync(MergeCertificateOptions mergeCertificateOptions, Context context) {
        Objects.requireNonNull(mergeCertificateOptions, "'mergeCertificateOptions' cannot be null.");

        CertificateMergeParameters mergeParameters =
            new CertificateMergeParameters()
                .x509Certificates(mergeCertificateOptions.getX509Certificates())
                .tags(mergeCertificateOptions.getTags())
                .certificateAttributes(new CertificateRequestAttributes().enabled(mergeCertificateOptions.isEnabled()));

        return service.mergeCertificateAsync(vaultUrl, mergeCertificateOptions.getName(), apiVersion, ACCEPT_LANGUAGE,
                mergeParameters, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Merging certificate - {}", mergeCertificateOptions.getName()))
            .doOnSuccess(response ->
                LOGGER.verbose("Merged certificate  - {}", response.getValue().getProperties().getName()))
            .doOnError(error ->
                LOGGER.warning("Failed to merge certificate - {}", mergeCertificateOptions.getName(), error));
    }

    public Response<KeyVaultCertificateWithPolicy> mergeCertificateWithResponse(MergeCertificateOptions mergeCertificateOptions,
                                                                                Context context) {
        Objects.requireNonNull(mergeCertificateOptions, "'mergeCertificateOptions' cannot be null.");

        CertificateMergeParameters mergeParameters =
            new CertificateMergeParameters()
                .x509Certificates(mergeCertificateOptions.getX509Certificates())
                .tags(mergeCertificateOptions.getTags())
                .certificateAttributes(new CertificateRequestAttributes().enabled(mergeCertificateOptions.isEnabled()));
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.mergeCertificate(vaultUrl, mergeCertificateOptions.getName(), apiVersion, ACCEPT_LANGUAGE,
            mergeParameters, CONTENT_TYPE_HEADER_VALUE, context);
    }

    public Mono<Response<CertificatePolicy>> getCertificatePolicyWithResponseAsync(String certificateName,
                                                                                   Context context) {
        return service.getCertificatePolicyAsync(vaultUrl, apiVersion, ACCEPT_LANGUAGE, certificateName,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Retrieving certificate policy - {}", certificateName))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved certificate policy - {}", certificateName))
            .doOnError(error -> LOGGER.warning("Failed to retrieve certificate policy - {}", certificateName, error));
    }

    public Response<CertificatePolicy> getCertificatePolicyWithResponse(String certificateName, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getCertificatePolicy(vaultUrl, apiVersion, ACCEPT_LANGUAGE, certificateName,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    public Mono<Response<CertificatePolicy>> updateCertificatePolicyWithResponseAsync(String certificateName,
                                                                                      CertificatePolicy policy,
                                                                                      Context context) {
        CertificatePolicyRequest policyRequest = new CertificatePolicyRequest(policy);

        return service.updateCertificatePolicyAsync(vaultUrl, apiVersion, ACCEPT_LANGUAGE, certificateName,
                policyRequest, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Updating certificate policy - {}", certificateName))
            .doOnSuccess(response ->
                LOGGER.verbose("Updated the certificate policy - {}", response.getValue().getUpdatedOn()))
            .doOnError(error -> LOGGER.warning("Failed to update the certificate policy - {}", certificateName, error));
    }

    public Response<CertificatePolicy> updateCertificatePolicyWithResponse(String certificateName, CertificatePolicy policy, Context context) {
        CertificatePolicyRequest policyRequest = new CertificatePolicyRequest(policy);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.updateCertificatePolicy(vaultUrl, apiVersion, ACCEPT_LANGUAGE, certificateName, policyRequest,
            CONTENT_TYPE_HEADER_VALUE, context);
    }


    public Mono<Response<CertificateIssuer>> createIssuerWithResponseAsync(CertificateIssuer issuer, Context context) {
        CertificateIssuerSetParameters parameters = new CertificateIssuerSetParameters()
            .provider(issuer.getProvider())
            .organizationDetails(new OrganizationDetails()
                .id(issuer.getOrganizationId())
                .adminDetails(issuer.getAdministratorContacts()))
            .credentials(new IssuerCredentials()
                .password(issuer.getPassword())
                .accountId(issuer.getAccountId()))
            .attributes(new IssuerAttributes()
                .enabled(issuer.isEnabled()));

        return service.setCertificateIssuerAsync(vaultUrl, apiVersion, ACCEPT_LANGUAGE, issuer.getName(), parameters,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Creating certificate issuer - {}", issuer.getName()))
            .doOnSuccess(response ->
                LOGGER.verbose("Created the certificate issuer - {}", response.getValue().getName()))
            .doOnError(error ->
                LOGGER.warning("Failed to create the certificate issuer - {}", issuer.getName(), error));
    }

    public Response<CertificateIssuer> createIssuerWithResponse(CertificateIssuer issuer, Context context) {
        CertificateIssuerSetParameters parameters = new CertificateIssuerSetParameters()
            .provider(issuer.getProvider())
            .organizationDetails(new OrganizationDetails()
                .id(issuer.getOrganizationId())
                .adminDetails(issuer.getAdministratorContacts()))
            .credentials(new IssuerCredentials()
                .password(issuer.getPassword())
                .accountId(issuer.getAccountId()))
            .attributes(new IssuerAttributes()
                .enabled(issuer.isEnabled()));
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.setCertificateIssuer(vaultUrl, apiVersion, ACCEPT_LANGUAGE, issuer.getName(), parameters,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    public Mono<Response<CertificateIssuer>> getIssuerWithResponseAsync(String issuerName, Context context) {
        return service.getCertificateIssuerAsync(vaultUrl, apiVersion, ACCEPT_LANGUAGE, issuerName,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Retrieving certificate issuer - {}", issuerName))
            .doOnSuccess(response ->
                LOGGER.verbose("Retrieved the certificate issuer - {}", response.getValue().getName()))
            .doOnError(error ->
                LOGGER.warning("Failed to retreive the certificate issuer - {}", issuerName, error));
    }

    public Response<CertificateIssuer> getIssuerWithResponse(String issuerName, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getCertificateIssuer(vaultUrl, apiVersion, ACCEPT_LANGUAGE, issuerName,
            CONTENT_TYPE_HEADER_VALUE, context);
    }


    public Mono<Response<CertificateIssuer>> deleteIssuerWithResponseAsync(String issuerName, Context context) {
        return service.deleteCertificateIssuerAsync(vaultUrl, issuerName, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Deleting certificate issuer - {}", issuerName))
            .doOnSuccess(response ->
                LOGGER.verbose("Deleted the certificate issuer - {}", response.getValue().getName()))
            .doOnError(error -> LOGGER.warning("Failed to delete the certificate issuer - {}", issuerName, error));
    }

    public Response<CertificateIssuer> deleteIssuerWithResponse(String issuerName, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.deleteCertificateIssuer(vaultUrl, issuerName, apiVersion, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    public PagedFlux<IssuerProperties> listPropertiesOfIssuersAsync() {
        try {
            return new PagedFlux<>(
                () -> withContext(this::listPropertiesOfIssuersFirstPageAsync),
                continuationToken ->
                    withContext(context -> listPropertiesOfIssuersNextPageAsync(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(LOGGER, ex));
        }
    }

    public PagedFlux<IssuerProperties> listPropertiesOfIssuersAsync(Context context) {
        return new PagedFlux<>(
            () -> listPropertiesOfIssuersFirstPageAsync(context),
            continuationToken -> listPropertiesOfIssuersNextPageAsync(continuationToken, context));
    }

    private Mono<PagedResponse<IssuerProperties>> listPropertiesOfIssuersFirstPageAsync(Context context) {
        try {
            return service.getCertificateIssuersAsync(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, apiVersion, ACCEPT_LANGUAGE,
                    CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> LOGGER.verbose("Listing certificate issuers - {}"))
                .doOnSuccess(response -> LOGGER.verbose("Listed certificate issuers - {}"))
                .doOnError(error -> LOGGER.warning("Failed to list certificate issuers - {}", error));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Gets attributes of all the certificates given by the {@code continuationToken} that was retrieved from a call to
     * {@link CertificateClientImpl#listPropertiesOfIssuersAsync(Context)}}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken()} ()} from a previous, successful call to
     * one of the list issuers operations.
     *
     * @return A {@link Mono} of {@link PagedResponse} from the next page of results containing {@link IssuerProperties}
     * instances from the next page of results.
     */
    private Mono<PagedResponse<IssuerProperties>> listPropertiesOfIssuersNextPageAsync(String continuationToken,
                                                                                       Context context) {
        try {
            return service.getCertificateIssuersAsync(vaultUrl, continuationToken, ACCEPT_LANGUAGE,
                    CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored ->
                    LOGGER.verbose("Listing next certificate issuers page - Page {} ", continuationToken))
                .doOnSuccess(response ->
                    LOGGER.verbose("Listed next certificate issuers page - Page {} ", continuationToken))
                .doOnError(error ->
                    LOGGER.warning("Failed to list next certificate issuers page - Page {} ", continuationToken,
                        error));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    public PagedIterable<IssuerProperties> listPropertiesOfIssuers() {
        return new PagedIterable<>(
            () -> listPropertiesOfIssuersFirstPage(Context.NONE),
            continuationToken -> listPropertiesOfIssuersNextPage(continuationToken, Context.NONE));
    }

    public PagedIterable<IssuerProperties> listPropertiesOfIssuers(Context context) {
        return new PagedIterable<>(
            () -> listPropertiesOfIssuersFirstPage(context),
            continuationToken -> listPropertiesOfIssuersNextPage(continuationToken, context));
    }

    private PagedResponse<IssuerProperties> listPropertiesOfIssuersFirstPage(Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getCertificateIssuers(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, apiVersion, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    /**
     * Gets attributes of all the certificates given by the {@code continuationToken} that was retrieved from a call to
     * {@link CertificateClientImpl#listPropertiesOfIssuers(Context)}}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken()} ()} from a previous, successful call to
     * one of the list issuers operations.
     *
     * @return A {@link PagedResponse} from the next page of results containing {@link IssuerProperties} instances from
     * the next page of results.
     */
    private PagedResponse<IssuerProperties> listPropertiesOfIssuersNextPage(String continuationToken,
                                                                            Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getCertificateIssuers(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context);
    }

    public Mono<Response<CertificateIssuer>> updateIssuerWithResponseAsync(CertificateIssuer issuer, Context context) {
        CertificateIssuerUpdateParameters updateParameters =
            new CertificateIssuerUpdateParameters()
                .provider(issuer.getProvider())
                .organizationDetails(new OrganizationDetails()
                    .id(issuer.getOrganizationId())
                    .adminDetails(issuer.getAdministratorContacts()))
                .credentials(new IssuerCredentials()
                    .password(issuer.getPassword())
                    .accountId(issuer.getAccountId()))
                .attributes(new IssuerAttributes()
                    .enabled(issuer.isEnabled()));

        return service.updateCertificateIssuerAsync(vaultUrl, issuer.getName(), apiVersion, ACCEPT_LANGUAGE,
                updateParameters, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Updating certificate issuer - {}", issuer.getName()))
            .doOnSuccess(response ->
                LOGGER.verbose("Updated up the certificate issuer - {}", response.getValue().getName()))
            .doOnError(error ->
                LOGGER.warning("Failed to updated the certificate issuer - {}", issuer.getName(), error));
    }

    public Response<CertificateIssuer> updateIssuerWithResponse(CertificateIssuer issuer, Context context) {
        CertificateIssuerUpdateParameters updateParameters =
            new CertificateIssuerUpdateParameters()
                .provider(issuer.getProvider())
                .organizationDetails(new OrganizationDetails()
                    .id(issuer.getOrganizationId())
                    .adminDetails(issuer.getAdministratorContacts()))
                .credentials(new IssuerCredentials()
                    .password(issuer.getPassword())
                    .accountId(issuer.getAccountId()))
                .attributes(new IssuerAttributes()
                    .enabled(issuer.isEnabled()));
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.updateCertificateIssuer(vaultUrl, issuer.getName(), apiVersion, ACCEPT_LANGUAGE,
            updateParameters, CONTENT_TYPE_HEADER_VALUE, context);
    }

    public PagedFlux<CertificateContact> setContactsAsync(List<CertificateContact> contacts) {
        try {
            return new PagedFlux<>(() -> withContext(context -> setCertificateContactsWithResponseAsync(contacts, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(LOGGER, ex));
        }
    }

    public PagedFlux<CertificateContact> setContactsAsync(List<CertificateContact> contacts, Context context) {
        return new PagedFlux<>(() -> setCertificateContactsWithResponseAsync(contacts, context));
    }

    private Mono<PagedResponse<CertificateContact>> setCertificateContactsWithResponseAsync(List<CertificateContact> contacts, Context context) {
        Contacts contactsParams = new Contacts().contactList(contacts);

        return service.setCertificateContactsAsync(vaultUrl, apiVersion, ACCEPT_LANGUAGE, contactsParams,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Listing certificate contacts - {}"))
            .doOnSuccess(response -> LOGGER.verbose("Listed certificate contacts - {}"))
            .doOnError(error -> LOGGER.warning("Failed to list certificate contacts - {}", error));
    }

    public PagedIterable<CertificateContact> setContacts(List<CertificateContact> contacts) {
        return new PagedIterable<>(() -> setCertificateContactsWithResponse(contacts, Context.NONE));
    }

    public PagedIterable<CertificateContact> setContacts(List<CertificateContact> contacts, Context context) {
        return new PagedIterable<>(() -> setCertificateContactsWithResponse(contacts, context));
    }

    private PagedResponse<CertificateContact> setCertificateContactsWithResponse(List<CertificateContact> contacts,
                                                                                 Context context) {
        Contacts contactsParams = new Contacts().contactList(contacts);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.setCertificateContacts(vaultUrl, apiVersion, ACCEPT_LANGUAGE, contactsParams,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    public PagedFlux<CertificateContact> listContactsAsync() {
        try {
            return new PagedFlux<>(() -> withContext(this::listCertificateContactsFirstPageAsync));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(LOGGER, ex));
        }
    }

    public PagedFlux<CertificateContact> listContactsAsync(Context context) {
        return new PagedFlux<>(() -> listCertificateContactsFirstPageAsync(context));
    }

    private Mono<PagedResponse<CertificateContact>> listCertificateContactsFirstPageAsync(Context context) {
        try {
            return service.getCertificateContactsAsync(vaultUrl, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                    context)
                .doOnRequest(ignored -> LOGGER.verbose("Listing certificate contacts - {}"))
                .doOnSuccess(response -> LOGGER.verbose("Listed certificate contacts - {}"))
                .doOnError(error -> LOGGER.warning("Failed to list certificate contacts - {}", error));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    public PagedIterable<CertificateContact> listContacts() {
        return new PagedIterable<>(() -> listCertificateContactsFirstPage(Context.NONE));
    }

    public PagedIterable<CertificateContact> listContacts(Context context) {
        return new PagedIterable<>(() -> listCertificateContactsFirstPage(context));
    }

    private PagedResponse<CertificateContact> listCertificateContactsFirstPage(Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getCertificateContacts(vaultUrl, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context);
    }

    public PagedFlux<CertificateContact> deleteContactsAsync() {
        try {
            return new PagedFlux<>(() -> withContext(this::deleteCertificateContactsWithResponseAsync));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(LOGGER, ex));
        }
    }

    public PagedFlux<CertificateContact> deleteContactsAsync(Context context) {
        return new PagedFlux<>(() -> deleteCertificateContactsWithResponseAsync(context));
    }

    private Mono<PagedResponse<CertificateContact>> deleteCertificateContactsWithResponseAsync(Context context) {
        return service.deleteCertificateContactsAsync(vaultUrl, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context)
            .doOnRequest(ignored -> LOGGER.verbose("Deleting certificate contacts - {}"))
            .doOnSuccess(response -> LOGGER.verbose("Deleted certificate contacts - {}"))
            .doOnError(error -> LOGGER.warning("Failed to delete certificate contacts - {}", error));
    }

    public PagedIterable<CertificateContact> deleteContacts() {
        return new PagedIterable<>(() -> deleteCertificateContactsWithResponse(Context.NONE));
    }

    public PagedIterable<CertificateContact> deleteContacts(Context context) {
        return new PagedIterable<>(() -> deleteCertificateContactsWithResponse(context));
    }

    private PagedResponse<CertificateContact> deleteCertificateContactsWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.deleteCertificateContacts(vaultUrl, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
            context);
    }

    public Mono<Response<CertificateOperation>> deleteCertificateOperationWithResponseAsync(String certificateName,
                                                                                            Context context) {
        return service.deleteCertificateOperationAsync(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Deleting certificate operation - {}", certificateName))
            .doOnSuccess(response -> LOGGER.verbose("Deleted the certificate operation - {}", response.getStatusCode()))
            .doOnError(error ->
                LOGGER.warning("Failed to delete the certificate operation - {}", certificateName, error));
    }

    public Response<CertificateOperation> deleteCertificateOperationWithResponse(String certificateName,
                                                                                 Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.deleteCertificateOperation(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context);
    }


    public Mono<Response<KeyVaultCertificateWithPolicy>> importCertificateWithResponseAsync(ImportCertificateOptions importCertificateOptions,
                                                                                            Context context) {
        CertificateImportParameters parameters = new CertificateImportParameters()
            .base64EncodedCertificate(transformCertificateForImport(importCertificateOptions))
            .certificateAttributes(new CertificateRequestAttributes(importCertificateOptions))
            .password(importCertificateOptions.getPassword())
            .tags(importCertificateOptions.getTags());

        if (importCertificateOptions.getPolicy() != null) {
            parameters.certificatePolicy(new CertificatePolicyRequest(importCertificateOptions.getPolicy()));
        }

        return service.importCertificateAsync(vaultUrl, importCertificateOptions.getName(), apiVersion, ACCEPT_LANGUAGE,
            parameters, CONTENT_TYPE_HEADER_VALUE, context);
    }

    public Response<KeyVaultCertificateWithPolicy> importCertificateWithResponse(ImportCertificateOptions importCertificateOptions,
                                                                                 Context context) {
        CertificateImportParameters parameters = new CertificateImportParameters()
            .base64EncodedCertificate(transformCertificateForImport(importCertificateOptions))
            .certificateAttributes(new CertificateRequestAttributes(importCertificateOptions))
            .password(importCertificateOptions.getPassword())
            .tags(importCertificateOptions.getTags());
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        if (importCertificateOptions.getPolicy() != null) {
            parameters.certificatePolicy(new CertificatePolicyRequest(importCertificateOptions.getPolicy()));
        }

        return service.importCertificate(vaultUrl, importCertificateOptions.getName(), apiVersion, ACCEPT_LANGUAGE,
            parameters, CONTENT_TYPE_HEADER_VALUE, context);
    }

    private String transformCertificateForImport(ImportCertificateOptions options) {
        CertificatePolicy policy = options.getPolicy();

        if (policy != null) {
            CertificateContentType contentType = policy.getContentType();

            if (contentType != null && contentType.equals(CertificateContentType.PEM)) {
                return new String(options.getCertificate(), StandardCharsets.US_ASCII);
            }
        }

        return Base64.getEncoder().encodeToString(options.getCertificate());
    }

    private Context enableSyncRestProxy(Context context) {
        return context.addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
    }
}
