import "./private-links.tsp";

using Azure.ResourceManager.CommonTypes.Private;

namespace Azure.ResourceManager.CommonTypes;

@@Azure.ResourceManager.Private.conditionalClientFlatten(PrivateLinkResource.properties);
@@Azure.ResourceManager.Private.conditionalClientFlatten(PrivateEndpointConnection.properties);

/** The private endpoint */
@@armCommonDefinition(PrivateEndpoint,
  "PrivateEndpoint",
  #{ version: Azure.ResourceManager.CommonTypes.Versions.v3, isDefault: true },
  "privatelinks.json"
);
@@armCommonDefinition(PrivateEndpoint,
  "PrivateEndpoint",
  Azure.ResourceManager.CommonTypes.Versions.v4,
  "privatelinks.json"
);
@@armCommonDefinition(PrivateEndpoint,
  "PrivateEndpoint",
  Azure.ResourceManager.CommonTypes.Versions.v5,
  "privatelinks.json"
);

/** The private endpoint resource */
@@armCommonDefinition(PrivateLinkResource,
  "PrivateLinkResource",
  #{ version: Azure.ResourceManager.CommonTypes.Versions.v3, isDefault: true },
  "privatelinks.json"
);
@@armCommonDefinition(PrivateLinkResource,
  "PrivateLinkResource",
  Azure.ResourceManager.CommonTypes.Versions.v4,
  "privatelinks.json"
);
@@armCommonDefinition(PrivateLinkResource,
  "PrivateLinkResource",
  Azure.ResourceManager.CommonTypes.Versions.v5,
  "privatelinks.json"
);

/** PrivateEndpointConnection */
@@armCommonDefinition(PrivateEndpointConnection,
  "PrivateEndpointConnection",
  #{ version: Azure.ResourceManager.CommonTypes.Versions.v3, isDefault: true },
  "privatelinks.json"
);
@@armCommonDefinition(PrivateEndpointConnection,
  "PrivateEndpointConnection",
  Azure.ResourceManager.CommonTypes.Versions.v4,
  "privatelinks.json"
);
@@armCommonDefinition(PrivateEndpointConnection,
  "PrivateEndpointConnection",
  Azure.ResourceManager.CommonTypes.Versions.v5,
  "privatelinks.json"
);

/** Properties of he private endpoint connection resource */
@@armCommonDefinition(PrivateEndpointConnectionProperties,
  "PrivateEndpointConnectionProperties",
  #{ version: Azure.ResourceManager.CommonTypes.Versions.v3, isDefault: true },
  "privatelinks.json"
);
@@armCommonDefinition(PrivateEndpointConnectionProperties,
  "PrivateEndpointConnectionProperties",
  Azure.ResourceManager.CommonTypes.Versions.v4,
  "privatelinks.json"
);
@@armCommonDefinition(PrivateEndpointConnectionProperties,
  "PrivateEndpointConnectionProperties",
  Azure.ResourceManager.CommonTypes.Versions.v5,
  "privatelinks.json"
);

/** A collection of information about the state of the connection between service consumer and provider. */
@@armCommonDefinition(PrivateLinkServiceConnectionState,
  "PrivateLinkServiceConnectionState",
  #{ version: Azure.ResourceManager.CommonTypes.Versions.v3, isDefault: true },
  "privatelinks.json"
);
@@armCommonDefinition(PrivateLinkServiceConnectionState,
  "PrivateLinkServiceConnectionState",
  Azure.ResourceManager.CommonTypes.Versions.v4,
  "privatelinks.json"
);
@@armCommonDefinition(PrivateLinkServiceConnectionState,
  "PrivateLinkServiceConnectionState",
  Azure.ResourceManager.CommonTypes.Versions.v5,
  "privatelinks.json"
);

/** Properties of a private link resource. */
#suppress "@azure-tools/typespec-azure-resource-manager/arm-resource-provisioning-state" "Matches current common code"
@@armCommonDefinition(PrivateLinkResourceProperties,
  "PrivateLinkResourceProperties",
  #{ version: Azure.ResourceManager.CommonTypes.Versions.v3, isDefault: true },
  "privatelinks.json"
);
@@armCommonDefinition(PrivateLinkResourceProperties,
  "PrivateLinkResourceProperties",
  Azure.ResourceManager.CommonTypes.Versions.v4,
  "privatelinks.json"
);
@@armCommonDefinition(PrivateLinkResourceProperties,
  "PrivateLinkResourceProperties",
  Azure.ResourceManager.CommonTypes.Versions.v5,
  "privatelinks.json"
);

/** PrivateEndpointConnectionListResult */
@@armCommonDefinition(PrivateEndpointConnectionListResult,
  "PrivateEndpointConnectionListResult",
  #{ version: Azure.ResourceManager.CommonTypes.Versions.v3, isDefault: true },
  "privatelinks.json"
);
@@armCommonDefinition(PrivateEndpointConnectionListResult,
  "PrivateEndpointConnectionListResult",
  Azure.ResourceManager.CommonTypes.Versions.v4,
  "privatelinks.json"
);
@@armCommonDefinition(PrivateEndpointConnectionListResult,
  "PrivateEndpointConnectionListResult",
  Azure.ResourceManager.CommonTypes.Versions.v5,
  "privatelinks.json"
);

/** PrivateLinkResourceListResult */
@@armCommonDefinition(PrivateLinkResourceListResult,
  "PrivateLinkResourceListResult",
  #{ version: Azure.ResourceManager.CommonTypes.Versions.v3, isDefault: true },
  "privatelinks.json"
);
@@armCommonDefinition(PrivateLinkResourceListResult,
  "PrivateLinkResourceListResult",
  Azure.ResourceManager.CommonTypes.Versions.v4,
  "privatelinks.json"
);
@@armCommonDefinition(PrivateLinkResourceListResult,
  "PrivateLinkResourceListResult",
  Azure.ResourceManager.CommonTypes.Versions.v5,
  "privatelinks.json"
);

// PrivateEndpointServiceConnectionStatus
@@armCommonDefinition(PrivateEndpointServiceConnectionStatus,
  "PrivateEndpointServiceConnectionStatus",
  Azure.ResourceManager.CommonTypes.Versions.v3,
  "privatelinks.json"
);
@@armCommonDefinition(PrivateEndpointServiceConnectionStatus,
  "PrivateEndpointServiceConnectionStatus",
  Azure.ResourceManager.CommonTypes.Versions.v4
);
@@armCommonDefinition(PrivateEndpointServiceConnectionStatus,
  "PrivateEndpointServiceConnectionStatus",
  Azure.ResourceManager.CommonTypes.Versions.v5
);

// PrivateEndpointServiceConnectionStatus
@@armCommonDefinition(PrivateEndpointConnectionProvisioningState,
  "PrivateEndpointConnectionProvisioningState",
  Azure.ResourceManager.CommonTypes.Versions.v3,
  "privatelinks.json"
);
@@armCommonDefinition(PrivateEndpointConnectionProvisioningState,
  "PrivateEndpointConnectionProvisioningState",
  Azure.ResourceManager.CommonTypes.Versions.v4
);
@@armCommonDefinition(PrivateEndpointConnectionProvisioningState,
  "PrivateEndpointConnectionProvisioningState",
  Azure.ResourceManager.CommonTypes.Versions.v5
);

/** PrivateEndpointConnectionParameter */
@@CommonTypes.Private.armCommonParameter(PrivateEndpointConnectionParameter.name,
  "PrivateEndpointConnectionName",
  #{ version: Azure.ResourceManager.CommonTypes.Versions.v3, isDefault: true },
  "privatelinks.json"
);
@@CommonTypes.Private.armCommonParameter(PrivateEndpointConnectionParameter.name,
  "PrivateEndpointConnectionName",
  Azure.ResourceManager.CommonTypes.Versions.v4,
  "privatelinks.json"
);
@@CommonTypes.Private.armCommonParameter(PrivateEndpointConnectionParameter.name,
  "PrivateEndpointConnectionName",
  Azure.ResourceManager.CommonTypes.Versions.v5,
  "privatelinks.json"
);
