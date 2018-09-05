# Azure Log Analytics

This project provides client tools or utilities in Java that make it easy to query data in [Azure Log Analytics](https://azure.microsoft.com/en-us/services/log-analytics/). For reference documentation on classes and models, please see the [Azure SDK for Java reference](https://docs.microsoft.com/en-us/java/api/overview/azure/?view=azure-java-stable). 

Azure Log Analytics provides agents for telemtry collection and enables deep analytics via a [rich query language](https://docs.loganalytics.io/index). This SDK provides query access to data already stored in Log Analytics. To start collecting data from different sources, take a look at these [quickstarts](https://docs.microsoft.com/en-us/azure/log-analytics/log-analytics-quick-collect-azurevm). 

## Example

```java
import java.util.List;
import java.util.stream.Collectors;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.loganalytics.implementation.LogAnalyticsDataClientImpl;
import com.microsoft.azure.loganalytics.models.QueryBody;
import com.microsoft.azure.loganalytics.models.QueryResults;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;

/**
 * Basic query example
 *
 */
public class LogAnalyticsClientExample 
{
    public static void main( String[] args )
    {
        // ApplicationTokenCredentials work well for service principal authentication
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
            "<clientId>",
            "<tenantId>",
            "<clientSecret>",
            AzureEnvironment.AZURE
        );
        
        // New up client. Accepts credentials, or a pre-authenticated restClient
        LogAnalyticsDataClientImpl client = new LogAnalyticsDataClientImpl(credentials);
        
        // Prepare information for query
        String query = "Heartbeat | take 1";
        String workspaceId = "<logAnalyticsWorkspaceGUID>";
        
        // Execute!
        QueryResults queryResults = client.query(workspaceId, new QueryBody().withQuery(query));
        
        // Process and print results
        List<Object> row = queryResults.tables().get(0).rows().get(0);
        List<String> columnNames = queryResults
            .tables()
            .get(0)
            .columns()
            .stream()
            .map(elt -> elt.name())
            .collect(Collectors.toList());

        for (int i = 0; i < row.size(); i++){        
            System.out.println("The value of " + columnNames.get(i) + " is " + row.get(i));
        }
        
        return;
    }
}
```

## Download

### Latest stable release

To get the binaries of the official Microsoft Azure Log Analytics SDK as distributed by Microsoft, reade for use within your project, you can use Maven.

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-loganalytics</artifactId>
    <version>LATEST</version>
</dependency>
```

## Prerequisites

- A Java Developer Kit (JDK), v 1.7 or later
- Maven

## Help and Issues

If you encounter any bugs with these SDKs, please file issues via [Issues](https://github.com/Azure/azure-sdk-for-java/issues) or checkout [StackOverflow for Azure Java SDK](http://stackoverflow.com/questions/tagged/azure-java-sdk).

## Contribute Code

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

## More information
- [Azure Java SDKs](https://docs.microsoft.com/java/azure/)
- If you don't have a Microsoft Azure subscription you can get a FREE trial account [here](http://go.microsoft.com/fwlink/?LinkId=330212)

---

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.