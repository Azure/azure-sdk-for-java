// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common;

public final class TestConstants {

    private TestConstants() {

    }

    public static final int DEFAULT_ENDPOINT_PORT = 443;
    public static final int ILLEGAL_ENDPOINT_PORT = -1;
    public static final String FAKE_ENDPOINT = "XXX-xxx.XXX-xxx.cosmosdb.azure.com";
    public static final String FAKE_USERNAME = "XXX-xxx.username";
    public static final String FAKE_PASSWORD = "XXX-xxx.password";
    public static final String EMPTY_STRING = "";

    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_LOCATION = "location";

    public static final String VERTEX_PERSON_LABEL = "label-person";
    public static final String VERTEX_PROJECT_LABEL = "label-project";
    public static final String EDGE_RELATIONSHIP_LABEL = "label-relationship";
    public static final String GRAPH_ROADMAP_COLLECTION_NAME = "roadmap-collection";

    public static final String VERTEX_PERSON_ID = "233333";
    public static final String VERTEX_PERSON_NAME = "incarnation-p-lee";

    public static final String VERTEX_PERSON_0_ID = "000000";
    public static final String VERTEX_PERSON_0_NAME = "silencer";

    public static final String VERTEX_PERSON_1_ID = "111111";
    public static final String VERTEX_PERSON_1_NAME = "templar-assassin";

    public static final String VERTEX_PROJECT_ID = "666666";
    public static final String VERTEX_PROJECT_NAME = "spring-data-gremlin";
    public static final String VERTEX_PROJECT_URI = "https://github.com/Incarnation-p-lee/spring-data-gremlin.git";

    public static final String VERTEX_PROJECT_0_ID = "222222";
    public static final String VERTEX_PROJECT_0_NAME = "spring-data-documentdb";
    public static final String VERTEX_PROJECT_0_URI = "https://github.com/Microsoft/spring-data-documentdb";

    public static final String EDGE_RELATIONSHIP_ID = "999999";
    public static final String EDGE_RELATIONSHIP_NAME = "created";
    public static final String EDGE_RELATIONSHIP_LOCATION = "shanghai";

    public static final String EDGE_RELATIONSHIP_0_ID = "333333";
    public static final String EDGE_RELATIONSHIP_0_NAME = "contributed";
    public static final String EDGE_RELATIONSHIP_0_LOCATION = "war3";

    public static final String EDGE_RELATIONSHIP_1_ID = "444444";
    public static final String EDGE_RELATIONSHIP_1_NAME = "contributed";
    public static final String EDGE_RELATIONSHIP_1_LOCATION = "dota";

    public static final String EDGE_RELATIONSHIP_2_ID = "555555";
    public static final String EDGE_RELATIONSHIP_2_NAME = "create";
    public static final String EDGE_RELATIONSHIP_2_LOCATION = "shanghai";

    public static final String VERTEX_LABEL = "label-vertex";
}
