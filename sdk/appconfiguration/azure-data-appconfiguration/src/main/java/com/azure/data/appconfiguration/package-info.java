// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
/**
 * <p><a href="https://learn.microsoft.com/azure/azure-app-configuration/">Azure App Configuration Service</a>
 * is a managed service provided by Microsoft Azure that allows developers to centralize configuration settings for
 * their applications. With App Configuration, developers can store and manage application settings, feature flags,
 * and other configuration data in one central location. This simplifies the management of configuration settings and
 * makes it easy to update configuration values for multiple applications.</p>
 *
 * <p>The Azure App Configuration library is a client library that provides Java developers with a simple and
 * easy-to-use interface for accessing and using the Azure App Configuration Service. This library allows developers to
 * easily manage their application's configuration settings, feature flags, and other configuration data stored in the
 * Azure App Configuration Service.
 * </p>
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the App Configuration service you'll need to create an instance of the Configuration
 * Client class. To make this possible you'll need the connection string of the configuration store. Alternatively,
 * you can use AAD authentication via
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable"> Azure Identity</a>
 * to connect to the service.
 *
 * (1) Connection string, see {@link com.azure.data.appconfiguration.ConfigurationClientBuilder#connectionString(java.lang.String)}
 * (2) Azure Active Directory, see {@link com.azure.data.appconfiguration.ConfigurationClientBuilder#credential(com.azure.core.credential.TokenCredential)}
 * </p>
 *
 * <p><strong>Sample: Construct Asynchronous Configuration Client with Connection String</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.data.appconfiguration.ConfigurationAsyncClient},
 * using the {@link com.azure.data.appconfiguration.ConfigurationClientBuilder} to configure it with a connection
 * string.</p>
 * <!-- src_embed com.azure.data.applicationconfig.async.configurationclient.instantiation  -->
 * <pre>
 * ConfigurationAsyncClient configurationAsyncClient = new ConfigurationClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.applicationconfig.async.configurationclient.instantiation  -->
 *
 * <p><strong>Sample: Construct synchronous Configuration Client with Connection String</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.data.appconfiguration.ConfigurationClient},
 * using the {@link com.azure.data.appconfiguration.ConfigurationClientBuilder} to configure it with a connection
 * string.</p>
 * <!-- src_embed com.azure.data.applicationconfig.configurationclient.instantiation -->
 * <pre>
 * ConfigurationClient configurationClient = new ConfigurationClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.applicationconfig.configurationclient.instantiation -->
 *
 * <p>App Configuration support multiple operations, such as create, update, retrieve, and delete a configuration setting.
 * See methods in client level class below to explore all capabilities that library provides.</p>
 *
 * <p><strong>Sample: Use asynchronous App Configuration client </strong></p>
 * <p>See class description in {@link com.azure.data.appconfiguration.ConfigurationAsyncClient}</p>
 *
 * <p><strong>Sample: Use synchronous App Configuration client </strong></p>
 * <p>See class description in {@link com.azure.data.appconfiguration.ConfigurationClient}</p>
 */
package com.azure.data.appconfiguration;
