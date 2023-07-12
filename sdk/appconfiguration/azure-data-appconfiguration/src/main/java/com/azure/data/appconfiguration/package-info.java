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
 * to connect to the service.</p>
 * <ol>
 *   <li>Connection string, see {@link com.azure.data.appconfiguration.ConfigurationClientBuilder#connectionString(java.lang.String) connectionString}.</li>
 *   <li>Azure Active Directory, see {@link com.azure.data.appconfiguration.ConfigurationClientBuilder#credential(com.azure.core.credential.TokenCredential) TokenCredential}.</li>
 * </ol>
 *
 * <p><strong>Sample: Construct Asynchronous Configuration Client with Connection String</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.data.appconfiguration.ConfigurationAsyncClient},
 * using the {@link com.azure.data.appconfiguration.ConfigurationClientBuilder} to configure it with a connection
 * string.</p>
 *
 * <!-- src_embed com.azure.data.applicationconfig.async.configurationclient.instantiation  -->
 * <pre>
 * ConfigurationAsyncClient configurationAsyncClient = new ConfigurationClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.applicationconfig.async.configurationclient.instantiation  -->
 *
 * <p><strong>Sample: Construct Synchronous Configuration Client with Connection String</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.data.appconfiguration.ConfigurationClient},
 * using the {@link com.azure.data.appconfiguration.ConfigurationClientBuilder} to configure it with a connection
 * string.</p>
 *
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
 * <p>For more configuration setting types, see
 * {@link com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting} and
 * {@link com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Add Configuration Setting</h2>
 *
 * <p>The {@link com.azure.data.appconfiguration.ConfigurationClient#addConfigurationSetting(com.azure.data.appconfiguration.models.ConfigurationSetting) addConfigurationSetting}
 * method can be used to add a configuration setting in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to add a setting with the key "prodDBConnection", label "westUS" and value "db_connection" using {@link com.azure.data.appconfiguration.ConfigurationClient}.</p>
 *
 * <!-- src_embed com.azure.data.appconfiguration.ConfigurationClient.addConfigurationSetting#ConfigurationSetting -->
 * <pre>
 * ConfigurationSetting setting = configurationClient.addConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
 *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *     .setLabel&#40;&quot;westUS&quot;&#41;
 *     .setValue&#40;&quot;db_connection&quot;&#41;&#41;;
 * System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getLabel&#40;&#41;, setting.getValue&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.appconfiguration.ConfigurationClient.addConfigurationSetting#ConfigurationSetting -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.data.appconfiguration.ConfigurationAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Update Configuration Setting</h2>
 *
 * <p>The {@link com.azure.data.appconfiguration.ConfigurationClient#setConfigurationSetting(com.azure.data.appconfiguration.models.ConfigurationSetting) setConfigurationSetting}
 * method can be used to update a configuration setting in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to update setting's value "db_connection" to "updated_db_connection"</p>
 *
 * <!-- src_embed com.azure.data.appconfiguration.ConfigurationClient.setConfigurationSetting#ConfigurationSetting -->
 * <pre>
 * ConfigurationSetting setting = configurationClient.setConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
 *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *     .setLabel&#40;&quot;westUS&quot;&#41;
 *     .setValue&#40;&quot;db_connection&quot;&#41;&#41;;
 * System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getLabel&#40;&#41;, setting.getValue&#40;&#41;&#41;;
 *
 * &#47;&#47; Update the value of the setting to &quot;updated_db_connection&quot;.
 * setting = configurationClient.setConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
 *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *     .setLabel&#40;&quot;westUS&quot;&#41;
 *     .setValue&#40;&quot;updated_db_connection&quot;&#41;&#41;;
 * System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getLabel&#40;&#41;, setting.getValue&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.appconfiguration.ConfigurationClient.setConfigurationSetting#ConfigurationSetting -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.data.appconfiguration.ConfigurationAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Get Configuration Setting</h2>
 *
 * <p>The {@link com.azure.data.appconfiguration.ConfigurationClient#getConfigurationSetting(com.azure.data.appconfiguration.models.ConfigurationSetting) getConfigurationSetting}
 * method can be used to get a configuration setting in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to retrieve the setting with the key "prodDBConnection".</p>
 *
 * <!-- src_embed com.azure.data.applicationconfig.configurationclient.getConfigurationSetting#ConfigurationSetting -->
 * <pre>
 * ConfigurationSetting setting = configurationClient.getConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
 *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *     .setLabel&#40;&quot;westUS&quot;&#41;&#41;;
 * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.applicationconfig.configurationclient.getConfigurationSetting#ConfigurationSetting -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.data.appconfiguration.ConfigurationAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Delete Configuration Setting</h2>
 *
 * <p>The {@link com.azure.data.appconfiguration.ConfigurationClient#deleteConfigurationSetting(com.azure.data.appconfiguration.models.ConfigurationSetting) deleteConfigurationSetting}
 * method can be used to delete a configuration setting in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to delete the setting with the key "prodDBConnection".</p>
 *
 * <!-- src_embed com.azure.data.applicationconfig.configurationclient.deleteConfigurationSetting#ConfigurationSetting -->
 * <pre>
 * ConfigurationSetting setting = configurationClient.deleteConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
 *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *     .setLabel&#40;&quot;westUS&quot;&#41;&#41;;
 * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.applicationconfig.configurationclient.deleteConfigurationSetting#ConfigurationSetting -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.data.appconfiguration.ConfigurationAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>List Configuration Settings</h2>
 *
 * <p>The {@link com.azure.data.appconfiguration.ConfigurationClient#listConfigurationSettings(com.azure.data.appconfiguration.models.SettingSelector) listConfigurationSettings}
 * method can be used to list configuration settings in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to list all settings that use the key "prodDBConnection".</p>
 * <!-- src_embed com.azure.data.applicationconfig.configurationclient.listConfigurationSettings#settingSelector -->
 * <pre>
 * SettingSelector settingSelector = new SettingSelector&#40;&#41;.setKeyFilter&#40;&quot;prodDBConnection&quot;&#41;;
 * configurationClient.listConfigurationSettings&#40;settingSelector&#41;.forEach&#40;setting -&gt; &#123;
 *     System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.data.applicationconfig.configurationclient.listConfigurationSettings#settingSelector -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.data.appconfiguration.ConfigurationAsyncClient}.</p>
 *
 * @see com.azure.data.appconfiguration.ConfigurationClientBuilder
 * @see com.azure.data.appconfiguration.ConfigurationAsyncClient
 * @see com.azure.data.appconfiguration.ConfigurationClient
 * @see com.azure.data.appconfiguration.models.ConfigurationSetting
 */
package com.azure.data.appconfiguration;
