// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.JavadocCustomization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains customizations for Azure Personalizer swagger code generation.
 */
public class PersonalizerCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        // Following classes are not intended for consumption by external callers. But we cannot do that since the
        // current autorest configuration only allows for two packages (and not three). Since these are with the
        // models package but still need to be accessible from the administration.models package, we have to make them
        // public. Once issue https://github.com/Azure/autorest.java/issues/1647 is fixed, we can have these in the
        // implementation package and hidden from the customer.
//        Arrays.asList("ErrorResponse", "ErrorResponseException",
//                "InternalError", "PersonalizerError", "PersonalizerErrorCode",
//                "ServiceStatus")
        Arrays.asList("ServiceStatus")
            .forEach(className -> {
                libraryCustomization
                    .getClass("com.azure.ai.personalizer.models", className)
                    .setModifier(0); // 0 -> package-private
            });


        // Same comment as line 21 above applies here as well.
//        Arrays.asList("EvaluationsCreateHeaders", "PersonalizerPolicyReferenceOptions")
//            .forEach(className -> {
//                libraryCustomization
//                    .getClass("com.azure.ai.personalizer.administration.models", className)
//                    .setModifier(0); // 0 -> package-private
//            });

        // useBinaryDataForRankApis(libraryCustomization, logger);
        renameLogMirrorSasUriProperty(libraryCustomization, logger);
        hideMethods(libraryCustomization, logger);
        renameOfflineExperimentationProperties(libraryCustomization, logger);
        returnBaseClassTypesForMethodReturnValues(libraryCustomization, logger);
    }

    private void useBinaryDataForRankApis(LibraryCustomization libraryCustomization, Logger logger) {
        Arrays.asList("PersonalizerRankableAction", "PersonalizerRankMultiSlotOptions", "PersonalizerRankOptions", "PersonalizerSlotOptions")
            .forEach(className -> {
                String fileName = "src/main/java/com/azure/ai/personalizer/models/" + className + ".java";
                libraryCustomization
                    .getClass("com.azure.ai.personalizer.models", className)
                    .addImports("com.azure.core.util.BinaryData");
                libraryCustomization.getRawEditor()
                    .searchText(fileName, "List<Object>")
                    .forEach(range -> {
                        libraryCustomization
                            .getRawEditor()
                            .replace(fileName, range.getStart(), range.getEnd(), "List<BinaryData>");
                    });
            });
    }

    private void renameLogMirrorSasUriProperty(LibraryCustomization libraryCustomization, Logger logger) {
        logger.info("Renaming logMirrorSasUri property");
        // renaming the model property
        libraryCustomization
            .getClass("com.azure.ai.personalizer.administration.models", "PersonalizerServiceProperties")
            .getProperty("logMirrorSasUri")
            .rename("logMirrorSasUrl");

        libraryCustomization
            .getClass("com.azure.ai.personalizer.administration.models", "PersonalizerServiceProperties")
            .getMethod("setLogMirrorSasUrl")
            .replaceParameters("String logMirrorSasUrl")
            .replaceBody("this.logMirrorSasUrl = logMirrorSasUrl;return this;");

        JavadocCustomization getMethodJavaDoc = libraryCustomization
            .getClass("com.azure.ai.personalizer.administration.models", "PersonalizerServiceProperties")
            .getMethod("getLogMirrorSasUrl")
            .getJavadoc();
        getMethodJavaDoc.setDescription(getMethodJavaDoc.getDescription().replace("Uri", "Url"));
        getMethodJavaDoc.setReturn(getMethodJavaDoc.getReturn().replace("Uri", "Url"));
        libraryCustomization
            .getClass("com.azure.ai.personalizer.administration.models", "PersonalizerServiceProperties")
            .getMethod("getLogMirrorSasUrl")
            .getJavadoc()
            .replace(getMethodJavaDoc);

        JavadocCustomization setMethodJavaDoc = libraryCustomization
            .getClass("com.azure.ai.personalizer.administration.models", "PersonalizerServiceProperties")
            .getMethod("setLogMirrorSasUrl")
            .getJavadoc();
        setMethodJavaDoc.setDescription(getMethodJavaDoc.getDescription().replace("Uri", "Url"));
        setMethodJavaDoc.setParam("logMirrorSasUrl", setMethodJavaDoc.getParams().get("logMirrorSasUri").replace("Uri", "Url"));
        setMethodJavaDoc.removeParam("logMirrorSasUri");
        libraryCustomization
            .getClass("com.azure.ai.personalizer.administration.models", "PersonalizerServiceProperties")
            .getMethod("getLogMirrorSasUrl")
            .getJavadoc()
            .replace(getMethodJavaDoc);
    }

    private void hideMethods(LibraryCustomization libraryCustomization, Logger logger) {
        Map<String, List<String>> adminModelsMap = new HashMap<String, List<String>>();
        adminModelsMap.put("PersonalizerPolicyResultSummary", Arrays.asList("setNonZeroProbability"));
        adminModelsMap.put("PersonalizerEvaluation", Arrays.asList("setEvaluationType", "getJobId", "setPolicyResults", "setFeatureImportance", "setOptimalPolicy", "setCreationTime"));
        adminModelsMap.forEach((className, methodNames) -> makeMethodPrivate(libraryCustomization, "com.azure.ai.personalizer.administration.models", className, methodNames, logger));

        Map<String, List<String>> modelsMap = new HashMap<String, List<String>>();
        modelsMap.put("PersonalizerSlotResult", Arrays.asList("setId"));
        modelsMap.put("PersonalizerError", Arrays.asList("setCode", "setMessage", "setTarget", "setDetails"));
        modelsMap.forEach((className, methodNames) -> makeMethodPrivate(libraryCustomization, "com.azure.ai.personalizer.models", className, methodNames, logger));
    }

    private void makeMethodPrivate(LibraryCustomization libraryCustomization, String packageName, String className, List<String> methodNames, Logger logger) {
        methodNames.forEach(methodName -> {
            libraryCustomization
                .getClass(packageName, className)
                .getMethod(methodName)
                .setModifier(0);
        });
    }

    private void renameOfflineExperimentationProperties(LibraryCustomization libraryCustomization, Logger logger) {
        libraryCustomization
            .getClass("com.azure.ai.personalizer.administration.models", "PersonalizerEvaluationOptions")
            .getMethod("isEnableOfflineExperimentation")
            .rename("isOfflineExperimentationEnabled");

        libraryCustomization
            .getClass("com.azure.ai.personalizer.administration.models", "PersonalizerEvaluationOptions")
            .getMethod("setEnableOfflineExperimentation")
            .rename("setOfflineExperimentationEnabled");
    }

    private void returnBaseClassTypesForMethodReturnValues(LibraryCustomization libraryCustomization, Logger logger) {
        libraryCustomization
            .getClass("com.azure.ai.personalizer.administration.models", "PersonalizerPolicyResult")
            .getMethod("getTotalSummary")
            .setReturnType("PersonalizerPolicyResultSummary", "returnValue");

        libraryCustomization
            .getClass("com.azure.ai.personalizer.administration.models", "PersonalizerLogProperties")
            .getMethod("getDateRange")
            .setReturnType("PersonalizerDateRange", "returnValue");
    }
}
