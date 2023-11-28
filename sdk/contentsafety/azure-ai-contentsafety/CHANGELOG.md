# Release History

## 1.0.0 (Unreleased)

### Features Added
- Support AAD Authentication
- Support 8 severity level for text:analyze API
### Breaking Changes
- Contract change for AnalyzeText, AnalyzeImage, Blocklist management related methods
### Bugs Fixed

### Other Changes

## 1.0.0-beta.1 (2023-09-28)

- Azure AI ContentSafety client library for Java. This package contains Microsoft Azure ContentSafety client library.

### Features Added
* Text Analysis API: Scans text for sexual content, violence, hate, and self harm with multi-severity levels.
* Image Analysis API: Scans images for sexual content, violence, hate, and self harm with multi-severity levels.
* Text Blocklist Management APIs: The default AI classifiers are sufficient for most content safety needs; however, you might need to screen for terms that are specific to your use case. You can create blocklists of terms to use with the Text API.
