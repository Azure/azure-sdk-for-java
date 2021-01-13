# Dev Azure Cosmos DB OLTP Spark connector client library for Java

### How to run unit

To run the tests of Spark connector without running the SDK tests you need to install azure-cosmos first
```bash
mvn -e -DskipTests  -Dgpg.skip -Dmaven.javadoc.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl ,azure-cosmos -am clean install
```

To run unit tests:
```
mvn -e -Dgpg.skip -Dmaven.javadoc.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl ,azure-cosmos-spark_3-0_2-12 test package -Punit
```

To run integration tests (requires Cosmos DB endpoint)

Create the file ~/cosmos-v4.properties with the following content (modify to match your cosmos endpoint):

```
ACCOUNT_HOST=https://localhost:8081
ACCOUNT_KEY=C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==
```

run the following command to run end to end integration tests:

```bash
mvn -e -Dgpg.skip -Dmaven.javadoc.skip=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true -pl ,azure-cosmos-spark_3-0_2-12 test package -PsparkE2E
```

How to run style check:
```bash
mvn -e -Dgpg.skip -DskipTests -Dmaven.javadoc.skip=true -Dspotbugs.skip=false -Dcheckstyle.skip=false -Drevapi.skip=true -pl ,azure-cosmos-spark_3-0_2-12 -am clean package
```


