# Release History

## 2.9.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
