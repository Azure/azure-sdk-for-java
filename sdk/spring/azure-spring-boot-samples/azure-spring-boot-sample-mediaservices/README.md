# Sample for Azure Media Services Spring Boot client library for Java

## Key concepts
This sample project demonstrates how to use Azure Media Services with Spring Boot.

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

### Create Azure Media Services on Azure

1. Go to [Azure portal](https://portal.azure.com/) and create the service by following this [link](https://docs.microsoft.com/azure/media-services/media-services-portal-create-account). 
2. Mark down the `TENANT`, `CLIENT ID` and `CLIENT SECRET` from this [link](https://docs.microsoft.com/azure/media-services/latest/stream-files-dotnet-quickstart#access-the-media-services-api),
 `REST API ENDPOINT` from the service.

## Examples

### Config the sample

1. Navigate to `src/main/resources` and open `application.properties`.
2. Fill in the `tenant`, `client-id`, `client-secret` and `rest-api-endpoint` with `TENANT`, `CLIENT ID`, `CLIENT SECRET`
and `REST API ENDPOINT` respectively.
    ```properties
    azure.mediaservices.tenant=${your-tenant-id}
    azure.mediaservices.client-id=${your-client-id}
    azure.mediaservices.client-secret=${your-client-secret}
    azure.mediaservices.rest-api-endpoint=${your-rest-api-endpoint}
    ```

    > <strong>Optional</strong>  
    > If you are using network proxy then add below properties to run azure media services behind proxy.  
    ```
    azure.mediaservices.proxy-host=put-your-network-proxy-host
    azure.mediaservices.proxy-port=put-your-network-proxy-port
    azure.mediaservices.proxy-scheme=put-your-network-proxy-scheme
    ```

### Run with Maven

```
cd azure-spring-boot-samples/azure-spring-boot-sample-mediaservices
mvn spring-boot:run
```

## Troubleshooting
## Next steps
## Contributing

## License

The video file `video.mp4` under `src/main/resources` folder is from GitHub repository [big-buck-bunny-480p-5s](https://github.com/bower-media-samples/big-buck-bunny-480p-5s) without any modification and is under the [Creative Commons Attribution 3.0 license](https://creativecommons.org/licenses/by/3.0/).

<!-- LINKS -->
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
