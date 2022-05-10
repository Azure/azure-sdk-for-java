# Code snippets and samples


## Approve

- [Invite](#approve_invite)

## Create

- [Lab](#create_lab)
- [Student](#create_student)

## Delete

- [Lab](#delete_lab)
- [Student](#delete_student)

## Deny

- [Invite](#deny_invite)

## Generate

- [InviteCode](#generate_invitecode)

## Get

- [Grant](#get_grant)
- [Lab](#get_lab)
- [Student](#get_student)
- [StudentLab](#get_studentlab)

## GrantOperation

- [List](#grantoperation_list)

## Grants

- [List](#grants_list)

## JoinRequestOperation

- [Get](#joinrequestoperation_get)

## JoinRequests

- [List](#joinrequests_list)

## LabOperation

- [List](#laboperation_list)

## Labs

- [List](#labs_list)

## Operations

- [List](#operations_list)

## Redeem

- [Invite](#redeem_invite)

## StudentLabs

- [List](#studentlabs_list)

## Students

- [List](#students_list)
### Approve_Invite

```java
import com.azure.core.util.Context;

/** Samples for Approve Invite. */
public final class ApproveInviteSamples {
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
            .approves()
            .inviteWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                "{joinRequestName}",
                Context.NONE);
    }
}
```

### Create_Lab

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.education.fluent.models.Amount;
import com.azure.resourcemanager.education.fluent.models.LabDetailsInner;
import java.time.OffsetDateTime;

/** Samples for Create Lab. */
public final class CreateLabSamples {
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
            .creates()
            .labWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                new LabDetailsInner()
                    .withDisplayName("example lab")
                    .withBudgetPerStudent(new Amount().withCurrency("USD").withValue(100.0f))
                    .withDescription("example lab description")
                    .withExpirationDate(OffsetDateTime.parse("2021-12-09T22:11:29.422Z")),
                Context.NONE);
    }
}
```

### Create_Student

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.education.fluent.models.Amount;
import com.azure.resourcemanager.education.fluent.models.StudentDetailsInner;
import com.azure.resourcemanager.education.models.StudentRole;
import java.time.OffsetDateTime;

/** Samples for Create Student. */
public final class CreateStudentSamples {
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
            .creates()
            .studentWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                "{studentAlias}",
                new StudentDetailsInner()
                    .withFirstName("test")
                    .withLastName("user")
                    .withEmail("test@contoso.com")
                    .withRole(StudentRole.STUDENT)
                    .withBudget(new Amount().withCurrency("USD").withValue(100.0f))
                    .withExpirationDate(OffsetDateTime.parse("2021-11-09T22:13:21.795Z")),
                Context.NONE);
    }
}
```

### Delete_Lab

```java
import com.azure.core.util.Context;

/** Samples for Delete Lab. */
public final class DeleteLabSamples {
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
            .deletes()
            .labWithResponse("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", Context.NONE);
    }
}
```

### Delete_Student

```java
import com.azure.core.util.Context;

/** Samples for Delete Student. */
public final class DeleteStudentSamples {
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
            .deletes()
            .studentWithResponse(
                "{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", "{studentAlias}", Context.NONE);
    }
}
```

### Deny_Invite

```java
import com.azure.core.util.Context;

/** Samples for Deny Invite. */
public final class DenyInviteSamples {
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
            .denies()
            .inviteWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                "{joinRequestName}",
                Context.NONE);
    }
}
```

### Generate_InviteCode

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.education.models.InviteCodeGenerateRequest;

/** Samples for Generate InviteCode. */
public final class GenerateInviteCodeSamples {
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
            .generates()
            .inviteCodeWithResponse(
                "{billingAccountName}",
                "{billingProfileName}",
                "{invoiceSectionName}",
                new InviteCodeGenerateRequest().withMaxStudentCount(10.0f),
                null,
                Context.NONE);
    }
}
```

### Get_Grant

```java
import com.azure.core.util.Context;

/** Samples for Get Grant. */
public final class GetGrantSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/Grant.json
     */
    /**
     * Sample code: Grant.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void grant(com.azure.resourcemanager.education.EducationManager manager) {
        manager.gets().grantWithResponse("{billingAccountName}", "{billingProfileName}", false, Context.NONE);
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
        manager.gets().grantWithResponse("{billingAccountName}", "{billingProfileName}", false, Context.NONE);
    }
}
```

### Get_Lab

```java
import com.azure.core.util.Context;

/** Samples for Get Lab. */
public final class GetLabSamples {
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
            .gets()
            .labWithResponse(
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
            .gets()
            .labWithResponse(
                "{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", false, Context.NONE);
    }
}
```

### Get_Student

```java
import com.azure.core.util.Context;

/** Samples for Get Student. */
public final class GetStudentSamples {
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
            .gets()
            .studentWithResponse(
                "{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", "{studentAlias}", Context.NONE);
    }
}
```

### Get_StudentLab

```java
import com.azure.core.util.Context;

/** Samples for Get StudentLab. */
public final class GetStudentLabSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/StudentLab.json
     */
    /**
     * Sample code: StudentLab.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void studentLab(com.azure.resourcemanager.education.EducationManager manager) {
        manager.gets().studentLabWithResponse("{studentLabName}", Context.NONE);
    }
}
```

### GrantOperation_List

```java
import com.azure.core.util.Context;

/** Samples for GrantOperation List. */
public final class GrantOperationListSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/GrantDefaultListIncludeAllocatedBudget.json
     */
    /**
     * Sample code: GrantListIncludeAllocatedBudget.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void grantListIncludeAllocatedBudget(com.azure.resourcemanager.education.EducationManager manager) {
        manager.grantOperations().list("{billingAccountName}", "{billingProfileName}", false, Context.NONE);
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
        manager.grantOperations().list("{billingAccountName}", "{billingProfileName}", false, Context.NONE);
    }
}
```

### Grants_List

```java
import com.azure.core.util.Context;

/** Samples for Grants List. */
public final class GrantsListSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/GrantListIncludeAllocatedBudget.json
     */
    /**
     * Sample code: GrantListIncludeAllocatedBudget.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void grantListIncludeAllocatedBudget(com.azure.resourcemanager.education.EducationManager manager) {
        manager.grants().list(true, Context.NONE);
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
        manager.grants().list(false, Context.NONE);
    }
}
```

### JoinRequestOperation_Get

```java
import com.azure.core.util.Context;

/** Samples for JoinRequestOperation Get. */
public final class JoinRequestOperationGetSamples {
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
            .joinRequestOperations()
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

### LabOperation_List

```java
import com.azure.core.util.Context;

/** Samples for LabOperation List. */
public final class LabOperationListSamples {
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
        manager
            .labOperations()
            .list("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", true, Context.NONE);
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
        manager
            .labOperations()
            .list("{billingAccountName}", "{billingProfileName}", "{invoiceSectionName}", true, Context.NONE);
    }
}
```

### Labs_List

```java
import com.azure.core.util.Context;

/** Samples for Labs List. */
public final class LabsListSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/LabList.json
     */
    /**
     * Sample code: LabList.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void labList(com.azure.resourcemanager.education.EducationManager manager) {
        manager.labs().list("{billingAccountName}", "{billingProfileName}", false, null, Context.NONE);
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
        manager.labs().list("{billingAccountName}", "{billingProfileName}", true, null, Context.NONE);
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

### Redeem_Invite

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.education.models.RedeemRequest;

/** Samples for Redeem Invite. */
public final class RedeemInviteSamples {
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
            .redeems()
            .inviteWithResponse(
                new RedeemRequest().withRedeemCode("exampleRedeemCode").withFirstName("test").withLastName("user"),
                Context.NONE);
    }
}
```

### StudentLabs_List

```java
import com.azure.core.util.Context;

/** Samples for StudentLabs List. */
public final class StudentLabsListSamples {
    /*
     * x-ms-original-file: specification/education/resource-manager/Microsoft.Education/preview/2021-12-01-preview/examples/StudentLabList.json
     */
    /**
     * Sample code: StudentLabList.
     *
     * @param manager Entry point to EducationManager.
     */
    public static void studentLabList(com.azure.resourcemanager.education.EducationManager manager) {
        manager.studentLabs().list(Context.NONE);
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

