// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

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
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.security.keyvault.certificates.implementation.CertificatePropertiesPage;
import com.azure.security.keyvault.certificates.implementation.ContactPage;
import com.azure.security.keyvault.certificates.implementation.DeletedCertificatePage;
import com.azure.security.keyvault.certificates.implementation.IssuerPropertiesPage;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.azure.security.keyvault.certificates.models.IssuerProperties;
import com.azure.security.keyvault.certificates.models.CertificateContact;
import reactor.core.publisher.Mono;

/**
 * The interface defining all the services for {@link CertificateAsyncClient} to be used
 * by the proxy service to perform REST calls.
 *
 * This is package-private so that these REST calls are transparent to the user.
 */
@Host("{url}")
@ServiceInterface(name = "KeyVault")
interface CertificateService {

    @Post("certificates/{certificate-name}/create")
    @ExpectedResponses({202})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<CertificateOperation>> createCertificate(@HostParam("url") String url,
                                          @PathParam("certificate-name") String certificateName,
                                          @QueryParam("api-version") String apiVersion,
                                          @HeaderParam("accept-language") String acceptLanguage,
                                          @BodyParam("body") CertificateRequestParameters parameters,
                                          @HeaderParam("Content-Type") String type,
                                          Context context);

    @Get("certificates/{certificate-name}/pending")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<CertificateOperation>> getCertificateOperation(@HostParam("url") String url,
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
    Mono<Response<CertificateOperation>> updateCertificateOperation(@HostParam("url") String url,
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
    Mono<Response<CertificateOperation>>deletetCertificateOperation(@HostParam("url") String url,
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
    Mono<Response<KeyVaultCertificate>> getCertificate(@HostParam("url") String url,
                                                       @PathParam("certificate-name") String certificateName,
                                                       @PathParam("certificate-version") String certificateVersion,
                                                       @QueryParam("api-version") String apiVersion,
                                                       @HeaderParam("accept-language") String acceptLanguage,
                                                       @HeaderParam("Content-Type") String type,
                                                       Context context);

    @Get("certificates/{certificate-name}/{certificate-version}")
    @ExpectedResponses({200, 404})
    @UnexpectedResponseExceptionType(code = {403}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyVaultCertificateWithPolicy>> getCertificatePoller(@HostParam("url") String url,
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
    Mono<Response<KeyVaultCertificateWithPolicy>> getCertificateWithPolicy(@HostParam("url") String url,
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
    Mono<PagedResponse<CertificateProperties>> getCertificates(@HostParam("url") String url,
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
    Mono<PagedResponse<CertificateProperties>> getCertificates(@HostParam("url") String url,
                                                               @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                               @HeaderParam("accept-language") String acceptLanguage,
                                                               @HeaderParam("Content-Type") String type,
                                                               Context context);

    @Delete("certificates/{certificate-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<DeletedCertificate>> deleteCertificate(@HostParam("url") String url,
                                                         @PathParam("certificate-name") String certificateName,
                                                         @QueryParam("api-version") String apiVersion,
                                                         @HeaderParam("accept-language") String acceptLanguage,
                                                         @HeaderParam("Content-Type") String type,
                                                         Context context);

    @Patch("certificates/{certificate-name}/{certificate-version}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {500}, value = HttpResponseException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyVaultCertificate>> updateCertificate(@HostParam("url") String url,
                                                          @PathParam("certificate-name") String certificateName,
                                                          @QueryParam("api-version") String apiVersion,
                                                          @HeaderParam("accept-language") String acceptLanguage,
                                                          @BodyParam("body") CertificateUpdateParameters properties,
                                                          @HeaderParam("Content-Type") String type,
                                                          Context context);


    @Get("deletedcertificates/{certificate-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<DeletedCertificate>> getDeletedCertificate(@HostParam("url") String url,
                                                   @PathParam("certificate-name") String certificateName,
                                                   @QueryParam("api-version") String apiVersion,
                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                   @HeaderParam("Content-Type") String type,
                                                   Context context);

    @Get("deletedcertificates/{certificate-name}")
    @ExpectedResponses({200, 404})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<DeletedCertificate>> getDeletedCertificatePoller(@HostParam("url") String url,
                                                             @PathParam("certificate-name") String certificateName,
                                                             @QueryParam("api-version") String apiVersion,
                                                             @HeaderParam("accept-language") String acceptLanguage,
                                                             @HeaderParam("Content-Type") String type,
                                                             Context context);

    @Delete("deletedcertificates/{certificate-name}")
    @ExpectedResponses({204})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Void>> purgeDeletedcertificate(@HostParam("url") String url,
                                               @PathParam("certificate-name") String certificateName,
                                               @QueryParam("api-version") String apiVersion,
                                               @HeaderParam("accept-language") String acceptLanguage,
                                               @HeaderParam("Content-Type") String type,
                                               Context context);

    @Post("deletedcertificates/{certificate-name}/recover")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyVaultCertificateWithPolicy>> recoverDeletedCertificate(@HostParam("url") String url,
                                                                  @PathParam("certificate-name") String certificateName,
                                                                  @QueryParam("api-version") String apiVersion,
                                                                  @HeaderParam("accept-language") String acceptLanguage,
                                                                  @HeaderParam("Content-Type") String type,
                                                                  Context context);


    @Post("certificates/{certificate-name}/backup")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<CertificateBackup>> backupCertificate(@HostParam("url") String url,
                                              @PathParam("certificate-name") String certificateName,
                                              @QueryParam("api-version") String apiVersion,
                                              @HeaderParam("accept-language") String acceptLanguage,
                                              @HeaderParam("Content-Type") String type,
                                              Context context);


    @Post("certificates/restore")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyVaultCertificateWithPolicy>> restoreCertificate(@HostParam("url") String url,
                                                           @QueryParam("api-version") String apiVersion,
                                                           @HeaderParam("accept-language") String acceptLanguage,
                                                           @BodyParam("application/json") CertificateRestoreParameters parameters,
                                                           @HeaderParam("Content-Type") String type,
                                                           Context context);

    @Get("deletedcertificates")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(DeletedCertificatePage.class)
    Mono<PagedResponse<DeletedCertificate>> getDeletedCertificates(@HostParam("url") String url,
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
    Mono<PagedResponse<DeletedCertificate>> getDeletedCertificates(@HostParam("url") String url,
                                                         @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                         @HeaderParam("accept-language") String acceptLanguage,
                                                         @HeaderParam("Content-Type") String type,
                                                         Context context);

    @Put("certificates/contacts")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(ContactPage.class)
    Mono<PagedResponse<CertificateContact>> setCertificateContacts(@HostParam("url") String url,
                                                                   @QueryParam("api-version") String apiVersion,
                                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                                   @BodyParam("application/json") Contacts contacts,
                                                                   @HeaderParam("Content-Type") String type,
                                                                   Context context);

    @Get("certificates/contacts")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(ContactPage.class)
    Mono<PagedResponse<CertificateContact>> getCertificateContacts(@HostParam("url") String url,
                                                                   @QueryParam("api-version") String apiVersion,
                                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                                   @HeaderParam("Content-Type") String type,
                                                                   Context context);

    @Delete("certificates/contacts")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(ContactPage.class)
    Mono<PagedResponse<CertificateContact>> deleteCertificateContacts(@HostParam("url") String url,
                                                                      @QueryParam("api-version") String apiVersion,
                                                                      @HeaderParam("accept-language") String acceptLanguage,
                                                                      @HeaderParam("Content-Type") String type,
                                                                      Context context);

    @Put("certificates/issuers/{issuer-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<CertificateIssuer>> setCertificateIssuer(@HostParam("url") String url,
                                                           @QueryParam("api-version") String apiVersion,
                                                           @HeaderParam("accept-language") String acceptLanguage,
                                                           @PathParam("issuer-name") String issuerName,
                                                           @BodyParam("application/json") CertificateIssuerSetParameters parameter,
                                                           @HeaderParam("Content-Type") String type,
                                                           Context context);


    @Get("certificates/issuers/{issuer-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<CertificateIssuer>> getCertificateIssuer(@HostParam("url") String url,
                                                           @QueryParam("api-version") String apiVersion,
                                                           @HeaderParam("accept-language") String acceptLanguage,
                                                           @PathParam("issuer-name") String issuerName,
                                                           @HeaderParam("Content-Type") String type,
                                                           Context context);


    @Delete("certificates/issuers/{issuer-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<CertificateIssuer>> deleteCertificateIssuer(@HostParam("url") String url,
                                                              @PathParam("issuer-name") String issuerName,
                                                              @QueryParam("api-version") String apiVersion,
                                                              @HeaderParam("accept-language") String acceptLanguage,
                                                              @HeaderParam("Content-Type") String type,
                                                              Context context);

    @Patch("certificates/issuers/{issuer-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {500}, value = HttpResponseException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<CertificateIssuer>> updateCertificateIssuer(@HostParam("url") String url,
                                                              @PathParam("issuer-name") String issuerName,
                                                              @QueryParam("api-version") String apiVersion,
                                                              @HeaderParam("accept-language") String acceptLanguage,
                                                              @BodyParam("body") CertificateIssuerUpdateParameters properties,
                                                              @HeaderParam("Content-Type") String type,
                                                              Context context);

    @Get("certificates/Issuers")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(IssuerPropertiesPage.class)
    Mono<PagedResponse<IssuerProperties>> getCertificateIssuers(@HostParam("url") String url,
                                                                @QueryParam("maxresults") Integer maxresults,
                                                                @QueryParam("api-version") String apiVersion,
                                                                @HeaderParam("accept-language") String acceptLanguage,
                                                                @HeaderParam("Content-Type") String type,
                                                                Context context);


    @Get("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(IssuerPropertiesPage.class)
    Mono<PagedResponse<IssuerProperties>> getCertificateIssuers(@HostParam("url") String url,
                                                                @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                                @HeaderParam("accept-language") String acceptLanguage,
                                                                @HeaderParam("Content-Type") String type,
                                                                Context context);


    @Get("certificates/{certificate-name}/versions")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(CertificatePropertiesPage.class)
    Mono<PagedResponse<CertificateProperties>> getCertificateVersions(@HostParam("url") String url,
                                                                      @PathParam("certificate-name") String certificateName,
                                                                      @QueryParam("maxresults") Integer maxresults,
                                                                      @QueryParam("api-version") String apiVersion,
                                                                      @HeaderParam("accept-language") String acceptLanguage,
                                                                      @HeaderParam("Content-Type") String type,
                                                                      Context context);


    @Post("certificates/{certificate-name}/import")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyVaultCertificateWithPolicy>> importCertificate(@HostParam("url") String url,
                                                          @PathParam("certificate-name") String certificateName,
                                                          @QueryParam("api-version") String apiVersion,
                                                          @HeaderParam("accept-language") String acceptLanguage,
                                                          @BodyParam("application/json") CertificateImportParameters parameters,
                                                          @HeaderParam("Content-Type") String type,
                                                          Context context);

    @Post("certificates/{certificate-name}/pending/merge")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<KeyVaultCertificateWithPolicy>> mergeCertificate(@HostParam("url") String url,
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
    Mono<Response<CertificatePolicy>> getCertificatePolicy(@HostParam("url") String url,
                                                           @QueryParam("api-version") String apiVersion,
                                                           @HeaderParam("accept-language") String acceptLanguage,
                                                           @PathParam("certificate-name") String certificateName,
                                                           @HeaderParam("Content-Type") String type,
                                                           Context context);

    @Patch("certificates/{certificate-name}/policy")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<CertificatePolicy>> updateCertificatePolicy(@HostParam("url") String url,
                                                           @QueryParam("api-version") String apiVersion,
                                                           @HeaderParam("accept-language") String acceptLanguage,
                                                           @PathParam("certificate-name") String certificateName,
                                                           @BodyParam("application/json") CertificatePolicyRequest certificatePolicyRequest,
                                                           @HeaderParam("Content-Type") String type,
                                                           Context context);

    @Get("certificates/{certificate-name}/pending")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<CertificateOperation>> getPendingCertificateSigningRequest(@HostParam("url") String url,
                                                              @QueryParam("api-version") String apiVersion,
                                                              @HeaderParam("accept-language") String acceptLanguage,
                                                              @PathParam("certificate-name") String certificateName,
                                                              @HeaderParam("Content-Type") String type,
                                                              Context context);
}
