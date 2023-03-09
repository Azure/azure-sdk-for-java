# Azure Azure Health Insights Clinical Matching client library for Java
<!--
[Health Insights](https://review.learn.microsoft.com/en-us/azure/cognitive-services/health-decision-support/overview?branch=main) is an Azure Applied AI Service built with the Azure Cognitive Services Framework, that leverages multiple Cognitive Services, Healthcare API services and other Azure resources.
The [Clinical Matching model](https://review.learn.microsoft.com/en-us/azure/cognitive-services/health-decision-support/trial-matcher/overview?branch=main) receives patients data and clinical trials protocols, and provides relevant clinical trials based on eligibility criteria.
-->
## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk] with version 8 or above
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

In order to interact with the Health Insighs Clinical Matching service, you'll need to create an instance of the [`ClinicalMatchingClient`][clinical_matching_client_class] class.  You will need an **endpoint** and an **API key** to instantiate a client object.  

#### Get API Key

You can obtain the endpoint and API key from the resource information in the [Azure Portal][azure_portal].

Alternatively, you can use the [Azure CLI][azure_cli] snippet below to get the API key from the Health Insights resource.

```bash
az cognitiveservices account keys list --resource-group <your-resource-group-name> --name <your-resource-name>
```

#### Create a ClinicalMatchingClient with an API Key Credential

Once you have the value for the API key, you can pass it as a string into an instance of **AzureKeyCredential**. Use the key as the credential parameter
to authenticate the client:

```Java Snippet:
String endpoint = "endpoint";
String apiKey = "apiKey";
ClinicalMatchingAsyncClient asyncClient = new ClinicalMatchingClientBuilder()
    .endpoint(endpoint)
    .serviceVersion(AzureHealthInsightsServiceVersion.getLatest())
    .httpClient(HttpClient.createDefault(new HttpClientOptions()))
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
```Java readme-sample-MatchTrialsSync
ClinicalMatchingClient syncClient = new ClinicalMatchingClientBuilder()
    .endpoint(endpoint)
    .serviceVersion(AzureHealthInsightsServiceVersion.getLatest())
    .httpClient(HttpClient.createDefault(new HttpClientOptions()))
    .credential(new AzureKeyCredential(apiKey))
    .buildClient();

SyncPoller<BinaryData, BinaryData> syncPoller = syncClient.beginMatchTrials(BinaryData.fromObject(trialMatcherOptions), null);
PollResponse<BinaryData> pollerResponse = syncPoller.waitForCompletion();

LongRunningOperationStatus status = pollerResponse.getStatus();
System.out.println("Status " + status);
TrialMatcherResult tmRespone = pollerResponse.getValue().toObject(TypeReference.createInstance(TrialMatcherResult.class));
if (status == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
    TrialMatcherResults tmResults = tmRespone.getResults();
    tmResults.getPatients().forEach(patientResult -> {
        System.out.println("Inferences of Patient " + patientResult.getId());
        patientResult.getInferences().forEach(inference -> {
            System.out.println("Trial Id " + inference.getId());
            System.out.println("Type: " + inference.getType() + " Value: " + inference.getValue());
            System.out.println("Description " + inference.getDescription());
        });
    });
} else if (status == LongRunningOperationStatus.FAILED) {
    List<ResponseError> errors = tmRespone.getErrors();
    errors.forEach(error -> {
        System.out.println(error.getCode() + " : " + error.getMessage());
    });
}

```

## Troubleshooting

## Next steps

To see the full example source files, see:
<!--
This code sample show common scenario operation with the Azure Health Insights Clinical Matching library. More samples can be found under the [samples](https://github.com/Azure/azure-sdk-for-python/blob/main/sdk/healthinsights/azure-health-insights-clinicalmatching/src/samples/java/com/azure/health/insights/) directory.
-->

### Additional documentation
<!--
For more extensive documentation on Azure Health Insights Clinical Matching, see the [Clinical Matching documentation](https://review.learn.microsoft.com/en-us/azure/cognitive-services/health-decision-support/trial-matcher/?branch=main) on docs.microsoft.com.
-->

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
<!--
[clinical_matching_client_class]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/healthinsights/azure-health-insights-clinicalmatching/src/main/java/com/azure/health/clinicalmatching/ClinicalMatchingClient.java
-->
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_portal]: https://portal.azure.com