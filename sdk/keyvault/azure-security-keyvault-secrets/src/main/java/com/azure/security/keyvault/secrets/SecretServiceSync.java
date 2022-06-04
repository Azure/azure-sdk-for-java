// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.security.keyvault.secrets.implementation.SecretRequestParameters;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

/**
 * The interface defining all the services for {@link SecretAsyncClient} to be used
 * by the proxy service to perform REST calls.
 *
 * This is package-private so that these REST calls are transparent to the user.
 */
@Host("{url}")
@ServiceInterface(name = "KeyVaultSecrets")
interface SecretServiceSync {

    @Put("secrets/{secret-name}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(code = {400}, value = ResourceModifiedException.class)
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Response<KeyVaultSecret> setSecret(@HostParam("url") String url,
                                             @PathParam("secret-name") String secretName,
                                             @QueryParam("api-version") String apiVersion,
                                             @HeaderParam("accept-language") String acceptLanguage,
                                             @BodyParam("application/json") SecretRequestParameters parameters,
                                             @HeaderParam("Content-Type") String type,
                                             Context context);
}
