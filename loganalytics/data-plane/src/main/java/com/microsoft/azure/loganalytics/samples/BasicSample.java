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
public class BasicSample 
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