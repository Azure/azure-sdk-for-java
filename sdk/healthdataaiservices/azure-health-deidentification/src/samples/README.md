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

| **File Name**                                                                                                                                                                                             | **Description**                                                              |
|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------|
| [DeidentifyText]<!--(https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/DeidentifyText.java)-->                                     | De-identify text input                                                       |
| [DeidentifyTextAsync]<!--(https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/DeidentifyTextAsync.java)-->                                | De-identify text input asynchronously                                        |
| [ListDeidentificationJobs]<!--(https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/ListDeidentificationJobs.java)-->                 | List all de-identification jobs                                              |
| [ListDeidentificationJobsAsync]<!--(https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/ListDeidentificationJobsAsync.java)-->                 | List all de-identification jobs asynchronously                                             |
| [BeginDeidentifyDocuments]<!--(https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/BeginDeidentifyDocuments.java)-->                 | Begin a job to de-identify documents in Azure Storage                        |
| [BeginDeidentifyDocumentsAsync]<!--(https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/BeginDeidentifyDocumentsAsync.java)-->                 | Begin a job to de-identify documents in Azure Storage asynchronously                       |
| [GetDeidentificationJob]<!--(https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/GetDeidentificationJob.java)-->                     | Get details of a de-identification job                                       |
| [GetDeidentificationJobAsync]<!--(https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/GetDeidentificationJobAsync.java)-->                     | Get details of a de-identification job asynchronously                                       |
| [ListProcessedDocumentsWithinAJob]<!--(https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/ListProcessedDocumentsWithinAJob.java)--> | List all documents processed by a de-identification job                      |
| [ListProcessedDocumentsWithinAJobAsync]<!--(https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/ListProcessedDocumentsWithinAJobAsync.java)--> | List all documents processed by a de-identification job asynchronously                      |
| [CancelDeidentificationJob]<!--(https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/CancelDeidentificationJob.java)-->               | Cancel the processing of documents in a de-identification job, while maintaining job metadata |
| [CancelDeidentificationJobAsync]<!--(https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/CancelDeidentificationJobAsync.java)-->               | Cancel the processing of documents in a de-identification job asynchronously, while maintaining job metadata |
| [DeleteDeidentificationJob]<!--(https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/DeleteDeidentificationJob.java)-->               | Delete all information about a de-identification job                         |
| [DeleteDeidentificationJobAsync]<!--(https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/DeleteDeidentificationJobAsync.java)-->               | Delete all information about a de-identification job asynchronously                         |

<!-- LINKS -->
[SDK_README]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/README.md
