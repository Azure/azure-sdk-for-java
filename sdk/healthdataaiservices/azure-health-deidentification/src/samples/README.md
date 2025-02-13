---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-health-deidentification
urlFragment: health-deidentification-java-samples
---

# Azure Health Data Services de-identification service client library for Java samples

This package contains a client library for the de-identification service in Azure Health Data Services. Each sample focuses
on a specific scenario and can be executed independently. For more information about the SDK including key concepts and 
how to get started, see the [Azure Health Data Services de-identification service client library for Java][SDK_README].

## Samples

| **File Name**                                         | **Description**                                                                               |
|:------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| [DeidentifyText][deidentify_text]                     | De-identify text input                                                                        |
| [ListDeidentificationJobs][list_jobs]                 | List all de-identification jobs                                                               |
| [BeginDeidentifyDocuments][begin_job]                 | Begin a job to de-identify documents in Azure Storage                                         |
| [GetDeidentificationJob][get_job]                     | Get details of a de-identification job                                                        |
| [ListProcessedDocumentsWithinAJob][get_job_documents] | List all documents processed by a de-identification job                                       |
| [CancelDeidentificationJob][cancel_job]               | Cancel the processing of documents in a de-identification job, while maintaining job metadata |
| [DeleteDeidentificationJob][delete_job]               | Delete all information about a de-identification job                                          |

<!-- LINKS -->
[SDK_README]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/healthdataaiservices/azure-health-deidentification/README.md
[deidentify_text]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/DeidentifyText.java
[list_jobs]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/ListDeidentificationJobs.java
[begin_job]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/BeginDeidentifyDocuments.java
[get_job]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/GetDeidentificationJob.java
[get_job_documents]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/ListProcessedDocumentsWithinAJob.java
[cancel_job]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/CancelDeidentificationJob.java
[delete_job]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/DeleteDeidentificationJob.java
