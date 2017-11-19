/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.entitysearch;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for EntityType.
 */
public final class EntityType extends ExpandableStringEnum<EntityType> {
    /** Static value Generic for EntityType. */
    public static final EntityType GENERIC = fromString("Generic");

    /** Static value Person for EntityType. */
    public static final EntityType PERSON = fromString("Person");

    /** Static value Place for EntityType. */
    public static final EntityType PLACE = fromString("Place");

    /** Static value Media for EntityType. */
    public static final EntityType MEDIA = fromString("Media");

    /** Static value Organization for EntityType. */
    public static final EntityType ORGANIZATION = fromString("Organization");

    /** Static value LocalBusiness for EntityType. */
    public static final EntityType LOCAL_BUSINESS = fromString("LocalBusiness");

    /** Static value Restaurant for EntityType. */
    public static final EntityType RESTAURANT = fromString("Restaurant");

    /** Static value Hotel for EntityType. */
    public static final EntityType HOTEL = fromString("Hotel");

    /** Static value TouristAttraction for EntityType. */
    public static final EntityType TOURIST_ATTRACTION = fromString("TouristAttraction");

    /** Static value Travel for EntityType. */
    public static final EntityType TRAVEL = fromString("Travel");

    /** Static value City for EntityType. */
    public static final EntityType CITY = fromString("City");

    /** Static value Country for EntityType. */
    public static final EntityType COUNTRY = fromString("Country");

    /** Static value Attraction for EntityType. */
    public static final EntityType ATTRACTION = fromString("Attraction");

    /** Static value House for EntityType. */
    public static final EntityType HOUSE = fromString("House");

    /** Static value State for EntityType. */
    public static final EntityType STATE = fromString("State");

    /** Static value RadioStation for EntityType. */
    public static final EntityType RADIO_STATION = fromString("RadioStation");

    /** Static value StreetAddress for EntityType. */
    public static final EntityType STREET_ADDRESS = fromString("StreetAddress");

    /** Static value Neighborhood for EntityType. */
    public static final EntityType NEIGHBORHOOD = fromString("Neighborhood");

    /** Static value Locality for EntityType. */
    public static final EntityType LOCALITY = fromString("Locality");

    /** Static value PostalCode for EntityType. */
    public static final EntityType POSTAL_CODE = fromString("PostalCode");

    /** Static value Region for EntityType. */
    public static final EntityType REGION = fromString("Region");

    /** Static value SubRegion for EntityType. */
    public static final EntityType SUB_REGION = fromString("SubRegion");

    /** Static value MinorRegion for EntityType. */
    public static final EntityType MINOR_REGION = fromString("MinorRegion");

    /** Static value Continent for EntityType. */
    public static final EntityType CONTINENT = fromString("Continent");

    /** Static value PointOfInterest for EntityType. */
    public static final EntityType POINT_OF_INTEREST = fromString("PointOfInterest");

    /** Static value Other for EntityType. */
    public static final EntityType OTHER = fromString("Other");

    /** Static value Movie for EntityType. */
    public static final EntityType MOVIE = fromString("Movie");

    /** Static value Book for EntityType. */
    public static final EntityType BOOK = fromString("Book");

    /** Static value TelevisionShow for EntityType. */
    public static final EntityType TELEVISION_SHOW = fromString("TelevisionShow");

    /** Static value TelevisionSeason for EntityType. */
    public static final EntityType TELEVISION_SEASON = fromString("TelevisionSeason");

    /** Static value VideoGame for EntityType. */
    public static final EntityType VIDEO_GAME = fromString("VideoGame");

    /** Static value MusicAlbum for EntityType. */
    public static final EntityType MUSIC_ALBUM = fromString("MusicAlbum");

    /** Static value MusicRecording for EntityType. */
    public static final EntityType MUSIC_RECORDING = fromString("MusicRecording");

    /** Static value MusicGroup for EntityType. */
    public static final EntityType MUSIC_GROUP = fromString("MusicGroup");

    /** Static value Composition for EntityType. */
    public static final EntityType COMPOSITION = fromString("Composition");

    /** Static value TheaterPlay for EntityType. */
    public static final EntityType THEATER_PLAY = fromString("TheaterPlay");

    /** Static value Event for EntityType. */
    public static final EntityType EVENT = fromString("Event");

    /** Static value Actor for EntityType. */
    public static final EntityType ACTOR = fromString("Actor");

    /** Static value Artist for EntityType. */
    public static final EntityType ARTIST = fromString("Artist");

    /** Static value Attorney for EntityType. */
    public static final EntityType ATTORNEY = fromString("Attorney");

    /** Static value Speciality for EntityType. */
    public static final EntityType SPECIALITY = fromString("Speciality");

    /** Static value CollegeOrUniversity for EntityType. */
    public static final EntityType COLLEGE_OR_UNIVERSITY = fromString("CollegeOrUniversity");

    /** Static value School for EntityType. */
    public static final EntityType SCHOOL = fromString("School");

    /** Static value Food for EntityType. */
    public static final EntityType FOOD = fromString("Food");

    /** Static value Drug for EntityType. */
    public static final EntityType DRUG = fromString("Drug");

    /** Static value Animal for EntityType. */
    public static final EntityType ANIMAL = fromString("Animal");

    /** Static value SportsTeam for EntityType. */
    public static final EntityType SPORTS_TEAM = fromString("SportsTeam");

    /** Static value Product for EntityType. */
    public static final EntityType PRODUCT = fromString("Product");

    /** Static value Car for EntityType. */
    public static final EntityType CAR = fromString("Car");

    /**
     * Creates or finds a EntityType from its string representation.
     * @param name a name to look for
     * @return the corresponding EntityType
     */
    @JsonCreator
    public static EntityType fromString(String name) {
        return fromString(name, EntityType.class);
    }

    /**
     * @return known EntityType values
     */
    public static Collection<EntityType> values() {
        return values(EntityType.class);
    }
}
