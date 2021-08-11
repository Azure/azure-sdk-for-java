## Troubleshooting Mananged Identity Authenticaiton Issues

### Credential Unavailable

#### Connection Timed Out / Connection could not be established / Target Environment could not be determined.
The Managed Identity credential runs only on Azure Hosted machines/servers. So ensure that you are running your application on an
Azure Hosted resource. Currently Azure Identity SDK supports [Managed Identity Authentication]((https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview)) 
in the below listed Azure Services, so ensure you're running your application on one of these resources and have enabled the Managed Identity on
them by following the instructions at their configuration links below.

Azure Service | Managed Identity Configuration
--- | --- |
[Azure Virtual Machines](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/how-to-use-vm-token) | [Configuration Instructions](https://docs.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/qs-configure-portal-windows-vm)
[Azure App Service](https://docs.microsoft.com/azure/app-service/overview-managed-identity?tabs=java) | [Configuration Instructions](https://docs.microsoft.com/en-us/azure/app-service/overview-managed-identity?tabs=java)
[Azure Kubernetes Service](https://docs.microsoft.com/azure/aks/use-managed-identity) | [Configuration Instructions](https://docs.microsoft.com/azure/aks/use-managed-identity)
[Azure Cloud Shell](https://docs.microsoft.com/azure/cloud-shell/msi-authorization) |  |
[Azure Arc](https://docs.microsoft.com/azure/azure-arc/servers/managed-identity-authentication) | [Configuration Instructions](https://docs.microsoft.com/en-us/azure/azure-arc/servers/security-overview#using-a-managed-identity-with-arc-enabled-servers)
[Azure Service Fabric](https://docs.microsoft.com/azure/service-fabric/concepts-managed-identity) | [Configuration Instructions](https://docs.microsoft.com/en-us/azure/service-fabric/configure-existing-cluster-enable-managed-identity-token-service)

### Client Authentication Issue

#### 403: Access Forbidden
Ensure that your Managed Identity (User Assigned / System assigned has right permissions assigned to it for the Azure Resource via RBAC.
Below are the instructions to assign specific RBAC roles to an individual Identity in Azure Portal.

## Step 1: Identify the needed scope

![Diagram showing the scope levels for Azure RBAC.](https://github.com/MicrosoftDocs/azure-docs/blob/master/includes/role-based-access-control/media/scope-levels.png)

1. Sign in to the [Azure portal](https://portal.azure.com).

1. In the Search box at the top, search for the scope you want to grant access to. For example, search for **Management groups**, **Subscriptions**, **Resource groups**, or a specific resource.

1. Click the specific resource for that scope.

    The following shows an example resource group.

    ![Screenshot of resource group overview page.](https://github.com/MicrosoftDocs/azure-docs/blob/master/articles/role-based-access-control/media/shared/rg-overview.png)

## Step 2: Open the Add role assignment pane

**Access control (IAM)** is the page that you typically use to assign roles to grant access to Azure resources. It's also known as identity and access management (IAM) and appears in several locations in the Azure portal.

1. Click **Access control (IAM)**.

    The following shows an example of the Access control (IAM) page for a resource group.

    ![Screenshot of Access control (IAM) page for a resource group.](https://github.com/MicrosoftDocs/azure-docs/blob/master/articles/role-based-access-control/media/shared/rg-access-control.png)

1. Click the **Role assignments** tab to view the role assignments at this scope.

1. Click **Add** > **Add role assignment**.
   If you don't have permissions to assign roles, the Add role assignment option will be disabled.

   ![Screenshot of Add > Add role assignment menu.](https://github.com/MicrosoftDocs/azure-docs/blob/master/articles/role-based-access-control/media/shared/add-role-assignment-menu.png)

    The Add role assignment pane opens.

   ![Screenshot of Add role assignment page with Role, Assign access to, and Select options.](https://github.com/MicrosoftDocs/azure-docs/blob/master/includes/role-based-access-control/media/add-role-assignment-page.png)

## Step 3: Select the appropriate role

1. In the **Role** list, search or scroll to find the role that you want to assign.

    To help you determine the appropriate role, you can hover over the info icon to display a description for the role. For additional information, you can view the [Azure built-in roles](built-in-roles.md) article.

   ![Screenshot of Select a role list in Add role assignment.](https://github.com/MicrosoftDocs/azure-docs/blob/master/articles/role-based-access-control/media/role-assignments-portal/add-role-assignment-role.png)

1. Click to select the role.

## Step 4: Select who needs access

1. In the **Assign access to** list, select the type of security principal to assign access to.

    | Type | Description |
    | --- | --- |
    | **User, group, or service principal** | If you want to assign the role to a user, group, or service principal (application), select this type. |
    | **User assigned managed identity** | If you want to assign the role to a [user-assigned managed identity](../active-directory/managed-identities-azure-resources/overview.md), select this type. |
    | *System assigned managed identity* | If you want to assign the role to a [system-assigned managed identity](../active-directory/managed-identities-azure-resources/overview.md), select the Azure service instance where the managed identity is located. |

   ![Screenshot of selecting a security principal in Add role assignment.](https://github.com/MicrosoftDocs/azure-docs/blob/master/articles/role-based-access-control/media/role-assignments-portal/add-role-assignment-type.png)

1. If you selected a user-assigned managed identity or a system-assigned managed identity, select the **Subscription** where the managed identity is located.

1. In the **Select** section, search for the security principal by entering a string or scrolling through the list.

   ![Screenshot of selecting a user in Add role assignment.](https://github.com/MicrosoftDocs/azure-docs/blob/master/articles/role-based-access-control/media/role-assignments-portal/add-role-assignment-user.png)

1. Once you have found the security principal, click to select it.

## Step 5: Assign role

1. To assign the role, click **Save**.

   After a few moments, the security principal is assigned the role at the selected scope.

1. On the **Role assignments** tab, verify that you see the role assignment in the list.

    ![Screenshot of role assignment list after assigning role.](https://github.com/MicrosoftDocs/azure-docs/blob/master/articles/role-based-access-control/media/role-assignments-portal/rg-role-assignments.png)
