# Test Framework

This SDK uses the TestBase core class to record and playback http sessions, to perform integration tests with Search service in Azure.

## Usage

The class: **SearchIndexClientTestBase** is the base test class which other test classes, that need to perform integration tests, must extend it.

These test classes then can use the base client builder, and run the relevant build method.

### Code example

```java
public class SyncTests extends SearchIndexClientTestBase {

    private SearchIndexClient asyncClient;

    @Override
    void beforeTest() {
        super.beforeTest();

        createHotelIndex();
        asyncClient = getSearchIndexClientBuilder("hotels").buildAsyncClient();
    }

    @Test
    public void uploadResultSucceeds() throws Exception {
        Document doc = new Document();
        doc.put("HotelId", "1");

        List<Document> docs = new ArrayList<>();
        docs.add(doc);

        DocumentIndexResult result = asyncClient.uploadDocuments(docs).block();
        assert result.results().get(0).statusCode() == 200;
    }
}
```

Before running the tests on PLAYBACK mode, you need to run them on RECORD mode to record the http sessions.

### Recording http sessions

1. Add the following environment variables to your development SDK to be used in JUnit runs:
    * AZURE_CLIENT_ID: *Client ID in Azure Active Directory*
    [How to create a Client ID in Azure Portal](https://docs.microsoft.com/azure/active-directory/develop/quickstart-register-app)
    * AZURE_CLIENT_SECRET: *Client ID Secret*
    * AZURE_TENANT_ID: *Tenant ID in Azure Active Directory*
    * AZURE_SUBSCRIPTION_ID: *Azure subscription ID*
    * AZURE_TEST_MODE: **RECORD** *for recording* (**PLAYBACK** *for playback mode*)
    * AZURE_TEST_ENVIRONMENT (optional): **DOGFOOD** if performing internal testing. Defaults to public Azure if omitted or set to any other value  

2. Run the tests.

    > Please note: In RECORD mode, every test is creating a new testing environment in Azure before the test, and deleting it after the test. This process takes some time, so the tests in this mode (RECORD) are slower.

3. After test runs finish, you can find the recorded sessions under the project's target folder:

    `target/test-classes/session-records`

    Every test will have its own session file.

4. Copy the records to the project's test resources folder:

    `test/resources/session-records`

> The session files should also be submitted to the project's repository, so the tests in the CI build will use them in playback mode.

### Running tests in PLAYBACK mode

To run tests in Playback mode:

1. Set the value of the **AZURE_TEST_MODE** environment variable to *'PLAYBACK'* (or delete it). You can also delete all other environment variables.
2. Run the tests.
