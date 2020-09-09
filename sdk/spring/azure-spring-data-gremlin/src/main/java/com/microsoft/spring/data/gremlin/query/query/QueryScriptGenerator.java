// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.query.query;

import java.util.List;

public interface QueryScriptGenerator {

    List<String> generate(GremlinQuery query);
}
