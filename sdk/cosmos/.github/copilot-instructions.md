# Cosmos DB AAD RBAC Setup Skill

When a user asks about Cosmos DB AAD authentication, RBAC setup, or errors like "Local Authorization is disabled" or "does not have required RBAC permissions", follow this guidance:

## Problem Recognition

Identify these common errors:
- "Local Authorization is disabled. Use an AAD token to authorize all requests"
- "principal [xxx] does not have required RBAC permissions to perform action [Microsoft.DocumentDB/databaseAccounts/...]"
- 403 Forbidden with substatus 5302

## Solution Overview

1. **Use `DefaultAzureCredential`** instead of master key in code
2. **Create a custom data plane role** with full permissions (built-in Data Contributor lacks `sqlDatabases/*`)
3. **Assign the role** to the user/service principal

## Code Change

Replace:
```java
.key(MASTER_KEY)
```

With:
```java
TokenCredential credential = new DefaultAzureCredentialBuilder().build();
// ...
.credential(credential)
```

Required import:
```java
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
```

Required dependency:
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
</dependency>
```

## RBAC Setup Script (Bash)

```bash
#!/bin/bash
# ============================================
# Cosmos DB AAD RBAC Setup Script
# ============================================

# Variables - user must fill these
SUBSCRIPTION_ID="<subscription-id>"
RESOURCE_GROUP="<resource-group>"
COSMOS_ACCOUNT="<cosmos-account-name>"
PRINCIPAL_ID="<principal-object-id>"  # az ad signed-in-user show --query id -o tsv

# Set subscription
az account set --subscription "$SUBSCRIPTION_ID"

# Create custom role with full data plane access
ROLE_DEFINITION_ID=$(az cosmosdb sql role definition create \
  --account-name "$COSMOS_ACCOUNT" \
  --resource-group "$RESOURCE_GROUP" \
  --body '{
    "RoleName": "CosmosDBDataPlaneFullAccess",
    "Type": "CustomRole",
    "AssignableScopes": ["/"],
    "Permissions": [{
      "DataActions": [
        "Microsoft.DocumentDB/databaseAccounts/readMetadata",
        "Microsoft.DocumentDB/databaseAccounts/sqlDatabases/*",
        "Microsoft.DocumentDB/databaseAccounts/sqlDatabases/containers/*",
        "Microsoft.DocumentDB/databaseAccounts/sqlDatabases/containers/items/*"
      ]
    }]
  }' --query name -o tsv)

echo "Created role definition: $ROLE_DEFINITION_ID"

# Assign role to principal
az cosmosdb sql role assignment create \
  --account-name "$COSMOS_ACCOUNT" \
  --resource-group "$RESOURCE_GROUP" \
  --principal-id "$PRINCIPAL_ID" \
  --role-definition-id "$ROLE_DEFINITION_ID" \
  --scope "/"

echo "Done! Wait 5-10 minutes for propagation."
```

## RBAC Setup Script (PowerShell)

```powershell
# ============================================
# Cosmos DB AAD RBAC Setup Script
# ============================================

# Variables - user must fill these
$SUBSCRIPTION_ID = "<subscription-id>"
$RESOURCE_GROUP = "<resource-group>"
$COSMOS_ACCOUNT = "<cosmos-account-name>"
$PRINCIPAL_ID = "<principal-object-id>"  # (az ad signed-in-user show --query id -o tsv)

# Set subscription
az account set --subscription $SUBSCRIPTION_ID

# Create custom role with full data plane access
$roleDefinition = @'
{
  "RoleName": "CosmosDBDataPlaneFullAccess",
  "Type": "CustomRole",
  "AssignableScopes": ["/"],
  "Permissions": [{
    "DataActions": [
      "Microsoft.DocumentDB/databaseAccounts/readMetadata",
      "Microsoft.DocumentDB/databaseAccounts/sqlDatabases/*",
      "Microsoft.DocumentDB/databaseAccounts/sqlDatabases/containers/*",
      "Microsoft.DocumentDB/databaseAccounts/sqlDatabases/containers/items/*"
    ]
  }]
}
'@

$roleResult = $roleDefinition | az cosmosdb sql role definition create `
  --account-name $COSMOS_ACCOUNT `
  --resource-group $RESOURCE_GROUP `
  --body "@-" | ConvertFrom-Json

$ROLE_DEFINITION_ID = $roleResult.name
Write-Host "Created role definition: $ROLE_DEFINITION_ID" -ForegroundColor Green

# Assign role to principal
az cosmosdb sql role assignment create `
  --account-name $COSMOS_ACCOUNT `
  --resource-group $RESOURCE_GROUP `
  --principal-id $PRINCIPAL_ID `
  --role-definition-id $ROLE_DEFINITION_ID `
  --scope "/"

Write-Host "Done! Wait 5-10 minutes for propagation." -ForegroundColor Green
```

## Key Concepts

### Built-in Roles Are Insufficient

| Role | ID | Includes `sqlDatabases/*`? |
|------|----|---------------------------|
| Cosmos DB Built-in Data Reader | 00000000-0000-0000-0000-000000000001 | ❌ No |
| Cosmos DB Built-in Data Contributor | 00000000-0000-0000-0000-000000000002 | ❌ No |
| Custom Role (create your own) | - | ✅ Yes |

The built-in roles only allow item-level operations, not database/container creation. A custom role is required for full SDK functionality.

### Two RBAC Systems

| Plane | Purpose | CLI Command |
|-------|---------|-------------|
| Management | Manage Azure resources, create RBAC assignments | `az role assignment create` |
| Data | Read/write data in Cosmos DB | `az cosmosdb sql role assignment create` |

### Required Permissions by Operation

| Operation | Data Action |
|-----------|-------------|
| Read account metadata | `readMetadata` |
| Create/delete database | `sqlDatabases/*` |
| Create/delete container | `sqlDatabases/containers/*` |
| CRUD items | `sqlDatabases/containers/items/*` |

### CI/CD with Federated Identity

- Ephemeral VMs authenticate as a service principal via OIDC
- Assign the same custom role to the CI/CD service principal
- Use `DefaultAzureCredential` which auto-detects the environment
- No secrets needed on the VM

### Propagation Delay

- Role assignments take 5-15 minutes to propagate
- Multi-region accounts may take longer
- Clear token cache if issues persist: `az account clear && az login`

## Troubleshooting Checklist

1. ☐ Correct subscription selected (`az account show`)
2. ☐ Custom role created with `sqlDatabases/*` permission
3. ☐ Role assigned to correct principal ID
4. ☐ Waited for propagation (5-15 min)
5. ☐ Fresh token obtained (restart app or clear cache)
6. ☐ Using `DefaultAzureCredential`, not master key

## Common Errors and Solutions

### Error: "Local Authorization is disabled"
**Cause**: Cosmos account requires AAD auth, but code is using master key.
**Solution**: Switch from `.key()` to `.credential(new DefaultAzureCredentialBuilder().build())`

### Error: "does not have required RBAC permissions to perform action [Microsoft.DocumentDB/databaseAccounts/sqlDatabases/write]"
**Cause**: Built-in Data Contributor role doesn't include database creation permission.
**Solution**: Create and assign custom role with `sqlDatabases/*` permission.

### Error: "does not have authorization to perform action 'Microsoft.DocumentDB/databaseAccounts/sqlRoleAssignments/write'"
**Cause**: User lacks management plane permission to create RBAC assignments.
**Solution**: Request `DocumentDB Account Contributor` role from subscription admin.

### Error: "AADSTS530084: Access has been blocked by conditional access token protection policy"
**Cause**: Organization's Conditional Access policy blocking non-interactive auth.
**Solution**: Run `az login --scope https://graph.microsoft.com//.default` or ask admin to exclude service principal from policy.

## Reference Links

- [Cosmos DB RBAC Documentation](https://learn.microsoft.com/en-us/azure/cosmos-db/how-to-connect-role-based-access-control)
- [Azure Identity Library](https://learn.microsoft.com/en-us/java/api/overview/azure/identity-readme)
- [DefaultAzureCredential](https://learn.microsoft.com/en-us/java/api/com.azure.identity.defaultazurecredential)
