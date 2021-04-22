// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.digitaltwins.parser.implementation.codegen.JavaClass;
import com.azure.digitaltwins.parser.implementation.codegen.JavaScope;

import java.util.List;

/**
 * Represents a control (either restriction or transformation) on the descendants of a class that is materialized in the parser object model.
 */
public interface DescendantControl {
    /**
     * Indicates whether this control applies to the given type name.
     *
     * @param typeName The name of the type to check.
     * @return True if applicable.
     */
    boolean appliesToType(String typeName);

    /**
     * Generate appropriate members for the material class that has this control.
     *
     * @param obverseClass >A {@link JavaClass} object to which to add the members.
     * @param typeName The type name (DTDL term) corresponding to the material class.
     * @param classIsBase True if the material class is the DTDL base class.
     * @param classIsAbstract True if the material class is abstract.
     * @param materialProperties A list of the {@link MaterialProperty} objects associated with the material class.
     */
    void addMembers(
        JavaClass obverseClass,
        String typeName,
        boolean classIsBase,
        boolean classIsAbstract,
        List<MaterialProperty> materialProperties
    );

    /**
     * Add code to the CheckRestrictions method in the material class that has this control.
     *
     * @param checkRestrictionsMethodBody A {@link JavaScope} object to which to add the code.
     * @param dtdlVersion The version of DTDL whose restriction should be added.
     * @param typeName The type name (DTDL term) corresponding to the material class.
     */
    void addRestriction(JavaScope checkRestrictionsMethodBody, int dtdlVersion, String typeName);

    /**
     * Add code to the ApplyTransformations method in the material class that has this control.
     *
     * @param applyTransformationsMethodBody A {@link JavaScope} object to which to add the code.
     * @param dtdlVersion The version of DTDL whose transformation should be added.
     * @param typeName The type name (DTDL term) corresponding to the material class.
     * @param materialProperties A list of the {@link MaterialProperty} objects associated with the material class.
     */
    void addTransformation(JavaScope applyTransformationsMethodBody, int dtdlVersion, String typeName, List<MaterialProperty> materialProperties);

    /**
     * Add code to the ExemplifyClass method in the material class that has this control.
     *
     * @param exemplifyClassMethodBody A {@link JavaScope} object to which to add the code.
     * @param typeName The type name (DTDL term) corresponding to the material class.
     * @param descendantRestrictionsVar Name of the variable that holds a list of DescendantRestriction objects.
     */
    void addExemplificationPrelude(JavaScope exemplifyClassMethodBody, String typeName, String descendantRestrictionsVar);

    /**
     *  Add code to the ExemplifyClass method in the material class that has this control.
     *
     * @param exemplifyClassMethodBody A {@link JavaScope} object to which to add the code.
     * @param typeName The type name (DTDL term) corresponding to the material class.
     * @param infoVar Name of the variable that holds the element info.
     * @param valueExemplifierVar Name of the variable that holds a ValueExemplifier object for exemplifying literal values.
     */
    void addExemplification(JavaScope exemplifyClassMethodBody, String typeName, String infoVar, String valueExemplifierVar);
}
