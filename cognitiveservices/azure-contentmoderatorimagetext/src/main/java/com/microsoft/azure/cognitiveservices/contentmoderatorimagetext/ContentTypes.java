/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for ContentTypes.
 */
public final class ContentTypes extends ExpandableStringEnum<ContentTypes> {
    /** Static value text/plain for ContentTypes. */
    public static final ContentTypes TEXTPLAIN = fromString("text/plain");

    /** Static value text/html for ContentTypes. */
    public static final ContentTypes TEXTHTML = fromString("text/html");

    /** Static value text/xml for ContentTypes. */
    public static final ContentTypes TEXTXML = fromString("text/xml");

    /** Static value text/markdown for ContentTypes. */
    public static final ContentTypes TEXTMARKDOWN = fromString("text/markdown");

    /**
     * Creates or finds a ContentTypes from its string representation.
     * @param name a name to look for
     * @return the corresponding ContentTypes
     */
    @JsonCreator
    public static ContentTypes fromString(String name) {
        return fromString(name, ContentTypes.class);
    }

    /**
     * @return known ContentTypes values
     */
    public static Collection<ContentTypes> values() {
        return values(ContentTypes.class);
    }
}
