// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.resourcemanager.appservice.fluent.models.FunctionEnvelopeInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.util.Map;

/** An immutable representation of function Information. */
public interface FunctionEnvelope extends HasInnerModel<FunctionEnvelopeInner> {

    /**
     * Gets the functionAppId value.
     *
     * @return the functionAppId value
     */
    String functionAppId();

    /**
     * Gets the scriptRootPathHref value.
     *
     * @return the scriptRootPathHref value
     */
    String scriptRootPathHref();

    /**
     * Gets the scriptHref value.
     *
     * @return the scriptHref value
     */
    String scriptHref();

    /**
     * Gets the configHref value.
     *
     * @return the configHref value
     */
    String configHref();

    /**
     * Gets the secretsFileHref value.
     *
     * @return the secretsFileHref value
     */
    String secretsFileHref();

    /**
     * Gets the href value.
     *
     * @return the href value
     */
    String href();

    /**
     * Gets the config value.
     *
     * @return the config value
     */
    Object config();

    /**
     * Gets the files value.
     *
     * @return the files value
     */
    Map<String, String> files();

    /**
     * Gets the testData value.
     *
     * @return the testData value
     */
    String testData();
}
