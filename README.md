# Java RX SDK for Azure DocumentDB

[![Build Status](https://api.travis-ci.org/Azure/azure-documentdb-rxjava.svg?branch=master)](https://travis-ci.org/Azure/azure-documentdb-rxjava)
[![Coverage Status](https://img.shields.io/codecov/c/github/Azure/azure-documentdb-rxjava.svg)](https://codecov.io/gh/Azure/azure-documentdb-rxjava)

## Disclaimer
The implementation in this project is intended for reference purpose only and does not reflect the latest official Azure DocumentDB Java SDK released on Maven repository.  

## Consuming the official Microsoft Azure DocumentDB Java SDK

To get the binaries of the latest official Microsoft Azure DocumentDB Java SDK as distributed by Microsoft, ready for use within your project, you can use Maven.

    <dependency>
    	<groupId>com.microsoft.azure</groupId>
    	<artifactId>azure-documentdb</artifactId>
    	<version>LATEST</version>
    </dependency>

## Minimum Requirements
* Java Development Kit 7
* (Optional) Maven

### Dependencies
Dependencies will be added automatically if Maven is used. Otherwise, please download the dependencies from the pom.xml file and add them to your build path. 

## Samples
We have samples in form of small executable unit tests in [documentdb-examples](https://github.com/Azure/azure-documentdb-rxjava/tree/master/azure-documentdb-examples/src/test/java/com/microsoft/azure/documentdb/rx/examples) sub project.

* Clone the Repo
```bash
git clone https://github.com/Azure/azure-documentdb-rxjava.git
cd azure-documentdb-rxjava
```

You can run the samples either using Eclipse or from Command Line using Maven:

### Eclipse

* Load the main parent project pom file in Eclipse (That should automatically load documentdb-examples).
* For running the samples you need a proper Azure Cosmos DB Endpoint. The endpoints are picked up from [src/test/java/com/microsoft/azure/documentdb/rx/examples/TestConfigurations.java](https://github.com/Azure/azure-documentdb-rxjava/blob/master/azure-documentdb-examples/src/test/java/com/microsoft/azure/documentdb/rx/examples/TestConfigurations.java). 
* You can pass your endpoint credentials as VM Arguments in Eclipse JUnit Run Config:
```bash
 -DACCOUNT_HOST="https://REPLACE_ME.documents.azure.com:443/" -DACCOUNT_KEY="REPLACE_ME"
 ```
* or you can simply put your endpoint credentials in AccountCredentials.java
* Now you can run the samples as JUnit tests in Eclipse.

### Command line

The other way for running samples is to use maven:

* Run Maven and pass your Azure Cosmos DB Endpoint credentials:
```bash
mvn test -DACCOUNT_HOST="https://REPLACE_ME_WITH_YOURS.documents.azure.com:443/" -DACCOUNT_KEY="REPLACE_ME_WITH_YOURS"
```

## License
MIT License
Copyright (c) 2017 Copyright (c) Microsoft Corporation
