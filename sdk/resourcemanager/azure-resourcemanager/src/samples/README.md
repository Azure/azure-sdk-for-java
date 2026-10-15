---
page_type: sample
languages:
  - java
products:
  - azure
urlFragment: azure-resourcemanager-samples
---

# Azure Resource Manager samples for Java

This contains Java samples for managing Azure resources with the Azure Resource Manager client library. Each sample is
self-contained, focuses on a specific scenario, and exposes a `main()` entrypoint so it can be run directly.

## Getting started

Getting started is explained in detail [here][RESOURCEMANAGER_README_GETTING_STARTED].
Please refer to it to add the dependency and configure the authentication environment variables.

For general documentation on how to use the management libraries, please [visit here][MGMT_SDK_LINK].
For additional code snippets grouped by service, see [SAMPLE.md][RESOURCEMANAGER_SAMPLE_DOC].

## Sample details

### App Service

- [Connect a web app to a SQL database (passwordless)][sample_connect_web_app_to_sql_database]
- [Connect a web app to a storage account (passwordless)][sample_connect_web_app_to_storage_account]
- [Deploy an image from Azure Container Registry to a Linux web app][sample_deploy_image_from_acr_to_linux_web_app]
- [Manage web app deployment slots][sample_manage_web_app_slots]
- [Manage a web app with a custom domain][sample_manage_web_app_with_custom_domain]
- [Scale a web app across multiple regions with a Traffic Manager][sample_scale_web_app_with_traffic_manager]

### Authorization (Microsoft Entra ID / RBAC)

- [Manage users, groups and roles][sample_manage_users_groups_and_roles]
- [Manage service principals][sample_manage_service_principal]

### Compute

- [Create a virtual machine from a custom image][sample_create_vm_using_custom_image_from_vm]
- [Create a virtual machine from a specialized managed disk (snapshot)][sample_create_vm_using_specialized_disk_from_snapshot]
- [Create multiple virtual machines in parallel in the same network][sample_manage_vms_in_parallel]

### Container Registry

- [Manage container registries][sample_manage_container_registry]

### Container Service

- [Manage an Azure Kubernetes Service (AKS) cluster][sample_manage_kubernetes_cluster]

### SQL Database

- [Create and manage a SQL database][sample_manage_sql_database]
- [Manage SQL databases across multiple regions][sample_manage_sql_databases_across_regions]

## Next steps

Start using the Azure Resource Manager Java SDK in your solutions. Our SDK details can be found at [SDK README][RESOURCEMANAGER_SDK_README].

For more information about other Azure Management SDKs, refer [here][MGMT_SDK_LINK].

## Contributing

For details on contributing to this repository, see the [contributing guide][RESOURCEMANAGER_README_CONTRIBUTING].

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[MGMT_SDK_LINK]: https://aka.ms/azsdk/java/mgmt
[RESOURCEMANAGER_SDK_README]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/resourcemanager/azure-resourcemanager
[RESOURCEMANAGER_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/resourcemanager/azure-resourcemanager#getting-started
[RESOURCEMANAGER_README_CONTRIBUTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md
[RESOURCEMANAGER_SAMPLE_DOC]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/SAMPLE.md
[sample_connect_web_app_to_sql_database]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager/src/samples/java/com/azure/resourcemanager/appservice/samples/ConnectWebAppToSqlDatabase.java
[sample_connect_web_app_to_storage_account]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager/src/samples/java/com/azure/resourcemanager/appservice/samples/ConnectWebAppToStorageAccount.java
[sample_deploy_image_from_acr_to_linux_web_app]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager/src/samples/java/com/azure/resourcemanager/appservice/samples/DeployImageFromAcrToLinuxWebApp.java
[sample_manage_web_app_slots]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager/src/samples/java/com/azure/resourcemanager/appservice/samples/ManageWebAppSlots.java
[sample_manage_web_app_with_custom_domain]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager/src/samples/java/com/azure/resourcemanager/appservice/samples/ManageWebAppWithCustomDomain.java
[sample_scale_web_app_with_traffic_manager]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager/src/samples/java/com/azure/resourcemanager/appservice/samples/ScaleWebAppWithTrafficManager.java
[sample_manage_users_groups_and_roles]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager/src/samples/java/com/azure/resourcemanager/authorization/samples/ManageUsersGroupsAndRoles.java
[sample_manage_service_principal]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager/src/samples/java/com/azure/resourcemanager/authorization/samples/ManageServicePrincipal.java
[sample_create_vm_using_custom_image_from_vm]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager/src/samples/java/com/azure/resourcemanager/compute/samples/CreateVirtualMachineUsingCustomImageFromVM.java
[sample_create_vm_using_specialized_disk_from_snapshot]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager/src/samples/java/com/azure/resourcemanager/compute/samples/CreateVirtualMachineUsingSpecializedDiskFromSnapshot.java
[sample_manage_vms_in_parallel]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager/src/samples/java/com/azure/resourcemanager/compute/samples/ManageVirtualMachinesInParallel.java
[sample_manage_container_registry]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager/src/samples/java/com/azure/resourcemanager/containerregistry/samples/ManageContainerRegistry.java
[sample_manage_kubernetes_cluster]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager/src/samples/java/com/azure/resourcemanager/containerservice/samples/ManageKubernetesCluster.java
[sample_manage_sql_database]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager/src/samples/java/com/azure/resourcemanager/sql/samples/ManageSqlDatabase.java
[sample_manage_sql_databases_across_regions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager/src/samples/java/com/azure/resourcemanager/sql/samples/ManageSqlDatabasesAcrossRegions.java
