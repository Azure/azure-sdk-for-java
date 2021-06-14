// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * OneDeploy type.
 */
public class DeployType extends ExpandableStringEnum<DeployType> {

    /**
     * Deploy the war file to {@code /home/site/wwwroot/app.war}.
     *
     * If {@code DeployOptions.path} is provided, {@code path=webapps/<appname>} will behave exactly like wardeploy by
     * unzipping app to {@code /home/site/wwwroot/webapps/<appname>}.
     */
    public static final DeployType WAR = fromString("war");

    /**
     * Deploy the jar file to {@code /home/site/wwwroot/app.jar}.
     */
    public static final DeployType JAR = fromString("jar");

    /**
     * Deploy the ear file to {@code /home/site/wwwroot/app.ear}.
     */
    public static final DeployType EAR = fromString("ear");

    /**
     * Deploy the jar to {@code /home/site/libs}. {@code DeployOptions.path} parameter needs to be specified.
     */
    public static final DeployType JAR_LIB = fromString("lib");

    /**
     * Deploy the static file to {@code /home/site/wwwroot/}.
     * {@code DeployOptions.path} parameter needs to be specified.
     */
    public static final DeployType STATIC = fromString("static");

    /**
     * Deploy the script file to {@code /home/site/scripts/}.
     * {@code DeployOptions.path} parameter needs to be specified.
     */
    public static final DeployType SCRIPT = fromString("script");

    /**
     * Deploy the script as startup.sh (Linux) or startup.cmd (Windows) to {@code /home/site/scripts/}.
     * {@code DeployOptions.path} parameter is not supported.
     */
    public static final DeployType SCRIPT_STARTUP = fromString("startup");

    /**
     * unzip the zip to {@code /home/site/wwwroot}. {@code DeployOptions.path} parameter is optional.
     */
    public static final DeployType ZIP = fromString("zip");

    /**
     * Creates or finds a DeployType from its string representation.
     * @param name a name to look for
     * @return the corresponding DeployType
     */
    @JsonCreator
    public static DeployType fromString(String name) {
        return fromString(name, DeployType.class);
    }

    /**
     * @return known DeployType type values
     */
    public static Collection<DeployType> values() {
        return values(DeployType.class);
    }
}
