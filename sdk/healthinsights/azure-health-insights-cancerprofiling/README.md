# Azure Azure Health Insights Cancer Profiling client library for Java
<!--
[Health Insights](https://review.learn.microsoft.com/en-us/azure/cognitive-services/health-decision-support/overview?branch=main) is an Azure Applied AI Service built with the Azure Cognitive Services Framework, that leverages multiple Cognitive Services, Healthcare API services and other Azure resources.
The [Cancer Profiling model](https://review.learn.microsoft.com/en-us/azure/cognitive-services/health-decision-support/oncophenotype/overview?branch=main) receives clinical records of oncology patients and outputs cancer staging, such as clinical stage TNM categories and pathologic stage TNM categories as well as tumor site, histology.
-->

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk] with version 8 or above
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

In order to interact with the Health Insighs Cancer Profiling service, you'll need to create an instance of the [`CancerProfilingClient`][cancer_profiling_client_class] class.  You will need an **endpoint** and an **API key** to instantiate a client object.  

#### Get API Key

You can obtain the endpoint and API key from the resource information in the [Azure Portal][azure_portal].

Alternatively, you can use the [Azure CLI][azure_cli] snippet below to get the API key from the Health Insights resource.

```bash
az cognitiveservices account keys list --resource-group <your-resource-group-name> --name <your-resource-name>
```

#### Create a CancerProfilingClient with an API Key Credential

Once you have the value for the API key, you can pass it as a string into an instance of **AzureKeyCredential**. Use the key as the credential parameter
to authenticate the client:

```Java Snippet:
String endpoint = "endpoint";
String apiKey = "apiKey";
OncoPhenotypeAsyncClient asyncClient = new CancerProfilingClientBuilder()
    .endpoint(endpoint)
    .serviceVersion(AzureHealthInsightsServiceVersion.getLatest())
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

```Java readme-sample-InferCancerProfile
CancerProfilingAsyncClient asyncClient = new CancerProfilingClientBuilder()
    .endpoint(endpoint)
    .serviceVersion(AzureHealthInsightsServiceVersion.getLatest())
    .credential(new AzureKeyCredential(apiKey))
    .buildAsyncClient();

    PollerFlux<OncoPhenotypeResult, OncoPhenotypeResult> asyncPoller = asyncClient.beginInferCancerProfile(oncoPhenotypeData);
    OncoPhenotypeResult result = asyncPoller.blockLast().getValue();

    OncoPhenotypeResults oncoResults = result.getResults();
    oncoResults.getPatients().forEach(patient_result -> {
        System.out.println("\n==== Inferences of Patient " + patient_result.getId() + " ====");
        patient_result.getInferences().forEach(onco_inference -> {
            System.out.println("\n=== Clinical Type: " + onco_inference.getType() + "  Value: " + onco_inference.getValue() + "   ConfidenceScore: " + onco_inference.getConfidenceScore() + " ===");
            onco_inference.getEvidence().forEach(evidence -> {
                if (evidence.getPatientDataEvidence() != null) {
                    ClinicalNoteEvidence dataEvidence = evidence.getPatientDataEvidence();
                    System.out.println("Evidence " + dataEvidence.getId() + " " + dataEvidence.getOffset() + " " + dataEvidence.getLength() + " " + dataEvidence.getText());
                }
                if (evidence.getPatientInfoEvidence() != null) {
                    ClinicalCodedElement infoEvidence = evidence.getPatientInfoEvidence();
                    System.out.println("Evidence " + infoEvidence.getSystem() + " " + infoEvidence.getCode() + " " + infoEvidence.getName() + " " + infoEvidence.getValue());
                }
            });
        });
    });


```

## Troubleshooting

## Next steps
<!--
This code sample show common scenario operation with the Azure Health Insights Cancer Profiling library. More samples can be found under the [samples](https://github.com/Azure/azure-sdk-for-python/blob/main/sdk/healthinsights/azure-health-insights-cancerprofiling/src/samples/java/com/azure/health/insights/) directory.
-->
### Additional documentation
<!--
For more extensive documentation on Azure Health Insights Cancer Profiling, see the [Cancer Profiling documentation](https://review.learn.microsoft.com/en-us/azure/cognitive-services/health-decision-support/oncophenotype/overview?branch=main) on docs.microsoft.com.
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
[cancer_profiling_client_class]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/healthinsights/azure-health-insights-cancerprofiling/src/main/java/com/azure/health/cancerprofiling/CancerProfilingClient.java
-->
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_portal]: https://portal.azure.com
