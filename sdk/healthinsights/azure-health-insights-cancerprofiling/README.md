# Azure Cognitive Services Health Insights Cancer Profiling client library for Java

[Health Insights](https://review.learn.microsoft.com/azure/azure-health-insights/?branch=release-azure-health-insights) is an Azure Applied AI Service built with the Azure Cognitive Services Framework, that leverages multiple Cognitive Services, Healthcare API services and other Azure resources.

The [Cancer Profiling model][cancer_profiling_docs] receives clinical records of oncology patients and outputs cancer staging, such as clinical stage TNM categories and pathologic stage TNM categories as well as tumor site, histology.


## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link] with version 8 or above
- [Azure Subscription][azure_subscription]
- An existing Cognitive Services Health Insights instance.

For more information about creating the resource or how to get the location and sku information see [here][cognitive_resource_cli].

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-health-insights-cancerprofiling;current})

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-health-insights-cancerprofiling</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```

[//]: # ({x-version-update-end})

### Authenticate the client

In order to interact with the Health Insights Cancer Profiling service, you'll need to create an instance of the [`CancerProfilingClient`][cancer_profiling_client_class] class. You will need an **endpoint** and an **API key** to instantiate a client object.  

#### Get API Key

You can obtain the endpoint and API key from the resource information in the [Azure Portal][azure_portal].

Alternatively, you can use the [Azure CLI][azure_cli] snippet below to get the API key from the Health Insights resource.

```bash
az cognitiveservices account keys list --resource-group <your-resource-group-name> --name <your-resource-name>
```

#### Create a CancerProfilingClient with an API Key Credential

Once you have the value for the API key, you can pass it as a string into an instance of **AzureKeyCredential**. Use the key as the credential parameter to authenticate the client:

```Java com.azure.health.insights.cancerprofiling.buildasyncclient
String endpoint = Configuration.getGlobalConfiguration().get("AZURE_HEALTH_INSIGHTS_ENDPOINT");
String apiKey = Configuration.getGlobalConfiguration().get("AZURE_HEALTH_INSIGHTS_API_KEY");

CancerProfilingAsyncClient asyncClient = new CancerProfilingClientBuilder()
    .endpoint(endpoint)
    .serviceVersion(CancerProfilingServiceVersion.getLatest())
    .credential(new AzureKeyCredential(apiKey))
    .buildAsyncClient();
```

## Key concepts

The Cancer Profiling model allows you to infer cancer attributes such as tumor site, histology, clinical stage TNM categories and pathologic stage TNM categories from unstructured clinical documents.

## Examples

Infer key cancer attributes such as tumor site, histology, clinical stage TNM categories and pathologic stage TNM categories from a patient's unstructured clinical documents.
<!--
- [SampleInferCancerProfile.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthinsights/azure-health-insights-cancerprofiling/src/samples/java/com/azure/health/insights/cancerprofiling/SampleInferCancerProfile.java).
-->

```Java com.azure.health.insights.cancerprofiling.infercancerprofile
// Construct Patient
PatientRecord patient1 = new PatientRecord("patient_id");
PatientInfo patientInfo = new PatientInfo();
patientInfo.setBirthDate(LocalDate.parse("1965-12-26"));
patientInfo.setSex(PatientInfoSex.FEMALE);
patient1.setInfo(patientInfo);
LinkedList<PatientDocument> patientDocuments = new LinkedList<>();
patient1.setData(patientDocuments);

// Add imaging document
final String docContent1 = String.join(System.getProperty("line.separator"),
    "15.8.2021",
    "Jane Doe 091175-8967",
    "42 year old female, married with 3 children, works as a nurse. ",
    "Healthy, no medications taken on a regular basis.",
    "PMHx is significant for migraines with aura, uses Mirena for contraception.",
    "Smoking history of 10 pack years (has stopped and relapsed several times).",
    "She is in c/o 2 weeks of productive cough and shortness of breath.",
    "She has a fever of 37.8 and general weakness. ",
    "Denies night sweats and rash. She denies symptoms of rhinosinusitis, asthma, and heartburn. ",
    "On PE:",
    "GENERAL: mild pallor, no cyanosis. Regular breathing rate. ",
    "LUNGS: decreased breath sounds on the base of the right lung. Vesicular breathing.",
    " No crackles, rales, and wheezes. Resonant percussion. ",
    "PLAN: ",
    "Will be referred for a chest x-ray. ",
    "======================================",
    "CXR showed mild nonspecific opacities in right lung base. ",
    "PLAN:",
    "Findings are suggestive of a working diagnosis of pneumonia. The patient is referred to a follow-up CXR in 2 weeks.");

PatientDocument patientDocument1 = new PatientDocument(
    DocumentType.NOTE,
    "doc1",
    new DocumentContent(DocumentContentSourceType.INLINE, docContent1));

patientDocument1.setClinicalType(ClinicalDocumentType.IMAGING);
patientDocument1.setLanguage("en");
patientDocument1.setCreatedDateTime(OffsetDateTime.parse("2021-08-15T10:15:30+01:00"));
patient1.getData().add(patientDocument1);

// Add Pathology documents
String docContent2 = String.join(System.getProperty("line.separator"),
    "Oncology Clinic ",
    "20.10.2021",
    "Jane Doe 091175-8967",
    "42-year-old healthy female who works as a nurse in the ER of this hospital. ",
    "First menstruation at 11 years old. First delivery- 27 years old. She has 3 children.",
    "Didn’t breastfeed. ",
    "Contraception- Mirena.",
    "Smoking- 10 pack years. ",
    "Mother- Belarusian. Father- Georgian. ",
    "About 3 months prior to admission, she stated she had SOB and was febrile. ",
    "She did a CXR as an outpatient which showed a finding in the base of the right lung- possibly an infiltrate.",
    "She was treated with antibiotics with partial response. ",
    "6 weeks later a repeat CXR was performed- a few solid dense findings in the right lung. ",
    "Therefore, she was referred for a PET-CT which demonstrated increased uptake in the right breast, lymph nodes on the right a few areas in the lungs and liver. ",
    "On biopsy from the lesion in the right breast- triple negative adenocarcinoma. Genetic testing has not been done thus far. ",
    "Genetic counseling- the patient denies a family history of breast, ovary, uterus, and prostate cancer. Her mother has chronic lymphocytic leukemia (CLL). ",
    "She is planned to undergo genetic tests because the aggressive course of the disease, and her young age. ",
    "Impression:",
    "Stage 4 triple negative breast adenocarcinoma. ",
    "Could benefit from biological therapy. ",
    "Different treatment options were explained- the patient wants to get a second opinion.");

PatientDocument patientDocument2 = new PatientDocument(DocumentType.NOTE,
    "doc2",
    new DocumentContent(DocumentContentSourceType.INLINE, docContent2));
patientDocument2.setClinicalType(ClinicalDocumentType.PATHOLOGY);
patientDocument2.setLanguage("en");
patientDocument2.setCreatedDateTime(OffsetDateTime.parse("2021-10-20T22:00:00.00Z"));
patient1.getData().add(patientDocument2);

String docContent3 = String.join(System.getProperty("line.separator"),
    "PATHOLOGY REPORT",
    "                          Clinical Information",
    "Ultrasound-guided biopsy; A. 18 mm mass; most likely diagnosis based on imaging:  IDC",
    "                               Diagnosis",
    " A.  BREAST, LEFT AT 2:00 4 CM FN; ULTRASOUND-GUIDED NEEDLE CORE BIOPSIES:",
    " - Invasive carcinoma of no special type (invasive ductal carcinoma), grade 1",
    " Nottingham histologic grade:  1/3 (tubules 2; nuclear grade 2; mitotic rate 1; total score;  5/9)",
    " Fragments involved by invasive carcinoma:  2",
    " Largest measurement of invasive carcinoma on a single fragment:  7 mm",
    " Ductal carcinoma in situ (DCIS):  Present",
    " Architectural pattern:  Cribriform",
    " Nuclear grade:  2-",
    "                  -intermediate",
    " Necrosis:  Not identified",
    " Fragments involved by DCIS:  1",
    " Largest measurement of DCIS on a single fragment:  Span 2 mm",
    " Microcalcifications:  Present in benign breast tissue and invasive carcinoma",
    " Blocks with invasive carcinoma:  A1",
    " Special studies: Pending");

PatientDocument patientDocument3 = new PatientDocument(DocumentType.NOTE,
                                                    "doc3",
                                                    new DocumentContent(DocumentContentSourceType.INLINE, docContent3));
patientDocument3.setClinicalType(ClinicalDocumentType.PATHOLOGY);
patientDocument3.setLanguage("en");
patientDocument3.setCreatedDateTime(OffsetDateTime.parse("2022-01-01T10:15:30+01:00"));

patient1.getData().add(patientDocument3);

// Set configuration to include evidence for the cancer staging inferences
OncoPhenotypeModelConfiguration configuration = new OncoPhenotypeModelConfiguration();
configuration.setIncludeEvidence(true);

// Construct the request with the patient and configration
OncoPhenotypeData oncoPhenotypeData = new OncoPhenotypeData(Arrays.asList(patient1));
oncoPhenotypeData.setConfiguration(configuration);

PollerFlux<OncoPhenotypeResult, OncoPhenotypeResult> asyncPoller = asyncClient.beginInferCancerProfile(oncoPhenotypeData);
```

## Troubleshooting

## Next steps
<!--
This code sample show common scenario operation with the Azure Health Insights Cancer Profiling library. More samples can be found under the [samples](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthinsights/azure-health-insights-cancerprofiling/src/samples/java/com/azure/health/insights/) directory.
-->

## Additional documentation
For more extensive documentation on Azure Health Insights Cancer Profiling, see the [Cancer Profiling documentation][cancer_profiling_docs] on docs.microsoft.com.
## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit [cla.microsoft.com][cla].

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][code_of_conduct]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[cla]: https://cla.microsoft.com
[code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[azure_subscription]: https://azure.microsoft.com/free/
[cognitive_resource_cli]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account-cli
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable

[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_portal]: https://portal.azure.com
[cancer_profiling_docs]: https://review.learn.microsoft.com/azure/cognitive-services/health-decision-support/oncophenotype/overview?branch=main
<!--
[cancer_profiling_client_class]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/healthinsights/azure-health-insights-cancerprofiling/src/main/java/com/azure/health/cancerprofiling/CancerProfilingClient.java
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%healthinsights%2Fazure-health-insights-cancerprofiling%2FREADME.png)
-->
