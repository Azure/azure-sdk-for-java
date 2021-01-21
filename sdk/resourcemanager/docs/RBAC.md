# Introduction to Managed Identity and Role-Based Access Control (RBAC)

[Managed identity][managed_identity] and [role-based access control][rbac] are commonly used together for identity and access management.

Managed identity provides an identity for the Azure resource in Azure Active Directory, and uses it to obtain Azure AD token.
RBAC enforces the role, scope, and access control of that managed identity. 

They are useful in scenarios of either managing Azure resource, and accessing data in Azure resource.

## Managing Azure resource



[managed_identity]: https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview
[rbac]: https://docs.microsoft.com/azure/role-based-access-control/overview
