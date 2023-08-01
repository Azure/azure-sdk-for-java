## Release History

### 1.0.0-beta.2 (Unreleased)

#### Features Added

#### Breaking Changes

#### Bugs Fixed

#### Other Changes

### 1.0.0-beta.1 (2022-09-08)

- Initial beta release for Personalizer client library.

Version 1.0.0-beta.1 is a preview of our efforts in creating a client library for Azure Personalizer service that is developer-friendly
and idiomatic to the Java ecosystem. The principles that guide
our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

For more information about this, and preview releases of other Azure SDK libraries, please visit
https://azure.github.io/azure-sdk/releases/latest/java.html.

- It uses the Personalizer service `v1.1-preview.3` API.
- There are two clients:
    - `PersonalizerClient/PersonalizerAsyncClient` to rank, activate and reward the events. The data sent using this is used to score and train the model. 
    - `PersonalizerAdministrationClient/PersonalizerAdministrationAsyncClient` manage configuration and run counterfactual evaluations.
