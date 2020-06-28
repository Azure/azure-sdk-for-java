// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.gremlin.common;

public interface Constants {

    String PROPERTY_ID = "id";
    String PROPERTY_LABEL = "label";
    String PROPERTY_TYPE = "type";
    String PROPERTY_VALUE = "value";
    String PROPERTY_PROPERTIES = "properties";
    String PROPERTY_INV = "inV";
    String PROPERTY_OUTV = "outV";

    String RESULT_TYPE_VERTEX = "vertex";
    String RESULT_TYPE_EDGE = "edge";

    String DEFAULT_VERTEX_LABEL = "";
    String DEFAULT_EDGE_LABEL = "";
    String DEFAULT_COLLECTION_NAME = "";
    int DEFAULT_ENDPOINT_PORT = 443;
    String DEFAULT_REPOSITORY_IMPLEMENT_POSTFIX = "Impl";

    String GREMLIN_MODULE_NAME = "Gremlin";
    String GREMLIN_MODULE_PREFIX = "gremlin";
    String GREMLIN_MAPPING_CONTEXT = "gremlinMappingContext";

    String GREMLIN_PRIMITIVE_GRAPH = "g";
    String GREMLIN_PRIMITIVE_INVOKE = ".";
    String GREMLIN_PRIMITIVE_DROP = "drop()";

    String GREMLIN_PRIMITIVE_EDGE_ALL = "E()";

    String GREMLIN_PRIMITIVE_VERTEX_ALL = "V()";

    String GREMLIN_PRIMITIVE_HAS_STRING = "has('%s', '%s')";
    String GREMLIN_PRIMITIVE_HAS_NUMBER = "has('%s', %d)";
    String GREMLIN_PRIMITIVE_HAS_BOOLEAN = "has('%s', %b)";

    String GREMLIN_PRIMITIVE_PROPERTY_STRING = "property('%s', '%s')";
    String GREMLIN_PRIMITIVE_PROPERTY_NUMBER = "property('%s', %d)";
    String GREMLIN_PRIMITIVE_PROPERTY_BOOLEAN = "property('%s', %b)";

    String GREMLIN_PRIMITIVE_AND = "and()";
    String GREMLIN_PRIMITIVE_OR = "or()";
    String GREMLIN_PRIMITIVE_WHERE = "where(%s)";

    String GREMLIN_QUERY_BARRIER = "barrier";

    String GREMLIN_PRIMITIVE_VALUES = "values('%s')";
    String GREMLIN_PRIMITIVE_IS = "is(%s)";
    String GREMLIN_PRIMITIVE_GT = "gt(%d)";
    String GREMLIN_PRIMITIVE_LT = "lt(%d)";
    String GREMLIN_PRIMITIVE_BETWEEN = "between(%d, %d)";

    String GREMLIN_PRIMITIVE_IS_GT = String.format(GREMLIN_PRIMITIVE_IS, GREMLIN_PRIMITIVE_GT);
    String GREMLIN_PRIMITIVE_IS_LT = String.format(GREMLIN_PRIMITIVE_IS, GREMLIN_PRIMITIVE_LT);
    String GREMLIN_PRIMITIVE_IS_BETWEEN = String.format(
            GREMLIN_PRIMITIVE_IS,
            GREMLIN_PRIMITIVE_BETWEEN
    );

    String GREMLIN_SCRIPT_EDGE_ALL = String.join(GREMLIN_PRIMITIVE_INVOKE,
            GREMLIN_PRIMITIVE_GRAPH,
            GREMLIN_PRIMITIVE_EDGE_ALL
    );

    String GREMLIN_SCRIPT_VERTEX_ALL = String.join(GREMLIN_PRIMITIVE_INVOKE,
            GREMLIN_PRIMITIVE_GRAPH,
            GREMLIN_PRIMITIVE_VERTEX_ALL
    );

    String GREMLIN_SCRIPT_EDGE_DROP_ALL = String.join(GREMLIN_PRIMITIVE_INVOKE,
            GREMLIN_PRIMITIVE_GRAPH,
            GREMLIN_PRIMITIVE_EDGE_ALL,
            GREMLIN_PRIMITIVE_DROP
    );

    String GREMLIN_SCRIPT_VERTEX_DROP_ALL = String.join(GREMLIN_PRIMITIVE_INVOKE,
            GREMLIN_PRIMITIVE_GRAPH,
            GREMLIN_PRIMITIVE_VERTEX_ALL,
            GREMLIN_PRIMITIVE_DROP
    );

    String GREMLIN_PROPERTY_CLASSNAME = "_classname";

    int DEFAULT_MAX_CONTENT_LENGTH = 65536;

}
