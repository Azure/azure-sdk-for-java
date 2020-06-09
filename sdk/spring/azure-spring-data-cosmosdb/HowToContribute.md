# How to Build and Contribute
This instruction is guideline for building and code contribution.

## Prequisites
- JDK 1.8 and above
- [Maven](http://maven.apache.org/) 3.0 and above

## Build from source
To build the project, run maven commands.

```bash
git clone https://github.com/Microsoft/spring-data-cosmosdb.git 
cd spring-data-cosmosdb
mvnw clean install
```

## Test
There're 3 profiles: `dev`, `integration-test-azure` and `integration-test-emulator`. Default profile is `dev`. Profile `integration-test-azure` will trigger integration test execution against Azure Cosmos DB. Profile `integration-test-emulator` will trigger integration test execution against [Azure Cosmos DB Emulator](https://docs.microsoft.com/en-us/azure/cosmos-db/local-emulator), you need to follow the link to setup emulator before test execution.

- Run unit tests
```bash
mvnw clean install
```

- Run integration tests
   - on Azure 
     >**NOTE** Please note that integration test against Azure will automatically create a Azure Cosmos DB Document API in your Azure subscription, then there will be **Azure usage fee.**
 
     Integration tests will require a Azure Subscription. If you don't already have an Azure subscription, you can activate your [MSDN subscriber benefits](https://azure.microsoft.com/en-us/pricing/member-offers/msdn-benefits-details/) or sign up for a [free Azure account](https://azure.microsoft.com/en-us/free/). 
  
     1. Create a service principal by using Azure Cli or by [Azure Portal](https://docs.microsoft.com/en-us/azure/azure-resource-manager/resource-group-create-service-principal-portal). 
     2. After service principal ready, set environment variables CLIENT_ID, CLIENT_KEY and TENANT_ID, where value of them are service principal id, key and tenant id.
     3. Run maven command with `integration-test-azure` profile. 
  
        ```bash
        set CLIENT_ID=your-azure-service-principal-id
        set CLIENT_KEY=your-azure-service-principal-key
        set TENANT_ID=your-azure-subscription-tenant-id
        mvnw -P integration-test-azure clean install
        ```
   - on Emulator
   
     Setup Azure Cosmos DB Emulator by following [this instruction](https://docs.microsoft.com/en-us/azure/cosmos-db/local-emulator), then run test with:
     ```bash
     mvnw -P integration-test-emulator install
     ```


- Skip tests execution
```bash
mvnw clean install -DskipTests
```

## Version management
Developing version naming convention is like `0.1.2-SNAPSHOT`. Release version naming convention is like `0.1.2`. 

## CI
Both [travis](https://travis-ci.org/Microsoft/spring-data-cosmosdb) and [appveyor](https://ci.appveyor.com/project/yungez/spring-data-cosmosdb) CI is enabled.

## Contribute to code
Code contribution is welcome. To contribute to this module, please make sure below check list are checked.
- [ ] Build pass. checkstyle and findbugs is enabled by default. Please check [checkstyle.xml](config/checkstyle.xml) to learn detail checkstyle configuration.
- [ ] Documents are updated to align with code.
- [ ] Code coverage for new codes >= 65%. Code coverage check is enabled with 65% bar.
