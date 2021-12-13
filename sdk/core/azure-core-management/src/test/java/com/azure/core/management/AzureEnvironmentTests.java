// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

public class AzureEnvironmentTests {

    private static final String AZURE_CLOUD_ENDPOINT = "https://management.azure.com/";

    @Test
    public void testAzureEnvironmentFromArmEndpoint() throws Exception {
        AzureEnvironment azureEnvironment = AzureEnvironment.fromAzureResourceManagerEndpoint(AZURE_CLOUD_ENDPOINT, mockResponse());

        Assertions.assertEquals("https://management.azure.com/", azureEnvironment.getResourceManagerEndpoint());
        Assertions.assertEquals("https://management.core.windows.net/", azureEnvironment.getManagementEndpoint());
        Assertions.assertEquals("https://management.core.windows.net/", azureEnvironment.getActiveDirectoryResourceId());
        Assertions.assertEquals("https://login.microsoftonline.com/", azureEnvironment.getActiveDirectoryEndpoint());
        Assertions.assertEquals("https://graph.windows.net/", azureEnvironment.getGraphEndpoint());
        Assertions.assertEquals("https://portal.azure.com", azureEnvironment.getPortal());
        Assertions.assertEquals(".core.windows.net", azureEnvironment.getStorageEndpointSuffix());
        Assertions.assertEquals(".vault.azure.net", azureEnvironment.getKeyVaultDnsSuffix());
        Assertions.assertEquals(".database.windows.net", azureEnvironment.getSqlServerHostnameSuffix());
        Assertions.assertEquals("https://management.core.windows.net:8443/", azureEnvironment.getSqlManagementEndpoint());
        Assertions.assertEquals("https://gallery.azure.com/", azureEnvironment.getGalleryEndpoint());
        Assertions.assertEquals("N/A", azureEnvironment.getMicrosoftGraphEndpoint());
    }

    @Test
    public void testAzureEnvironmentFromStackEndpoint() throws Exception {
        HttpPipeline mockedPipeline = mockResponse("[{\"portal\":\"https://portal.redmond.azurestack.corp.microsoft.com/\",\"authentication\":{\"loginEndpoint\":\"https://adfs.redmond.azurestack.corp.microsoft.com/adfs\",\"audiences\":[\"https://management.adfs.redmond.selfhost.local/d48c9ef1-e46d-4148-b41e-ca95b463f5a8\"]},\"graphAudience\":\"https://graph.redmond.azurestack.corp.microsoft.com/\",\"graph\":\"https://graph.redmond.azurestack.corp.microsoft.com/\",\"name\":\"AzureStack-User-d48c9ef1-e46d-4148-b41e-ca95b463f5a8\",\"suffixes\":{\"keyVaultDns\":\"vault.redmond.azurestack.corp.microsoft.com\",\"storage\":\"redmond.azurestack.corp.microsoft.com\"},\"gallery\":\"https://providers.redmond.selfhost.local:30016/\"}]");

        AzureEnvironment azureEnvironment = AzureEnvironment.fromAzureResourceManagerEndpoint("https://management.redmond.azurestack.corp.microsoft.com/", mockedPipeline);

        Assertions.assertEquals("https://management.redmond.azurestack.corp.microsoft.com/", azureEnvironment.getResourceManagerEndpoint());
        Assertions.assertEquals("https://management.adfs.redmond.selfhost.local/d48c9ef1-e46d-4148-b41e-ca95b463f5a8", azureEnvironment.getManagementEndpoint());
        Assertions.assertEquals("https://management.adfs.redmond.selfhost.local/d48c9ef1-e46d-4148-b41e-ca95b463f5a8", azureEnvironment.getActiveDirectoryResourceId());
        Assertions.assertEquals("https://adfs.redmond.azurestack.corp.microsoft.com/adfs/", azureEnvironment.getActiveDirectoryEndpoint());
        Assertions.assertEquals("https://graph.redmond.azurestack.corp.microsoft.com/", azureEnvironment.getGraphEndpoint());
        Assertions.assertEquals("https://portal.redmond.azurestack.corp.microsoft.com/", azureEnvironment.getPortal());
        Assertions.assertEquals(".redmond.azurestack.corp.microsoft.com", azureEnvironment.getStorageEndpointSuffix());
        Assertions.assertEquals(".vault.redmond.azurestack.corp.microsoft.com", azureEnvironment.getKeyVaultDnsSuffix());
        Assertions.assertEquals("N/A", azureEnvironment.getSqlServerHostnameSuffix());
        Assertions.assertEquals("N/A", azureEnvironment.getSqlManagementEndpoint());
        Assertions.assertEquals("https://providers.redmond.selfhost.local:30016/", azureEnvironment.getGalleryEndpoint());
        Assertions.assertEquals("N/A", azureEnvironment.getMicrosoftGraphEndpoint());
    }

    @Test
    public void testAzureEnvironmentFromArmEndpointNegative() {
        // invalid URL
        Assertions.assertThrows(IllegalArgumentException.class, () -> AzureEnvironment.fromAzureResourceManagerEndpoint("management.azure.com", mockResponse()));

        // null URL
        Assertions.assertThrows(NullPointerException.class, () -> AzureEnvironment.fromAzureResourceManagerEndpoint(null, mockResponse()));

        // empty metadata
        Assertions.assertThrows(HttpResponseException.class, () -> AzureEnvironment.fromAzureResourceManagerEndpoint(AZURE_CLOUD_ENDPOINT, mockResponse("[]")));

        // not sufficient metadata
        Assertions.assertThrows(HttpResponseException.class, () -> AzureEnvironment.fromAzureResourceManagerEndpoint(AZURE_CLOUD_ENDPOINT, mockResponse("[{\"portal\":\"https://portal.azure.com\",\"authentication\":{\"loginEndpoint\":\"https://login.microsoftonline.com/\",\"audiences\":[]}}]")));
    }

    private HttpPipeline mockResponse() {
        String response = "[{\"portal\":\"https://portal.azure.com\",\"authentication\":{\"loginEndpoint\":\"https://login.microsoftonline.com/\",\"audiences\":[\"https://management.core.windows.net/\",\"https://management.azure.com/\"],\"tenant\":\"common\",\"identityProvider\":\"AAD\"},\"media\":\"https://rest.media.azure.net\",\"graphAudience\":\"https://graph.windows.net/\",\"graph\":\"https://graph.windows.net/\",\"name\":\"AzureCloud\",\"suffixes\":{\"azureDataLakeStoreFileSystem\":\"azuredatalakestore.net\",\"acrLoginServer\":\"azurecr.io\",\"sqlServerHostname\":\"database.windows.net\",\"azureDataLakeAnalyticsCatalogAndJob\":\"azuredatalakeanalytics.net\",\"keyVaultDns\":\"vault.azure.net\",\"storage\":\"core.windows.net\",\"azureFrontDoorEndpointSuffix\":\"azurefd.net\"},\"batch\":\"https://batch.core.windows.net/\",\"resourceManager\":\"https://management.azure.com/\",\"vmImageAliasDoc\":\"https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/arm-compute/quickstart-templates/aliases.json\",\"activeDirectoryDataLake\":\"https://datalake.azure.net/\",\"sqlManagement\":\"https://management.core.windows.net:8443/\",\"gallery\":\"https://gallery.azure.com/\"},{\"portal\":\"https://portal.azure.cn\",\"authentication\":{\"loginEndpoint\":\"https://login.chinacloudapi.cn\",\"audiences\":[\"https://management.core.chinacloudapi.cn\",\"https://management.chinacloudapi.cn\"],\"tenant\":\"common\",\"identityProvider\":\"AAD\"},\"media\":\"https://rest.media.chinacloudapi.cn\",\"graphAudience\":\"https://graph.chinacloudapi.cn\",\"graph\":\"https://graph.chinacloudapi.cn\",\"name\":\"AzureChinaCloud\",\"suffixes\":{\"acrLoginServer\":\"azurecr.cn\",\"sqlServerHostname\":\"database.chinacloudapi.cn\",\"keyVaultDns\":\"vault.azure.cn\",\"storage\":\"core.chinacloudapi.cn\",\"azureFrontDoorEndpointSuffix\":\"\"},\"batch\":\"https://batch.chinacloudapi.cn\",\"resourceManager\":\"https://management.chinacloudapi.cn\",\"vmImageAliasDoc\":\"https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/arm-compute/quickstart-templates/aliases.json\",\"sqlManagement\":\"https://management.core.chinacloudapi.cn:8443\",\"gallery\":\"https://gallery.chinacloudapi.cn\"},{\"portal\":\"https://portal.azure.us\",\"authentication\":{\"loginEndpoint\":\"https://login.microsoftonline.us\",\"audiences\":[\"https://management.core.usgovcloudapi.net\",\"https://management.usgovcloudapi.net\"],\"tenant\":\"common\",\"identityProvider\":\"AAD\"},\"media\":\"https://rest.media.usgovcloudapi.net\",\"graphAudience\":\"https://graph.windows.net\",\"graph\":\"https://graph.windows.net\",\"name\":\"AzureUSGovernment\",\"suffixes\":{\"acrLoginServer\":\"azurecr.us\",\"sqlServerHostname\":\"database.usgovcloudapi.net\",\"keyVaultDns\":\"vault.usgovcloudapi.net\",\"storage\":\"core.usgovcloudapi.net\",\"azureFrontDoorEndpointSuffix\":\"\"},\"batch\":\"https://batch.core.usgovcloudapi.net\",\"resourceManager\":\"https://management.usgovcloudapi.net\",\"vmImageAliasDoc\":\"https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/arm-compute/quickstart-templates/aliases.json\",\"sqlManagement\":\"https://management.core.usgovcloudapi.net:8443\",\"gallery\":\"https://gallery.usgovcloudapi.net\"},{\"portal\":\"https://portal.microsoftazure.de\",\"authentication\":{\"loginEndpoint\":\"https://login.microsoftonline.de\",\"audiences\":[\"https://management.core.cloudapi.de\",\"https://management.microsoftazure.de\"],\"tenant\":\"common\",\"identityProvider\":\"AAD\"},\"media\":\"https://rest.media.cloudapi.de\",\"graphAudience\":\"https://graph.cloudapi.de\",\"graph\":\"https://graph.cloudapi.de\",\"name\":\"AzureGermanCloud\",\"suffixes\":{\"sqlServerHostname\":\"database.cloudapi.de\",\"keyVaultDns\":\"vault.microsoftazure.de\",\"storage\":\"core.cloudapi.de\",\"azureFrontDoorEndpointSuffix\":\"\"},\"batch\":\"https://batch.cloudapi.de\",\"resourceManager\":\"https://management.microsoftazure.de\",\"vmImageAliasDoc\":\"https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/arm-compute/quickstart-templates/aliases.json\",\"sqlManagement\":\"https://management.core.cloudapi.de:8443\",\"gallery\":\"https://gallery.cloudapi.de\"}]";
        return mockResponse(response);
    }

    private HttpPipeline mockResponse(String response) {
        HttpClient mockedClient = Mockito.mock(HttpClient.class);
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(mockedClient).build();
        HttpResponse mockedResponse = Mockito.mock(HttpResponse.class);

        Mockito.when(mockedClient.send(Mockito.any(HttpRequest.class), Mockito.any(Context.class))).thenReturn(Mono.just(mockedResponse));
        Mockito.when(mockedResponse.getStatusCode()).thenReturn(200);
        Mockito.when(mockedResponse.getBodyAsString()).thenReturn(Mono.just(response));

        return pipeline;
    }
}
