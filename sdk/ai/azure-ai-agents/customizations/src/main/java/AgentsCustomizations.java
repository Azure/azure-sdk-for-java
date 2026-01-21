import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;


/**
 * This class contains the customization code to customize the AutoRest generated code for the Agents Client library
 * Reference: https://github.com/Azure/autorest.java/blob/main/customization-base/README.md
 */
public class AgentsCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        // no-op: reserved for future use
    }
}