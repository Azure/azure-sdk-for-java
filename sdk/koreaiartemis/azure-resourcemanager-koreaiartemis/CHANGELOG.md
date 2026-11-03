# Release History

## 1.0.0-beta.1 (2026-10-19)

- Azure Resource Manager Kore AI Artemis client library for Java. This package contains Microsoft Azure SDK for Kore AI Artemis Management SDK. Kore.AgentPlatform Resource Provider for managing Kore AI Agent Platform
Organizations and their Workspaces.

Organizations are top-level tracked ARM resources. Each Organization is
provisioned together with a default Workspace. Additional Workspaces are
managed as a child proxy resource type under the Organization using the
standard ARM verbs (PUT / DELETE / GET / LIST) on the workspace URL.

Service-to-service authentication between Liftr and Kore uses federated
Microsoft Entra credentials and is enforced at the runtime layer
(not on the ARM surface). Package api-version 2026-07-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-koreaiartemis Java SDK.
