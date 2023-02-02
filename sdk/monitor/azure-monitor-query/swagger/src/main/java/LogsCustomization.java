import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

public class LogsCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        ClassCustomization azureLogAnalytics = libraryCustomization.getClass("com.azure.monitor.query.implementation.logs", "AzureLogAnalytics");
        azureLogAnalytics.rename("AzureLogAnalyticsImpl");

        ClassCustomization metadatas = libraryCustomization.getClass("com.azure.monitor.query.implementation.logs", "Metadatas");
        metadatas.rename("MetadatasImpl");

        ClassCustomization queries = libraryCustomization.getClass("com.azure.monitor.query.implementation.logs", "Queries");
        queries.rename("QueriesImpl");

        ClassCustomization azureLogAnalyticsBuilder = libraryCustomization.getClass("com.azure.monitor.query.implementation.logs", "AzureLogAnalyticsBuilder");
        azureLogAnalyticsBuilder.rename("AzureLogAnalyticsImplBuilder");
        String replace = libraryCustomization.getRawEditor().getFileContent("src/main/java/com/azure/monitor/query/implementation" +
                        "/logs/AzureLogAnalyticsImplBuilder.java")
                .replace("policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format(\"%s/" +
                                ".default\", host)));",
                        "String localHost = (host != null) ? host : \"https://api.loganalytics.io\";\n" +
                                "policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format(\"%s/.default\", localHost)));");
        libraryCustomization.getRawEditor().replaceFile("src/main/java/com/azure/monitor/query/implementation" +
                "/logs/AzureLogAnalyticsImplBuilder.java", replace);
    }
}
