// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.resourcemanager.appservice.fluent.inner.FunctionEnvelopeInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import java.util.Map;

/** An immutable representation of function Information. */
public interface FunctionEnvelope extends HasInner<FunctionEnvelopeInner> {

    /** @return the functionAppId value */
    String functionAppId();

    /** @return the scriptRootPathHref value */
    String scriptRootPathHref();

    /** @return the scriptHref value */
    String scriptHref();

    /** @return the configHref value */
    String configHref();

    /** @return the secretsFileHref value */
    String secretsFileHref();

    /** @return the href value */
    String href();

    /** @return the config value */
    Object config();

    /** @return the files value */
    Map<String, String> files();

    /** @return the testData value */
    String testData();
}
