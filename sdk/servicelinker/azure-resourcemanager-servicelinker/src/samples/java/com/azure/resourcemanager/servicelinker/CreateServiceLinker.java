// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicelinker;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appplatform.models.SkuName;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.RuntimeStack;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.servicelinker.models.AzureResource;
import com.azure.resourcemanager.servicelinker.models.ClientType;
import com.azure.resourcemanager.servicelinker.models.LinkerResource;
import com.azure.resourcemanager.servicelinker.models.SecretAuthInfo;
import com.azure.resourcemanager.servicelinker.models.SourceConfiguration;
import com.azure.resourcemanager.servicelinker.models.UserAssignedIdentityAuthInfo;
import com.azure.resourcemanager.servicelinker.models.ValueSecretInfo;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlServer;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.UUID;

public class CreateServiceLinker {
    private static final String USER_TOKEN_HEADER = "x-ms-serviceconnector-user-token";

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) throws Exception {
        // Authentication environment variables need to be set: AZURE_CLIENT_ID,AZURE_TENANT_ID,AZURE_CLIENT_SECRET,AZURE_SUBSCRIPTION_ID
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
            .build();

        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        // create a policy for setting user token into a specific header for some special scenarios
        HttpPipelinePolicy userTokenPolicy = new UserTokenPolicy(credential, profile.getEnvironment());

        // build resource manager to create azure resource
        AzureResourceManager azureResourceManager = AzureResourceManager.authenticate(credential, profile).withDefaultSubscription();
        ServiceLinkerManager serviceLinkerManager = ServiceLinkerManager.authenticate(credential, profile);
        ServiceLinkerManager serviceLinkerManagerWithUserToken = ServiceLinkerManager.configure().withPolicy(userTokenPolicy).authenticate(credential, profile);

        createSpringCloudAndSQLConnection(azureResourceManager, serviceLinkerManager);

        // for KeyVault/SecretStore/VirtualNetwork scenario, it needs user token in header for creating connection
        createWebAppAndKeyVaultConnectionWithUserIdentity(azureResourceManager, serviceLinkerManagerWithUserToken);
    }

    private static void createSpringCloudAndSQLConnection(AzureResourceManager azureResourceManager, ServiceLinkerManager serviceLinkerManager) {
        String resourceGroupName = "rg" + randomString(8);
        Region region = Region.US_EAST;
        String springServiceName = "spring" + randomString(8);
        String springAppName = "app" + randomString(8);
        String sqlServerName = "sqlserver" + randomString(8);
        String sqlDatabaseName = "sqldb" + randomString(8);
        String sqlUserName = "sql" + randomString(8);
        String sqlPassword = "5$Ql" + randomString(8);

        SpringService springService = azureResourceManager.springServices().define(springServiceName)
            .withRegion(region)
            .withNewResourceGroup(resourceGroupName)
            .withSku(SkuName.B0)
            .create();

        SpringApp springApp = springService.apps().define(springAppName)
            .withDefaultActiveDeployment()
            .create();

        SqlServer sqlServer = azureResourceManager.sqlServers().define(sqlServerName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroupName)
            .withAdministratorLogin(sqlUserName)
            .withAdministratorPassword(sqlPassword)
            .create();

        SqlDatabase sqlDatabase = sqlServer.databases().define(sqlDatabaseName)
            .withBasicEdition()
            .create();

        LinkerResource linker = serviceLinkerManager.linkers().define("sql")
            .withExistingResourceUri(springApp.getActiveDeployment().id())
            .withTargetService(
                new AzureResource()
                    .withId(sqlDatabase.id())
            )
            .withAuthInfo(
                new SecretAuthInfo()
                    .withName(sqlUserName)
                    .withSecretInfo(
                        new ValueSecretInfo()
                            .withValue(sqlPassword)
                    )
            )
            .withClientType(ClientType.SPRING_BOOT)
            .create();

        System.out.println("Configurations:");
        for (SourceConfiguration sourceConfiguration : linker.listConfigurations().configurations()) {
            System.out.printf("\t%s: %s%n", sourceConfiguration.name(), sourceConfiguration.value());
        }
    }

    private static void createWebAppAndKeyVaultConnectionWithUserIdentity(AzureResourceManager azureResourceManager, ServiceLinkerManager serviceLinkerManager) {
        String resourceGroupName = "rg" + randomString(8);
        Region region = Region.US_EAST;
        String webAppName = "web" + randomString(8);
        String keyVaultName = "vault" + randomString(8);
        String identityName = "identity" + randomString(8);

        WebApp webApp = azureResourceManager.webApps().define(webAppName)
            .withRegion(region)
            .withNewResourceGroup(resourceGroupName)
            .withNewLinuxPlan(PricingTier.BASIC_B1)
            .withBuiltInImage(RuntimeStack.NODEJS_14_LTS)
            .create();

        Vault vault = azureResourceManager.vaults().define(keyVaultName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroupName)
            .withEmptyAccessPolicy()
            .create();

        Identity identity = azureResourceManager.identities().define(identityName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroupName)
            .create();

        LinkerResource linker = serviceLinkerManager.linkers().define("keyvault")
            .withExistingResourceUri(webApp.id())
            .withTargetService(
                new AzureResource()
                    .withId(vault.id())
            )
            .withAuthInfo(
                new UserAssignedIdentityAuthInfo()
                    .withSubscriptionId(azureResourceManager.subscriptionId())
                    .withClientId(identity.clientId())
            )
            .withClientType(ClientType.NODEJS)
            .create();

        System.out.println("Configurations:");
        for (SourceConfiguration sourceConfiguration : linker.listConfigurations().configurations()) {
            System.out.printf("\t%s: %s%n", sourceConfiguration.name(), sourceConfiguration.value());
        }
    }

    private static String randomString(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, length);
    }

    public static class UserTokenPolicy implements HttpPipelinePolicy {
        private final TokenCredential credential;
        private final AzureEnvironment environment;

        public UserTokenPolicy(TokenCredential credential, AzureEnvironment environment) {
            this.credential = credential;
            this.environment = environment;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            Mono<String> token = null;
            String bearerTokenPrefix = "bearer ";
            String authorization = context.getHttpRequest().getHeaders().getValue("Authorization");
            if (authorization != null && authorization.toLowerCase(Locale.ROOT).startsWith(bearerTokenPrefix)) {
                token = Mono.just(authorization.substring(bearerTokenPrefix.length()));
            } else {
                token = credential
                    .getToken(new TokenRequestContext().addScopes(environment.getResourceManagerEndpoint() + "/.default"))
                    .map(AccessToken::getToken);
            }

            return token
                .flatMap(accessToken -> {
                    context.getHttpRequest().getHeaders().set(USER_TOKEN_HEADER, accessToken);
                    return next.process();
                });
        }
    }
}
