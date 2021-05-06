// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.JavaClass;
import com.azure.digitaltwins.parser.implementation.codegen.JavaScope;
import com.azure.digitaltwins.parser.implementation.codegen.JavaSorted;
import com.azure.digitaltwins.parser.implementation.codegen.JavaSwitch;
import com.azure.digitaltwins.parser.implementation.parsergen.MaterialPropertyDigest;
import com.azure.digitaltwins.parser.implementation.parsergen.ParserGeneratorStringValues;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public abstract class MaterialProperty {
    private final ClientLogger logger = new ClientLogger(MaterialProperty.class);

    private String propertyName;
    protected String obversePropertyName;
    protected Map<Integer, String> propertyNameUris;
    protected MaterialPropertyDigest propertyDigest;
    protected String missingPropertyVariable;
    protected String keyProperty;
    protected String propertyType;
    protected PropertyRepresentation representation;
    protected String shadowExpression;
    protected PropertyKind propertyKind;
    private Map<Integer, List<PropertyRestriction>> propertyRestrictions;

    protected static final String REGEX_PATTERN_FIELD_SUFFIX = "PropertyRegexPatternV";
    private static final String MISSING_PROPERTY_VARIABLE_SUFFIX = "PropertyMissing";

    /**
     * Initializes a new instance of the {@link MaterialProperty} class.
     *
     * @param propertyName The name of the property in DTDL.
     * @param obversePropertyName The name of the property in the java object model.
     * @param propertyNameUris A map that maps from DTDL version to the URI of the property name.
     * @param propertyDigest A {@link MaterialPropertyDigest} object containing material property information extracted from the meta-model.
     * @param propertyRestrictions A list of objects that implement the {@link PropertyRestriction} interface.
     */
    public MaterialProperty(
        String propertyName,
        String obversePropertyName,
        Map<Integer, String> propertyNameUris,
        MaterialPropertyDigest propertyDigest, Map<Integer,
        List<PropertyRestriction>> propertyRestrictions) {
        this.propertyName = propertyName;
        this.obversePropertyName = obversePropertyName;
        this.propertyNameUris = propertyNameUris;
        this.propertyDigest = propertyDigest;
        this.propertyRestrictions = propertyRestrictions;
        this.missingPropertyVariable = (this.propertyName == null ? "" : this.propertyName).concat(MISSING_PROPERTY_VARIABLE_SUFFIX);
        this.shadowExpression = propertyDigest.isShadowed()
            ? "(this."
                .concat(ParserGeneratorStringValues.SHADDOW_PROPERTY_PREFIX)
                .concat(obversePropertyName)
                .concat(" != null ? ")
                .concat("this.")
                .concat(ParserGeneratorStringValues.SHADDOW_PROPERTY_PREFIX)
                .concat(obversePropertyName)
                .concat(" : this.")
                .concat(obversePropertyName)
                .concat(")")
            : "this.".concat(obversePropertyName);

        // TODO: remove once class is fully implemented.
        this.keyProperty = "";
        this.representation = PropertyRepresentation.ITEM;
        this.propertyKind = PropertyKind.INTERNAL;

        logger.info(String.format("%s", this.keyProperty));
        logger.info(String.format("%s", this.representation));
        logger.info(String.format("%s", this.propertyKind));
        logger.info(String.format("%s", this.propertyRestrictions));
    }

    /**
     * @return Gets the name of the property in DTDL.
     */
    public String getPropertyName() {
        return this.propertyName;
    }

    /**
     * @return Gets the kind of property.
     */
    public abstract PropertyKind getPropertyKind();

    /**
     * @return Gets the form in which the property is represented.
     */
    public abstract PropertyRepresentation getRepresentation();

    /**
     * @return Gets the type of the property.
     */
    public abstract String getPropertyType();

    /**
     * @return Gets the name of the key property if the property representation is a map, or null if it is not.
     */
    public String getKeyProperty() {
        return null;
    }

    /**
     * @return Gets the name of the property in the java object model.
     */
    protected String getObversePropertyName() {
        return this.obversePropertyName;
    }

    /**
     *
     * @return Gets map that maps from DTDL version to the URI of the property name.
     */
    protected Map<Integer, String> getPropertyNameUris() {
        return this.propertyNameUris;
    }

    /**
     * @return Gets a {@link MaterialPropertyDigest} object containing material property information extracted from the meta-model.
     */
    protected MaterialPropertyDigest getPropertyDigest() {
        return this.propertyDigest;
    }

    /**
     * @return Gets the name of a variable that indicates a required property is not present in the model.
     */
    protected String getMissingPropertyVariable() {
        return this.missingPropertyVariable;
    }

    /**
     * @return Gets the name of an expression that provides a shadowed value of the property if shadowed or the obverse name if not.
     */
    protected String getShadowExpression() {
        return this.shadowExpression;
    }

    /**
     * Indicates whether the property's value can be established by the content of a model.
     *
     * @param dtdlVersion The DTDL version that defines the property.
     * @return True if the property's value can be established by the content of a model.
     */
    public abstract boolean isParsable(int dtdlVersion);

    /**
     * Indicates whether the property has a restriction on the minimum or maximum count of values.
     *
     * @param dtdlVersion The DTDL version that defines the property.
     * @return True if there is a restriction.
     */
    public abstract boolean hasCountRestriction(int dtdlVersion);

    /**
     * Generate code for the constructor of the material class that has this property.
     *
     * @param sorted A {@link JavaSorted} object to which to add the code.
     */
    public abstract void generateConstructorCode(JavaSorted sorted);

    /**
     * Generate code for the Equals method of the material class that has this property.
     *
     * @param sorted A {@link JavaSorted} object to which to add the code.
     */
    public abstract void addEqualsLine(JavaSorted sorted);

    /**
     * Generate code for the GetHashCode method of the material class that has this property.
     *
     * @param sorted A {@link JavaSorted} object to which to add the code.
     */
    public abstract void addHashLine(JavaSorted sorted);

    /**
     * Generate appropriate members for the material class that has this property.
     *
     * @param dtdlVersions A list of DTDL major version numbers to generate members for.
     * @param obverseClass A {@link JavaClass} object to which to add the members.
     * @param classIsAugmentable True if the material class that has the property is augmentable.
     */
    public void addMembers(List<Integer> dtdlVersions, JavaClass obverseClass, boolean classIsAugmentable) {
        // TODO: implement.
    }

    /**
     * Generate code to iterate through all elements of the property and assign each one to a variable.
     *
     * @param outerScope The {@link JavaScope} to which to add the code.
     * @param varName The name of the variable to which each element is to be assigned.
     * @return A {@link JavaScope} to which additional code can be added by the caller.
     */
    public abstract JavaScope iterate(JavaScope outerScope, String varName);


    /**
     * Generate code to determine whether the property has at least one value.
     *
     * @param outerScope A {@link JavaScope} to which additional code can be added by the caller.
     * @return A {@link JavaScope} to which additional code can be added by the caller.
     */
    public abstract JavaScope checkPresence(JavaScope outerScope);

    /**
     * Generate code to set the defined value, if any, to the property.
     *
     * @param dtdlVersion The DTDL version that defines the value for the property.
     * @param scope The {@link JavaScope} to which to add the code.
     * @param infoVar Name of the variable that holds the entity info object.
     */
    public void setValue(int dtdlVersion, JavaScope scope, String infoVar) {
        // TODO: implement.
    }

    /**
     * Generate code to declare and initialize a variable that indicates a required property is not present in the model.
     *
     * @param dtdlVersion The DTDL version that determines whether the property is required.
     * @param scope The {@link JavaScope} to which to add the code.
     */
    public abstract void initMissingPropertyVariable(int dtdlVersion, JavaScope scope);

    /**
     * Generate code for the property's case within the parse-properties switch statement.
     *
     * @param dtdlVersion The DTDL version that determines the parsing logic for the property.
     * @param switchOnProperty The {@link JavaSwitch} to which to add the code.
     * @param classIsAugmentable True if the material class is augmentable.
     * @param classIsPartition True if the material class is a partition.
     * @param valueCountVar Name of the variable that holds the count of values found by the parse.
     * @param definedInVar Name of the variable that holds the identifier of the partition or top-level element under which this element is defined.
     */
    public abstract void addCaseToParseSwitch(
        int dtdlVersion,
        JavaSwitch switchOnProperty,
        boolean classIsAugmentable,
        boolean classIsPartition,
        String valueCountVar,
        String definedInVar);

    /**
     * Generate code to check for a required property.
     * @param dtdlVersion The DTDL version that determines whether the property is required.
     * @param scope The {@link JavaScope} to which to add the code.
     */
    public abstract void addCheckForRequiredProperty(int dtdlVersion, JavaScope scope);

    /**
     * Generate code to add the value of the property to a JSON object.
     *
     * @param dtdlVersion The DTDL version.
     * @param scope The {@link JavaScope} to which to add the code.
     * @param objectVar Name of a {@link JsonNode} variable to add the value to.
     * @param outlineByPartitionVar Name of variable indicating whether objects should be outlined if they have partition types.
     * @param outlineIfIdentifiedVar Name of variable indicating whether objects should be outlined if they have user-assigned identifiers.
     * @param contextIdsVar Name of variable holding a set of context identifiers to be updated by any contexts used in the DTDL of this element.
     */
    public abstract void addValueToObject(
        int dtdlVersion,
        JavaScope scope,
        String objectVar,
        String outlineByPartitionVar,
        String outlineIfIdentifiedVar,
        String contextIdsVar);

    /**
     * Generate code to add the property to a {@link JsonNode} of object properties.
     *
     * @param scope The {@link JavaScope} to which to add the code.
     * @param arrayVariable Name of a {@link JsonNode} variable to add the property to.
     * @param referenceVariable Name of a variable to add any object references to.
     */
    public abstract void addObjectPropertiesToArray(JavaScope scope, String arrayVariable, String referenceVariable);

    /**
     * Generate code to add the property to a <c>JArray</c> of literal properties.
     *
     * @param scope The {@link JavaScope} to which to add the code.
     * @param arrayVariable Name of a {@link JsonNode} variable to add the property to.
     */
    public abstract void addLiteralPropertiesToArray(JavaScope scope, String arrayVariable);

    /**
     * Generate code for the property's case within the TrySetObjectProperty method's switch statement.
     *
     * @param switchOnProperty The {@link JavaSwitch} to which to add the code.
     * @param valueVar Name of the variable that holds the value to set.
     * @param keyVar Name of the variable that holds the value of a map key.
     */
    public abstract void addCaseToTrySetObjectPropertySwitch(JavaSwitch switchOnProperty, String valueVar, String keyVar);

    /**
     * Generate code for the property's case within the property dictionary switch statement.
     * @param switchOnProperty The {@link JavaSwitch} to which to add the code.
     */
    public void addCaseToDictionaryKeySwitch(JavaSwitch switchOnProperty) {
    }

    /**
     * Generate code for the property's case within the add constraint switch statement.
     * @param switchOnProperty The {@link JavaSwitch} to which to add the code.
     * @param constraintVariable Name of a ValueConstraint variable to add to the field.
     */
    public void addCaseForValueConstraintSwitch(JavaSwitch switchOnProperty, String constraintVariable) {
    }

    /**
     * Generate code for the property's case within the add instance property switch statement.
     *
     * @param switchOnProperty The {@link JavaSwitch} to which to add the code.
     * @param instancePropVariable Name of a string variable to add to the field.
     */
    public void addCaseForInstancePropertySwitch(JavaSwitch switchOnProperty, String instancePropVariable) {
    }

    /**
     * Add code to the CheckRestrictions method in the material class that has this property.
     *
     * @param checkRestrictionsMethodBody A {@link JavaScope} object to which to add the code.
     * @param dtdlVersion The DTDL version that specifies the restrictions.
     * @param typeName The type name (DTDL term) corresponding to the material class.
     * @param classIsAugmentable True if the material class is augmentable.
     */
    public void addRestrictions(JavaScope checkRestrictionsMethodBody, int dtdlVersion, String typeName, boolean classIsAugmentable) {
        // TODO: implement.
    }

    /**
     * Add exemplification code for group property configuration.
     *
     * @param dtdlVersion The DTDL version to exemplify.
     * @param exemplifyMethodBody A {@link JavaScope} object to which to add the code.
     * @param infoVar Name of the variable that holds the element info.
     * @param configuratorVar Name of the variable that holds a ClassConfigurator object that provides exemplification instructions.
     * @param exemplifierVar Name of the variable that holds a ValueExemplifier object for exemplifying literal values.
     * @param descendantRestrictionsVar Name of the variable that holds a list of TypeExclusion objects to restrict exemplification.
     * @param keyVar Name of the variable that holds a value to be used for the key property if the parent exposes a collection of these elements as a map.
     * @param segVar Name of the variable that holds a DTMI segment value, used for deriving an Id value from a parent identifier.
     */
    public abstract void addGroupExemplification(
        int dtdlVersion,
        JavaScope exemplifyMethodBody,
        String infoVar,
        String configuratorVar,
        String exemplifierVar,
        String descendantRestrictionsVar,
        String keyVar,
        String segVar);

    /**
     * Add exemplification code for target properties.
     *
     * @param dtdlVersion Add exemplification code for target properties.
     * @param exemplifyMethodBody A {@link JavaScope} object to which to add the code.
     * @param typeName The type name (DTDL term) corresponding to the material class.
     * @param className The java name of the material class.
     * @param kindProperty The property on the DTDL base obverse class that indicates the kind of DTDL element.
     * @param infoVar Name of the variable that holds the element info.
     * @param configuratorVar Name of the variable that holds a ClassConfigurator object that provides exemplification instructions.
     * @param exemplifierVar Name of the variable that holds a <c>ValueExemplifier</c> object for exemplifying literal values.
     * @param descendantRestrictionsVar Name of the variable that holds a list of TypeExclusion objects to restrict exemplification.
     * @param keyVar Name of the variable that holds a value to be used for the key property if the parent exposes a collection of these elements as a dictionary.
     * @param descendantControls A list of objects that implement the {@link DescendantControl} interface.
     */
    public abstract void addIndividualExemplification(
        int dtdlVersion,
        JavaScope exemplifyMethodBody,
        String typeName,
        String className,
        String kindProperty,
        String infoVar,
        String configuratorVar,
        String exemplifierVar,
        String descendantRestrictionsVar,
        String keyVar,
        List<DescendantControl> descendantControls);

    /**
     * Add exemplification code for a property with a data-type given by a string variable.
     *
     * @param dtdlVersion The DTDL version to exemplify.
     * @param scope A {@link JavaScope} object to which to add the code.
     * @param datatypeVar Name of the variable that holds a DTDL schema ID indicating the appropriate data-type.
     */
    public void exemplifyDatatype(int dtdlVersion, JavaScope scope, String datatypeVar) {
    }
}
