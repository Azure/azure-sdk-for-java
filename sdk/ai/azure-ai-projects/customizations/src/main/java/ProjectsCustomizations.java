import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;


/**
 * This class contains the customization code to customize the AutoRest generated code for the Agents Client library
 */
public class ProjectsCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
//        removeConversationsClientBuilder(libraryCustomization, logger);
    }

    private void removeConversationsClientBuilder(LibraryCustomization customization, Logger logger) {
        logger.info("Removing ConversationsClientBuilder class");
        customization.getRawEditor().removeFile("src/main/java/com/azure/ai/agents/ConversationsClientBuilder.java");
    }
}