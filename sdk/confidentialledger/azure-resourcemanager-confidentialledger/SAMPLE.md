# Code snippets and samples


## Ledger

- [Backup](#ledger_backup)
- [Create](#ledger_create)
- [Delete](#ledger_delete)
- [GetByResourceGroup](#ledger_getbyresourcegroup)
- [List](#ledger_list)
- [ListByResourceGroup](#ledger_listbyresourcegroup)
- [Restore](#ledger_restore)
- [Update](#ledger_update)

## ManagedCcf

- [Backup](#managedccf_backup)
- [Create](#managedccf_create)
- [Delete](#managedccf_delete)
- [GetByResourceGroup](#managedccf_getbyresourcegroup)
- [List](#managedccf_list)
- [ListByResourceGroup](#managedccf_listbyresourcegroup)
- [Restore](#managedccf_restore)
- [Update](#managedccf_update)

## Operations

- [List](#operations_list)

## ResourceProvider

- [CheckNameAvailability](#resourceprovider_checknameavailability)
### Ledger_Backup

```java
import com.azure.resourcemanager.confidentialledger.models.ConfidentialLedgerBackup;

/**
 * Samples for Ledger Backup.
 */
public final class LedgerBackupSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2023-06-28-preview/examples/ConfidentialLedger_Backup.json
     */
    /**
     * Sample code: ConfidentialLedgerBackup.
     * 
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void
        confidentialLedgerBackup(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.ledgers()
            .backup("DummyResourceGroupName", "DummyLedgerName",
                new ConfidentialLedgerBackup().withRestoreRegion("EastUS").withUri("DummySASUri"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Ledger_Create

```java
import com.azure.resourcemanager.confidentialledger.models.AadBasedSecurityPrincipal;
import com.azure.resourcemanager.confidentialledger.models.CertBasedSecurityPrincipal;
import com.azure.resourcemanager.confidentialledger.models.LedgerProperties;
import com.azure.resourcemanager.confidentialledger.models.LedgerRoleName;
import com.azure.resourcemanager.confidentialledger.models.LedgerSku;
import com.azure.resourcemanager.confidentialledger.models.LedgerType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Ledger Create.
 */
public final class LedgerCreateSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2023-06-28-preview/examples/ConfidentialLedger_Create.json
     */
    /**
     * Sample code: ConfidentialLedgerCreate.
     * 
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void
        confidentialLedgerCreate(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.ledgers()
            .define("DummyLedgerName")
            .withRegion("EastUS")
            .withExistingResourceGroup("DummyResourceGroupName")
            .withTags(mapOf("additionalProps1", "additional properties"))
            .withProperties(new LedgerProperties().withLedgerType(LedgerType.PUBLIC)
                .withLedgerSku(LedgerSku.STANDARD)
                .withAadBasedSecurityPrincipals(Arrays
                    .asList(new AadBasedSecurityPrincipal().withPrincipalId("34621747-6fc8-4771-a2eb-72f31c461f2e")
                        .withTenantId("bce123b9-2b7b-4975-8360-5ca0b9b1cd08")
                        .withLedgerRoleName(LedgerRoleName.ADMINISTRATOR)))
                .withCertBasedSecurityPrincipals(Arrays.asList(new CertBasedSecurityPrincipal().withCert(
                    "-----BEGIN CERTIFICATE-----MIIBsjCCATigAwIBAgIUZWIbyG79TniQLd2UxJuU74tqrKcwCgYIKoZIzj0EAwMwEDEOMAwGA1UEAwwFdXNlcjAwHhcNMjEwMzE2MTgwNjExWhcNMjIwMzE2MTgwNjExWjAQMQ4wDAYDVQQDDAV1c2VyMDB2MBAGByqGSM49AgEGBSuBBAAiA2IABBiWSo/j8EFit7aUMm5lF+lUmCu+IgfnpFD+7QMgLKtxRJ3aGSqgS/GpqcYVGddnODtSarNE/HyGKUFUolLPQ5ybHcouUk0kyfA7XMeSoUA4lBz63Wha8wmXo+NdBRo39qNTMFEwHQYDVR0OBBYEFPtuhrwgGjDFHeUUT4nGsXaZn69KMB8GA1UdIwQYMBaAFPtuhrwgGjDFHeUUT4nGsXaZn69KMA8GA1UdEwEB/wQFMAMBAf8wCgYIKoZIzj0EAwMDaAAwZQIxAOnozm2CyqRwSSQLls5r+mUHRGRyXHXwYtM4Dcst/VEZdmS9fqvHRCHbjUlO/+HNfgIwMWZ4FmsjD3wnPxONOm9YdVn/PRD7SsPRPbOjwBiE4EBGaHDsLjYAGDSGi7NJnSkA-----END CERTIFICATE-----")
                    .withLedgerRoleName(LedgerRoleName.READER))))
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

### Ledger_Delete

```java
/**
 * Samples for Ledger Delete.
 */
public final class LedgerDeleteSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2023-06-28-preview/examples/ConfidentialLedger_Delete.json
     */
    /**
     * Sample code: ConfidentialLedgerDelete.
     * 
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void
        confidentialLedgerDelete(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.ledgers().delete("DummyResourceGroupName", "DummyLedgerName", com.azure.core.util.Context.NONE);
    }
}
```

### Ledger_GetByResourceGroup

```java
/**
 * Samples for Ledger GetByResourceGroup.
 */
public final class LedgerGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2023-06-28-preview/examples/ConfidentialLedger_Get.json
     */
    /**
     * Sample code: ConfidentialLedgerGet.
     * 
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void
        confidentialLedgerGet(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.ledgers()
            .getByResourceGroupWithResponse("DummyResourceGroupName", "DummyLedgerName",
                com.azure.core.util.Context.NONE);
    }
}
```

### Ledger_List

```java
/**
 * Samples for Ledger List.
 */
public final class LedgerListSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2023-06-28-preview/examples/ConfidentialLedger_ListBySub.json
     */
    /**
     * Sample code: ConfidentialLedgerListBySub.
     * 
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void
        confidentialLedgerListBySub(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.ledgers().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### Ledger_ListByResourceGroup

```java
/**
 * Samples for Ledger ListByResourceGroup.
 */
public final class LedgerListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2023-06-28-preview/examples/ConfidentialLedger_List.json
     */
    /**
     * Sample code: ConfidentialLedgerList.
     * 
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void
        confidentialLedgerList(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.ledgers().listByResourceGroup("DummyResourceGroupName", null, com.azure.core.util.Context.NONE);
    }
}
```

### Ledger_Restore

```java
import com.azure.resourcemanager.confidentialledger.models.ConfidentialLedgerRestore;

/**
 * Samples for Ledger Restore.
 */
public final class LedgerRestoreSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2023-06-28-preview/examples/ConfidentialLedger_Restore.json
     */
    /**
     * Sample code: ConfidentialLedgerRestore.
     * 
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void
        confidentialLedgerRestore(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.ledgers()
            .restore("DummyResourceGroupName", "DummyLedgerName",
                new ConfidentialLedgerRestore().withFileShareName("DummyFileShareName")
                    .withRestoreRegion("EastUS")
                    .withUri("DummySASUri"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Ledger_Update

```java
import com.azure.resourcemanager.confidentialledger.models.AadBasedSecurityPrincipal;
import com.azure.resourcemanager.confidentialledger.models.CertBasedSecurityPrincipal;
import com.azure.resourcemanager.confidentialledger.models.ConfidentialLedger;
import com.azure.resourcemanager.confidentialledger.models.LedgerProperties;
import com.azure.resourcemanager.confidentialledger.models.LedgerRoleName;
import com.azure.resourcemanager.confidentialledger.models.LedgerType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Ledger Update.
 */
public final class LedgerUpdateSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2023-06-28-preview/examples/ConfidentialLedger_Update.json
     */
    /**
     * Sample code: ConfidentialLedgerUpdate.
     * 
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void
        confidentialLedgerUpdate(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        ConfidentialLedger resource = manager.ledgers()
            .getByResourceGroupWithResponse("DummyResourceGroupName", "DummyLedgerName",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("additionProps2", "additional property value", "additionalProps1", "additional properties"))
            .withProperties(new LedgerProperties().withLedgerType(LedgerType.PUBLIC)
                .withAadBasedSecurityPrincipals(Arrays
                    .asList(new AadBasedSecurityPrincipal().withPrincipalId("34621747-6fc8-4771-a2eb-72f31c461f2e")
                        .withTenantId("bce123b9-2b7b-4975-8360-5ca0b9b1cd08")
                        .withLedgerRoleName(LedgerRoleName.ADMINISTRATOR)))
                .withCertBasedSecurityPrincipals(Arrays.asList(new CertBasedSecurityPrincipal().withCert(
                    "-----BEGIN CERTIFICATE-----\nMIIDUjCCAjqgAwIBAgIQJ2IrDBawSkiAbkBYmiAopDANBgkqhkiG9w0BAQsFADAmMSQwIgYDVQQDExtTeW50aGV0aWNzIExlZGdlciBVc2VyIENlcnQwHhcNMjAwOTIzMjIxODQ2WhcNMjEwOTIzMjIyODQ2WjAmMSQwIgYDVQQDExtTeW50aGV0aWNzIExlZGdlciBVc2VyIENlcnQwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCX2s/Eu4q/eQ63N+Ugeg5oAciZua/YCJr41c/696szvSY7Zg1SNJlW88/nbz70+QpO55OmqlEE3QCU+T0Vl/h0Gf//n1PYcoBbTGUnYEmV+fTTHict6rFiEwrGJ62tvcpYgwapInSLyEeUzjki0zhOLJ1OfRnYd1eGnFVMpE5aVjiS8Q5dmTEUyd51EIprGE8RYAW9aeWSwTH7gjHUsRlJnHKcdhaK/v5QKJnNu5bzPFUcpC0ZBcizoMPAtroLAD4B68Jl0z3op18MgZe6lRrVoWuxfqnk5GojuB/Vu8ohAZKoFhQ6NB6r+LL2AUs+Zr7Bt26IkEdR178n9JMEA4gHAgMBAAGjfDB6MA4GA1UdDwEB/wQEAwIFoDAJBgNVHRMEAjAAMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjAfBgNVHSMEGDAWgBS/a7PU9iOfOKEyZCp11Oen5VSuuDAdBgNVHQ4EFgQUv2uz1PYjnzihMmQqddTnp+VUrrgwDQYJKoZIhvcNAQELBQADggEBAF5q2fDwnse8egXhfaJCqqM969E9gSacqFmASpoDJPRPEX7gqoO7v1ww7nqRtRDoRiBvo/yNk7jlSAkRN3nRRnZLZZ3MYQdmCr4FGyIqRg4Y94+nja+Du9pDD761rxRktMVPSOaAVM/E5DQvscDlPvlPYe9mkcrLCE4DXYpiMmLT8Tm55LJJq5m07dVDgzAIR1L/hmEcbK0pnLgzciMtMLxGO2udnyyW/UW9WxnjvrrD2JluTHH9mVbb+XQP1oFtlRBfH7aui1ZgWfKvxrdP4zdK9QoWSUvRux3TLsGmHRBjBMtqYDY3y5mB+aNjLelvWpeVb0m2aOSVXynrLwNCAVA=\n-----END CERTIFICATE-----")
                    .withLedgerRoleName(LedgerRoleName.READER))))
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

### ManagedCcf_Backup

```java
import com.azure.resourcemanager.confidentialledger.models.ManagedCcfBackup;

/**
 * Samples for ManagedCcf Backup.
 */
public final class ManagedCcfBackupSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2023-06-28-preview/examples/ManagedCCF_Backup.json
     */
    /**
     * Sample code: ManagedCCFBackup.
     * 
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void
        managedCCFBackup(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.managedCcfs()
            .backup("DummyResourceGroupName", "DummyMccfAppName",
                new ManagedCcfBackup().withRestoreRegion("EastUS").withUri("DummySASUri"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagedCcf_Create

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.confidentialledger.models.DeploymentType;
import com.azure.resourcemanager.confidentialledger.models.LanguageRuntime;
import com.azure.resourcemanager.confidentialledger.models.ManagedCcfProperties;
import com.azure.resourcemanager.confidentialledger.models.MemberIdentityCertificate;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ManagedCcf Create.
 */
public final class ManagedCcfCreateSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2023-06-28-preview/examples/ManagedCCF_Create.json
     */
    /**
     * Sample code: ManagedCCFCreate.
     * 
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void managedCCFCreate(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager)
        throws IOException {
        manager.managedCcfs()
            .define("DummyMccfAppName")
            .withRegion("EastUS")
            .withExistingResourceGroup("DummyResourceGroupName")
            .withTags(mapOf("additionalProps1", "additional properties"))
            .withProperties(new ManagedCcfProperties()
                .withMemberIdentityCertificates(Arrays.asList(new MemberIdentityCertificate().withCertificate(
                    "-----BEGIN CERTIFICATE-----MIIBsjCCATigAwIBAgIUZWIbyG79TniQLd2UxJuU74tqrKcwCgYIKoZIzj0EAwMwEDEOMAwGA1UEAwwFdXNlcjAwHhcNMjEwMzE2MTgwNjExWhcNMjIwMzE2MTgwNjExWjAQMQ4wDAYDVQQDDAV1c2VyMDB2MBAGByqGSM49AgEGBSuBBAAiA2IABBiWSo/j8EFit7aUMm5lF+lUmCu+IgfnpFD+7QMgLKtxRJ3aGSqgS/GpqcYVGddnODtSarNE/HyGKUFUolLPQ5ybHcouUk0kyfA7XMeSoUA4lBz63Wha8wmXo+NdBRo39qNTMFEwHQYDVR0OBBYEFPtuhrwgGjDFHeUUT4nGsXaZn69KMB8GA1UdIwQYMBaAFPtuhrwgGjDFHeUUT4nGsXaZn69KMA8GA1UdEwEB/wQFMAMBAf8wCgYIKoZIzj0EAwMDaAAwZQIxAOnozm2CyqRwSSQLls5r+mUHRGRyXHXwYtM4Dcst/VEZdmS9fqvHRCHbjUlO/+HNfgIwMWZ4FmsjD3wnPxONOm9YdVn/PRD7SsPRPbOjwBiE4EBGaHDsLjYAGDSGi7NJnSkA-----END CERTIFICATE-----")
                    .withEncryptionkey("fakeTokenPlaceholder")
                    .withTags(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize("{\"additionalProps1\":\"additional properties\"}", Object.class,
                            SerializerEncoding.JSON))))
                .withDeploymentType(new DeploymentType().withLanguageRuntime(LanguageRuntime.CPP)
                    .withAppSourceUri(
                        "https://myaccount.blob.core.windows.net/storage/mccfsource?sv=2022-02-11%st=2022-03-11"))
                .withNodeCount(5))
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

### ManagedCcf_Delete

```java
/**
 * Samples for ManagedCcf Delete.
 */
public final class ManagedCcfDeleteSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2023-06-28-preview/examples/ManagedCCF_Delete.json
     */
    /**
     * Sample code: ConfidentialLedgerDelete.
     * 
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void
        confidentialLedgerDelete(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.managedCcfs().delete("DummyResourceGroupName", "DummyMccfAppName", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedCcf_GetByResourceGroup

```java
/**
 * Samples for ManagedCcf GetByResourceGroup.
 */
public final class ManagedCcfGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2023-06-28-preview/examples/ManagedCCF_Get.json
     */
    /**
     * Sample code: ManagedCCFGet.
     * 
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void managedCCFGet(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.managedCcfs()
            .getByResourceGroupWithResponse("DummyResourceGroupName", "DummyMccfAppName",
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagedCcf_List

```java
/**
 * Samples for ManagedCcf List.
 */
public final class ManagedCcfListSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2023-06-28-preview/examples/ManagedCCF_ListBySub.json
     */
    /**
     * Sample code: ManagedCCFListBySub.
     * 
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void
        managedCCFListBySub(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.managedCcfs().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### ManagedCcf_ListByResourceGroup

```java
/**
 * Samples for ManagedCcf ListByResourceGroup.
 */
public final class ManagedCcfListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2023-06-28-preview/examples/ManagedCCF_List.json
     */
    /**
     * Sample code: ManagedCCFList.
     * 
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void managedCCFList(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.managedCcfs().listByResourceGroup("DummyResourceGroupName", null, com.azure.core.util.Context.NONE);
    }
}
```

### ManagedCcf_Restore

```java
import com.azure.resourcemanager.confidentialledger.models.ManagedCcfRestore;

/**
 * Samples for ManagedCcf Restore.
 */
public final class ManagedCcfRestoreSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2023-06-28-preview/examples/ManagedCCF_Restore.json
     */
    /**
     * Sample code: ManagedCCFRestore.
     * 
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void
        managedCCFRestore(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.managedCcfs()
            .restore("DummyResourceGroupName", "DummyMccfAppName",
                new ManagedCcfRestore().withFileShareName("DummyFileShareName")
                    .withRestoreRegion("EastUS")
                    .withUri("DummySASUri"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagedCcf_Update

```java
import com.azure.resourcemanager.confidentialledger.models.DeploymentType;
import com.azure.resourcemanager.confidentialledger.models.LanguageRuntime;
import com.azure.resourcemanager.confidentialledger.models.ManagedCcf;
import com.azure.resourcemanager.confidentialledger.models.ManagedCcfProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ManagedCcf Update.
 */
public final class ManagedCcfUpdateSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2023-06-28-preview/examples/ManagedCCF_Update.json
     */
    /**
     * Sample code: ManagedCCFUpdate.
     * 
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void
        managedCCFUpdate(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        ManagedCcf resource = manager.managedCcfs()
            .getByResourceGroupWithResponse("DummyResourceGroupName", "DummyMccfAppName",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("additionalProps1", "additional properties"))
            .withProperties(new ManagedCcfProperties()
                .withDeploymentType(new DeploymentType().withLanguageRuntime(LanguageRuntime.CPP)
                    .withAppSourceUri(
                        "https://myaccount.blob.core.windows.net/storage/mccfsource?sv=2022-02-11%st=2022-03-11")))
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
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2023-06-28-preview/examples/Operations_Get.json
     */
    /**
     * Sample code: OperationsGet.
     * 
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void operationsGet(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_CheckNameAvailability

```java
import com.azure.resourcemanager.confidentialledger.models.CheckNameAvailabilityRequest;

/**
 * Samples for ResourceProvider CheckNameAvailability.
 */
public final class ResourceProviderCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/preview/2023-06-28-preview/examples/CheckNameAvailability.json
     */
    /**
     * Sample code: CheckNameAvailability.
     * 
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void
        checkNameAvailability(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.resourceProviders()
            .checkNameAvailabilityWithResponse(new CheckNameAvailabilityRequest().withName("sample-name")
                .withType("Microsoft.ConfidentialLedger/ledgers"), com.azure.core.util.Context.NONE);
    }
}
```

