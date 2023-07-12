# Release History

## 2.29.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 2.28.0 (2023-06-25)

### Features Added

- Supported `WebApplicationFirewallPolicy` for Web Application Firewall Policy.
- Supported associating `ApplicationGateway` with `WebApplicationFirewallPolicy`.
- Deprecated `withWebApplicationFirewall` in `ApplicationGateway` since no further investments will be made on legacy 
  WAF configuration.

## 2.27.0 (2023-05-25)

### Features Added

- Supported `withNatGateway` in `Subnet`.

### Breaking Changes

- Removed `DRAIN` from class `LoadBalancerBackendAddressAdminState`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-11-01`.

## 2.26.0 (2023-04-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.25.0 (2023-03-24)

### Bugs Fixed

- Fixed a bug that `listAvailablePrivateIPAddresses` in `Subnet` throws NPE when user has no permission.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-09-01`.

## 2.24.0 (2023-02-17)

### Bugs Fixed

- Fixed a bug that updating source/destination of `NetworkSecurityGroup` rules would fail when from application security group to ip addresses/service tags.

## 2.23.0 (2023-01-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.22.0 (2022-12-23)

### Other Changes

- Remove validation for properties `selector` and `selectorMatchOperator` from `ApplicationGatewayFirewallExclusion` to support `Equals any` operator.

#### Dependency Updates

- Updated `api-version` to `2022-07-01`.

## 2.21.0 (2022-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.20.0 (2022-10-26)

### Features Added

- Introduced new top-level properties on the Public Ip Resource which allows customers to enable DDoS Protection as for a new feature called DDoS Per-IP SKU.

### Breaking Changes

- Removed properties from Legacy Custom Policy Resource. The feature was never released to customer.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-05-01`.

## 2.19.0 (2022-09-23)

### Breaking Changes

- Since `2.10.0` (`api-version`:`2021-05-01`), `PublicIpAddress` of `Basic` Sku no longer supported configuring with Availability Zone.

## 2.18.0 (2022-08-26)

### Features Added

- Supported setting default `WebApplicationFirewall` when selected `ApplicationGatewayTier` is `ApplicationGatewayTier.WAF_v2`.

### Breaking Changes

- Rename class `ExplicitProxySettings` to `ExplicitProxy`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-01-01`.

## 2.17.0 (2022-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.16.0 (2022-06-24)

### Features Added

- Supported `priority` in `ApplicationGatewayRequestRoutingRule`.

### Breaking Changes

- Renamed class `OrderByOrder` to `FirewallPolicyIdpsQuerySortOrder`.
- Renamed class `SingleQueryResultDirection` to `FirewallPolicyIdpsSignatureDirection`.
- Renamed class `SingleQueryResultMode` to `FirewallPolicyIdpsSignatureMode`.
- Renamed class `SingleQueryResultSeverity` to `FirewallPolicyIdpsSignatureSeverity`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-08-01`.

## 2.15.0 (2022-05-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.14.0 (2022-04-11)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.13.0 (2022-03-11)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.12.0 (2022-02-14)

### Features Added

- Supported wildcard hostname in `ApplicationGateway` listener.
- Supported `withHostnames()` and `hostnames()` in `HasHostname`.

## 2.11.0 (2022-01-17)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.10.0 (2021-11-22)

### Features Added

- Supported `getByVirtualMachineScaleSetInstanceIdAsync()` in `NetworkInterfaces`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-05-01`.

## 2.9.0 (2021-10-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.8.0 (2021-09-15)

### Dependency Updates

- Updated `api-version` to `2021-03-01`.

### Features Added

- Supported `LoadBalancerOutboundRule` for `LoadBalancer`.

### Breaking Changes

- Removed unused class `NetworkOperationStatus`, `VirtualHubEffectiveRoute`.

## 2.7.0 (2021-08-12)

### Dependency Updates

- Updated core dependency from resources

## 2.6.0 (2021-06-18)

- Updated `api-version` to `2021-02-01`
- Supported multiple `ApplicationSecurityGroup` in rules of `NetworkSecurityGroup`.

## 2.5.0 (2021-05-28)
- Updated `api-version` to `2020-11-01`
- Supported `NetworkProfile`

## 2.4.0 (2021-04-28)

- Refreshed `api-version` `2020-08-01`
- Supported configure `ApplicationSecurityGroup` for `NetworkInterface`

## 2.3.0 (2021-03-30)

- Supported `PrivateEndpoint` and `PrivateDnsZone`

## 2.2.0 (2021-02-24)

- Updated `api-version` to `2020-08-01`
- Removed field `GCM_AES_128` and `GCM_AES_256` from class `ExpressRouteLinkMacSecCipher`
- Changed return type from `Integer` to `Long` for `ConnectionStateSnapshot::avgLatencyInMs()`, `ConnectionStateSnapshot::maxLatencyInMs()`, `ConnectionStateSnapshot::minLatencyInMs()`, `ConnectionStateSnapshot::probesFailed()`, `ConnectionStateSnapshot::probesSent()`
- Changed return type from `Integer` to `Long` for `HopLink::roundTripTimeAvg()`, `HopLink::roundTripTimeMax()`, `HopLink::roundTripTimeMin()`
- Changed return type from `Integer` to `Long` for `PacketCaptureParameters::bytesToCapturePerPacket()`, `PacketCaptureParameters::totalBytesPerSession()`
- Changed return type from `int` to `long` for `PacketCapture::bytesToCapturePerPacket()`, `PacketCapture::totalBytesPerSession()`
- Changed return type from `Resource` to `String` for `EffectiveRoutesParameters::resourceId()`

## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0 (2020-10-19)

- Updated core dependency from resources

## 2.0.0-beta.4 (2020-09-02)

- Supported beginCreate/beginDelete for PublicIpAddress and NetworkInterface
