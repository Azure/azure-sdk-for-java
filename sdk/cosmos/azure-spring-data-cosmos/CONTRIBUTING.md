# Contributing
This instruction is guideline for building and code contribution.

## Prequisites
- JDK 1.8 and above
- [Maven](http://maven.apache.org/) 3.0 and above

## Build from source
To build the project, run maven commands.

```bash
git clone https://github.com/Azure/azure-sdk-for-java.git 
cd sdk/cosmos/azure-spring-data-cosmos-core
mvnw clean install
```

## Test
There're integration tests on azure and on emulator to trigger integration test execution against Azure Cosmos DB and against [Azure Cosmos DB Emulator](https://docs.microsoft.com/azure/cosmos-db/local-emulator), you need to follow the link to setup emulator before test execution.

- Run unit tests
```bash
mvn clean install -Dgpg.skip
```

- Run integration tests
   - on Azure 
     >**NOTE** Please note that integration test against Azure requires Azure Cosmos DB Document API and will automatically create a Cosmos database in your Azure subscription, then there will be **Azure usage fee.**
 
     Integration tests will require a Azure Subscription. If you don't already have an Azure subscription, you can activate your [MSDN subscriber benefits](https://azure.microsoft.com/pricing/member-offers/msdn-benefits-details/) or sign up for a [free Azure account](https://azure.microsoft.com/free/). 
  
     1. Create an Azure Cosmos DB on Azure.
        - Go to [Azure portal](https://portal.azure.com/) and click +New.
        - Click Databases, and then click Azure Cosmos DB to create your database. 
        - Navigate to the database you have created, and click Access keys and copy your URI and access keys for your database.
  
     2. Set environment variables ACCOUNT_HOST, ACCOUNT_KEY and SECONDARY_ACCOUNT_KEY, where value of them are Cosmos account URI, primary key and secondary key. 
     
        `azure-spring-data-cosmos-core` also support multiple database configuration. So set the second group environment variables NEW_ACCOUNT_HOST, NEW_ACCOUNT_KEY and NEW_SECONDARY_ACCOUNT_KEY, the two group environment variables can be same.
     3. Run maven command with `integration-test-azure` profile. 
  
        ```bash
        set ACCOUNT_HOST=your-cosmos-account-uri
        set ACCOUNT_KEY=your-cosmos-account-primary-key
        set SECONDARY_ACCOUNT_KEY=your-cosmos-account-secondary-key
        
        set NEW_ACCOUNT_HOST=your-cosmos-account-uri
        set NEW_ACCOUNT_KEY=your-cosmos-account-primary-key
        set NEW_SECONDARY_ACCOUNT_KEY=your-cosmos-account-secondary-key
        mvnw -P integration-test-azure clean install
        ```
        
   - on Emulator
   
     Setup Azure Cosmos DB Emulator by following [this instruction](https://docs.microsoft.com/azure/cosmos-db/local-emulator), and set associated environment variables. Then run test with:
     ```bash
     mvnw -P integration-test-emulator install
     ```


- Skip tests execution
```bash
mvn clean install -Dgpg.skip-DskipTests
```

## Version management
Developing version naming convention is like `0.1.2-beta.1`. Release version naming convention is like `0.1.2`. 

## Contribute to code
Contribution is welcome. Please follow [this instruction](https://github.com/Azure/azure-sdk-for-java/blob/master/CONTRIBUTING.md) to contribute code.
