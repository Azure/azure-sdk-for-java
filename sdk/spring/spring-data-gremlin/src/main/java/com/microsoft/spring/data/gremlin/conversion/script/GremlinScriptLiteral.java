// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.conversion.script;

import com.microsoft.spring.data.gremlin.conversion.source.GremlinSource;

import java.util.List;

/**
 * Provider interface to generate different query to gremlin server.
 * The scripts return queries in steps, organized by List.
 */
public interface GremlinScriptLiteral {
    /**
     * Generate the insert query from source (Vertex, Edge or Graph).
     */
    <T> List<String> generateInsertScript(GremlinSource<T> source);

    /**
     * Generate the deleteAll query from source (Vertex, Edge or Graph).
     */
    List<String> generateDeleteAllScript();

    /**
     * Generate the deleteAll By Domain Class query from source (Vertex, Edge or Graph).
     */
    <T> List<String> generateDeleteAllByClassScript(GremlinSource<T> source);

    /**
     * Generate the findById query from source (Vertex, Edge).
     */
    <T> List<String> generateFindByIdScript(GremlinSource<T> source);

    /**
     * Generate the update query from source (Vertex, Edge or Graph).
     */
    <T> List<String> generateUpdateScript(GremlinSource<T> source);

    /**
     * Generate the findAll query from source (Vertex, Edge or Graph).
     */
    <T> List<String> generateFindAllScript(GremlinSource<T> source);

    /**
     * Generate the DeleteById query from source (Vertex, Edge or Graph).
     */
    <T> List<String> generateDeleteByIdScript(GremlinSource<T> source);

    /**
     * Generate the Count query from Source (Vertex, Edge)
     */
    <T> List<String> generateCountScript(GremlinSource<T> source);
}
