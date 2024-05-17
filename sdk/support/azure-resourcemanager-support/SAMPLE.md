# Code snippets and samples


## ChatTranscripts

- [Get](#chattranscripts_get)
- [List](#chattranscripts_list)

## ChatTranscriptsNoSubscription

- [Get](#chattranscriptsnosubscription_get)
- [List](#chattranscriptsnosubscription_list)

## Communications

- [CheckNameAvailability](#communications_checknameavailability)
- [Create](#communications_create)
- [Get](#communications_get)
- [List](#communications_list)

## CommunicationsNoSubscription

- [CheckNameAvailability](#communicationsnosubscription_checknameavailability)
- [Create](#communicationsnosubscription_create)
- [Get](#communicationsnosubscription_get)
- [List](#communicationsnosubscription_list)

## FileWorkspaces

- [Create](#fileworkspaces_create)
- [Get](#fileworkspaces_get)

## FileWorkspacesNoSubscription

- [Create](#fileworkspacesnosubscription_create)
- [Get](#fileworkspacesnosubscription_get)

## Files

- [Create](#files_create)
- [Get](#files_get)
- [List](#files_list)
- [Upload](#files_upload)

## FilesNoSubscription

- [Create](#filesnosubscription_create)
- [Get](#filesnosubscription_get)
- [List](#filesnosubscription_list)
- [Upload](#filesnosubscription_upload)

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

## SupportTicketsNoSubscription

- [CheckNameAvailability](#supportticketsnosubscription_checknameavailability)
- [Create](#supportticketsnosubscription_create)
- [Get](#supportticketsnosubscription_get)
- [List](#supportticketsnosubscription_list)
- [Update](#supportticketsnosubscription_update)
### ChatTranscripts_Get

```java
/**
 * Samples for ChatTranscripts Get.
 */
public final class ChatTranscriptsGetSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/GetchatTranscriptDetailsForSubscriptionSupportTicket.json
     */
    /**
     * Sample code: Get chat transcript details for a subscription support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void getChatTranscriptDetailsForASubscriptionSupportTicket(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.chatTranscripts()
            .getWithResponse("testticket", "69586795-45e9-45b5-bd9e-c9bb237d3e44", com.azure.core.util.Context.NONE);
    }
}
```

### ChatTranscripts_List

```java
/**
 * Samples for ChatTranscripts List.
 */
public final class ChatTranscriptsListSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListChatTranscriptsForSubscriptionSupportTicket.json
     */
    /**
     * Sample code: List chat transcripts for a subscription support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        listChatTranscriptsForASubscriptionSupportTicket(com.azure.resourcemanager.support.SupportManager manager) {
        manager.chatTranscripts().list("testticket", com.azure.core.util.Context.NONE);
    }
}
```

### ChatTranscriptsNoSubscription_Get

```java
/**
 * Samples for ChatTranscriptsNoSubscription Get.
 */
public final class ChatTranscriptsNoSubscriptionGetSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/GetchatTranscriptDetailsForSupportTicket.json
     */
    /**
     * Sample code: Get chat transcript details for a subscription support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void getChatTranscriptDetailsForASubscriptionSupportTicket(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.chatTranscriptsNoSubscriptions()
            .getWithResponse("testticket", "b371192a-b094-4a71-b093-7246029b0a54", com.azure.core.util.Context.NONE);
    }
}
```

### ChatTranscriptsNoSubscription_List

```java
/**
 * Samples for ChatTranscriptsNoSubscription List.
 */
public final class ChatTranscriptsNoSubscriptionListSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListChatTranscriptsForSupportTicket.json
     */
    /**
     * Sample code: List chat transcripts for a no-subscription support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        listChatTranscriptsForANoSubscriptionSupportTicket(com.azure.resourcemanager.support.SupportManager manager) {
        manager.chatTranscriptsNoSubscriptions().list("testticket", com.azure.core.util.Context.NONE);
    }
}
```

### Communications_CheckNameAvailability

```java
import com.azure.resourcemanager.support.models.CheckNameAvailabilityInput;
import com.azure.resourcemanager.support.models.Type;

/**
 * Samples for Communications CheckNameAvailability.
 */
public final class CommunicationsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CheckNameAvailabilityForSupportTicketCommunication.json
     */
    /**
     * Sample code: Checks whether name is available for Communication resource for a subscription support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void checksWhetherNameIsAvailableForCommunicationResourceForASubscriptionSupportTicket(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.communications()
            .checkNameAvailabilityWithResponse("testticket",
                new CheckNameAvailabilityInput().withName("sampleName").withType(Type.MICROSOFT_SUPPORT_COMMUNICATIONS),
                com.azure.core.util.Context.NONE);
    }
}
```

### Communications_Create

```java
/**
 * Samples for Communications Create.
 */
public final class CommunicationsCreateSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateSupportTicketCommunication.json
     */
    /**
     * Sample code: AddCommunicationToSubscriptionTicket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void addCommunicationToSubscriptionTicket(com.azure.resourcemanager.support.SupportManager manager) {
        manager.communications()
            .define("testcommunication")
            .withExistingSupportTicket("testticket")
            .withSubject("This is a test message from a customer!")
            .withBody("This is a test message from a customer!")
            .withSender("user@contoso.com")
            .create();
    }
}
```

### Communications_Get

```java
/**
 * Samples for Communications Get.
 */
public final class CommunicationsGetSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/GetCommunicationDetailsForSubscriptionSupportTicket.json
     */
    /**
     * Sample code: Get communication details for a subscription support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        getCommunicationDetailsForASubscriptionSupportTicket(com.azure.resourcemanager.support.SupportManager manager) {
        manager.communications().getWithResponse("testticket", "testmessage", com.azure.core.util.Context.NONE);
    }
}
```

### Communications_List

```java
/**
 * Samples for Communications List.
 */
public final class CommunicationsListSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListCommunicationsForSubscriptionSupportTicket.json
     */
    /**
     * Sample code: List communications for a subscription support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        listCommunicationsForASubscriptionSupportTicket(com.azure.resourcemanager.support.SupportManager manager) {
        manager.communications().list("testticket", null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListWebCommunicationsForSubscriptionSupportTicket.json
     */
    /**
     * Sample code: List web communications for a subscription support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        listWebCommunicationsForASubscriptionSupportTicket(com.azure.resourcemanager.support.SupportManager manager) {
        manager.communications()
            .list("testticket", null, "communicationType eq 'web'", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListWebCommunicationsForSubscriptionSupportTicketCreatedOnOrAfter.json
     */
    /**
     * Sample code: List web communication created on or after a specific date for a subscription support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void listWebCommunicationCreatedOnOrAfterASpecificDateForASubscriptionSupportTicket(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.communications()
            .list("testticket", null, "communicationType eq 'web' and createdDate ge 2020-03-10T22:08:51Z",
                com.azure.core.util.Context.NONE);
    }
}
```

### CommunicationsNoSubscription_CheckNameAvailability

```java
import com.azure.resourcemanager.support.models.CheckNameAvailabilityInput;
import com.azure.resourcemanager.support.models.Type;

/**
 * Samples for CommunicationsNoSubscription CheckNameAvailability.
 */
public final class CommunicationsNoSubscriptionCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CheckNameAvailabilityForNoSubscriptionSupportTicketCommunication.json
     */
    /**
     * Sample code: Checks whether name is available for Communication resource.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        checksWhetherNameIsAvailableForCommunicationResource(com.azure.resourcemanager.support.SupportManager manager) {
        manager.communicationsNoSubscriptions()
            .checkNameAvailabilityWithResponse("testticket",
                new CheckNameAvailabilityInput().withName("sampleName").withType(Type.MICROSOFT_SUPPORT_COMMUNICATIONS),
                com.azure.core.util.Context.NONE);
    }
}
```

### CommunicationsNoSubscription_Create

```java
import com.azure.resourcemanager.support.fluent.models.CommunicationDetailsInner;

/**
 * Samples for CommunicationsNoSubscription Create.
 */
public final class CommunicationsNoSubscriptionCreateSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateNoSubscriptionSupportTicketCommunication.json
     */
    /**
     * Sample code: AddCommunicationToNoSubscriptionTicket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        addCommunicationToNoSubscriptionTicket(com.azure.resourcemanager.support.SupportManager manager) {
        manager.communicationsNoSubscriptions()
            .create("testticket", "testcommunication",
                new CommunicationDetailsInner().withSender("user@contoso.com")
                    .withSubject("This is a test message from a customer!")
                    .withBody("This is a test message from a customer!"),
                com.azure.core.util.Context.NONE);
    }
}
```

### CommunicationsNoSubscription_Get

```java
/**
 * Samples for CommunicationsNoSubscription Get.
 */
public final class CommunicationsNoSubscriptionGetSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/GetCommunicationDetailsForSupportTicket.json
     */
    /**
     * Sample code: Get communication details for a no-subscription support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void getCommunicationDetailsForANoSubscriptionSupportTicket(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.communicationsNoSubscriptions()
            .getWithResponse("testticket", "testmessage", com.azure.core.util.Context.NONE);
    }
}
```

### CommunicationsNoSubscription_List

```java
/**
 * Samples for CommunicationsNoSubscription List.
 */
public final class CommunicationsNoSubscriptionListSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListCommunicationsForSupportTicket.json
     */
    /**
     * Sample code: List communications for a no-subscription support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        listCommunicationsForANoSubscriptionSupportTicket(com.azure.resourcemanager.support.SupportManager manager) {
        manager.communicationsNoSubscriptions().list("testticket", null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListWebCommunicationsForSupportTicketCreatedOnOrAfter.json
     */
    /**
     * Sample code: List web communication created on or after a specific date for a no-subscription support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void listWebCommunicationCreatedOnOrAfterASpecificDateForANoSubscriptionSupportTicket(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.communicationsNoSubscriptions()
            .list("testticket", null, "communicationType eq 'web' and createdDate ge 2020-03-10T22:08:51Z",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListWebCommunicationsForSupportTicket.json
     */
    /**
     * Sample code: List web communications for a no-subscription support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        listWebCommunicationsForANoSubscriptionSupportTicket(com.azure.resourcemanager.support.SupportManager manager) {
        manager.communicationsNoSubscriptions()
            .list("testticket", null, "communicationType eq 'web'", com.azure.core.util.Context.NONE);
    }
}
```

### FileWorkspaces_Create

```java
/**
 * Samples for FileWorkspaces Create.
 */
public final class FileWorkspacesCreateSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateFileWorkspaceForSubscription.json
     */
    /**
     * Sample code: Create a file workspace for a subscription.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createAFileWorkspaceForASubscription(com.azure.resourcemanager.support.SupportManager manager) {
        manager.fileWorkspaces().createWithResponse("testworkspace", com.azure.core.util.Context.NONE);
    }
}
```

### FileWorkspaces_Get

```java
/**
 * Samples for FileWorkspaces Get.
 */
public final class FileWorkspacesGetSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/GetFileWorkspaceDetailsForSubscription.json
     */
    /**
     * Sample code: Get details of a subscription file workspace.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        getDetailsOfASubscriptionFileWorkspace(com.azure.resourcemanager.support.SupportManager manager) {
        manager.fileWorkspaces().getWithResponse("testworkspace", com.azure.core.util.Context.NONE);
    }
}
```

### FileWorkspacesNoSubscription_Create

```java
/**
 * Samples for FileWorkspacesNoSubscription Create.
 */
public final class FileWorkspacesNoSubscriptionCreateSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateFileWorkspace.json
     */
    /**
     * Sample code: Create a file workspace.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createAFileWorkspace(com.azure.resourcemanager.support.SupportManager manager) {
        manager.fileWorkspacesNoSubscriptions().createWithResponse("testworkspace", com.azure.core.util.Context.NONE);
    }
}
```

### FileWorkspacesNoSubscription_Get

```java
/**
 * Samples for FileWorkspacesNoSubscription Get.
 */
public final class FileWorkspacesNoSubscriptionGetSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/GetFileWorkspaceDetails.json
     */
    /**
     * Sample code: Get details of a file workspace.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void getDetailsOfAFileWorkspace(com.azure.resourcemanager.support.SupportManager manager) {
        manager.fileWorkspacesNoSubscriptions().getWithResponse("testworkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Files_Create

```java
/**
 * Samples for Files Create.
 */
public final class FilesCreateSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateFileForSubscription.json
     */
    /**
     * Sample code: Create a file under a subscription workspace.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        createAFileUnderASubscriptionWorkspace(com.azure.resourcemanager.support.SupportManager manager) {
        manager.files()
            .define("test.txt")
            .withExistingFileWorkspace("testworkspace")
            .withChunkSize(41423)
            .withFileSize(41423)
            .withNumberOfChunks(1)
            .create();
    }
}
```

### Files_Get

```java
/**
 * Samples for Files Get.
 */
public final class FilesGetSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/GetFileDetailsForSubscription.json
     */
    /**
     * Sample code: Get details of a subscription file.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void getDetailsOfASubscriptionFile(com.azure.resourcemanager.support.SupportManager manager) {
        manager.files().getWithResponse("testworkspace", "test.txt", com.azure.core.util.Context.NONE);
    }
}
```

### Files_List

```java
/**
 * Samples for Files List.
 */
public final class FilesListSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListFilesForSubscriptionUnderFileWorkspace.json
     */
    /**
     * Sample code: List files under a workspace for a subscription.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        listFilesUnderAWorkspaceForASubscription(com.azure.resourcemanager.support.SupportManager manager) {
        manager.files().list("testworkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Files_Upload

```java
import com.azure.resourcemanager.support.models.UploadFile;

/**
 * Samples for Files Upload.
 */
public final class FilesUploadSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/UploadFileForSubscription.json
     */
    /**
     * Sample code: UploadFileForSubscription.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void uploadFileForSubscription(com.azure.resourcemanager.support.SupportManager manager) {
        manager.files()
            .uploadWithResponse("testworkspaceName", "test.txt", new UploadFile().withContent(
                "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAABGdBTUEAALGPC/xhBQAAAAFzUkdCAK7OHOkAAAAgY0hSTQAAeiYAAICEAAD6AAAAgOgAAHUwAADqYAAAOpgAABd")
                .withChunkIndex(0), com.azure.core.util.Context.NONE);
    }
}
```

### FilesNoSubscription_Create

```java
import com.azure.resourcemanager.support.fluent.models.FileDetailsInner;

/**
 * Samples for FilesNoSubscription Create.
 */
public final class FilesNoSubscriptionCreateSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateFile.json
     */
    /**
     * Sample code: Create a file workspace.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createAFileWorkspace(com.azure.resourcemanager.support.SupportManager manager) {
        manager.filesNoSubscriptions()
            .createWithResponse("testworkspace", "test.txt",
                new FileDetailsInner().withChunkSize(41423).withFileSize(41423).withNumberOfChunks(1),
                com.azure.core.util.Context.NONE);
    }
}
```

### FilesNoSubscription_Get

```java
/**
 * Samples for FilesNoSubscription Get.
 */
public final class FilesNoSubscriptionGetSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/GetFileDetails.json
     */
    /**
     * Sample code: Get details of a subscription file.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void getDetailsOfASubscriptionFile(com.azure.resourcemanager.support.SupportManager manager) {
        manager.filesNoSubscriptions().getWithResponse("testworkspace", "test.txt", com.azure.core.util.Context.NONE);
    }
}
```

### FilesNoSubscription_List

```java
/**
 * Samples for FilesNoSubscription List.
 */
public final class FilesNoSubscriptionListSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListFilesUnderFileWorkspace.json
     */
    /**
     * Sample code: List files under a workspace.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void listFilesUnderAWorkspace(com.azure.resourcemanager.support.SupportManager manager) {
        manager.filesNoSubscriptions().list("testworkspace", com.azure.core.util.Context.NONE);
    }
}
```

### FilesNoSubscription_Upload

```java
import com.azure.resourcemanager.support.models.UploadFile;

/**
 * Samples for FilesNoSubscription Upload.
 */
public final class FilesNoSubscriptionUploadSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/UploadFile.json
     */
    /**
     * Sample code: UploadFile.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void uploadFile(com.azure.resourcemanager.support.SupportManager manager) {
        manager.filesNoSubscriptions()
            .uploadWithResponse("testworkspaceName", "test.txt", new UploadFile().withContent(
                "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAABGdBTUEAALGPC/xhBQAAAAFzUkdCAK7OHOkAAAAgY0hSTQAAeiYAAICEAAD6AAAAgOgAAHUwAADqYAAAOpgAABd")
                .withChunkIndex(0), com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListOperations.json
     */
    /**
     * Sample code: Get all operations.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void getAllOperations(com.azure.resourcemanager.support.SupportManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ProblemClassifications_Get

```java
/**
 * Samples for ProblemClassifications Get.
 */
public final class ProblemClassificationsGetSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/GetProblemClassification.json
     */
    /**
     * Sample code: Gets details of problemClassification for Azure service.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        getsDetailsOfProblemClassificationForAzureService(com.azure.resourcemanager.support.SupportManager manager) {
        manager.problemClassifications()
            .getWithResponse("service_guid", "problemClassification_guid", com.azure.core.util.Context.NONE);
    }
}
```

### ProblemClassifications_List

```java
/**
 * Samples for ProblemClassifications List.
 */
public final class ProblemClassificationsListSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListProblemClassifications.json
     */
    /**
     * Sample code: Gets list of problemClassifications for a service for which a support ticket can be created.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void getsListOfProblemClassificationsForAServiceForWhichASupportTicketCanBeCreated(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.problemClassifications().list("service_guid", com.azure.core.util.Context.NONE);
    }
}
```

### Services_Get

```java
/**
 * Samples for Services Get.
 */
public final class ServicesGetSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/GetService.json
     */
    /**
     * Sample code: Gets details of the Azure service.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void getsDetailsOfTheAzureService(com.azure.resourcemanager.support.SupportManager manager) {
        manager.services().getWithResponse("service_guid", com.azure.core.util.Context.NONE);
    }
}
```

### Services_List

```java
/**
 * Samples for Services List.
 */
public final class ServicesListSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListServices.json
     */
    /**
     * Sample code: Gets list of services for which a support ticket can be created.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        getsListOfServicesForWhichASupportTicketCanBeCreated(com.azure.resourcemanager.support.SupportManager manager) {
        manager.services().list(com.azure.core.util.Context.NONE);
    }
}
```

### SupportTickets_CheckNameAvailability

```java
import com.azure.resourcemanager.support.models.CheckNameAvailabilityInput;
import com.azure.resourcemanager.support.models.Type;

/**
 * Samples for SupportTickets CheckNameAvailability.
 */
public final class SupportTicketsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CheckNameAvailabilityWithSubscription.json
     */
    /**
     * Sample code: Checks whether name is available for a subscription support ticket resource.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void checksWhetherNameIsAvailableForASubscriptionSupportTicketResource(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .checkNameAvailabilityWithResponse(new CheckNameAvailabilityInput().withName("sampleName")
                .withType(Type.MICROSOFT_SUPPORT_SUPPORT_TICKETS), com.azure.core.util.Context.NONE);
    }
}
```

### SupportTickets_Create

```java
import com.azure.resourcemanager.support.models.Consent;
import com.azure.resourcemanager.support.models.ContactProfile;
import com.azure.resourcemanager.support.models.PreferredContactMethod;
import com.azure.resourcemanager.support.models.QuotaChangeRequest;
import com.azure.resourcemanager.support.models.QuotaTicketDetails;
import com.azure.resourcemanager.support.models.SecondaryConsent;
import com.azure.resourcemanager.support.models.SeverityLevel;
import com.azure.resourcemanager.support.models.TechnicalTicketDetails;
import com.azure.resourcemanager.support.models.UserConsent;
import java.util.Arrays;

/**
 * Samples for SupportTickets Create.
 */
public final class SupportTicketsCreateSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateSqlDatawarehouseQuotaTicketForDTUs.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for DTUs for Azure Synapse Analytics.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForDTUsForAzureSynapseAnalytics(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/sql_datawarehouse_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withAdvancedDiagnosticConsent(Consent.YES)
            .withContactDetails(new ContactProfile().withFirstName("abc")
                .withLastName("xyz")
                .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                .withPrimaryEmailAddress("abc@contoso.com")
                .withPreferredTimeZone("Pacific Standard Time")
                .withCountry("usa")
                .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withSupportPlanId(
                "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
            .withQuotaTicketDetails(new QuotaTicketDetails().withQuotaChangeRequestSubType("DTUs")
                .withQuotaChangeRequestVersion("1.0")
                .withQuotaChangeRequests(Arrays.asList(new QuotaChangeRequest().withRegion("EastUS")
                    .withPayload("{\"ServerName\":\"testserver\",\"NewLimit\":54000}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateBatchQuotaTicketForSpecificBatchAccountForActiveJobs.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for Active Jobs and Job Schedules for a Batch account.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForActiveJobsAndJobSchedulesForABatchAccount(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/batch_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withAdvancedDiagnosticConsent(Consent.YES)
            .withContactDetails(new ContactProfile().withFirstName("abc")
                .withLastName("xyz")
                .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                .withPrimaryEmailAddress("abc@contoso.com")
                .withPreferredTimeZone("Pacific Standard Time")
                .withCountry("usa")
                .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withSupportPlanId(
                "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
            .withQuotaTicketDetails(new QuotaTicketDetails().withQuotaChangeRequestSubType("Account")
                .withQuotaChangeRequestVersion("1.0")
                .withQuotaChangeRequests(Arrays.asList(new QuotaChangeRequest().withRegion("EastUS")
                    .withPayload("{\"AccountName\":\"test\",\"NewLimit\":200,\"Type\":\"Jobs\"}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateMachineLearningQuotaTicketForLowPriorityCores.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for Low-priority cores for Machine Learning service.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForLowPriorityCoresForMachineLearningService(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/machine_learning_service_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withAdvancedDiagnosticConsent(Consent.YES)
            .withContactDetails(new ContactProfile().withFirstName("abc")
                .withLastName("xyz")
                .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                .withPrimaryEmailAddress("abc@contoso.com")
                .withPreferredTimeZone("Pacific Standard Time")
                .withCountry("usa")
                .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withSupportPlanId(
                "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
            .withQuotaTicketDetails(new QuotaTicketDetails().withQuotaChangeRequestSubType("BatchAml")
                .withQuotaChangeRequestVersion("1.0")
                .withQuotaChangeRequests(Arrays.asList(new QuotaChangeRequest().withRegion("EastUS")
                    .withPayload("{\"NewLimit\":200,\"Type\":\"LowPriority\"}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateBatchQuotaTicketForSubscription.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for Batch accounts for a subscription.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForBatchAccountsForASubscription(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/batch_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withAdvancedDiagnosticConsent(Consent.YES)
            .withContactDetails(new ContactProfile().withFirstName("abc")
                .withLastName("xyz")
                .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                .withPrimaryEmailAddress("abc@contoso.com")
                .withPreferredTimeZone("Pacific Standard Time")
                .withCountry("usa")
                .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withSupportPlanId(
                "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
            .withQuotaTicketDetails(new QuotaTicketDetails().withQuotaChangeRequestSubType("Subscription")
                .withQuotaChangeRequestVersion("1.0")
                .withQuotaChangeRequests(Arrays.asList(new QuotaChangeRequest().withRegion("EastUS")
                    .withPayload("{\"NewLimit\":200,\"Type\":\"Account\"}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateSqlDatabaseQuotaTicketForDTUs.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for DTUs for SQL Database.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForDTUsForSQLDatabase(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/sql_database_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withAdvancedDiagnosticConsent(Consent.YES)
            .withContactDetails(new ContactProfile().withFirstName("abc")
                .withLastName("xyz")
                .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                .withPrimaryEmailAddress("abc@contoso.com")
                .withPreferredTimeZone("Pacific Standard Time")
                .withCountry("usa")
                .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withSupportPlanId(
                "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
            .withQuotaTicketDetails(new QuotaTicketDetails().withQuotaChangeRequestSubType("DTUs")
                .withQuotaChangeRequestVersion("1.0")
                .withQuotaChangeRequests(Arrays.asList(new QuotaChangeRequest().withRegion("EastUS")
                    .withPayload("{\"ServerName\":\"testserver\",\"NewLimit\":54000}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateGenericQuotaTicket.json
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
        manager.supportTickets()
            .define("testticket")
            .withDescription("Increase the maximum throughput per container limit to 10000 for account foo bar")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/cosmosdb_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withAdvancedDiagnosticConsent(Consent.YES)
            .withContactDetails(new ContactProfile().withFirstName("abc")
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
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateBatchQuotaTicketForSpecificBatchAccountForLowPriorityCores.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for Low-priority cores for a Batch account.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForLowPriorityCoresForABatchAccount(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/batch_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withAdvancedDiagnosticConsent(Consent.YES)
            .withContactDetails(new ContactProfile().withFirstName("abc")
                .withLastName("xyz")
                .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                .withPrimaryEmailAddress("abc@contoso.com")
                .withPreferredTimeZone("Pacific Standard Time")
                .withCountry("usa")
                .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withSupportPlanId(
                "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
            .withQuotaTicketDetails(new QuotaTicketDetails().withQuotaChangeRequestSubType("Account")
                .withQuotaChangeRequestVersion("1.0")
                .withQuotaChangeRequests(Arrays.asList(new QuotaChangeRequest().withRegion("EastUS")
                    .withPayload("{\"AccountName\":\"test\",\"NewLimit\":200,\"Type\":\"LowPriority\"}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateSqlManagedInstanceQuotaTicket.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for Azure SQL managed instance.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForAzureSQLManagedInstance(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/sql_managedinstance_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withAdvancedDiagnosticConsent(Consent.YES)
            .withContactDetails(new ContactProfile().withFirstName("abc")
                .withLastName("xyz")
                .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                .withPrimaryEmailAddress("abc@contoso.com")
                .withPreferredTimeZone("Pacific Standard Time")
                .withCountry("usa")
                .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withSupportPlanId(
                "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
            .withQuotaTicketDetails(new QuotaTicketDetails().withQuotaChangeRequestSubType("SQLMI")
                .withQuotaChangeRequestVersion("1.0")
                .withQuotaChangeRequests(Arrays.asList(
                    new QuotaChangeRequest().withRegion("EastUS")
                        .withPayload("{\"NewLimit\":200, \"Metadata\":null, \"Type\":\"vCore\"}"),
                    new QuotaChangeRequest().withRegion("EastUS")
                        .withPayload("{\"NewLimit\":200, \"Metadata\":null, \"Type\":\"Subnet\"}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateSubMgmtSupportTicketForSubscription.json
     */
    /**
     * Sample code: Create a ticket for Subscription Management related issues for a subscription.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketForSubscriptionManagementRelatedIssuesForASubscription(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/subscription_management_service_guid/problemClassifications/subscription_management_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withAdvancedDiagnosticConsent(Consent.NO)
            .withContactDetails(new ContactProfile().withFirstName("abc")
                .withLastName("xyz")
                .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                .withPrimaryEmailAddress("abc@contoso.com")
                .withPreferredTimeZone("Pacific Standard Time")
                .withCountry("usa")
                .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/subscription_management_service_guid")
            .withSupportPlanId(
                "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
            .withFileWorkspaceName("6f16735c-1530836f-e9970f1a-2e49-47b7-96cd-9746b83aa066")
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateTechnicalSupportTicketForSubscription.json
     */
    /**
     * Sample code: Create a ticket for Technical issue related to a specific resource for a subscription.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketForTechnicalIssueRelatedToASpecificResourceForASubscription(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/virtual_machine_running_linux_service_guid/problemClassifications/problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withAdvancedDiagnosticConsent(Consent.YES)
            .withContactDetails(new ContactProfile().withFirstName("abc")
                .withLastName("xyz")
                .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                .withPrimaryEmailAddress("abc@contoso.com")
                .withPreferredTimeZone("Pacific Standard Time")
                .withCountry("usa")
                .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/cddd3eb5-1830-b494-44fd-782f691479dc")
            .withProblemScopingQuestions(
                "{\"articleId\":\"076846c1-4c0b-4b21-91c6-1a30246b3867\",\"scopingDetails\":[{\"question\":\"When did the problem begin?\",\"controlId\":\"problem_start_time\",\"orderId\":1,\"inputType\":\"static\",\"answer\":{\"displayValue\":\"2023-08-31T18:55:00.739Z\",\"value\":\"2023-08-31T18:55:00.739Z\",\"type\":\"datetime\"}},{\"question\":\"API Type of the Cosmos DB account\",\"controlId\":\"api_type\",\"orderId\":2,\"inputType\":\"static\",\"answer\":{\"displayValue\":\"Table\",\"value\":\"tables\",\"type\":\"string\"}},{\"question\":\"Table name\",\"controlId\":\"collection_name_table\",\"orderId\":11,\"inputType\":\"nonstatic\",\"answer\":{\"displayValue\":\"Select Table Name\",\"value\":\"dont_know_answer\",\"type\":\"string\"}},{\"question\":\"Provide additional details about the issue you're facing\",\"controlId\":\"problem_description\",\"orderId\":12,\"inputType\":\"nonstatic\",\"answer\":{\"displayValue\":\"test ticket, please ignore and close\",\"value\":\"test ticket, please ignore and close\",\"type\":\"string\"}}]}")
            .withSupportPlanId(
                "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
            .withFileWorkspaceName("6f16735c-1530836f-e9970f1a-2e49-47b7-96cd-9746b83aa066")
            .withTechnicalTicketDetails(new TechnicalTicketDetails().withResourceId(
                "/subscriptions/132d901f-189d-4381-9214-fe68e27e05a1/resourceGroups/test/providers/Microsoft.Compute/virtualMachines/testserver"))
            .withSecondaryConsent(Arrays.asList(
                new SecondaryConsent().withUserConsent(UserConsent.YES).withType("virtualmachinerunninglinuxservice")))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateBatchQuotaTicketForSpecificBatchAccountForPools.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for Pools for a Batch account.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForPoolsForABatchAccount(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/batch_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withAdvancedDiagnosticConsent(Consent.YES)
            .withContactDetails(new ContactProfile().withFirstName("abc")
                .withLastName("xyz")
                .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                .withPrimaryEmailAddress("abc@contoso.com")
                .withPreferredTimeZone("Pacific Standard Time")
                .withCountry("usa")
                .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withSupportPlanId(
                "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
            .withQuotaTicketDetails(new QuotaTicketDetails().withQuotaChangeRequestSubType("Account")
                .withQuotaChangeRequestVersion("1.0")
                .withQuotaChangeRequests(Arrays.asList(new QuotaChangeRequest().withRegion("EastUS")
                    .withPayload("{\"AccountName\":\"test\",\"NewLimit\":200,\"Type\":\"Pools\"}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateBatchQuotaTicketForSpecificBatchAccountForDedicatedCores.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for specific VM family cores for a Batch account.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForSpecificVMFamilyCoresForABatchAccount(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/batch_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withAdvancedDiagnosticConsent(Consent.YES)
            .withContactDetails(new ContactProfile().withFirstName("abc")
                .withLastName("xyz")
                .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                .withPrimaryEmailAddress("abc@contoso.com")
                .withPreferredTimeZone("Pacific Standard Time")
                .withCountry("usa")
                .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withSupportPlanId(
                "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
            .withQuotaTicketDetails(new QuotaTicketDetails().withQuotaChangeRequestSubType("Account")
                .withQuotaChangeRequestVersion("1.0")
                .withQuotaChangeRequests(Arrays.asList(new QuotaChangeRequest().withRegion("EastUS")
                    .withPayload(
                        "{\"AccountName\":\"test\",\"VMFamily\":\"standardA0_A7Family\",\"NewLimit\":200,\"Type\":\"Dedicated\"}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateSqlDatabaseQuotaTicketForServers.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for Servers for SQL Database.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForServersForSQLDatabase(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/sql_database_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withAdvancedDiagnosticConsent(Consent.YES)
            .withContactDetails(new ContactProfile().withFirstName("abc")
                .withLastName("xyz")
                .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                .withPrimaryEmailAddress("abc@contoso.com")
                .withPreferredTimeZone("Pacific Standard Time")
                .withCountry("usa")
                .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withSupportPlanId(
                "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
            .withQuotaTicketDetails(new QuotaTicketDetails().withQuotaChangeRequestSubType("Servers")
                .withQuotaChangeRequestVersion("1.0")
                .withQuotaChangeRequests(
                    Arrays.asList(new QuotaChangeRequest().withRegion("EastUS").withPayload("{\"NewLimit\":200}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateBillingSupportTicketForSubscription.json
     */
    /**
     * Sample code: Create a ticket for Billing related issues.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketForBillingRelatedIssues(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/billing_service_guid/problemClassifications/billing_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withAdvancedDiagnosticConsent(Consent.NO)
            .withContactDetails(new ContactProfile().withFirstName("abc")
                .withLastName("xyz")
                .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                .withPrimaryEmailAddress("abc@contoso.com")
                .withPreferredTimeZone("Pacific Standard Time")
                .withCountry("usa")
                .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/billing_service_guid")
            .withSupportPlanId(
                "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
            .withFileWorkspaceName("6f16735c-1530836f-e9970f1a-2e49-47b7-96cd-9746b83aa066")
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateMachineLearningQuotaTicketForDedicatedCores.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for specific VM family cores for Machine Learning service.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForSpecificVMFamilyCoresForMachineLearningService(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/machine_learning_service_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withAdvancedDiagnosticConsent(Consent.YES)
            .withContactDetails(new ContactProfile().withFirstName("abc")
                .withLastName("xyz")
                .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                .withPrimaryEmailAddress("abc@contoso.com")
                .withPreferredTimeZone("Pacific Standard Time")
                .withCountry("usa")
                .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withSupportPlanId(
                "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
            .withQuotaTicketDetails(new QuotaTicketDetails().withQuotaChangeRequestSubType("BatchAml")
                .withQuotaChangeRequestVersion("1.0")
                .withQuotaChangeRequests(Arrays.asList(new QuotaChangeRequest().withRegion("EastUS")
                    .withPayload("{\"VMFamily\":\"standardA0_A7Family\",\"NewLimit\":200,\"Type\":\"Dedicated\"}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateSqlDatawarehouseQuotaTicketForServers.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for Servers for Azure Synapse Analytics.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketToRequestQuotaIncreaseForServersForAzureSynapseAnalytics(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/sql_datawarehouse_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withAdvancedDiagnosticConsent(Consent.YES)
            .withContactDetails(new ContactProfile().withFirstName("abc")
                .withLastName("xyz")
                .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                .withPrimaryEmailAddress("abc@contoso.com")
                .withPreferredTimeZone("Pacific Standard Time")
                .withCountry("usa")
                .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withSupportPlanId(
                "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
            .withQuotaTicketDetails(new QuotaTicketDetails().withQuotaChangeRequestSubType("Servers")
                .withQuotaChangeRequestVersion("1.0")
                .withQuotaChangeRequests(
                    Arrays.asList(new QuotaChangeRequest().withRegion("EastUS").withPayload("{\"NewLimit\":200}"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateCoresQuotaTicketForSubscription.json
     */
    /**
     * Sample code: Create a ticket to request Quota increase for Compute VM Cores.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        createATicketToRequestQuotaIncreaseForComputeVMCores(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .define("testticket")
            .withDescription("my description")
            .withProblemClassificationId(
                "/providers/Microsoft.Support/services/quota_service_guid/problemClassifications/cores_problemClassification_guid")
            .withSeverity(SeverityLevel.MODERATE)
            .withAdvancedDiagnosticConsent(Consent.YES)
            .withContactDetails(new ContactProfile().withFirstName("abc")
                .withLastName("xyz")
                .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                .withPrimaryEmailAddress("abc@contoso.com")
                .withPreferredTimeZone("Pacific Standard Time")
                .withCountry("usa")
                .withPreferredSupportLanguage("en-US"))
            .withTitle("my title")
            .withServiceId("/providers/Microsoft.Support/services/quota_service_guid")
            .withSupportPlanId(
                "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
            .withQuotaTicketDetails(new QuotaTicketDetails().withQuotaChangeRequestVersion("1.0")
                .withQuotaChangeRequests(Arrays.asList(new QuotaChangeRequest().withRegion("EastUS")
                    .withPayload("{\"SKU\":\"DSv3 Series\",\"NewLimit\":104}"))))
            .create();
    }
}
```

### SupportTickets_Get

```java
/**
 * Samples for SupportTickets Get.
 */
public final class SupportTicketsGetSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/GetSubscriptionSupportTicketDetails.json
     */
    /**
     * Sample code: Get details of a subscription ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void getDetailsOfASubscriptionTicket(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets().getWithResponse("testticket", com.azure.core.util.Context.NONE);
    }
}
```

### SupportTickets_List

```java
/**
 * Samples for SupportTickets List.
 */
public final class SupportTicketsListSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListSupportTicketsServiceIdEqualsForSubscription.json
     */
    /**
     * Sample code: List support tickets with a certain service id for a subscription.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void listSupportTicketsWithACertainServiceIdForASubscription(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets().list(null, "ServiceId eq 'vm_windows_service_guid'", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListSupportTicketsCreatedOnOrAfterAndInOpenStateBySubscription.json
     */
    /**
     * Sample code: List support tickets created on or after a certain date and in open state for a subscription.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void listSupportTicketsCreatedOnOrAfterACertainDateAndInOpenStateForASubscription(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .list(null, "createdDate ge 2020-03-10T22:08:51Z and status eq 'Open'", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListSupportTicketsInUpdatingStateBySubscription.json
     */
    /**
     * Sample code: List support tickets in updating state for a subscription.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        listSupportTicketsInUpdatingStateForASubscription(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets().list(null, "status eq 'Updating'", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListSupportTicketsCreatedOnOrAfterAndInUpdatingStateBySubscription.json
     */
    /**
     * Sample code: List support tickets created on or after a certain date and in updating state for a subscription.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void listSupportTicketsCreatedOnOrAfterACertainDateAndInUpdatingStateForASubscription(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .list(null, "createdDate ge 2020-03-10T22:08:51Z and status eq 'Updating'",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListSupportTicketsProblemClassificationIdEqualsForSubscription.json
     */
    /**
     * Sample code: List support tickets with a certain problem classification id for a subscription.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void listSupportTicketsWithACertainProblemClassificationIdForASubscription(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets()
            .list(null, "ProblemClassificationId eq 'compute_vm_problemClassification_guid'",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListSupportTicketsBySubscription.json
     */
    /**
     * Sample code: List support tickets for a subscription.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void listSupportTicketsForASubscription(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets().list(null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListSupportTicketsInOpenStateBySubscription.json
     */
    /**
     * Sample code: List support tickets in open state for a subscription.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        listSupportTicketsInOpenStateForASubscription(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTickets().list(null, "status eq 'Open'", com.azure.core.util.Context.NONE);
    }
}
```

### SupportTickets_Update

```java
import com.azure.resourcemanager.support.models.Consent;
import com.azure.resourcemanager.support.models.PreferredContactMethod;
import com.azure.resourcemanager.support.models.SeverityLevel;
import com.azure.resourcemanager.support.models.Status;
import com.azure.resourcemanager.support.models.SupportTicketDetails;
import com.azure.resourcemanager.support.models.UpdateContactProfile;
import java.util.Arrays;

/**
 * Samples for SupportTickets Update.
 */
public final class SupportTicketsUpdateSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/UpdateAdvancedDiagnosticConsentOfSupportTicketForSubscription.json
     */
    /**
     * Sample code: Update advanced diagnostic consent of a subscription support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void updateAdvancedDiagnosticConsentOfASubscriptionSupportTicket(
        com.azure.resourcemanager.support.SupportManager manager) {
        SupportTicketDetails resource
            = manager.supportTickets().getWithResponse("testticket", com.azure.core.util.Context.NONE).getValue();
        resource.update().withAdvancedDiagnosticConsent(Consent.YES).apply();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/UpdateSeverityOfSupportTicketForSubscription.json
     */
    /**
     * Sample code: Update severity of a subscription support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        updateSeverityOfASubscriptionSupportTicket(com.azure.resourcemanager.support.SupportManager manager) {
        SupportTicketDetails resource
            = manager.supportTickets().getWithResponse("testticket", com.azure.core.util.Context.NONE).getValue();
        resource.update().withSeverity(SeverityLevel.CRITICAL).apply();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/UpdateStatusOfSupportTicketForSubscription.json
     */
    /**
     * Sample code: Update status of a subscription support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        updateStatusOfASubscriptionSupportTicket(com.azure.resourcemanager.support.SupportManager manager) {
        SupportTicketDetails resource
            = manager.supportTickets().getWithResponse("testticket", com.azure.core.util.Context.NONE).getValue();
        resource.update().withStatus(Status.CLOSED).apply();
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/UpdateContactDetailsOfSupportTicketForSubscription.json
     */
    /**
     * Sample code: Update contact details of a subscription support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        updateContactDetailsOfASubscriptionSupportTicket(com.azure.resourcemanager.support.SupportManager manager) {
        SupportTicketDetails resource
            = manager.supportTickets().getWithResponse("testticket", com.azure.core.util.Context.NONE).getValue();
        resource.update()
            .withContactDetails(new UpdateContactProfile().withFirstName("first name")
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

### SupportTicketsNoSubscription_CheckNameAvailability

```java
import com.azure.resourcemanager.support.models.CheckNameAvailabilityInput;
import com.azure.resourcemanager.support.models.Type;

/**
 * Samples for SupportTicketsNoSubscription CheckNameAvailability.
 */
public final class SupportTicketsNoSubscriptionCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CheckNameAvailability.json
     */
    /**
     * Sample code: Checks whether name is available for SupportTicket resource.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        checksWhetherNameIsAvailableForSupportTicketResource(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTicketsNoSubscriptions()
            .checkNameAvailabilityWithResponse(new CheckNameAvailabilityInput().withName("sampleName")
                .withType(Type.MICROSOFT_SUPPORT_SUPPORT_TICKETS), com.azure.core.util.Context.NONE);
    }
}
```

### SupportTicketsNoSubscription_Create

```java
import com.azure.resourcemanager.support.fluent.models.SupportTicketDetailsInner;
import com.azure.resourcemanager.support.models.Consent;
import com.azure.resourcemanager.support.models.ContactProfile;
import com.azure.resourcemanager.support.models.PreferredContactMethod;
import com.azure.resourcemanager.support.models.SecondaryConsent;
import com.azure.resourcemanager.support.models.SeverityLevel;
import com.azure.resourcemanager.support.models.UserConsent;
import java.util.Arrays;

/**
 * Samples for SupportTicketsNoSubscription Create.
 */
public final class SupportTicketsNoSubscriptionCreateSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateBillingSupportTicket.json
     */
    /**
     * Sample code: Create a ticket for Billing related issues.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketForBillingRelatedIssues(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTicketsNoSubscriptions()
            .create("testticket", new SupportTicketDetailsInner().withDescription("my description")
                .withProblemClassificationId(
                    "/providers/Microsoft.Support/services/billing_service_guid/problemClassifications/billing_problemClassification_guid")
                .withSeverity(SeverityLevel.MODERATE)
                .withAdvancedDiagnosticConsent(Consent.NO)
                .withSupportPlanId(
                    "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
                .withContactDetails(new ContactProfile().withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
                .withTitle("my title")
                .withServiceId("/providers/Microsoft.Support/services/billing_service_guid")
                .withFileWorkspaceName("6f16735c-1530836f-e9970f1a-2e49-47b7-96cd-9746b83aa066"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateSubMgmtSupportTicket.json
     */
    /**
     * Sample code: Create a ticket for Subscription Management related issues.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        createATicketForSubscriptionManagementRelatedIssues(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTicketsNoSubscriptions()
            .create("testticket", new SupportTicketDetailsInner().withDescription("my description")
                .withProblemClassificationId(
                    "/providers/Microsoft.Support/services/subscription_management_service_guid/problemClassifications/subscription_management_problemClassification_guid")
                .withSeverity(SeverityLevel.MODERATE)
                .withAdvancedDiagnosticConsent(Consent.YES)
                .withSupportPlanId(
                    "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
                .withContactDetails(new ContactProfile().withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
                .withTitle("my title")
                .withServiceId("/providers/Microsoft.Support/services/subscription_management_service_guid")
                .withFileWorkspaceName("6f16735c-1530836f-e9970f1a-2e49-47b7-96cd-9746b83aa066"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/CreateTechnicalSupportTicket.json
     */
    /**
     * Sample code: Create a ticket for Technical issue related to a specific resource.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void createATicketForTechnicalIssueRelatedToASpecificResource(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTicketsNoSubscriptions()
            .create("testticket", new SupportTicketDetailsInner().withDescription("my description")
                .withProblemClassificationId(
                    "/providers/Microsoft.Support/services/virtual_machine_running_linux_service_guid/problemClassifications/problemClassification_guid")
                .withSeverity(SeverityLevel.MODERATE)
                .withAdvancedDiagnosticConsent(Consent.YES)
                .withProblemScopingQuestions(
                    "{\"articleId\":\"076846c1-4c0b-4b21-91c6-1a30246b3867\",\"scopingDetails\":[{\"question\":\"When did the problem begin?\",\"controlId\":\"problem_start_time\",\"orderId\":1,\"inputType\":\"static\",\"answer\":{\"displayValue\":\"2023-08-31T18:55:00.739Z\",\"value\":\"2023-08-31T18:55:00.739Z\",\"type\":\"datetime\"}},{\"question\":\"API Type of the Cosmos DB account\",\"controlId\":\"api_type\",\"orderId\":2,\"inputType\":\"static\",\"answer\":{\"displayValue\":\"Table\",\"value\":\"tables\",\"type\":\"string\"}},{\"question\":\"Table name\",\"controlId\":\"collection_name_table\",\"orderId\":11,\"inputType\":\"nonstatic\",\"answer\":{\"displayValue\":\"Select Table Name\",\"value\":\"dont_know_answer\",\"type\":\"string\"}},{\"question\":\"Provide additional details about the issue you're facing\",\"controlId\":\"problem_description\",\"orderId\":12,\"inputType\":\"nonstatic\",\"answer\":{\"displayValue\":\"test ticket, please ignore and close\",\"value\":\"test ticket, please ignore and close\",\"type\":\"string\"}}]}")
                .withSupportPlanId(
                    "U291cmNlOlNDTSxDbGFyaWZ5SW5zdGFsbGF0aW9uU2l0ZUlkOjcsTGluZUl0ZW1JZDo5ODY1NzIyOSxDb250cmFjdElkOjk4NjU5MTk0LFN1YnNjcmlwdGlvbklkOjc2Y2I3N2ZhLThiMTctNGVhYi05NDkzLWI2NWRhY2U5OTgxMyw=")
                .withContactDetails(new ContactProfile().withFirstName("abc")
                    .withLastName("xyz")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("abc@contoso.com")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("usa")
                    .withPreferredSupportLanguage("en-US"))
                .withTitle("my title")
                .withServiceId("/providers/Microsoft.Support/services/cddd3eb5-1830-b494-44fd-782f691479dc")
                .withFileWorkspaceName("6f16735c-1530836f-e9970f1a-2e49-47b7-96cd-9746b83aa066")
                .withSecondaryConsent(Arrays.asList(new SecondaryConsent().withUserConsent(UserConsent.YES)
                    .withType("virtualmachinerunninglinuxservice"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### SupportTicketsNoSubscription_Get

```java
/**
 * Samples for SupportTicketsNoSubscription Get.
 */
public final class SupportTicketsNoSubscriptionGetSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/GetSupportTicketDetails.json
     */
    /**
     * Sample code: Get details of a ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void getDetailsOfATicket(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTicketsNoSubscriptions().getWithResponse("testticket", com.azure.core.util.Context.NONE);
    }
}
```

### SupportTicketsNoSubscription_List

```java
/**
 * Samples for SupportTicketsNoSubscription List.
 */
public final class SupportTicketsNoSubscriptionListSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListSupportTicketsCreatedOnOrAfterAndInUpdatingState.json
     */
    /**
     * Sample code: List support tickets created on or after a certain date and in updating state.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void listSupportTicketsCreatedOnOrAfterACertainDateAndInUpdatingState(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTicketsNoSubscriptions()
            .list(null, "createdDate ge 2020-03-10T22:08:51Z and status eq 'Updating'",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListSupportTicketsInOpenState.json
     */
    /**
     * Sample code: List support tickets in open state.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void listSupportTicketsInOpenState(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTicketsNoSubscriptions().list(null, "status eq 'Open'", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListSupportTickets.json
     */
    /**
     * Sample code: List support tickets.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void listSupportTickets(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTicketsNoSubscriptions().list(null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListSupportTicketsInUpdatingState.json
     */
    /**
     * Sample code: List support tickets in updating state.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void listSupportTicketsInUpdatingState(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTicketsNoSubscriptions().list(null, "status eq 'Updating'", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListSupportTicketsCreatedOnOrAfterAndInOpenState.json
     */
    /**
     * Sample code: List support tickets created on or after a certain date and in open state.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void listSupportTicketsCreatedOnOrAfterACertainDateAndInOpenState(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTicketsNoSubscriptions()
            .list(null, "createdDate ge 2020-03-10T22:08:51Z and status eq 'Open'", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListSupportTicketsServiceIdEquals.json
     */
    /**
     * Sample code: List support tickets with a certain service id.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        listSupportTicketsWithACertainServiceId(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTicketsNoSubscriptions()
            .list(null, "ServiceId eq 'vm_windows_service_guid'", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/ListSupportTicketsProblemClassificationIdEquals.json
     */
    /**
     * Sample code: List support tickets with a certain problem classification id.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void listSupportTicketsWithACertainProblemClassificationId(
        com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTicketsNoSubscriptions()
            .list(null, "ProblemClassificationId eq 'compute_vm_problemClassification_guid'",
                com.azure.core.util.Context.NONE);
    }
}
```

### SupportTicketsNoSubscription_Update

```java
import com.azure.resourcemanager.support.models.Consent;
import com.azure.resourcemanager.support.models.PreferredContactMethod;
import com.azure.resourcemanager.support.models.SeverityLevel;
import com.azure.resourcemanager.support.models.Status;
import com.azure.resourcemanager.support.models.UpdateContactProfile;
import com.azure.resourcemanager.support.models.UpdateSupportTicket;
import java.util.Arrays;

/**
 * Samples for SupportTicketsNoSubscription Update.
 */
public final class SupportTicketsNoSubscriptionUpdateSamples {
    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/UpdateAdvancedDiagnosticConsentOfSupportTicket.json
     */
    /**
     * Sample code: Update advanced diagnostic consent of a support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void
        updateAdvancedDiagnosticConsentOfASupportTicket(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTicketsNoSubscriptions()
            .updateWithResponse("testticket", new UpdateSupportTicket().withAdvancedDiagnosticConsent(Consent.YES),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/UpdateSeverityOfSupportTicket.json
     */
    /**
     * Sample code: Update severity of a support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void updateSeverityOfASupportTicket(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTicketsNoSubscriptions()
            .updateWithResponse("testticket", new UpdateSupportTicket().withSeverity(SeverityLevel.CRITICAL),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/UpdateStatusOfSupportTicket.json
     */
    /**
     * Sample code: Update status of a support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void updateStatusOfASupportTicket(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTicketsNoSubscriptions()
            .updateWithResponse("testticket", new UpdateSupportTicket().withStatus(Status.CLOSED),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/support/resource-manager/Microsoft.Support/stable/2024-04-01/examples/UpdateContactDetailsOfSupportTicket.json
     */
    /**
     * Sample code: Update contact details of a support ticket.
     * 
     * @param manager Entry point to SupportManager.
     */
    public static void updateContactDetailsOfASupportTicket(com.azure.resourcemanager.support.SupportManager manager) {
        manager.supportTicketsNoSubscriptions()
            .updateWithResponse("testticket",
                new UpdateSupportTicket().withContactDetails(new UpdateContactProfile().withFirstName("first name")
                    .withLastName("last name")
                    .withPreferredContactMethod(PreferredContactMethod.EMAIL)
                    .withPrimaryEmailAddress("test.name@contoso.com")
                    .withAdditionalEmailAddresses(Arrays.asList("tname@contoso.com", "teamtest@contoso.com"))
                    .withPhoneNumber("123-456-7890")
                    .withPreferredTimeZone("Pacific Standard Time")
                    .withCountry("USA")
                    .withPreferredSupportLanguage("en-US")),
                com.azure.core.util.Context.NONE);
    }
}
```

