package com.azure.analytics.purview.catalog.implementation;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import java.nio.ByteBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** An instance of this class provides access to all the operations defined in Glossaries. */
public final class GlossariesImpl {
    /** The proxy service used to perform REST calls. */
    private final GlossariesService service;

    /** The service client containing this operation class. */
    private final PurviewCatalogServiceRestAPIDocumentImpl client;

    /**
     * Initializes an instance of GlossariesImpl.
     *
     * @param client the instance of the service client containing this operation class.
     */
    GlossariesImpl(PurviewCatalogServiceRestAPIDocumentImpl client) {
        this.service =
                RestProxy.create(GlossariesService.class, client.getHttpPipeline(), client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for PurviewCatalogServiceRestAPIDocumentGlossaries to be used by the
     * proxy service to perform REST calls.
     */
    @Host("{Endpoint}/api")
    @ServiceInterface(name = "PurviewCatalogServic")
    private interface GlossariesService {
        @Get("/atlas/v2/glossary")
        Mono<Response<BinaryData>> listGlossaries(
                @HostParam("Endpoint") String endpoint,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Post("/atlas/v2/glossary")
        Mono<Response<BinaryData>> createGlossary(
                @HostParam("Endpoint") String endpoint,
                @BodyParam("application/json") BinaryData atlasGlossary,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Post("/atlas/v2/glossary/categories")
        Mono<Response<BinaryData>> createGlossaryCategories(
                @HostParam("Endpoint") String endpoint,
                @BodyParam("application/json") BinaryData glossaryCategory,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Post("/atlas/v2/glossary/category")
        Mono<Response<BinaryData>> createGlossaryCategory(
                @HostParam("Endpoint") String endpoint,
                @BodyParam("application/json") BinaryData glossaryCategory,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/glossary/category/{categoryGuid}")
        Mono<Response<BinaryData>> getGlossaryCategory(
                @HostParam("Endpoint") String endpoint,
                @PathParam("categoryGuid") String categoryGuid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Put("/atlas/v2/glossary/category/{categoryGuid}")
        Mono<Response<BinaryData>> updateGlossaryCategory(
                @HostParam("Endpoint") String endpoint,
                @PathParam("categoryGuid") String categoryGuid,
                @BodyParam("application/json") BinaryData glossaryCategory,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Delete("/atlas/v2/glossary/category/{categoryGuid}")
        Mono<Response<Void>> deleteGlossaryCategory(
                @HostParam("Endpoint") String endpoint,
                @PathParam("categoryGuid") String categoryGuid,
                RequestOptions requestOptions,
                Context context);

        @Put("/atlas/v2/glossary/category/{categoryGuid}/partial")
        Mono<Response<BinaryData>> partialUpdateGlossaryCategory(
                @HostParam("Endpoint") String endpoint,
                @PathParam("categoryGuid") String categoryGuid,
                @BodyParam("application/json") BinaryData partialUpdates,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/glossary/category/{categoryGuid}/related")
        Mono<Response<BinaryData>> listRelatedCategories(
                @HostParam("Endpoint") String endpoint,
                @PathParam("categoryGuid") String categoryGuid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/glossary/category/{categoryGuid}/terms")
        Mono<Response<BinaryData>> listCategoryTerms(
                @HostParam("Endpoint") String endpoint,
                @PathParam("categoryGuid") String categoryGuid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Post("/atlas/v2/glossary/term")
        Mono<Response<BinaryData>> createGlossaryTerm(
                @HostParam("Endpoint") String endpoint,
                @BodyParam("application/json") BinaryData glossaryTerm,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/glossary/term/{termGuid}")
        Mono<Response<BinaryData>> getGlossaryTerm(
                @HostParam("Endpoint") String endpoint,
                @PathParam("termGuid") String termGuid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Put("/atlas/v2/glossary/term/{termGuid}")
        Mono<Response<BinaryData>> updateGlossaryTerm(
                @HostParam("Endpoint") String endpoint,
                @PathParam("termGuid") String termGuid,
                @BodyParam("application/json") BinaryData glossaryTerm,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Delete("/atlas/v2/glossary/term/{termGuid}")
        Mono<Response<Void>> deleteGlossaryTerm(
                @HostParam("Endpoint") String endpoint,
                @PathParam("termGuid") String termGuid,
                RequestOptions requestOptions,
                Context context);

        @Put("/atlas/v2/glossary/term/{termGuid}/partial")
        Mono<Response<BinaryData>> partialUpdateGlossaryTerm(
                @HostParam("Endpoint") String endpoint,
                @PathParam("termGuid") String termGuid,
                @BodyParam("application/json") BinaryData partialUpdates,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Post("/atlas/v2/glossary/terms")
        Mono<Response<BinaryData>> createGlossaryTerms(
                @HostParam("Endpoint") String endpoint,
                @BodyParam("application/json") BinaryData glossaryTerm,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/glossary/terms/{termGuid}/assignedEntities")
        Mono<Response<BinaryData>> getEntitiesAssignedWithTerm(
                @HostParam("Endpoint") String endpoint,
                @PathParam("termGuid") String termGuid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Post("/atlas/v2/glossary/terms/{termGuid}/assignedEntities")
        Mono<Response<Void>> assignTermToEntities(
                @HostParam("Endpoint") String endpoint,
                @PathParam("termGuid") String termGuid,
                @BodyParam("application/json") BinaryData relatedObjectIds,
                RequestOptions requestOptions,
                Context context);

        @Put("/atlas/v2/glossary/terms/{termGuid}/assignedEntities")
        Mono<Response<Void>> removeTermAssignmentFromEntities(
                @HostParam("Endpoint") String endpoint,
                @PathParam("termGuid") String termGuid,
                @BodyParam("application/json") BinaryData relatedObjectIds,
                RequestOptions requestOptions,
                Context context);

        @Delete("/atlas/v2/glossary/terms/{termGuid}/assignedEntities")
        Mono<Response<Void>> deleteTermAssignmentFromEntities(
                @HostParam("Endpoint") String endpoint,
                @PathParam("termGuid") String termGuid,
                @BodyParam("application/json") BinaryData relatedObjectIds,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/glossary/terms/{termGuid}/related")
        Mono<Response<BinaryData>> listRelatedTerms(
                @HostParam("Endpoint") String endpoint,
                @PathParam("termGuid") String termGuid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/glossary/{glossaryGuid}")
        Mono<Response<BinaryData>> getGlossary(
                @HostParam("Endpoint") String endpoint,
                @PathParam("glossaryGuid") String glossaryGuid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Put("/atlas/v2/glossary/{glossaryGuid}")
        Mono<Response<BinaryData>> updateGlossary(
                @HostParam("Endpoint") String endpoint,
                @PathParam("glossaryGuid") String glossaryGuid,
                @BodyParam("application/json") BinaryData updatedGlossary,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Delete("/atlas/v2/glossary/{glossaryGuid}")
        Mono<Response<Void>> deleteGlossary(
                @HostParam("Endpoint") String endpoint,
                @PathParam("glossaryGuid") String glossaryGuid,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/glossary/{glossaryGuid}/categories")
        Mono<Response<BinaryData>> listGlossaryCategories(
                @HostParam("Endpoint") String endpoint,
                @PathParam("glossaryGuid") String glossaryGuid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/glossary/{glossaryGuid}/categories/headers")
        Mono<Response<BinaryData>> listGlossaryCategoriesHeaders(
                @HostParam("Endpoint") String endpoint,
                @PathParam("glossaryGuid") String glossaryGuid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/glossary/{glossaryGuid}/detailed")
        Mono<Response<BinaryData>> getDetailedGlossary(
                @HostParam("Endpoint") String endpoint,
                @PathParam("glossaryGuid") String glossaryGuid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Put("/atlas/v2/glossary/{glossaryGuid}/partial")
        Mono<Response<BinaryData>> partialUpdateGlossary(
                @HostParam("Endpoint") String endpoint,
                @PathParam("glossaryGuid") String glossaryGuid,
                @BodyParam("application/json") BinaryData partialUpdates,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/glossary/{glossaryGuid}/terms")
        Mono<Response<BinaryData>> listGlossaryTerms(
                @HostParam("Endpoint") String endpoint,
                @PathParam("glossaryGuid") String glossaryGuid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/glossary/{glossaryGuid}/terms/headers")
        Mono<Response<BinaryData>> listGlossaryTermHeaders(
                @HostParam("Endpoint") String endpoint,
                @PathParam("glossaryGuid") String glossaryGuid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        // @Multipart not supported by RestProxy
        @Post("/glossary/{glossaryGuid}/terms/import")
        Mono<Response<BinaryData>> importGlossaryTermsViaCsv(
                @HostParam("Endpoint") String endpoint,
                @PathParam("glossaryGuid") String glossaryGuid,
                @QueryParam("api-version") String apiVersion,
                @BodyParam("multipart/form-data") BinaryData file,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        // @Multipart not supported by RestProxy
        @Post("/glossary/name/{glossaryName}/terms/import")
        Mono<Response<BinaryData>> importGlossaryTermsViaCsvByGlossaryName(
                @HostParam("Endpoint") String endpoint,
                @PathParam("glossaryName") String glossaryName,
                @QueryParam("api-version") String apiVersion,
                @BodyParam("multipart/form-data") BinaryData file,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/glossary/terms/import/{operationGuid}")
        Mono<Response<BinaryData>> getImportCsvOperationStatus(
                @HostParam("Endpoint") String endpoint,
                @PathParam("operationGuid") String operationGuid,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Post("/glossary/{glossaryGuid}/terms/export")
        Mono<Response<Flux<ByteBuffer>>> exportGlossaryTermsAsCsv(
                @HostParam("Endpoint") String endpoint,
                @PathParam("glossaryGuid") String glossaryGuid,
                @QueryParam("api-version") String apiVersion,
                @BodyParam("application/json") BinaryData termGuids,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/glossary/name/{glossaryName}/terms")
        Mono<Response<BinaryData>> listTermsByGlossaryName(
                @HostParam("Endpoint") String endpoint,
                @PathParam("glossaryName") String glossaryName,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);
    }

    /**
     * Get all glossaries registered with Atlas.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         language: String
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         usage: String
     *     }
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listGlossariesWithResponseAsync(RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context -> service.listGlossaries(this.client.getEndpoint(), accept, requestOptions, context));
    }

    /**
     * Get all glossaries registered with Atlas.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         language: String
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         usage: String
     *     }
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listGlossariesWithResponseAsync(RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listGlossaries(this.client.getEndpoint(), accept, requestOptions, context);
    }

    /**
     * Get all glossaries registered with Atlas.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         language: String
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         usage: String
     *     }
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listGlossariesAsync(RequestOptions requestOptions) {
        return listGlossariesWithResponseAsync(requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get all glossaries registered with Atlas.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         language: String
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         usage: String
     *     }
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listGlossariesAsync(RequestOptions requestOptions, Context context) {
        return listGlossariesWithResponseAsync(requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get all glossaries registered with Atlas.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         language: String
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         usage: String
     *     }
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData listGlossaries(RequestOptions requestOptions) {
        return listGlossariesAsync(requestOptions).block();
    }

    /**
     * Get all glossaries registered with Atlas.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         language: String
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         usage: String
     *     }
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> listGlossariesWithResponse(RequestOptions requestOptions, Context context) {
        return listGlossariesWithResponseAsync(requestOptions, context).block();
    }

    /**
     * Create a glossary.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createGlossaryWithResponseAsync(
            BinaryData atlasGlossary, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.createGlossary(
                                this.client.getEndpoint(), atlasGlossary, accept, requestOptions, context));
    }

    /**
     * Create a glossary.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createGlossaryWithResponseAsync(
            BinaryData atlasGlossary, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.createGlossary(this.client.getEndpoint(), atlasGlossary, accept, requestOptions, context);
    }

    /**
     * Create a glossary.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createGlossaryAsync(BinaryData atlasGlossary, RequestOptions requestOptions) {
        return createGlossaryWithResponseAsync(atlasGlossary, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Create a glossary.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createGlossaryAsync(
            BinaryData atlasGlossary, RequestOptions requestOptions, Context context) {
        return createGlossaryWithResponseAsync(atlasGlossary, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Create a glossary.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData createGlossary(BinaryData atlasGlossary, RequestOptions requestOptions) {
        return createGlossaryAsync(atlasGlossary, requestOptions).block();
    }

    /**
     * Create a glossary.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> createGlossaryWithResponse(
            BinaryData atlasGlossary, RequestOptions requestOptions, Context context) {
        return createGlossaryWithResponseAsync(atlasGlossary, requestOptions, context).block();
    }

    /**
     * Create glossary category in bulk.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         childrenCategories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         parentCategory: (recursive schema, see parentCategory above)
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     (recursive schema, see above)
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createGlossaryCategoriesWithResponseAsync(
            BinaryData glossaryCategory, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.createGlossaryCategories(
                                this.client.getEndpoint(), glossaryCategory, accept, requestOptions, context));
    }

    /**
     * Create glossary category in bulk.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         childrenCategories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         parentCategory: (recursive schema, see parentCategory above)
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     (recursive schema, see above)
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createGlossaryCategoriesWithResponseAsync(
            BinaryData glossaryCategory, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.createGlossaryCategories(
                this.client.getEndpoint(), glossaryCategory, accept, requestOptions, context);
    }

    /**
     * Create glossary category in bulk.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         childrenCategories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         parentCategory: (recursive schema, see parentCategory above)
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     (recursive schema, see above)
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createGlossaryCategoriesAsync(BinaryData glossaryCategory, RequestOptions requestOptions) {
        return createGlossaryCategoriesWithResponseAsync(glossaryCategory, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Create glossary category in bulk.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         childrenCategories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         parentCategory: (recursive schema, see parentCategory above)
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     (recursive schema, see above)
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createGlossaryCategoriesAsync(
            BinaryData glossaryCategory, RequestOptions requestOptions, Context context) {
        return createGlossaryCategoriesWithResponseAsync(glossaryCategory, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Create glossary category in bulk.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         childrenCategories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         parentCategory: (recursive schema, see parentCategory above)
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     (recursive schema, see above)
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData createGlossaryCategories(BinaryData glossaryCategory, RequestOptions requestOptions) {
        return createGlossaryCategoriesAsync(glossaryCategory, requestOptions).block();
    }

    /**
     * Create glossary category in bulk.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         childrenCategories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         parentCategory: (recursive schema, see parentCategory above)
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     (recursive schema, see above)
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> createGlossaryCategoriesWithResponse(
            BinaryData glossaryCategory, RequestOptions requestOptions, Context context) {
        return createGlossaryCategoriesWithResponseAsync(glossaryCategory, requestOptions, context).block();
    }

    /**
     * Create a glossary category.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createGlossaryCategoryWithResponseAsync(
            BinaryData glossaryCategory, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.createGlossaryCategory(
                                this.client.getEndpoint(), glossaryCategory, accept, requestOptions, context));
    }

    /**
     * Create a glossary category.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createGlossaryCategoryWithResponseAsync(
            BinaryData glossaryCategory, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.createGlossaryCategory(
                this.client.getEndpoint(), glossaryCategory, accept, requestOptions, context);
    }

    /**
     * Create a glossary category.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createGlossaryCategoryAsync(BinaryData glossaryCategory, RequestOptions requestOptions) {
        return createGlossaryCategoryWithResponseAsync(glossaryCategory, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Create a glossary category.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createGlossaryCategoryAsync(
            BinaryData glossaryCategory, RequestOptions requestOptions, Context context) {
        return createGlossaryCategoryWithResponseAsync(glossaryCategory, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Create a glossary category.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData createGlossaryCategory(BinaryData glossaryCategory, RequestOptions requestOptions) {
        return createGlossaryCategoryAsync(glossaryCategory, requestOptions).block();
    }

    /**
     * Create a glossary category.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> createGlossaryCategoryWithResponse(
            BinaryData glossaryCategory, RequestOptions requestOptions, Context context) {
        return createGlossaryCategoryWithResponseAsync(glossaryCategory, requestOptions, context).block();
    }

    /**
     * Get specific glossary category by its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getGlossaryCategoryWithResponseAsync(
            String categoryGuid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getGlossaryCategory(
                                this.client.getEndpoint(), categoryGuid, accept, requestOptions, context));
    }

    /**
     * Get specific glossary category by its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getGlossaryCategoryWithResponseAsync(
            String categoryGuid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getGlossaryCategory(this.client.getEndpoint(), categoryGuid, accept, requestOptions, context);
    }

    /**
     * Get specific glossary category by its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getGlossaryCategoryAsync(String categoryGuid, RequestOptions requestOptions) {
        return getGlossaryCategoryWithResponseAsync(categoryGuid, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get specific glossary category by its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getGlossaryCategoryAsync(
            String categoryGuid, RequestOptions requestOptions, Context context) {
        return getGlossaryCategoryWithResponseAsync(categoryGuid, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get specific glossary category by its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getGlossaryCategory(String categoryGuid, RequestOptions requestOptions) {
        return getGlossaryCategoryAsync(categoryGuid, requestOptions).block();
    }

    /**
     * Get specific glossary category by its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getGlossaryCategoryWithResponse(
            String categoryGuid, RequestOptions requestOptions, Context context) {
        return getGlossaryCategoryWithResponseAsync(categoryGuid, requestOptions, context).block();
    }

    /**
     * Update the given glossary category by its GUID.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> updateGlossaryCategoryWithResponseAsync(
            String categoryGuid, BinaryData glossaryCategory, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.updateGlossaryCategory(
                                this.client.getEndpoint(),
                                categoryGuid,
                                glossaryCategory,
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Update the given glossary category by its GUID.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> updateGlossaryCategoryWithResponseAsync(
            String categoryGuid, BinaryData glossaryCategory, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.updateGlossaryCategory(
                this.client.getEndpoint(), categoryGuid, glossaryCategory, accept, requestOptions, context);
    }

    /**
     * Update the given glossary category by its GUID.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> updateGlossaryCategoryAsync(
            String categoryGuid, BinaryData glossaryCategory, RequestOptions requestOptions) {
        return updateGlossaryCategoryWithResponseAsync(categoryGuid, glossaryCategory, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Update the given glossary category by its GUID.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> updateGlossaryCategoryAsync(
            String categoryGuid, BinaryData glossaryCategory, RequestOptions requestOptions, Context context) {
        return updateGlossaryCategoryWithResponseAsync(categoryGuid, glossaryCategory, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Update the given glossary category by its GUID.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData updateGlossaryCategory(
            String categoryGuid, BinaryData glossaryCategory, RequestOptions requestOptions) {
        return updateGlossaryCategoryAsync(categoryGuid, glossaryCategory, requestOptions).block();
    }

    /**
     * Update the given glossary category by its GUID.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> updateGlossaryCategoryWithResponse(
            String categoryGuid, BinaryData glossaryCategory, RequestOptions requestOptions, Context context) {
        return updateGlossaryCategoryWithResponseAsync(categoryGuid, glossaryCategory, requestOptions, context).block();
    }

    /**
     * Delete a glossary category.
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteGlossaryCategoryWithResponseAsync(
            String categoryGuid, RequestOptions requestOptions) {
        return FluxUtil.withContext(
                context ->
                        service.deleteGlossaryCategory(
                                this.client.getEndpoint(), categoryGuid, requestOptions, context));
    }

    /**
     * Delete a glossary category.
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteGlossaryCategoryWithResponseAsync(
            String categoryGuid, RequestOptions requestOptions, Context context) {
        return service.deleteGlossaryCategory(this.client.getEndpoint(), categoryGuid, requestOptions, context);
    }

    /**
     * Delete a glossary category.
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteGlossaryCategoryAsync(String categoryGuid, RequestOptions requestOptions) {
        return deleteGlossaryCategoryWithResponseAsync(categoryGuid, requestOptions)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete a glossary category.
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteGlossaryCategoryAsync(String categoryGuid, RequestOptions requestOptions, Context context) {
        return deleteGlossaryCategoryWithResponseAsync(categoryGuid, requestOptions, context)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete a glossary category.
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteGlossaryCategory(String categoryGuid, RequestOptions requestOptions) {
        deleteGlossaryCategoryAsync(categoryGuid, requestOptions).block();
    }

    /**
     * Delete a glossary category.
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteGlossaryCategoryWithResponse(
            String categoryGuid, RequestOptions requestOptions, Context context) {
        return deleteGlossaryCategoryWithResponseAsync(categoryGuid, requestOptions, context).block();
    }

    /**
     * Update the glossary category partially.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> partialUpdateGlossaryCategoryWithResponseAsync(
            String categoryGuid, BinaryData partialUpdates, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.partialUpdateGlossaryCategory(
                                this.client.getEndpoint(),
                                categoryGuid,
                                partialUpdates,
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Update the glossary category partially.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> partialUpdateGlossaryCategoryWithResponseAsync(
            String categoryGuid, BinaryData partialUpdates, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.partialUpdateGlossaryCategory(
                this.client.getEndpoint(), categoryGuid, partialUpdates, accept, requestOptions, context);
    }

    /**
     * Update the glossary category partially.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> partialUpdateGlossaryCategoryAsync(
            String categoryGuid, BinaryData partialUpdates, RequestOptions requestOptions) {
        return partialUpdateGlossaryCategoryWithResponseAsync(categoryGuid, partialUpdates, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Update the glossary category partially.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> partialUpdateGlossaryCategoryAsync(
            String categoryGuid, BinaryData partialUpdates, RequestOptions requestOptions, Context context) {
        return partialUpdateGlossaryCategoryWithResponseAsync(categoryGuid, partialUpdates, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Update the glossary category partially.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData partialUpdateGlossaryCategory(
            String categoryGuid, BinaryData partialUpdates, RequestOptions requestOptions) {
        return partialUpdateGlossaryCategoryAsync(categoryGuid, partialUpdates, requestOptions).block();
    }

    /**
     * Update the glossary category partially.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> partialUpdateGlossaryCategoryWithResponse(
            String categoryGuid, BinaryData partialUpdates, RequestOptions requestOptions, Context context) {
        return partialUpdateGlossaryCategoryWithResponseAsync(categoryGuid, partialUpdates, requestOptions, context)
                .block();
    }

    /**
     * Get all related categories (parent and children). Limit, offset, and sort parameters are currently not being
     * enabled and won't work even they are passed.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listRelatedCategoriesWithResponseAsync(
            String categoryGuid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.listRelatedCategories(
                                this.client.getEndpoint(), categoryGuid, accept, requestOptions, context));
    }

    /**
     * Get all related categories (parent and children). Limit, offset, and sort parameters are currently not being
     * enabled and won't work even they are passed.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listRelatedCategoriesWithResponseAsync(
            String categoryGuid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listRelatedCategories(this.client.getEndpoint(), categoryGuid, accept, requestOptions, context);
    }

    /**
     * Get all related categories (parent and children). Limit, offset, and sort parameters are currently not being
     * enabled and won't work even they are passed.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listRelatedCategoriesAsync(String categoryGuid, RequestOptions requestOptions) {
        return listRelatedCategoriesWithResponseAsync(categoryGuid, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get all related categories (parent and children). Limit, offset, and sort parameters are currently not being
     * enabled and won't work even they are passed.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listRelatedCategoriesAsync(
            String categoryGuid, RequestOptions requestOptions, Context context) {
        return listRelatedCategoriesWithResponseAsync(categoryGuid, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get all related categories (parent and children). Limit, offset, and sort parameters are currently not being
     * enabled and won't work even they are passed.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData listRelatedCategories(String categoryGuid, RequestOptions requestOptions) {
        return listRelatedCategoriesAsync(categoryGuid, requestOptions).block();
    }

    /**
     * Get all related categories (parent and children). Limit, offset, and sort parameters are currently not being
     * enabled and won't work even they are passed.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> listRelatedCategoriesWithResponse(
            String categoryGuid, RequestOptions requestOptions, Context context) {
        return listRelatedCategoriesWithResponseAsync(categoryGuid, requestOptions, context).block();
    }

    /**
     * Get all terms associated with the specific category.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         description: String
     *         displayText: String
     *         expression: String
     *         relationGuid: String
     *         source: String
     *         status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         steward: String
     *         termGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listCategoryTermsWithResponseAsync(
            String categoryGuid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.listCategoryTerms(
                                this.client.getEndpoint(), categoryGuid, accept, requestOptions, context));
    }

    /**
     * Get all terms associated with the specific category.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         description: String
     *         displayText: String
     *         expression: String
     *         relationGuid: String
     *         source: String
     *         status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         steward: String
     *         termGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listCategoryTermsWithResponseAsync(
            String categoryGuid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listCategoryTerms(this.client.getEndpoint(), categoryGuid, accept, requestOptions, context);
    }

    /**
     * Get all terms associated with the specific category.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         description: String
     *         displayText: String
     *         expression: String
     *         relationGuid: String
     *         source: String
     *         status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         steward: String
     *         termGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listCategoryTermsAsync(String categoryGuid, RequestOptions requestOptions) {
        return listCategoryTermsWithResponseAsync(categoryGuid, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get all terms associated with the specific category.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         description: String
     *         displayText: String
     *         expression: String
     *         relationGuid: String
     *         source: String
     *         status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         steward: String
     *         termGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listCategoryTermsAsync(
            String categoryGuid, RequestOptions requestOptions, Context context) {
        return listCategoryTermsWithResponseAsync(categoryGuid, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get all terms associated with the specific category.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         description: String
     *         displayText: String
     *         expression: String
     *         relationGuid: String
     *         source: String
     *         status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         steward: String
     *         termGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData listCategoryTerms(String categoryGuid, RequestOptions requestOptions) {
        return listCategoryTermsAsync(categoryGuid, requestOptions).block();
    }

    /**
     * Get all terms associated with the specific category.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         description: String
     *         displayText: String
     *         expression: String
     *         relationGuid: String
     *         source: String
     *         status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         steward: String
     *         termGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> listCategoryTermsWithResponse(
            String categoryGuid, RequestOptions requestOptions, Context context) {
        return listCategoryTermsWithResponseAsync(categoryGuid, requestOptions, context).block();
    }

    /**
     * Create a glossary term.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createGlossaryTermWithResponseAsync(
            BinaryData glossaryTerm, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.createGlossaryTerm(
                                this.client.getEndpoint(), glossaryTerm, accept, requestOptions, context));
    }

    /**
     * Create a glossary term.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createGlossaryTermWithResponseAsync(
            BinaryData glossaryTerm, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.createGlossaryTerm(this.client.getEndpoint(), glossaryTerm, accept, requestOptions, context);
    }

    /**
     * Create a glossary term.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createGlossaryTermAsync(BinaryData glossaryTerm, RequestOptions requestOptions) {
        return createGlossaryTermWithResponseAsync(glossaryTerm, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Create a glossary term.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createGlossaryTermAsync(
            BinaryData glossaryTerm, RequestOptions requestOptions, Context context) {
        return createGlossaryTermWithResponseAsync(glossaryTerm, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Create a glossary term.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData createGlossaryTerm(BinaryData glossaryTerm, RequestOptions requestOptions) {
        return createGlossaryTermAsync(glossaryTerm, requestOptions).block();
    }

    /**
     * Create a glossary term.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> createGlossaryTermWithResponse(
            BinaryData glossaryTerm, RequestOptions requestOptions, Context context) {
        return createGlossaryTermWithResponseAsync(glossaryTerm, requestOptions, context).block();
    }

    /**
     * Get a specific glossary term by its GUID.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getGlossaryTermWithResponseAsync(String termGuid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getGlossaryTerm(this.client.getEndpoint(), termGuid, accept, requestOptions, context));
    }

    /**
     * Get a specific glossary term by its GUID.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getGlossaryTermWithResponseAsync(
            String termGuid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getGlossaryTerm(this.client.getEndpoint(), termGuid, accept, requestOptions, context);
    }

    /**
     * Get a specific glossary term by its GUID.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getGlossaryTermAsync(String termGuid, RequestOptions requestOptions) {
        return getGlossaryTermWithResponseAsync(termGuid, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get a specific glossary term by its GUID.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getGlossaryTermAsync(String termGuid, RequestOptions requestOptions, Context context) {
        return getGlossaryTermWithResponseAsync(termGuid, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get a specific glossary term by its GUID.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getGlossaryTerm(String termGuid, RequestOptions requestOptions) {
        return getGlossaryTermAsync(termGuid, requestOptions).block();
    }

    /**
     * Get a specific glossary term by its GUID.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getGlossaryTermWithResponse(
            String termGuid, RequestOptions requestOptions, Context context) {
        return getGlossaryTermWithResponseAsync(termGuid, requestOptions, context).block();
    }

    /**
     * Update the given glossary term by its GUID.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> updateGlossaryTermWithResponseAsync(
            String termGuid, BinaryData glossaryTerm, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.updateGlossaryTerm(
                                this.client.getEndpoint(), termGuid, glossaryTerm, accept, requestOptions, context));
    }

    /**
     * Update the given glossary term by its GUID.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> updateGlossaryTermWithResponseAsync(
            String termGuid, BinaryData glossaryTerm, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.updateGlossaryTerm(
                this.client.getEndpoint(), termGuid, glossaryTerm, accept, requestOptions, context);
    }

    /**
     * Update the given glossary term by its GUID.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> updateGlossaryTermAsync(
            String termGuid, BinaryData glossaryTerm, RequestOptions requestOptions) {
        return updateGlossaryTermWithResponseAsync(termGuid, glossaryTerm, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Update the given glossary term by its GUID.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> updateGlossaryTermAsync(
            String termGuid, BinaryData glossaryTerm, RequestOptions requestOptions, Context context) {
        return updateGlossaryTermWithResponseAsync(termGuid, glossaryTerm, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Update the given glossary term by its GUID.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData updateGlossaryTerm(String termGuid, BinaryData glossaryTerm, RequestOptions requestOptions) {
        return updateGlossaryTermAsync(termGuid, glossaryTerm, requestOptions).block();
    }

    /**
     * Update the given glossary term by its GUID.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> updateGlossaryTermWithResponse(
            String termGuid, BinaryData glossaryTerm, RequestOptions requestOptions, Context context) {
        return updateGlossaryTermWithResponseAsync(termGuid, glossaryTerm, requestOptions, context).block();
    }

    /**
     * Delete a glossary term.
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteGlossaryTermWithResponseAsync(String termGuid, RequestOptions requestOptions) {
        return FluxUtil.withContext(
                context -> service.deleteGlossaryTerm(this.client.getEndpoint(), termGuid, requestOptions, context));
    }

    /**
     * Delete a glossary term.
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteGlossaryTermWithResponseAsync(
            String termGuid, RequestOptions requestOptions, Context context) {
        return service.deleteGlossaryTerm(this.client.getEndpoint(), termGuid, requestOptions, context);
    }

    /**
     * Delete a glossary term.
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteGlossaryTermAsync(String termGuid, RequestOptions requestOptions) {
        return deleteGlossaryTermWithResponseAsync(termGuid, requestOptions)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete a glossary term.
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteGlossaryTermAsync(String termGuid, RequestOptions requestOptions, Context context) {
        return deleteGlossaryTermWithResponseAsync(termGuid, requestOptions, context)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete a glossary term.
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteGlossaryTerm(String termGuid, RequestOptions requestOptions) {
        deleteGlossaryTermAsync(termGuid, requestOptions).block();
    }

    /**
     * Delete a glossary term.
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteGlossaryTermWithResponse(
            String termGuid, RequestOptions requestOptions, Context context) {
        return deleteGlossaryTermWithResponseAsync(termGuid, requestOptions, context).block();
    }

    /**
     * Update the glossary term partially.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> partialUpdateGlossaryTermWithResponseAsync(
            String termGuid, BinaryData partialUpdates, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.partialUpdateGlossaryTerm(
                                this.client.getEndpoint(), termGuid, partialUpdates, accept, requestOptions, context));
    }

    /**
     * Update the glossary term partially.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> partialUpdateGlossaryTermWithResponseAsync(
            String termGuid, BinaryData partialUpdates, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.partialUpdateGlossaryTerm(
                this.client.getEndpoint(), termGuid, partialUpdates, accept, requestOptions, context);
    }

    /**
     * Update the glossary term partially.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> partialUpdateGlossaryTermAsync(
            String termGuid, BinaryData partialUpdates, RequestOptions requestOptions) {
        return partialUpdateGlossaryTermWithResponseAsync(termGuid, partialUpdates, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Update the glossary term partially.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> partialUpdateGlossaryTermAsync(
            String termGuid, BinaryData partialUpdates, RequestOptions requestOptions, Context context) {
        return partialUpdateGlossaryTermWithResponseAsync(termGuid, partialUpdates, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Update the glossary term partially.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData partialUpdateGlossaryTerm(
            String termGuid, BinaryData partialUpdates, RequestOptions requestOptions) {
        return partialUpdateGlossaryTermAsync(termGuid, partialUpdates, requestOptions).block();
    }

    /**
     * Update the glossary term partially.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> partialUpdateGlossaryTermWithResponse(
            String termGuid, BinaryData partialUpdates, RequestOptions requestOptions, Context context) {
        return partialUpdateGlossaryTermWithResponseAsync(termGuid, partialUpdates, requestOptions, context).block();
    }

    /**
     * Create glossary terms in bulk.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     (recursive schema, see above)
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createGlossaryTermsWithResponseAsync(
            BinaryData glossaryTerm, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.createGlossaryTerms(
                                this.client.getEndpoint(), glossaryTerm, accept, requestOptions, context));
    }

    /**
     * Create glossary terms in bulk.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     (recursive schema, see above)
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createGlossaryTermsWithResponseAsync(
            BinaryData glossaryTerm, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.createGlossaryTerms(this.client.getEndpoint(), glossaryTerm, accept, requestOptions, context);
    }

    /**
     * Create glossary terms in bulk.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     (recursive schema, see above)
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createGlossaryTermsAsync(BinaryData glossaryTerm, RequestOptions requestOptions) {
        return createGlossaryTermsWithResponseAsync(glossaryTerm, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Create glossary terms in bulk.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     (recursive schema, see above)
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createGlossaryTermsAsync(
            BinaryData glossaryTerm, RequestOptions requestOptions, Context context) {
        return createGlossaryTermsWithResponseAsync(glossaryTerm, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Create glossary terms in bulk.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     (recursive schema, see above)
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData createGlossaryTerms(BinaryData glossaryTerm, RequestOptions requestOptions) {
        return createGlossaryTermsAsync(glossaryTerm, requestOptions).block();
    }

    /**
     * Create glossary terms in bulk.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     (recursive schema, see above)
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> createGlossaryTermsWithResponse(
            BinaryData glossaryTerm, RequestOptions requestOptions, Context context) {
        return createGlossaryTermsWithResponseAsync(glossaryTerm, requestOptions, context).block();
    }

    /**
     * Get all related objects assigned with the specified term.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getEntitiesAssignedWithTermWithResponseAsync(
            String termGuid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getEntitiesAssignedWithTerm(
                                this.client.getEndpoint(), termGuid, accept, requestOptions, context));
    }

    /**
     * Get all related objects assigned with the specified term.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getEntitiesAssignedWithTermWithResponseAsync(
            String termGuid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getEntitiesAssignedWithTerm(
                this.client.getEndpoint(), termGuid, accept, requestOptions, context);
    }

    /**
     * Get all related objects assigned with the specified term.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getEntitiesAssignedWithTermAsync(String termGuid, RequestOptions requestOptions) {
        return getEntitiesAssignedWithTermWithResponseAsync(termGuid, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get all related objects assigned with the specified term.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getEntitiesAssignedWithTermAsync(
            String termGuid, RequestOptions requestOptions, Context context) {
        return getEntitiesAssignedWithTermWithResponseAsync(termGuid, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get all related objects assigned with the specified term.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getEntitiesAssignedWithTerm(String termGuid, RequestOptions requestOptions) {
        return getEntitiesAssignedWithTermAsync(termGuid, requestOptions).block();
    }

    /**
     * Get all related objects assigned with the specified term.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getEntitiesAssignedWithTermWithResponse(
            String termGuid, RequestOptions requestOptions, Context context) {
        return getEntitiesAssignedWithTermWithResponseAsync(termGuid, requestOptions, context).block();
    }

    /**
     * Assign the given term to the provided list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> assignTermToEntitiesWithResponseAsync(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions) {
        return FluxUtil.withContext(
                context ->
                        service.assignTermToEntities(
                                this.client.getEndpoint(), termGuid, relatedObjectIds, requestOptions, context));
    }

    /**
     * Assign the given term to the provided list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> assignTermToEntitiesWithResponseAsync(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions, Context context) {
        return service.assignTermToEntities(
                this.client.getEndpoint(), termGuid, relatedObjectIds, requestOptions, context);
    }

    /**
     * Assign the given term to the provided list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> assignTermToEntitiesAsync(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions) {
        return assignTermToEntitiesWithResponseAsync(termGuid, relatedObjectIds, requestOptions)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Assign the given term to the provided list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> assignTermToEntitiesAsync(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions, Context context) {
        return assignTermToEntitiesWithResponseAsync(termGuid, relatedObjectIds, requestOptions, context)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Assign the given term to the provided list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void assignTermToEntities(String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions) {
        assignTermToEntitiesAsync(termGuid, relatedObjectIds, requestOptions).block();
    }

    /**
     * Assign the given term to the provided list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> assignTermToEntitiesWithResponse(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions, Context context) {
        return assignTermToEntitiesWithResponseAsync(termGuid, relatedObjectIds, requestOptions, context).block();
    }

    /**
     * Delete the term assignment for the given list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeTermAssignmentFromEntitiesWithResponseAsync(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions) {
        return FluxUtil.withContext(
                context ->
                        service.removeTermAssignmentFromEntities(
                                this.client.getEndpoint(), termGuid, relatedObjectIds, requestOptions, context));
    }

    /**
     * Delete the term assignment for the given list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeTermAssignmentFromEntitiesWithResponseAsync(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions, Context context) {
        return service.removeTermAssignmentFromEntities(
                this.client.getEndpoint(), termGuid, relatedObjectIds, requestOptions, context);
    }

    /**
     * Delete the term assignment for the given list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeTermAssignmentFromEntitiesAsync(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions) {
        return removeTermAssignmentFromEntitiesWithResponseAsync(termGuid, relatedObjectIds, requestOptions)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete the term assignment for the given list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeTermAssignmentFromEntitiesAsync(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions, Context context) {
        return removeTermAssignmentFromEntitiesWithResponseAsync(termGuid, relatedObjectIds, requestOptions, context)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete the term assignment for the given list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void removeTermAssignmentFromEntities(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions) {
        removeTermAssignmentFromEntitiesAsync(termGuid, relatedObjectIds, requestOptions).block();
    }

    /**
     * Delete the term assignment for the given list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeTermAssignmentFromEntitiesWithResponse(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions, Context context) {
        return removeTermAssignmentFromEntitiesWithResponseAsync(termGuid, relatedObjectIds, requestOptions, context)
                .block();
    }

    /**
     * Delete the term assignment for the given list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteTermAssignmentFromEntitiesWithResponseAsync(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions) {
        return FluxUtil.withContext(
                context ->
                        service.deleteTermAssignmentFromEntities(
                                this.client.getEndpoint(), termGuid, relatedObjectIds, requestOptions, context));
    }

    /**
     * Delete the term assignment for the given list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteTermAssignmentFromEntitiesWithResponseAsync(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions, Context context) {
        return service.deleteTermAssignmentFromEntities(
                this.client.getEndpoint(), termGuid, relatedObjectIds, requestOptions, context);
    }

    /**
     * Delete the term assignment for the given list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteTermAssignmentFromEntitiesAsync(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions) {
        return deleteTermAssignmentFromEntitiesWithResponseAsync(termGuid, relatedObjectIds, requestOptions)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete the term assignment for the given list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteTermAssignmentFromEntitiesAsync(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions, Context context) {
        return deleteTermAssignmentFromEntitiesWithResponseAsync(termGuid, relatedObjectIds, requestOptions, context)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete the term assignment for the given list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteTermAssignmentFromEntities(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions) {
        deleteTermAssignmentFromEntitiesAsync(termGuid, relatedObjectIds, requestOptions).block();
    }

    /**
     * Delete the term assignment for the given list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteTermAssignmentFromEntitiesWithResponse(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions, Context context) {
        return deleteTermAssignmentFromEntitiesWithResponseAsync(termGuid, relatedObjectIds, requestOptions, context)
                .block();
    }

    /**
     * Get all related terms for a specific term by its GUID. Limit, offset, and sort parameters are currently not being
     * enabled and won't work even they are passed.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listRelatedTermsWithResponseAsync(
            String termGuid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.listRelatedTerms(this.client.getEndpoint(), termGuid, accept, requestOptions, context));
    }

    /**
     * Get all related terms for a specific term by its GUID. Limit, offset, and sort parameters are currently not being
     * enabled and won't work even they are passed.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listRelatedTermsWithResponseAsync(
            String termGuid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listRelatedTerms(this.client.getEndpoint(), termGuid, accept, requestOptions, context);
    }

    /**
     * Get all related terms for a specific term by its GUID. Limit, offset, and sort parameters are currently not being
     * enabled and won't work even they are passed.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listRelatedTermsAsync(String termGuid, RequestOptions requestOptions) {
        return listRelatedTermsWithResponseAsync(termGuid, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get all related terms for a specific term by its GUID. Limit, offset, and sort parameters are currently not being
     * enabled and won't work even they are passed.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listRelatedTermsAsync(String termGuid, RequestOptions requestOptions, Context context) {
        return listRelatedTermsWithResponseAsync(termGuid, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get all related terms for a specific term by its GUID. Limit, offset, and sort parameters are currently not being
     * enabled and won't work even they are passed.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData listRelatedTerms(String termGuid, RequestOptions requestOptions) {
        return listRelatedTermsAsync(termGuid, requestOptions).block();
    }

    /**
     * Get all related terms for a specific term by its GUID. Limit, offset, and sort parameters are currently not being
     * enabled and won't work even they are passed.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> listRelatedTermsWithResponse(
            String termGuid, RequestOptions requestOptions, Context context) {
        return listRelatedTermsWithResponseAsync(termGuid, requestOptions, context).block();
    }

    /**
     * Get a specific Glossary by its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getGlossaryWithResponseAsync(String glossaryGuid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getGlossary(this.client.getEndpoint(), glossaryGuid, accept, requestOptions, context));
    }

    /**
     * Get a specific Glossary by its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getGlossaryWithResponseAsync(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getGlossary(this.client.getEndpoint(), glossaryGuid, accept, requestOptions, context);
    }

    /**
     * Get a specific Glossary by its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getGlossaryAsync(String glossaryGuid, RequestOptions requestOptions) {
        return getGlossaryWithResponseAsync(glossaryGuid, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get a specific Glossary by its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getGlossaryAsync(String glossaryGuid, RequestOptions requestOptions, Context context) {
        return getGlossaryWithResponseAsync(glossaryGuid, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get a specific Glossary by its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getGlossary(String glossaryGuid, RequestOptions requestOptions) {
        return getGlossaryAsync(glossaryGuid, requestOptions).block();
    }

    /**
     * Get a specific Glossary by its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getGlossaryWithResponse(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return getGlossaryWithResponseAsync(glossaryGuid, requestOptions, context).block();
    }

    /**
     * Update the given glossary.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> updateGlossaryWithResponseAsync(
            String glossaryGuid, BinaryData updatedGlossary, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.updateGlossary(
                                this.client.getEndpoint(),
                                glossaryGuid,
                                updatedGlossary,
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Update the given glossary.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> updateGlossaryWithResponseAsync(
            String glossaryGuid, BinaryData updatedGlossary, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.updateGlossary(
                this.client.getEndpoint(), glossaryGuid, updatedGlossary, accept, requestOptions, context);
    }

    /**
     * Update the given glossary.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> updateGlossaryAsync(
            String glossaryGuid, BinaryData updatedGlossary, RequestOptions requestOptions) {
        return updateGlossaryWithResponseAsync(glossaryGuid, updatedGlossary, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Update the given glossary.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> updateGlossaryAsync(
            String glossaryGuid, BinaryData updatedGlossary, RequestOptions requestOptions, Context context) {
        return updateGlossaryWithResponseAsync(glossaryGuid, updatedGlossary, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Update the given glossary.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData updateGlossary(String glossaryGuid, BinaryData updatedGlossary, RequestOptions requestOptions) {
        return updateGlossaryAsync(glossaryGuid, updatedGlossary, requestOptions).block();
    }

    /**
     * Update the given glossary.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> updateGlossaryWithResponse(
            String glossaryGuid, BinaryData updatedGlossary, RequestOptions requestOptions, Context context) {
        return updateGlossaryWithResponseAsync(glossaryGuid, updatedGlossary, requestOptions, context).block();
    }

    /**
     * Delete a glossary.
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteGlossaryWithResponseAsync(String glossaryGuid, RequestOptions requestOptions) {
        return FluxUtil.withContext(
                context -> service.deleteGlossary(this.client.getEndpoint(), glossaryGuid, requestOptions, context));
    }

    /**
     * Delete a glossary.
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteGlossaryWithResponseAsync(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return service.deleteGlossary(this.client.getEndpoint(), glossaryGuid, requestOptions, context);
    }

    /**
     * Delete a glossary.
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteGlossaryAsync(String glossaryGuid, RequestOptions requestOptions) {
        return deleteGlossaryWithResponseAsync(glossaryGuid, requestOptions)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete a glossary.
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteGlossaryAsync(String glossaryGuid, RequestOptions requestOptions, Context context) {
        return deleteGlossaryWithResponseAsync(glossaryGuid, requestOptions, context)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete a glossary.
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteGlossary(String glossaryGuid, RequestOptions requestOptions) {
        deleteGlossaryAsync(glossaryGuid, requestOptions).block();
    }

    /**
     * Delete a glossary.
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteGlossaryWithResponse(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return deleteGlossaryWithResponseAsync(glossaryGuid, requestOptions, context).block();
    }

    /**
     * Get the categories belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         childrenCategories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         parentCategory: (recursive schema, see parentCategory above)
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listGlossaryCategoriesWithResponseAsync(
            String glossaryGuid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.listGlossaryCategories(
                                this.client.getEndpoint(), glossaryGuid, accept, requestOptions, context));
    }

    /**
     * Get the categories belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         childrenCategories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         parentCategory: (recursive schema, see parentCategory above)
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listGlossaryCategoriesWithResponseAsync(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listGlossaryCategories(this.client.getEndpoint(), glossaryGuid, accept, requestOptions, context);
    }

    /**
     * Get the categories belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         childrenCategories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         parentCategory: (recursive schema, see parentCategory above)
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listGlossaryCategoriesAsync(String glossaryGuid, RequestOptions requestOptions) {
        return listGlossaryCategoriesWithResponseAsync(glossaryGuid, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get the categories belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         childrenCategories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         parentCategory: (recursive schema, see parentCategory above)
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listGlossaryCategoriesAsync(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return listGlossaryCategoriesWithResponseAsync(glossaryGuid, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get the categories belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         childrenCategories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         parentCategory: (recursive schema, see parentCategory above)
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData listGlossaryCategories(String glossaryGuid, RequestOptions requestOptions) {
        return listGlossaryCategoriesAsync(glossaryGuid, requestOptions).block();
    }

    /**
     * Get the categories belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         childrenCategories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         parentCategory: (recursive schema, see parentCategory above)
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> listGlossaryCategoriesWithResponse(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return listGlossaryCategoriesWithResponseAsync(glossaryGuid, requestOptions, context).block();
    }

    /**
     * Get the category headers belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         categoryGuid: String
     *         description: String
     *         displayText: String
     *         parentCategoryGuid: String
     *         relationGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listGlossaryCategoriesHeadersWithResponseAsync(
            String glossaryGuid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.listGlossaryCategoriesHeaders(
                                this.client.getEndpoint(), glossaryGuid, accept, requestOptions, context));
    }

    /**
     * Get the category headers belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         categoryGuid: String
     *         description: String
     *         displayText: String
     *         parentCategoryGuid: String
     *         relationGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listGlossaryCategoriesHeadersWithResponseAsync(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listGlossaryCategoriesHeaders(
                this.client.getEndpoint(), glossaryGuid, accept, requestOptions, context);
    }

    /**
     * Get the category headers belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         categoryGuid: String
     *         description: String
     *         displayText: String
     *         parentCategoryGuid: String
     *         relationGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listGlossaryCategoriesHeadersAsync(String glossaryGuid, RequestOptions requestOptions) {
        return listGlossaryCategoriesHeadersWithResponseAsync(glossaryGuid, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get the category headers belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         categoryGuid: String
     *         description: String
     *         displayText: String
     *         parentCategoryGuid: String
     *         relationGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listGlossaryCategoriesHeadersAsync(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return listGlossaryCategoriesHeadersWithResponseAsync(glossaryGuid, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get the category headers belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         categoryGuid: String
     *         description: String
     *         displayText: String
     *         parentCategoryGuid: String
     *         relationGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData listGlossaryCategoriesHeaders(String glossaryGuid, RequestOptions requestOptions) {
        return listGlossaryCategoriesHeadersAsync(glossaryGuid, requestOptions).block();
    }

    /**
     * Get the category headers belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         categoryGuid: String
     *         description: String
     *         displayText: String
     *         parentCategoryGuid: String
     *         relationGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> listGlossaryCategoriesHeadersWithResponse(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return listGlossaryCategoriesHeadersWithResponseAsync(glossaryGuid, requestOptions, context).block();
    }

    /**
     * Get a specific glossary with detailed information.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     *     categoryInfo: {
     *         String: {
     *             guid: String
     *             classifications: [
     *                 (recursive schema, see above)
     *             ]
     *             longDescription: String
     *             name: String
     *             qualifiedName: String
     *             shortDescription: String
     *             lastModifiedTS: String
     *             anchor: {
     *                 displayText: String
     *                 glossaryGuid: String
     *                 relationGuid: String
     *             }
     *             childrenCategories: [
     *                 (recursive schema, see above)
     *             ]
     *             parentCategory: (recursive schema, see parentCategory above)
     *             terms: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     }
     *     termInfo: {
     *         String: {
     *             guid: String
     *             classifications: [
     *                 (recursive schema, see above)
     *             ]
     *             longDescription: String
     *             name: String
     *             qualifiedName: String
     *             shortDescription: String
     *             lastModifiedTS: String
     *             abbreviation: String
     *             templateName: [
     *                 Object
     *             ]
     *             anchor: (recursive schema, see anchor above)
     *             antonyms: [
     *                 (recursive schema, see above)
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             updateTime: Float
     *             updatedBy: String
     *             status: String(Draft/Approved/Alert/Expired)
     *             resources: [
     *                 {
     *                     displayName: String
     *                     url: String
     *                 }
     *             ]
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *             attributes: {
     *                 String: {
     *                     String: Object
     *                 }
     *             }
     *             assignedEntities: [
     *                 {
     *                     guid: String
     *                     typeName: String
     *                     uniqueAttributes: {
     *                         String: Object
     *                     }
     *                     displayText: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     relationshipType: String
     *                     relationshipAttributes: {
     *                         attributes: {
     *                             String: Object
     *                         }
     *                         typeName: String
     *                         lastModifiedTS: String
     *                     }
     *                     relationshipGuid: String
     *                     relationshipStatus: String(ACTIVE/DELETED)
     *                 }
     *             ]
     *             categories: [
     *                 {
     *                     categoryGuid: String
     *                     description: String
     *                     displayText: String
     *                     relationGuid: String
     *                     status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 }
     *             ]
     *             classifies: [
     *                 (recursive schema, see above)
     *             ]
     *             examples: [
     *                 String
     *             ]
     *             isA: [
     *                 (recursive schema, see above)
     *             ]
     *             preferredTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             preferredToTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             replacedBy: [
     *                 (recursive schema, see above)
     *             ]
     *             replacementTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             seeAlso: [
     *                 (recursive schema, see above)
     *             ]
     *             synonyms: [
     *                 (recursive schema, see above)
     *             ]
     *             translatedTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             translationTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             usage: String
     *             validValues: [
     *                 (recursive schema, see above)
     *             ]
     *             validValuesFor: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     }
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getDetailedGlossaryWithResponseAsync(
            String glossaryGuid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getDetailedGlossary(
                                this.client.getEndpoint(), glossaryGuid, accept, requestOptions, context));
    }

    /**
     * Get a specific glossary with detailed information.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     *     categoryInfo: {
     *         String: {
     *             guid: String
     *             classifications: [
     *                 (recursive schema, see above)
     *             ]
     *             longDescription: String
     *             name: String
     *             qualifiedName: String
     *             shortDescription: String
     *             lastModifiedTS: String
     *             anchor: {
     *                 displayText: String
     *                 glossaryGuid: String
     *                 relationGuid: String
     *             }
     *             childrenCategories: [
     *                 (recursive schema, see above)
     *             ]
     *             parentCategory: (recursive schema, see parentCategory above)
     *             terms: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     }
     *     termInfo: {
     *         String: {
     *             guid: String
     *             classifications: [
     *                 (recursive schema, see above)
     *             ]
     *             longDescription: String
     *             name: String
     *             qualifiedName: String
     *             shortDescription: String
     *             lastModifiedTS: String
     *             abbreviation: String
     *             templateName: [
     *                 Object
     *             ]
     *             anchor: (recursive schema, see anchor above)
     *             antonyms: [
     *                 (recursive schema, see above)
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             updateTime: Float
     *             updatedBy: String
     *             status: String(Draft/Approved/Alert/Expired)
     *             resources: [
     *                 {
     *                     displayName: String
     *                     url: String
     *                 }
     *             ]
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *             attributes: {
     *                 String: {
     *                     String: Object
     *                 }
     *             }
     *             assignedEntities: [
     *                 {
     *                     guid: String
     *                     typeName: String
     *                     uniqueAttributes: {
     *                         String: Object
     *                     }
     *                     displayText: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     relationshipType: String
     *                     relationshipAttributes: {
     *                         attributes: {
     *                             String: Object
     *                         }
     *                         typeName: String
     *                         lastModifiedTS: String
     *                     }
     *                     relationshipGuid: String
     *                     relationshipStatus: String(ACTIVE/DELETED)
     *                 }
     *             ]
     *             categories: [
     *                 {
     *                     categoryGuid: String
     *                     description: String
     *                     displayText: String
     *                     relationGuid: String
     *                     status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 }
     *             ]
     *             classifies: [
     *                 (recursive schema, see above)
     *             ]
     *             examples: [
     *                 String
     *             ]
     *             isA: [
     *                 (recursive schema, see above)
     *             ]
     *             preferredTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             preferredToTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             replacedBy: [
     *                 (recursive schema, see above)
     *             ]
     *             replacementTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             seeAlso: [
     *                 (recursive schema, see above)
     *             ]
     *             synonyms: [
     *                 (recursive schema, see above)
     *             ]
     *             translatedTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             translationTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             usage: String
     *             validValues: [
     *                 (recursive schema, see above)
     *             ]
     *             validValuesFor: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     }
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getDetailedGlossaryWithResponseAsync(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getDetailedGlossary(this.client.getEndpoint(), glossaryGuid, accept, requestOptions, context);
    }

    /**
     * Get a specific glossary with detailed information.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     *     categoryInfo: {
     *         String: {
     *             guid: String
     *             classifications: [
     *                 (recursive schema, see above)
     *             ]
     *             longDescription: String
     *             name: String
     *             qualifiedName: String
     *             shortDescription: String
     *             lastModifiedTS: String
     *             anchor: {
     *                 displayText: String
     *                 glossaryGuid: String
     *                 relationGuid: String
     *             }
     *             childrenCategories: [
     *                 (recursive schema, see above)
     *             ]
     *             parentCategory: (recursive schema, see parentCategory above)
     *             terms: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     }
     *     termInfo: {
     *         String: {
     *             guid: String
     *             classifications: [
     *                 (recursive schema, see above)
     *             ]
     *             longDescription: String
     *             name: String
     *             qualifiedName: String
     *             shortDescription: String
     *             lastModifiedTS: String
     *             abbreviation: String
     *             templateName: [
     *                 Object
     *             ]
     *             anchor: (recursive schema, see anchor above)
     *             antonyms: [
     *                 (recursive schema, see above)
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             updateTime: Float
     *             updatedBy: String
     *             status: String(Draft/Approved/Alert/Expired)
     *             resources: [
     *                 {
     *                     displayName: String
     *                     url: String
     *                 }
     *             ]
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *             attributes: {
     *                 String: {
     *                     String: Object
     *                 }
     *             }
     *             assignedEntities: [
     *                 {
     *                     guid: String
     *                     typeName: String
     *                     uniqueAttributes: {
     *                         String: Object
     *                     }
     *                     displayText: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     relationshipType: String
     *                     relationshipAttributes: {
     *                         attributes: {
     *                             String: Object
     *                         }
     *                         typeName: String
     *                         lastModifiedTS: String
     *                     }
     *                     relationshipGuid: String
     *                     relationshipStatus: String(ACTIVE/DELETED)
     *                 }
     *             ]
     *             categories: [
     *                 {
     *                     categoryGuid: String
     *                     description: String
     *                     displayText: String
     *                     relationGuid: String
     *                     status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 }
     *             ]
     *             classifies: [
     *                 (recursive schema, see above)
     *             ]
     *             examples: [
     *                 String
     *             ]
     *             isA: [
     *                 (recursive schema, see above)
     *             ]
     *             preferredTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             preferredToTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             replacedBy: [
     *                 (recursive schema, see above)
     *             ]
     *             replacementTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             seeAlso: [
     *                 (recursive schema, see above)
     *             ]
     *             synonyms: [
     *                 (recursive schema, see above)
     *             ]
     *             translatedTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             translationTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             usage: String
     *             validValues: [
     *                 (recursive schema, see above)
     *             ]
     *             validValuesFor: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     }
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getDetailedGlossaryAsync(String glossaryGuid, RequestOptions requestOptions) {
        return getDetailedGlossaryWithResponseAsync(glossaryGuid, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get a specific glossary with detailed information.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     *     categoryInfo: {
     *         String: {
     *             guid: String
     *             classifications: [
     *                 (recursive schema, see above)
     *             ]
     *             longDescription: String
     *             name: String
     *             qualifiedName: String
     *             shortDescription: String
     *             lastModifiedTS: String
     *             anchor: {
     *                 displayText: String
     *                 glossaryGuid: String
     *                 relationGuid: String
     *             }
     *             childrenCategories: [
     *                 (recursive schema, see above)
     *             ]
     *             parentCategory: (recursive schema, see parentCategory above)
     *             terms: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     }
     *     termInfo: {
     *         String: {
     *             guid: String
     *             classifications: [
     *                 (recursive schema, see above)
     *             ]
     *             longDescription: String
     *             name: String
     *             qualifiedName: String
     *             shortDescription: String
     *             lastModifiedTS: String
     *             abbreviation: String
     *             templateName: [
     *                 Object
     *             ]
     *             anchor: (recursive schema, see anchor above)
     *             antonyms: [
     *                 (recursive schema, see above)
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             updateTime: Float
     *             updatedBy: String
     *             status: String(Draft/Approved/Alert/Expired)
     *             resources: [
     *                 {
     *                     displayName: String
     *                     url: String
     *                 }
     *             ]
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *             attributes: {
     *                 String: {
     *                     String: Object
     *                 }
     *             }
     *             assignedEntities: [
     *                 {
     *                     guid: String
     *                     typeName: String
     *                     uniqueAttributes: {
     *                         String: Object
     *                     }
     *                     displayText: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     relationshipType: String
     *                     relationshipAttributes: {
     *                         attributes: {
     *                             String: Object
     *                         }
     *                         typeName: String
     *                         lastModifiedTS: String
     *                     }
     *                     relationshipGuid: String
     *                     relationshipStatus: String(ACTIVE/DELETED)
     *                 }
     *             ]
     *             categories: [
     *                 {
     *                     categoryGuid: String
     *                     description: String
     *                     displayText: String
     *                     relationGuid: String
     *                     status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 }
     *             ]
     *             classifies: [
     *                 (recursive schema, see above)
     *             ]
     *             examples: [
     *                 String
     *             ]
     *             isA: [
     *                 (recursive schema, see above)
     *             ]
     *             preferredTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             preferredToTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             replacedBy: [
     *                 (recursive schema, see above)
     *             ]
     *             replacementTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             seeAlso: [
     *                 (recursive schema, see above)
     *             ]
     *             synonyms: [
     *                 (recursive schema, see above)
     *             ]
     *             translatedTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             translationTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             usage: String
     *             validValues: [
     *                 (recursive schema, see above)
     *             ]
     *             validValuesFor: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     }
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getDetailedGlossaryAsync(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return getDetailedGlossaryWithResponseAsync(glossaryGuid, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get a specific glossary with detailed information.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     *     categoryInfo: {
     *         String: {
     *             guid: String
     *             classifications: [
     *                 (recursive schema, see above)
     *             ]
     *             longDescription: String
     *             name: String
     *             qualifiedName: String
     *             shortDescription: String
     *             lastModifiedTS: String
     *             anchor: {
     *                 displayText: String
     *                 glossaryGuid: String
     *                 relationGuid: String
     *             }
     *             childrenCategories: [
     *                 (recursive schema, see above)
     *             ]
     *             parentCategory: (recursive schema, see parentCategory above)
     *             terms: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     }
     *     termInfo: {
     *         String: {
     *             guid: String
     *             classifications: [
     *                 (recursive schema, see above)
     *             ]
     *             longDescription: String
     *             name: String
     *             qualifiedName: String
     *             shortDescription: String
     *             lastModifiedTS: String
     *             abbreviation: String
     *             templateName: [
     *                 Object
     *             ]
     *             anchor: (recursive schema, see anchor above)
     *             antonyms: [
     *                 (recursive schema, see above)
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             updateTime: Float
     *             updatedBy: String
     *             status: String(Draft/Approved/Alert/Expired)
     *             resources: [
     *                 {
     *                     displayName: String
     *                     url: String
     *                 }
     *             ]
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *             attributes: {
     *                 String: {
     *                     String: Object
     *                 }
     *             }
     *             assignedEntities: [
     *                 {
     *                     guid: String
     *                     typeName: String
     *                     uniqueAttributes: {
     *                         String: Object
     *                     }
     *                     displayText: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     relationshipType: String
     *                     relationshipAttributes: {
     *                         attributes: {
     *                             String: Object
     *                         }
     *                         typeName: String
     *                         lastModifiedTS: String
     *                     }
     *                     relationshipGuid: String
     *                     relationshipStatus: String(ACTIVE/DELETED)
     *                 }
     *             ]
     *             categories: [
     *                 {
     *                     categoryGuid: String
     *                     description: String
     *                     displayText: String
     *                     relationGuid: String
     *                     status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 }
     *             ]
     *             classifies: [
     *                 (recursive schema, see above)
     *             ]
     *             examples: [
     *                 String
     *             ]
     *             isA: [
     *                 (recursive schema, see above)
     *             ]
     *             preferredTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             preferredToTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             replacedBy: [
     *                 (recursive schema, see above)
     *             ]
     *             replacementTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             seeAlso: [
     *                 (recursive schema, see above)
     *             ]
     *             synonyms: [
     *                 (recursive schema, see above)
     *             ]
     *             translatedTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             translationTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             usage: String
     *             validValues: [
     *                 (recursive schema, see above)
     *             ]
     *             validValuesFor: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     }
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getDetailedGlossary(String glossaryGuid, RequestOptions requestOptions) {
        return getDetailedGlossaryAsync(glossaryGuid, requestOptions).block();
    }

    /**
     * Get a specific glossary with detailed information.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     *     categoryInfo: {
     *         String: {
     *             guid: String
     *             classifications: [
     *                 (recursive schema, see above)
     *             ]
     *             longDescription: String
     *             name: String
     *             qualifiedName: String
     *             shortDescription: String
     *             lastModifiedTS: String
     *             anchor: {
     *                 displayText: String
     *                 glossaryGuid: String
     *                 relationGuid: String
     *             }
     *             childrenCategories: [
     *                 (recursive schema, see above)
     *             ]
     *             parentCategory: (recursive schema, see parentCategory above)
     *             terms: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     }
     *     termInfo: {
     *         String: {
     *             guid: String
     *             classifications: [
     *                 (recursive schema, see above)
     *             ]
     *             longDescription: String
     *             name: String
     *             qualifiedName: String
     *             shortDescription: String
     *             lastModifiedTS: String
     *             abbreviation: String
     *             templateName: [
     *                 Object
     *             ]
     *             anchor: (recursive schema, see anchor above)
     *             antonyms: [
     *                 (recursive schema, see above)
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             updateTime: Float
     *             updatedBy: String
     *             status: String(Draft/Approved/Alert/Expired)
     *             resources: [
     *                 {
     *                     displayName: String
     *                     url: String
     *                 }
     *             ]
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *             attributes: {
     *                 String: {
     *                     String: Object
     *                 }
     *             }
     *             assignedEntities: [
     *                 {
     *                     guid: String
     *                     typeName: String
     *                     uniqueAttributes: {
     *                         String: Object
     *                     }
     *                     displayText: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     relationshipType: String
     *                     relationshipAttributes: {
     *                         attributes: {
     *                             String: Object
     *                         }
     *                         typeName: String
     *                         lastModifiedTS: String
     *                     }
     *                     relationshipGuid: String
     *                     relationshipStatus: String(ACTIVE/DELETED)
     *                 }
     *             ]
     *             categories: [
     *                 {
     *                     categoryGuid: String
     *                     description: String
     *                     displayText: String
     *                     relationGuid: String
     *                     status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 }
     *             ]
     *             classifies: [
     *                 (recursive schema, see above)
     *             ]
     *             examples: [
     *                 String
     *             ]
     *             isA: [
     *                 (recursive schema, see above)
     *             ]
     *             preferredTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             preferredToTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             replacedBy: [
     *                 (recursive schema, see above)
     *             ]
     *             replacementTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             seeAlso: [
     *                 (recursive schema, see above)
     *             ]
     *             synonyms: [
     *                 (recursive schema, see above)
     *             ]
     *             translatedTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             translationTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             usage: String
     *             validValues: [
     *                 (recursive schema, see above)
     *             ]
     *             validValuesFor: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     }
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getDetailedGlossaryWithResponse(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return getDetailedGlossaryWithResponseAsync(glossaryGuid, requestOptions, context).block();
    }

    /**
     * Update the glossary partially. Some properties such as qualifiedName are not allowed to be updated.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> partialUpdateGlossaryWithResponseAsync(
            String glossaryGuid, BinaryData partialUpdates, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.partialUpdateGlossary(
                                this.client.getEndpoint(),
                                glossaryGuid,
                                partialUpdates,
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Update the glossary partially. Some properties such as qualifiedName are not allowed to be updated.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> partialUpdateGlossaryWithResponseAsync(
            String glossaryGuid, BinaryData partialUpdates, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.partialUpdateGlossary(
                this.client.getEndpoint(), glossaryGuid, partialUpdates, accept, requestOptions, context);
    }

    /**
     * Update the glossary partially. Some properties such as qualifiedName are not allowed to be updated.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> partialUpdateGlossaryAsync(
            String glossaryGuid, BinaryData partialUpdates, RequestOptions requestOptions) {
        return partialUpdateGlossaryWithResponseAsync(glossaryGuid, partialUpdates, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Update the glossary partially. Some properties such as qualifiedName are not allowed to be updated.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> partialUpdateGlossaryAsync(
            String glossaryGuid, BinaryData partialUpdates, RequestOptions requestOptions, Context context) {
        return partialUpdateGlossaryWithResponseAsync(glossaryGuid, partialUpdates, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Update the glossary partially. Some properties such as qualifiedName are not allowed to be updated.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData partialUpdateGlossary(
            String glossaryGuid, BinaryData partialUpdates, RequestOptions requestOptions) {
        return partialUpdateGlossaryAsync(glossaryGuid, partialUpdates, requestOptions).block();
    }

    /**
     * Update the glossary partially. Some properties such as qualifiedName are not allowed to be updated.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> partialUpdateGlossaryWithResponse(
            String glossaryGuid, BinaryData partialUpdates, RequestOptions requestOptions, Context context) {
        return partialUpdateGlossaryWithResponseAsync(glossaryGuid, partialUpdates, requestOptions, context).block();
    }

    /**
     * Get terms belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listGlossaryTermsWithResponseAsync(
            String glossaryGuid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.listGlossaryTerms(
                                this.client.getEndpoint(), glossaryGuid, accept, requestOptions, context));
    }

    /**
     * Get terms belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listGlossaryTermsWithResponseAsync(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listGlossaryTerms(this.client.getEndpoint(), glossaryGuid, accept, requestOptions, context);
    }

    /**
     * Get terms belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listGlossaryTermsAsync(String glossaryGuid, RequestOptions requestOptions) {
        return listGlossaryTermsWithResponseAsync(glossaryGuid, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get terms belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listGlossaryTermsAsync(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return listGlossaryTermsWithResponseAsync(glossaryGuid, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get terms belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData listGlossaryTerms(String glossaryGuid, RequestOptions requestOptions) {
        return listGlossaryTermsAsync(glossaryGuid, requestOptions).block();
    }

    /**
     * Get terms belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> listGlossaryTermsWithResponse(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return listGlossaryTermsWithResponseAsync(glossaryGuid, requestOptions, context).block();
    }

    /**
     * Get term headers belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         description: String
     *         displayText: String
     *         expression: String
     *         relationGuid: String
     *         source: String
     *         status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         steward: String
     *         termGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listGlossaryTermHeadersWithResponseAsync(
            String glossaryGuid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.listGlossaryTermHeaders(
                                this.client.getEndpoint(), glossaryGuid, accept, requestOptions, context));
    }

    /**
     * Get term headers belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         description: String
     *         displayText: String
     *         expression: String
     *         relationGuid: String
     *         source: String
     *         status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         steward: String
     *         termGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listGlossaryTermHeadersWithResponseAsync(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listGlossaryTermHeaders(
                this.client.getEndpoint(), glossaryGuid, accept, requestOptions, context);
    }

    /**
     * Get term headers belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         description: String
     *         displayText: String
     *         expression: String
     *         relationGuid: String
     *         source: String
     *         status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         steward: String
     *         termGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listGlossaryTermHeadersAsync(String glossaryGuid, RequestOptions requestOptions) {
        return listGlossaryTermHeadersWithResponseAsync(glossaryGuid, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get term headers belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         description: String
     *         displayText: String
     *         expression: String
     *         relationGuid: String
     *         source: String
     *         status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         steward: String
     *         termGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listGlossaryTermHeadersAsync(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return listGlossaryTermHeadersWithResponseAsync(glossaryGuid, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get term headers belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         description: String
     *         displayText: String
     *         expression: String
     *         relationGuid: String
     *         source: String
     *         status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         steward: String
     *         termGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData listGlossaryTermHeaders(String glossaryGuid, RequestOptions requestOptions) {
        return listGlossaryTermHeadersAsync(glossaryGuid, requestOptions).block();
    }

    /**
     * Get term headers belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         description: String
     *         displayText: String
     *         expression: String
     *         relationGuid: String
     *         source: String
     *         status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         steward: String
     *         termGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> listGlossaryTermHeadersWithResponse(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return listGlossaryTermHeadersWithResponseAsync(glossaryGuid, requestOptions, context).block();
    }

    /**
     * Import Glossary Terms from local csv file.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Header Parameters</strong>
     *
     * <table border="1">
     *     <caption>Header Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>contentLength</td><td>long</td><td>Yes</td><td>The contentLength parameter</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> importGlossaryTermsViaCsvWithResponseAsync(
            String glossaryGuid, BinaryData file, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.importGlossaryTermsViaCsv(
                                this.client.getEndpoint(),
                                glossaryGuid,
                                this.client.getApiVersion(),
                                file,
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Import Glossary Terms from local csv file.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Header Parameters</strong>
     *
     * <table border="1">
     *     <caption>Header Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>contentLength</td><td>long</td><td>Yes</td><td>The contentLength parameter</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> importGlossaryTermsViaCsvWithResponseAsync(
            String glossaryGuid, BinaryData file, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.importGlossaryTermsViaCsv(
                this.client.getEndpoint(),
                glossaryGuid,
                this.client.getApiVersion(),
                file,
                accept,
                requestOptions,
                context);
    }

    /**
     * Import Glossary Terms from local csv file.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Header Parameters</strong>
     *
     * <table border="1">
     *     <caption>Header Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>contentLength</td><td>long</td><td>Yes</td><td>The contentLength parameter</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> importGlossaryTermsViaCsvAsync(
            String glossaryGuid, BinaryData file, RequestOptions requestOptions) {
        return importGlossaryTermsViaCsvWithResponseAsync(glossaryGuid, file, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Import Glossary Terms from local csv file.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Header Parameters</strong>
     *
     * <table border="1">
     *     <caption>Header Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>contentLength</td><td>long</td><td>Yes</td><td>The contentLength parameter</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> importGlossaryTermsViaCsvAsync(
            String glossaryGuid, BinaryData file, RequestOptions requestOptions, Context context) {
        return importGlossaryTermsViaCsvWithResponseAsync(glossaryGuid, file, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Import Glossary Terms from local csv file.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Header Parameters</strong>
     *
     * <table border="1">
     *     <caption>Header Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>contentLength</td><td>long</td><td>Yes</td><td>The contentLength parameter</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData importGlossaryTermsViaCsv(String glossaryGuid, BinaryData file, RequestOptions requestOptions) {
        return importGlossaryTermsViaCsvAsync(glossaryGuid, file, requestOptions).block();
    }

    /**
     * Import Glossary Terms from local csv file.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Header Parameters</strong>
     *
     * <table border="1">
     *     <caption>Header Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>contentLength</td><td>long</td><td>Yes</td><td>The contentLength parameter</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> importGlossaryTermsViaCsvWithResponse(
            String glossaryGuid, BinaryData file, RequestOptions requestOptions, Context context) {
        return importGlossaryTermsViaCsvWithResponseAsync(glossaryGuid, file, requestOptions, context).block();
    }

    /**
     * Import Glossary Terms from local csv file by glossaryName.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Header Parameters</strong>
     *
     * <table border="1">
     *     <caption>Header Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>contentLength</td><td>long</td><td>Yes</td><td>The contentLength parameter</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param glossaryName The name of the glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> importGlossaryTermsViaCsvByGlossaryNameWithResponseAsync(
            String glossaryName, BinaryData file, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.importGlossaryTermsViaCsvByGlossaryName(
                                this.client.getEndpoint(),
                                glossaryName,
                                this.client.getApiVersion(),
                                file,
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Import Glossary Terms from local csv file by glossaryName.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Header Parameters</strong>
     *
     * <table border="1">
     *     <caption>Header Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>contentLength</td><td>long</td><td>Yes</td><td>The contentLength parameter</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param glossaryName The name of the glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> importGlossaryTermsViaCsvByGlossaryNameWithResponseAsync(
            String glossaryName, BinaryData file, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.importGlossaryTermsViaCsvByGlossaryName(
                this.client.getEndpoint(),
                glossaryName,
                this.client.getApiVersion(),
                file,
                accept,
                requestOptions,
                context);
    }

    /**
     * Import Glossary Terms from local csv file by glossaryName.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Header Parameters</strong>
     *
     * <table border="1">
     *     <caption>Header Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>contentLength</td><td>long</td><td>Yes</td><td>The contentLength parameter</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param glossaryName The name of the glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> importGlossaryTermsViaCsvByGlossaryNameAsync(
            String glossaryName, BinaryData file, RequestOptions requestOptions) {
        return importGlossaryTermsViaCsvByGlossaryNameWithResponseAsync(glossaryName, file, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Import Glossary Terms from local csv file by glossaryName.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Header Parameters</strong>
     *
     * <table border="1">
     *     <caption>Header Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>contentLength</td><td>long</td><td>Yes</td><td>The contentLength parameter</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param glossaryName The name of the glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> importGlossaryTermsViaCsvByGlossaryNameAsync(
            String glossaryName, BinaryData file, RequestOptions requestOptions, Context context) {
        return importGlossaryTermsViaCsvByGlossaryNameWithResponseAsync(glossaryName, file, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Import Glossary Terms from local csv file by glossaryName.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Header Parameters</strong>
     *
     * <table border="1">
     *     <caption>Header Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>contentLength</td><td>long</td><td>Yes</td><td>The contentLength parameter</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param glossaryName The name of the glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData importGlossaryTermsViaCsvByGlossaryName(
            String glossaryName, BinaryData file, RequestOptions requestOptions) {
        return importGlossaryTermsViaCsvByGlossaryNameAsync(glossaryName, file, requestOptions).block();
    }

    /**
     * Import Glossary Terms from local csv file by glossaryName.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Header Parameters</strong>
     *
     * <table border="1">
     *     <caption>Header Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>contentLength</td><td>long</td><td>Yes</td><td>The contentLength parameter</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param glossaryName The name of the glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> importGlossaryTermsViaCsvByGlossaryNameWithResponse(
            String glossaryName, BinaryData file, RequestOptions requestOptions, Context context) {
        return importGlossaryTermsViaCsvByGlossaryNameWithResponseAsync(glossaryName, file, requestOptions, context)
                .block();
    }

    /**
     * Get the status of import csv operation.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param operationGuid The globally unique identifier for async operation/job.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getImportCsvOperationStatusWithResponseAsync(
            String operationGuid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getImportCsvOperationStatus(
                                this.client.getEndpoint(),
                                operationGuid,
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Get the status of import csv operation.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param operationGuid The globally unique identifier for async operation/job.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getImportCsvOperationStatusWithResponseAsync(
            String operationGuid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getImportCsvOperationStatus(
                this.client.getEndpoint(), operationGuid, this.client.getApiVersion(), accept, requestOptions, context);
    }

    /**
     * Get the status of import csv operation.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param operationGuid The globally unique identifier for async operation/job.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getImportCsvOperationStatusAsync(String operationGuid, RequestOptions requestOptions) {
        return getImportCsvOperationStatusWithResponseAsync(operationGuid, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get the status of import csv operation.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param operationGuid The globally unique identifier for async operation/job.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getImportCsvOperationStatusAsync(
            String operationGuid, RequestOptions requestOptions, Context context) {
        return getImportCsvOperationStatusWithResponseAsync(operationGuid, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get the status of import csv operation.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param operationGuid The globally unique identifier for async operation/job.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getImportCsvOperationStatus(String operationGuid, RequestOptions requestOptions) {
        return getImportCsvOperationStatusAsync(operationGuid, requestOptions).block();
    }

    /**
     * Get the status of import csv operation.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param operationGuid The globally unique identifier for async operation/job.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getImportCsvOperationStatusWithResponse(
            String operationGuid, RequestOptions requestOptions, Context context) {
        return getImportCsvOperationStatusWithResponseAsync(operationGuid, requestOptions, context).block();
    }

    /**
     * Export Glossary Terms as csv file.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     String
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Flux<ByteBuffer>>> exportGlossaryTermsAsCsvWithResponseAsync(
            String glossaryGuid, BinaryData termGuids, RequestOptions requestOptions) {
        final String accept = "text/csv";
        return FluxUtil.withContext(
                context ->
                        service.exportGlossaryTermsAsCsv(
                                this.client.getEndpoint(),
                                glossaryGuid,
                                this.client.getApiVersion(),
                                termGuids,
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Export Glossary Terms as csv file.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     String
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Flux<ByteBuffer>>> exportGlossaryTermsAsCsvWithResponseAsync(
            String glossaryGuid, BinaryData termGuids, RequestOptions requestOptions, Context context) {
        final String accept = "text/csv";
        return service.exportGlossaryTermsAsCsv(
                this.client.getEndpoint(),
                glossaryGuid,
                this.client.getApiVersion(),
                termGuids,
                accept,
                requestOptions,
                context);
    }

    /**
     * Export Glossary Terms as csv file.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     String
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Flux<ByteBuffer>> exportGlossaryTermsAsCsvAsync(
            String glossaryGuid, BinaryData termGuids, RequestOptions requestOptions) {
        return exportGlossaryTermsAsCsvWithResponseAsync(glossaryGuid, termGuids, requestOptions)
                .flatMap(
                        (Response<Flux<ByteBuffer>> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Export Glossary Terms as csv file.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     String
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Flux<ByteBuffer>> exportGlossaryTermsAsCsvAsync(
            String glossaryGuid, BinaryData termGuids, RequestOptions requestOptions, Context context) {
        return exportGlossaryTermsAsCsvWithResponseAsync(glossaryGuid, termGuids, requestOptions, context)
                .flatMap(
                        (Response<Flux<ByteBuffer>> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Export Glossary Terms as csv file.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     String
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Flux<ByteBuffer> exportGlossaryTermsAsCsv(
            String glossaryGuid, BinaryData termGuids, RequestOptions requestOptions) {
        return exportGlossaryTermsAsCsvAsync(glossaryGuid, termGuids, requestOptions).block();
    }

    /**
     * Export Glossary Terms as csv file.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     String
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Flux<ByteBuffer>> exportGlossaryTermsAsCsvWithResponse(
            String glossaryGuid, BinaryData termGuids, RequestOptions requestOptions, Context context) {
        return exportGlossaryTermsAsCsvWithResponseAsync(glossaryGuid, termGuids, requestOptions, context).block();
    }

    /**
     * Get terms by glossary name.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryName The name of the glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listTermsByGlossaryNameWithResponseAsync(
            String glossaryName, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.listTermsByGlossaryName(
                                this.client.getEndpoint(),
                                glossaryName,
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Get terms by glossary name.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryName The name of the glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listTermsByGlossaryNameWithResponseAsync(
            String glossaryName, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listTermsByGlossaryName(
                this.client.getEndpoint(), glossaryName, this.client.getApiVersion(), accept, requestOptions, context);
    }

    /**
     * Get terms by glossary name.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryName The name of the glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listTermsByGlossaryNameAsync(String glossaryName, RequestOptions requestOptions) {
        return listTermsByGlossaryNameWithResponseAsync(glossaryName, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get terms by glossary name.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryName The name of the glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listTermsByGlossaryNameAsync(
            String glossaryName, RequestOptions requestOptions, Context context) {
        return listTermsByGlossaryNameWithResponseAsync(glossaryName, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Get terms by glossary name.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryName The name of the glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData listTermsByGlossaryName(String glossaryName, RequestOptions requestOptions) {
        return listTermsByGlossaryNameAsync(glossaryName, requestOptions).block();
    }

    /**
     * Get terms by glossary name.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryName The name of the glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> listTermsByGlossaryNameWithResponse(
            String glossaryName, RequestOptions requestOptions, Context context) {
        return listTermsByGlossaryNameWithResponseAsync(glossaryName, requestOptions, context).block();
    }
}
