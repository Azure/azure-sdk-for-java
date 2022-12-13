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
    }
}
