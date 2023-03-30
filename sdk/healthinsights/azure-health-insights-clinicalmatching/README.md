# Azure Cognitive Services Health Insights Clinical Matching client library for Java

[Health Insights](https://review.learn.microsoft.com/azure/cognitive-services/health-decision-support/overview?branch=main) is an Azure Applied AI Service built with the Azure Cognitive Services Framework, that leverages multiple Cognitive Services, Healthcare API services and other Azure resources.

The [Clinical Matching model][clinical_matching_docs] receives patients data and clinical trials protocols, and provides relevant clinical trials based on eligibility criteria.

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link] with version 8 or above
- [Azure Subscription][azure_subscription]
- An existing Cognitive Services Health Insights instance.

For more information about creating the resource or how to get the location and sku information see [here][cognitive_resource_cli].

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-health-insights-clinicalmatching;current})

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-health-insights-clinicalmatching</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```

[//]: # ({x-version-update-end})

### Authenticate the client

In order to interact with the Health Insights Clinical Matching service, you'll need to create an instance of the [`ClinicalMatchingClient`][clinical_matching_client_class] class.  You will need an **endpoint** and an **API key** to instantiate a client object.  

#### Get API Key

You can obtain the endpoint and API key from the resource information in the [Azure Portal][azure_portal].

Alternatively, you can use the [Azure CLI][azure_cli] snippet below to get the API key from the Health Insights resource.

```bash
az cognitiveservices account keys list --resource-group <your-resource-group-name> --name <your-resource-name>
```

#### Create a ClinicalMatchingClient with an API Key Credential

Once you have the value for the API key, you can pass it as a string into an instance of **AzureKeyCredential**. Use the key as the credential parameter
to authenticate the client:

```Java com.azure.health.insights.cancerprofiling.clinicalmatching
String endpoint = Configuration.getGlobalConfiguration().get("AZURE_HEALTH_INSIGHTS_ENDPOINT");
String apiKey = Configuration.getGlobalConfiguration().get("AZURE_HEALTH_INSIGHTS_API_KEY");

ClinicalMatchingAsyncClient asyncClient = new ClinicalMatchingClientBuilder()
    .endpoint(endpoint)
    .serviceVersion(ClinicalMatchingServiceVersion.getLatest())
    .credential(new AzureKeyCredential(apiKey))
    .buildAsyncClient();
```

## Key concepts

Trial Matcher provides the user of the services two main modes of operation: patients centric and clinical trial centric.
- On patient centric mode, the Trial Matcher model bases the patient matching on the clinical condition, location, priorities, eligibility criteria, and other criteria that the patient and/or service users may choose to prioritize. The model helps narrow down and prioritize the set of relevant clinical trials to a smaller set of trials to start with, that the specific patient appears to be qualified for.
- On clinical trial centric, the Trial Matcher is finding a group of patients potentially eligible to a clinical trial. The Trial Matcher narrows down the patients, first filtered on clinical condition and selected clinical observations, and then focuses on those patients who met the baseline criteria, to find the group of patients that appears to be eligible patients to a trial.

## Examples

Finding potential eligible trials for a patient.
<!--
- [SampleMatchTrialsSync.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthinsights/azure-health-insights-clinicalmatching/src/samples/java/com/azure/health/insights/clinicalmatching/SampleMatchTrialsSync.java).
-->
```Java com.azure.health.insights.clinicalmatching.findtrials
// Construct Patient
PatientRecord patient1 = new PatientRecord("patient_1");
PatientInfo patientInfo = new PatientInfo();
patientInfo.setBirthDate(LocalDate.parse("1965-12-26"));
patientInfo.setSex(PatientInfoSex.MALE);
final List<ClinicalCodedElement> clinicalInfo = new ArrayList<ClinicalCodedElement>();
patientInfo.setClinicalInfo(clinicalInfo);
patient1.setInfo(patientInfo);

final String system = "http://www.nlm.nih.gov/research/umls";
// Attach clinical info to the patient
clinicalInfo.add(createClinicalCodedElement(system, "C0006826", "Malignant Neoplasms", "true"));
clinicalInfo.add(createClinicalCodedElement(system, "C1522449", "Therapeutic radiology procedure", "true"));
clinicalInfo.add(createClinicalCodedElement(system, "C1512162", "Eastern Cooperative Oncology Group", "1"));
clinicalInfo.add(createClinicalCodedElement(system, "C0019693", "HIV Infections", "false"));
clinicalInfo.add(createClinicalCodedElement(system, "C1300072", "Tumor stage", "2"));
clinicalInfo.add(createClinicalCodedElement(system, "METASTATIC", "metastatic", "true"));
clinicalInfo.add(createClinicalCodedElement(system, "C0019163", "Hepatitis B", "false"));
clinicalInfo.add(createClinicalCodedElement(system, "C0018802", "Congestive heart failure", "true"));
clinicalInfo.add(createClinicalCodedElement(system, "C0019196", "Hepatitis C", "false"));
clinicalInfo.add(createClinicalCodedElement(system, "C0220650", "Metastatic malignant neoplasm to brain", "true"));

// Create registry filter
ClinicalTrialRegistryFilter registryFilters = new ClinicalTrialRegistryFilter();
// Limit the trial to a specific patient condition ("Non-small cell lung cancer")
registryFilters.setConditions(Arrays.asList("Non-small cell lung cancer"));
// Limit the clinical trial to a certain phase, phase 1
registryFilters.setPhases(Arrays.asList(ClinicalTrialPhase.PHASE1));
// Specify the clinical trial registry source as ClinicalTrials.Gov
registryFilters.setSources(Arrays.asList(ClinicalTrialSource.CLINICALTRIALS_GOV));
// Limit the clinical trial to a certain location, in this case California, USA

GeographicLocation location = new GeographicLocation("United States");
location.setCity("Gilbert");
location.setState("Arizona");
registryFilters.setFacilityLocations(Arrays.asList(location));
// Limit the trial to a specific study type, interventional
registryFilters.setStudyTypes(Arrays.asList(ClinicalTrialStudyType.INTERVENTIONAL));

// Construct ClinicalTrial instance and attach the registry filter to it.
ClinicalTrials clinicalTrials = new ClinicalTrials();
clinicalTrials.setRegistryFilters(Arrays.asList(registryFilters));

// Create TrialMatcherData
TrialMatcherModelConfiguration configuration = new TrialMatcherModelConfiguration(clinicalTrials);
TrialMatcherData trialMatcherData = new TrialMatcherData(Arrays.asList(patient1));
trialMatcherData.setConfiguration(configuration);

PollerFlux<TrialMatcherResult, TrialMatcherResult> asyncPoller = asyncClient.beginMatchTrials(trialMatcherData);
```

## Troubleshooting

## Next steps

To see the full example source files, see:
<!--
This code sample show common scenario operation with the Azure Health Insights Clinical Matching library. More samples can be found under the [samples](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthinsights/azure-health-insights-clinicalmatching/src/samples/java/com/azure/health/insights/) directory.
-->

## Additional documentation
For more extensive documentation on Azure Health Insights Clinical Matching, see the [Clinical Matching documentation][clinical_matching_docs] on docs.microsoft.com.


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
[clinical_matching_docs]: https://review.learn.microsoft.com/azure/cognitive-services/health-decision-support/trial-matcher/overview?branch=main
<!--
[clinical_matching_client_class]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/healthinsights/azure-health-insights-clinicalmatching/src/main/java/com/azure/health/clinicalmatching/ClinicalMatchingClient.java
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%healthinsights%2Fazure-health-insights-clinicalmatching%2FREADME.png)
-->
