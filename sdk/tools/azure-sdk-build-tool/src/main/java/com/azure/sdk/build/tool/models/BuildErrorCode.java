// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.build.tool.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Enumeration of all build error codes.
 */
public class BuildErrorCode extends ExpandableStringEnum<BuildErrorCode> {

    public static final BuildErrorCode BOM_NOT_USED = fromString("BomNotUsed");
    public static final BuildErrorCode BOM_VERSION_OVERRIDDEN = fromString("BomVersionOverridden");
    public static final BuildErrorCode BETA_API_USED = fromString("BetaApiUsed");
    public static final BuildErrorCode OUTDATED_DEPENDENCY = fromString("OutdatedDependency");
    public static final BuildErrorCode BETA_DEPENDENCY_USED = fromString("BetaDependencyUsed");
    public static final BuildErrorCode DEPRECATED_DEPENDENCY_USED = fromString("DeprecatedDependencyUsed");
    public static final BuildErrorCode DEPRECATED_TRANSITIVE_DEPENDENCY = fromString("DeprecatedTransitiveDependency");

    /**
     * Creates or finds a {@link BuildErrorCode} from its string representation.
     *
     * @param name the name of the error code.
     * @return the {@link BuildErrorCode} associated with the name.
     */
    public static BuildErrorCode fromString(String name) {
        return fromString(name, BuildErrorCode.class);
    }

    /**
     * Returns all the known list of {@link BuildErrorCode}s.
     * @return known build error codes.
     */
    public static Collection<BuildErrorCode> values() {
        return values(BuildErrorCode.class);
    }
}
