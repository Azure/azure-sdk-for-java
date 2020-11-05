// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.implementation.models.DocumentStatistics;
import com.azure.ai.textanalytics.implementation.models.ErrorCodeValue;
import com.azure.ai.textanalytics.implementation.models.ErrorResponseException;
import com.azure.ai.textanalytics.implementation.models.HealthcareResult;
import com.azure.ai.textanalytics.implementation.models.InnerError;
import com.azure.ai.textanalytics.implementation.models.InnerErrorCodeValue;
import com.azure.ai.textanalytics.implementation.models.LanguageInput;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageInput;
import com.azure.ai.textanalytics.implementation.models.RequestStatistics;
import com.azure.ai.textanalytics.implementation.models.State;
import com.azure.ai.textanalytics.implementation.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.EntityCategory;
import com.azure.ai.textanalytics.models.HealthcareEntity;
import com.azure.ai.textanalytics.models.HealthcareEntityCollection;
import com.azure.ai.textanalytics.models.HealthcareEntityLink;
import com.azure.ai.textanalytics.models.HealthcareEntityRelation;
import com.azure.ai.textanalytics.models.JobState;
import com.azure.ai.textanalytics.models.RecognizeHealthcareEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsErrorCode;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.ai.textanalytics.models.WarningCode;
import com.azure.ai.textanalytics.util.RecognizeHealthcareEntitiesResultCollection;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.implementation.models.State.CANCELLED;
import static com.azure.ai.textanalytics.implementation.models.State.CANCELLING;
import static com.azure.ai.textanalytics.implementation.models.State.FAILED;
import static com.azure.ai.textanalytics.implementation.models.State.NOTSTARTED;
import static com.azure.ai.textanalytics.implementation.models.State.PARTIALLY_COMPLETED;
import static com.azure.ai.textanalytics.implementation.models.State.RUNNING;
import static com.azure.ai.textanalytics.implementation.models.State.SUCCEEDED;

/**
 * Utility method class.
 */
public final class Utility {
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);
    // default time interval for polling
    public static final Duration DEFAULT_POLL_DURATION = Duration.ofSeconds(5);

    private Utility() {
    }

    /**
     * Verify that list of documents are not null or empty. Otherwise, throw exception.
     *
     * @param documents A list of documents.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     */
    public static void inputDocumentsValidation(Iterable<?> documents) {
        Objects.requireNonNull(documents, "'documents' cannot be null.");
        final Iterator<?> iterator = documents.iterator();
        if (!iterator.hasNext()) {
            throw new IllegalArgumentException("'documents' cannot be empty.");
        }
    }

    /**
     * Get a mock {@link HttpResponse} that only return status code 400.
     *
     * @param response A {@link SimpleResponse} with any type
     * @return A mock {@link HttpResponse} that only return status code 400.
     */
    public static HttpResponse getEmptyErrorIdHttpResponse(SimpleResponse<?> response) {
        return new HttpResponse(response.getRequest()) {
            @Override
            public int getStatusCode() {
                return 400;
            }

            @Override
            public String getHeaderValue(String s) {
                return null;
            }

            @Override
            public HttpHeaders getHeaders() {
                return null;
            }

            @Override
            public Flux<ByteBuffer> getBody() {
                return null;
            }

            @Override
            public Mono<byte[]> getBodyAsByteArray() {
                return null;
            }

            @Override
            public Mono<String> getBodyAsString() {
                return null;
            }

            @Override
            public Mono<String> getBodyAsString(Charset charset) {
                return null;
            }
        };
    }

    /**
     * Mapping a {@link ErrorResponseException} to {@link HttpResponseException} if exist. Otherwise, return
     * original {@link Throwable}.
     *
     * @param throwable A {@link Throwable}.
     * @return A {@link HttpResponseException} or the original throwable type.
     */
    public static Throwable mapToHttpResponseExceptionIfExist(Throwable throwable) {
        if (throwable instanceof ErrorResponseException) {
            ErrorResponseException errorException = (ErrorResponseException) throwable;
            return new HttpResponseException(errorException.getMessage(), errorException.getResponse(),
                toTextAnalyticsError(errorException.getValue().getError()));
        }
        return throwable;
    }

    /**
     * Given a list of documents will apply the indexing function to it and return the updated list.
     *
     * @param documents the inputs to apply the mapping function to.
     * @param mappingFunction the function which applies the index to the incoming input value.
     * @param <T> the type of items being returned in the list.
     * @return The list holding all the generic items combined.
     */
    public static <T> List<T> mapByIndex(Iterable<String> documents, BiFunction<String, String, T> mappingFunction) {
        Objects.requireNonNull(documents, "'documents' cannot be null.");
        AtomicInteger i = new AtomicInteger(0);
        List<T> result = new ArrayList<>();
        documents.forEach(document ->
            result.add(mappingFunction.apply(String.valueOf(i.getAndIncrement()), document))
        );
        return result;
    }

    /**
     * Convert {@link DocumentStatistics} to {@link TextDocumentStatistics}
     *
     * @param statistics the {@link DocumentStatistics} provided by the service.
     * @return the {@link TextDocumentStatistics} returned by the SDK.
     */
    public static TextDocumentStatistics toTextDocumentStatistics(DocumentStatistics statistics) {
        return new TextDocumentStatistics(statistics.getCharactersCount(), statistics.getTransactionsCount());
    }

    /**
     * Convert {@link RequestStatistics} to {@link TextDocumentBatchStatistics}
     *
     * @param statistics the {@link RequestStatistics} provided by the service.
     * @return the {@link TextDocumentBatchStatistics} returned by the SDK.
     */
    public static TextDocumentBatchStatistics toBatchStatistics(RequestStatistics statistics) {
        return new TextDocumentBatchStatistics(statistics.getDocumentsCount(), statistics.getValidDocumentsCount(),
            statistics.getErroneousDocumentsCount(), statistics.getTransactionsCount());
    }

    /**
     * Convert {@link TextAnalyticsError} to {@link com.azure.ai.textanalytics.models.TextAnalyticsError}
     * This function maps the service returned {@link TextAnalyticsError inner error} to the top level
     * {@link com.azure.ai.textanalytics.models.TextAnalyticsError error}, if inner error present.
     *
     * @param textAnalyticsError the {@link TextAnalyticsError} returned by the service.
     * @return the {@link com.azure.ai.textanalytics.models.TextAnalyticsError} returned by the SDK.
     */
    public static com.azure.ai.textanalytics.models.TextAnalyticsError toTextAnalyticsError(
        TextAnalyticsError textAnalyticsError) {

        final InnerError innerError = textAnalyticsError.getInnererror();

        if (innerError == null) {
            final ErrorCodeValue errorCodeValue = textAnalyticsError.getCode();
            return new com.azure.ai.textanalytics.models.TextAnalyticsError(
                TextAnalyticsErrorCode.fromString(errorCodeValue == null ? null : errorCodeValue.toString()),
                textAnalyticsError.getMessage(),
                textAnalyticsError.getTarget());
        }

        final InnerErrorCodeValue innerErrorCodeValue = innerError.getCode();
        return new com.azure.ai.textanalytics.models.TextAnalyticsError(
            TextAnalyticsErrorCode.fromString(innerErrorCodeValue == null ? null : innerErrorCodeValue.toString()),
            innerError.getMessage(),
            innerError.getTarget());
    }

    /**
     * Convert the incoming input {@link TextDocumentInput} to the service expected {@link MultiLanguageInput}.
     *
     * @param documents the user provided input in {@link TextDocumentInput}
     * @return the service required input {@link MultiLanguageInput}
     */
    public static List<MultiLanguageInput> toMultiLanguageInput(Iterable<TextDocumentInput> documents) {
        List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
        for (TextDocumentInput textDocumentInput : documents) {
            multiLanguageInputs.add(new MultiLanguageInput().setId(textDocumentInput.getId())
                .setText(textDocumentInput.getText()).setLanguage(textDocumentInput.getLanguage()));
        }
        return multiLanguageInputs;
    }

    /**
     * Convert the incoming input {@link com.azure.ai.textanalytics.models.TextAnalyticsError}
     * to a {@link TextAnalyticsException}.
     *
     * @param error the {@link com.azure.ai.textanalytics.models.TextAnalyticsError}.
     * @return the {@link TextAnalyticsException} to be thrown.
     */
    public static TextAnalyticsException toTextAnalyticsException(
        com.azure.ai.textanalytics.models.TextAnalyticsError error) {
        return new TextAnalyticsException(error.getMessage(), error.getErrorCode(), error.getTarget());
    }

    /**
     * Convert to a list of {@link LanguageInput} from {@link DetectLanguageInput}.
     *
     * @param documents The list of documents to detect languages for.
     *
     * @return a list of {@link LanguageInput}.
     */
    public static List<LanguageInput> toLanguageInput(Iterable<DetectLanguageInput> documents) {
        final List<LanguageInput> multiLanguageInputs = new ArrayList<>();
        documents.forEach(textDocumentInput -> multiLanguageInputs.add(new LanguageInput()
            .setId(textDocumentInput.getId())
            .setText(textDocumentInput.getText())
            .setCountryHint(textDocumentInput.getCountryHint())));
        return multiLanguageInputs;
    }

    /**
     * Extracts the result ID from the URL.
     *
     * @param operationLocation The URL specified in the 'Operation-Location' response header containing the
     * resultId used to track the progress and obtain the result of the analyze operation.
     *
     * @return The resultId used to track the progress.
     */
    public static String parseModelId(String operationLocation) {
        if (!CoreUtils.isNullOrEmpty(operationLocation)) {
            int lastIndex = operationLocation.lastIndexOf('/');
            if (lastIndex != -1) {
                return operationLocation.substring(lastIndex + 1);
            }
        }
        throw LOGGER.logExceptionAsError(
            new RuntimeException("Failed to parse operation header for result Id from: " + operationLocation));
    }

    /**
     * Extract the next pagination link which contains the request parameter values, into map,
     * such as '$skip=20' and '$top=2'.
     *
     * @param nextLink the next pagination link.
     *
     * @return A map that holds the request parameter value of next pagination link.
     */
    public static Map<String, Integer> parseNextLink(String nextLink) {
        if (!CoreUtils.isNullOrEmpty(nextLink)) {
            Map<String, Integer> parameterMap = new HashMap<>();
            String[] strings = nextLink.split("\\?", 2);
            String[] parameters = strings[1].split("&");
            for (String parameter : parameters) {
                String[] parameterPair = parameter.split("=");
                parameterMap.put(parameterPair[0], Integer.valueOf(parameterPair[1]));
            }
            return parameterMap;
        }
        return new HashMap<>();
        // TODO: solve this SpotBugs
        // [ERROR] Unchecked/unconfirmed cast from Throwable to RuntimeException of return value in
        // com.azure.ai.textanalytics.implementation.Utility.parseNextLink(String)
        // [com.azure.ai.textanalytics.implementation.Utility] At Utility.java:[line 302]
        // BC_UNCONFIRMED_CAST_OF_RETURN_VALUE
        //throw LOGGER.logThrowableAsError(
        //    new RuntimeException(String.format("Failed to parse nextLink for pagination, nextLink: %s%n",
        //        nextLink)));
    }

    /**
     * Transfer {@link HealthcareResult} into {@link RecognizeHealthcareEntitiesResultCollection}
     *
     * @param healthcareResult the service side raw data, HealthcareResult.
     *
     * @return the client side explored model, RecognizeHealthcareEntitiesResultCollection.
     */
    public static RecognizeHealthcareEntitiesResultCollection toRecognizeHealthcareEntitiesResultCollection(
        HealthcareResult healthcareResult) {
        // List of document results
        List<RecognizeHealthcareEntitiesResult> recognizeHealthcareEntitiesResults = new ArrayList<>();
        healthcareResult.getDocuments().forEach(
            documentEntities -> {
                final List<TextAnalyticsWarning> warnings = Optional.ofNullable(documentEntities.getWarnings())
                    .map(textAnalyticsWarnings -> textAnalyticsWarnings.stream().map(
                        textAnalyticsWarning -> new TextAnalyticsWarning(
                            Optional.ofNullable(textAnalyticsWarning.getCode())
                                .map(warningCodeValue -> WarningCode.fromString(warningCodeValue.toString()))
                                .orElse(null),
                            textAnalyticsWarning.getMessage())
                        ).collect(Collectors.toList())
                    ).orElse(new ArrayList<>());

                final List<HealthcareEntity> healthcareEntities = documentEntities.getEntities().stream().map(
                    entity -> {
                        final HealthcareEntity healthcareEntity = new HealthcareEntity();
                        HealthcareEntityPropertiesHelper.setText(healthcareEntity, entity.getText());
                        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity,
                            EntityCategory.fromString(entity.getCategory()));
                        HealthcareEntityPropertiesHelper.setSubcategory(healthcareEntity, entity.getSubcategory());
                        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity,
                            entity.getConfidenceScore());
                        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity, entity.getOffset());
                        HealthcareEntityPropertiesHelper.setNegated(healthcareEntity, entity.isNegated());
                        HealthcareEntityPropertiesHelper.setHealthcareEntityLinks(healthcareEntity,
                            entity.getLinks() == null ? null : entity.getLinks().stream()
                                .map(healthcareEntityLink -> {
                                    final HealthcareEntityLink healthcareEntityLinkOrigin = new HealthcareEntityLink();
                                    HealthcareEntityLinkPropertiesHelper.setDataSource(healthcareEntityLinkOrigin,
                                        healthcareEntityLink.getDataSource());
                                    HealthcareEntityLinkPropertiesHelper.setDataSourceId(healthcareEntityLinkOrigin,
                                        healthcareEntityLink.getId());
                                    return healthcareEntityLinkOrigin;
                                })
                                .collect(Collectors.toList()));
                        return healthcareEntity;
                    }).collect(Collectors.toList());

                final HealthcareEntityCollection healthcareEntityCollection = new HealthcareEntityCollection(
                    new IterableStream<>(healthcareEntities));
                HealthcareEntityCollectionPropertiesHelper.setWarnings(healthcareEntityCollection,
                    new IterableStream<>(warnings));
                HealthcareEntityCollectionPropertiesHelper.setEntityRelations(healthcareEntityCollection,
                    new IterableStream<>(documentEntities.getRelations() == null ? null
                        : documentEntities.getRelations()
                        .stream()
                        .map(healthcareRelation -> {
                            final HealthcareEntityRelation relation = new HealthcareEntityRelation();
                            HealthcareEntityRelationPropertiesHelper.setRelationType(relation,
                                healthcareRelation.getRelationType());
                            HealthcareEntityRelationPropertiesHelper.setBidirectional(relation,
                                healthcareRelation.isBidirectional());
                            HealthcareEntityRelationPropertiesHelper.setSourceLink(relation,
                                healthcareRelation.getSource());
                            HealthcareEntityRelationPropertiesHelper.setTargetLink(relation,
                                healthcareRelation.getTarget());
                            return relation;
                        })
                        .collect(Collectors.toList())));


                final RecognizeHealthcareEntitiesResult recognizeHealthcareEntitiesResult =
                    new RecognizeHealthcareEntitiesResult(
                        documentEntities.getId(),
                        documentEntities.getStatistics() == null ? null
                            : toTextDocumentStatistics(documentEntities.getStatistics()),
                        null);
                RecognizeHealthcareEntitiesResultPropertiesHelper.setEntities(recognizeHealthcareEntitiesResult,
                    healthcareEntityCollection);

                recognizeHealthcareEntitiesResults.add(recognizeHealthcareEntitiesResult);
            });
        // Document errors
        healthcareResult.getErrors().forEach(documentError ->
            recognizeHealthcareEntitiesResults.add(new RecognizeHealthcareEntitiesResult(
                documentError.getId(),
                null,
                toTextAnalyticsError(documentError.getError())))
        );

        final RecognizeHealthcareEntitiesResultCollection healthcareEntitiesResults =
            new RecognizeHealthcareEntitiesResultCollection(recognizeHealthcareEntitiesResults);
        RecognizeHealthcareEntitiesResultCollectionPropertiesHelper.setModelVersion(healthcareEntitiesResults,
            healthcareResult.getModelVersion());
        RecognizeHealthcareEntitiesResultCollectionPropertiesHelper.setStatistics(healthcareEntitiesResults,
            healthcareResult.getStatistics() == null ? null : toBatchStatistics(healthcareResult.getStatistics()));
        return healthcareEntitiesResults;
    }

    /**
     * Transfer {@link State} into {@link JobState}
     *
     * @param jobState the service side raw data, State.
     *
     * @return the client side explored model, JobState.
     */
    public static JobState toJobState(State jobState) {
        if (jobState == NOTSTARTED) {
            return JobState.CANCELLED;
        } else if (jobState == RUNNING) {
            return JobState.RUNNING;
        } else if (jobState == SUCCEEDED) {
            return JobState.SUCCEEDED;
        } else if (jobState == FAILED) {
            return JobState.FAILED;
        } else if (jobState == CANCELLED) {
            return JobState.CANCELLED;
        } else if (jobState == CANCELLING) {
            return JobState.CANCELLING;
        } else if (jobState == PARTIALLY_COMPLETED) {
            return JobState.PARTIALLY_COMPLETED;
        } else {
            throw new RuntimeException(String.format("job state, %s is not supported.", jobState));
        }
    }
}
