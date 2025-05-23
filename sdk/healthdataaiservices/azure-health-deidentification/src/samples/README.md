---
page_type: sample
languages:
  - java
products:
  - azure
urlFragment: health-deidentification-java-samples
---

# Azure Health Data Services de-identification service client library for Java samples

This package contains a client library for the de-identification service in Azure Health Data Services. Each sample focuses
on a specific scenario and can be executed independently. For more information about the SDK including key concepts and 
how to get started, see the [Azure Health Data Services de-identification service client library for Java][SDK_README].

## Samples

| **File Name**                                                                                                                                                                                                | **Description**                                                              |
|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------|
| [DeidentifyText](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/java/com/azure/health/deidentification/DeidentifyText.java)        | De-identify text input                                                       |
| [DeidentifyTextAsync](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/java/com/azure/health/deidentification/DeidentifyTextAsync.java)                                     | De-identify text input asynchronously                                        |
| [ListDeidentificationJobs](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/java/com/azure/health/deidentification/ListDeidentificationJobs.java)                           | List all de-identification jobs                                              |
| [ListDeidentificationJobsAsync](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/java/com/azure/health/deidentification/ListDeidentificationJobsAsync.java)                 | List all de-identification jobs asynchronously                                             |
| [BeginDeidentifyDocuments](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/java/com/azure/health/deidentification/BeginDeidentifyDocuments.java)                           | Begin a job to de-identify documents in Azure Storage                        |
| [BeginDeidentifyDocumentsAsync](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/java/com/azure/health/deidentification/BeginDeidentifyDocumentsAsync.java)                 | Begin a job to de-identify documents in Azure Storage asynchronously                       |
| [GetDeidentificationJob](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/java/com/azure/health/deidentification/GetDeidentificationJob.java)                               | Get details of a de-identification job                                       |
| [GetDeidentificationJobAsync](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/java/com/azure/health/deidentification/GetDeidentificationJobAsync.java)                     | Get details of a de-identification job asynchronously                                       |
| [ListProcessedDocumentsWithinAJob](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/java/com/azure/health/deidentification/ListProcessedDocumentsWithinAJob.java)           | List all documents processed by a de-identification job                      |
| [ListProcessedDocumentsWithinAJobAsync](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/java/com/azure/health/deidentification/ListProcessedDocumentsWithinAJobAsync.java) | List all documents processed by a de-identification job asynchronously                      |
| [CancelDeidentificationJob](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/java/com/azure/health/deidentification/CancelDeidentificationJob.java)                         | Cancel the processing of documents in a de-identification job, while maintaining job metadata |
| [CancelDeidentificationJobAsync](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/java/com/azure/health/deidentification/CancelDeidentificationJobAsync.java)               | Cancel the processing of documents in a de-identification job asynchronously, while maintaining job metadata |
| [DeleteDeidentificationJob](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/java/com/azure/health/deidentification/DeleteDeidentificationJob.java)                         | Delete all information about a de-identification job                         |
| [DeleteDeidentificationJobAsync](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/java/com/azure/health/deidentification/DeleteDeidentificationJobAsync.java)               | Delete all information about a de-identification job asynchronously                         |

<!-- LINKS -->
[SDK_README]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/README.md
