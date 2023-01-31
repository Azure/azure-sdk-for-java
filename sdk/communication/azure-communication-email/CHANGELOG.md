# Release History

## 1.0.0-beta.2 (Unreleased)

### Features Added

- AAD token auth has been added for `EmailClient` and `EmailAsyncClient`
- Overloads have been added for creating an email address object
- `withResponse` methods have been added for `send` and `getSendStatus` operations

### Breaking Changes

- `recipients` is a required property in the `EmailMessage` object and must now be passed in through the constructor

### Bugs Fixed

### Other Changes

## 1.0.0-beta.1 (2022-08-09)

The initial release of the Azure Communication Services SDK for Email has the following features:

- send emails to multiple recipients with attachments
- get the status of a sent message

