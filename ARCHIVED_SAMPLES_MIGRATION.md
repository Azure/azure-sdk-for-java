# Archived MS Learn Java sample migration

The Azure-Samples GitHub repositories linked from the following (now archived) MS Learn pages have been moved
into the `azure-resourcemanager` module under
`sdk/resourcemanager/azure-resourcemanager/src/samples/java/com/azure/resourcemanager/`.

Source pages:

- https://learn.microsoft.com/en-us/azure/developer/java/sdk/virtual-machine-samples
- https://learn.microsoft.com/en-us/azure/developer/java/sdk/web-apps-samples
- https://learn.microsoft.com/en-us/azure/developer/java/sdk/sql-database-samples
- https://learn.microsoft.com/en-us/azure/developer/java/sdk/containers-samples

The moved samples are intentionally **concise**: they focus on the resource-management operations, drop the verbose
`Utils.print(...)` resource dumps, and only print output where it is meaningful. A minimal shared helper
(`com.azure.resourcemanager.samples.SampleUtils`) provides random names, a sample password, and an SSH key.

## Sample name (MS Learn) → file path

Paths are relative to `sdk/resourcemanager/azure-resourcemanager/src/samples/java/com/azure/resourcemanager/`.

### Virtual machines

| MS Learn sample | Azure-Samples repo | File path |
| --- | --- | --- |
| Create a virtual machine from a custom image | https://github.com/Azure-Samples/managed-disk-java-create-virtual-machine-using-custom-image | `compute/samples/CreateVirtualMachineUsingCustomImageFromVM.java` |
| Create a virtual machine using specialized VHD from a snapshot | https://github.com/Azure-Samples/managed-disk-java-create-virtual-machine-using-specialized-disk-from-vhd | `compute/samples/CreateVirtualMachineUsingSpecializedDiskFromVhd.java` |
| Create virtual machines in parallel in the same network | https://github.com/Azure-Samples/compute-java-manage-virtual-machines-in-parallel | `compute/samples/ManageVirtualMachinesInParallel.java` |

### Web apps

| MS Learn sample | Azure-Samples repo | File path |
| --- | --- | --- |
| Create a web app and deploy from FTP or GitHub | _none (link is `./`)_ | **Skipped** (see below) |
| Create a web app and manage deployment slots | https://github.com/Azure-Samples/app-service-java-manage-staging-and-production-slots-for-web-apps | `appservice/samples/ManageWebAppSlots.java` |
| Create a web app and configure a custom domain | https://github.com/Azure-Samples/app-service-java-manage-web-apps-with-custom-domains | `appservice/samples/ManageWebAppWithCustomDomain.java` |
| Scale a web app with high availability across multiple regions | https://github.com/Azure-Samples/app-service-java-scale-web-apps-on-linux | `appservice/samples/ScaleWebAppWithTrafficManager.java` |
| Connect a web app to a storage account | https://github.com/Azure-Samples/app-service-java-manage-storage-connections-for-web-apps | `appservice/samples/ConnectWebAppToStorageAccount.java` |
| Connect a web app to a SQL database | https://github.com/Azure-Samples/app-service-java-manage-data-connections-for-web-apps | `appservice/samples/ConnectWebAppToSqlDatabase.java` |

### SQL Database

| MS Learn sample | Azure-Samples repo | File path |
| --- | --- | --- |
| Connect and query data from Azure SQL Database using JDBC | _none (JDBC how-to tutorial)_ | **Skipped** (see below) |
| Create and manage SQL databases | https://github.com/Azure-Samples/sql-database-java-manage-db | `sql/samples/ManageSqlDatabase.java` |
| Manage SQL databases across multiple regions | https://github.com/Azure-Samples/sql-database-java-manage-sql-databases-across-regions | `sql/samples/ManageSqlDatabasesAcrossRegions.java` |

### Containers

| MS Learn sample | Azure-Samples repo | File path |
| --- | --- | --- |
| Manage Azure Container Registries | https://github.com/Azure-Samples/acr-java-manage-azure-container-registry | `containerregistry/samples/ManageContainerRegistry.java` |
| Manage Azure Container Service | https://github.com/Azure-Samples/acs-java-manage-azure-container-service-with-kubernetes-orchestrator | `containerservice/samples/ManageKubernetesCluster.java` (ACS retired → AKS, see below) |
| Deploy an image from Azure Container Registry into a new Linux Web App | https://github.com/Azure-Samples/app-service-java-deploy-image-from-acr-to-linux | `appservice/samples/DeployImageFromAcrToLinuxWebApp.java` |

## Skipped samples

- **Web apps → "Create a web app and deploy from FTP or GitHub"**: the MS Learn table links to `./` (no
  Azure-Samples repository / empty link). Nothing to move.
- **SQL Database → "Connect and query data from Azure SQL Database using JDBC"**: links to a JDBC how-to tutorial,
  not an Azure management-library sample repository. Out of scope for `azure-resourcemanager`.

## Outdated features fixed during the move

- **Ubuntu 16.04 LTS (end-of-life)** → **Ubuntu 20.04 LTS** in all VM samples.
- **Azure Container Service (ACS) with the retired Track 1 `com.microsoft.azure.management` SDK** → **Azure
  Kubernetes Service (AKS)** using `KubernetesCluster` with a **system-assigned managed identity** (no service
  principal).
- **Java 8 / Tomcat 8.0** → **Java 11 / Tomcat 9.0** for Windows web apps; Linux built-in image updated to
  **Java 17**.
- **Custom-image-from-VM** sample migrated from **unmanaged disks** to **managed disks** (unmanaged disks are legacy).
- Removed the legacy `Microsoft.OSTCExtensions` `CustomScriptForLinux` v1.4 extension setup and the
  `org.apache.commons.lang3.time.StopWatch` dependency.

## Security refinements applied

A follow-up pass applied these passwordless/security improvements (all compile against the module):

- **SQL passwordless auth** (`ConnectWebAppToSqlDatabase`): replaced the SQL admin login/password (previously stored in
  web app settings) with a **Microsoft Entra-only** SQL Server whose administrator is the web app's **system-assigned
  managed identity**. The app now stores only a secret-free JDBC connection string using
  `authentication=ActiveDirectoryMSI`.
- **ACR AcrPull via RBAC** (`ManageContainerRegistry`): removed the ACR **admin user**; the registry is created without
  admin credentials and a **managed identity** is granted the built-in **`AcrPull`** role scoped to the registry
  (`accessManagement().roleAssignments()...withBuiltInRole(BuiltInRole.ACR_PULL)`).
- **SSH key** (`SampleUtils.sshPublicKey()`): kept **2048-bit RSA** (dependency-free, Java 8 compatible) and added a
  ready-to-uncomment **Ed25519** helper for users on **JDK 15+** (`EdECPublicKey` / `EdECPoint` are unavailable on
  Java 8, so enabling it by default would require a third-party crypto dependency such as BouncyCastle).

## Potential refinements (NOT changed — for a later pass)

These were left as-is, either to keep the move focused or because of a current API limitation:

- **Storage account key connection string in app settings** (`ConnectWebAppToStorageAccount`): prefer a managed
  identity with the appropriate data-plane role instead of account-key connection strings. **Left as-is** — the
  handwritten App Service fluent layer does not support configuring the storage connection with a managed identity.
- **ACR pull for the Linux web app** (`DeployImageFromAcrToLinuxWebApp`): still uses ACR **admin credentials** for the
  image pull. **Left as-is** — the fluent `withPrivateRegistryImage(...)` mandates `withCredentials(username, password)`
  and the handwritten App Service layer does not expose managed-identity ACR pull (`acrUseManagedIdentityCreds` exists
  only on the inner `SiteConfig`). Moving to managed-identity pull would require dropping to the inner layer.
- **SQL-across-regions sample scope**: the original sample also created 5 virtual networks and 5 VMs to derive
  firewall IPs. This was trimmed to the SQL geo-replication core plus a single example firewall rule for conciseness.
- **Ed25519 SSH keys**: enable the commented `sshPublicKeyEd25519()` helper once the samples baseline moves to JDK 15+.
- **Sample password generation** (`SampleUtils.password()`) is deterministic-ish and for samples only; do not use in
  production.

## RBAC references

- **`ManageContainerRegistry`** now contains an explicit RBAC role assignment: it grants the built-in **`AcrPull`** role
  to a managed identity, scoped to the container registry.
- **`ConnectWebAppToSqlDatabase`** sets the web app's system-assigned managed identity as the SQL Server's Microsoft
  Entra administrator (an identity/access configuration rather than an Azure RBAC role assignment).
- The **AKS** sample uses a **system-assigned managed identity**. Running the cluster still relies on Azure assigning
  the necessary RBAC roles to that identity behind the scenes; if the sample is extended to pull from ACR, an
  `AcrPull` role assignment on the kubelet identity would be required.
- The original (retired, not moved) **ACS** sample used a **service principal** with credentials, which is an implicit
  RBAC concern that AKS managed identity removes.
