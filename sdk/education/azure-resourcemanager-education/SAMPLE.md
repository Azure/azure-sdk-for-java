# Code snippets and samples


## Grants

- [Get](#grants_get)
- [List](#grants_list)
- [ListAll](#grants_listall)

## JoinRequests

- [Approve](#joinrequests_approve)
- [Deny](#joinrequests_deny)
- [Get](#joinrequests_get)
- [List](#joinrequests_list)

## Labs

- [CreateOrUpdate](#labs_createorupdate)
- [Delete](#labs_delete)
- [GenerateInviteCode](#labs_generateinvitecode)
- [Get](#labs_get)
- [List](#labs_list)
- [ListAll](#labs_listall)

## Operations

- [List](#operations_list)

## ResourceProvider

- [RedeemInvitationCode](#resourceprovider_redeeminvitationcode)

## StudentLabs

- [Get](#studentlabs_get)
- [ListAll](#studentlabs_listall)

## Students

- [CreateOrUpdate](#students_createorupdate)
- [Delete](#students_delete)
- [Get](#students_get)
- [List](#students_list)
### Grants_Get

```java
/**
 * Samples for Grants Get.
 */
public final class GrantsGetSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/Grant.json
     */
    /**
     * Sample code: Grant.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void grant(com.azure.resourcemanager.education.EducationManager manager) {
        manager.grants()
            .getWithResponse("{billingAccountName}", "{billingProfileName}", false, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2021-12-01-preview/GrantIncludeAllocatedBudget.json
     */
    /**
     * Sample code: GrantIncludeAllocatedBudget.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void grantIncludeAllocatedBudget(com.azure.resourcemanager.education.EducationManager manager) {
        manager.grants()
            .getWithResponse("{billingAccountName}", "{billingProfileName}", false, com.azure.core.util.Context.NONE);
    }
}
```

### Grants_List

```java
/**
 * Samples for Grants List.
 */
public final class GrantsListSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/GrantDefaultListIncludeAllocatedBudget.json
     */
    /**
     * Sample code: GrantListIncludeAllocatedBudget.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void grantListIncludeAllocatedBudget(com.azure.resourcemanager.education.EducationManager manager) {
        manager.grants().list("{billingAccountName}", "{billingProfileName}", false, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2021-12-01-preview/GrantDefaultList.json
     */
    /**
     * Sample code: GrantList.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void grantList(com.azure.resourcemanager.education.EducationManager manager) {
        manager.grants().list("{billingAccountName}", "{billingProfileName}", false, com.azure.core.util.Context.NONE);
    }
}
```

### Grants_ListAll

```java
/**
 * Samples for Grants ListAll.
 */
public final class GrantsListAllSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/GrantListIncludeAllocatedBudget.json
     */
    /**
     * Sample code: GrantListIncludeAllocatedBudget.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void grantListIncludeAllocatedBudget(com.azure.resourcemanager.education.EducationManager manager) {
        manager.grants().listAll(true, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2021-12-01-preview/GrantList.json
     */
    /**
     * Sample code: GrantList.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void grantList(com.azure.resourcemanager.education.EducationManager manager) {
        manager.grants().listAll(false, com.azure.core.util.Context.NONE);
    }
}
```

### JoinRequests_Approve

```java
/**
 * Samples for JoinRequests Approve.
 */
public final class JoinRequestsApproveSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/JoinRequestApproveAndDenyForApprove.json
     */
    /**
     * Sample code: JoinRequestApprove.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void joinRequestApprove(com.azure.resourcemanager.education.EducationManager manager) {
        manager.joinRequests()
            .approveWithResponse("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}",
                "{joinRequestName}", com.azure.core.util.Context.NONE);
    }
}
```

### JoinRequests_Deny

```java
/**
 * Samples for JoinRequests Deny.
 */
public final class JoinRequestsDenySamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/JoinRequestApproveAndDeny.json
     */
    /**
     * Sample code: JoinRequestDeny.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void joinRequestDeny(com.azure.resourcemanager.education.EducationManager manager) {
        manager.joinRequests()
            .denyWithResponse("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}",
                "{joinRequestName}", com.azure.core.util.Context.NONE);
    }
}
```

### JoinRequests_Get

```java
/**
 * Samples for JoinRequests Get.
 */
public final class JoinRequestsGetSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/JoinRequest.json
     */
    /**
     * Sample code: JoinRequest.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void joinRequest(com.azure.resourcemanager.education.EducationManager manager) {
        manager.joinRequests()
            .getWithResponse("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}",
                "{joinRequestName}", com.azure.core.util.Context.NONE);
    }
}
```

### JoinRequests_List

```java
/**
 * Samples for JoinRequests List.
 */
public final class JoinRequestsListSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/JoinRequestList.json
     */
    /**
     * Sample code: JoinRequestList.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void joinRequestList(com.azure.resourcemanager.education.EducationManager manager) {
        manager.joinRequests()
            .list("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", false,
                com.azure.core.util.Context.NONE);
    }
}
```

### Labs_CreateOrUpdate

```java
import com.azure.resourcemanager.education.fluent.models.Amount;
import com.azure.resourcemanager.education.fluent.models.LabDetailsInner;
import java.time.OffsetDateTime;

/**
 * Samples for Labs CreateOrUpdate.
 */
public final class LabsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/CreateLab.json
     */
    /**
     * Sample code: CreateLab.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void createLab(com.azure.resourcemanager.education.EducationManager manager) {
        manager.labs()
            .createOrUpdateWithResponse("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}",
                new LabDetailsInner().withDisplayName("example lab")
                    .withBudgetPerStudent(new Amount().withCurrency("USD").withValue(100.0F))
                    .withDescription("example lab description")
                    .withExpirationDate(OffsetDateTime.parse("2021-12-09T22:11:29.422Z")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Labs_Delete

```java
/**
 * Samples for Labs Delete.
 */
public final class LabsDeleteSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/DeleteLab.json
     */
    /**
     * Sample code: DeleteLab.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void deleteLab(com.azure.resourcemanager.education.EducationManager manager) {
        manager.labs()
            .deleteWithResponse("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}",
                com.azure.core.util.Context.NONE);
    }
}
```

### Labs_GenerateInviteCode

```java
import com.azure.resourcemanager.education.models.InviteCodeGenerateRequest;

/**
 * Samples for Labs GenerateInviteCode.
 */
public final class LabsGenerateInviteCodeSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/GenerateInviteCode.json
     */
    /**
     * Sample code: CreateLab.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void createLab(com.azure.resourcemanager.education.EducationManager manager) {
        manager.labs()
            .generateInviteCodeWithResponse("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}",
                new InviteCodeGenerateRequest().withMaxStudentCount(10.0F), null, com.azure.core.util.Context.NONE);
    }
}
```

### Labs_Get

```java
/**
 * Samples for Labs Get.
 */
public final class LabsGetSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/LabIncludeBudget.json
     */
    /**
     * Sample code: LabIncludeBudget.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void labIncludeBudget(com.azure.resourcemanager.education.EducationManager manager) {
        manager.labs()
            .getWithResponse("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", true,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2021-12-01-preview/Lab.json
     */
    /**
     * Sample code: Lab.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void lab(com.azure.resourcemanager.education.EducationManager manager) {
        manager.labs()
            .getWithResponse("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", false,
                com.azure.core.util.Context.NONE);
    }
}
```

### Labs_List

```java
/**
 * Samples for Labs List.
 */
public final class LabsListSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/LabListWithInvoiceSectionNameIncludeBudget.json
     */
    /**
     * Sample code: LabListWithInvoiceSectionNameIncludeBudget.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void
        labListWithInvoiceSectionNameIncludeBudget(com.azure.resourcemanager.education.EducationManager manager) {
        manager.labs()
            .list("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", true,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2021-12-01-preview/LabListWithInvoiceSectionName.json
     */
    /**
     * Sample code: LabListWithInvoiceSectionName.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void labListWithInvoiceSectionName(com.azure.resourcemanager.education.EducationManager manager) {
        manager.labs()
            .list("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", true,
                com.azure.core.util.Context.NONE);
    }
}
```

### Labs_ListAll

```java
/**
 * Samples for Labs ListAll.
 */
public final class LabsListAllSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/LabList.json
     */
    /**
     * Sample code: LabList.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void labList(com.azure.resourcemanager.education.EducationManager manager) {
        manager.labs()
            .listAll("{billingAccountName}", "{billingProfileName}", false, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2021-12-01-preview/LabListIncludeBudget.json
     */
    /**
     * Sample code: LabListIncludeBudget.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void labListIncludeBudget(com.azure.resourcemanager.education.EducationManager manager) {
        manager.labs()
            .listAll("{billingAccountName}", "{billingProfileName}", true, null, com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2021-12-01-preview/GetOperations.json
     */
    /**
     * Sample code: GetOperations.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void getOperations(com.azure.resourcemanager.education.EducationManager manager) {
        manager.operations().listWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_RedeemInvitationCode

```java
import com.azure.resourcemanager.education.models.RedeemRequest;

/**
 * Samples for ResourceProvider RedeemInvitationCode.
 */
public final class ResourceProviderRedeemInvitationCodeSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/RedeemCode.json
     */
    /**
     * Sample code: RedeemCode.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void redeemCode(com.azure.resourcemanager.education.EducationManager manager) {
        manager.resourceProviders()
            .redeemInvitationCodeWithResponse(
                new RedeemRequest().withRedeemCode("fakeTokenPlaceholder").withFirstName("test").withLastName("user"),
                com.azure.core.util.Context.NONE);
    }
}
```

### StudentLabs_Get

```java
/**
 * Samples for StudentLabs Get.
 */
public final class StudentLabsGetSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/StudentLab.json
     */
    /**
     * Sample code: StudentLab.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void studentLab(com.azure.resourcemanager.education.EducationManager manager) {
        manager.studentLabs().getWithResponse("{studentLabName}", com.azure.core.util.Context.NONE);
    }
}
```

### StudentLabs_ListAll

```java
/**
 * Samples for StudentLabs ListAll.
 */
public final class StudentLabsListAllSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/StudentLabList.json
     */
    /**
     * Sample code: StudentLabList.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void studentLabList(com.azure.resourcemanager.education.EducationManager manager) {
        manager.studentLabs().listAll(com.azure.core.util.Context.NONE);
    }
}
```

### Students_CreateOrUpdate

```java
import com.azure.resourcemanager.education.fluent.models.Amount;
import com.azure.resourcemanager.education.fluent.models.StudentDetailsInner;
import com.azure.resourcemanager.education.models.StudentRole;
import java.time.OffsetDateTime;

/**
 * Samples for Students CreateOrUpdate.
 */
public final class StudentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/CreateStudent.json
     */
    /**
     * Sample code: Student.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void student(com.azure.resourcemanager.education.EducationManager manager) {
        manager.students()
            .createOrUpdateWithResponse("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}",
                "{studentAlias}",
                new StudentDetailsInner().withFirstName("test")
                    .withLastName("user")
                    .withEmail("test@contoso.com")
                    .withRole(StudentRole.STUDENT)
                    .withBudget(new Amount().withCurrency("USD").withValue(100.0F))
                    .withExpirationDate(OffsetDateTime.parse("2021-11-09T22:13:21.795Z")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Students_Delete

```java
/**
 * Samples for Students Delete.
 */
public final class StudentsDeleteSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/DeleteStudent.json
     */
    /**
     * Sample code: DeleteLab.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void deleteLab(com.azure.resourcemanager.education.EducationManager manager) {
        manager.students()
            .deleteWithResponse("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}",
                "{studentAlias}", com.azure.core.util.Context.NONE);
    }
}
```

### Students_Get

```java
/**
 * Samples for Students Get.
 */
public final class StudentsGetSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/Student.json
     */
    /**
     * Sample code: Student.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void student(com.azure.resourcemanager.education.EducationManager manager) {
        manager.students()
            .getWithResponse("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", "{studentAlias}",
                com.azure.core.util.Context.NONE);
    }
}
```

### Students_List

```java
/**
 * Samples for Students List.
 */
public final class StudentsListSamples {
    /*
     * x-ms-original-file: 2021-12-01-preview/StudentList.json
     */
    /**
     * Sample code: StudentList.
     * 
     * @param manager Entry point to EducationManager.
     */
    public static void studentList(com.azure.resourcemanager.education.EducationManager manager) {
        manager.students()
            .list("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", null,
                com.azure.core.util.Context.NONE);
    }
}
```

