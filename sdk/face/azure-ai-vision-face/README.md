# Azure AI Vision Face client library for Java

The Azure AI Face service provides AI algorithms that detect, recognize, and analyze human faces in images. It includes the following main features:

- Face detection and analysis
- Liveness detection
- Face recognition
  - Face verification ("one-to-one" matching)
- Find similar faces
- Group faces

[Source code][source_code]
| [Package (Java)][face_java_package_maven]
| [API reference documentation][face_ref_java_docs]
| [Product documentation][face_product_docs]
| [Samples][face_samples]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk_link] with version 8 or above
- You need an [Azure subscription][azure_sub] to use this package and either
  Your Azure account must have a `Cognitive Services Contributor` role assigned in order for you to agree to the responsible AI terms and create a resource. To get this role assigned to your account, follow the steps in the [Assign roles][steps_assign_an_azure_role] documentation, or contact your administrator.
  * an [Azure Face account][azure_portal_list_face_account] or
  * an [Azure AI services multi-service account][azure_portal_list_cognitive_service_account]

### Create a Face or an Azure AI services multi-service account

Azure AI Face supports both [multi-service][azure_cognitive_service_account] and single-service access. Create an Azure AI services multi-service account if you plan to access multiple Azure AI services under a single endpoint/key. For Face access only, create a Face resource.

* To create a new Face or Azure AI services multi-service account, you can use [Azure Portal][azure_portal_create_face_account], [Azure PowerShell][quick_start_create_account_via_azure_powershell], or [Azure CLI][quick_start_create_account_via_azure_cli].

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-ai-vision-face;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-vision-face</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the client

In order to interact with the Face service, you will need to create an instance of a client class,
[FaceAsyncClient][face_client_async] or [FaceClient][face_client] by using [FaceClientBuilder][face_client_builder].
[FaceSessionAsyncClient][face_session_client_async] or [FaceSessionClient][face_session_client] by using [FaceSessionClientBuilder][face_session_client_builder].

An **endpoint** and **credential** are necessary to instantiate the client object.

#### Get the endpoint and API keys

You can find the endpoint and keys for your Face resource using the [Azure Portal][azure_sdk_java_azure_key_credential] or [Azure CLI][get_endpoint_via_azure_cli]:

```bash
# Get the endpoint for the Face resource
az cognitiveservices account show --name "resource-name" --resource-group "resource-group-name" --query "properties.endpoint"
```

Either a regional endpoint or a custom subdomain can be used for authentication. They are formatted as follows:

```
Regional endpoint: https://<region>.api.cognitive.microsoft.com/
Custom subdomain: https://<resource-name>.cognitiveservices.azure.com/
```

A regional endpoint is the same for every resource in a region. A complete list of supported regional endpoints can be consulted [here][regional_endpoints]. Please note that regional endpoints do not support Microsoft Entra ID authentication. If you'd like migrate your resource to use custom subdomain, follow the instructions [here][migrate_to_custom_subdomain]

A custom subdomain, on the other hand, is a name that is unique to the Face resource. They can only be used by [single-service resources][azure_portal_create_face_account].

```bash
# Get the API keys for the Face resource
az cognitiveservices account keys list --name "<resource-name>" --resource-group "<resource-group-name>"
```
#### Create the client with a Microsoft Entra ID credential

`AzureKeyCredential` authentication is used in the examples in this getting started guide because of its' simplicity, but we recommend to authenticate with Microsoft Entra ID using the [azure-identity][azure_sdk_java_default_azure_credential] library. Microsoft Entra ID is more secure and reliable.
Note that regional endpoints do not support AAD authentication. Create a [custom subdomain][custom_subdomain] name for your resource in order to use this type of authentication.

To use the [DefaultAzureCredential][azure_sdk_java_default_azure_credential] type shown below, or other credential types provided with the Azure SDK, please add the `azure-identity` package:

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.13.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

You will also need to [register an Microsoft Entra application and grant access][register_aad_app] to Face by assigning the `"Cognitive Services User"` role to your service principal.

Once completed, set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables:
`AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_CLIENT_SECRET`.

```java com.azure.ai.vision.face.readme.aadAuthentication
//DefaultAzureCredential will use the values from these environment
//variables: AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET
String endpoint = "https://<my-custom-subdomain>.cognitiveservices.azure.com/";
FaceClient client = new FaceClientBuilder()
    .endpoint(endpoint)
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

#### Create the client with AzureKeyCredential

To use an API key as the `credential` parameter, pass the key as a string into an instance of [AzureKeyCredential][azure_sdk_java_azure_key_credential].

```java com.azure.ai.vision.face.readme.keyAuthentication
String endpoint = "https://<my-custom-subdomain>.cognitiveservices.azure.com/";
String accountKey = "<api_key>";
FaceClient client = new FaceClientBuilder()
    .endpoint(endpoint)
    .credential(new KeyCredential(accountKey))
    .buildClient();
```

## Key concepts

### FaceClient

`FaceClient` provides operations for:

 - Face detection and analysis: Detect human faces in an image and return the rectangle coordinates of their locations,
   and optionally with landmarks, and face-related attributes. This operation is required as a first step in all the
   other face recognition scenarios.
 - Face recognition: Confirm that a user is who they claim to be based on how closely their face data matches the target face.
   It includes Face verification ("one-to-one" matching) and Face identification ("one-to-many" matching).
 - Finding similar faces from a smaller set of faces that look similar to the target face.
 - Grouping faces into several smaller groups based on similarity.

### FaceSessionClient

`FaceSessionClient` is provided to interact with sessions which is used for Liveness detection.

 - Create, query, and delete the session.
 - Query the liveness and verification result.
 - Query the audit result.

## Examples

The following section provides several code snippets covering some of the most common Face tasks, including:

* [Detecting faces in an image](#face-detection "Face Detection")
* [Determining if a face in an video is real (live) or fake (spoof)](#liveness-detection "Liveness Detection")

### Face Detection
Detect faces and analyze them from an binary data. The latest model is the most accurate and recommended to be used. For the detailed differences between different versions of Detection and Recognition model, please refer to the following links.
* [Detection model][evaluate_different_detection_models]
* [Recognition model][recommended_recognition_model]

```java com.azure.ai.vision.face.readme.detectFace
FaceClient client = new FaceClientBuilder()
    .endpoint(endpoint)
    .credential(new KeyCredential(accountKey))
    .buildClient();

String imagePathString = Resources.TEST_IMAGE_PATH_DETECT_SAMPLE_IMAGE;
Path path = Paths.get(imagePathString);
BinaryData imageData = BinaryData.fromFile(path);
List<FaceAttributeType> attributeTypes = Arrays.asList(
    FaceAttributeType.ModelDetection03.HEAD_POSE, FaceAttributeType.ModelDetection03.MASK, FaceAttributeType.ModelRecognition04.QUALITY_FOR_RECOGNITION);

List<FaceDetectionResult> results = client.detect(
    imageData, FaceDetectionModel.DETECTION_03, FaceRecognitionModel.RECOGNITION_04, true,
    attributeTypes, true, true, 120);

for (int i = 0, size = results.size(); i < size; i++) {
    System.out.println("----- Detection result: #" + i + " -----");
    FaceDetectionResult result = results.get(i);
    System.out.println("Face ID:" + result.getFaceId());
    // Do what you need for the result
}
```

### Liveness detection
Face Liveness detection can be used to determine if a face in an input video stream is real (live) or fake (spoof).
The goal of liveness detection is to ensure that the system is interacting with a physically present live person at
the time of authentication. The whole process of authentication is called a session.

There're two different components in the authentication: a frontend application and an app server/orchestrator.
Before uploading the video stream, the app server has to create a session, and then the mobile client could upload
the payload with a `session authorization token` to call the liveness detection. The app server can query for the
liveness detection result and audit logs anytime until the session is deleted.

The Liveness detection operation can not only confirm if the input is live or spoof, but also verify whether the input
belongs to the expected person's face, which is called **liveness detection with face verification**. For the detail
information, please refer to the [tutorial][liveness_tutorial].

We'll only demonstrates how to create, query, delete a session and get the audit logs here. For how to perform a
liveness detection, please see the sample of [frontend applications][integrate_liveness_into_mobile_application].

Here is an example to create and get the liveness detection result of a session.
```java com.azure.ai.vision.face.readme.createLivenessSessionAndGetResult
System.out.println("Create a liveness session.");
FaceSessionClient sessionClient = new FaceSessionClientBuilder()
    .endpoint(endpoint)
    .credential(new KeyCredential(accountKey))
    .buildClient();

String deviceCorrelationId = UUID.randomUUID().toString();
CreateLivenessSessionContent parameters = new CreateLivenessSessionContent(LivenessOperationMode.PASSIVE)
    .setDeviceCorrelationId(deviceCorrelationId)
    .setSendResultsToClient(false);

CreateLivenessSessionResult createLivenessSessionResult = sessionClient.createLivenessSession(parameters);
String sessionId = createLivenessSessionResult.getSessionId();
System.out.println("Result: " + sessionId);

System.out.println("Get the liveness detection result.");
LivenessSession session = sessionClient.getLivenessSessionResult(sessionId);
System.out.println("Result: " + session.getResult().getResponse().getBody().getLivenessDecision());
```

Here is another example for the liveness detection with face verification.
```java com.azure.ai.vision.face.readme.createLivenessWithVerifySessionAndGetResult
System.out.println("Create a liveness session.");
FaceSessionClient sessionClient = new FaceSessionClientBuilder()
    .endpoint(endpoint)
    .credential(new KeyCredential(accountKey))
    .buildClient();

String deviceCorrelationId = UUID.randomUUID().toString();
CreateLivenessWithVerifySessionContent parameters = new CreateLivenessWithVerifySessionContent(LivenessOperationMode.PASSIVE)
    .setDeviceCorrelationId(deviceCorrelationId)
    .setSendResultsToClient(false);
Path path = Paths.get(imagePathString);
BinaryData verifyImage = BinaryData.fromFile(path);

CreateLivenessWithVerifySessionResult createLivenessSessionResult = sessionClient
    .createLivenessWithVerifySession(parameters, verifyImage);
String sessionId = createLivenessSessionResult.getSessionId();
System.out.println("Result: " + sessionId);

System.out.println("Get the liveness detection result.");
LivenessWithVerifySession session = sessionClient.getLivenessWithVerifySessionResult(sessionId);
LivenessResponseBody response = session.getResult().getResponse().getBody();
System.out.println("Result: " + response.getLivenessDecision() + ", Verify result:" + response.getVerifyResult());
```

## Troubleshooting
### Enable client logging
You can set the `AZURE_LOG_LEVEL` environment variable to view logging statements made in the client library. For
example, setting `AZURE_LOG_LEVEL=2` would show all informational, warning, and error log messages. The log levels can
be found here: [log levels][logLevels].

### Default HTTP Client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure
the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the
[HTTP clients wiki](https://learn.microsoft.com/azure/developer/java/sdk/http-client-pipeline#http-clients).

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL
operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides
better performance compared to the default SSL implementation within the JDK. For more information, including how to
reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps

### More sample code

See the [Sample README][face_samples] for several code snippets illustrating common patterns used in the Face API Java SDK.

### Additional documentation

For more extensive documentation on Azure AI Face, see the [Face documentation][face_product_docs] on learn.microsoft.com.

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request


<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/face/azure-ai-vision-face/src/
[face_java_package_maven]: https://central.sonatype.com/artifact/com.azure/azure-ai-vision-face/
[face_ref_java_docs]: https://aka.ms/azsdk-java-face-ref
[face_product_docs]: https://learn.microsoft.com/azure/ai-services/computer-vision/overview-identity
[face_samples]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/face/azure-ai-vision-face/src/samples

[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[azure_sub]: https://azure.microsoft.com/free/
[steps_assign_an_azure_role]: https://learn.microsoft.com/azure/role-based-access-control/role-assignments-steps
[azure_portal_list_face_account]: https://portal.azure.com/#blade/Microsoft_Azure_ProjectOxford/CognitiveServicesHub/Face
[azure_cognitive_service_account]: https://learn.microsoft.com/azure/ai-services/multi-service-resource?tabs=windows&pivots=azportal#supported-services-with-a-multi-service-resource
[azure_portal_list_cognitive_service_account]: https://portal.azure.com/#view/Microsoft_Azure_ProjectOxford/CognitiveServicesHub/~/AllInOne
[azure_portal_create_face_account]: https://ms.portal.azure.com/#create/Microsoft.CognitiveServicesFace
[quick_start_create_account_via_azure_cli]: https://learn.microsoft.com/azure/ai-services/multi-service-resource?tabs=windows&pivots=azcli
[quick_start_create_account_via_azure_powershell]: https://learn.microsoft.com/azure/ai-services/multi-service-resource?tabs=windows&pivots=azpowershell

[get_endpoint_via_azure_cli]: https://learn.microsoft.com/azure/ai-services/multi-service-resource?tabs=windows&pivots=azcli#get-the-keys-for-your-resource
[regional_endpoints]: https://azure.microsoft.com/global-infrastructure/services/?products=cognitive-services
[azure_sdk_java_azure_key_credential]: https://learn.microsoft.com/java/api/com.azure.core.credential.azurekeycredential?view=azure-java-stable

[face_client_async]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/face/azure-ai-vision-face/src/main/java/com/azure/ai/vision/face/FaceAsyncClient.java
[face_client]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/face/azure-ai-vision-face/src/main/java/com/azure/ai/vision/face/FaceClient.java
[face_client_builder]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/face/azure-ai-vision-face/src/main/java/com/azure/ai/vision/face/FaceClientBuilder.java
[face_session_client_async]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/face/azure-ai-vision-face/src/main/java/com/azure/ai/vision/face/FaceSessionAsyncClient.java
[face_session_client]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/face/azure-ai-vision-face/src/main/java/com/azure/ai/vision/face/FaceSessionClient.java
[face_session_client_builder]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/face/azure-ai-vision-face/src/main/java/com/azure/ai/vision/face/FaceSessionClientBuilder.java

[azure_sdk_java_identity]: https://learn.microsoft.com/azure/developer/java/sdk/identity
[custom_subdomain]: https://docs.microsoft.com/azure/cognitive-services/authentication#create-a-resource-with-a-custom-subdomain
[migrate_to_custom_subdomain]: https://learn.microsoft.com/azure/ai-services/cognitive-services-custom-subdomains#how-does-this-impact-existing-resources
[azure_sdk_java_default_azure_credential]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity#defaultazurecredential
[register_aad_app]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal

[evaluate_different_detection_models]: https://learn.microsoft.com/azure/ai-services/computer-vision/how-to/specify-detection-model#evaluate-different-models
[recommended_recognition_model]: https://learn.microsoft.com/azure/ai-services/computer-vision/how-to/specify-recognition-model#recommended-model
[liveness_tutorial]: https://learn.microsoft.com/azure/ai-services/computer-vision/tutorials/liveness
[integrate_liveness_into_mobile_application]: https://learn.microsoft.com/azure/ai-services/computer-vision/tutorials/liveness#integrate-liveness-into-mobile-application

[logLevels]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
