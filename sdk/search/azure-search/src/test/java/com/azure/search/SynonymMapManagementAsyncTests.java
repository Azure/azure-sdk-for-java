// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.rest.Response;
import com.azure.core.util.FluxUtil;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.SynonymMap;
import com.azure.search.test.AccessConditionAsyncTests;
import com.azure.search.test.AccessOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class SynonymMapManagementAsyncTests extends SynonymMapManagementTestBase {
    private SearchServiceAsyncClient client;

    // commonly used lambda definitions
    private BiFunction<SynonymMap,
        AccessOptions,
        Mono<SynonymMap>> createOrUpdateSynonymMapAsyncFunc =
            (SynonymMap synonymMap, AccessOptions accessOptions) ->
                createOrUpdateSynonymMap(
                    synonymMap, accessOptions.getAccessCondition(), accessOptions.getRequestOptions());

    private Supplier<SynonymMap> newSynonymMapFunc = this::createTestSynonymMap;

    private Function<SynonymMap, SynonymMap> mutateSynonymMapFunc = this::mutateSynonymsInSynonymMap;

    private BiFunction<String, AccessOptions, Mono<Void>> deleteSynonymMapAsyncFunc =
        (String name, AccessOptions ac) ->
            deleteSynonymMap(name, ac.getAccessCondition(), ac.getRequestOptions());

    private Mono<SynonymMap> createOrUpdateSynonymMap(
        SynonymMap sm, AccessCondition ac, RequestOptions ro) {
        return client.createOrUpdateSynonymMapWithResponse(sm, ac, ro).map(Response::getValue);
    }

    private Mono<Void> deleteSynonymMap(
        String name, AccessCondition ac, RequestOptions ro) {
        return client.deleteSynonymMapWithResponse(name, ac, ro).flatMap(FluxUtil::toMono);
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildAsyncClient();
    }

    @Test
    public void createSynonymMapReturnsCorrectDefinition() {
        SynonymMap expectedSynonymMap = createTestSynonymMap();

        StepVerifier
            .create(client.createSynonymMap(expectedSynonymMap))
            .assertNext(actualSynonymMap ->
                assertSynonymMapsEqual(expectedSynonymMap, actualSynonymMap)
            )
            .verifyComplete();
    }

    @Test
    public void createSynonymMapReturnsCorrectDefinitionWithResponse() {
        SynonymMap expectedSynonymMap = createTestSynonymMap();

        StepVerifier
            .create(client.createSynonymMapWithResponse(expectedSynonymMap, generateRequestOptions()))
            .assertNext(actualSynonymMap ->
                assertSynonymMapsEqual(expectedSynonymMap, actualSynonymMap.getValue())
            )
            .verifyComplete();
    }

    @Test
    public void createSynonymMapFailsWithUsefulMessageOnUserError() {
        // Create SynonymMap with invalid synonym
        SynonymMap expectedSynonymMap = createTestSynonymMap()
            .setSynonyms("a => b => c");

        assertHttpResponseExceptionAsync(
            client.createSynonymMap(expectedSynonymMap),
            HttpResponseStatus.BAD_REQUEST,
            "Syntax error in line 1: 'a => b => c'. "
                + "Only one explicit mapping (=>) can be specified in a synonym rule."
        );
    }

    @Test
    public void getSynonymMapReturnsCorrectDefinition() {
        SynonymMap expected = createTestSynonymMap();
        Mono<SynonymMap> synonymMap = client.createSynonymMap(expected).flatMap(sm -> client.getSynonymMap(expected.getName()));

        StepVerifier
            .create(synonymMap)
            .assertNext(actual -> assertSynonymMapsEqual(expected, actual))
            .verifyComplete();
    }

    @Test
    public void getSynonymMapReturnsCorrectDefinitionWithResponse() {
        SynonymMap expected = createTestSynonymMap();
        Mono<Response<SynonymMap>> synonymMapWithResponse = client.createSynonymMap(expected)
            .flatMap(sm -> client.getSynonymMapWithResponse(expected.getName(), generateRequestOptions()));

        StepVerifier
            .create(synonymMapWithResponse)
            .assertNext(result -> assertSynonymMapsEqual(expected, result.getValue()))
            .verifyComplete();
    }

    @Test
    public void getSynonymMapThrowsOnNotFound() {
        final String synonymMapName = "thisSynonymMapDoesNotExist";
        final String exceptionMessage = String.format("No synonym map with the name '%s' was found", synonymMapName);

        assertHttpResponseExceptionAsync(
            client.getSynonymMap(synonymMapName),
            HttpResponseStatus.NOT_FOUND,
            exceptionMessage
        );
    }

    @Test
    public void getSynonymMapThrowsOnNotFoundWithResponse() {
        final String synonymMapName = "thisSynonymMapDoesNotExist";
        final String exceptionMessage = String.format("No synonym map with the name '%s' was found", synonymMapName);

        assertHttpResponseExceptionAsync(
            client.getSynonymMapWithResponse(synonymMapName, generateRequestOptions()),
            HttpResponseStatus.NOT_FOUND,
            exceptionMessage
        );
    }

    @Test
    public void canUpdateSynonymMap() {
        SynonymMap initial = createTestSynonymMap();

        client.createSynonymMap(initial).block();

        SynonymMap updatedExpected = createTestSynonymMap()
            .setName(initial.getName())
            .setSynonyms("newword1,newword2");

        StepVerifier
            .create(client.createOrUpdateSynonymMap(updatedExpected))
            .assertNext(updatedActual -> assertSynonymMapsEqual(updatedExpected, updatedActual))
            .verifyComplete();

        StepVerifier
            .create(client.listSynonymMaps())
            .assertNext(synonymMap -> assertSynonymMapsEqual(synonymMap, updatedExpected))
            .verifyComplete();
    }

    @Test
    public void canUpdateSynonymMapWithResponse() {
        SynonymMap initial = createTestSynonymMap();

        client.createSynonymMap(initial).block();

        SynonymMap updatedExpected = createTestSynonymMap()
            .setName(initial.getName())
            .setSynonyms("newword1,newword2");

        StepVerifier
            .create(client.createOrUpdateSynonymMapWithResponse(updatedExpected, new AccessCondition(),
                generateRequestOptions()))
            .assertNext(updatedActual -> assertSynonymMapsEqual(updatedExpected, updatedActual.getValue()))
            .verifyComplete();

        StepVerifier
            .create(client.listSynonymMaps())
            .assertNext(synonymMap -> assertSynonymMapsEqual(synonymMap, updatedExpected))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateSynonymMapCreatesWhenSynonymMapDoesNotExist() {
        SynonymMap expected = createTestSynonymMap();

        StepVerifier
            .create(client.createOrUpdateSynonymMap(expected))
            .assertNext(res -> assertSynonymMapsEqual(expected, res))
            .verifyComplete();

        StepVerifier
            .create(client.createOrUpdateSynonymMapWithResponse(expected.setName("test-synonym2"),
                new AccessCondition(), generateRequestOptions()))
            .assertNext(res -> {
                Assert.assertEquals(HttpResponseStatus.CREATED.code(), res.getStatusCode());
                assertSynonymMapsEqual(expected, res.getValue());
            })
            .verifyComplete();
    }

    @Test
    public void createOrUpdateSynonymMapCreatesWhenSynonymMapDoesNotExistWithResponse() {
        SynonymMap expected = createTestSynonymMap();

        StepVerifier
            .create(client.createOrUpdateSynonymMapWithResponse(expected,
                new AccessCondition(), generateRequestOptions()))
            .assertNext(res -> {
                Assert.assertEquals(HttpResponseStatus.CREATED.code(), res.getStatusCode());
                assertSynonymMapsEqual(expected, res.getValue());
            })
            .verifyComplete();
    }

    @Test
    public void createOrUpdateSynonymMapIfNotExistsSucceedsOnNoResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.createOrUpdateIfNotExistsSucceedsOnNoResourceAsync(
            createOrUpdateSynonymMapAsyncFunc,
            newSynonymMapFunc);
    }

    @Test
    public void createOrUpdateSynonymMapIfExistsSucceedsOnExistingResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();
        act.updateIfExistsSucceedsOnExistingResourceAsync(
            newSynonymMapFunc,
            createOrUpdateSynonymMapAsyncFunc,
            mutateSynonymMapFunc);
    }

    @Test
    public void createOrUpdateSynonymMapIfExistsFailsOnNoResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();
        act.updateIfExistsFailsOnNoResourceAsync(
            newSynonymMapFunc,
            createOrUpdateSynonymMapAsyncFunc);
    }

    @Test
    public void createOrUpdateSynonymMapIfNotChangedSucceedsWhenResourceUnchanged() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();
        act.updateIfNotChangedSucceedsWhenResourceUnchangedAsync(
            newSynonymMapFunc,
            createOrUpdateSynonymMapAsyncFunc,
            mutateSynonymMapFunc);
    }

    @Test
    public void createOrUpdateSynonymMapIfNotChangedFailsWhenResourceChanged() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();
        act.updateIfNotChangedFailsWhenResourceChangedAsync(
            newSynonymMapFunc,
            createOrUpdateSynonymMapAsyncFunc,
            mutateSynonymMapFunc);
    }

    @Test
    public void deleteSynonymMapIsIdempotent() {
        SynonymMap synonymMap = createTestSynonymMap();

        StepVerifier
            .create(client.deleteSynonymMapWithResponse(synonymMap.getName(), new AccessCondition(), generateRequestOptions()))
            .assertNext(synonymMapResponse ->
                Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), synonymMapResponse.getStatusCode())
            )
            .verifyComplete();

        StepVerifier
            .create(client.createSynonymMapWithResponse(synonymMap, generateRequestOptions()))
            .assertNext(synonymMapResponse ->
                Assert.assertEquals(HttpResponseStatus.CREATED.code(), synonymMapResponse.getStatusCode())
            )
            .verifyComplete();

        StepVerifier
            .create(client.deleteSynonymMapWithResponse(synonymMap.getName(),
                new AccessCondition(), generateRequestOptions()))
            .assertNext(synonymMapResponse ->
                Assert.assertEquals(HttpResponseStatus.NO_CONTENT.code(), synonymMapResponse.getStatusCode())
            )
            .verifyComplete();

        StepVerifier
            .create(client.deleteSynonymMapWithResponse(synonymMap.getName(),
                new AccessCondition(), generateRequestOptions()))
            .assertNext(synonymMapResponse ->
                Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), synonymMapResponse.getStatusCode())
            )
            .verifyComplete();
    }

    @Test
    public void canCreateAndDeleteSynonymMap() {
        SynonymMap synonymMap = createTestSynonymMap();

        StepVerifier
            .create(client.createSynonymMap(synonymMap)
                .then(client.deleteSynonymMap(synonymMap.getName()))
                .then(client.synonymMapExists(synonymMap.getName())))
            .assertNext(Assert::assertFalse)
            .verifyComplete();
    }

    @Test
    public void canCreateAndListSynonymMaps() {
        SynonymMap synonymMap1 = createTestSynonymMap();
        SynonymMap synonymMap2 = createTestSynonymMap().setName("test-synonym1");

        Mono<SynonymMap> creationResponse = client.createSynonymMap(synonymMap1)
            .then(client.createSynonymMap(synonymMap2));


        StepVerifier
            .create(creationResponse.thenMany(client.listSynonymMaps()))
            .assertNext(sm1 -> Assert.assertEquals(synonymMap1.getName(), sm1.getName()))
            .assertNext(sm2 -> Assert.assertEquals(synonymMap2.getName(), sm2.getName()))
            .verifyComplete();
    }

    @Test
    public void canListSynonymMapsWithSelectedField() {
        SynonymMap synonymMap1 = createTestSynonymMap();
        SynonymMap synonymMap2 = createTestSynonymMap().setName("test-synonym1");

        Mono<SynonymMap> creationResponse = client.createSynonymMap(synonymMap1)
            .then(client.createSynonymMap(synonymMap2));

        StepVerifier
            .create(creationResponse.thenMany(client.listSynonymMaps("name", generateRequestOptions())))
            .assertNext(sm1 -> {
                Assert.assertEquals(synonymMap1.getName(), sm1.getName());
                Assert.assertNull(sm1.getSynonyms());
                Assert.assertNull(sm1.getETag());
            })
            .assertNext(sm2 -> {
                Assert.assertEquals(synonymMap2.getName(), sm2.getName());
                Assert.assertNull(sm2.getSynonyms());
                Assert.assertNull(sm2.getETag());
            })
            .verifyComplete();
    }

    @Test
    public void existsReturnsTrueForExistingSynonymMap() {
        SynonymMap synonymMap = createTestSynonymMap();

        StepVerifier
            .create(client.createSynonymMap(synonymMap)
                .then(client.synonymMapExists(synonymMap.getName())))
            .assertNext(Assert::assertTrue)
            .verifyComplete();
    }

    @Test
    public void existsReturnsTrueForExistingSynonymMapWithResponse() {
        SynonymMap synonymMap = createTestSynonymMap();

        StepVerifier
            .create(client.createSynonymMap(synonymMap)
                .then(client.synonymMapExistsWithResponse(synonymMap.getName(), generateRequestOptions())))
            .assertNext(res -> Assert.assertTrue(res.getValue()))
            .verifyComplete();
    }

    @Test
    public void existsReturnsFalseForNonExistingSynonymMap() {
        StepVerifier
            .create(client.synonymMapExists("thisSynonymMapDoesNotExist"))
            .assertNext(Assert::assertFalse)
            .verifyComplete();
    }

    @Test
    public void existsReturnsFalseForNonExistingSynonymMapWithResponse() {
        StepVerifier
            .create(client.synonymMapExistsWithResponse("thisSynonymMapDoesNotExist", generateRequestOptions()))
            .assertNext(res -> Assert.assertFalse(res.getValue()))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateSynonymMapIfNotExistsFailsOnExistingResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.createOrUpdateIfNotExistsFailsOnExistingResourceAsync(
            createOrUpdateSynonymMapAsyncFunc,
            newSynonymMapFunc,
            mutateSynonymMapFunc);
    }

    @Test
    public void deleteSynonymMapIfNotChangedWorksOnlyOnCurrentResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        String synonymName = "test-synonym";
        act.deleteIfNotChangedWorksOnlyOnCurrentResourceAsync(
            deleteSynonymMapAsyncFunc,
            newSynonymMapFunc,
            createOrUpdateSynonymMapAsyncFunc,
            mutateSynonymMapFunc,
            synonymName);
    }

    @Test
    public void deleteSynonymMapIfExistsWorksOnlyWhenResourceExists() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.deleteIfExistsWorksOnlyWhenResourceExistsAsync(
            deleteSynonymMapAsyncFunc,
            createOrUpdateSynonymMapAsyncFunc,
            newSynonymMapFunc,
            "test-synonym");
    }
}

