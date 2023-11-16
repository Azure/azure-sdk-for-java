import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;

import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * This class contains the customization code to customize the AutoRest generated code for App Configuration.
 */
public class MyCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        logger.info("Customizing the JobMatchingMode class");
        PackageCustomization packageCustomization = customization.getPackage("com.azure.communication.jobrouter.models");
        ClassCustomization classCustomization = packageCustomization.getClass("JobMatchingMode");
        classCustomization.setModifier(Modifier.ABSTRACT);
    }
}