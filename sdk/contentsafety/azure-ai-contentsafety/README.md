# Azure ContentSafety client library for Java

[Azure AI Content Safety][contentsafety_overview] detects harmful user-generated and AI-generated content in applications and services. Content Safety includes several APIs that allow you to detect material that is harmful:

* Text Analysis API: Scans text for sexual content, violence, hate, and self-harm with multi-severity levels.
* Image Analysis API: Scans images for sexual content, violence, hate, and self-harm with multi-severity levels.
* Text Blocklist Management APIs: The default AI classifiers are sufficient for most content safety needs; however, you might need to screen for terms that are specific to your use case. You can create blocklists of terms to use with the Text API.

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]
- [Product documentation][product_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- You need an [Azure subscription][azure_subscription] to use this package.
- An [Azure AI Content Safety][contentsafety_overview] resource, if no existing resource, you could [create a new one](https://aka.ms/acs-create).

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-ai-contentsafety;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-contentsafety</artifactId>
    <version>1.0.5</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the client

#### Get the endpoint
You can find the endpoint for your Azure AI Content Safety service resource using the [Azure Portal][azure_portal] or [Azure CLI][azure_cli_endpoint_lookup]:

```bash
# Get the endpoint for the Azure AI Content Safety service resource
az cognitiveservices account show --name "<resource-name>" --resource-group "<resource-group-name>" --query "properties.endpoint"
```

#### Get the API key

The API key can be found in the [Azure Portal][azure_portal] or by running the following [Azure CLI][azure_cli_key_lookup] command:

```bash
az cognitiveservices account keys list --name "<resource-name>" --resource-group "<resource-group-name>"
```

#### Create a ContentSafetyClient/BlocklistClient with KeyCredential
```java com.azure.ai.contentsafety.createclientcredential
String endpoint = Configuration.getGlobalConfiguration().get("CONTENT_SAFETY_ENDPOINT");
String key = Configuration.getGlobalConfiguration().get("CONTENT_SAFETY_KEY");
ContentSafetyClient contentSafetyClient = new ContentSafetyClientBuilder()
    .credential(new KeyCredential(key))
    .endpoint(endpoint).buildClient();
BlocklistClient blocklistClient = new BlocklistClientBuilder()
    .credential(new KeyCredential(key))
    .endpoint(endpoint).buildClient();
```
#### Create a ContentSafetyClient/BlocklistClient with Microsoft Entra ID (formerly Azure Active Directory (AAD)) token credential
- Step 1: Enable Microsoft Entra ID for your resource   
Please refer to this Cognitive Services authentication document [Authenticate with Microsoft Entra ID.][authenticate_with_microsoft_entra_id] for the steps to enable AAD for your resource.
  The main steps are:
    - Create resource with a custom subdomain.
    - Create Service Principal and assign Cognitive Services User role to it.

- Step 2: Set the values of the client ID, tenant ID, and client secret of the Enable Microsoft Entra ID application as environment variables: `AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_CLIENT_SECRET`.

After setup, you can use [DefaultAzureCredential](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity#authenticate-with-defaultazurecredential)
can be used to authenticate the client.

DefaultAzureCredential will use the values from these environment variables.
```java com.azure.ai.contentsafety.createclienttoken
ContentSafetyClient contentSafetyClientOauth = new ContentSafetyClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint(endpoint).buildClient();
BlocklistClient blocklistClientOauth = new BlocklistClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint(endpoint).buildClient();
```

## Key concepts
### Available features
There are different types of analysis available from this service. The following table describes the currently available APIs.

|Feature  | Description                                                                                                                                                                                                           |
|---------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|Text Analysis API| Scans text for sexual content, violence, hate, and self-harm with multi-severity levels.                                                                                                                              |
|Image Analysis API| Scans images for sexual content, violence, hate, and self-harm with multi-severity levels.                                                                                                                            |
| Text Blocklist Management APIs| The default AI classifiers are sufficient for most content safety needs. However, you might need to screen for terms that are specific to your use case. You can create blocklists of terms to use with the Text API. |

### Harm categories
Content Safety recognizes four distinct categories of objectionable content.

|Category|Description|
|---------|---------|
|Hate |Hate and fairness-related harms refer to any content that attacks or uses pejorative or discriminatory language with reference to a person or identity group based on certain differentiating attributes of these groups including but not limited to race, ethnicity, nationality, gender identity and expression, sexual orientation, religion, immigration status, ability status, personal appearance, and body size.|
|Sexual |Sexual describes language related to anatomical organs and genitals, romantic relationships, acts portrayed in erotic or affectionate terms, pregnancy, physical sexual acts, including those portrayed as an assault or a forced sexual violent act against one's will, prostitution, pornography, and abuse.|
|Violence |Violence describes language related to physical actions intended to hurt, injure, damage, or kill someone or something; describes weapons, guns and related entities, such as manufactures, associations, legislation, and so on.|
|Self-harm |Self-harm describes language related to physical actions intended to purposely hurt, injure, damage one's body or kill oneself.|

Classification can be multi-labeled. For example, when a text sample goes through the text moderation model, it could be classified as both Sexual content and Violence.

### Severity levels
Every harm category the service applies also comes with a severity level rating. The severity level is meant to indicate the severity of the consequences of showing the flagged content.

**Text**: The current version of the text model supports the **full 0-7 severity scale**. By default, the response will output 4 values: 0, 2, 4, and 6. Each two adjacent levels are mapped to a single level. Users could use "outputType" in request and set it as "EightSeverityLevels" to get 8 values in output: 0,1,2,3,4,5,6,7.  
You can refer [text content severity levels definitions][text_severity_levels] for details. 

- [0,1] -> 0
- [2,3] -> 2
- [4,5] -> 4
- [6,7] -> 6

**Image**: The current version of the image model supports the **trimmed version of the full 0-7 severity scale**. The classifier only returns severities 0, 2, 4, and 6; each two adjacent levels are mapped to a single level. You can refer [image content severity levels definitions][image_severity_levels] for details.

- [0,1] -> 0
- [2,3] -> 2
- [4,5] -> 4
- [6,7] -> 6

### Text blocklist management
Following operations are supported to manage your text blocklist:
- Create or modify a blocklist
- List all blocklists
- Get a blocklist by blocklistName
- Add blockItems to a blocklist
- Remove blocklistItems from a blocklist
- List all blocklistItems in a blocklist by blocklistName
- Get a blocklistItem in a blocklist by blocklistItemId and blocklistName
- Delete a blocklist and all of its blocklistItems

You can set the blocklists you want to use when analyze text, then you can get blocklist match result from returned response.

## Examples
The following section provides several code snippets covering some of the most common Content Safety service tasks, including:

* [Analyze text](#analyze-text)
* [Analyze image](#analyze-image)
* [Manage text blocklist](#manage-text-blocklist)

### Analyze text

#### Analyze text without blocklists
```java com.azure.ai.contentsafety.analyzetext
String endpoint = Configuration.getGlobalConfiguration().get("CONTENT_SAFETY_ENDPOINT");
String key = Configuration.getGlobalConfiguration().get("CONTENT_SAFETY_KEY");
ContentSafetyClient contentSafetyClient = new ContentSafetyClientBuilder()
    .credential(new KeyCredential(key))
    .endpoint(endpoint).buildClient();

AnalyzeTextResult response = contentSafetyClient.analyzeText(new AnalyzeTextOptions("This is text example"));

for (TextCategoriesAnalysis result : response.getCategoriesAnalysis()) {
    System.out.println(result.getCategory() + " severity: " + result.getSeverity());
}
```

#### Analyze text with blocklists
```java com.azure.ai.contentsafety.analyzetextwithblocklist
// After you edit your blocklist, it usually takes effect in 5 minutes, please wait some time before analyzing with blocklist after editing.
ContentSafetyClient contentSafetyClient = new ContentSafetyClientBuilder()
    .credential(new KeyCredential(key))
    .endpoint(endpoint).buildClient();
AnalyzeTextOptions request = new AnalyzeTextOptions("I h*te you and I want to k*ll you");
request.setBlocklistNames(Arrays.asList(blocklistName));
request.setHaltOnBlocklistHit(true);

AnalyzeTextResult analyzeTextResult;
try {
    analyzeTextResult = contentSafetyClient.analyzeText(request);
} catch (HttpResponseException ex) {
    System.out.println("Analyze text failed.\nStatus code: " + ex.getResponse().getStatusCode() + ", Error message: " + ex.getMessage());
    throw ex;
}

if (analyzeTextResult.getBlocklistsMatch() != null) {
    System.out.println("\nBlocklist match result:");
    for (TextBlocklistMatch matchResult : analyzeTextResult.getBlocklistsMatch()) {
        System.out.println("BlocklistName: " + matchResult.getBlocklistName() + ", BlockItemId: " + matchResult.getBlocklistItemId() + ", BlockItemText: " + matchResult.getBlocklistItemText());
    }
}
```

### Analyze image
```java com.azure.ai.contentsafety.analyzeimage
String endpoint = Configuration.getGlobalConfiguration().get("CONTENT_SAFETY_ENDPOINT");
String key = Configuration.getGlobalConfiguration().get("CONTENT_SAFETY_KEY");

ContentSafetyClient contentSafetyClient = new ContentSafetyClientBuilder()
    .credential(new KeyCredential(key))
    .endpoint(endpoint).buildClient();

ContentSafetyImageData image = new ContentSafetyImageData();
String cwd = System.getProperty("user.dir");
String source = "/src/samples/resources/image.png";
image.setContent(BinaryData.fromBytes(Files.readAllBytes(Paths.get(cwd, source))));

AnalyzeImageResult response =
    contentSafetyClient.analyzeImage(new AnalyzeImageOptions(image));

for (ImageCategoriesAnalysis result : response.getCategoriesAnalysis()) {
    System.out.println(result.getCategory() + " severity: " + result.getSeverity());
}
```

### Manage text blocklist

#### Create or update text blocklist
```java com.azure.ai.contentsafety.createtextblocklist
String blocklistName = "TestBlocklist";
Map<String, String> description = new HashMap<>();
description.put("description", "Test Blocklist");
BinaryData resource = BinaryData.fromObject(description);
RequestOptions requestOptions = new RequestOptions();
Response<BinaryData> response =
    blocklistClient.createOrUpdateTextBlocklistWithResponse(blocklistName, resource, requestOptions);
if (response.getStatusCode() == 201) {
    System.out.println("\nBlocklist " + blocklistName + " created.");
} else if (response.getStatusCode() == 200) {
    System.out.println("\nBlocklist " + blocklistName + " updated.");
}
```
#### Add blockItems
```java com.azure.ai.contentsafety.addblockitems
String blockItemText1 = "k*ll";
String blockItemText2 = "h*te";
List<TextBlocklistItem> blockItems = Arrays.asList(new TextBlocklistItem(blockItemText1).setDescription("Kill word"),
    new TextBlocklistItem(blockItemText2).setDescription("Hate word"));
AddOrUpdateTextBlocklistItemsResult addedBlockItems = blocklistClient.addOrUpdateBlocklistItems(blocklistName,
    new AddOrUpdateTextBlocklistItemsOptions(blockItems));
if (addedBlockItems != null && addedBlockItems.getBlocklistItems() != null) {
    System.out.println("\nBlockItems added:");
    for (TextBlocklistItem addedBlockItem : addedBlockItems.getBlocklistItems()) {
        System.out.println("BlockItemId: " + addedBlockItem.getBlocklistItemId() + ", Text: " + addedBlockItem.getText() + ", Description: " + addedBlockItem.getDescription());
    }
}
```
#### List text blocklists
```java com.azure.ai.contentsafety.listtextblocklists
PagedIterable<TextBlocklist> allTextBlocklists = blocklistClient.listTextBlocklists();
System.out.println("\nList Blocklist:");
for (TextBlocklist blocklist : allTextBlocklists) {
    System.out.println("Blocklist: " + blocklist.getName() + ", Description: " + blocklist.getDescription());
}
```
#### Get text blocklist
```java com.azure.ai.contentsafety.gettextblocklist
TextBlocklist getBlocklist = blocklistClient.getTextBlocklist(blocklistName);
if (getBlocklist != null) {
    System.out.println("\nGet blocklist:");
    System.out.println("BlocklistName: " + getBlocklist.getName() + ", Description: " + getBlocklist.getDescription());
}
```
#### List blockItems
``` java com.azure.ai.contentsafety.listtextblocklistitems
PagedIterable<TextBlocklistItem> allBlockitems = blocklistClient.listTextBlocklistItems(blocklistName);
System.out.println("\nList BlockItems:");
for (TextBlocklistItem blocklistItem : allBlockitems) {
    System.out.println("BlockItemId: " + blocklistItem.getBlocklistItemId() + ", Text: " + blocklistItem.getText() + ", Description: " + blocklistItem.getDescription());
}
```
#### Get blockItem
```java com.azure.ai.contentsafety.gettextblocklistitem
String getBlockItemId = addedBlockItems.getBlocklistItems().get(0).getBlocklistItemId();
TextBlocklistItem getBlockItem = blocklistClient.getTextBlocklistItem(blocklistName, getBlockItemId);
System.out.println("\nGet BlockItem:");
System.out.println("BlockItemId: " + getBlockItem.getBlocklistItemId() + ", Text: " + getBlockItem.getText() + ", Description: " + getBlockItem.getDescription());
```
#### Remove blockItems
```java com.azure.ai.contentsafety.removeblockitems
String removeBlockItemId = addedBlockItems.getBlocklistItems().get(0).getBlocklistItemId();
List<String> removeBlockItemIds = new ArrayList<>();
removeBlockItemIds.add(removeBlockItemId);
blocklistClient.removeBlocklistItems(blocklistName, new RemoveTextBlocklistItemsOptions(removeBlockItemIds));
```
#### Delete text blocklist
```java com.azure.ai.contentsafety.deletetextblocklist
blocklistClient.deleteTextBlocklist(blocklistName);
```
## Troubleshooting
### General

Azure AI Content Safety client library will raise exceptions defined in [Azure Core][azure_core_exception]. Error codes are defined as below:

|Error Code	|Possible reasons	|Suggestions|
|-----------|-------------------|-----------|
|InvalidRequestBody	|One or more fields in the request body do not match the API definition.	|1. Check the API version you specified in the API call.<br>2. Check the corresponding API definition for the API version you selected.|
|InvalidResourceName	|The resource name you specified in the URL does not meet the requirements, like the blocklist name, blocklist term ID, etc.	|1. Check the API version you specified in the API call.<br>2. Check whether the given name has invalid characters according to the API definition.|
|ResourceNotFound	|The resource you specified in the URL may not exist, like the blocklist name.	|1. Check the API version you specified in the API call.<br>2. Double check the existence of the resource specified in the URL.|
|InternalError	|Some unexpected situations on the server side have been triggered.	|1. You may want to retry a few times after a small period and see it the issue happens again.<br>2. Contact Azure Support if this issue persists.|
|ServerBusy	|The server side cannot process the request temporarily.	|1. You may want to retry a few times after a small period and see it the issue happens again.<br>2.Contact Azure Support if this issue persists.|
|TooManyRequests	|The current RPS has exceeded the quota for your current SKU.	|1. Check the pricing table to understand the RPS quota.<br>2.Contact Azure Support if you need more QPS.|

## Next steps
### Additional documentation

For more extensive documentation on Azure Content Safety, see the [Azure AI Content Safety][contentsafety_overview] on docs.microsoft.com.

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[product_documentation]: https://aka.ms/acs-doc
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[contentsafety_overview]: https://aka.ms/acs-doc
[azure_portal]: https://ms.portal.azure.com/
[azure_cli_endpoint_lookup]: https://docs.microsoft.com/cli/azure/cognitiveservices/account?view=azure-cli-latest#az-cognitiveservices-account-show
[azure_cli_key_lookup]: https://docs.microsoft.com/cli/azure/cognitiveservices/account/keys?view=azure-cli-latest#az-cognitiveservices-account-keys-list
[authenticate_with_microsoft_entra_id]: https://learn.microsoft.com/azure/ai-services/authentication?tabs=powershell#authenticate-with-microsoft-entra-id
[text_severity_levels]: https://learn.microsoft.com/azure/ai-services/content-safety/concepts/harm-categories?tabs=definitions#text-content
[image_severity_levels]: https://learn.microsoft.com/azure/ai-services/content-safety/concepts/harm-categories?tabs=definitions#image-content
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcontentsafety%2Fazure-ai-contentsafety%2FREADME.png)
