// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>The Azure App Configuration library perform operation on Azure App Configuration service.
 * It helps developers centralize their application configurations simply and securely by storing
 * all the settings for their application and secure accesses in one place.</p>
 *
 * <p>Azure App Configuration uses builder pattern to create an asynchronous or synchronous client.</p>
 *
 * <p><strong>Sample: Create a configuration client </strong></p>
 *
 * <p>See {@link com.azure.data.appconfiguration.ConfigurationClientBuilder} for how to use the client builder
 * to create client before make any request to service.</p>
 *
 * <p>App Configuration supports two types of authentication methods.
 * (1) Connection string, see {@link com.azure.data.appconfiguration.ConfigurationClientBuilder#connectionString(java.lang.String)}
 * (2) Azure Active Directory, see {@link com.azure.data.appconfiguration.ConfigurationClientBuilder#credential(com.azure.core.credential.TokenCredential)}
 * </p>
 *
 * <p>App Configuration support multiple operations, such as create, update, retrieve, and delete a configuration setting.
 * See methods in client level class below to explore all capabilities that library provides.
 *
 * <p><strong>Sample: Use asynchronous App Configuration client </strong></p>
 * <p>See class description in {@link com.azure.data.appconfiguration.ConfigurationAsyncClient}</p>
 *
 * <p><strong>Sample: Use synchronous App Configuration client </strong></p>
 * <p>See class description in {@link com.azure.data.appconfiguration.ConfigurationClient}</p>
 */
package com.azure.data.appconfiguration;
