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
import com.azure.core.util.Context;

/** Samples for Grants Get. */
public final class GrantsGetSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/Grant.json
     */
    /**
     * Sample code: Grant.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void grant(com.azure.resourcemanager.education.EducationManager manager) {
        manager.grants().getWithResponse("{billingAccountName}", "{billingProfileName}", false, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/GrantIncludeAllocatedBudget.json
     */
    /**
     * Sample code: GrantIncludeAllocatedBudget.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void grantIncludeAllocatedBudget(com.azure.resourcemanager.education.EducationManager manager) {
        manager.grants().getWithResponse("{billingAccountName}", "{billingProfileName}", false, Context.NONE);
    }
}
```

### Grants_List

```java
import com.azure.core.util.Context;

/** Samples for Grants List. */
public final class GrantsListSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/GrantDefaultListIncludeAllocatedBudget.json
     */
    /**
     * Sample code: GrantListIncludeAllocatedBudget.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void grantListIncludeAllocatedBudget(com.azure.resourcemanager.education.EducationManager manager) {
        manager.grants().list("{billingAccountName}", "{billingProfileName}", false, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/GrantDefaultList.json
     */
    /**
     * Sample code: GrantList.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void grantList(com.azure.resourcemanager.education.EducationManager manager) {
        manager.grants().list("{billingAccountName}", "{billingProfileName}", false, Context.NONE);
    }
}
```

### Grants_ListAll

```java
import com.azure.core.util.Context;

/** Samples for Grants ListAll. */
public final class GrantsListAllSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/GrantListIncludeAllocatedBudget.json
     */
    /**
     * Sample code: GrantListIncludeAllocatedBudget.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void grantListIncludeAllocatedBudget(com.azure.resourcemanager.education.EducationManager manager) {
        manager.grants().listAll(true, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/GrantList.json
     */
    /**
     * Sample code: GrantList.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void grantList(com.azure.resourcemanager.education.EducationManager manager) {
        manager.grants().listAll(false, Context.NONE);
    }
}
```

### JoinRequests_Approve

```java
import com.azure.core.util.Context;

/** Samples for JoinRequests Approve. */
public final class JoinRequestsApproveSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/JoinRequestApproveAndDeny.json
     */
    /**
     * Sample code: JoinRequestApprove.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void joinRequestApprove(com.azure.resourcemanager.education.EducationManager manager) {
        manager
            .joinRequests()
            .approveWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                "{joinRequestName}",
                Context.NONE);
    }
}
```

### JoinRequests_Deny

```java
import com.azure.core.util.Context;

/** Samples for JoinRequests Deny. */
public final class JoinRequestsDenySamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/JoinRequestApproveAndDeny.json
     */
    /**
     * Sample code: JoinRequestDeny.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void joinRequestDeny(com.azure.resourcemanager.education.EducationManager manager) {
        manager
            .joinRequests()
            .denyWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                "{joinRequestName}",
                Context.NONE);
    }
}
```

### JoinRequests_Get

```java
import com.azure.core.util.Context;

/** Samples for JoinRequests Get. */
public final class JoinRequestsGetSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/JoinRequest.json
     */
    /**
     * Sample code: JoinRequest.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void joinRequest(com.azure.resourcemanager.education.EducationManager manager) {
        manager
            .joinRequests()
            .getWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                "{joinRequestName}",
                Context.NONE);
    }
}
```

### JoinRequests_List

```java
import com.azure.core.util.Context;

/** Samples for JoinRequests List. */
public final class JoinRequestsListSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/JoinRequestList.json
     */
    /**
     * Sample code: JoinRequestList.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void joinRequestList(com.azure.resourcemanager.education.EducationManager manager) {
        manager
            .joinRequests()
            .list("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", false, Context.NONE);
    }
}
```

### Labs_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.education.fluent.models.Amount;
import com.azure.resourcemanager.education.fluent.models.LabDetailsInner;
import java.time.OffsetDateTime;

/** Samples for Labs CreateOrUpdate. */
public final class LabsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/CreateLab.json
     */
    /**
     * Sample code: CreateLab.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void createLab(com.azure.resourcemanager.education.EducationManager manager) {
        manager
            .labs()
            .createOrUpdateWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                new LabDetailsInner()
                    .withDisplayName("example lab")
                    .withBudgetPerStudent(new Amount().withCurrency("USD").withValue(100.0F))
                    .withDescription("example lab description")
                    .withExpirationDate(OffsetDateTime.parse("2021-12-09T22:11:29.422Z")),
                Context.NONE);
    }
}
```

### Labs_Delete

```java
import com.azure.core.util.Context;

/** Samples for Labs Delete. */
public final class LabsDeleteSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/DeleteLab.json
     */
    /**
     * Sample code: DeleteLab.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void deleteLab(com.azure.resourcemanager.education.EducationManager manager) {
        manager
            .labs()
            .deleteWithResponse("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", Context.NONE);
    }
}
```

### Labs_GenerateInviteCode

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.education.models.InviteCodeGenerateRequest;

/** Samples for Labs GenerateInviteCode. */
public final class LabsGenerateInviteCodeSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/GenerateInviteCode.json
     */
    /**
     * Sample code: CreateLab.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void createLab(com.azure.resourcemanager.education.EducationManager manager) {
        manager
            .labs()
            .generateInviteCodeWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                new InviteCodeGenerateRequest().withMaxStudentCount(10.0F),
                null,
                Context.NONE);
    }
}
```

### Labs_Get

```java
import com.azure.core.util.Context;

/** Samples for Labs Get. */
public final class LabsGetSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/LabIncludeBudget.json
     */
    /**
     * Sample code: LabIncludeBudget.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void labIncludeBudget(com.azure.resourcemanager.education.EducationManager manager) {
        manager
            .labs()
            .getWithResponse(
                "{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", true, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/Lab.json
     */
    /**
     * Sample code: Lab.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void lab(com.azure.resourcemanager.education.EducationManager manager) {
        manager
            .labs()
            .getWithResponse(
                "{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", false, Context.NONE);
    }
}
```

### Labs_List

```java
import com.azure.core.util.Context;

/** Samples for Labs List. */
public final class LabsListSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/LabListWithInvoiceSectionNameIncludeBudget.json
     */
    /**
     * Sample code: LabListWithInvoiceSectionNameIncludeBudget.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void labListWithInvoiceSectionNameIncludeBudget(
        com.azure.resourcemanager.education.EducationManager manager) {
        manager.labs().list("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", true, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/LabListWithInvoiceSectionName.json
     */
    /**
     * Sample code: LabListWithInvoiceSectionName.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void labListWithInvoiceSectionName(com.azure.resourcemanager.education.EducationManager manager) {
        manager.labs().list("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", true, Context.NONE);
    }
}
```

### Labs_ListAll

```java
import com.azure.core.util.Context;

/** Samples for Labs ListAll. */
public final class LabsListAllSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/LabList.json
     */
    /**
     * Sample code: LabList.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void labList(com.azure.resourcemanager.education.EducationManager manager) {
        manager.labs().listAll("{billingAccountName}", "{billingProfileName}", false, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/LabListIncludeBudget.json
     */
    /**
     * Sample code: LabListIncludeBudget.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void labListIncludeBudget(com.azure.resourcemanager.education.EducationManager manager) {
        manager.labs().listAll("{billingAccountName}", "{billingProfileName}", true, null, Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/GetOperations.json
     */
    /**
     * Sample code: GetOperations.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void getOperations(com.azure.resourcemanager.education.EducationManager manager) {
        manager.operations().listWithResponse(Context.NONE);
    }
}
```

### ResourceProvider_RedeemInvitationCode

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.education.models.RedeemRequest;

/** Samples for ResourceProvider RedeemInvitationCode. */
public final class ResourceProviderRedeemInvitationCodeSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/RedeemCode.json
     */
    /**
     * Sample code: RedeemCode.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void redeemCode(com.azure.resourcemanager.education.EducationManager manager) {
        manager
            .resourceProviders()
            .redeemInvitationCodeWithResponse(
                new RedeemRequest().withRedeemCode("exampleRedeemCode").withFirstName("test").withLastName("user"),
                Context.NONE);
    }
}
```

### StudentLabs_Get

```java
import com.azure.core.util.Context;

/** Samples for StudentLabs Get. */
public final class StudentLabsGetSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/StudentLab.json
     */
    /**
     * Sample code: StudentLab.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void studentLab(com.azure.resourcemanager.education.EducationManager manager) {
        manager.studentLabs().getWithResponse("{studentLabName}", Context.NONE);
    }
}
```

### StudentLabs_ListAll

```java
import com.azure.core.util.Context;

/** Samples for StudentLabs ListAll. */
public final class StudentLabsListAllSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/StudentLabList.json
     */
    /**
     * Sample code: StudentLabList.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void studentLabList(com.azure.resourcemanager.education.EducationManager manager) {
        manager.studentLabs().listAll(Context.NONE);
    }
}
```

### Students_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.education.fluent.models.Amount;
import com.azure.resourcemanager.education.fluent.models.StudentDetailsInner;
import com.azure.resourcemanager.education.models.StudentRole;
import java.time.OffsetDateTime;

/** Samples for Students CreateOrUpdate. */
public final class StudentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/CreateStudent.json
     */
    /**
     * Sample code: Student.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void student(com.azure.resourcemanager.education.EducationManager manager) {
        manager
            .students()
            .createOrUpdateWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                "{studentAlias}",
                new StudentDetailsInner()
                    .withFirstName("test")
                    .withLastName("user")
                    .withEmail("test@contoso.com")
                    .withRole(StudentRole.STUDENT)
                    .withBudget(new Amount().withCurrency("USD").withValue(100.0F))
                    .withExpirationDate(OffsetDateTime.parse("2021-11-09T22:13:21.795Z")),
                Context.NONE);
    }
}
```

### Students_Delete

```java
import com.azure.core.util.Context;

/** Samples for Students Delete. */
public final class StudentsDeleteSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/DeleteStudent.json
     */
    /**
     * Sample code: DeleteLab.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void deleteLab(com.azure.resourcemanager.education.EducationManager manager) {
        manager
            .students()
            .deleteWithResponse(
                "{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", "{studentAlias}", Context.NONE);
    }
}
```

### Students_Get

```java
import com.azure.core.util.Context;

/** Samples for Students Get. */
public final class StudentsGetSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/Student.json
     */
    /**
     * Sample code: Student.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void student(com.azure.resourcemanager.education.EducationManager manager) {
        manager
            .students()
            .getWithResponse(
                "{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", "{studentAlias}", Context.NONE);
    }
}
```

### Students_List

```java
import com.azure.core.util.Context;

/** Samples for Students List. */
public final class StudentsListSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/StudentList.json
     */
    /**
     * Sample code: StudentList.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void studentList(com.azure.resourcemanager.education.EducationManager manager) {
        manager
            .students()
            .list("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", null, Context.NONE);
    }
}
```

