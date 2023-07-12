# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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

