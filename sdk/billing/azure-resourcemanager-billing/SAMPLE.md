# Code snippets and samples


## Address

- [Validate](#address_validate)

## Agreements

- [Get](#agreements_get)
- [ListByBillingAccount](#agreements_listbybillingaccount)

## AvailableBalances

- [Get](#availablebalances_get)

## BillingAccounts

- [Get](#billingaccounts_get)
- [List](#billingaccounts_list)
- [ListInvoiceSectionsByCreateSubscriptionPermission](#billingaccounts_listinvoicesectionsbycreatesubscriptionpermission)
- [Update](#billingaccounts_update)

## BillingPeriods

- [Get](#billingperiods_get)
- [List](#billingperiods_list)

## BillingPermissions

- [ListByBillingAccount](#billingpermissions_listbybillingaccount)
- [ListByBillingProfile](#billingpermissions_listbybillingprofile)
- [ListByCustomer](#billingpermissions_listbycustomer)
- [ListByInvoiceSections](#billingpermissions_listbyinvoicesections)

## BillingProfiles

- [CreateOrUpdate](#billingprofiles_createorupdate)
- [Get](#billingprofiles_get)
- [ListByBillingAccount](#billingprofiles_listbybillingaccount)

## BillingProperty

- [Get](#billingproperty_get)
- [Update](#billingproperty_update)

## BillingRoleAssignments

- [DeleteByBillingAccount](#billingroleassignments_deletebybillingaccount)
- [DeleteByBillingProfile](#billingroleassignments_deletebybillingprofile)
- [DeleteByInvoiceSection](#billingroleassignments_deletebyinvoicesection)
- [GetByBillingAccount](#billingroleassignments_getbybillingaccount)
- [GetByBillingProfile](#billingroleassignments_getbybillingprofile)
- [GetByInvoiceSection](#billingroleassignments_getbyinvoicesection)
- [ListByBillingAccount](#billingroleassignments_listbybillingaccount)
- [ListByBillingProfile](#billingroleassignments_listbybillingprofile)
- [ListByInvoiceSection](#billingroleassignments_listbyinvoicesection)

## BillingRoleDefinitions

- [GetByBillingAccount](#billingroledefinitions_getbybillingaccount)
- [GetByBillingProfile](#billingroledefinitions_getbybillingprofile)
- [GetByInvoiceSection](#billingroledefinitions_getbyinvoicesection)
- [ListByBillingAccount](#billingroledefinitions_listbybillingaccount)
- [ListByBillingProfile](#billingroledefinitions_listbybillingprofile)
- [ListByInvoiceSection](#billingroledefinitions_listbyinvoicesection)

## BillingSubscriptions

- [Get](#billingsubscriptions_get)
- [ListByBillingAccount](#billingsubscriptions_listbybillingaccount)
- [ListByBillingProfile](#billingsubscriptions_listbybillingprofile)
- [ListByCustomer](#billingsubscriptions_listbycustomer)
- [ListByInvoiceSection](#billingsubscriptions_listbyinvoicesection)
- [Move](#billingsubscriptions_move)
- [Update](#billingsubscriptions_update)
- [ValidateMove](#billingsubscriptions_validatemove)

## Customers

- [Get](#customers_get)
- [ListByBillingAccount](#customers_listbybillingaccount)
- [ListByBillingProfile](#customers_listbybillingprofile)

## EnrollmentAccounts

- [Get](#enrollmentaccounts_get)
- [List](#enrollmentaccounts_list)

## Instructions

- [Get](#instructions_get)
- [ListByBillingProfile](#instructions_listbybillingprofile)
- [Put](#instructions_put)

## InvoiceSections

- [CreateOrUpdate](#invoicesections_createorupdate)
- [Get](#invoicesections_get)
- [ListByBillingProfile](#invoicesections_listbybillingprofile)

## Invoices

- [DownloadBillingSubscriptionInvoice](#invoices_downloadbillingsubscriptioninvoice)
- [DownloadInvoice](#invoices_downloadinvoice)
- [DownloadMultipleBillingProfileInvoices](#invoices_downloadmultiplebillingprofileinvoices)
- [DownloadMultipleBillingSubscriptionInvoices](#invoices_downloadmultiplebillingsubscriptioninvoices)
- [Get](#invoices_get)
- [GetById](#invoices_getbyid)
- [GetBySubscriptionAndInvoiceId](#invoices_getbysubscriptionandinvoiceid)
- [ListByBillingAccount](#invoices_listbybillingaccount)
- [ListByBillingProfile](#invoices_listbybillingprofile)
- [ListByBillingSubscription](#invoices_listbybillingsubscription)

## Operations

- [List](#operations_list)

## Policies

- [GetByBillingProfile](#policies_getbybillingprofile)
- [GetByCustomer](#policies_getbycustomer)
- [Update](#policies_update)
- [UpdateCustomer](#policies_updatecustomer)

## Products

- [Get](#products_get)
- [ListByBillingAccount](#products_listbybillingaccount)
- [ListByBillingProfile](#products_listbybillingprofile)
- [ListByCustomer](#products_listbycustomer)
- [ListByInvoiceSection](#products_listbyinvoicesection)
- [Move](#products_move)
- [Update](#products_update)
- [ValidateMove](#products_validatemove)

## Reservations

- [ListByBillingAccount](#reservations_listbybillingaccount)
- [ListByBillingProfile](#reservations_listbybillingprofile)

## Transactions

- [ListByInvoice](#transactions_listbyinvoice)
### Address_Validate

```java
import com.azure.resourcemanager.billing.models.AddressDetails;

/** Samples for Address Validate. */
public final class AddressValidateSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/AddressInvalid.json
     */
    /**
     * Sample code: AddressInvalid.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void addressInvalid(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .address()
            .validateWithResponse(
                new AddressDetails()
                    .withAddressLine1("1 Test")
                    .withCity("bellevue")
                    .withRegion("wa")
                    .withCountry("us")
                    .withPostalCode("fakeTokenPlaceholder"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/AddressValid.json
     */
    /**
     * Sample code: AddressValid.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void addressValid(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .address()
            .validateWithResponse(
                new AddressDetails()
                    .withAddressLine1("1 Test Address")
                    .withCity("bellevue")
                    .withRegion("wa")
                    .withCountry("us")
                    .withPostalCode("fakeTokenPlaceholder"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Agreements_Get

```java
/** Samples for Agreements Get. */
public final class AgreementsGetSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/AgreementByName.json
     */
    /**
     * Sample code: AgreementByName.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void agreementByName(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .agreements()
            .getWithResponse("{billingAccountName}", "{agreementName}", null, com.azure.core.util.Context.NONE);
    }
}
```

### Agreements_ListByBillingAccount

```java
/** Samples for Agreements ListByBillingAccount. */
public final class AgreementsListByBillingAccountSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/AgreementsListByBillingAccount.json
     */
    /**
     * Sample code: AgreementsListByBillingAccount.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void agreementsListByBillingAccount(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.agreements().listByBillingAccount("{billingAccountName}", null, com.azure.core.util.Context.NONE);
    }
}
```

### AvailableBalances_Get

```java
/** Samples for AvailableBalances Get. */
public final class AvailableBalancesGetSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/AvailableBalanceByBillingProfile.json
     */
    /**
     * Sample code: AvailableBalanceByBillingProfile.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void availableBalanceByBillingProfile(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .availableBalances()
            .getWithResponse("{billingAccountName}", "{billingProfileName}", com.azure.core.util.Context.NONE);
    }
}
```

### BillingAccounts_Get

```java
/** Samples for BillingAccounts Get. */
public final class BillingAccountsGetSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingAccountWithExpand.json
     */
    /**
     * Sample code: BillingAccountWithExpand.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingAccountWithExpand(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingAccounts()
            .getWithResponse(
                "{billingAccountName}",
                "soldTo,billingProfiles,billingProfiles/invoiceSections",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingAccount.json
     */
    /**
     * Sample code: BillingAccounts.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingAccounts(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.billingAccounts().getWithResponse("{billingAccountName}", null, com.azure.core.util.Context.NONE);
    }
}
```

### BillingAccounts_List

```java
/** Samples for BillingAccounts List. */
public final class BillingAccountsListSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingAccountsList.json
     */
    /**
     * Sample code: BillingAccountsList.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingAccountsList(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.billingAccounts().list(null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingAccountsListWithExpandForEnrollmentDetails.json
     */
    /**
     * Sample code: BillingAccountsListWithExpandForEnrollmentDetails.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingAccountsListWithExpandForEnrollmentDetails(
        com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingAccounts()
            .list("enrollmentDetails,departments,enrollmentAccounts", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingAccountsListWithExpand.json
     */
    /**
     * Sample code: BillingAccountsListWithExpand.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingAccountsListWithExpand(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingAccounts()
            .list("soldTo,billingProfiles,billingProfiles/invoiceSections", com.azure.core.util.Context.NONE);
    }
}
```

### BillingAccounts_ListInvoiceSectionsByCreateSubscriptionPermission

```java
/** Samples for BillingAccounts ListInvoiceSectionsByCreateSubscriptionPermission. */
public final class BillingAccountsListInvoiceSectionsByCreateSubscriptionPermissionSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/InvoiceSectionsListWithCreateSubPermission.json
     */
    /**
     * Sample code: InvoiceSectionsListWithCreateSubPermission.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void invoiceSectionsListWithCreateSubPermission(
        com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingAccounts()
            .listInvoiceSectionsByCreateSubscriptionPermission(
                "{billingAccountName}", com.azure.core.util.Context.NONE);
    }
}
```

### BillingAccounts_Update

```java
import com.azure.resourcemanager.billing.models.AddressDetails;
import com.azure.resourcemanager.billing.models.BillingAccountUpdateRequest;

/** Samples for BillingAccounts Update. */
public final class BillingAccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/UpdateBillingAccount.json
     */
    /**
     * Sample code: UpdateBillingAccount.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void updateBillingAccount(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingAccounts()
            .update(
                "{billingAccountName}",
                new BillingAccountUpdateRequest()
                    .withDisplayName("Test Account")
                    .withSoldTo(
                        new AddressDetails()
                            .withFirstName("Test")
                            .withLastName("User")
                            .withCompanyName("Contoso")
                            .withAddressLine1("Test Address 1")
                            .withCity("Redmond")
                            .withRegion("WA")
                            .withCountry("US")
                            .withPostalCode("fakeTokenPlaceholder")),
                com.azure.core.util.Context.NONE);
    }
}
```

### BillingPeriods_Get

```java
/** Samples for BillingPeriods Get. */
public final class BillingPeriodsGetSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/preview/2018-03-01-preview/examples/BillingPeriodsGet.json
     */
    /**
     * Sample code: BillingPeriodsGet.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingPeriodsGet(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.billingPeriods().getWithResponse("201702-1", com.azure.core.util.Context.NONE);
    }
}
```

### BillingPeriods_List

```java
/** Samples for BillingPeriods List. */
public final class BillingPeriodsListSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/preview/2018-03-01-preview/examples/BillingPeriodsList.json
     */
    /**
     * Sample code: BillingPeriodsList.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingPeriodsList(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.billingPeriods().list(null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### BillingPermissions_ListByBillingAccount

```java
/** Samples for BillingPermissions ListByBillingAccount. */
public final class BillingPermissionsListByBillingAccountSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingAccountPermissionsList.json
     */
    /**
     * Sample code: BillingAccountPermissionsList.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingAccountPermissionsList(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.billingPermissions().listByBillingAccount("{billingAccountName}", com.azure.core.util.Context.NONE);
    }
}
```

### BillingPermissions_ListByBillingProfile

```java
/** Samples for BillingPermissions ListByBillingProfile. */
public final class BillingPermissionsListByBillingProfileSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingProfilePermissionsList.json
     */
    /**
     * Sample code: BillingProfilePermissionsList.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingProfilePermissionsList(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingPermissions()
            .listByBillingProfile("{billingAccountName}", "{billingProfileName}", com.azure.core.util.Context.NONE);
    }
}
```

### BillingPermissions_ListByCustomer

```java
/** Samples for BillingPermissions ListByCustomer. */
public final class BillingPermissionsListByCustomerSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/CustomerPermissionsList.json
     */
    /**
     * Sample code: BillingProfilePermissionsList.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingProfilePermissionsList(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingPermissions()
            .listByCustomer("{billingAccountName}", "{customerName}", com.azure.core.util.Context.NONE);
    }
}
```

### BillingPermissions_ListByInvoiceSections

```java
/** Samples for BillingPermissions ListByInvoiceSections. */
public final class BillingPermissionsListByInvoiceSectionsSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/InvoiceSectionPermissionsList.json
     */
    /**
     * Sample code: InvoiceSectionPermissionsList.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void invoiceSectionPermissionsList(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingPermissions()
            .listByInvoiceSections(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                com.azure.core.util.Context.NONE);
    }
}
```

### BillingProfiles_CreateOrUpdate

```java
import com.azure.resourcemanager.billing.fluent.models.BillingProfileInner;
import com.azure.resourcemanager.billing.models.AddressDetails;
import com.azure.resourcemanager.billing.models.AzurePlan;
import java.util.Arrays;

/** Samples for BillingProfiles CreateOrUpdate. */
public final class BillingProfilesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/PutBillingProfile.json
     */
    /**
     * Sample code: CreateBillingProfile.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void createBillingProfile(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingProfiles()
            .createOrUpdate(
                "{billingAccountName}",
                "{billingProfileName}",
                new BillingProfileInner()
                    .withDisplayName("Finance")
                    .withPoNumber("ABC12345")
                    .withBillTo(
                        new AddressDetails()
                            .withFirstName("Test")
                            .withLastName("User")
                            .withAddressLine1("Test Address 1")
                            .withCity("Redmond")
                            .withRegion("WA")
                            .withCountry("US")
                            .withPostalCode("fakeTokenPlaceholder"))
                    .withInvoiceEmailOptIn(true)
                    .withEnabledAzurePlans(
                        Arrays.asList(new AzurePlan().withSkuId("0001"), new AzurePlan().withSkuId("0002"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### BillingProfiles_Get

```java
/** Samples for BillingProfiles Get. */
public final class BillingProfilesGetSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingProfileWithExpand.json
     */
    /**
     * Sample code: BillingProfileWithExpand.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingProfileWithExpand(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingProfiles()
            .getWithResponse(
                "{billingAccountName}", "{billingProfileName}", "invoiceSections", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingProfile.json
     */
    /**
     * Sample code: BillingProfile.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingProfile(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingProfiles()
            .getWithResponse("{billingAccountName}", "{billingProfileName}", null, com.azure.core.util.Context.NONE);
    }
}
```

### BillingProfiles_ListByBillingAccount

```java
/** Samples for BillingProfiles ListByBillingAccount. */
public final class BillingProfilesListByBillingAccountSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingProfilesListWithExpand.json
     */
    /**
     * Sample code: BillingProfilesListWithExpand.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingProfilesListWithExpand(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingProfiles()
            .listByBillingAccount("{billingAccountName}", "invoiceSections", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingProfilesListByBillingAccount.json
     */
    /**
     * Sample code: BillingProfilesListByBillingAccount.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingProfilesListByBillingAccount(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.billingProfiles().listByBillingAccount("{billingAccountName}", null, com.azure.core.util.Context.NONE);
    }
}
```

### BillingProperty_Get

```java
/** Samples for BillingProperty Get. */
public final class BillingPropertyGetSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingProperty.json
     */
    /**
     * Sample code: BillingProperty.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingProperty(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.billingProperties().getWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### BillingProperty_Update

```java
import com.azure.resourcemanager.billing.fluent.models.BillingPropertyInner;

/** Samples for BillingProperty Update. */
public final class BillingPropertyUpdateSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/UpdateBillingProperty.json
     */
    /**
     * Sample code: UpdateBillingProperty.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void updateBillingProperty(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingProperties()
            .updateWithResponse(new BillingPropertyInner().withCostCenter("1010"), com.azure.core.util.Context.NONE);
    }
}
```

### BillingRoleAssignments_DeleteByBillingAccount

```java
/** Samples for BillingRoleAssignments DeleteByBillingAccount. */
public final class BillingRoleAssignmentsDeleteByBillingAccountSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingAccountRoleAssignmentDelete.json
     */
    /**
     * Sample code: BillingAccountRoleAssignmentDelete.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingAccountRoleAssignmentDelete(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingRoleAssignments()
            .deleteByBillingAccountWithResponse(
                "{billingAccountName}", "{billingRoleAssignmentName}", com.azure.core.util.Context.NONE);
    }
}
```

### BillingRoleAssignments_DeleteByBillingProfile

```java
/** Samples for BillingRoleAssignments DeleteByBillingProfile. */
public final class BillingRoleAssignmentsDeleteByBillingProfileSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingProfileRoleAssignmentDelete.json
     */
    /**
     * Sample code: BillingProfileRoleAssignmentDelete.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingProfileRoleAssignmentDelete(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingRoleAssignments()
            .deleteByBillingProfileWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{billingRoleAssignmentName}",
                com.azure.core.util.Context.NONE);
    }
}
```

### BillingRoleAssignments_DeleteByInvoiceSection

```java
/** Samples for BillingRoleAssignments DeleteByInvoiceSection. */
public final class BillingRoleAssignmentsDeleteByInvoiceSectionSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/InvoiceSectionRoleAssignmentDelete.json
     */
    /**
     * Sample code: InvoiceSectionRoleAssignmentDelete.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void invoiceSectionRoleAssignmentDelete(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingRoleAssignments()
            .deleteByInvoiceSectionWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                "{billingRoleAssignmentName}",
                com.azure.core.util.Context.NONE);
    }
}
```

### BillingRoleAssignments_GetByBillingAccount

```java
/** Samples for BillingRoleAssignments GetByBillingAccount. */
public final class BillingRoleAssignmentsGetByBillingAccountSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingAccountRoleAssignment.json
     */
    /**
     * Sample code: BillingAccountRoleAssignment.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingAccountRoleAssignment(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingRoleAssignments()
            .getByBillingAccountWithResponse(
                "{billingAccountName}", "{billingRoleAssignmentId}", com.azure.core.util.Context.NONE);
    }
}
```

### BillingRoleAssignments_GetByBillingProfile

```java
/** Samples for BillingRoleAssignments GetByBillingProfile. */
public final class BillingRoleAssignmentsGetByBillingProfileSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingProfileRoleAssignment.json
     */
    /**
     * Sample code: BillingProfileRoleAssignment.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingProfileRoleAssignment(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingRoleAssignments()
            .getByBillingProfileWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{billingRoleAssignmentName}",
                com.azure.core.util.Context.NONE);
    }
}
```

### BillingRoleAssignments_GetByInvoiceSection

```java
/** Samples for BillingRoleAssignments GetByInvoiceSection. */
public final class BillingRoleAssignmentsGetByInvoiceSectionSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/InvoiceSectionRoleAssignment.json
     */
    /**
     * Sample code: InvoiceSectionRoleAssignment.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void invoiceSectionRoleAssignment(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingRoleAssignments()
            .getByInvoiceSectionWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                "{billingRoleAssignmentName}",
                com.azure.core.util.Context.NONE);
    }
}
```

### BillingRoleAssignments_ListByBillingAccount

```java
/** Samples for BillingRoleAssignments ListByBillingAccount. */
public final class BillingRoleAssignmentsListByBillingAccountSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingAccountRoleAssignmentList.json
     */
    /**
     * Sample code: BillingAccountRoleAssignmentList.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingAccountRoleAssignmentList(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.billingRoleAssignments().listByBillingAccount("{billingAccountName}", com.azure.core.util.Context.NONE);
    }
}
```

### BillingRoleAssignments_ListByBillingProfile

```java
/** Samples for BillingRoleAssignments ListByBillingProfile. */
public final class BillingRoleAssignmentsListByBillingProfileSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingProfileRoleAssignmentList.json
     */
    /**
     * Sample code: BillingProfileRoleAssignmentList.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingProfileRoleAssignmentList(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingRoleAssignments()
            .listByBillingProfile("{billingAccountName}", "{billingProfileName}", com.azure.core.util.Context.NONE);
    }
}
```

### BillingRoleAssignments_ListByInvoiceSection

```java
/** Samples for BillingRoleAssignments ListByInvoiceSection. */
public final class BillingRoleAssignmentsListByInvoiceSectionSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/InvoiceSectionRoleAssignmentList.json
     */
    /**
     * Sample code: InvoiceSectionRoleAssignmentList.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void invoiceSectionRoleAssignmentList(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingRoleAssignments()
            .listByInvoiceSection(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                com.azure.core.util.Context.NONE);
    }
}
```

### BillingRoleDefinitions_GetByBillingAccount

```java
/** Samples for BillingRoleDefinitions GetByBillingAccount. */
public final class BillingRoleDefinitionsGetByBillingAccountSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingAccountRoleDefinition.json
     */
    /**
     * Sample code: BillingAccountRoleDefinition.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingAccountRoleDefinition(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingRoleDefinitions()
            .getByBillingAccountWithResponse(
                "{billingAccountName}", "{billingRoleDefinitionName}", com.azure.core.util.Context.NONE);
    }
}
```

### BillingRoleDefinitions_GetByBillingProfile

```java
/** Samples for BillingRoleDefinitions GetByBillingProfile. */
public final class BillingRoleDefinitionsGetByBillingProfileSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingProfileRoleDefinition.json
     */
    /**
     * Sample code: BillingProfileRoleDefinition.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingProfileRoleDefinition(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingRoleDefinitions()
            .getByBillingProfileWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{billingRoleDefinitionName}",
                com.azure.core.util.Context.NONE);
    }
}
```

### BillingRoleDefinitions_GetByInvoiceSection

```java
/** Samples for BillingRoleDefinitions GetByInvoiceSection. */
public final class BillingRoleDefinitionsGetByInvoiceSectionSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/InvoiceSectionRoleDefinition.json
     */
    /**
     * Sample code: InvoiceSectionRoleDefinition.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void invoiceSectionRoleDefinition(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingRoleDefinitions()
            .getByInvoiceSectionWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                "{billingRoleDefinitionName}",
                com.azure.core.util.Context.NONE);
    }
}
```

### BillingRoleDefinitions_ListByBillingAccount

```java
/** Samples for BillingRoleDefinitions ListByBillingAccount. */
public final class BillingRoleDefinitionsListByBillingAccountSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingAccountRoleDefinitionsList.json
     */
    /**
     * Sample code: BillingAccountRoleDefinitionsList.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingAccountRoleDefinitionsList(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.billingRoleDefinitions().listByBillingAccount("{billingAccountName}", com.azure.core.util.Context.NONE);
    }
}
```

### BillingRoleDefinitions_ListByBillingProfile

```java
/** Samples for BillingRoleDefinitions ListByBillingProfile. */
public final class BillingRoleDefinitionsListByBillingProfileSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingProfileRoleDefinitionsList.json
     */
    /**
     * Sample code: BillingProfileRoleDefinitionsList.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingProfileRoleDefinitionsList(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingRoleDefinitions()
            .listByBillingProfile("{billingAccountName}", "{billingProfileName}", com.azure.core.util.Context.NONE);
    }
}
```

### BillingRoleDefinitions_ListByInvoiceSection

```java
/** Samples for BillingRoleDefinitions ListByInvoiceSection. */
public final class BillingRoleDefinitionsListByInvoiceSectionSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/InvoiceSectionRoleDefinitionsList.json
     */
    /**
     * Sample code: InvoiceSectionRoleDefinitionsList.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void invoiceSectionRoleDefinitionsList(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingRoleDefinitions()
            .listByInvoiceSection(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                com.azure.core.util.Context.NONE);
    }
}
```

### BillingSubscriptions_Get

```java
/** Samples for BillingSubscriptions Get. */
public final class BillingSubscriptionsGetSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingSubscription.json
     */
    /**
     * Sample code: BillingSubscription.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingSubscription(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.billingSubscriptions().getWithResponse("{billingAccountName}", com.azure.core.util.Context.NONE);
    }
}
```

### BillingSubscriptions_ListByBillingAccount

```java
/** Samples for BillingSubscriptions ListByBillingAccount. */
public final class BillingSubscriptionsListByBillingAccountSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingSubscriptionsListByBillingAccount.json
     */
    /**
     * Sample code: BillingSubscriptionsListByBillingAccount.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingSubscriptionsListByBillingAccount(
        com.azure.resourcemanager.billing.BillingManager manager) {
        manager.billingSubscriptions().listByBillingAccount("{billingAccountName}", com.azure.core.util.Context.NONE);
    }
}
```

### BillingSubscriptions_ListByBillingProfile

```java
/** Samples for BillingSubscriptions ListByBillingProfile. */
public final class BillingSubscriptionsListByBillingProfileSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingSubscriptionsListByBillingProfile.json
     */
    /**
     * Sample code: BillingSubscriptionsListByBillingProfile.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingSubscriptionsListByBillingProfile(
        com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingSubscriptions()
            .listByBillingProfile("{billingAccountName}", "{billingProfileName}", com.azure.core.util.Context.NONE);
    }
}
```

### BillingSubscriptions_ListByCustomer

```java
/** Samples for BillingSubscriptions ListByCustomer. */
public final class BillingSubscriptionsListByCustomerSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingSubscriptionsListByCustomer.json
     */
    /**
     * Sample code: BillingSubscriptionsListByCustomer.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingSubscriptionsListByCustomer(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingSubscriptions()
            .listByCustomer("{billingAccountName}", "{customerName}", com.azure.core.util.Context.NONE);
    }
}
```

### BillingSubscriptions_ListByInvoiceSection

```java
/** Samples for BillingSubscriptions ListByInvoiceSection. */
public final class BillingSubscriptionsListByInvoiceSectionSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingSubscriptionsListByInvoiceSection.json
     */
    /**
     * Sample code: BillingSubscriptionsListByInvoiceSection.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingSubscriptionsListByInvoiceSection(
        com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingSubscriptions()
            .listByInvoiceSection(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                com.azure.core.util.Context.NONE);
    }
}
```

### BillingSubscriptions_Move

```java
import com.azure.resourcemanager.billing.models.TransferBillingSubscriptionRequestProperties;

/** Samples for BillingSubscriptions Move. */
public final class BillingSubscriptionsMoveSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/MoveBillingSubscription.json
     */
    /**
     * Sample code: MoveBillingSubscription.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void moveBillingSubscription(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingSubscriptions()
            .move(
                "{billingAccountName}",
                new TransferBillingSubscriptionRequestProperties()
                    .withDestinationInvoiceSectionId(
                        "/providers/Microsoft.Billing/billingAccounts/{billingAccountName}/billingProfiles/{billingProfileName}/invoiceSections/{newInvoiceSectionName}"),
                com.azure.core.util.Context.NONE);
    }
}
```

### BillingSubscriptions_Update

```java
import com.azure.resourcemanager.billing.fluent.models.BillingSubscriptionInner;

/** Samples for BillingSubscriptions Update. */
public final class BillingSubscriptionsUpdateSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/UpdateBillingSubscription.json
     */
    /**
     * Sample code: UpdateBillingProperty.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void updateBillingProperty(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingSubscriptions()
            .updateWithResponse(
                "{billingAccountName}",
                new BillingSubscriptionInner().withCostCenter("ABC1234"),
                com.azure.core.util.Context.NONE);
    }
}
```

### BillingSubscriptions_ValidateMove

```java
import com.azure.resourcemanager.billing.models.TransferBillingSubscriptionRequestProperties;

/** Samples for BillingSubscriptions ValidateMove. */
public final class BillingSubscriptionsValidateMoveSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/ValidateSubscriptionMoveFailure.json
     */
    /**
     * Sample code: SubscriptionMoveValidateFailure.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void subscriptionMoveValidateFailure(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingSubscriptions()
            .validateMoveWithResponse(
                "{billingAccountName}",
                new TransferBillingSubscriptionRequestProperties()
                    .withDestinationInvoiceSectionId(
                        "/providers/Microsoft.Billing/billingAccounts/{billingAccountName}/billingProfiles/{billingProfileName}/invoiceSections/{newInvoiceSectionName}"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/ValidateSubscriptionMoveSuccess.json
     */
    /**
     * Sample code: SubscriptionMoveValidateSuccess.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void subscriptionMoveValidateSuccess(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .billingSubscriptions()
            .validateMoveWithResponse(
                "{billingAccountName}",
                new TransferBillingSubscriptionRequestProperties()
                    .withDestinationInvoiceSectionId(
                        "/providers/Microsoft.Billing/billingAccounts/{billingAccountName}/billingProfiles/{billingProfileName}/invoiceSections/{newInvoiceSectionName}"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Customers_Get

```java
/** Samples for Customers Get. */
public final class CustomersGetSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/CustomerWithExpand.json
     */
    /**
     * Sample code: CustomerWithExpand.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void customerWithExpand(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .customers()
            .getWithResponse(
                "{billingAccountName}",
                "{customerName}",
                "enabledAzurePlans,resellers",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/Customer.json
     */
    /**
     * Sample code: Customer.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void customer(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .customers()
            .getWithResponse("{billingAccountName}", "{customerName}", null, com.azure.core.util.Context.NONE);
    }
}
```

### Customers_ListByBillingAccount

```java
/** Samples for Customers ListByBillingAccount. */
public final class CustomersListByBillingAccountSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/CustomersListByBillingAccount.json
     */
    /**
     * Sample code: CustomersListByBillingAccount.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void customersListByBillingAccount(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.customers().listByBillingAccount("{billingAccountName}", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Customers_ListByBillingProfile

```java
/** Samples for Customers ListByBillingProfile. */
public final class CustomersListByBillingProfileSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/CustomersListByBillingProfile.json
     */
    /**
     * Sample code: CustomersListByBillingProfile.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void customersListByBillingProfile(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .customers()
            .listByBillingProfile(
                "{billingAccountName}", "{billingProfileName}", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### EnrollmentAccounts_Get

```java
/** Samples for EnrollmentAccounts Get. */
public final class EnrollmentAccountsGetSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/preview/2018-03-01-preview/examples/EnrollmentAccountsGet.json
     */
    /**
     * Sample code: EnrollmentAccountsGet.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void enrollmentAccountsGet(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .enrollmentAccounts()
            .getWithResponse("e1bf1c8c-5ac6-44a0-bdcd-aa7c1cf60556", com.azure.core.util.Context.NONE);
    }
}
```

### EnrollmentAccounts_List

```java
/** Samples for EnrollmentAccounts List. */
public final class EnrollmentAccountsListSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/preview/2018-03-01-preview/examples/EnrollmentAccountsList.json
     */
    /**
     * Sample code: EnrollmentAccountsList.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void enrollmentAccountsList(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.enrollmentAccounts().list(com.azure.core.util.Context.NONE);
    }
}
```

### Instructions_Get

```java
/** Samples for Instructions Get. */
public final class InstructionsGetSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/Instruction.json
     */
    /**
     * Sample code: Instruction.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void instruction(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .instructions()
            .getWithResponse(
                "{billingAccountName}", "{billingProfileName}", "{instructionName}", com.azure.core.util.Context.NONE);
    }
}
```

### Instructions_ListByBillingProfile

```java
/** Samples for Instructions ListByBillingProfile. */
public final class InstructionsListByBillingProfileSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/InstructionsListByBillingProfile.json
     */
    /**
     * Sample code: InstructionsListByBillingProfile.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void instructionsListByBillingProfile(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .instructions()
            .listByBillingProfile("{billingAccountName}", "{billingProfileName}", com.azure.core.util.Context.NONE);
    }
}
```

### Instructions_Put

```java
import com.azure.resourcemanager.billing.fluent.models.InstructionInner;
import java.time.OffsetDateTime;

/** Samples for Instructions Put. */
public final class InstructionsPutSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/PutInstruction.json
     */
    /**
     * Sample code: PutInstruction.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void putInstruction(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .instructions()
            .putWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{instructionName}",
                new InstructionInner()
                    .withAmount(5000f)
                    .withStartDate(OffsetDateTime.parse("2019-12-30T21:26:47.997Z"))
                    .withEndDate(OffsetDateTime.parse("2020-12-30T21:26:47.997Z")),
                com.azure.core.util.Context.NONE);
    }
}
```

### InvoiceSections_CreateOrUpdate

```java
import com.azure.resourcemanager.billing.fluent.models.InvoiceSectionInner;
import java.util.HashMap;
import java.util.Map;

/** Samples for InvoiceSections CreateOrUpdate. */
public final class InvoiceSectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/PutInvoiceSection.json
     */
    /**
     * Sample code: PutInvoiceSection.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void putInvoiceSection(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .invoiceSections()
            .createOrUpdate(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                new InvoiceSectionInner()
                    .withDisplayName("invoiceSection1")
                    .withLabels(mapOf("costCategory", "Support", "pcCode", "fakeTokenPlaceholder")),
                com.azure.core.util.Context.NONE);
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

### InvoiceSections_Get

```java
/** Samples for InvoiceSections Get. */
public final class InvoiceSectionsGetSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/InvoiceSection.json
     */
    /**
     * Sample code: InvoiceSection.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void invoiceSection(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .invoiceSections()
            .getWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                com.azure.core.util.Context.NONE);
    }
}
```

### InvoiceSections_ListByBillingProfile

```java
/** Samples for InvoiceSections ListByBillingProfile. */
public final class InvoiceSectionsListByBillingProfileSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/InvoiceSectionsListByBillingProfile.json
     */
    /**
     * Sample code: InvoiceSectionsListByBillingProfile.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void invoiceSectionsListByBillingProfile(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .invoiceSections()
            .listByBillingProfile("{billingAccountName}", "{billingProfileName}", com.azure.core.util.Context.NONE);
    }
}
```

### Invoices_DownloadBillingSubscriptionInvoice

```java
/** Samples for Invoices DownloadBillingSubscriptionInvoice. */
public final class InvoicesDownloadBillingSubscriptionInvoiceSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingSubscriptionInvoiceDownload.json
     */
    /**
     * Sample code: BillingSubscriptionInvoiceDownload.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingSubscriptionInvoiceDownload(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .invoices()
            .downloadBillingSubscriptionInvoice("{invoiceName}", "DRS_12345", com.azure.core.util.Context.NONE);
    }
}
```

### Invoices_DownloadInvoice

```java
/** Samples for Invoices DownloadInvoice. */
public final class InvoicesDownloadInvoiceSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/ModernInvoiceDownload.json
     */
    /**
     * Sample code: InvoiceDownload.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void invoiceDownload(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .invoices()
            .downloadInvoice("{billingAccountName}", "{invoiceName}", "DRS_12345", com.azure.core.util.Context.NONE);
    }
}
```

### Invoices_DownloadMultipleBillingProfileInvoices

```java
import java.util.Arrays;

/** Samples for Invoices DownloadMultipleBillingProfileInvoices. */
public final class InvoicesDownloadMultipleBillingProfileInvoicesSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/MultipleModernInvoiceDownload.json
     */
    /**
     * Sample code: BillingProfileInvoiceDownload.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingProfileInvoiceDownload(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .invoices()
            .downloadMultipleBillingProfileInvoices(
                "{billingAccountName}",
                Arrays
                    .asList(
                        "https://management.azure.com/providers/Microsoft.Billing/billingAccounts/{billingAccountName}/invoices/{invoiceName}/download?downloadToken={downloadToken}&useCache=True&api-version=2020-05-01",
                        "https://management.azure.com/providers/Microsoft.Billing/billingAccounts/{billingAccountName}/invoices/{invoiceName}/download?downloadToken={downloadToken}&useCache=True&api-version=2020-05-01",
                        "https://management.azure.com/providers/Microsoft.Billing/billingAccounts/{billingAccountName}/invoices/{invoiceName}/download?downloadToken={downloadToken}&useCache=True&api-version=2020-05-01"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Invoices_DownloadMultipleBillingSubscriptionInvoices

```java
import java.util.Arrays;

/** Samples for Invoices DownloadMultipleBillingSubscriptionInvoices. */
public final class InvoicesDownloadMultipleBillingSubscriptionInvoicesSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/MultipleBillingSubscriptionInvoiceDownload.json
     */
    /**
     * Sample code: BillingSubscriptionInvoiceDownload.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingSubscriptionInvoiceDownload(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .invoices()
            .downloadMultipleBillingSubscriptionInvoices(
                Arrays
                    .asList(
                        "https://management.azure.com/providers/Microsoft.Billing/billingAccounts/default/billingSubscriptions/{subscriptionId}/invoices/{invoiceName}/download?downloadToken={downloadToken}&useCache=True&api-version=2020-05-01",
                        "https://management.azure.com/providers/Microsoft.Billing/billingAccounts/default/billingSubscriptions/{subscriptionId}/invoices/{invoiceName}/download?downloadToken={downloadToken}&useCache=True&api-version=2020-05-01",
                        "https://management.azure.com/providers/Microsoft.Billing/billingAccounts/default/billingSubscriptions/{subscriptionId}/invoices/{invoiceName}/download?downloadToken={downloadToken}&useCache=True&api-version=2020-05-01"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Invoices_Get

```java
/** Samples for Invoices Get. */
public final class InvoicesGetSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/Invoice.json
     */
    /**
     * Sample code: Invoice.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void invoice(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.invoices().getWithResponse("{billingAccountName}", "{invoiceName}", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/CreditNote.json
     */
    /**
     * Sample code: CreditNote.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void creditNote(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.invoices().getWithResponse("{billingAccountName}", "{invoiceName}", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/VoidInvoice.json
     */
    /**
     * Sample code: VoidInvoice.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void voidInvoice(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.invoices().getWithResponse("{billingAccountName}", "{invoiceName}", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/InvoiceWithRebillDetails.json
     */
    /**
     * Sample code: InvoiceWithRebillDetails.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void invoiceWithRebillDetails(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.invoices().getWithResponse("{billingAccountName}", "{invoiceName}", com.azure.core.util.Context.NONE);
    }
}
```

### Invoices_GetById

```java
/** Samples for Invoices GetById. */
public final class InvoicesGetByIdSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/InvoiceById.json
     */
    /**
     * Sample code: Invoice.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void invoice(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.invoices().getByIdWithResponse("{invoiceName}", com.azure.core.util.Context.NONE);
    }
}
```

### Invoices_GetBySubscriptionAndInvoiceId

```java
/** Samples for Invoices GetBySubscriptionAndInvoiceId. */
public final class InvoicesGetBySubscriptionAndInvoiceIdSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingSubscriptionInvoice.json
     */
    /**
     * Sample code: BillingSubscriptionsListByBillingAccount.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingSubscriptionsListByBillingAccount(
        com.azure.resourcemanager.billing.BillingManager manager) {
        manager.invoices().getBySubscriptionAndInvoiceIdWithResponse("{invoiceName}", com.azure.core.util.Context.NONE);
    }
}
```

### Invoices_ListByBillingAccount

```java
/** Samples for Invoices ListByBillingAccount. */
public final class InvoicesListByBillingAccountSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingAccountInvoicesList.json
     */
    /**
     * Sample code: BillingAccountInvoicesList.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingAccountInvoicesList(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .invoices()
            .listByBillingAccount("{billingAccountName}", "2018-01-01", "2018-06-30", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingAccountInvoicesListWithRebillDetails.json
     */
    /**
     * Sample code: BillingAccountInvoicesListWithRebillDetails.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingAccountInvoicesListWithRebillDetails(
        com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .invoices()
            .listByBillingAccount("{billingAccountName}", "2018-01-01", "2018-06-30", com.azure.core.util.Context.NONE);
    }
}
```

### Invoices_ListByBillingProfile

```java
/** Samples for Invoices ListByBillingProfile. */
public final class InvoicesListByBillingProfileSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/InvoicesListByBillingProfile.json
     */
    /**
     * Sample code: InvoicesListByBillingProfile.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void invoicesListByBillingProfile(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .invoices()
            .listByBillingProfile(
                "{billingAccountName}",
                "{billingProfileName}",
                "2018-01-01",
                "2018-06-30",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/InvoicesListByBillingProfileWithRebillDetails.json
     */
    /**
     * Sample code: InvoicesListByBillingProfileWithRebillDetails.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void invoicesListByBillingProfileWithRebillDetails(
        com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .invoices()
            .listByBillingProfile(
                "{billingAccountName}",
                "{billingProfileName}",
                "2018-01-01",
                "2018-06-30",
                com.azure.core.util.Context.NONE);
    }
}
```

### Invoices_ListByBillingSubscription

```java
/** Samples for Invoices ListByBillingSubscription. */
public final class InvoicesListByBillingSubscriptionSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/BillingSubscriptionInvoicesList.json
     */
    /**
     * Sample code: BillingSubscriptionsListByBillingAccount.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingSubscriptionsListByBillingAccount(
        com.azure.resourcemanager.billing.BillingManager manager) {
        manager.invoices().listByBillingSubscription("2022-01-01", "2022-06-30", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/GetOperations.json
     */
    /**
     * Sample code: BillingAccountPermissionsList.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void billingAccountPermissionsList(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Policies_GetByBillingProfile

```java
/** Samples for Policies GetByBillingProfile. */
public final class PoliciesGetByBillingProfileSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/Policy.json
     */
    /**
     * Sample code: PolicyByBillingProfile.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void policyByBillingProfile(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .policies()
            .getByBillingProfileWithResponse(
                "{billingAccountName}", "{billingProfileName}", com.azure.core.util.Context.NONE);
    }
}
```

### Policies_GetByCustomer

```java
/** Samples for Policies GetByCustomer. */
public final class PoliciesGetByCustomerSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/CustomerPolicy.json
     */
    /**
     * Sample code: PolicyByCustomer.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void policyByCustomer(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .policies()
            .getByCustomerWithResponse("{billingAccountName}", "{customerName}", com.azure.core.util.Context.NONE);
    }
}
```

### Policies_Update

```java
import com.azure.resourcemanager.billing.fluent.models.PolicyInner;
import com.azure.resourcemanager.billing.models.MarketplacePurchasesPolicy;
import com.azure.resourcemanager.billing.models.ReservationPurchasesPolicy;
import com.azure.resourcemanager.billing.models.ViewChargesPolicy;

/** Samples for Policies Update. */
public final class PoliciesUpdateSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/UpdatePolicy.json
     */
    /**
     * Sample code: UpdatePolicy.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void updatePolicy(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .policies()
            .updateWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                new PolicyInner()
                    .withMarketplacePurchases(MarketplacePurchasesPolicy.ONLY_FREE_ALLOWED)
                    .withReservationPurchases(ReservationPurchasesPolicy.NOT_ALLOWED)
                    .withViewCharges(ViewChargesPolicy.ALLOWED),
                com.azure.core.util.Context.NONE);
    }
}
```

### Policies_UpdateCustomer

```java
import com.azure.resourcemanager.billing.fluent.models.CustomerPolicyInner;
import com.azure.resourcemanager.billing.models.ViewCharges;

/** Samples for Policies UpdateCustomer. */
public final class PoliciesUpdateCustomerSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/UpdateCustomerPolicy.json
     */
    /**
     * Sample code: UpdateCustomer.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void updateCustomer(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .policies()
            .updateCustomerWithResponse(
                "{billingAccountName}",
                "{customerName}",
                new CustomerPolicyInner().withViewCharges(ViewCharges.NOT_ALLOWED),
                com.azure.core.util.Context.NONE);
    }
}
```

### Products_Get

```java
/** Samples for Products Get. */
public final class ProductsGetSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/Product.json
     */
    /**
     * Sample code: Product.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void product(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.products().getWithResponse("{billingAccountName}", "{productName}", com.azure.core.util.Context.NONE);
    }
}
```

### Products_ListByBillingAccount

```java
/** Samples for Products ListByBillingAccount. */
public final class ProductsListByBillingAccountSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/ProductsListByBillingAccount.json
     */
    /**
     * Sample code: ProductsListByBillingAccount.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void productsListByBillingAccount(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.products().listByBillingAccount("{billingAccountName}", null, com.azure.core.util.Context.NONE);
    }
}
```

### Products_ListByBillingProfile

```java
/** Samples for Products ListByBillingProfile. */
public final class ProductsListByBillingProfileSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/ProductsListByBillingProfile.json
     */
    /**
     * Sample code: ProductsListByBillingProfile.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void productsListByBillingProfile(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .products()
            .listByBillingProfile(
                "{billingAccountName}", "{billingProfileName}", null, com.azure.core.util.Context.NONE);
    }
}
```

### Products_ListByCustomer

```java
/** Samples for Products ListByCustomer. */
public final class ProductsListByCustomerSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/ProductsListByCustomer.json
     */
    /**
     * Sample code: ProductsListByInvoiceSection.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void productsListByInvoiceSection(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.products().listByCustomer("{billingAccountName}", "{customerName}", com.azure.core.util.Context.NONE);
    }
}
```

### Products_ListByInvoiceSection

```java
/** Samples for Products ListByInvoiceSection. */
public final class ProductsListByInvoiceSectionSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/ProductsListByInvoiceSection.json
     */
    /**
     * Sample code: ProductsListByInvoiceSection.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void productsListByInvoiceSection(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .products()
            .listByInvoiceSection(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Products_Move

```java
import com.azure.resourcemanager.billing.models.TransferProductRequestProperties;

/** Samples for Products Move. */
public final class ProductsMoveSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/MoveProduct.json
     */
    /**
     * Sample code: MoveProduct.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void moveProduct(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .products()
            .moveWithResponse(
                "{billingAccountName}",
                "{productName}",
                new TransferProductRequestProperties()
                    .withDestinationInvoiceSectionId(
                        "/providers/Microsoft.Billing/billingAccounts/{billingAccountName}/billingProfiles/{billingProfileName}/invoiceSections/{newInvoiceSectionName}"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Products_Update

```java
import com.azure.resourcemanager.billing.fluent.models.ProductInner;
import com.azure.resourcemanager.billing.models.AutoRenew;

/** Samples for Products Update. */
public final class ProductsUpdateSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/UpdateProduct.json
     */
    /**
     * Sample code: UpdateBillingProperty.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void updateBillingProperty(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .products()
            .updateWithResponse(
                "{billingAccountName}",
                "{productName}",
                new ProductInner().withAutoRenew(AutoRenew.OFF),
                com.azure.core.util.Context.NONE);
    }
}
```

### Products_ValidateMove

```java
import com.azure.resourcemanager.billing.models.TransferProductRequestProperties;

/** Samples for Products ValidateMove. */
public final class ProductsValidateMoveSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/ValidateProductMoveFailure.json
     */
    /**
     * Sample code: SubscriptionMoveValidateFailure.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void subscriptionMoveValidateFailure(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .products()
            .validateMoveWithResponse(
                "{billingAccountName}",
                "{productName}",
                new TransferProductRequestProperties()
                    .withDestinationInvoiceSectionId(
                        "/providers/Microsoft.Billing/billingAccounts/{billingAccountName}/billingProfiles/{billingProfileName}/invoiceSections/{newInvoiceSectionName}"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/ValidateProductMoveSuccess.json
     */
    /**
     * Sample code: SubscriptionMoveValidateSuccess.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void subscriptionMoveValidateSuccess(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .products()
            .validateMoveWithResponse(
                "{billingAccountName}",
                "{productName}",
                new TransferProductRequestProperties()
                    .withDestinationInvoiceSectionId(
                        "/providers/Microsoft.Billing/billingAccounts/{billingAccountName}/billingProfiles/{billingProfileName}/invoiceSections/{newInvoiceSectionName}"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Reservations_ListByBillingAccount

```java
/** Samples for Reservations ListByBillingAccount. */
public final class ReservationsListByBillingAccountSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/ReservationsListByBillingAccount.json
     */
    /**
     * Sample code: ReservationsListByBillingAccount.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void reservationsListByBillingAccount(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .reservations()
            .listByBillingAccount(
                "{billingAccountName}",
                "properties/reservedResourceType eq 'VirtualMachines'",
                "properties/userFriendlyAppliedScopeType asc",
                "true",
                "Succeeded",
                com.azure.core.util.Context.NONE);
    }
}
```

### Reservations_ListByBillingProfile

```java
/** Samples for Reservations ListByBillingProfile. */
public final class ReservationsListByBillingProfileSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/ReservationsListByBillingProfile.json
     */
    /**
     * Sample code: ReservationsListByBillingProfile.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void reservationsListByBillingProfile(com.azure.resourcemanager.billing.BillingManager manager) {
        manager
            .reservations()
            .listByBillingProfile(
                "{billingAccountName}",
                "{billingProfileName}",
                "properties/reservedResourceType eq 'VirtualMachines'",
                "properties/userFriendlyAppliedScopeType asc",
                "true",
                "Succeeded",
                com.azure.core.util.Context.NONE);
    }
}
```

### Transactions_ListByInvoice

```java
/** Samples for Transactions ListByInvoice. */
public final class TransactionsListByInvoiceSamples {
    /*
     * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/TransactionsListByInvoice.json
     */
    /**
     * Sample code: TransactionsListByInvoice.
     *
     * @param manager Entry point to BillingManager.
     */
    public static void transactionsListByInvoice(com.azure.resourcemanager.billing.BillingManager manager) {
        manager.transactions().listByInvoice("{billingAccountName}", "{invoiceName}", com.azure.core.util.Context.NONE);
    }
}
```

