// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains customizations for Azure Analytics Planetary Computer code generation.
 *
 * <p>Workaround for non-deterministic TypeReference ordering in generated Java clients.
 * The Java code generator produces TypeReference entries for the StacItemOrStacItemCollection
 * discriminated union subtypes (StacItem, StacItemCollection) in a non-deterministic order,
 * causing spurious diffs on each regeneration in DataClient.java and DataAsyncClient.java.
 *
 * <p>This customization enforces a consistent alphabetical ordering of TypeReference entries.
 *
 * <p><b>Temporary:</b> This can be removed once the generator fix for deterministic
 * TypeReference ordering is shipped.
 */
public class PlanetaryComputerCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        PackageCustomization rootPackage = libraryCustomization.getPackage("com.azure.analytics.planetarycomputer");

        // DataAsyncClient and DataClient have known issues with TypeReference order changes between generation.
        sortTypeReferencesInFile(rootPackage.getClass("DataAsyncClient"));
        sortTypeReferencesInFile(rootPackage.getClass("DataClient"));

        // Also sort the other clients just in case.
        sortTypeReferencesInFile(rootPackage.getClass("IngestionAsyncClient"));
        sortTypeReferencesInFile(rootPackage.getClass("IngestionClient"));
        sortTypeReferencesInFile(rootPackage.getClass("SharedAccessSignatureAsyncClient"));
        sortTypeReferencesInFile(rootPackage.getClass("SharedAccessSignatureClient"));
        sortTypeReferencesInFile(rootPackage.getClass("StacAsyncClient"));
        sortTypeReferencesInFile(rootPackage.getClass("StacClient"));
    }

    /**
     * Sorts consecutive lines containing TypeReference declarations/usages in the
     * given file to enforce a deterministic order.
     *
     * <p>The method identifies groups of consecutive lines that each contain a
     * TypeReference pattern, sorts each group alphabetically by the generic type
     * parameter name, and writes the sorted content back to the file.
     *
     * @param customization The Java code generator class customizer.
     */
    private static void sortTypeReferencesInFile(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            // TypeReference types are fields in the class with a single variable.
            List<FieldDeclaration> typeReferenceFields = clazz.getFields().stream()
                .filter(field -> field.getVariables().size() == 1
                    && field.getVariable(0).getType().toString().startsWith("TypeReference"))
                .collect(Collectors.toList());

            // Remove each TypeReference field from the class definition.
            typeReferenceFields.forEach(Node::remove);

            // Sort on the field name.
            typeReferenceFields.sort(Comparator.comparing(o -> o.getVariable(0).getNameAsString()));

            // Add each TypeReference back to the class.
            typeReferenceFields.forEach(clazz::addMember);
        }));
    }
}
