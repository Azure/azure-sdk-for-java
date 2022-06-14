package com.azure.resourcemanager.mediaservices;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.mediaservices.models.AccountEncryption;
import com.azure.resourcemanager.mediaservices.models.AccountEncryptionKeyType;
import com.azure.resourcemanager.mediaservices.models.KeyVaultProperties;
import com.azure.resourcemanager.mediaservices.models.MediaService;
import com.azure.resourcemanager.mediaservices.models.ResourceIdentity;
import com.azure.resourcemanager.mediaservices.models.StorageAccount;
import com.azure.resourcemanager.mediaservices.models.StorageAccountType;
import com.azure.resourcemanager.mediaservices.models.StorageAuthentication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;

public class ClientTests {

    @Test
    public void testMediaservicesClient() {
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        ArgumentCaptor<HttpRequest> httpRequest = ArgumentCaptor.forClass(HttpRequest.class);

        String response = "{\n" +
            "        \"name\": \"contososports\",\n" +
            "        \"id\": \"/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso/providers/Microsoft.Media/mediaservices/contososports\",\n" +
            "        \"type\": \"Microsoft.Media/mediaservices\",\n" +
            "        \"location\": \"South Central US\",\n" +
            "        \"tags\": {\n" +
            "          \"key1\": \"value1\",\n" +
            "          \"key2\": \"value2\"\n" +
            "        },\n" +
            "        \"properties\": {\n" +
            "          \"mediaServiceId\": \"42bea25f-1aa9-4f7a-82af-5a417592f22d\",\n" +
            "          \"storageAccounts\": [\n" +
            "            {\n" +
            "              \"id\": \"/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso/providers/Microsoft.Storage/storageAccounts/contososportsstore\",\n" +
            "              \"type\": \"Primary\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    }";

        Mockito.when(httpResponse.getStatusCode())
            .thenReturn(200);
        Mockito.when(httpResponse.getHeaders())
            .thenReturn(new HttpHeaders());
        Mockito.when(httpResponse.getBodyAsByteArray())
            .thenReturn(Mono.just(response.getBytes(StandardCharsets.UTF_8)));

        Mockito.when(httpClient.send(httpRequest.capture(), Mockito.any()))
            .thenReturn(Mono.defer(() -> {
                Mockito.when(httpResponse.getRequest())
                    .thenReturn(httpRequest.getValue());
                return Mono.just(httpResponse);
            }));

        MediaServicesManager manager = MediaServicesManager.configure()
            .withHttpClient(httpClient)
            .authenticate(
                tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureEnvironment.AZURE));

        MediaService mediaservice = manager
            .mediaservices()
            .define("contososports")
            .withRegion("South Central US")
            .withExistingResourceGroup("contoso")
            .withStorageAccounts(
                Arrays
                    .asList(
                        new StorageAccount()
                            .withId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso/providers/Microsoft.Storage/storageAccounts/contososportsstore")
                            .withType(StorageAccountType.PRIMARY)
                            .withIdentity(
                                new ResourceIdentity()
                                    .withUserAssignedIdentity(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1")
                                    .withUseSystemAssignedIdentity(false))))
            .withStorageAuthentication(StorageAuthentication.MANAGED_IDENTITY)
            .withEncryption(
                new AccountEncryption()
                    .withType(AccountEncryptionKeyType.CUSTOMER_KEY)
                    .withKeyVaultProperties(
                        new KeyVaultProperties().withKeyIdentifier("https://keyvault.vault.azure.net/keys/key1"))
                    .withIdentity(
                        new ResourceIdentity()
                            .withUserAssignedIdentity(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1")
                            .withUseSystemAssignedIdentity(false)))
            .create();

        Assertions.assertNotNull(mediaservice.id());
    }
}
