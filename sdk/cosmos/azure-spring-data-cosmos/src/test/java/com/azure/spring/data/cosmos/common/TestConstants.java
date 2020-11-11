// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import com.azure.cosmos.models.IndexingMode;
import com.azure.spring.data.cosmos.domain.Address;

import java.util.Arrays;
import java.util.List;

public final class TestConstants {

    private static final Address ADDRESS_1 = new Address("201107", "Zixing Road", "Shanghai");
    private static final Address ADDRESS_2 = new Address("200000", "Xuhui", "Shanghai");
    public static final List<String> HOBBIES = Arrays.asList("photography", "fishing");
    public static final List<String> UPDATED_HOBBIES = Arrays.asList("updatedPhotography", "updatedFishing");
    public static final List<Address> ADDRESSES = Arrays.asList(ADDRESS_1, ADDRESS_2);

    public static final int DEFAULT_TIME_TO_LIVE = -1;
    public static final boolean DEFAULT_INDEXING_POLICY_AUTOMATIC = true;
    public static final IndexingMode DEFAULT_INDEXING_POLICY_MODE = IndexingMode.CONSISTENT;

    public static final String ROLE_COLLECTION_NAME = "RoleCollectionName";
    public static final int TIME_TO_LIVE = 5;
    public static final boolean INDEXING_POLICY_AUTOMATIC = false;
    public static final IndexingMode INDEXING_POLICY_MODE = IndexingMode.CONSISTENT;

    public static final String DB_NAME = "testdb";
    public static final String FIRST_NAME = "first_name_li";
    public static final String LAST_NAME = "last_name_p";
    public static final String ID_1 = "id-1";
    public static final String UPDATED_FIRST_NAME = "updated_first_name";
    public static final String UPDATED_LAST_NAME = "updated_last_name";

    public static final String DATE_STRING = "8/8/2017";
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATE_TIMEZONE_STRING = "1/1/2000 00:00 GMT";
    public static final String DATE_TIMEZONE_FORMAT = "dd/MM/yyyy HH:mm ZZZ";
    public static final long MILLI_SECONDS = 946684800000L;

    public static final String POSTAL_CODE = "98052";
    public static final String CITY = "testCity";
    public static final String UPDATED_CITY = "updatedCityOne";
    public static final String STREET = "testStreet";
    public static final String UPDATED_STREET = "updatedTestStreet";
    public static final String MESSAGE = "test pojo with date";

    public static final String CRITERIA_KEY = "CriteriaTestKey";
    public static final String CRITERIA_OBJECT = "CriteriaTestObject";

    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_FIRST_NAME = "firstName";
    public static final String PROPERTY_LAST_NAME = "lastName";

    public static final String PROPERTY_CITY = "city";
    public static final String PROPERTY_STREET = "street";

    public static final String PROPERTY_MESSAGE = "message";
    public static final String PROPERTY_DATE = "date";

    public static final String PROPERTY_ETAG_DEFAULT = "_etag";
    public static final String PROPERTY_ETAG_RENAMED = "etag";

    public static final String DYNAMIC_PROPERTY_COLLECTION_NAME = "spel-property-collection";

    private TestConstants() {
    }
}

