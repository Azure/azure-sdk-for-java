## Overview
This sample project demonstrates how to use Azure Media Services via Spring Boot Starter `azure-mediaservices-spring-boot-starter`. 

## Prerequisites

* An Azure subscription; if you don't already have an Azure subscription, you can activate your [MSDN subscriber benefits](https://azure.microsoft.com/en-us/pricing/member-offers/msdn-benefits-details/) or sign up for a [free Azure account](https://azure.microsoft.com/en-us/free/).

* A [Java Development Kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/), version 1.8.

* [Apache Maven](http://maven.apache.org/), version 3.0 or later.

## Quick Start

### Create Azure Media Services on Azure

1. Go to [Azure portal](https://portal.azure.com/) and create the service by following this [link](https://docs.microsoft.com/en-us/azure/media-services/media-services-portal-create-account). 
2. Mark down the `TENANT`, `CLIENT ID` and `CLIENT SECRET` from this [link](https://docs.microsoft.com/en-us/azure/media-services/latest/stream-files-dotnet-quickstart#access-the-media-services-api),
 `REST API ENDPOINT` from the service.
                                                                                                                                                                                                                                                                                               
### Config the sample

1. Navigate to `src/main/resources` and open `application.properties`.
2. Fill in the `tenant`, `client-id`, `client-secret` and `rest-api-endpoint` with `TENANT`, `CLIENT ID`, `CLIENT SECRET`
and `REST API ENDPOINT` respectively.

### Run the sample

1. Change directory to folder `azure-mediaservices-spring-boot-sample`.
2. Run below commands. 

   - Use Maven 

     ```
     mvn package
     java -jar target/azure-mediaservices-spring-boot-sample-0.0.1-SNAPSHOT.jar
     ```

   - Use Gradle 
   
     ```
     gradle bootRepackage
     java -jar build/libs/azure-mediaservices-spring-boot-sample-0.0.1-SNAPSHOT.jar
     ```

## License

The video file `video.mp4` under `src/main/resources` folder is from GitHub repository [big-buck-bunny-480p-5s](https://github.com/bower-media-samples/big-buck-bunny-480p-5s) without any modification and is under the [Creative Commons Attribution 3.0 license](http://creativecommons.org/licenses/by/3.0/).
