# Azure Communication Administration library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Communication Administration library, simply Install AutoRest and in this folder, run:

### Setup
```ps
Fork and clone https://github.com/Azure/autorest.java
git checkout v4
git submodule update --init --recursive
mvn package -Dlocal
npm install
npm install -g autorest
```

### Generation

There are two swaggers for Administration management APIs, `identity` and `phonenumber`.

```ps
cd <swagger-folder>
autorest --use=@autorest/java@4.0.1 --tag=identity
autorest --use=@autorest/java@4.0.1 --tag=phonenumber
```



### Tag: identity

These settings apply only when `--tag=identity` is specified on the command line.

``` yaml $(tag) == 'identity'
input-file: swagger.json
add-context-parameter: true
```

### Tag: phonenumber

These settings apply only when `--tag=phonenumber` is specified on the command line.

``` yaml $(tag) == 'phonenumber'
input-file: ./PNM_Swagger.json
override-client-name: PhoneNumberAdminClient
```

### Code generation settings

``` yaml
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.administration
generate-client-as-impl: true
custom-types-subpackage: models
sync-methods: all
context-client-method-parameter: true
```