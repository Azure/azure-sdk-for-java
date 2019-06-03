// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.certificates;

import com.azure.core.annotations.ExpectedResponses;
import com.azure.core.annotations.Host;
import com.azure.core.annotations.UnexpectedResponseExceptionType;
import com.azure.core.annotations.HostParam;
import com.azure.core.annotations.PathParam;
import com.azure.core.annotations.BodyParam;
import com.azure.core.annotations.QueryParam;
import com.azure.core.annotations.HeaderParam;
import com.azure.core.annotations.GET;
import com.azure.core.annotations.PATCH;
import com.azure.core.annotations.POST;
import com.azure.core.exception.HttpRequestException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.exception.ServerException;
import com.azure.core.http.rest.Response;
import com.azure.keyvault.certificates.models.Certificate;
import reactor.core.publisher.Mono;

/**
 * The interface defining all the services for {@link CertificateAsyncClient} to be used
 * by the proxy service to perform REST calls.
 *
 * This is package-private so that these REST calls are transparent to the user.
 */
@Host("{url}")
interface CertificateService {

    @POST("certificates/{certificate-name}/create")
    @ExpectedResponses({202})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<CertificateOperation>> createCertificate(@HostParam("url") String url,
                                          @PathParam("certificate-name") String certificateName,
                                          @QueryParam("api-version") String apiVersion,
                                          @HeaderParam("accept-language") String acceptLanguage,
                                          @BodyParam("body") CertificateRequestParameters parameters,
                                          @HeaderParam("Content-Type") String type);

    @GET("certificates/{certificate-name}/{certificate-version}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(code = {403}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<Certificate>> getCertificate(@HostParam("url") String url,
                                     @PathParam("certificate-name") String certificateName,
                                     @PathParam("certificate-version") String certificateVersion,
                                     @QueryParam("api-version") String apiVersion,
                                     @HeaderParam("accept-language") String acceptLanguage,
                                     @HeaderParam("Content-Type") String type);


    @PATCH("certificates/{certificate-name}/policy")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {500}, value = ServerException.class)
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<Certificate>> updateCertificateProperties(@HostParam("url") String url,
                                            @PathParam("certificate-name") String certificateName,
                                            @QueryParam("api-version") String apiVersion,
                                            @HeaderParam("accept-language") String acceptLanguage,
                                            @BodyParam("body") CertificatePolicyRequest properties,
                                            @HeaderParam("Content-Type") String type);


    /*@DELETE("secrets/{secret-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<DeletedSecret>> deleteSecret(@HostParam("url") String url,
                                               @PathParam("secret-name") String secretName,
                                               @QueryParam("api-version") String apiVersion,
                                               @HeaderParam("accept-language") String acceptLanguage,
                                               @HeaderParam("Content-Type") String type);


    @GET("deletedsecrets/{secret-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<DeletedSecret>> getDeletedSecret(@HostParam("url") String url,
                                                   @PathParam("secret-name") String secretName,
                                                   @QueryParam("api-version") String apiVersion,
                                                   @HeaderParam("accept-language") String acceptLanguage,
                                                   @HeaderParam("Content-Type") String type);

    @DELETE("deletedsecrets/{secret-name}")
    @ExpectedResponses({204})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<VoidResponse> purgeDeletedSecret(@HostParam("url") String url,
                                          @PathParam("secret-name") String secretName,
                                          @QueryParam("api-version") String apiVersion,
                                          @HeaderParam("accept-language") String acceptLanguage,
                                          @HeaderParam("Content-Type") String type);


    @POST("deletedsecrets/{secret-name}/recover")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<Secret>> recoverDeletedSecret(@HostParam("url") String url,
                                                @PathParam("secret-name") String secretName,
                                                @QueryParam("api-version") String apiVersion,
                                                @HeaderParam("accept-language") String acceptLanguage,
                                                @HeaderParam("Content-Type") String type);


    @POST("secrets/{secret-name}/backup")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<SecretBackup>> backupSecret(@HostParam("url") String url,
                                              @PathParam("secret-name") String secretName,
                                              @QueryParam("api-version") String apiVersion,
                                              @HeaderParam("accept-language") String acceptLanguage,
                                              @HeaderParam("Content-Type") String type);



    @POST("secrets/restore")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    Mono<Response<Secret>> restoreSecret(@HostParam("url") String url,
                                         @QueryParam("api-version") String apiVersion,
                                         @HeaderParam("accept-language") String acceptLanguage,
                                         @BodyParam("application/json") SecretRestoreRequestParameters parameters,
                                         @HeaderParam("Content-Type") String type);


    @GET("secrets")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    @ReturnValueWireType(SecretBasePage.class)
    Mono<PagedResponse<SecretBase>> getSecrets(@HostParam("url") String url,
                                               @QueryParam("maxresults") Integer maxresults,
                                               @QueryParam("api-version") String apiVersion,
                                               @HeaderParam("accept-language") String acceptLanguage,
                                               @HeaderParam("Content-Type") String type);


    @GET("secrets/{secret-name}/versions")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    @ReturnValueWireType(SecretBasePage.class)
    Mono<PagedResponse<SecretBase>> getSecretVersions(@HostParam("url") String url,
                                                      @PathParam("secret-name") String secretName,
                                                      @QueryParam("maxresults") Integer maxresults,
                                                      @QueryParam("api-version") String apiVersion,
                                                      @HeaderParam("accept-language") String acceptLanguage,
                                                      @HeaderParam("Content-Type") String type);


    @GET("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    @ReturnValueWireType(SecretBasePage.class)
    Mono<PagedResponse<SecretBase>> getSecrets(@HostParam("url") String url,
                                               @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                               @HeaderParam("accept-language") String acceptLanguage,
                                               @HeaderParam("Content-Type") String type);


    @GET("deletedsecrets")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    @ReturnValueWireType(DeletedSecretPage.class)
    Mono<PagedResponse<DeletedSecret>> getDeletedSecrets(@HostParam("url") String url,
                                                         @QueryParam("maxresults") Integer maxresults,
                                                         @QueryParam("api-version") String apiVersion,
                                                         @HeaderParam("accept-language") String acceptLanguage,
                                                         @HeaderParam("Content-Type") String type);

    @GET("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpRequestException.class)
    @ReturnValueWireType(DeletedSecretPage.class)
    Mono<PagedResponse<DeletedSecret>> getDeletedSecrets(@HostParam("url") String url,
                                                         @PathParam(value = "nextUrl", encoded = true) String nextUrl,
                                                         @HeaderParam("accept-language") String acceptLanguage,
                                                         @HeaderParam("Content-Type") String type);*/
}
