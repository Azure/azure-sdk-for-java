# Dev notes

## Checking out the code

The SDK is open source and is available here [sdk](sdk/).

Clone the Repo

```bash
git clone https://github.com/Azure/azure-cosmosdb-java.git
cd azure-cosmosdb-java
```

## How to Build from Command Line

- Run the following maven command to build:

```bash
maven clean package -DskipTests
```

## Running Tests from Command Line

Running tests require Azure Cosmos DB Endpoint credentials:

```bash
mvn test -DACCOUNT_HOST="https://REPLACE_ME_WITH_YOURS.documents.azure.com:443/" -DACCOUNT_KEY="REPLACE_ME_WITH_YOURS"
```

## Import into Intellij or Eclipse

- Load the main parent project pom file in Intellij/Eclipse (That should automatically load examples).
- For running the samples you need a proper Azure Cosmos DB Endpoint. The endpoints are picked up from [TestConfigurations.java](examples/src/test/java/com/microsoft/azure/cosmosdb/rx/examples/TestConfigurations.java). There is a similar endpoint config file for the sdk tests [here](sdk/src/test/java/com/microsoft/azure/cosmosdb/rx/TestConfigurations.java).
- You can pass your endpoint credentials as VM Arguments in Eclipse JUnit Run Config:

```bash
 -DACCOUNT_HOST="https://REPLACE_ME.documents.azure.com:443/" -DACCOUNT_KEY="REPLACE_ME"
```

- or you can simply put your endpoint credentials in TestConfigurations.java
- The SDK tests are written using TestNG framework, if you use Eclipse you may have to
  add TestNG plugin to your eclipse IDE as explained [here](http://testng.org/doc/eclipse.html).
  Intellij has builtin support for TestNG.
- Now you can run the tests in your Intellij/Eclipse IDE.