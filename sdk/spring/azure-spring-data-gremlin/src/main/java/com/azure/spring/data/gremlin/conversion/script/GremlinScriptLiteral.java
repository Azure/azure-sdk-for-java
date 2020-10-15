// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.script;

import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import java.util.List;

/**
 * Provider interface to generate different query to gremlin server. The scripts return queries in steps, organized by
 * List.
 */
public interface GremlinScriptLiteral {

    /**
     * Generate the insert query from source (Vertex, Edge or Graph).
     *
     * @param <T> The type of domain.
     * @param source the gremlin source
     * @return insert script
     */
    <T> List<String> generateInsertScript(GremlinSource<T> source);

    /**
     * Generate the deleteAll query from source (Vertex, Edge or Graph).
     *
     * @return deleteAll script
     */
    List<String> generateDeleteAllScript();

    /**
     * Generate the deleteAll By Domain Class query from source (Vertex, Edge or Graph).
     *
     * @param <T> The type of domain.
     * @param source the gremlin source
     * @return deleteAllByClass script
     */
    <T> List<String> generateDeleteAllByClassScript(GremlinSource<T> source);

    /**
     * Generate the findById query from source (Vertex, Edge).
     *
     * @param <T> The type of domain.
     * @param source the gremlin source
     * @return findById script script
     */
    <T> List<String> generateFindByIdScript(GremlinSource<T> source);

    /**
     * Generate the update query from source (Vertex, Edge or Graph).
     *
     * @param <T> The type of domain.
     * @param source the gremlin source
     * @return update script
     */
    <T> List<String> generateUpdateScript(GremlinSource<T> source);

    /**
     * Generate the findAll query from source (Vertex, Edge or Graph).
     *
     * @param <T> The type of domain.
     * @param source the gremlin source
     * @return findAll script
     */
    <T> List<String> generateFindAllScript(GremlinSource<T> source);

    /**
     * Generate the DeleteById query from source (Vertex, Edge or Graph).
     *
     * @param <T> The type of domain.
     * @param source the gremlin source
     * @return deleteById script
     */
    <T> List<String> generateDeleteByIdScript(GremlinSource<T> source);

    /**
     * Generate the Count query from Source (Vertex, Edge)
     *
     * @param <T> The type of domain.
     * @param source the gremlin source
     * @return count query
     */
    <T> List<String> generateCountScript(GremlinSource<T> source);
}
