# Code snippets and samples


## Communications

- [CheckNameAvailability](#communications_checknameavailability)
- [Create](#communications_create)
- [Get](#communications_get)
- [List](#communications_list)

## Operations

- [List](#operations_list)

## ProblemClassifications

- [Get](#problemclassifications_get)
- [List](#problemclassifications_list)

## Services

- [Get](#services_get)
- [List](#services_list)

## SupportTickets

- [CheckNameAvailability](#supporttickets_checknameavailability)
- [Create](#supporttickets_create)
- [Get](#supporttickets_get)
- [List](#supporttickets_list)
- [Update](#supporttickets_update)
### Communications_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.support.models.CheckNameAvailabilityInput;
import com.azure.resourcemanager.support.models.Type;

/** Samples for Communications CheckNameAvailability. */
public final class CommunicationsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CheckNameAvailabilityForSupportTicketCommunication.json
     */
    /**
     * Sample code: Checks whether name is available for Communication resource.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void checksWhetherNameIsAvailableForCommunicationResource(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .communications()
            .checkNameAvailabilityWithResponse(
                "testticket",
                new CheckNameAvailabilityInput().withName("sampleName").withType(Type.MICROSOFT_SUPPORT_COMMUNICATIONS),
                Context.NONE);
    }
}
```

### Communications_Create

```java
/** Samples for Communications Create. */
public final class CommunicationsCreateSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CreateSupportTicketCommunication.json
     */
    /**
     * Sample code: AddCommunicationToSubscriptionTicket.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void addCommunicationToSubscriptionTicket(com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .communications()
            .define("testcommunication")
            .withExistingSupportTicket("testticket")
            .withSender("user@contoso.com")
            .withSubject("This is a test message from a customer!")
            .withBody("This is a test message from a customer!")
            .create();
    }
}
```

### Communications_Get

```java
import com.azure.core.util.Context;

/** Samples for Communications Get. */
public final class CommunicationsGetSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/GetCommunicationDetailsForSubscriptionSupportTicket.json
     */
    /**
     * Sample code: Get communication details for a subscription support ticket.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void getCommunicationDetailsForASubscriptionSupportTicket(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.communications().getWithResponse("testticket", "testmessage", Context.NONE);
    }
}
```

### Communications_List

```java
import com.azure.core.util.Context;

/** Samples for Communications List. */
public final class CommunicationsListSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/ListCommunicationsForSubscriptionSupportTicket.json
     */
    /**
     * Sample code: List communications for a subscription support ticket.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void listCommunicationsForASubscriptionSupportTicket(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.communications().list("testticket", null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/ListWebCommunicationsForSubscriptionSupportTicket.json
     */
    /**
     * Sample code: List web communications for a subscription support ticket.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void listWebCommunicationsForASubscriptionSupportTicket(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.communications().list("testticket", null, "communicationType eq 'web'", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/ListWebCommunicationsForSubscriptionSupportTicketCreatedOnOrAfter.json
     */
    /**
     * Sample code: List web communication created on or after a specific date for a subscription support ticket.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void listWebCommunicationCreatedOnOrAfterASpecificDateForASubscriptionSupportTicket(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .communications()
            .list(
                "testticket", null, "communicationType eq 'web' and createdDate ge 2020-03-10T22:08:51Z", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/ListOperations.json
     */
    /**
     * Sample code: Get all operations.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void getAllOperations(com.azure.resourcemanager.support.SupportManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### ProblemClassifications_Get

```java
import com.azure.core.util.Context;

/** Samples for ProblemClassifications Get. */
public final class ProblemClassificationsGetSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/GetProblemClassification.json
     */
    /**
     * Sample code: Gets details of problemClassification for Azure service.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void getsDetailsOfProblemClassificationForAzureService(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.problemClassifications().getWithResponse("service_guid", "problemClassification_guid", Context.NONE);
    }
}
```

### ProblemClassifications_List

```java
import com.azure.core.util.Context;

/** Samples for ProblemClassifications List. */
public final class ProblemClassificationsListSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/ListProblemClassifications.json
     */
    /**
     * Sample code: Gets list of problemClassifications for a service for which a support ticket can be created.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void getsListOfProblemClassificationsForAServiceForWhichASupportTicketCanBeCreated(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.problemClassifications().list("service_guid", Context.NONE);
    }
}
```

### Services_Get

```java
import com.azure.core.util.Context;

/** Samples for Services Get. */
public final class ServicesGetSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/GetService.json
     */
    /**
     * Sample code: Gets details of the Azure service.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void getsDetailsOfTheAzureService(com.azure.resourcemanager.support.SupportManager manager) {
        manager.services().getWithResponse("service_guid", Context.NONE);
    }
}
```

### Services_List

```java
import com.azure.core.util.Context;

/** Samples for Services List. */
public final class ServicesListSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/ListServices.json
     */
    /**
     * Sample code: Gets list of services for which a support ticket can be created.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void getsListOfServicesForWhichASupportTicketCanBeCreated(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.services().list(Context.NONE);
    }
}
```

### SupportTickets_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.support.models.CheckNameAvailabilityInput;
import com.azure.resourcemanager.support.models.Type;

/** Samples for SupportTickets CheckNameAvailability. */
public final class SupportTicketsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CheckNameAvailabilityWithSubscription.json
     */
    /**
     * Sample code: Checks whether name is available for SupportTicket resource.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void checksWhetherNameIsAvailableForSupportTicketResource(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityInput()
                    .withName("sampleName")
                    .withType(Type.MICROSOFT_SUPPORT_SUPPORT_TICKETS),
                Context.NONE);
    }
}
```

### SupportTickets_Create

```java
import com.azure.resourcemanager.support.models.ContactProfile;
import com.azure.resourcemanager.support.models.PreferredContactMethod;
import com.azure.resourcemanager.support.models.QuotaChangeRequest;
import com.azure.resourcemanager.support.models.QuotaTicketDetails;
import com.azure.resourcemanager.support.models.SeverityLevel;
import com.azure.resourcemanager.support.models.TechnicalTicketDetails;
import java.util.Arrays;

/** Samples for SupportTickets Create. */
public final class SupportTicketsCreateSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CreateSqlDatawarehouseQuotaTicketForDTUs.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for DTUs for Azure Synapse Analytics.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForDTUsForAzureSynapseAnalytics(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/sql_datawarehouse_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withContactDetails(
                new ContactProfile()
                    .withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withQuotaTicketDetails(
                new QuotaTicketDetails()
                    .withQuotaChangeRequestSubType("DTUs")
                    .withQuotaChangeRequestVersion("1.0")
                    .withQuotaChangeRequests(
                        Arrays
                            .asList(
                                new QuotaChangeRequest()
                                    .withRegion("EastUS")
                                    .withPayload("{\"ServerName\":\"testserver\",\"NewLimit\":54000}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CreateBatchQuotaTicketForSpecificBatchAccountForActiveJobs.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for Active Jobs and Job Schedules for a Batch account.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForActiveJobsAndJobSchedulesForABatchAccount(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/batch_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withContactDetails(
                new ContactProfile()
                    .withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withQuotaTicketDetails(
                new QuotaTicketDetails()
                    .withQuotaChangeRequestSubType("Account")
                    .withQuotaChangeRequestVersion("1.0")
                    .withQuotaChangeRequests(
                        Arrays
                            .asList(
                                new QuotaChangeRequest()
                                    .withRegion("EastUS")
                                    .withPayload("{\"AccountName\":\"test\",\"NewLimit\":200,\"Type\":\"Jobs\"}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CreateMachineLearningQuotaTicketForLowPriorityCores.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for Low-priority cores for Machine Learning service.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForLowPriorityCoresForMachineLearningService(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/machine_learning_service_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withContactDetails(
                new ContactProfile()
                    .withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withQuotaTicketDetails(
                new QuotaTicketDetails()
                    .withQuotaChangeRequestSubType("BatchAml")
                    .withQuotaChangeRequestVersion("1.0")
                    .withQuotaChangeRequests(
                        Arrays
                            .asList(
                                new QuotaChangeRequest()
                                    .withRegion("EastUS")
                                    .withPayload("{\"NewLimit\":200,\"Type\":\"LowPriority\"}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CreateBatchQuotaTicketForSubscription.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for Batch accounts for a subscription.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForBatchAccountsForASubscription(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/batch_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withContactDetails(
                new ContactProfile()
                    .withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withQuotaTicketDetails(
                new QuotaTicketDetails()
                    .withQuotaChangeRequestSubType("Subscription")
                    .withQuotaChangeRequestVersion("1.0")
                    .withQuotaChangeRequests(
                        Arrays
                            .asList(
                                new QuotaChangeRequest()
                                    .withRegion("EastUS")
                                    .withPayload("{\"NewLimit\":200,\"Type\":\"Account\"}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CreateSqlDatabaseQuotaTicketForDTUs.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for DTUs for SQL Database.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForDTUsForSQLDatabase(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/sql_database_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withContactDetails(
                new ContactProfile()
                    .withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withQuotaTicketDetails(
                new QuotaTicketDetails()
                    .withQuotaChangeRequestSubType("DTUs")
                    .withQuotaChangeRequestVersion("1.0")
                    .withQuotaChangeRequests(
                        Arrays
                            .asList(
                                new QuotaChangeRequest()
                                    .withRegion("EastUS")
                                    .withPayload("{\"ServerName\":\"testserver\",\"NewLimit\":54000}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CreateGenericQuotaTicket.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for services that do not require additional details in the
     * quotaTicketDetails object.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void
        createATicketToRequestQuotaIncreaseForServicesThatDoNotRequireAdditionalDetailsInTheQuotaTicketDetailsObject(
            com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .define("testticket")
            .withDescription("Increase the maximum throughput per container limit to 10000 for account foo bar")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/cosmosdb_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withContactDetails(
                new ContactProfile()
                    .withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CreateBatchQuotaTicketForSpecificBatchAccountForLowPriorityCores.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for Low-priority cores for a Batch account.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForLowPriorityCoresForABatchAccount(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/batch_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withContactDetails(
                new ContactProfile()
                    .withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withQuotaTicketDetails(
                new QuotaTicketDetails()
                    .withQuotaChangeRequestSubType("Account")
                    .withQuotaChangeRequestVersion("1.0")
                    .withQuotaChangeRequests(
                        Arrays
                            .asList(
                                new QuotaChangeRequest()
                                    .withRegion("EastUS")
                                    .withPayload(
                                        "{\"AccountName\":\"test\",\"NewLimit\":200,\"Type\":\"LowPriority\"}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CreateSqlManagedInstanceQuotaTicket.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for Azure SQL managed instance.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForAzureSQLManagedInstance(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/sql_managedinstance_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withContactDetails(
                new ContactProfile()
                    .withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withQuotaTicketDetails(
                new QuotaTicketDetails()
                    .withQuotaChangeRequestSubType("SQLMI")
                    .withQuotaChangeRequestVersion("1.0")
                    .withQuotaChangeRequests(
                        Arrays
                            .asList(
                                new QuotaChangeRequest()
                                    .withRegion("EastUS")
                                    .withPayload("{\"NewLimit\":200, \"Metadata\":null, \"Type\":\"vCore\"}"),
                                new QuotaChangeRequest()
                                    .withRegion("EastUS")
                                    .withPayload("{\"NewLimit\":200, \"Metadata\":null, \"Type\":\"Subnet\"}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CreateBatchQuotaTicketForSpecificBatchAccountForPools.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for Pools for a Batch account.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForPoolsForABatchAccount(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/batch_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withContactDetails(
                new ContactProfile()
                    .withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withQuotaTicketDetails(
                new QuotaTicketDetails()
                    .withQuotaChangeRequestSubType("Account")
                    .withQuotaChangeRequestVersion("1.0")
                    .withQuotaChangeRequests(
                        Arrays
                            .asList(
                                new QuotaChangeRequest()
                                    .withRegion("EastUS")
                                    .withPayload("{\"AccountName\":\"test\",\"NewLimit\":200,\"Type\":\"Pools\"}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CreateBatchQuotaTicketForSpecificBatchAccountForDedicatedCores.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for specific VM family cores for a Batch account.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForSpecificVMFamilyCoresForABatchAccount(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/batch_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withContactDetails(
                new ContactProfile()
                    .withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withQuotaTicketDetails(
                new QuotaTicketDetails()
                    .withQuotaChangeRequestSubType("Account")
                    .withQuotaChangeRequestVersion("1.0")
                    .withQuotaChangeRequests(
                        Arrays
                            .asList(
                                new QuotaChangeRequest()
                                    .withRegion("EastUS")
                                    .withPayload(
                                        "{\"AccountName\":\"test\",\"VMFamily\":\"standardA0_A7Family\",\"NewLimit\":200,\"Type\":\"Dedicated\"}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CreateSqlDatabaseQuotaTicketForServers.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for Servers for SQL Database.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForServersForSQLDatabase(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/sql_database_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withContactDetails(
                new ContactProfile()
                    .withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withQuotaTicketDetails(
                new QuotaTicketDetails()
                    .withQuotaChangeRequestSubType("Servers")
                    .withQuotaChangeRequestVersion("1.0")
                    .withQuotaChangeRequests(
                        Arrays.asList(new QuotaChangeRequest().withRegion("EastUS").withPayload("{\"NewLimit\":200}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CreateBillingSupportTicketForSubscription.json
     */
    /**
     * Sample code: Create a ticket for Billing related issues.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketForBillingRelatedIssues(com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/billing_service_guid/problemClassifications/billing_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withContactDetails(
                new ContactProfile()
                    .withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/billing_service_guid")
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CreateSubMgmtSupportTicketForSubscription.json
     */
    /**
     * Sample code: Create a ticket for Subscription Management related issues.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketForSubscriptionManagementRelatedIssues(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/subscription_management_service_guid/problemClassifications/subscription_management_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withContactDetails(
                new ContactProfile()
                    .withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/subscription_management_service_guid")
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CreateTechnicalSupportTicketForSubscription.json
     */
    /**
     * Sample code: Create a ticket for Technical issue related to a specific resource.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketForTechnicalIssueRelatedToASpecificResource(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/virtual_machine_running_linux_service_guid/problemClassifications/problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withContactDetails(
                new ContactProfile()
                    .withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/cddd3eb5-1830-b494-44fd-782f691479dc")
            .withTechnicalTicketDetails(
                new TechnicalTicketDetails()
                    .withResourceId(
                        "/subscriptions/subid/resourceGroups/test/providers/Microsoft.Compute/virtualMachines/testserver"))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CreateMachineLearningQuotaTicketForDedicatedCores.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for specific VM family cores for Machine Learning service.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForSpecificVMFamilyCoresForMachineLearningService(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/machine_learning_service_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withContactDetails(
                new ContactProfile()
                    .withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withQuotaTicketDetails(
                new QuotaTicketDetails()
                    .withQuotaChangeRequestSubType("BatchAml")
                    .withQuotaChangeRequestVersion("1.0")
                    .withQuotaChangeRequests(
                        Arrays
                            .asList(
                                new QuotaChangeRequest()
                                    .withRegion("EastUS")
                                    .withPayload(
                                        "{\"VMFamily\":\"standardA0_A7Family\",\"NewLimit\":200,\"Type\":\"Dedicated\"}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CreateSqlDatawarehouseQuotaTicketForServers.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for Servers for Azure Synapse Analytics.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForServersForAzureSynapseAnalytics(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/sql_datawarehouse_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withContactDetails(
                new ContactProfile()
                    .withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withQuotaTicketDetails(
                new QuotaTicketDetails()
                    .withQuotaChangeRequestSubType("Servers")
                    .withQuotaChangeRequestVersion("1.0")
                    .withQuotaChangeRequests(
                        Arrays.asList(new QuotaChangeRequest().withRegion("EastUS").withPayload("{\"NewLimit\":200}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/CreateCoresQuotaTicketForSubscription.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for Compute VM Cores.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForComputeVMCores(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/cores_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withContactDetails(
                new ContactProfile()
                    .withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withQuotaTicketDetails(
                new QuotaTicketDetails()
                    .withQuotaChangeRequestVersion("1.0")
                    .withQuotaChangeRequests(
                        Arrays
                            .asList(
                                new QuotaChangeRequest()
                                    .withRegion("EastUS")
                                    .withPayload("{\"SKU\":\"DSv3 Series\",\"NewLimit\":104}"))))
            .create();
    }
}
```

### SupportTickets_Get

```java
import com.azure.core.util.Context;

/** Samples for SupportTickets Get. */
public final class SupportTicketsGetSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/GetSubscriptionSupportTicketDetails.json
     */
    /**
     * Sample code: Get details of a subscription ticket.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void getDetailsOfASubscriptionTicket(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets().getWithResponse("testticket", Context.NONE);
    }
}
```

### SupportTickets_List

```java
import com.azure.core.util.Context;

/** Samples for SupportTickets List. */
public final class SupportTicketsListSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/ListSupportTicketsServiceIdEqualsForSubscription.json
     */
    /**
     * Sample code: List support tickets with a certain service id for a subscription.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void listSupportTicketsWithACertainServiceIdForASubscription(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets().list(null, "ServiceId eq 'vm_windows_service_guid'", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/ListSupportTicketsCreatedOnOrAfterAndInOpenStateBySubscription.json
     */
    /**
     * Sample code: List support tickets created on or after a certain date and in open state for a subscription.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void listSupportTicketsCreatedOnOrAfterACertainDateAndInOpenStateForASubscription(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets().list(null, "createdDate ge 2020-03-10T22:08:51Z and status eq 'Open'", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/ListSupportTicketsProblemClassificationIdEqualsForSubscription.json
     */
    /**
     * Sample code: List support tickets with a certain problem classification id for a subscription.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void listSupportTicketsWithACertainProblemClassificationIdForASubscription(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager
            .supportTickets()
            .list(null, "ProblemClassificationId eq 'compute_vm_problemClassification_guid'", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/ListSupportTicketsBySubscription.json
     */
    /**
     * Sample code: List support tickets for a subscription.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void listSupportTicketsForASubscription(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets().list(null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/ListSupportTicketsInOpenStateBySubscription.json
     */
    /**
     * Sample code: List support tickets in open state for a subscription.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void listSupportTicketsInOpenStateForASubscription(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets().list(null, "status eq 'Open'", Context.NONE);
    }
}
```

### SupportTickets_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.support.models.PreferredContactMethod;
import com.azure.resourcemanager.support.models.SeverityLevel;
import com.azure.resourcemanager.support.models.Status;
import com.azure.resourcemanager.support.models.SupportTicketDetails;
import com.azure.resourcemanager.support.models.UpdateContactProfile;
import java.util.Arrays;

/** Samples for SupportTickets Update. */
public final class SupportTicketsUpdateSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/UpdateSeverityOfSupportTicketForSubscription.json
     */
    /**
     * Sample code: Update severity of a support ticket.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void updateSeverityOfASupportTicket(com.azure.resourcemanager.support.SupportManager manager) {
        SupportTicketDetails resource = manager.supportTickets().getWithResponse("testticket", Context.NONE).getValue();
        resource.update().withSeverity(SeverityLevel.CRITICAL).apply();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/UpdateStatusOfSupportTicketForSubscription.json
     */
    /**
     * Sample code: Update status of a support ticket.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void updateStatusOfASupportTicket(com.azure.resourcemanager.support.SupportManager manager) {
        SupportTicketDetails resource = manager.supportTickets().getWithResponse("testticket", Context.NONE).getValue();
        resource.update().withStatus(Status.CLOSED).apply();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2020-04-01/examples/UpdateContactDetailsOfSupportTicketForSubscription.json
     */
    /**
     * Sample code: Update contact details of a support ticket.
     *
     * @param manager Entry point to SupportManager.
     */
    public static void updateContactDetailsOfASupportTicket(com.azure.resourcemanager.support.SupportManager manager) {
        SupportTicketDetails resource = manager.supportTickets().getWithResponse("testticket", Context.NONE).getValue();
        resource
            .update()
            .withContactDetails(
                new UpdateContactProfile()
                    .withFirstName("first name")
                    .withLastName("last name")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("test.name@contoso.com")
                    .withAdditionalEmailAddresses(Arrays.asList("tname@contoso.com", "teamtest@contoso.com"))
                    .withPhoneNumber("123-456-7890")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("USA")
                    .withPreferredSupportLanguage("en-US"))
            .apply();
    }
}
```

