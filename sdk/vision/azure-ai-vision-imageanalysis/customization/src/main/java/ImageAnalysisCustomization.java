import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.JavadocCustomization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.MethodCustomization;
import org.slf4j.Logger;
import com.github.javaparser.StaticJavaParser;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains the customization code to customize the TypeSpec generated code for Azure AI Image Analysis.
 */
public class ImageAnalysisCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        updateClientJavadoc(
            customization.getPackage("com.azure.ai.vision.imageanalysis").getClass("ImageAnalysisAsyncClient"),
            "async-client-api-key-auth",
            "async-client-entra-id-auth"
        );

        updateClientJavadoc(
            customization.getPackage("com.azure.ai.vision.imageanalysis").getClass("ImageAnalysisClient"),
            "sync-client-api-key-auth",
            "sync-client-entra-id-auth"
        );
    }

    private void updateClientJavadoc(ClassCustomization clientClass, String apiKeyAuthTag, String entraIdAuthTag) {
        JavadocCustomization javadoc = clientClass.getJavadoc();
        String description = javadoc.getDescription();
        String codeSnippetTags = String.format(
            "\n* <!-- src_embed com.azure.ai.vision.imageanalysis.%s -->" +
                "\n* <!-- end com.azure.ai.vision.imageanalysis.%s -->" +
                "\n* <!-- src_embed com.azure.ai.vision.imageanalysis.%s -->" +
                "\n* <!-- end com.azure.ai.vision.imageanalysis.%s -->",
            apiKeyAuthTag, apiKeyAuthTag, entraIdAuthTag, entraIdAuthTag);
        javadoc.setDescription(description + codeSnippetTags);
    }


}
