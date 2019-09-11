// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.annotation.BodyParam;
import com.azure.core.implementation.annotation.Delete;
import com.azure.core.implementation.annotation.ExpectedResponses;
import com.azure.core.implementation.annotation.Get;
import com.azure.core.implementation.annotation.HeaderParam;
import com.azure.core.implementation.annotation.Host;
import com.azure.core.implementation.annotation.HostParam;
import com.azure.core.implementation.annotation.Patch;
import com.azure.core.implementation.annotation.Post;
import com.azure.core.implementation.annotation.Put;
import com.azure.core.implementation.annotation.PathParam;
import com.azure.core.implementation.annotation.QueryParam;
import com.azure.core.implementation.annotation.ReturnValueWireType;
import com.azure.core.implementation.annotation.ServiceInterface;
import com.azure.core.implementation.annotation.UnexpectedResponseExceptionType;
import com.azure.core.util.Context;
import com.azure.security.keyvault.certificates.implementation.CertificateBasePage;
import com.azure.security.keyvault.certificates.implementation.ContactPage;
import com.azure.security.keyvault.certificates.implementation.DeletedCertificatePage;
import com.azure.security.keyvault.certificates.implementation.IssuerBasePage;
import com.azure.security.keyvault.certificates.models.Certificate;
import com.azure.security.keyvault.certificates.models.CertificateBase;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.Contact;
import com.azure.security.keyvault.certificates.models.Issuer;
import com.azure.security.keyvault.certificates.models.IssuerBase;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
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
    Mono<Response<Certificate>> getCertificate(@HostParam("url") String url,
                                               @PathParam("certificate-name") String certificateName,
                                               @PathParam("certificate-version") String certificateVersion,
                                               @QueryParam("api-version") String apiVersion,
                                               @HeaderParam("accept-language") String acceptLanguage,
                                               @HeaderParam("Content-Type") String type,
                                               Context context);



    @Get("certificates")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(CertificateBasePage.class)
    Mono<PagedResponse<CertificateBase>> getCertificates(@HostParam("url") String url,
                                                         @QueryParam("maxresults") Integer maxresults,
                                                         @QueryParam("includePending") Boolean includePending,
                                                         @QueryParam("api-version") String apiVersion,
                                                         @HeaderParam("accept-language") String acceptLanguage,
                                                         @HeaderParam("Content-Type") String type,
                                                         Context context);



    @Get("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(CertificateBasePage.class)
    Mono<PagedResponse<CertificateBase>> getCertificates(@HostParam("url") String url,
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
    Mono<Response<Certificate>> updateCertificate(@HostParam("url") String url,
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

    @Delete("deletedcertificates/{certificate-name}")
    @ExpectedResponses({204})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<VoidResponse> purgeDeletedcertificate(@HostParam("url") String url,
                                               @PathParam("certificate-name") String certificateName,
                                               @QueryParam("api-version") String apiVersion,
                                               @HeaderParam("accept-language") String acceptLanguage,
                                               @HeaderParam("Content-Type") String type,
                                               Context context);

    @Post("deletedcertificates/{certificate-name}/recover")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Certificate>> recoverDeletedCertificate(@HostParam("url") String url,
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
    Mono<Response<Certificate>> restoreCertificate(@HostParam("url") String url,
                                         @QueryParam("api-version") String apiVersion,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @BodyParam("application/json") CertificateRestoreParameters parameters,
                                         @HeaderParam("Content-Type") String type,
                                         Context context);

    @Get("deletedsecrets")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(DeletedCertificatePage.class)
    Mono<PagedResponse<DeletedCertificate>> getDeletedCertificates(@HostParam("url") String url,
                                                         @QueryParam("maxresults") Integer maxresults,
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
    Mono<PagedResponse<Contact>> setCertificateContacts(@HostParam("url") String url,
                                                        @QueryParam("api-version") String apiVersion,
                                                        @HeaderParam("accept-language") String acceptLanguage,
                                                        @BodyParam("application/json") Contacts contacts,
                                                        @HeaderParam("Content-Type") String type,
                                                        Context context);

    @Get("certificates/contacts")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(ContactPage.class)
    Mono<PagedResponse<Contact>> getCertificateContacts(@HostParam("url") String url,
                                                                   @QueryParam("api-version") String apiVersion,
                                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                                   @HeaderParam("Content-Type") String type,
                                                                   Context context);

    @Delete("certificates/contacts")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(ContactPage.class)
    Mono<PagedResponse<Contact>> deleteCertificateContacts(@HostParam("url") String url,
                                                         @QueryParam("api-version") String apiVersion,
                                                         @HeaderParam("accept-language") String acceptLanguage,
                                                         @HeaderParam("Content-Type") String type,
                                                         Context context);

    @Put("certificates/issuers/{issuer-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Issuer>> setCertificateIssuer(@HostParam("url") String url,
                                                     @QueryParam("api-version") String apiVersion,
                                                     @HeaderParam("accept-language") String acceptLanguage,
                                                     @PathParam("issuer-name") String issuerName,
                                                     @BodyParam("application/json") CertificateIssuerSetParameters parameter,
                                                     @HeaderParam("Content-Type") String type,
                                                     Context context);


    @Get("certificates/issuers/{issuer-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Issuer>> getCertificateIssuer(@HostParam("url") String url,
                                                     @QueryParam("api-version") String apiVersion,
                                                     @HeaderParam("accept-language") String acceptLanguage,
                                                     @PathParam("issuer-name") String issuerName,
                                                     @HeaderParam("Content-Type") String type,
                                                     Context context);


    @Delete("certificates/issuers/{issuer-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Issuer>> deleteCertificateIssuer(@HostParam("url") String url,
                                                         @PathParam("issuer-name") String issuerName,
                                                         @QueryParam("api-version") String apiVersion,
                                                         @HeaderParam("accept-language") String acceptLanguage,
                                                         @HeaderParam("Content-Type") String type,
                                                         Context context);

    @Patch("certificates/issuers/{issuer-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {500}, value = HttpResponseException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Issuer>> updateCertificateIssuer(@HostParam("url") String url,
                                                  @PathParam("issuer-name") String issuerName,
                                                  @QueryParam("api-version") String apiVersion,
                                                  @HeaderParam("accept-language") String acceptLanguage,
                                                  @BodyParam("body") CertificateIssuerUpdateParameters properties,
                                                  @HeaderParam("Content-Type") String type,
                                                  Context context);

    @Get("certificates/Issuers")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(IssuerBasePage.class)
    Mono<PagedResponse<IssuerBase>> getCertificateIssuers(@HostParam("url") String url,
                                                         @QueryParam("maxresults") Integer maxresults,
                                                         @QueryParam("api-version") String apiVersion,
                                                         @HeaderParam("accept-language") String acceptLanguage,
                                                         @HeaderParam("Content-Type") String type,
                                                         Context context);


    @Get("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(IssuerBasePage.class)
    Mono<PagedResponse<IssuerBase>> getCertificateIssuers(@HostParam("url") String url,
                                                         @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                         @HeaderParam("accept-language") String acceptLanguage,
                                                         @HeaderParam("Content-Type") String type,
                                                         Context context);


    @Get("certificates/{certificate-name}/versions")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(CertificateBasePage.class)
    Mono<PagedResponse<CertificateBase>> getCertificateVersions(@HostParam("url") String url,
                                                                @PathParam("certificate-name") String certificateName,
                                                                @QueryParam("maxresults") Integer maxresults,
                                                                @QueryParam("api-version") String apiVersion,
                                                                @HeaderParam("accept-language") String acceptLanguage,
                                                                @HeaderParam("Content-Type") String type,
                                                                Context context);


    @Post("certificates/{certificate-name}/import")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Certificate>> importCertificate(@HostParam("url") String url,
                                                                @PathParam("certificate-name") String certificateName,
                                                                @QueryParam("api-version") String apiVersion,
                                                                @HeaderParam("accept-language") String acceptLanguage,
                                                                @BodyParam("application/json") CertificateImportParameters parameters,
                                                                @HeaderParam("Content-Type") String type,
                                                                Context context);

    @Post("certificates/{certificate-name}/pending/merge")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<Certificate>> mergeCertificate(@HostParam("url") String url,
                                                       @PathParam("certificate-name") String certificateName,
                                                       @QueryParam("api-version") String apiVersion,
                                                       @HeaderParam("accept-language") String acceptLanguage,
                                                       @BodyParam("application/json") CertificateMergeParameters parameters,
                                                       @HeaderParam("Content-Type") String type,
                                                       Context context);

    @Get("certificates/{certificate-name}/policy")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<CertificatePolicy>> getCertificatePolicy(@HostParam("url") String url,
                                                           @QueryParam("api-version") String apiVersion,
                                                           @HeaderParam("accept-language") String acceptLanguage,
                                                           @PathParam("certificate-name") String certificateName,
                                                           @HeaderParam("Content-Type") String type,
                                                           Context context);

    @Patch("certificates/{certificate-name}/policy")
    @ExpectedResponses({200})
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
