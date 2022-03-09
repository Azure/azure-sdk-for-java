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

``` yaml $(tag) == 'v1.0'
input-file:
- https://github.com/microsoftgraph/msgraph-sdk-powershell/raw/543ce61/openApiDocs/v1.0/Applications.yml
- Groups.yml
- https://github.com/microsoftgraph/msgraph-sdk-powershell/raw/543ce61/openApiDocs/v1.0/Users.yml
- https://github.com/microsoftgraph/msgraph-sdk-powershell/raw/543ce61/openApiDocs/v1.0/Identity.DirectoryManagement.yml
```
