# Key Vault issue investigation context

This is advisory service context for agentic issue investigation, not a rule for automatically closing issues.
Read [the Java troubleshooting guide](TROUBLESHOOTING.md), the affected package's README, CHANGELOG, and troubleshooting guide alongside it.
Match the documented operation and conditions to the report; an HTTP status alone does not establish service ownership.
Do not recommend weakening access controls, purging data, or changing key state merely to make an error disappear.

## Known service behaviors

### Soft-delete recovery window conflicts

A deleted vault or object can retain its name during the recovery window. Recreating it can conflict with the deleted resource.
Check the resource type, deletion state, and retention policy. Recovery or waiting may be appropriate; purge is destructive and may be prohibited by purge protection.

- https://learn.microsoft.com/azure/key-vault/general/soft-delete-overview
- https://learn.microsoft.com/azure/key-vault/general/key-vault-recovery

### Throttling at vault operation limits

Service limits can produce HTTP 429. Compare the workload and operation with the documented limits before treating it as expected throttling.
Review the Java client's retry behavior and client reuse; a broken SDK retry path is still an SDK issue.

- https://learn.microsoft.com/azure/key-vault/general/service-limits
- https://learn.microsoft.com/azure/key-vault/general/overview-throttling

### Certificate import key-certificate mismatch

Certificate import requires supported certificate material and a matching private key. Check the supplied format and specific validation error without requesting private key material.

- https://learn.microsoft.com/azure/key-vault/certificates/tutorial-import-certificate
- https://learn.microsoft.com/azure/key-vault/certificates/about-certificates

### Access policies replaced during resource deployment

Setting the vault's full access-policy collection can replace existing entries rather than merge them.
Distinguish a full resource update from incremental access-policy operations before attributing removed policies to a Java management-client defect.

- https://learn.microsoft.com/azure/key-vault/general/assign-access-policy
- https://learn.microsoft.com/azure/key-vault/general/rbac-migration

### Firewall blocks access from an unexpected network

Firewall rules can reject requests from a network that is not permitted. Check the actual network path and applicable restrictions, including trusted-service limitations.
Use approved network access rather than suggesting that the customer disable the firewall.

- https://learn.microsoft.com/azure/key-vault/general/network-security
- https://learn.microsoft.com/azure/key-vault/general/overview-vnet-service-endpoints

### Role assignment propagation delay

Azure role assignments can take time to propagate. A recent assignment and an authorization failure may justify checking identity, scope, and propagation; HTTP 403 by itself does not prove this explanation.

- https://learn.microsoft.com/azure/role-based-access-control/troubleshooting

### Wrong token audience

Key Vault data-plane authentication requires the audience appropriate to the resource and cloud.
Distinguish credential configuration from an SDK challenge or scope-selection defect. Do not hardcode the public-cloud audience for sovereign-cloud reports.

- https://learn.microsoft.com/azure/key-vault/general/authentication

### Unexpected tenant or identity

Authorization can fail when the credential selected a different identity or tenant from the one granted access.
Consult Java credential-chain and multi-tenant guidance and ask only for sanitized diagnostics.

- https://learn.microsoft.com/azure/developer/java/sdk/authentication/overview
- [Azure Identity troubleshooting](../identity/azure-identity/TROUBLESHOOTING.md)

### Purge protection prevents immediate permanent deletion

Purge protection prevents permanent deletion during the retention period. Do not suggest bypassing it or promise immediate name reuse.

- https://learn.microsoft.com/azure/key-vault/general/soft-delete-overview

### Private endpoint DNS configuration

Private endpoint access depends on DNS resolving the vault hostname to the intended endpoint from the application's network.
Verify that path before attributing a connection or authorization failure to the Java client.

- https://learn.microsoft.com/azure/key-vault/general/private-link-service

### Managed identity is not enabled or authorized

The hosting resource must expose the intended managed identity, and that identity needs the appropriate data-plane permissions.
Differentiate credential acquisition failure from service authorization failure.

- https://learn.microsoft.com/azure/key-vault/general/authentication
- https://learn.microsoft.com/azure/app-service/overview-managed-identity

### Role-based access control and access-policy model mismatch

Permissions must be configured in the vault's active authorization model. An assignment in the inactive model does not establish access.

- https://learn.microsoft.com/azure/key-vault/general/rbac-guide
- https://learn.microsoft.com/azure/key-vault/general/rbac-migration

### Object state and validity periods

Disabled state, validity dates, object type, and operation can affect service behavior. Expiration metadata is not a universal prohibition on retrieval.
Check the documented semantics of the particular key, secret, or certificate operation instead of treating every expired object as unusable.

- https://learn.microsoft.com/azure/key-vault/secrets/about-secrets
- https://learn.microsoft.com/azure/key-vault/keys/about-keys
- https://learn.microsoft.com/azure/key-vault/certificates/about-certificates

### Managed HSM and vault endpoint differences

Managed HSM and standard vaults support different operations. Confirm that the Java client and operation support the resource type; Managed HSM does not provide secret or certificate APIs.

- https://learn.microsoft.com/azure/key-vault/managed-hsm/overview
- https://learn.microsoft.com/azure/key-vault/general/about-keys-secrets-certificates
