import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.ConstructorCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.JavadocCustomization;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import org.slf4j.Logger;

import java.lang.reflect.Modifier;
import java.util.Arrays;


public class AzureOpenAICustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        // remove unused class (no reference to them, after partial-update)
        removeClients(customization, logger);
    }

    private void removeClients(LibraryCustomization customization, Logger logger) {
        // right now doing this customization manually, as the emitter fails otherwise
//        Arrays.asList(
//            "AzureAudioSpeechClient",
//            "AzureAudioTranscriptionClient",
//            "AzureAudioTranslationClient",
//            "AzureChatClient",
//            "AzureCompletionsClient",
//            "AzureFilesClient",
//            "AzureImagesClient",
//            "AzureMessagesClient",
//            "AzureOpenAIClientBuilder",
//            "AzureOpenAIServiceVersion",
//            "AzureVectorStoresClient"
//        ).forEach(className -> {
//            ClassCustomization classCustomization = customization.getClassCustomization(className);
//            if (classCustomization != null) {
//                classCustomization.setRemove(true);
//            }
//        });
    }
}