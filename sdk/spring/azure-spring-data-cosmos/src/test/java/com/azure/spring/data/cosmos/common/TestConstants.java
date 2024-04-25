// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import com.azure.cosmos.models.IndexingMode;
import com.azure.spring.data.cosmos.domain.Address;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TestConstants {

    public static final String AUTOSCALE_MAX_THROUGHPUT = "4000";

    public static final String MULTI_PARTITION_THROUGHPUT = "12000";
    private static final Address ADDRESS_1 = new Address("201107", "Zixing Road", "Shanghai");
    private static final Address ADDRESS_2 = new Address("200000", "Xuhui", "Shanghai");
    public static final String HOBBY1 = "photography";
    public static final String PATCH_HOBBY1 = "shopping";
    public static final List<String> HOBBIES = Arrays.asList(HOBBY1, "fishing");
    public static final List<String> PATCH_HOBBIES = Arrays.asList(HOBBY1, "fishing", PATCH_HOBBY1);
    public static final List<String> UPDATED_HOBBIES = Arrays.asList("updatedPhotography", "updatedFishing");
    public static final List<Address> ADDRESSES = Arrays.asList(ADDRESS_1, ADDRESS_2);

    public static final int DEFAULT_TIME_TO_LIVE = -1;
    public static final String DEFAULT_MINIMUM_RU = "400";
    public static final boolean DEFAULT_INDEXING_POLICY_AUTOMATIC = true;
    public static final IndexingMode DEFAULT_INDEXING_POLICY_MODE = IndexingMode.CONSISTENT;

    public static final String ROLE_COLLECTION_NAME = "RoleCollectionName";
    public static final String BOOK_COLLECTION_NAME = "BookCollectionName";
    public static final int TIME_TO_LIVE = 5;
    public static final boolean INDEXING_POLICY_AUTOMATIC = false;
    public static final IndexingMode INDEXING_POLICY_MODE = IndexingMode.CONSISTENT;
    public static final String INCLUDED_PATH_0 = "/*";
    public static final String INCLUDED_PATH_1 = "/cache/*";
    public static final String INCLUDED_PATH_2 = "/entities/*";
    public static final String[] INCLUDED_PATHS = {
        INCLUDED_PATH_0,
        INCLUDED_PATH_1,
        INCLUDED_PATH_2,
    };
    public static final String EXCLUDED_PATH_0 = "/excluded/*";
    public static final String EXCLUDED_PATH_1 = "/props/*";
    public static final String EXCLUDED_PATH_2 = "/_etag/?";
    public static final String[] EXCLUDED_PATHS = {
        EXCLUDED_PATH_0,
        EXCLUDED_PATH_1,
        EXCLUDED_PATH_2,
    };

    public static final String DEFAULT_UNIQUE_KEY_NAME = "/name";
    public static final String DEFAULT_UNIQUE_KEY_LEVEL = "/level";

    public static final String DB_NAME = "testdb";
    public static final String FIRST_NAME = "first_name_li";
    public static final String PATCH_FIRST_NAME = "first_name_replace";
    public static final String LAST_NAME = "last_name_p";
    public static final Integer ZIP_CODE = 12345;
    public static final String ID_1 = "id-1";
    public static final String ID_2 = "id-2";
    public static final String ID_3 = "id-3";
    public static final String ID_4 = "id-4";
    public static final String ID_5 = "id-5";

    public static final String ID_6 = "id-6";
    public static final String NEW_FIRST_NAME = "new_first_name";
    public static final String NEW_LAST_NAME = "new_last_name";

    public static final String TRANSIENT_PROPERTY = "transient_property";
    public static final Integer NEW_ZIP_CODE = 67890;
    public static final String UPDATED_FIRST_NAME = "updated_first_name";
    public static final String UPDATED_LAST_NAME = "updated_last_name";
    public static final String LEVEL = "B";
    public static final String LEVEL_2 = "C";
    public static final String ROLE_NAME = "Developer";
    public static final String ROLE_NAME_2 = "Engineer";
    public static final String NOT_EXIST_ID = "non_exist_id";

    public static final String DATE_STRING = "8/8/2017";
    public static final String DATE_BEFORE_STRING = "8/1/2017";
    public static final String DATE_AFTER_STRING = "8/13/2017";
    public static final String DATE_FUTURE_STRING_1 = "9/13/2017";
    public static final String DATE_FUTURE_STRING_2 = "9/14/2017";
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATE_TIMEZONE_STRING = "1/1/2000 00:00 GMT";
    public static final String DATE_TIMEZONE_FORMAT = "dd/MM/yyyy HH:mm ZZZ";
    public static final long MILLI_SECONDS = 946684800000L;

    public static final String POSTAL_CODE = "98052";
    public static final String POSTAL_CODE_0 = "00000";
    public static final String POSTAL_CODE_1 = "11111";
    public static final String POSTAL_CODE_3 = "10112";
    public static final String CITY = "testCity";
    public static final String CITY_0 = "testCityZero";
    public static final String CITY_1 = "testCityOne";
    public static final String UPDATED_CITY = "updatedCityOne";
    public static final String STREET = "testStreet";
    public static final String STREET_0 = "testStreetZero";
    public static final String STREET_1 = "testStreetOne";
    public static final String STREET_2 = "testStreetTwo";
    public static final String NEW_STREET = "newTestStreet";
    public static final String UPDATED_STREET = "updatedTestStreet";
    public static final String MESSAGE = "test pojo with date";
    public static final String NEW_MESSAGE = "new test message";

    public static final String CRITERIA_KEY = "CriteriaTestKey";
    public static final String CRITERIA_OBJECT = "CriteriaTestObject";

    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_ZIP_CODE = "zipCode";
    public static final String PROPERTY_FIRST_NAME = "firstName";
    public static final String PROPERTY_LAST_NAME = "lastName";

    public static final String PROPERTY_CITY = "city";
    public static final String PROPERTY_STREET = "street";

    public static final int PAGE_SIZE_1 = 1;
    public static final int PAGE_SIZE_2 = 2;
    public static final int PAGE_SIZE_3 = 3;

    public static final String PROPERTY_MESSAGE = "message";
    public static final String PROPERTY_DATE = "date";

    public static final String PROPERTY_ETAG_DEFAULT = "_etag";
    public static final String PROPERTY_ETAG_RENAMED = "etag";

    public static final String DYNAMIC_PROPERTY_COLLECTION_NAME = "spel-property-collection";
    public static final String DYNAMIC_BEAN_COLLECTION_NAME = "spel-bean-collection";

    public static final String COURSE_NAME = "test-course";
    public static final String DEPARTMENT = "test-department";

    public static final Integer AGE = 24;
    public static final Integer PATCH_AGE_1 = 25;
    public static final Integer PATCH_AGE_INCREMENT = 2;

    //  Some constants from Cosmos HttpConstants
    public static final int PRECONDITION_FAILED_STATUS_CODE = 412;
    public static final int CONFLICT_STATUS_CODE = 409;

    public static final Long LONG_ID_1 = Long.valueOf(10000001);
    public static final Long LONG_ID_2 = Long.valueOf(20000002);
    public static final Long LONG_ID_3 = Long.valueOf(54600003);

    public static final Integer HOME_NUMBER_1 = 1;
    public static final Integer HOME_NUMBER_2 = 99;
    public static final Integer HOME_NUMBER_3 = 4445;

    public static final LocalDate REGISTRATION_TIME_1D_AGO = LocalDate.now().minusDays(1);
    public static final LocalDate REGISTRATION_TIME_1W_AGO = LocalDate.now().minusDays(7);
    public static final LocalDate REGISTRATION_TIME_1M_AGO = LocalDate.now().minusMonths(1);

    public static final Map<String, String> PASSPORT_IDS_BY_COUNTRY = new HashMap<String, String>() {{
        put("United States of America", "123456789");
        put("Côte d'Ivoire", "IC1234567");
    }};

    public static final Map<String, String> NEW_PASSPORT_IDS_BY_COUNTRY = new HashMap<String, String>() {{
        put("United Kingdom", "123456789");
        put("Germany", "IC1234567");
    }};

    private TestConstants() {
    }
}

