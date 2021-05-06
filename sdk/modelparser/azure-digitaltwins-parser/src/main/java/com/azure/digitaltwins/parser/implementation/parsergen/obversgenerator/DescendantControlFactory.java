// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.parsergen.DescendantControlDigest;

import java.util.ArrayList;
import java.util.List;

/**
 * A factory for generating objects that implement the {@link DescendantControl} interface.
 */
public class DescendantControlFactory {
    private final ClientLogger logger = new ClientLogger(DescendantControlFactory.class);

    private final String kindEnum;
    private final String kindProperty;

    /**
     * Initializes a new instance of the {@link DescendantControlFactory} class.
     *
     * @param kindEnum The enum type used to represent DTDL element kind.
     * @param kindProperty The property on the DTDL base obverse class that indicates the kind of DTDL element.
     */
    public DescendantControlFactory(String kindEnum, String kindProperty) {
        this.kindEnum = kindEnum;
        this.kindProperty = kindProperty;

        // TODO: remove once class is fully implemented.
        logger.info(String.format("%s", this.kindProperty));
        logger.info(String.format("%s", this.kindEnum));
    }

    /**
     * Create objects that exports the {@link DescendantControl} interface.
     *
     * @param descendantControlDigest A {@link DescendantControlDigest} object that describes a descednant control defined in the meta-model digest.
     * @return List of the objects created.
     */
    public List<DescendantControl> create(DescendantControlDigest descendantControlDigest) {
        List<DescendantControl> descendantControls = new ArrayList<>();

        // TODO: implement factory method.
        return descendantControls;
    }
}
