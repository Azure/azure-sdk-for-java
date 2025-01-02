# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.3 (2024-10-31)

- Azure Resource Manager DevHub client library for Java. This package contains Microsoft Azure SDK for DevHub Management SDK. The AKS Developer Hub Service Client. Package tag package-preview-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.GitHubOAuthCallRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Acr` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Operation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkflowRun` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkflowListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GitHubWorkflowProfileOidcCredentials` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TagsObject` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DeploymentProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.2 (2023-05-17)

- Azure Resource Manager DevHub client library for Java. This package contains Microsoft Azure SDK for DevHub Management SDK. The AKS Developer Hub Service Client. Package tag package-preview-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Workflows` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Workflow` was modified

* `models.ManifestType authStatus()` -> `models.AuthorizationStatus authStatus()`

#### `models.Workflow$Definition` was modified

* `withAuthStatus(models.ManifestType)` was removed

### Features Added

* `models.WorkflowRunStatus` was added

* `models.GenerationManifestType` was added

* `models.DockerfileGenerationMode` was added

* `models.GenerationLanguage` was added

* `models.ManifestGenerationMode` was added

* `models.AuthorizationStatus` was added

#### `models.Workflows` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Workflow` was modified

* `generationLanguage()` was added
* `dockerfileGenerationMode()` was added
* `manifestOutputDirectory()` was added
* `manifestGenerationMode()` was added
* `dockerfileOutputDirectory()` was added
* `systemData()` was added
* `port()` was added
* `imageName()` was added
* `namespaceArtifactGenerationPropertiesNamespace()` was added
* `languageVersion()` was added
* `appName()` was added
* `builderVersion()` was added
* `manifestType()` was added
* `imageTag()` was added

#### `models.WorkflowRun` was modified

* `withWorkflowRunStatus(models.WorkflowRunStatus)` was added
* `workflowRunStatus()` was added

#### `models.GitHubOAuthResponse` was modified

* `systemData()` was added

#### `models.Workflow$Definition` was modified

* `withDockerfileGenerationMode(models.DockerfileGenerationMode)` was added
* `withBuilderVersion(java.lang.String)` was added
* `withNamespaceArtifactGenerationPropertiesNamespace(java.lang.String)` was added
* `withAppName(java.lang.String)` was added
* `withPort(java.lang.String)` was added
* `withDockerfileOutputDirectory(java.lang.String)` was added
* `withLanguageVersion(java.lang.String)` was added
* `withImageTag(java.lang.String)` was added
* `withManifestOutputDirectory(java.lang.String)` was added
* `withGenerationLanguage(models.GenerationLanguage)` was added
* `withManifestType(models.GenerationManifestType)` was added
* `withImageName(java.lang.String)` was added
* `withManifestGenerationMode(models.ManifestGenerationMode)` was added

#### `models.ResourceProviders` was modified

* `generatePreviewArtifactsWithResponse(java.lang.String,fluent.models.ArtifactGenerationProperties,com.azure.core.util.Context)` was added
* `generatePreviewArtifacts(java.lang.String,fluent.models.ArtifactGenerationProperties)` was added

## 1.0.0-beta.1 (2022-09-26)

- Azure Resource Manager DevHub client library for Java. This package contains Microsoft Azure SDK for DevHub Management SDK. The AKS Developer Hub Service Client. Package tag package-preview-2022-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
