# Code snippets and samples


## Assessments

- [CreateOrUpdate](#assessments_createorupdate)
- [Delete](#assessments_delete)
- [Get](#assessments_get)
- [List](#assessments_list)
- [ListUploadToken](#assessments_listuploadtoken)

## Operations

- [List](#operations_list)

## Rules

- [CreateOrUpdate](#rules_createorupdate)
- [Get](#rules_get)
- [List](#rules_list)
- [Update](#rules_update)
### Assessments_CreateOrUpdate

```java
import com.azure.resourcemanager.billing.trust.fluent.models.AssessmentInner;
import com.azure.resourcemanager.billing.trust.models.AssessmentProperties;
import com.azure.resourcemanager.billing.trust.models.AssessmentType;
import com.azure.resourcemanager.billing.trust.models.DomainEntry;
import com.azure.resourcemanager.billing.trust.models.EduInitialValue;
import java.util.Arrays;

/**
 * Samples for Assessments CreateOrUpdate.
 */
public final class AssessmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-03-17-preview/Assessments_CreateOrUpdate_PayeeEnrollment.json
     */
    /**
     * Sample code: Create or update the PayeeEnrollment assessment for a billing account.
     * 
     * @param manager Entry point to BillingTrustManager.
     */
    public static void createOrUpdateThePayeeEnrollmentAssessmentForABillingAccount(
        com.azure.resourcemanager.billing.trust.BillingTrustManager manager) {
        manager.assessments()
            .createOrUpdate(
                "providers/Microsoft.Billing/billingAccounts/abc123:00000000-0000-0000-0000-000000000000_2019-05-31",
                new AssessmentInner()
                    .withProperties(new AssessmentProperties().withAssessmentType(AssessmentType.PAYEE_ENROLLMENT)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-03-17-preview/Assessments_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update the Edu assessment for an enrollment.
     * 
     * @param manager Entry point to BillingTrustManager.
     */
    public static void createOrUpdateTheEduAssessmentForAnEnrollment(
        com.azure.resourcemanager.billing.trust.BillingTrustManager manager) {
        manager.assessments()
            .createOrUpdate(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/billing-edu-rg/providers/Microsoft.Program/educationEnrollments/default",
                new AssessmentInner().withProperties(new AssessmentProperties().withAssessmentType(AssessmentType.EDU)
                    .withInitialValues(Arrays.asList(new EduInitialValue().withDomains(Arrays.asList(
                        new DomainEntry().withDomainNames(Arrays.asList("students.contoso.edu", "faculty.contoso.edu"))
                            .withTenantId("11111111-1111-1111-1111-111111111111")))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-03-17-preview/Assessments_CreateOrUpdate_BV.json
     */
    /**
     * Sample code: Create or update the BusinessVerification assessment for a billing account.
     * 
     * @param manager Entry point to BillingTrustManager.
     */
    public static void createOrUpdateTheBusinessVerificationAssessmentForABillingAccount(
        com.azure.resourcemanager.billing.trust.BillingTrustManager manager) {
        manager.assessments()
            .createOrUpdate(
                "providers/Microsoft.Billing/billingAccounts/abc123:00000000-0000-0000-0000-000000000000_2019-05-31",
                new AssessmentInner().withProperties(
                    new AssessmentProperties().withAssessmentType(AssessmentType.BUSINESS_VERIFICATION)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-03-17-preview/Assessments_CreateOrUpdate_PayeeProfile.json
     */
    /**
     * Sample code: Create or update the PayeeProfile assessment for a billing account.
     * 
     * @param manager Entry point to BillingTrustManager.
     */
    public static void createOrUpdateThePayeeProfileAssessmentForABillingAccount(
        com.azure.resourcemanager.billing.trust.BillingTrustManager manager) {
        manager.assessments()
            .createOrUpdate(
                "providers/Microsoft.Billing/billingAccounts/abc123:00000000-0000-0000-0000-000000000000_2019-05-31",
                new AssessmentInner()
                    .withProperties(new AssessmentProperties().withAssessmentType(AssessmentType.PAYEE_PROFILE)),
                com.azure.core.util.Context.NONE);
    }
}
```

### Assessments_Delete

```java
/**
 * Samples for Assessments Delete.
 */
public final class AssessmentsDeleteSamples {
    /*
     * x-ms-original-file: 2026-03-17-preview/Assessments_Delete.json
     */
    /**
     * Sample code: Delete the Edu assessment for an enrollment.
     * 
     * @param manager Entry point to BillingTrustManager.
     */
    public static void
        deleteTheEduAssessmentForAnEnrollment(com.azure.resourcemanager.billing.trust.BillingTrustManager manager) {
        manager.assessments()
            .delete(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/billing-edu-rg/providers/Microsoft.Program/educationEnrollments/default",
                com.azure.core.util.Context.NONE);
    }
}
```

### Assessments_Get

```java
/**
 * Samples for Assessments Get.
 */
public final class AssessmentsGetSamples {
    /*
     * x-ms-original-file: 2026-03-17-preview/Assessments_Get.json
     */
    /**
     * Sample code: Get the Edu assessment for an enrollment.
     * 
     * @param manager Entry point to BillingTrustManager.
     */
    public static void
        getTheEduAssessmentForAnEnrollment(com.azure.resourcemanager.billing.trust.BillingTrustManager manager) {
        manager.assessments()
            .getWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/billing-edu-rg/providers/Microsoft.Program/educationEnrollments/default",
                com.azure.core.util.Context.NONE);
    }
}
```

### Assessments_List

```java
/**
 * Samples for Assessments List.
 */
public final class AssessmentsListSamples {
    /*
     * x-ms-original-file: 2026-03-17-preview/Assessments_List.json
     */
    /**
     * Sample code: List assessments for an enrollment scope.
     * 
     * @param manager Entry point to BillingTrustManager.
     */
    public static void
        listAssessmentsForAnEnrollmentScope(com.azure.resourcemanager.billing.trust.BillingTrustManager manager) {
        manager.assessments()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/billing-edu-rg/providers/Microsoft.Program/educationEnrollments/default",
                com.azure.core.util.Context.NONE);
    }
}
```

### Assessments_ListUploadToken

```java
/**
 * Samples for Assessments ListUploadToken.
 */
public final class AssessmentsListUploadTokenSamples {
    /*
     * x-ms-original-file: 2026-03-17-preview/Assessments_ListUploadToken.json
     */
    /**
     * Sample code: List an upload token for supplemental documents.
     * 
     * @param manager Entry point to BillingTrustManager.
     */
    public static void
        listAnUploadTokenForSupplementalDocuments(com.azure.resourcemanager.billing.trust.BillingTrustManager manager) {
        manager.assessments()
            .listUploadTokenWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/billing-edu-rg/providers/Microsoft.Program/educationEnrollments/default",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-03-17-preview/Operations_List.json
     */
    /**
     * Sample code: List operations for Microsoft.BillingTrust.
     * 
     * @param manager Entry point to BillingTrustManager.
     */
    public static void
        listOperationsForMicrosoftBillingTrust(com.azure.resourcemanager.billing.trust.BillingTrustManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Rules_CreateOrUpdate

```java
import com.azure.resourcemanager.billing.trust.models.DomainEntry;
import com.azure.resourcemanager.billing.trust.models.EduQualificationRuleProperties;
import java.util.Arrays;

/**
 * Samples for Rules CreateOrUpdate.
 */
public final class RulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-03-17-preview/Rules_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update an eduQualification rule.
     * 
     * @param manager Entry point to BillingTrustManager.
     */
    public static void
        createOrUpdateAnEduQualificationRule(com.azure.resourcemanager.billing.trust.BillingTrustManager manager) {
        manager.rules()
            .define("Qualify_Edu")
            .withExistingResourceUri(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/billing-edu-rg/providers/Microsoft.Program/educationEnrollments/default")
            .withProperties(new EduQualificationRuleProperties().withDomains(Arrays
                .asList(new DomainEntry().withDomainNames(Arrays.asList("students.contoso.edu", "faculty.contoso.edu"))
                    .withTenantId("11111111-1111-1111-1111-111111111111"))))
            .create();
    }
}
```

### Rules_Get

```java
/**
 * Samples for Rules Get.
 */
public final class RulesGetSamples {
    /*
     * x-ms-original-file: 2026-03-17-preview/Rules_Get.json
     */
    /**
     * Sample code: Get an eduQualification rule.
     * 
     * @param manager Entry point to BillingTrustManager.
     */
    public static void getAnEduQualificationRule(com.azure.resourcemanager.billing.trust.BillingTrustManager manager) {
        manager.rules()
            .getWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/billing-edu-rg/providers/Microsoft.Program/educationEnrollments/default",
                "Qualify_Edu", com.azure.core.util.Context.NONE);
    }
}
```

### Rules_List

```java
/**
 * Samples for Rules List.
 */
public final class RulesListSamples {
    /*
     * x-ms-original-file: 2026-03-17-preview/Rules_List.json
     */
    /**
     * Sample code: List rules for the Edu assessment.
     * 
     * @param manager Entry point to BillingTrustManager.
     */
    public static void
        listRulesForTheEduAssessment(com.azure.resourcemanager.billing.trust.BillingTrustManager manager) {
        manager.rules()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/billing-edu-rg/providers/Microsoft.Program/educationEnrollments/default",
                com.azure.core.util.Context.NONE);
    }
}
```

### Rules_Update

```java
import com.azure.resourcemanager.billing.trust.models.Rule;

/**
 * Samples for Rules Update.
 */
public final class RulesUpdateSamples {
    /*
     * x-ms-original-file: 2026-03-17-preview/Rules_Update_BV.json
     */
    /**
     * Sample code: Patch a businessVerification rule with externalId when actionRequired (resolves AmbiguousMatch by
     * disambiguating with DUNS).
     * 
     * @param manager Entry point to BillingTrustManager.
     */
    public static void
        patchABusinessVerificationRuleWithExternalIdWhenActionRequiredResolvesAmbiguousMatchByDisambiguatingWithDUNS(
            com.azure.resourcemanager.billing.trust.BillingTrustManager manager) {
        Rule resource = manager.rules()
            .getWithResponse(
                "providers/Microsoft.Billing/billingAccounts/abc123:00000000-0000-0000-0000-000000000000_2019-05-31",
                "Verify_Business", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }

    /*
     * x-ms-original-file: 2026-03-17-preview/Rules_Update.json
     */
    /**
     * Sample code: Update an eduQualification rule with supplemental documents when actionRequired.
     * 
     * @param manager Entry point to BillingTrustManager.
     */
    public static void updateAnEduQualificationRuleWithSupplementalDocumentsWhenActionRequired(
        com.azure.resourcemanager.billing.trust.BillingTrustManager manager) {
        Rule resource = manager.rules()
            .getWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/billing-edu-rg/providers/Microsoft.Program/educationEnrollments/default",
                "Qualify_Edu", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }
}
```

