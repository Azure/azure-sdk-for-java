# Azure Cognitive Services Health Insights Radiology Insights client library for Java


[Health Insights][health_insights] is an Azure Applied AI Service built with the Azure Cognitive Services Framework, that leverages multiple Cognitive Services, Healthcare API services and other Azure resources.

Radiology Insights is a model that aims to provide quality checks as feedback on errors and inconsistencies (mismatches) and ensures critical findings are identified and communicated using the full context of the report. Follow-up recommendations and clinical findings with measurements (sizes) documented by the radiologist are also identified.

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- An existing Cognitive Services Health Insights instance.

For more information about creating the resource or how to get the location and sku information see [here][cognitive_resource_cli].

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-health-insights-radiologyinsights;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-health-insights-radiologyinsights</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the client

In order to interact with the Health Insights Clinical Matching service, you'll need to create an instance of the RadiologyInsightsClient class.  You will need an **endpoint** and an **API key** to instantiate a client object.  

#### Get API Key

You can obtain the endpoint and API key from the resource information in the [Azure Portal][azure_portal].

Alternatively, you can use the [Azure CLI][azure_cli] snippet below to get the API key from the Health Insights resource.

```bash
az cognitiveservices account keys list --resource-group <your-resource-group-name> --name <your-resource-name>
```

#### Create a RadiologyInsightsClient with an API Key Credential

Once you have the value for the API key, you can pass it as a string into an instance of **AzureKeyCredential**. Use the key as the credential parameter
to authenticate the client:

```java
String endpoint = Configuration.getGlobalConfiguration().get("AZURE_HEALTH_INSIGHTS_ENDPOINT");
String apiKey = Configuration.getGlobalConfiguration().get("AZURE_HEALTH_INSIGHTS_API_KEY");

RadiologyInsightsAsyncClient radiologyInsightsAsyncClient = new RadiologyInsightsClientBuilder()
	.endpoint(endpoint)
	.serviceVersion(AzureHealthInsightsServiceVersion.getLatest())
	.credential(new AzureKeyCredential(apiKey))
	.buildAsyncClient();
```

## Key concepts

Radiology Insights currently supports one document from one patient. Please take a look here for more detailed information about the inferences this service produces.  

## Examples

### Create a RadiologyInsights request and get the result using an asynchronous client

For an example how to create a client, a request and get the result see the example in the sample folder.

### Get Critical Result Inference information
```java
List<RadiologyInsightsPatientResult> patientResults = radiologyInsightsResult.getPatientResults();
	for (RadiologyInsightsPatientResult patientResult : patientResults) {
		List<FhirR4Extendible1> inferences = patientResult.getInferences();
		for (FhirR4Extendible1 inference : inferences) {
			if (inference instanceof CriticalResultInference) {
				CriticalResultInference criticalResultInference = (CriticalResultInference) inference;
				String description = criticalResultInference.getResult().getDescription();
				System.out.println("Critical Result Inference found: "+description);					
			}
		}
	}
```
For detailed conceptual information of this and other inferences please read more here.

## Troubleshooting

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[health_insights]: https://learn.microsoft.com/azure/azure-health-insights/overview
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[cognitive_resource_cli]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account-cli
[azure_cli]: https://docs.microsoft.com/cli/azure
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_portal]: https://portal.azure.com

![Impressions]: https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fhealthinsights%2Fazure-health-insights-radiologyinsights%2FREADME.png
