# Release History

## 1.0.0-beta.4 (2026-05-08)

- Azure Resource Manager DevHub client library for Java. This package contains Microsoft Azure SDK for DevHub Management SDK. The AKS Developer Hub Service Client. Package api-version 2025-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.GitHubOAuthListResponse` was removed

#### `models.WorkflowListResult` was removed

#### `models.GitHubOAuthCallRequest` was modified

* `validate()` was removed

#### `models.Operation` was modified

* `Operation()` was changed to private access
* `withDisplay(models.OperationDisplay)` was removed
* `validate()` was removed

#### `models.ResourceProviders` was modified

* `models.GitHubOAuthListResponse listGitHubOAuth(java.lang.String)` -> `com.azure.core.http.rest.PagedIterable listGitHubOAuth(java.lang.String)`
* `listGitHubOAuthWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Acr` was modified

* `validate()` was removed

#### `models.WorkflowRun` was modified

* `validate()` was removed
* `withWorkflowRunStatus(models.WorkflowRunStatus)` was removed

#### `models.GitHubWorkflowProfileOidcCredentials` was modified

* `validate()` was removed

#### `models.TagsObject` was modified

* `validate()` was removed

#### `models.DeploymentProperties` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed

### Features Added

* `models.ParameterType` was added

* `models.GenerateVersionedTemplateResponse` was added

* `models.ParameterDefault` was added

* `models.IacProfile$Definition` was added

* `models.AdoOAuthCallRequest` was added

* `models.IacTemplateProperties` was added

* `models.QuickStartTemplateType` was added

* `models.AdoOAuthResponses` was added

* `models.TemplateWorkflowProfile` was added

* `models.Parameter` was added

* `models.ScaleProperty` was added

* `models.IacProfile$DefinitionStages` was added

* `models.VersionedTemplateProperties` was added

* `models.GitHubRepository` was added

* `models.TemplateReference` was added

* `models.Build` was added

* `models.ExportTemplateRequest` was added

* `models.IacTemplateDetails` was added

* `models.IacProfile$UpdateStages` was added

* `models.AzurePipelineProfile` was added

* `models.AdoOAuthInfoResponse` was added

* `models.IacProfile` was added

* `models.StageProperties` was added

* `models.ADORepository` was added

* `models.AdoOAuth` was added

* `models.TemplateType` was added

* `models.PrLinkResponse` was added

* `models.IacProfile$Update` was added

* `models.Template` was added

* `models.AdoOAuthResponse` was added

* `models.OidcCredentials` was added

* `models.ScaleTemplateRequest` was added

* `models.ParameterKind` was added

* `models.ADOProviderProfile` was added

* `models.VersionedTemplate` was added

* `models.TemplateProperties` was added

* `models.GitHubProviderProfile` was added

* `models.Templates` was added

* `models.RepositoryProviderType` was added

* `models.PullRequest` was added

* `models.VersionedTemplates` was added

* `models.IacProfiles` was added

#### `models.Workflow` was modified

* `azurePipelineProfile()` was added
* `templateWorkflowProfile()` was added

#### `models.ManifestType` was modified

* `KUSTOMIZE` was added

#### `models.ResourceProviders` was modified

* `getAdoOAuthInfoWithResponse(java.lang.String,models.AdoOAuthCallRequest,com.azure.core.util.Context)` was added
* `listGitHubOAuth(java.lang.String,com.azure.core.util.Context)` was added
* `getAdoOAuthInfo(java.lang.String)` was added

#### `models.Workflow$Definition` was modified

* `withTemplateWorkflowProfile(models.TemplateWorkflowProfile)` was added
* `withAzurePipelineProfile(models.AzurePipelineProfile)` was added

#### `DevHubManager` was modified

* `iacProfiles()` was added
* `versionedTemplates()` was added
* `adoOAuthResponses()` was added
* `templates()` was added

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
