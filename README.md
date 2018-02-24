# Java SDK for Document API of Azure Cosmos DB

[![Build Status](https://api.travis-ci.org/Azure/azure-cosmosdb-java.svg?branch=master)](https://travis-ci.org/Azure/azure-cosmosdb-java)
[![Coverage Status](https://img.shields.io/codecov/c/github/Azure/azure-cosmosdb-java.svg)](https://codecov.io/gh/Azure/azure-cosmosdb-java)

## Consuming the official Microsoft Azure Cosmos DB Java SDK

To get the binaries of the latest official Microsoft Azure Cosmos DB Java SDK as distributed by Microsoft, ready for use within your project, you can use Maven.

    <dependency>
    	<groupId>com.microsoft.azure</groupId>
    	<artifactId>azure-cosmosdb</artifactId>
    	<version>LATEST</version>
    </dependency>

## Minimum Requirements
* Java Development Kit 8
* (Optional) Maven

### Dependencies
Dependencies will be added automatically if Maven is used. Otherwise, please download the dependencies from the pom.xml file and add them to your build path. 

## Samples
We have samples in form of small executable unit tests in [examples](https://github.com/Azure/azure-cosmosdb-java/tree/master/examples/src/test/java/com/microsoft/azure/cosmosdb/rx/examples) sub project.

* Clone the Repo
```bash
git clone https://github.com/Azure/azure-cosmosdb-java.git
cd azure-cosmosdb-java
```

You can run the samples either using Eclipse or from Command Line using Maven:

### Eclipse

* Load the main parent project pom file in Eclipse (That should automatically load examples).
* For running the samples you need a proper Azure Cosmos DB Endpoint. The endpoints are picked up from [src/test/java/com/microsoft/azure/cosmosdb/rx/examples/TestConfigurations.java](https://github.com/Azure/azure-cosmosdb-java/blob/master/examples/src/test/java/com/microsoft/azure/cosmosdb/rx/examples/TestConfigurations.java). 
* You can pass your endpoint credentials as VM Arguments in Eclipse JUnit Run Config:
```bash
 -DACCOUNT_HOST="https://REPLACE_ME.documents.azure.com:443/" -DACCOUNT_KEY="REPLACE_ME"
 ```
* or you can simply put your endpoint credentials in TestConfigurations.java
* Now you can run the samples as JUnit tests in Eclipse.

### Command line

The other way for running samples is to use maven:

* Run Maven and pass your Azure Cosmos DB Endpoint credentials:
```bash
mvn test -DACCOUNT_HOST="https://REPLACE_ME_WITH_YOURS.documents.azure.com:443/" -DACCOUNT_KEY="REPLACE_ME_WITH_YOURS"
```

## License
MIT License
Copyright (c) 2018 Copyright (c) Microsoft Corporation
