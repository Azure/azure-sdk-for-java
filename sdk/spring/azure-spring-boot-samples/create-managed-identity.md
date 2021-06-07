## Overview

[MSI][managed-identities] (Managed Service Identity, aka Managed Identity) for Azure resources
provides Azure services with an automatically managed identity in [Azure AD][azure-ad].
You can use this identity to authenticate to any service that supports Azure AD authentication
without having any credentials in your code.

### Set up managed identity

Please note your application should run in VM (Virtual Machine) or App Services on Azure for
support of MSI. Choose any of them.

**Note**: When using Managed Identity, it's not supported to create a resource group automatically, and the resource group should be assigned at least `Contributor` role. 

#### Method 1: Set up VM and assign identity

1.  Create VM in Azure portal. Please refer to
    [Create a Windows virtual machine in the Azure portal][create-vm-windows]
    or [Create a Linux virtual machine in the Azure portal][create-vm-linux].
    Choose either one according to your needs.

1.  Create a user-assigned identity in Azure Portal. Please refer to
    [Create a user-assigned managed identity][create-user-assigned-mi].

1.  Assign the user-assigned identity to the VM. Please refer to
    [Assign an user-assigned managed identity to an existing VM][assign-user-assigned-mi-to-vm].

#### Method 2: Set up App Service and assign identity

1. Create a managed identity for App Service.
    - If you choose system-assigned identity, follow [Adding a system assigned identity][app-service-add-system-assigned-mi].
    - If you choose user-assigned identity, follow [Adding a user assigned identity][app-service-add-user-assigned-mi].

### Add Role Assignment for Resource Group

1. Resource Group: assign `Reader` role for managed identity. See
   [Add or remove Azure role assignments][role-assignment]
   to add the role assignment for Resource Group.

### Deploy application
1. Run `mvn azure-webapp:config` to configure the app service, check the maven plugin in *pom.xml*.

1. Run `mvn clean package azure-webapp:deploy` to deploy to Azure App Service. Please see [Create a app on Azure App Service][create-java-app-on-app-service].


For different built-in role’s descriptions, please see [Built-in role descriptions][built-in-roles].

<!-- Links -->
[app-service-add-system-assigned-mi]: https://docs.microsoft.com/azure/app-service/overview-managed-identity#adding-a-system-assigned-identity
[app-service-add-user-assigned-mi]: https://docs.microsoft.com/azure/app-service/overview-managed-identity#adding-a-user-assigned-identity
[assign-user-assigned-mi-to-vm]: https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/qs-configure-portal-windows-vm#assign-a-user-assigned-managed-identity-to-an-existing-vm
[azure-ad]: https://docs.microsoft.com/azure/active-directory/fundamentals/active-directory-whatis
[built-in-roles]: https://docs.microsoft.com/azure/role-based-access-control/built-in-roles
[create-user-assigned-mi]: https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/how-to-manage-ua-identity-portal#create-a-user-assigned-managed-identity
[create-vm-windows]: https://docs.microsoft.com/azure/virtual-machines/windows/quick-create-portal
[create-vm-linux]: https://docs.microsoft.com/azure/virtual-machines/linux/quick-create-portal
[managed-identities]: https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/
[role-assignment]: https://docs.microsoft.com/azure/role-based-access-control/role-assignments-portal
[create-java-app-on-app-service]: https://docs.microsoft.com/azure/app-service/quickstart-java?tabs=javase&pivots=platform-linux
