# Code snippets and samples


## ExternalAuths

- [CreateOrUpdate](#externalauths_createorupdate)
- [Delete](#externalauths_delete)
- [Get](#externalauths_get)
- [ListByParent](#externalauths_listbyparent)
- [Update](#externalauths_update)

## HcpOpenShiftClusters

- [CreateOrUpdate](#hcpopenshiftclusters_createorupdate)
- [Delete](#hcpopenshiftclusters_delete)
- [GetByResourceGroup](#hcpopenshiftclusters_getbyresourcegroup)
- [List](#hcpopenshiftclusters_list)
- [ListByResourceGroup](#hcpopenshiftclusters_listbyresourcegroup)
- [RequestAdminCredential](#hcpopenshiftclusters_requestadmincredential)
- [RevokeCredentials](#hcpopenshiftclusters_revokecredentials)
- [Update](#hcpopenshiftclusters_update)

## HcpOpenShiftVersions

- [Get](#hcpopenshiftversions_get)
- [List](#hcpopenshiftversions_list)

## HcpOperatorIdentityRoleSets

- [Get](#hcpoperatoridentityrolesets_get)
- [List](#hcpoperatoridentityrolesets_list)

## NodePools

- [CreateOrUpdate](#nodepools_createorupdate)
- [Delete](#nodepools_delete)
- [Get](#nodepools_get)
- [ListByParent](#nodepools_listbyparent)
- [Update](#nodepools_update)

## Operations

- [List](#operations_list)
### ExternalAuths_CreateOrUpdate

```java
import com.azure.resourcemanager.redhatopenshifthcp.models.ExternalAuthClaimProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.ExternalAuthClientComponentProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.ExternalAuthClientProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.ExternalAuthClientType;
import com.azure.resourcemanager.redhatopenshifthcp.models.ExternalAuthProperties;
import com.azure.resourcemanager.redhatopenshifthcp.models.GroupClaimProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.TokenClaimMappingsProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.TokenClaimValidationRule;
import com.azure.resourcemanager.redhatopenshifthcp.models.TokenIssuerProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.TokenRequiredClaim;
import com.azure.resourcemanager.redhatopenshifthcp.models.TokenValidationRuleType;
import com.azure.resourcemanager.redhatopenshifthcp.models.UsernameClaimPrefixPolicy;
import com.azure.resourcemanager.redhatopenshifthcp.models.UsernameClaimProfile;
import java.util.Arrays;

/**
 * Samples for ExternalAuths CreateOrUpdate.
 */
public final class ExternalAuthsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/ExternalAuths_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ExternalAuths_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void externalAuthsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.externalAuths()
            .define("my-cool-auth")
            .withExistingHcpOpenShiftCluster("rgopenapi", "hcpCluster-name")
            .withProperties(new ExternalAuthProperties()
                .withIssuer(new TokenIssuerProfile().withUrl("https://microsoft.com/a")
                    .withAudiences(Arrays.asList("audience1", "audience2", "audience3", "audience4", "audience5"))
                    .withCa("lrakpuqodeqscdauefb"))
                .withClients(Arrays.asList(new ExternalAuthClientProfile()
                    .withComponent(new ExternalAuthClientComponentProfile().withName("my-cool-component")
                        .withAuthClientNamespace("my-cool-namespace"))
                    .withClientId("vobxtzobefgl")
                    .withExtraScopes(Arrays.asList("ejmvezdxvoozyiickteiqnvpxqciep"))
                    .withType(ExternalAuthClientType.CONFIDENTIAL)))
                .withClaim(new ExternalAuthClaimProfile().withMappings(new TokenClaimMappingsProfile()
                    .withUsername(new UsernameClaimProfile().withClaim(
                        "utlmketyrdxmwijowjzbuqyawuoqrlriryuknigayeviriulgjvuwvxjrsrhpmvavyyxzapgkfeyedcklnoddeviibefgvubvecffqgdhntammtlwjsjemhsqhafmmorskpuwbtjgkoggxq")
                        .withPrefix("ojmwi")
                        .withPrefixPolicy(UsernameClaimPrefixPolicy.fromString("mdbghfytgejdqobfllqmajtc")))
                    .withGroups(new GroupClaimProfile().withClaim(
                        "icvcoadhpyprqygxyvqhewaycjdtzrwjzbjgmyralburdaolouyvkymfpetymlcwpqsoteryaatoapieizbsnttmkkxsrhyaacnucznujhgmxkmnmgtcjntjsmuabplpoyxberrjdikkkqqiqfnlvwngpbfajzhxzdgqicoconqtrrstzzumdurgfsheypcm")
                        .withPrefix("kjlxhbjdvwarcwdu")))
                    .withValidationRules(
                        Arrays.asList(new TokenClaimValidationRule().withType(TokenValidationRuleType.REQUIRED_CLAIM)
                            .withRequiredClaim(new TokenRequiredClaim().withClaim("ciapdmvrnfitudpx")
                                .withRequiredValue("mqzzjiozgxfgflhdrnwawpke"))))))
            .create();
    }
}
```

### ExternalAuths_Delete

```java
/**
 * Samples for ExternalAuths Delete.
 */
public final class ExternalAuthsDeleteSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/ExternalAuths_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: ExternalAuths_Delete_MaximumSet.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void externalAuthsDeleteMaximumSet(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.externalAuths()
            .delete("rgopenapi", "hcpCluster-name", "my-cool-auth", com.azure.core.util.Context.NONE);
    }
}
```

### ExternalAuths_Get

```java
/**
 * Samples for ExternalAuths Get.
 */
public final class ExternalAuthsGetSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/ExternalAuths_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ExternalAuths_Get_MaximumSet.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void externalAuthsGetMaximumSet(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.externalAuths()
            .getWithResponse("rgopenapi", "hcpCluster-name", "my-cool-auth", com.azure.core.util.Context.NONE);
    }
}
```

### ExternalAuths_ListByParent

```java
/**
 * Samples for ExternalAuths ListByParent.
 */
public final class ExternalAuthsListByParentSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/ExternalAuths_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: ExternalAuths_ListByParent_MaximumSet.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void externalAuthsListByParentMaximumSet(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.externalAuths().listByParent("rgopenapi", "hcpCluster-name", com.azure.core.util.Context.NONE);
    }
}
```

### ExternalAuths_Update

```java
import com.azure.resourcemanager.redhatopenshifthcp.models.ExternalAuth;
import com.azure.resourcemanager.redhatopenshifthcp.models.ExternalAuthClaimProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.ExternalAuthClientComponentProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.ExternalAuthClientProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.ExternalAuthClientType;
import com.azure.resourcemanager.redhatopenshifthcp.models.ExternalAuthProperties;
import com.azure.resourcemanager.redhatopenshifthcp.models.GroupClaimProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.TokenClaimMappingsProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.TokenClaimValidationRule;
import com.azure.resourcemanager.redhatopenshifthcp.models.TokenIssuerProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.TokenRequiredClaim;
import com.azure.resourcemanager.redhatopenshifthcp.models.TokenValidationRuleType;
import com.azure.resourcemanager.redhatopenshifthcp.models.UsernameClaimPrefixPolicy;
import com.azure.resourcemanager.redhatopenshifthcp.models.UsernameClaimProfile;
import java.util.Arrays;

/**
 * Samples for ExternalAuths Update.
 */
public final class ExternalAuthsUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/ExternalAuths_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: ExternalAuths_Update_MaximumSet.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void externalAuthsUpdateMaximumSet(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        ExternalAuth resource = manager.externalAuths()
            .getWithResponse("rgopenapi", "hcpCluster-name", "my-cool-auth", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new ExternalAuthProperties()
                .withIssuer(new TokenIssuerProfile().withUrl("https://microsoft.com/a")
                    .withAudiences(Arrays.asList("audience1", "audience2", "audience3", "audience4", "audience5"))
                    .withCa("rgmklhpshpjkbpjskqxtyfwetjjxr"))
                .withClients(Arrays.asList(new ExternalAuthClientProfile()
                    .withComponent(new ExternalAuthClientComponentProfile().withName("my-cool-component")
                        .withAuthClientNamespace("my-cool-namespace"))
                    .withClientId("vobxtzobefgl")
                    .withExtraScopes(Arrays.asList("ejmvezdxvoozyiickteiqnvpxqciep"))
                    .withType(ExternalAuthClientType.CONFIDENTIAL)))
                .withClaim(new ExternalAuthClaimProfile().withMappings(new TokenClaimMappingsProfile()
                    .withUsername(new UsernameClaimProfile().withClaim(
                        "nmaleeslaspkxdurlxhdntydjdcdqmwizhqpgtywqzzykfvxnouqlewuwqyqlejnddtlmudupjlndnogagnkbnupmpxjsplsfbpoknppcbsjbymnlqmmtukbaiaipzevwugtrgxuxqgwlevtdtabxbcauvuwjqzngklgovnnjwcliigxeedcum")
                        .withPrefix("krxszffgjhffwcszyzttmujlinm")
                        .withPrefixPolicy(UsernameClaimPrefixPolicy.fromString("grjqszciuqlznueyltsmgec")))
                    .withGroups(new GroupClaimProfile().withClaim("yrqawnseinzjlcevwxetagxeqkxoepjoctyrvddrfozociinj")
                        .withPrefix("ajnojzalbh")))
                    .withValidationRules(
                        Arrays.asList(new TokenClaimValidationRule().withType(TokenValidationRuleType.REQUIRED_CLAIM)
                            .withRequiredClaim(new TokenRequiredClaim().withClaim("ciapdmvrnfitudpx")
                                .withRequiredValue("mqzzjiozgxfgflhdrnwawpke"))))))
            .apply();
    }
}
```

### HcpOpenShiftClusters_CreateOrUpdate

```java
import com.azure.resourcemanager.redhatopenshifthcp.models.ApiProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.ClusterAutoscalingProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.ClusterImageRegistryProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.ClusterImageRegistryState;
import com.azure.resourcemanager.redhatopenshifthcp.models.CryptoRestrictions;
import com.azure.resourcemanager.redhatopenshifthcp.models.CustomerManagedEncryptionProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.CustomerManagedEncryptionType;
import com.azure.resourcemanager.redhatopenshifthcp.models.DnsProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.EtcdDataEncryptionKeyManagementModeType;
import com.azure.resourcemanager.redhatopenshifthcp.models.EtcdDataEncryptionProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.EtcdProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.HcpOpenShiftClusterProperties;
import com.azure.resourcemanager.redhatopenshifthcp.models.ImageDigestMirror;
import com.azure.resourcemanager.redhatopenshifthcp.models.IngressProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.IngressType;
import com.azure.resourcemanager.redhatopenshifthcp.models.KeyVaultVisibility;
import com.azure.resourcemanager.redhatopenshifthcp.models.KmsEncryptionProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.KmsKey;
import com.azure.resourcemanager.redhatopenshifthcp.models.ManagedServiceIdentity;
import com.azure.resourcemanager.redhatopenshifthcp.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.redhatopenshifthcp.models.NetworkProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.NetworkType;
import com.azure.resourcemanager.redhatopenshifthcp.models.OperatorsAuthenticationProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.OutboundType;
import com.azure.resourcemanager.redhatopenshifthcp.models.PlatformProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.UserAssignedIdentitiesProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.UserAssignedIdentity;
import com.azure.resourcemanager.redhatopenshifthcp.models.VersionProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.Visibility;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for HcpOpenShiftClusters CreateOrUpdate.
 */
public final class HcpOpenShiftClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/HcpOpenShiftClusters_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: HcpOpenShiftClusters_CreateOrUpdate.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void hcpOpenShiftClustersCreateOrUpdate(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.hcpOpenShiftClusters()
            .define("hcpCluster-name")
            .withRegion("ayecbdqonsqfowbq")
            .withExistingResourceGroup("rgopenapi")
            .withTags(mapOf("key4181", "fakeTokenPlaceholder"))
            .withProperties(new HcpOpenShiftClusterProperties()
                .withVersion(new VersionProfile().withId("4.12").withChannelGroup("stable"))
                .withDns(new DnsProfile().withBaseDomainPrefix("jcldjrtyebhrlxs"))
                .withNetwork(new NetworkProfile().withNetworkType(NetworkType.OVNKUBERNETES)
                    .withPodCIDR("10.128.0.0/14")
                    .withServiceCIDR("172.30.0.0/16")
                    .withMachineCIDR("10.0.0.0/16")
                    .withHostPrefix(21))
                .withApi(new ApiProfile().withVisibility(Visibility.PUBLIC)
                    .withAuthorizedCIDRs(Arrays.asList("192.168.1.0/24", "10.0.0.0/16")))
                .withIngress(new IngressProfile().withType(IngressType.PUBLIC))
                .withPlatform(new PlatformProfile().withManagedResourceGroup("nhyhywrxupo")
                    .withSubnetId(
                        "/subscriptions/FDEA43EA-0230-4A7D-BDEE-F3AFF2183B1D/resourceGroups/resourceGroupName/providers/Microsoft.Network/virtualNetworks/hcp-network-example/subnets/example-subnet")
                    .withVnetIntegrationSubnetId(
                        "/subscriptions/FDEA43EA-0230-4A7D-BDEE-F3AFF2183B1D/resourceGroups/resourceGroupName/providers/Microsoft.Network/virtualNetworks/hcp-network-example/subnets/vnet-integration-subnet")
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withNetworkSecurityGroupId(
                        "/subscriptions/FDEA43EA-0230-4A7D-BDEE-F3AFF2183B1D/resourceGroups/resourceGroupName/providers/Microsoft.Network/networkSecurityGroups/nsg-example")
                    .withOperatorsAuthentication(new OperatorsAuthenticationProfile().withUserAssignedIdentities(
                        new UserAssignedIdentitiesProfile().withControlPlaneOperators(mapOf())
                            .withDataPlaneOperators(mapOf())
                            .withServiceManagedIdentity(
                                "/subscriptions/FDEA43EA-0230-4A7D-BDEE-F3AFF2183B1D/resourceGroups/rgopenapi/providers/Microsoft.ManagedIdentity/userAssignedIdentities/serviceMI"))))
                .withAutoscaling(new ClusterAutoscalingProfile().withMaxNodesTotal(0)
                    .withMaxPodGracePeriodSeconds(0)
                    .withMaxNodeProvisionTimeSeconds(0)
                    .withPodPriorityThreshold(1))
                .withEtcd(
                    new EtcdProfile()
                        .withDataEncryption(new EtcdDataEncryptionProfile()
                            .withKeyManagementMode(EtcdDataEncryptionKeyManagementModeType.CUSTOMER_MANAGED)
                            .withCustomerManaged(new CustomerManagedEncryptionProfile()
                                .withEncryptionType(CustomerManagedEncryptionType.KMS)
                                .withKms(new KmsEncryptionProfile().withVaultName("my-cool-vault")
                                    .withVisibility(KeyVaultVisibility.PUBLIC)
                                    .withActiveKey(new KmsKey().withName("my-cool-key")
                                        .withVersion("8e73e7d1fd7d4a87b730f676fc77d3a6"))))))
                .withImageDigestMirrors(Arrays.asList(
                    new ImageDigestMirror().withSource("registry.example.com/image1")
                        .withMirrors(Arrays.asList("mirror1.example.com/image1", "mirror2.example.com/image1")),
                    new ImageDigestMirror().withSource("registry.example.com/image2")
                        .withMirrors(Arrays.asList("mirror1.example.com/image2"))))
                .withNodeDrainTimeoutMinutes(20)
                .withClusterImageRegistry(
                    new ClusterImageRegistryProfile().withState(ClusterImageRegistryState.ENABLED))
                .withCryptoRestrictions(CryptoRestrictions.NONE))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/FDEA43EA-0230-4A7D-BDEE-F3AFF2183B1D/resourceGroups/rgopenapi/providers/Microsoft.ManagedIdentity/userAssignedIdentities/serviceMI",
                    new UserAssignedIdentity())))
            .create();
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### HcpOpenShiftClusters_Delete

```java
/**
 * Samples for HcpOpenShiftClusters Delete.
 */
public final class HcpOpenShiftClustersDeleteSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/HcpOpenShiftClusters_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: HcpOpenShiftClusters_Delete.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void hcpOpenShiftClustersDelete(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.hcpOpenShiftClusters().delete("rgopenapi", "hcpCluster-name", com.azure.core.util.Context.NONE);
    }
}
```

### HcpOpenShiftClusters_GetByResourceGroup

```java
/**
 * Samples for HcpOpenShiftClusters GetByResourceGroup.
 */
public final class HcpOpenShiftClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/HcpOpenShiftClusters_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: HcpOpenShiftClusters_Get.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void hcpOpenShiftClustersGet(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.hcpOpenShiftClusters()
            .getByResourceGroupWithResponse("rgopenapi", "my-cool-cluster", com.azure.core.util.Context.NONE);
    }
}
```

### HcpOpenShiftClusters_List

```java
/**
 * Samples for HcpOpenShiftClusters List.
 */
public final class HcpOpenShiftClustersListSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/HcpOpenShiftClusters_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: HcpOpenShiftClusters_ListBySubscription.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void hcpOpenShiftClustersListBySubscription(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.hcpOpenShiftClusters().list(com.azure.core.util.Context.NONE);
    }
}
```

### HcpOpenShiftClusters_ListByResourceGroup

```java
/**
 * Samples for HcpOpenShiftClusters ListByResourceGroup.
 */
public final class HcpOpenShiftClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/HcpOpenShiftClusters_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: HcpOpenShiftClusters_ListByResourceGroup.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void hcpOpenShiftClustersListByResourceGroup(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.hcpOpenShiftClusters().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }
}
```

### HcpOpenShiftClusters_RequestAdminCredential

```java
import com.azure.resourcemanager.redhatopenshifthcp.models.HcpOpenShiftClusterAdminCredentialRequest;

/**
 * Samples for HcpOpenShiftClusters RequestAdminCredential.
 */
public final class HcpOpenShiftClustersRequestAdminCredentialSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/HcpOpenShiftClusters_RequestAdminCredential_MaximumSet_Gen.json
     */
    /**
     * Sample code: HcpOpenShiftClusters_RequestAdminCredential_MaximumSet.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void hcpOpenShiftClustersRequestAdminCredentialMaximumSet(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.hcpOpenShiftClusters()
            .requestAdminCredential("rgopenapi", "hcpCluster-name",
                new HcpOpenShiftClusterAdminCredentialRequest().withCertificateSigningRequest(
                    "-----BEGIN CERTIFICATE REQUEST-----\nMIIBhTCB7wIBADBFMQswCQYDVQQGEwJVUzELMAkGA1UECAwCQ0ExDjAMBgNVBAoM\nBVRlc3QxGTAXBgNVBAMMEHRlc3QuZXhhbXBsZS5jb20wdjAQBgcqhkjOPQIBBgUr\ngQQAIgNiAARIm+7hphQ7m8kzCB5keJ3lPVQvsEH6ABXz0kIvxkNF7+OBFCdPJIBT\nksaGJnJFfPUROYGJIo7FMOO/vEqE9gHqRCVao0RPDaZLtceCYqbeI0vFhW7qTmYL\nNp/RTer7C0+gITAfBgkqhkiG9w0BCQ4xEjAQMA4GA1UdEQQHMAWCA2FiYzAKBggq\nhkjOPQQDAgNoADBlAjBLQDR3K8k1XPFH3Y0oEFYrBi3L4FOX0kz0aK/JuFJN/kBP\nA2ViVNHl+5iVxvpJE5sCMQCF+nPr18qRaib09BHSBKl+ZVpXC1K3PN/VGjYv+Zjl\nK8eCiPwwRBpRMbqMSXxlS3Q=\n-----END CERTIFICATE REQUEST-----\n"),
                com.azure.core.util.Context.NONE);
    }
}
```

### HcpOpenShiftClusters_RevokeCredentials

```java
/**
 * Samples for HcpOpenShiftClusters RevokeCredentials.
 */
public final class HcpOpenShiftClustersRevokeCredentialsSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/HcpOpenShiftClusters_RevokeCredentials_MaximumSet_Gen.json
     */
    /**
     * Sample code: HcpOpenShiftClusters_RevokeCredentials_MaximumSet.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void hcpOpenShiftClustersRevokeCredentialsMaximumSet(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.hcpOpenShiftClusters()
            .revokeCredentials("rgopenapi", "hcpCluster-name", com.azure.core.util.Context.NONE);
    }
}
```

### HcpOpenShiftClusters_Update

```java
import com.azure.resourcemanager.redhatopenshifthcp.models.HcpOpenShiftCluster;
import com.azure.resourcemanager.redhatopenshifthcp.models.ManagedServiceIdentity;
import com.azure.resourcemanager.redhatopenshifthcp.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.redhatopenshifthcp.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for HcpOpenShiftClusters Update.
 */
public final class HcpOpenShiftClustersUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/HcpOpenShiftClusters_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: HcpOpenShiftClusters_Update.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void hcpOpenShiftClustersUpdate(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        HcpOpenShiftCluster resource = manager.hcpOpenShiftClusters()
            .getByResourceGroupWithResponse("rgopenapi", "hcpCluster-name", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key4965", "fakeTokenPlaceholder"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/FDEA43EA-0230-4A7D-BDEE-F3AFF2183B1D/resourceGroups/rgopenapi/providers/Microsoft.ManagedIdentity/userAssignedIdentities/serviceMI",
                    new UserAssignedIdentity())))
            .apply();
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### HcpOpenShiftVersions_Get

```java
/**
 * Samples for HcpOpenShiftVersions Get.
 */
public final class HcpOpenShiftVersionsGetSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/HcpOpenShiftVersions_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: HcpOpenShiftVersions_Get_MaximumSet - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void hcpOpenShiftVersionsGetMaximumSetGeneratedByMaximumSetRule(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.hcpOpenShiftVersions().getWithResponse("uksouth", "4.18.1", com.azure.core.util.Context.NONE);
    }
}
```

### HcpOpenShiftVersions_List

```java
/**
 * Samples for HcpOpenShiftVersions List.
 */
public final class HcpOpenShiftVersionsListSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/HcpOpenShiftVersions_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: HcpOpenShiftVersions_List_MaximumSet - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void hcpOpenShiftVersionsListMaximumSetGeneratedByMaximumSetRule(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.hcpOpenShiftVersions().list("uksouth", com.azure.core.util.Context.NONE);
    }
}
```

### HcpOperatorIdentityRoleSets_Get

```java
/**
 * Samples for HcpOperatorIdentityRoleSets Get.
 */
public final class HcpOperatorIdentityRoleSetsGetSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/HcpOperatorIdentityRoleSets_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: HcpOperatorIdentityRoleSets_Get_MaximumSet.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void hcpOperatorIdentityRoleSetsGetMaximumSet(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.hcpOperatorIdentityRoleSets()
            .getWithResponse("uksouth", "hcp-example-role-set", com.azure.core.util.Context.NONE);
    }
}
```

### HcpOperatorIdentityRoleSets_List

```java
/**
 * Samples for HcpOperatorIdentityRoleSets List.
 */
public final class HcpOperatorIdentityRoleSetsListSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/HcpOperatorIdentityRoleSets_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: HcpOperatorIdentityRoleSets_List_MaximumSet.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void hcpOperatorIdentityRoleSetsListMaximumSet(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.hcpOperatorIdentityRoleSets().list("uksouth", com.azure.core.util.Context.NONE);
    }
}
```

### NodePools_CreateOrUpdate

```java
import com.azure.resourcemanager.redhatopenshifthcp.models.DiskStorageAccountType;
import com.azure.resourcemanager.redhatopenshifthcp.models.Effect;
import com.azure.resourcemanager.redhatopenshifthcp.models.Label;
import com.azure.resourcemanager.redhatopenshifthcp.models.NodePoolAutoScaling;
import com.azure.resourcemanager.redhatopenshifthcp.models.NodePoolPlatformProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.NodePoolProperties;
import com.azure.resourcemanager.redhatopenshifthcp.models.NodePoolVersionProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.OsDiskProfile;
import com.azure.resourcemanager.redhatopenshifthcp.models.OsDiskType;
import com.azure.resourcemanager.redhatopenshifthcp.models.Taint;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NodePools CreateOrUpdate.
 */
public final class NodePoolsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/NodePools_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: NodePools_CreateOrUpdate.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void nodePoolsCreateOrUpdate(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.nodePools()
            .define("nodePool-name")
            .withRegion("mqewzbuvnyxnwbmir")
            .withExistingHcpOpenShiftCluster("rgopenapi", "hcpCluster-name")
            .withTags(mapOf("key", "fakeTokenPlaceholder"))
            .withProperties(new NodePoolProperties()
                .withVersion(new NodePoolVersionProfile().withId("4.12").withChannelGroup("stable"))
                .withPlatform(new NodePoolPlatformProfile().withSubnetId(
                    "/subscriptions/FDEA43EA-0230-4A7D-BDEE-F3AFF2183B1D/resourceGroups/resourceGroupName/providers/Microsoft.Network/virtualNetworks/hcp-network-example/subnets/example-subnet")
                    .withVmSize("Standard_D2s_v3")
                    .withEnableEncryptionAtHost(true)
                    .withOsDisk(new OsDiskProfile().withSizeGiB(64)
                        .withDiskStorageAccountType(DiskStorageAccountType.PREMIUM_LRS)
                        .withEncryptionSetId(
                            "/subscriptions/FDEA43EA-0230-4A7D-BDEE-F3AFF2183B1D/resourceGroups/resourceGroupName/providers/Microsoft.Compute/diskEncryptionSets/hcp-disk-encryption-set-example")
                        .withDiskType(OsDiskType.MANAGED))
                    .withAvailabilityZone("australiaeast-az1"))
                .withReplicas(18)
                .withAutoRepair(true)
                .withAutoScaling(new NodePoolAutoScaling().withMin(6).withMax(29))
                .withLabels(Arrays.asList(new Label().withKey("fakeTokenPlaceholder").withValue("4.12")))
                .withTaints(Arrays
                    .asList(new Taint().withKey("fakeTokenPlaceholder").withValue("x").withEffect(Effect.NO_SCHEDULE)))
                .withNodeDrainTimeoutMinutes(20))
            .create();
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### NodePools_Delete

```java
/**
 * Samples for NodePools Delete.
 */
public final class NodePoolsDeleteSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/NodePools_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: NodePools_Delete.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void nodePoolsDelete(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.nodePools().delete("rgopenapi", "hcpCluster-name", "nodePool-name", com.azure.core.util.Context.NONE);
    }
}
```

### NodePools_Get

```java
/**
 * Samples for NodePools Get.
 */
public final class NodePoolsGetSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/NodePools_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: NodePools_Get.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void
        nodePoolsGet(com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.nodePools()
            .getWithResponse("rgopenapi", "hcpCluster-name", "nodepool-name", com.azure.core.util.Context.NONE);
    }
}
```

### NodePools_ListByParent

```java
/**
 * Samples for NodePools ListByParent.
 */
public final class NodePoolsListByParentSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/NodePools_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: NodePools_ListByParent.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void nodePoolsListByParent(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.nodePools().listByParent("rgopenapi", "hcpCluster-name", com.azure.core.util.Context.NONE);
    }
}
```

### NodePools_Update

```java
import com.azure.resourcemanager.redhatopenshifthcp.models.Effect;
import com.azure.resourcemanager.redhatopenshifthcp.models.Label;
import com.azure.resourcemanager.redhatopenshifthcp.models.ManagedServiceIdentity;
import com.azure.resourcemanager.redhatopenshifthcp.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.redhatopenshifthcp.models.NodePool;
import com.azure.resourcemanager.redhatopenshifthcp.models.NodePoolAutoScaling;
import com.azure.resourcemanager.redhatopenshifthcp.models.NodePoolProperties;
import com.azure.resourcemanager.redhatopenshifthcp.models.Taint;
import com.azure.resourcemanager.redhatopenshifthcp.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NodePools Update.
 */
public final class NodePoolsUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/NodePools_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: NodePools_Update.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void nodePoolsUpdate(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        NodePool resource = manager.nodePools()
            .getWithResponse("rgopenapi", "hcpCluster-name", "nodePool-name", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key", "fakeTokenPlaceholder"))
            .withProperties(new NodePoolProperties().withReplicas(7)
                .withAutoScaling(new NodePoolAutoScaling().withMin(29).withMax(2))
                .withLabels(Arrays.asList(new Label().withKey("fakeTokenPlaceholder").withValue("4.12")))
                .withTaints(Arrays
                    .asList(new Taint().withKey("fakeTokenPlaceholder").withValue("x").withEffect(Effect.NO_SCHEDULE)))
                .withNodeDrainTimeoutMinutes(20))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE)
                .withUserAssignedIdentities(mapOf("key4794", new UserAssignedIdentity())))
            .apply();
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_Maximum.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void operationsListMaximum(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-09-01-preview/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_Minimum.
     * 
     * @param manager Entry point to RedHatOpenShiftHostedControlPlanesManager.
     */
    public static void operationsListMinimum(
        com.azure.resourcemanager.redhatopenshifthcp.RedHatOpenShiftHostedControlPlanesManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

