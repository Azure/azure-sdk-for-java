// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.Access;
import com.azure.digitaltwins.parser.implementation.codegen.Multiplicity;
import com.azure.digitaltwins.parser.implementation.codegen.JavaScope;
import com.azure.digitaltwins.parser.implementation.codegen.JavaClass;
import com.azure.digitaltwins.parser.implementation.codegen.Mutability;
import com.azure.digitaltwins.parser.implementation.codegen.JavaSorted;
import com.azure.digitaltwins.parser.implementation.codegen.JavaSwitch;
import com.azure.digitaltwins.parser.implementation.parsergen.MaterialPropertyDigest;
import com.azure.digitaltwins.parser.implementation.parsergen.NameFormatter;
import com.azure.digitaltwins.parser.implementation.parsergen.PropertyVersionDigest;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents an object property on a class that is materialized in the parser object model.
 */
public abstract class ObjectProperty extends MaterialProperty {
    private final ClientLogger logger = new ClientLogger(ObjectProperty.class);

    private static final String VALUE_CONSTRAINT_FIELD_SUFFIX = "ValueConstraints";
    private static final String INSTANCE_PROPERTIES_FIELD_SUFFIX = "InstanceProperties";
    private static final String ALLOWED_VERSIONS_FIELD_SUFFIX = "AllowedVersions";

    protected String className;
    protected Map<Integer, String> versionedClassName;
    protected String valueConstraintsField;
    protected String instancePropertiesField;
    protected String allowedVersionsField;

    /**
     * Initializes a new instance of the {@link ObjectProperty} class.
     *
     * @param propertyName         The name of the property in DTDL.
     * @param obversePropertyName  The name of the property in the java object model.
     * @param propertyNameUris     A map that maps from DTDL version to the URI of the property name.
     * @param propertyDigest       A {@link MaterialPropertyDigest} object containing material property information extracted from the meta-model.
     * @param propertyRestrictions A list of objects that implement the {@link PropertyRestriction} interface.
     */
    public ObjectProperty(
        String propertyName,
        String obversePropertyName,
        Map<Integer, String> propertyNameUris,
        MaterialPropertyDigest propertyDigest,
        Map<Integer, List<PropertyRestriction>> propertyRestrictions) {
        super(propertyName, obversePropertyName, propertyNameUris, propertyDigest, propertyRestrictions);
        this.className = NameFormatter.formatNameAsClass(this.getPropertyDigest().getClassType());


        this.versionedClassName = new HashMap<>();
        this.getPropertyDigest().getPropertyVersions()
            .forEach(
                (key, val) ->
                    this.versionedClassName.put(key, NameFormatter.formatNameAsClass(val.getClassType())));

        this.valueConstraintsField = propertyName.concat(VALUE_CONSTRAINT_FIELD_SUFFIX);
        this.instancePropertiesField = propertyName.concat(INSTANCE_PROPERTIES_FIELD_SUFFIX);
        this.allowedVersionsField = propertyName.concat(ALLOWED_VERSIONS_FIELD_SUFFIX);
    }

    /**
     * @return Gets the obverse class name of the object property value.
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * @return Gets the obverse class name of the object property value for each DTDL version.
     */
    public Map<Integer, String> getVersionedClassName() {
        return this.versionedClassName;
    }

    /**
     * @return Gets the name of a field that holds a list of ValueConstraint objects for the object property.
     */
    public String getValueConstraintsField() {
        return this.valueConstraintsField;
    }

    /**
     * @return Gets the name of a field that holds a list of instance property names for the object property.
     */
    public String getInstancePropertiesField() {
        return this.instancePropertiesField;
    }

    /**
     * @return Gets the name of a field that holds a list of allowed version numbers for the object property.
     */
    public String getAllowedVersionsField() {
        return this.allowedVersionsField;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyKind getPropertyKind() {
        return PropertyKind.OBJECT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyRepresentation getRepresentation() {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPropertyType() {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isParsable(int dtdlVersion) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasCountRestriction(int dtdlVersion) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMembers(List<Integer> dtdlVersions, JavaClass obverseClass, boolean classIsAugmentable) {
        super.addMembers(dtdlVersions, obverseClass, classIsAugmentable);

        if (this.getPropertyDigest().getPropertyVersions().entrySet().stream().anyMatch(e -> e.getValue().isAllowed()) && classIsAugmentable) {
            obverseClass.field(Access.PRIVATE, "List<ValueConstraints>", this.valueConstraintsField, "null", Multiplicity.INSTANCE, Mutability.MUTABLE, null);
            obverseClass.field(Access.PRIVATE, "List<String>", this.instancePropertiesField, "null", Multiplicity.INSTANCE, Mutability.MUTABLE, null);
        }

        for (int dtdlVersion : dtdlVersions) {
            PropertyVersionDigest versionDigest = this.getPropertyDigest().getPropertyVersions().get(dtdlVersion);

            if (versionDigest != null) {
                List<String> classVersions = versionDigest.getClassVersions().stream().map(Object::toString).collect(Collectors.toList());

                obverseClass.field(
                    Access.PRIVATE,
                    "Set<Integer>",
                    this.allowedVersionsField.concat("V").concat(String.valueOf(dtdlVersion)),
                    "new HashSet<>(Arrays.asList(".concat(String.join(", ", classVersions)).concat("))"),
                    Multiplicity.INSTANCE,
                    Mutability.MUTABLE,
                    null);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateConstructorCode(JavaSorted sorted) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEqualsLine(JavaSorted sorted) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addHashLine(JavaSorted sorted) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaScope iterate(JavaScope outerScope, String varName) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaScope checkPresence(JavaScope outerScope) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initMissingPropertyVariable(int dtdlVersion, JavaScope scope) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCaseToParseSwitch(int dtdlVersion, JavaSwitch switchOnProperty, boolean classIsAugmentable, boolean classIsPartition, String valueCountVar, String definedInVar) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCheckForRequiredProperty(int dtdlVersion, JavaScope scope) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addValueToObject(int dtdlVersion, JavaScope scope, String objectVar, String outlineByPartitionVar, String outlineIfIdentifiedVar, String contextIdsVar) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addObjectPropertiesToArray(JavaScope scope, String arrayVariable, String referenceVariable) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLiteralPropertiesToArray(JavaScope scope, String arrayVariable) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCaseToTrySetObjectPropertySwitch(JavaSwitch switchOnProperty, String valueVar, String keyVar) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }
}
