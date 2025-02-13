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
| [CreateADeIdentificationJob][create_job]              | Begin a job to de-identify documents in Azure Storage                                         |
| [GetADeIdentificationJob][get_job]                    | Get details of a de-identification job                                                        |
| [ListProcessedDocumentsWithinAJob][get_job_documents] | List all documents processed by a de-identification job                                       |
| [CancelADeIdentificationJob][cancel_job]              | Cancel the processing of documents in a de-identification job, while maintaining job metadata |
| [DeleteADeIdentificationJob][delete_job]              | Delete all information about a de-identification job                                          |
| [ListDeIdentificationJobs][list_jobs]                 | List all de-identification jobs                                                               |


<!-- LINKS -->
[SDK_README]: ../../README.md
[deidentify_text]: java/com/azure/health/deidentification/generated/DeIdentifyText.java
[create_job]: java/com/azure/health/deidentification/generated/CreateADeIdentificationJob.java
[get_job]: java/com/azure/health/deidentification/generated/GetADeIdentificationJob.java
[get_job_documents]: java/com/azure/health/deidentification/generated/ListProcessedDocumentsWithinAJob.java
[cancel_job]: java/com/azure/health/deidentification/generated/CancelADeIdentificationJob.java
[delete_job]: java/com/azure/health/deidentification/generated/DeleteADeIdentificationJob.java
[list_jobs]: java/com/azure/health/deidentification/generated/ListDeIdentificationJobs.java
