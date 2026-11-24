// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

public class TemplateCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        /*
         * Java Autorest customizations are a way to programmatically modify Autorest generated code after generation.
         * Customizations can perform operations not available to Swagger transforms such as adding custom methods,
         * modifying annotations, and renaming properties in ways not possible in Swagger. The purpose of customizations
         * is to remove the need for manual translation layers in implementation code, this offers multiple benefits:
         *
         * - Reduces code size of an SDK as customizations happen during code generation.
         * - Improves performance as customizations are done during code generation and not during runtime.
         * - Reduces SDK code maintenance as translation layers need to be updated whenever there are changes to the
         *   types being translated.
         *
         * Code customizations can perform many of the operations available to Swagger transforms such as client side
         * renaming of properties, class renames, etc. When given the choice between a code customization and Swagger
         * transform ALWAYS choose the Swagger transform. Swagger transforms are a simple re-writing of JSON where code
         * customizations are a refactoring process requiring the use of a language server (started and stopped during
         * code generation). In addition to the overhead to perform the changes, code customization happens after Java
         * Autorest generates based on the Swagger, meaning code generation doesn't know of the changes it will make.
         * For example, adding a constructor using code customization could break serialization as Java Autorest will
         * generate serialization handling based on the Swagger definition, or another example is removing a property
         * from a class.
         */
    }
}
