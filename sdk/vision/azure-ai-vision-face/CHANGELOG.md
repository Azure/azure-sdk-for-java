# Release History

## 1.0.0-beta.1 (Unreleased)

Version 1.0.0-beta.1 is a preview of our efforts in creating a client library for Azure AI Vision Face Service that is developer-friendly
and idiomatic to the Java ecosystem. The principles that guide
our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html). For more information about this, and preview releases of other Azure SDK libraries, please visit
https://azure.github.io/azure-sdk/releases/latest/java.html.

- It uses the Azure AI Vision Face service `v1.1-preview.1` API.
- Three client design:
    - `FaceClient` provides functionality for face detection and recognition, including 'detect', 'verify', 'findsimilar', 'group' and 'identify'
    - `FaceAdministrationClient` to build and manage data collection of face features for face recognition, including 'Person Directory', 'LargePersonGroup' 'LargeFaceList', 'PersonGroup', 'Face List'
    - `FaceSessionClient` to orchestrate with mobile client application to perform liveness check and verification
- Authentication with API key supported using `AzureKeyCredential("<api_key>")` from `com.azure.core.credential`
- Authentication with AAD using `DefaultAzureCredentialBuilder()` from `com.azure.identity`

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes
