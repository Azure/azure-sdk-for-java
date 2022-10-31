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
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

public class CertificateClientImpl {
    private final String apiVersion;
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    // Please see <a href=https://docs.microsoft.com/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    private static final String KEYVAULT_TRACING_NAMESPACE_VALUE = "Microsoft.KeyVault";
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";

    private static final Duration DEFAULT_POLLING_INTERVAL = Duration.ofSeconds(1);

    private final String vaultUrl;
    private final CertificateService service;
    private final ClientLogger logger = new ClientLogger(CertificateAsyncClient.class);

    private final HttpPipeline pipeline;

    /**
     * Creates a CertificateClientImpl instance that uses {@code pipeline} to service requests
     *
     * @param vaultUrl URL for the Azure KeyVault service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     * @param version {@link CertificateServiceVersion} of the service to be used when making requests.
     */
    public CertificateClientImpl(String vaultUrl, HttpPipeline pipeline, CertificateServiceVersion version) {
        Objects.requireNonNull(vaultUrl,
            KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));

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
        @ExpectedResponses({200})
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
        @ExpectedResponses({200})
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

    public PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> beginCreateCertificate(String certificateName,
                                                                                                  CertificatePolicy policy,
                                                                                                  Boolean isEnabled,
                                                                                                  Map<String, String> tags) {
        return new PollerFlux<>(getDefaultPollingInterval(),
            activationOperation(certificateName, policy, isEnabled, tags),
            createPollOperation(certificateName),
            cancelOperation(certificateName),
            fetchResultOperation(certificateName));
    }

    private BiFunction<PollingContext<CertificateOperation>, PollResponse<CertificateOperation>, Mono<CertificateOperation>> cancelOperation(String certificateName) {
        return (pollingContext, firstResponse) ->
            withContext(context -> cancelCertificateOperationWithResponseAsync(certificateName, context))
                .flatMap(FluxUtil::toMono);
    }

    private Function<PollingContext<CertificateOperation>, Mono<CertificateOperation>> activationOperation(String certificateName,
                                                                                                           CertificatePolicy policy,
                                                                                                           boolean enabled,
                                                                                                           Map<String, String> tags) {
        return (pollingContext) ->
            withContext(context -> createCertificateWithResponseAsync(certificateName, policy, enabled, tags, context))
                .flatMap(certificateOperationResponse -> Mono.just(certificateOperationResponse.getValue()));
    }

    private Function<PollingContext<CertificateOperation>,
        Mono<KeyVaultCertificateWithPolicy>> fetchResultOperation(String certificateName) {
        return (pollingContext) ->
            withContext(context -> getCertificateWithResponseAsync(certificateName, "", context))
                .flatMap(certificateResponse -> Mono.just(certificateResponse.getValue()));
    }

    /*
   Polling operation to poll on create certificate operation status.
 */
    private Function<PollingContext<CertificateOperation>, Mono<PollResponse<CertificateOperation>>> createPollOperation(String certificateName) {
        return (pollingContext) -> {
            try {
                return withContext(context ->
                    service.getCertificateOperationAsync(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
                        CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                            KEYVAULT_TRACING_NAMESPACE_VALUE)))
                    .flatMap(this::processCertificateOperationResponse);
            } catch (HttpResponseException e) {
                logger.logExceptionAsError(e);

                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            }
        };
    }

    private Mono<PollResponse<CertificateOperation>> processCertificateOperationResponse(Response<CertificateOperation> certificateOperationResponse) {
        LongRunningOperationStatus status = null;
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

        return Mono.just(new PollResponse<>(status, certificateOperationResponse.getValue()));
    }

    public Mono<Response<CertificateOperation>> cancelCertificateOperationWithResponseAsync(String certificateName,
                                                                                            Context context) {
        CertificateOperationUpdateParameter parameter =
            new CertificateOperationUpdateParameter().cancellationRequested(true);
        context = context == null ? Context.NONE : context;

        return service.updateCertificateOperationAsync(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
                parameter, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Cancelling certificate operation - {}", certificateName))
            .doOnSuccess(response ->
                logger.verbose("Cancelled the certificate operation - {}", response.getValue().getStatus()))
            .doOnError(error ->
                logger.warning("Failed to cancel the certificate operation - {}", certificateName, error));
    }

    public Response<CertificateOperation> cancelCertificateOperationWithResponse(String certificateName,
                                                                                 Context context) {
        CertificateOperationUpdateParameter parameter =
            new CertificateOperationUpdateParameter().cancellationRequested(true);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.updateCertificateOperation(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE, parameter,
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
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
                certificateRequestParameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Starting creation of certificate - {}", certificateName))
            .doOnSuccess(response -> logger.verbose("Started creation of certificate - {}", certificateName))
            .doOnError(error -> logger.warning("Failed to create the certificate - {}", certificateName, error));
    }

    public PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> getCertificateOperation(String certificateName) {
        return new PollerFlux<>(getDefaultPollingInterval(),
            (pollingContext) -> Mono.empty(),
            createPollOperation(certificateName),
            cancelOperation(certificateName),
            fetchResultOperation(certificateName));
    }

    public Mono<Response<KeyVaultCertificateWithPolicy>> getCertificateWithResponseAsync(String certificateName,
                                                                                         String version,
                                                                                         Context context) {
        context = context == null ? Context.NONE : context;

        return service.getCertificateWithPolicyAsync(vaultUrl, certificateName, version, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Retrieving certificate - {}", certificateName))
            .doOnSuccess(response ->
                logger.verbose("Retrieved the certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to Retrieve the certificate - {}", certificateName, error));
    }

    public Response<KeyVaultCertificateWithPolicy> getCertificateWithResponse(String certificateName, String version,
                                                                              Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getCertificateWithPolicy(vaultUrl, certificateName, version, apiVersion, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    public Mono<Response<KeyVaultCertificate>> getCertificateVersionWithResponseAsync(String certificateName,
                                                                                      String version, Context context) {
        context = context == null ? Context.NONE : context;

        return service.getCertificateAsync(vaultUrl, certificateName, version, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.verbose("Retrieving certificate - {}", certificateName))
            .doOnSuccess(response ->
                logger.verbose("Retrieved the certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to Retrieve the certificate - {}", certificateName, error));
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
        context = context == null ? Context.NONE : context;

        return service.updateCertificateAsync(vaultUrl, properties.getName(), properties.getVersion(), apiVersion,
                ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Updating certificate - {}", properties.getName()))
            .doOnSuccess(response -> logger.verbose("Updated the certificate - {}", properties.getName()))
            .doOnError(error -> logger.warning("Failed to update the certificate - {}", properties.getName(), error));
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
            ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                KEYVAULT_TRACING_NAMESPACE_VALUE));
    }


    public PollerFlux<DeletedCertificate, Void> beginDeleteCertificate(String certificateName) {
        return new PollerFlux<>(getDefaultPollingInterval(),
            activationOperation(certificateName),
            createDeletePollOperation(certificateName),
            (context, firstResponse) -> Mono.empty(),
            (context) -> Mono.empty());
    }

    private Function<PollingContext<DeletedCertificate>, Mono<DeletedCertificate>> activationOperation(String certificateName) {
        return (pollingContext) ->
            withContext(context -> deleteCertificateWithResponseAsync(certificateName, context))
                .flatMap(deletedCertificateResponse -> Mono.just(deletedCertificateResponse.getValue()));
    }

    /*
    Polling operation to poll on create delete certificate operation status.
    */
    private Function<PollingContext<DeletedCertificate>, Mono<PollResponse<DeletedCertificate>>> createDeletePollOperation(String keyName) {
        return pollingContext ->
            withContext(context -> service.getDeletedCertificatePollerAsync(vaultUrl, keyName, apiVersion,
                ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE)))
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

    private Mono<Response<DeletedCertificate>> deleteCertificateWithResponseAsync(String certificateName,
                                                                                  Context context) {
        return service.deleteCertificateAsync(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Deleting certificate - {}", certificateName))
            .doOnSuccess(response -> logger.verbose("Deleted the certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to delete the certificate - {}", certificateName, error));
    }

    public Mono<Response<DeletedCertificate>> getDeletedCertificateWithResponseAsync(String certificateName, Context context) {
        context = context == null ? Context.NONE : context;

        return service.getDeletedCertificateAsync(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Retrieving deleted certificate - {}", certificateName))
            .doOnSuccess(response -> logger.verbose("Retrieved the deleted certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to Retrieve the deleted certificate - {}", certificateName, error));
    }

    public Response<DeletedCertificate> getDeletedCertificateWithResponse(String certificateName, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);
        return service.getDeletedCertificate(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }


    public Mono<Response<Void>> purgeDeletedCertificateWithResponseAsync(String certificateName, Context context) {
        context = context == null ? Context.NONE : context;

        return service.purgeDeletedCertificateAsync(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Purging certificate - {}", certificateName))
            .doOnSuccess(response -> logger.verbose("Purged the certificate - {}", response.getStatusCode()))
            .doOnError(error -> logger.warning("Failed to purge the certificate - {}", certificateName, error));
    }

    public Response<Void> purgeDeletedCertificateWithResponse(String certificateName, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);
        return service.purgeDeletedCertificate(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }


    public PollerFlux<KeyVaultCertificateWithPolicy, Void> beginRecoverDeletedCertificate(String certificateName) {
        return new PollerFlux<>(getDefaultPollingInterval(),
            recoverActivationOperation(certificateName),
            createRecoverPollOperation(certificateName),
            (context, firstResponse) -> Mono.empty(),
            context -> Mono.empty());
    }

    private Function<PollingContext<KeyVaultCertificateWithPolicy>, Mono<KeyVaultCertificateWithPolicy>> recoverActivationOperation(String certificateName) {
        return (pollingContext) ->
            withContext(context -> recoverDeletedCertificateWithResponseAsync(certificateName, context))
                .flatMap(certificateResponse -> Mono.just(certificateResponse.getValue()));
    }

    /*
    Polling operation to poll on create recover certificate operation status.
    */
    private Function<PollingContext<KeyVaultCertificateWithPolicy>, Mono<PollResponse<KeyVaultCertificateWithPolicy>>> createRecoverPollOperation(String keyName) {
        return pollingContext ->
            withContext(context -> service.getCertificatePollerAsync(vaultUrl, keyName, "", apiVersion,
                ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE)))
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

    private Mono<Response<KeyVaultCertificateWithPolicy>> recoverDeletedCertificateWithResponseAsync(String certificateName,
                                                                                                     Context context) {
        return service.recoverDeletedCertificateAsync(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Recovering deleted certificate - {}", certificateName))
            .doOnSuccess(response ->
                logger.verbose("Recovered the deleted certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to recover the deleted certificate - {}", certificateName, error));
    }

    public Mono<Response<byte[]>> backupCertificateWithResponseAsync(String certificateName, Context context) {
        context = context == null ? Context.NONE : context;

        return service.backupCertificateAsync(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Backing up certificate - {}", certificateName))
            .doOnSuccess(response -> logger.verbose("Backed up the certificate - {}", response.getStatusCode()))
            .doOnError(error -> logger.warning("Failed to back up the certificate - {}", certificateName, error))
            .flatMap(certificateBackupResponse ->
                Mono.just(new SimpleResponse<>(certificateBackupResponse.getRequest(),
                    certificateBackupResponse.getStatusCode(), certificateBackupResponse.getHeaders(),
                    certificateBackupResponse.getValue().getValue())));
    }

    public Response<byte[]> backupCertificateWithResponse(String certificateName, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        Response<CertificateBackup> certificateBackupResponse = service.backupCertificate(vaultUrl, certificateName,
            apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                KEYVAULT_TRACING_NAMESPACE_VALUE));

        return new SimpleResponse<>(certificateBackupResponse.getRequest(), certificateBackupResponse.getStatusCode(),
            certificateBackupResponse.getHeaders(), certificateBackupResponse.getValue().getValue());
    }

    public Mono<Response<KeyVaultCertificateWithPolicy>> restoreCertificateBackupWithResponseAsync(byte[] backup, Context context) {
        CertificateRestoreParameters parameters = new CertificateRestoreParameters().certificateBundleBackup(backup);
        context = context == null ? Context.NONE : context;

        return service.restoreCertificateAsync(vaultUrl, apiVersion, ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Restoring the certificate"))
            .doOnSuccess(response ->
                logger.verbose("Restored the certificate - {}", response.getValue().getProperties().getName()))
            .doOnError(error -> logger.warning("Failed to restore the certificate - {}", error));
    }

    public Response<KeyVaultCertificateWithPolicy> restoreCertificateBackupWithResponse(byte[] backup,
                                                                                        Context context) {
        CertificateRestoreParameters parameters = new CertificateRestoreParameters().certificateBundleBackup(backup);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.restoreCertificate(vaultUrl, apiVersion, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    public PagedFlux<CertificateProperties> listPropertiesOfCertificates() {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listCertificatesFirstPage(false, context)),
                continuationToken -> withContext(context -> listCertificatesNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    public PagedFlux<CertificateProperties> listPropertiesOfCertificates(boolean includePending, Context context) {
        return new PagedFlux<>(
            () -> listCertificatesFirstPage(includePending, context),
            continuationToken -> listCertificatesNextPage(continuationToken, context));
    }

    public PagedFlux<CertificateProperties> listPropertiesOfCertificates(boolean includePending) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listCertificatesFirstPage(includePending, context)),
                continuationToken -> withContext(context -> listCertificatesNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /*
     * Gets attributes of all the certificates given by the {@code nextPageLink} that was retrieved from a call to
     * {@link CertificateAsyncClient#listCertificates()}.
     *
     * @param continuationToken The {@link PagedResponse#nextLink()} from a previous, successful call to one of the
     * listCertificates operations.
     *
     * @return A {@link Mono} of {@link PagedResponse<KeyBase>} from the next page of results.
     */
    private Mono<PagedResponse<CertificateProperties>> listCertificatesNextPage(String continuationToken,
                                                                                Context context) {
        try {
            return service.getCertificatesAsync(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing next certificates page - Page {} ", continuationToken))
                .doOnSuccess(response -> logger.verbose("Listed next certificates page - Page {} ", continuationToken))
                .doOnError(error ->
                    logger.warning("Failed to list next certificates page - Page {} ", continuationToken, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code DEFAULT_MAX_PAGE_RESULTS}
     * values.
     */
    private Mono<PagedResponse<CertificateProperties>> listCertificatesFirstPage(boolean includePending,
                                                                                 Context context) {
        try {
            return service
                .getCertificatesAsync(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, includePending, apiVersion, ACCEPT_LANGUAGE,
                    CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                        KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing certificates"))
                .doOnSuccess(response -> logger.verbose("Listed certificates"))
                .doOnError(error -> logger.warning("Failed to list certificates", error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    public PagedFlux<DeletedCertificate> listDeletedCertificates(boolean includePending) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listDeletedCertificatesFirstPage(includePending, context)),
                continuationToken ->
                    withContext(context -> listDeletedCertificatesNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    public PagedFlux<DeletedCertificate> listDeletedCertificates(Boolean includePending, Context context) {
        return new PagedFlux<>(
            () -> listDeletedCertificatesFirstPage(includePending, context),
            continuationToken -> listDeletedCertificatesNextPage(continuationToken, context));
    }

    /*
     * Gets attributes of all the certificates given by the {@code nextPageLink}
     *
     * @param continuationToken The {@link PagedResponse#nextLink()} from a previous, successful call to one of the list
     * operations.
     *
     * @return A {@link Mono} of {@link PagedResponse<DeletedCertificate>} from the next page of results.
     */
    private Mono<PagedResponse<DeletedCertificate>> listDeletedCertificatesNextPage(String continuationToken,
                                                                                    Context context) {
        try {
            return service.getDeletedCertificatesAsync(vaultUrl, continuationToken, ACCEPT_LANGUAGE,
                    CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                        KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored ->
                    logger.verbose("Listing next deleted certificates page - Page {} ", continuationToken))
                .doOnSuccess(response ->
                    logger.verbose("Listed next deleted certificates page - Page {} ", continuationToken))
                .doOnError(error ->
                    logger.warning("Failed to list next deleted certificates page - Page {} ", continuationToken,
                        error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /*
     * Calls the service and retrieve first page result. It makes one call and retrieve {@code DEFAULT_MAX_PAGE_RESULTS}
     * values.
     */
    private Mono<PagedResponse<DeletedCertificate>> listDeletedCertificatesFirstPage(boolean includePending,
                                                                                     Context context) {
        try {
            return service.getDeletedCertificatesAsync(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, includePending, apiVersion,
                    ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                        KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing deleted certificates"))
                .doOnSuccess(response -> logger.verbose("Listed deleted certificates"))
                .doOnError(error -> logger.warning("Failed to list deleted certificates", error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    public PagedFlux<DeletedCertificate> listDeletedCertificates() {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listDeletedCertificatesFirstPage(false, context)),
                continuationToken ->
                    withContext(context -> listDeletedCertificatesNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    public PagedFlux<CertificateProperties> listPropertiesOfCertificateVersions(String certificateName) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listCertificateVersionsFirstPage(certificateName, context)),
                continuationToken ->
                    withContext(context -> listCertificateVersionsNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    public PagedFlux<CertificateProperties> listPropertiesOfCertificateVersions(String certificateName,
                                                                                Context context) {
        return new PagedFlux<>(
            () -> listCertificateVersionsFirstPage(certificateName, context),
            continuationToken -> listCertificateVersionsNextPage(continuationToken, context));
    }

    private Mono<PagedResponse<CertificateProperties>> listCertificateVersionsFirstPage(String certificateName,
                                                                                        Context context) {
        try {
            return service.getCertificateVersionsAsync(vaultUrl, certificateName, DEFAULT_MAX_PAGE_RESULTS, apiVersion,
                    ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                        KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing certificate versions - {}", certificateName))
                .doOnSuccess(response -> logger.verbose("Listed certificate versions - {}", certificateName))
                .doOnError(error -> logger.warning("Failed to list certificate versions - {}", certificateName, error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /*
     * Gets attributes of all the certificates given by the {@code nextPageLink}
     */
    private Mono<PagedResponse<CertificateProperties>> listCertificateVersionsNextPage(String continuationToken,
                                                                                       Context context) {
        try {
            return service.getCertificatesAsync(vaultUrl, continuationToken, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored ->
                    logger.verbose("Listing next certificate versions page - Page {} ", continuationToken))
                .doOnSuccess(response ->
                    logger.verbose("Listed next certificate versions page - Page {} ", continuationToken))
                .doOnError(error ->
                    logger.warning("Failed to list next certificate versions page - Page {} ", continuationToken,
                        error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    public Mono<Response<KeyVaultCertificateWithPolicy>> mergeCertificateWithResponseAsync(MergeCertificateOptions mergeCertificateOptions, Context context) {
        Objects.requireNonNull(mergeCertificateOptions, "'mergeCertificateOptions' cannot be null.");

        CertificateMergeParameters mergeParameters =
            new CertificateMergeParameters().x509Certificates(mergeCertificateOptions.getX509Certificates())
                .tags(mergeCertificateOptions.getTags())
                .certificateAttributes(new CertificateRequestAttributes().enabled(mergeCertificateOptions.isEnabled()));
        context = context == null ? Context.NONE : context;

        return service.mergeCertificateAsync(vaultUrl, mergeCertificateOptions.getName(), apiVersion, ACCEPT_LANGUAGE,
            mergeParameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Merging certificate - {}",  mergeCertificateOptions.getName()))
            .doOnSuccess(response ->
                logger.verbose("Merged certificate  - {}", response.getValue().getProperties().getName()))
            .doOnError(error ->
                logger.warning("Failed to merge certificate - {}", mergeCertificateOptions.getName(), error));
    }

    public Response<KeyVaultCertificateWithPolicy> mergeCertificateWithResponse(MergeCertificateOptions mergeCertificateOptions,
                                                                                Context context) {
        Objects.requireNonNull(mergeCertificateOptions, "'mergeCertificateOptions' cannot be null.");
        CertificateMergeParameters mergeParameters =
            new CertificateMergeParameters().x509Certificates(mergeCertificateOptions.getX509Certificates())
            .tags(mergeCertificateOptions.getTags())
            .certificateAttributes(new CertificateRequestAttributes().enabled(mergeCertificateOptions.isEnabled()));
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.mergeCertificate(vaultUrl, mergeCertificateOptions.getName(), apiVersion, ACCEPT_LANGUAGE,
            mergeParameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    public Mono<Response<CertificatePolicy>> getCertificatePolicyWithResponseAsync(String certificateName,
                                                                                   Context context) {
        context = context == null ? Context.NONE : context;

        return service.getCertificatePolicyAsync(vaultUrl, apiVersion, ACCEPT_LANGUAGE, certificateName,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Retrieving certificate policy - {}",  certificateName))
            .doOnSuccess(response -> logger.verbose("Retrieved certificate policy - {}", certificateName))
            .doOnError(error -> logger.warning("Failed to retrieve certificate policy - {}", certificateName, error));
    }

    public Response<CertificatePolicy> getCertificatePolicyWithResponse(String certificateName, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getCertificatePolicy(vaultUrl, apiVersion, ACCEPT_LANGUAGE, certificateName,
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    public Mono<Response<CertificatePolicy>> updateCertificatePolicyWithResponseAsync(String certificateName,
                                                                                      CertificatePolicy policy,
                                                                                      Context context) {
        CertificatePolicyRequest policyRequest = new CertificatePolicyRequest(policy);
        context = context == null ? Context.NONE : context;

        return service.updateCertificatePolicyAsync(vaultUrl, apiVersion, ACCEPT_LANGUAGE, certificateName,
                policyRequest, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Updating certificate policy - {}", certificateName))
            .doOnSuccess(response ->
                logger.verbose("Updated the certificate policy - {}", response.getValue().getUpdatedOn()))
            .doOnError(error -> logger.warning("Failed to update the certificate policy - {}", certificateName, error));
    }

    public Response<CertificatePolicy> updateCertificatePolicyWithResponse(String certificateName, CertificatePolicy policy, Context context) {
        CertificatePolicyRequest policyRequest = new CertificatePolicyRequest(policy);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.updateCertificatePolicy(vaultUrl, apiVersion, ACCEPT_LANGUAGE, certificateName, policyRequest,
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
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
        context = context == null ? Context.NONE : context;

        return service.setCertificateIssuerAsync(vaultUrl, apiVersion, ACCEPT_LANGUAGE, issuer.getName(), parameters,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Creating certificate issuer - {}", issuer.getName()))
            .doOnSuccess(response ->
                logger.verbose("Created the certificate issuer - {}", response.getValue().getName()))
            .doOnError(error ->
                logger.warning("Failed to create the certificate issuer - {}", issuer.getName(), error));
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
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    public Mono<Response<CertificateIssuer>> getIssuerWithResponseAsync(String issuerName, Context context) {
        context = context == null ? Context.NONE : context;

        return service.getCertificateIssuerAsync(vaultUrl, apiVersion, ACCEPT_LANGUAGE, issuerName,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Retrieving certificate issuer - {}", issuerName))
            .doOnSuccess(response ->
                logger.verbose("Retrieved the certificate issuer - {}", response.getValue().getName()))
            .doOnError(error ->
                logger.warning("Failed to retreive the certificate issuer - {}", issuerName, error));
    }

    public Response<CertificateIssuer> getIssuerWithResponse(String issuerName, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getCertificateIssuer(vaultUrl, apiVersion, ACCEPT_LANGUAGE, issuerName,
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }


    public Mono<Response<CertificateIssuer>> deleteIssuerWithResponseAsync(String issuerName, Context context) {
        context = context == null ? Context.NONE : context;

        return service.deleteCertificateIssuerAsync(vaultUrl, issuerName, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Deleting certificate issuer - {}",  issuerName))
            .doOnSuccess(response ->
                logger.verbose("Deleted the certificate issuer - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to delete the certificate issuer - {}", issuerName, error));
    }

    public Response<CertificateIssuer> deleteIssuerWithResponse(String issuerName, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.deleteCertificateIssuer(vaultUrl, issuerName, apiVersion, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    public PagedFlux<IssuerProperties> listPropertiesOfIssuers() {
        try {
            return new PagedFlux<>(
                () -> withContext(this::listPropertiesOfIssuersFirstPage),
                continuationToken ->
                    withContext(context -> listPropertiesOfIssuersNextPage(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    public PagedFlux<IssuerProperties> listPropertiesOfIssuers(Context context) {
        return new PagedFlux<>(
            () -> listPropertiesOfIssuersFirstPage(context),
            continuationToken -> listPropertiesOfIssuersNextPage(continuationToken, context));
    }

    private Mono<PagedResponse<IssuerProperties>> listPropertiesOfIssuersFirstPage(Context context) {
        try {
            return service.getCertificateIssuersAsync(vaultUrl, DEFAULT_MAX_PAGE_RESULTS, apiVersion, ACCEPT_LANGUAGE,
                    CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                        KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing certificate issuers - {}"))
                .doOnSuccess(response -> logger.verbose("Listed certificate issuers - {}"))
                .doOnError(error -> logger.warning("Failed to list certificate issuers - {}", error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /*
     * Gets attributes of all the certificates given by the {@code nextPageLink} that was retrieved from a call to
     * {@link KeyAsyncClient#listKeyVersions()}.
     *
     * @param continuationToken The {@link PagedResponse#nextLink()} from a previous, successful call to one of the
     * listKeys operations.
     *
     * @return A {@link Mono} of {@link PagedResponse<KeyBase>} from the next page of results.
     */
    private Mono<PagedResponse<IssuerProperties>> listPropertiesOfIssuersNextPage(String continuationToken,
                                                                                  Context context) {
        try {
            return service.getCertificateIssuersAsync(vaultUrl, continuationToken, ACCEPT_LANGUAGE,
                    CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                        KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored ->
                    logger.verbose("Listing next certificate issuers page - Page {} ", continuationToken))
                .doOnSuccess(response ->
                    logger.verbose("Listed next certificate issuers page - Page {} ", continuationToken))
                .doOnError(error ->
                    logger.warning("Failed to list next certificate issuers page - Page {} ", continuationToken,
                        error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    public Mono<Response<CertificateIssuer>> updateIssuerWithResponseAsync(CertificateIssuer issuer, Context context) {
        CertificateIssuerUpdateParameters updateParameters = new CertificateIssuerUpdateParameters()
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

        return service.updateCertificateIssuerAsync(vaultUrl, issuer.getName(), apiVersion, ACCEPT_LANGUAGE,
                updateParameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Updating certificate issuer - {}", issuer.getName()))
            .doOnSuccess(response ->
                logger.verbose("Updated up the certificate issuer - {}", response.getValue().getName()))
            .doOnError(error ->
                logger.warning("Failed to updated the certificate issuer - {}", issuer.getName(), error));
    }

    public Response<CertificateIssuer> updateIssuerWithResponse(CertificateIssuer issuer, Context context) {
        CertificateIssuerUpdateParameters updateParameters = new CertificateIssuerUpdateParameters()
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
            updateParameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                KEYVAULT_TRACING_NAMESPACE_VALUE));
    }

    public PagedFlux<CertificateContact> setContacts(List<CertificateContact> contacts) {
        try {
            return new PagedFlux<>(() -> withContext(context -> setCertificateContactsWithResponse(contacts, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    public PagedFlux<CertificateContact> setContacts(List<CertificateContact> contacts, Context context) {
        return new PagedFlux<>(() -> setCertificateContactsWithResponse(contacts, context));
    }

    private Mono<PagedResponse<CertificateContact>> setCertificateContactsWithResponse(List<CertificateContact> contacts, Context context) {
        Contacts contactsParams = new Contacts().contactList(contacts);

        return service.setCertificateContactsAsync(vaultUrl, apiVersion, ACCEPT_LANGUAGE, contactsParams,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Listing certificate contacts - {}"))
            .doOnSuccess(response -> logger.verbose("Listed certificate contacts - {}"))
            .doOnError(error -> logger.warning("Failed to list certificate contacts - {}", error));
    }

    public PagedFlux<CertificateContact> listContacts() {
        try {
            return new PagedFlux<>(() -> withContext(this::listCertificateContactsFirstPage));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    public PagedFlux<CertificateContact> listContacts(Context context) {
        return new PagedFlux<>(() -> listCertificateContactsFirstPage(context));
    }

    private Mono<PagedResponse<CertificateContact>> listCertificateContactsFirstPage(Context context) {
        try {
            return service.getCertificateContactsAsync(vaultUrl, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing certificate contacts - {}"))
                .doOnSuccess(response -> logger.verbose("Listed certificate contacts - {}"))
                .doOnError(error -> logger.warning("Failed to list certificate contacts - {}", error));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    public PagedFlux<CertificateContact> deleteContacts() {
        try {
            return new PagedFlux<>(() -> withContext(this::deleteCertificateContactsWithResponse));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    public PagedFlux<CertificateContact> deleteContacts(Context context) {
        return new PagedFlux<>(() -> deleteCertificateContactsWithResponse(context));
    }

    private Mono<PagedResponse<CertificateContact>> deleteCertificateContactsWithResponse(Context context) {
        return service.deleteCertificateContactsAsync(vaultUrl, apiVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Deleting certificate contacts - {}"))
            .doOnSuccess(response -> logger.verbose("Deleted certificate contacts - {}"))
            .doOnError(error -> logger.warning("Failed to delete certificate contacts - {}", error));
    }

    public Mono<Response<CertificateOperation>> deleteCertificateOperationWithResponseAsync(String certificateName,
                                                                                            Context context) {
        return service.deleteCertificateOperationAsync(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
                CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.verbose("Deleting certificate operation - {}", certificateName))
            .doOnSuccess(response -> logger.verbose("Deleted the certificate operation - {}", response.getStatusCode()))
            .doOnError(error ->
                logger.warning("Failed to delete the certificate operation - {}", certificateName, error));
    }

    public Response<CertificateOperation> deleteCertificateOperationWithResponse(String certificateName,
                                                                                 Context context) {
        context = enableSyncRestProxy(context);

        return service.deleteCertificateOperation(vaultUrl, certificateName, apiVersion, ACCEPT_LANGUAGE,
            CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE));
    }


    public Mono<Response<KeyVaultCertificateWithPolicy>> importCertificateWithResponseAsync(ImportCertificateOptions importCertificateOptions,
                                                                                            Context context) {
        CertificateImportParameters parameters = new CertificateImportParameters()
            .base64EncodedCertificate(transformCertificateForImport(importCertificateOptions))
            .certificateAttributes(new CertificateRequestAttributes(importCertificateOptions))
            .password(importCertificateOptions.getPassword())
            .tags(importCertificateOptions.getTags());
        context = context == null ? Context.NONE : context;

        if (importCertificateOptions.getPolicy() != null) {
            parameters.certificatePolicy(new CertificatePolicyRequest(importCertificateOptions.getPolicy()));
        }

        return service.importCertificateAsync(vaultUrl, importCertificateOptions.getName(), apiVersion, ACCEPT_LANGUAGE,
            parameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                KEYVAULT_TRACING_NAMESPACE_VALUE));
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
            parameters, CONTENT_TYPE_HEADER_VALUE, context.addData(AZ_TRACING_NAMESPACE_KEY,
                KEYVAULT_TRACING_NAMESPACE_VALUE));
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
