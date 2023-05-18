# Azure Template for Java

> see https://aka.ms/autorest

This is the template AutoRest configuration file for <SDK clients here>.
---
## Getting Started

To build the SDK for <SDK clients here>, simply [Install AutoRest](https://aka.ms/autorest) and in this folder, run:

> `autorest`

To see additional help and options, run:

> `autorest --help`

### Setup
```ps
npm install -g autorest
```

### Generation

Generating client SDKs from Swagger involves using the `autorest` command installed to the command line above while
also referencing the Java AutoRest packages, either the local installation performed above or using a released version.

#### Default

Autorest README configuration files should include a `use` configuration which indicates to Autorest which version of
Java Autorest should be used to generate, meaning it's not necessary to pass `--use` when running the `autorest`
command.

```ps
cd <swagger-folder>
autorest
```

#### Local Installation

Using a local installation of Java AutoRest allows for the most up-to-date code to be used and allows for debugging of
code generation, see the [autorest.java usage](https://github.com/Azure/autorest.java#usage) for more details.

```ps
cd <swagger-folder>
autorest --use=<directory where autorest.java was cloned>
```

#### Released Version

Using a released build of Java AutoRest ensures that a well-tested and durable implementation is used, as rebuilding
the local installation of Java AutoRest won't affect code generation as it would above.

```ps
cd <swagger-folder>
autorest --java --use:@autorest/java@4.0.x
```

## Configuration

Java AutoRest can accept configurations from both the command-line and from a configuration file. It's preferred to use
a configuration file for client SDKs as Java AutoRest [has many configuration settings](https://github.com/Azure/autorest.java#settings)
and tracking them in command-line is tedious and error-prone. The following outlines a few key configurations that are
either required or considered best practices by Azure SDKs for Java:

_Anything inside `<>` should be removed as it's an explanation._

``` yaml
use: '@autorest/java@4.1.16' <latest version of Autorest Java at the time of creation or update>
input-file: <required, can either be a local file or URL with URL preferred>
java: true <required, indicates this is Java code generation>
output-folder: ../ <required, where code will be generated>
namespace: <required, the base package where files will be written, *-subpackage configurations will be extended from this>
generate-client-as-impl: true <optional, generates the Swagger interfaces as implementation>
generate-client-interfaces: false <optional, generates the Swagger implementations as interfaces>
service-interface-as-public: true <optional, will generated interfaces used by RestProxy as public to prevent SecurityManager issues>
sync-methods: none <optional, will only generate asynchronous methods in the interface layer>
license-header: MICROSOFT_MIT_SMALL <optional, configuration of the code generation license header>
context-client-method-parameter: true <optional, generates methods with Context as the final parameter for passing additional metadata per-call>
default-http-exception-type: <optional, points to hand-written implementation of HttpResponseException that should be used instead of the generated error type>
models-subpackage: <optional, package where code generated models will be placed>
custom-types: <optional, list of model names that will be generated into the custom-types-subpackage, generally used for generated models that should be public API>
custom-types-subpackage: <optional, package where custom-types will be placed>
generic-response-type: true <optional, generated Swagger response types using ResponseBase<Headers, Body> instead of a sub-type, helps reduce usage of reflection>
custom-strongly-typed-header-deserialization: true <optional, generated strongly-typed HTTP header classes will use simplified deserialization that is better performing>
enable-sync-stack: true <optional, fully synchronous call paths will be generated removing or limiting usage of Reactor>
disable-client-builder: true <optional, Autorest won't generate a client builder, useful for handwritten SDKs to reduce code area>
customization-class: <path to Java file extending Customization from azure-autorest-customization>
```

Using `azure-storage-blob` as an example with the configurations above:

_`input-file` and `custom-types` are omitted as they change frequently._

``` yaml
input-file: <URL of Swagger file>
java: true
output-folder: ../
namespace: com.azure.storage.blob
generate-client-as-impl: true
generate-client-interfaces: false
service-interface-as-public: true
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
context-client-method-parameter: true
default-http-exception-type: com.azure.storage.blob.models.BlobStorageException
models-subpackage: implementation.models
custom-types: <list of models that are generated into public API> 
custom-types-subpackage: models
generic-response-type: true
custom-strongly-typed-header-deserialization: true
enable-sync-stack: true
disable-client-builder: true
customization-class: src/main/java/TemplateCustomization.java
```

Some services may leverage multiple Swagger files, which means that they may require different packages based on the
file being used in generation to prevent overwriting same-name classes. For this, sections with name `Tag:` and `yaml`
configurations with `$(tag) == '<tag value>'` can be used to have the Java AutoRest generation have branching generation 
logic, each tag will need to be generated with a separate run of `autorest`. Fortunately, a top-level configuration, 
such as above, can be used for common features between each Swagger file, such as `output-folder`, `license-header`, etc., 
the only configurations that must be different are the `input-file`, `namespace`, `models-subpackage`, `custom-types`, 
and `custom-types-subpackage`. The following is an example:

_Shared configurations_

``` yaml
java: true
output-folder: ../
generate-client-as-impl: true
generate-client-interfaces: false
service-interface-as-public: true
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
context-client-method-parameter: true
generic-response-type: true
custom-strongly-typed-header-deserialization: true
```

### Tag: storage-blob-package

``` yaml $(tag) == 'storage-blob-package'
input-file: <URL of Swagger file>
namespace: com.azure.storage.blob
models-subpackage: implementation.models
custom-types: <list of models that are generated into public API> 
custom-types-subpackage: models
```

### Tag: storage-another-package

``` yaml $(tag) == 'storage-another-package'
input-file: <URL of Swagger file>
namespace: com.azure.storage.another
models-subpackage: implementation.models
custom-types: <list of models that are generated into public API> 
custom-types-subpackage: models
```

When the configuration file has multiple tags, `--tag=<tag name>` needs to be passed to specify which Swagger to
generate.
