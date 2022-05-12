# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2022-05-10)

- Azure Resource Manager ContainerAppsApi client library for Java. This package contains Microsoft Azure SDK for ContainerAppsApi Management SDK.  Package tag package-2022-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ManagedEnvironmentPatch` was removed

* `models.ContainerAppPatch` was removed

#### `models.ContainerAppsRevisions` was modified

* `listRevisions(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ContainerAppProbeHttpGet` was modified

* `java.lang.String scheme()` -> `models.Scheme scheme()`
* `withScheme(java.lang.String)` was removed

#### `models.AuthConfig` was modified

* `systemData()` was removed

#### `models.GithubActionConfiguration` was modified

* `dockerfilePath()` was removed
* `withDockerfilePath(java.lang.String)` was removed

### Features Added

* `models.Namespaces` was added

* `models.CheckNameAvailabilityResponse` was added

* `models.CheckNameAvailabilityRequest` was added

* `models.DaprSecretsCollection` was added

* `models.Scheme` was added

* `models.CheckNameAvailabilityReason` was added

#### `models.ManagedEnvironments` was modified

* `update(java.lang.String,java.lang.String,fluent.models.ManagedEnvironmentInner,com.azure.core.util.Context)` was added
* `update(java.lang.String,java.lang.String,fluent.models.ManagedEnvironmentInner)` was added

#### `models.RegistryCredentials` was modified

* `withIdentity(java.lang.String)` was added
* `identity()` was added

#### `models.ManagedEnvironment$Update` was modified

* `withVnetConfiguration(models.VnetConfiguration)` was added
* `withAppLogsConfiguration(models.AppLogsConfiguration)` was added

#### `models.DaprComponents` was modified

* `listSecrets(java.lang.String,java.lang.String,java.lang.String)` was added
* `listSecretsWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ContainerAppsRevisions` was modified

* `listRevisions(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.TrafficWeight` was modified

* `label()` was added
* `withLabel(java.lang.String)` was added

#### `models.ContainerAppProbeHttpGet` was modified

* `withScheme(models.Scheme)` was added

#### `models.CustomHostnameAnalysisResult` was modified

* `systemData()` was added

#### `models.ManagedEnvironment$Definition` was modified

* `withDaprAIConnectionString(java.lang.String)` was added
* `withZoneRedundant(java.lang.Boolean)` was added

#### `models.ContainerApp$Update` was modified

* `withConfiguration(models.Configuration)` was added
* `withTemplate(models.Template)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.GithubActionConfiguration` was modified

* `image()` was added
* `withImage(java.lang.String)` was added
* `contextPath()` was added
* `withContextPath(java.lang.String)` was added

#### `ContainerAppsApiManager` was modified

* `namespaces()` was added

#### `models.DaprComponent` was modified

* `listSecretsWithResponse(com.azure.core.util.Context)` was added
* `listSecrets()` was added

#### `models.ContainerApps` was modified

* `update(java.lang.String,java.lang.String,fluent.models.ContainerAppInner)` was added
* `update(java.lang.String,java.lang.String,fluent.models.ContainerAppInner,com.azure.core.util.Context)` was added

#### `models.ManagedEnvironment` was modified

* `daprAIConnectionString()` was added
* `zoneRedundant()` was added

## 1.0.0-beta.1 (2022-04-28)

- Azure Resource Manager ContainerAppsApi client library for Java. This package contains Microsoft Azure SDK for ContainerAppsApi Management SDK.  Package tag package-2022-01-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
