# Microsoft Graph

> see https://aka.ms/autorest

## Configuration

### Basic Information

These are the global settings for the Microsoft Graph API.

``` yaml
openapi-type: data-plane
tag: v1.0
```

### Tag: v1.0

These settings apply only when `--tag=v1.0` is specified on the command line.

Modification based on https://github.com/microsoftgraph/msgraph-sdk-powershell/tree/543ce61a713b9efacbf65c5d58e52c2f9659391a/openApiDocs/v1.0

``` yaml $(tag) == 'v1.0'
input-file:
- Applications.yml
- Groups.yml
- Users.yml
- Identity.DirectoryManagement.yml
```
