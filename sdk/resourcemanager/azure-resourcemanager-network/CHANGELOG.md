# Release History

## 2.2.0-beta.1 (Unreleased)

- Updated `api-version` to `2020-07-01`
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
