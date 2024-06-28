# Release History

## 1.0.14 (2024-06-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.3.2` to version `1.3.3`.
- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.
- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.1`.


## 1.0.13 (2024-05-28)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.0`.
- Upgraded `azure-communication-common` from `1.3.1` to version `1.3.2`.


## 1.0.12 (2024-04-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.3.1` to version `1.3.2`.
- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `azure-core-http-netty` from `1.14.1` to version `1.14.2`.


## 1.0.11 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.
- Upgraded `azure-core-http-netty` from `1.14.0` to version `1.14.1`.


## 1.0.10 (2024-02-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.11` to version `1.14.0`.
- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.
- Upgraded `azure-communication-common` from `1.2.14` to version `1.3.0`.


## 1.0.9 (2023-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.10` to version `1.13.11`.
- Upgraded `azure-communication-common` from `1.2.14` to version `1.2.15`.
- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.


## 1.0.8 (2023-11-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.
- Upgraded `azure-core-http-netty` from `1.13.9` to version `1.13.10`.
- Upgraded `azure-communication-common` from `1.2.13` to version `1.2.14`.


## 1.0.7 (2023-10-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.
- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.9`.
- Upgraded `azure-communication-common` from `1.2.12` to version `1.2.13`.


## 1.0.6 (2023-09-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.
- Upgraded `azure-core-http-netty` from `1.13.6` to version `1.13.7`.
- Upgraded `azure-communication-common` from `1.2.11` to version `1.2.12`.


## 1.0.5 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.
- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.
- Upgraded `azure-communication-common` from `1.2.10` to version `1.2.11`.


## 1.0.4 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.2.9` to version `1.2.10`.
- Upgraded `azure-core-http-netty` from `1.13.4` to version `1.13.5`.
- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.


## 1.0.3 (2023-06-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.2.8` to version `1.2.9`.
- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.
- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.


## 1.0.2 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.
- Upgraded `azure-communication-common` from `1.2.6` to version `1.2.8`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.


## 1.0.1 (2023-04-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.
- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.13.2`.

## 1.0.0 (2023-03-31)

### Features Added

The public release of the Azure Communication Services SDK for Email has the following features:

- send emails with a variety of options (multiple recipients, attachments, etc.)
- poll for the status of the email that was sent to track its progress

## 1.0.0-beta.2 (2023-03-01)

### Features Added

- AAD token auth has been added for `EmailClient` and `EmailAsyncClient`

### Breaking Changes

- Reworked the SDK to follow the LRO (long running operation) approach. The 'beginSend' method returns a poller that can be used to check for the status of sending the email and retrieve the result. The return object has been adjusted to fit this approach.
- The `EmailMessage` model has been reworked. It now accepts properties through setters. The EmailRecipients and EmailContent objects have been removed.
- The `getSendStatus` method has been removed.
- The `EmailAttachment` constructor now accepts BinaryData instead of a string
- The `contentBytesBase64` property under `attachments` has been changed to `contentInBase64`
- The `attachmentType` property under `attachments` has been changed to 'contentType'. This now accepts the attachment mime type.
- The `sender` property has been changed to `senderAddress`.
- The `email` property under the recipient object has been changed to `address`.
- Custom headers in the email message are now key/value pairs.
- The importance property was removed. Email importance can now be specified through either the `x-priority` or `x-msmail-priority` custom headers.

## 1.0.0-beta.1 (2022-08-09)

The initial release of the Azure Communication Services SDK for Email has the following features:

- send emails to multiple recipients with attachments
- get the status of a sent message

