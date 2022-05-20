# Code snippets and samples


## Ledger

- [Create](#ledger_create)
- [Delete](#ledger_delete)
- [GetByResourceGroup](#ledger_getbyresourcegroup)
- [List](#ledger_list)
- [ListByResourceGroup](#ledger_listbyresourcegroup)
- [Update](#ledger_update)

## Operations

- [List](#operations_list)

## ResourceProvider

- [CheckNameAvailability](#resourceprovider_checknameavailability)
### Ledger_Create

```java
import com.azure.resourcemanager.confidentialledger.models.AadBasedSecurityPrincipal;
import com.azure.resourcemanager.confidentialledger.models.CertBasedSecurityPrincipal;
import com.azure.resourcemanager.confidentialledger.models.LedgerProperties;
import com.azure.resourcemanager.confidentialledger.models.LedgerRoleName;
import com.azure.resourcemanager.confidentialledger.models.LedgerType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Ledger Create. */
public final class LedgerCreateSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/stable/2022-05-13/examples/ConfidentialLedger_Create.json
     */
    /**
     * Sample code: ConfidentialLedgerCreate.
     *
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void confidentialLedgerCreate(
        com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager
            .ledgers()
            .define("DummyLedgerName")
            .withExistingResourceGroup("DummyResourceGroupName")
            .withRegion("EastUS")
            .withTags(mapOf("additionalProps1", "additional properties"))
            .withProperties(
                new LedgerProperties()
                    .withLedgerType(LedgerType.PUBLIC)
                    .withAadBasedSecurityPrincipals(
                        Arrays
                            .asList(
                                new AadBasedSecurityPrincipal()
                                    .withPrincipalId("34621747-6fc8-4771-a2eb-72f31c461f2e")
                                    .withTenantId("bce123b9-2b7b-4975-8360-5ca0b9b1cd08")
                                    .withLedgerRoleName(LedgerRoleName.ADMINISTRATOR)))
                    .withCertBasedSecurityPrincipals(
                        Arrays
                            .asList(
                                new CertBasedSecurityPrincipal()
                                    .withCert(
                                        "-----BEGIN"
                                            + " CERTIFICATE-----MIIBsjCCATigAwIBAgIUZWIbyG79TniQLd2UxJuU74tqrKcwCgYIKoZIzj0EAwMwEDEOMAwGA1UEAwwFdXNlcjAwHhcNMjEwMzE2MTgwNjExWhcNMjIwMzE2MTgwNjExWjAQMQ4wDAYDVQQDDAV1c2VyMDB2MBAGByqGSM49AgEGBSuBBAAiA2IABBiWSo/j8EFit7aUMm5lF+lUmCu+IgfnpFD+7QMgLKtxRJ3aGSqgS/GpqcYVGddnODtSarNE/HyGKUFUolLPQ5ybHcouUk0kyfA7XMeSoUA4lBz63Wha8wmXo+NdBRo39qNTMFEwHQYDVR0OBBYEFPtuhrwgGjDFHeUUT4nGsXaZn69KMB8GA1UdIwQYMBaAFPtuhrwgGjDFHeUUT4nGsXaZn69KMA8GA1UdEwEB/wQFMAMBAf8wCgYIKoZIzj0EAwMDaAAwZQIxAOnozm2CyqRwSSQLls5r+mUHRGRyXHXwYtM4Dcst/VEZdmS9fqvHRCHbjUlO/+HNfgIwMWZ4FmsjD3wnPxONOm9YdVn/PRD7SsPRPbOjwBiE4EBGaHDsLjYAGDSGi7NJnSkA-----END"
                                            + " CERTIFICATE-----")
                                    .withLedgerRoleName(LedgerRoleName.READER))))
            .create();
    }

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
import com.azure.core.util.Context;

/** Samples for Ledger Delete. */
public final class LedgerDeleteSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/stable/2022-05-13/examples/ConfidentialLedger_Delete.json
     */
    /**
     * Sample code: ConfidentialLedgerDelete.
     *
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void confidentialLedgerDelete(
        com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.ledgers().delete("DummyResourceGroupName", "DummyLedgerName", Context.NONE);
    }
}
```

### Ledger_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Ledger GetByResourceGroup. */
public final class LedgerGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/stable/2022-05-13/examples/ConfidentialLedger_Get.json
     */
    /**
     * Sample code: ConfidentialLedgerGet.
     *
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void confidentialLedgerGet(
        com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.ledgers().getByResourceGroupWithResponse("DummyResourceGroupName", "DummyLedgerName", Context.NONE);
    }
}
```

### Ledger_List

```java
import com.azure.core.util.Context;

/** Samples for Ledger List. */
public final class LedgerListSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/stable/2022-05-13/examples/ConfidentialLedger_ListBySub.json
     */
    /**
     * Sample code: ConfidentialLedgerListBySub.
     *
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void confidentialLedgerListBySub(
        com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.ledgers().list(null, Context.NONE);
    }
}
```

### Ledger_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Ledger ListByResourceGroup. */
public final class LedgerListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/stable/2022-05-13/examples/ConfidentialLedger_List.json
     */
    /**
     * Sample code: ConfidentialLedgerList.
     *
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void confidentialLedgerList(
        com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.ledgers().listByResourceGroup("DummyResourceGroupName", null, Context.NONE);
    }
}
```

### Ledger_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.confidentialledger.models.AadBasedSecurityPrincipal;
import com.azure.resourcemanager.confidentialledger.models.CertBasedSecurityPrincipal;
import com.azure.resourcemanager.confidentialledger.models.ConfidentialLedger;
import com.azure.resourcemanager.confidentialledger.models.LedgerProperties;
import com.azure.resourcemanager.confidentialledger.models.LedgerRoleName;
import com.azure.resourcemanager.confidentialledger.models.LedgerType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Ledger Update. */
public final class LedgerUpdateSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/stable/2022-05-13/examples/ConfidentialLedger_Update.json
     */
    /**
     * Sample code: ConfidentialLedgerUpdate.
     *
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void confidentialLedgerUpdate(
        com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        ConfidentialLedger resource =
            manager
                .ledgers()
                .getByResourceGroupWithResponse("DummyResourceGroupName", "DummyLedgerName", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("additionProps2", "additional property value", "additionalProps1", "additional properties"))
            .withProperties(
                new LedgerProperties()
                    .withLedgerType(LedgerType.PUBLIC)
                    .withAadBasedSecurityPrincipals(
                        Arrays
                            .asList(
                                new AadBasedSecurityPrincipal()
                                    .withPrincipalId("34621747-6fc8-4771-a2eb-72f31c461f2e")
                                    .withTenantId("bce123b9-2b7b-4975-8360-5ca0b9b1cd08")
                                    .withLedgerRoleName(LedgerRoleName.ADMINISTRATOR)))
                    .withCertBasedSecurityPrincipals(
                        Arrays
                            .asList(
                                new CertBasedSecurityPrincipal()
                                    .withCert(
                                        "-----BEGIN CERTIFICATE-----\n"
                                            + "MIIDUjCCAjqgAwIBAgIQJ2IrDBawSkiAbkBYmiAopDANBgkqhkiG9w0BAQsFADAmMSQwIgYDVQQDExtTeW50aGV0aWNzIExlZGdlciBVc2VyIENlcnQwHhcNMjAwOTIzMjIxODQ2WhcNMjEwOTIzMjIyODQ2WjAmMSQwIgYDVQQDExtTeW50aGV0aWNzIExlZGdlciBVc2VyIENlcnQwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCX2s/Eu4q/eQ63N+Ugeg5oAciZua/YCJr41c/696szvSY7Zg1SNJlW88/nbz70+QpO55OmqlEE3QCU+T0Vl/h0Gf//n1PYcoBbTGUnYEmV+fTTHict6rFiEwrGJ62tvcpYgwapInSLyEeUzjki0zhOLJ1OfRnYd1eGnFVMpE5aVjiS8Q5dmTEUyd51EIprGE8RYAW9aeWSwTH7gjHUsRlJnHKcdhaK/v5QKJnNu5bzPFUcpC0ZBcizoMPAtroLAD4B68Jl0z3op18MgZe6lRrVoWuxfqnk5GojuB/Vu8ohAZKoFhQ6NB6r+LL2AUs+Zr7Bt26IkEdR178n9JMEA4gHAgMBAAGjfDB6MA4GA1UdDwEB/wQEAwIFoDAJBgNVHRMEAjAAMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjAfBgNVHSMEGDAWgBS/a7PU9iOfOKEyZCp11Oen5VSuuDAdBgNVHQ4EFgQUv2uz1PYjnzihMmQqddTnp+VUrrgwDQYJKoZIhvcNAQELBQADggEBAF5q2fDwnse8egXhfaJCqqM969E9gSacqFmASpoDJPRPEX7gqoO7v1ww7nqRtRDoRiBvo/yNk7jlSAkRN3nRRnZLZZ3MYQdmCr4FGyIqRg4Y94+nja+Du9pDD761rxRktMVPSOaAVM/E5DQvscDlPvlPYe9mkcrLCE4DXYpiMmLT8Tm55LJJq5m07dVDgzAIR1L/hmEcbK0pnLgzciMtMLxGO2udnyyW/UW9WxnjvrrD2JluTHH9mVbb+XQP1oFtlRBfH7aui1ZgWfKvxrdP4zdK9QoWSUvRux3TLsGmHRBjBMtqYDY3y5mB+aNjLelvWpeVb0m2aOSVXynrLwNCAVA=\n"
                                            + "-----END CERTIFICATE-----")
                                    .withLedgerRoleName(LedgerRoleName.READER))))
            .apply();
    }

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
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/stable/2022-05-13/examples/Operations_Get.json
     */
    /**
     * Sample code: OperationsGet.
     *
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void operationsGet(com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### ResourceProvider_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.confidentialledger.models.CheckNameAvailabilityRequest;

/** Samples for ResourceProvider CheckNameAvailability. */
public final class ResourceProviderCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/confidentialledger/resource-manager/Microsoft.ConfidentialLedger/stable/2022-05-13/examples/CheckNameAvailability.json
     */
    /**
     * Sample code: CheckNameAvailability.
     *
     * @param manager Entry point to ConfidentialLedgerManager.
     */
    public static void checkNameAvailability(
        com.azure.resourcemanager.confidentialledger.ConfidentialLedgerManager manager) {
        manager
            .resourceProviders()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityRequest()
                    .withName("sample-name")
                    .withType("Microsoft.ConfidentialLedger/ledgers"),
                Context.NONE);
    }
}
```

