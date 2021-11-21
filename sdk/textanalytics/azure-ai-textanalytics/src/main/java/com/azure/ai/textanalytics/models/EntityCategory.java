// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Gets the entity category inferred by the text analytics service's named entity recognition model.
 * The list of available categories is described at
 * See <a href="https://docs.microsoft.com/azure/cognitive-services/Text-Analytics/named-entity-types">named entity types</a>.
 */
@Immutable
public final class EntityCategory extends ExpandableStringEnum<EntityCategory> {
    /**
     * Specifies that the entity corresponds to an address.
     */
    public static final EntityCategory ADDRESS = fromString("Address");

    /**
     * Specifies that the entity corresponds to a person.
     */
    public static final EntityCategory PERSON = fromString("Person");

    /**
     * Specifies that the entity corresponds to a person type.
     */
    public static final EntityCategory PERSON_TYPE = fromString("PersonType");

    /**
     * Specifies that entity contains natural or human-made landmarks, structures, or geographical features.
     */
    public static final EntityCategory LOCATION = fromString("Location");

    /**
     * Specifies that the entity contains the name of an organization, corporation, agency, or other group of people.
     */
    public static final EntityCategory ORGANIZATION = fromString("Organization");

    /**
     * Specifies that the entity contains historical, social and natural-occuring events.
     */
    public static final EntityCategory EVENT = fromString("Event");

    /**
     * Specifies that the entity contains a physical objects of various categories.
     */
    public static final EntityCategory PRODUCT = fromString("Product");

    /**
     * Specifies that the entity contains an entity describing a capability or expertise.
     */
    public static final EntityCategory SKILL = fromString("Skill");

    /**
     * Specifies that the entity contains a phone number (US phone numbers only).
     */
    public static final EntityCategory PHONE_NUMBER = fromString("PhoneNumber");

    /**
     * Specifies that the entity contains an email address.
     */
    public static final EntityCategory EMAIL = fromString("Email");

    /**
     * Specifies that the entity contains an Internet URL.
     */
    public static final EntityCategory URL = fromString("URL");

    /**
     * Specifies that the entity contains an Internet Protocol address
     */
    public static final EntityCategory IP_ADDRESS = fromString("IP Address");

    /**
     * Specifies that the entity contains a date, time or duration.
     */
    public static final EntityCategory DATE_TIME = fromString("DateTime");

    /**
     * Specifies that the entity contains a number or numeric quantity.
     */
    public static final EntityCategory QUANTITY = fromString("Quantity");

    /**
     * Creates or finds a {@link EntityCategory} from its string representation.
     *
     * @param name The string name to look for.
     * @return The corresponding {@link EntityCategory}.
     */
    @JsonCreator
    public static EntityCategory fromString(String name) {
        return fromString(name, EntityCategory.class);
    }
}
