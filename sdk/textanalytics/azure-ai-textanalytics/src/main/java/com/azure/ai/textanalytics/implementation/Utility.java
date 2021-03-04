// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.implementation.models.Association;
import com.azure.ai.textanalytics.implementation.models.Certainty;
import com.azure.ai.textanalytics.implementation.models.Conditionality;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.DocumentKeyPhrases;
import com.azure.ai.textanalytics.implementation.models.DocumentStatistics;
import com.azure.ai.textanalytics.implementation.models.EntitiesResult;
import com.azure.ai.textanalytics.implementation.models.ErrorCodeValue;
import com.azure.ai.textanalytics.implementation.models.ErrorResponse;
import com.azure.ai.textanalytics.implementation.models.ErrorResponseException;
import com.azure.ai.textanalytics.implementation.models.HealthcareAssertion;
import com.azure.ai.textanalytics.implementation.models.HealthcareResult;
import com.azure.ai.textanalytics.implementation.models.InnerError;
import com.azure.ai.textanalytics.implementation.models.InnerErrorCodeValue;
import com.azure.ai.textanalytics.implementation.models.KeyPhraseResult;
import com.azure.ai.textanalytics.implementation.models.LanguageInput;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageInput;
import com.azure.ai.textanalytics.implementation.models.PiiResult;
import com.azure.ai.textanalytics.implementation.models.RelationType;
import com.azure.ai.textanalytics.implementation.models.RequestStatistics;
import com.azure.ai.textanalytics.implementation.models.StringIndexType;
import com.azure.ai.textanalytics.implementation.models.TextAnalyticsError;
import com.azure.ai.textanalytics.implementation.models.WarningCodeValue;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesResult;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.CategorizedEntityCollection;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.EntityAssociation;
import com.azure.ai.textanalytics.models.EntityCategory;
import com.azure.ai.textanalytics.models.EntityCertainty;
import com.azure.ai.textanalytics.models.EntityConditionality;
import com.azure.ai.textanalytics.models.EntityDataSource;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.HealthcareEntity;
import com.azure.ai.textanalytics.models.HealthcareEntityAssertion;
import com.azure.ai.textanalytics.models.HealthcareEntityRelation;
import com.azure.ai.textanalytics.models.HealthcareEntityRelationRole;
import com.azure.ai.textanalytics.models.HealthcareEntityRelationType;
import com.azure.ai.textanalytics.models.KeyPhrasesCollection;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsErrorCode;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.ai.textanalytics.models.WarningCode;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Utility method class.
 */
public final class Utility {
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);
    // default time interval for polling
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(5);

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
     * Mapping a {@link ErrorResponseException} to {@link HttpResponseException} if exist. Otherwise, return
     * original {@link Throwable}.
     *
     * @param throwable A {@link Throwable}.
     * @return A {@link HttpResponseException} or the original throwable type.
     */
    public static Throwable mapToHttpResponseExceptionIfExists(Throwable throwable) {
        if (throwable instanceof ErrorResponseException) {
            ErrorResponseException errorException = (ErrorResponseException) throwable;
            final ErrorResponse errorResponse = errorException.getValue();
            com.azure.ai.textanalytics.models.TextAnalyticsError textAnalyticsError = null;
            if (errorResponse != null && errorResponse.getError() != null) {
                textAnalyticsError = toTextAnalyticsError(errorResponse.getError());
            }
            return new HttpResponseException(errorException.getMessage(), errorException.getResponse(),
                textAnalyticsError);
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
     * Extracts the operation ID from the 'operation-location' URL. An example of 'operation-location' is
     * https://[...]/analyze/jobs/aaa11111-a111-a111-a1111-a12345678901
     *
     * @param operationLocation The URL specified in the 'Operation-Location' response header containing the
     * operation ID used to track the progress and obtain the ID of the analyze operation.
     *
     * @return The operation ID that tracks the long running operation progress.
     */
    public static String parseOperationId(String operationLocation) {
        if (!CoreUtils.isNullOrEmpty(operationLocation)) {
            int lastIndex = operationLocation.lastIndexOf('/');
            if (lastIndex != -1) {
                return operationLocation.substring(lastIndex + 1);
            }
        }
        throw LOGGER.logExceptionAsError(
            new RuntimeException("Failed to parse operation header for operation Id from: " + operationLocation));
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

    public static RecognizeEntitiesResultCollection toRecognizeEntitiesResultCollectionResponse(
        final EntitiesResult entitiesResult) {
        // List of documents results
        List<RecognizeEntitiesResult> recognizeEntitiesResults = new ArrayList<>();
        entitiesResult.getDocuments().forEach(documentEntities ->
            recognizeEntitiesResults.add(new RecognizeEntitiesResult(
                documentEntities.getId(),
                documentEntities.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentEntities.getStatistics()),
                null,
                new CategorizedEntityCollection(
                    new IterableStream<>(documentEntities.getEntities().stream().map(entity -> {
                        final CategorizedEntity categorizedEntity = new CategorizedEntity(entity.getText(),
                            EntityCategory.fromString(entity.getCategory()), entity.getSubcategory(),
                            entity.getConfidenceScore(), entity.getOffset());
                        CategorizedEntityPropertiesHelper.setLength(categorizedEntity, entity.getLength());
                        return categorizedEntity;
                    }).collect(Collectors.toList())),
                    new IterableStream<>(documentEntities.getWarnings().stream()
                        .map(warning -> {
                            final WarningCodeValue warningCodeValue = warning.getCode();
                            return new TextAnalyticsWarning(
                                WarningCode.fromString(warningCodeValue == null ? null : warningCodeValue.toString()),
                                warning.getMessage());
                        }).collect(Collectors.toList())))
            )));
        // Document errors
        for (DocumentError documentError : entitiesResult.getErrors()) {
            recognizeEntitiesResults.add(new RecognizeEntitiesResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }

        return new RecognizeEntitiesResultCollection(recognizeEntitiesResults, entitiesResult.getModelVersion(),
            entitiesResult.getStatistics() == null ? null : toBatchStatistics(entitiesResult.getStatistics()));
    }

    public static RecognizePiiEntitiesResultCollection toRecognizePiiEntitiesResultCollection(
        final PiiResult piiEntitiesResult) {
        // List of documents results
        final List<RecognizePiiEntitiesResult> recognizeEntitiesResults = new ArrayList<>();
        piiEntitiesResult.getDocuments().forEach(documentEntities -> {
            // Pii entities list
            final List<PiiEntity> piiEntities = documentEntities.getEntities().stream().map(entity ->
                new PiiEntity(entity.getText(), EntityCategory.fromString(entity.getCategory()),
                    entity.getSubcategory(), entity.getConfidenceScore(), entity.getOffset()))
                .collect(Collectors.toList());
            // Warnings
            final List<TextAnalyticsWarning> warnings = documentEntities.getWarnings().stream()
                .map(warning -> {
                    final WarningCodeValue warningCodeValue = warning.getCode();
                    return new TextAnalyticsWarning(
                        WarningCode.fromString(warningCodeValue == null ? null : warningCodeValue.toString()),
                        warning.getMessage());
                }).collect(Collectors.toList());

            recognizeEntitiesResults.add(new RecognizePiiEntitiesResult(
                documentEntities.getId(),
                documentEntities.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentEntities.getStatistics()),
                null,
                new PiiEntityCollection(new IterableStream<>(piiEntities), documentEntities.getRedactedText(),
                    new IterableStream<>(warnings))
            ));
        });
        // Document errors
        for (DocumentError documentError : piiEntitiesResult.getErrors()) {
            recognizeEntitiesResults.add(new RecognizePiiEntitiesResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }

        return new RecognizePiiEntitiesResultCollection(recognizeEntitiesResults, piiEntitiesResult.getModelVersion(),
            piiEntitiesResult.getStatistics() == null ? null : toBatchStatistics(piiEntitiesResult.getStatistics()));
    }

    public static ExtractKeyPhrasesResultCollection toExtractKeyPhrasesResultCollection(
        final KeyPhraseResult keyPhraseResult) {
        // List of documents results
        final List<ExtractKeyPhraseResult> keyPhraseResultList = new ArrayList<>();
        for (DocumentKeyPhrases documentKeyPhrases : keyPhraseResult.getDocuments()) {
            final String documentId = documentKeyPhrases.getId();
            keyPhraseResultList.add(new ExtractKeyPhraseResult(
                documentId,
                documentKeyPhrases.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentKeyPhrases.getStatistics()), null,
                new KeyPhrasesCollection(
                    new IterableStream<>(documentKeyPhrases.getKeyPhrases()),
                    new IterableStream<>(documentKeyPhrases.getWarnings().stream().map(warning -> {
                        final WarningCodeValue warningCodeValue = warning.getCode();
                        return new TextAnalyticsWarning(
                            WarningCode.fromString(warningCodeValue == null ? null : warningCodeValue.toString()),
                            warning.getMessage());
                    }).collect(Collectors.toList())))));
        }
        // Document errors
        for (DocumentError documentError : keyPhraseResult.getErrors()) {
            keyPhraseResultList.add(new ExtractKeyPhraseResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }

        return new ExtractKeyPhrasesResultCollection(keyPhraseResultList, keyPhraseResult.getModelVersion(),
            keyPhraseResult.getStatistics() == null ? null : toBatchStatistics(keyPhraseResult.getStatistics()));
    }

    /**
     * Transfer {@link HealthcareResult} into {@link IterableStream} of {@link AnalyzeHealthcareEntitiesResult}.
     *
     * @param healthcareResult the service side raw data, HealthcareResult.
     *
     * @return the client side explored model, RecognizeHealthcareEntitiesResultCollection.
     */
    public static IterableStream<AnalyzeHealthcareEntitiesResult> toRecognizeHealthcareEntitiesResults(
        HealthcareResult healthcareResult) {
        // List of document results
        List<AnalyzeHealthcareEntitiesResult> analyzeHealthcareEntitiesResults = new ArrayList<>();
        healthcareResult.getDocuments().forEach(
            documentEntities -> {
                final AnalyzeHealthcareEntitiesResult analyzeHealthcareEntitiesResult =
                    new AnalyzeHealthcareEntitiesResult(
                        documentEntities.getId(),
                        documentEntities.getStatistics() == null ? null
                            : toTextDocumentStatistics(documentEntities.getStatistics()),
                        null);
                // Warnings
                final List<TextAnalyticsWarning> warnings = documentEntities.getWarnings().stream().map(
                    textAnalyticsWarning -> new TextAnalyticsWarning(
                        Optional.ofNullable(textAnalyticsWarning.getCode())
                            .map(warningCodeValue -> WarningCode.fromString(warningCodeValue.toString()))
                            .orElse(null),
                        textAnalyticsWarning.getMessage())
                ).collect(Collectors.toList());
                AnalyzeHealthcareEntitiesResultPropertiesHelper.setWarnings(analyzeHealthcareEntitiesResult,
                    IterableStream.of(warnings));
                // Healthcare entities
                final List<HealthcareEntity> healthcareEntities = documentEntities.getEntities().stream().map(
                    entity -> {
                        final HealthcareEntity healthcareEntity = new HealthcareEntity();
                        HealthcareEntityPropertiesHelper.setText(healthcareEntity, entity.getText());
                        HealthcareEntityPropertiesHelper.setNormalizedText(healthcareEntity, entity.getName());
                        HealthcareEntityPropertiesHelper.setCategory(healthcareEntity, entity.getCategory());
                        HealthcareEntityPropertiesHelper.setConfidenceScore(healthcareEntity,
                            entity.getConfidenceScore());
                        HealthcareEntityPropertiesHelper.setOffset(healthcareEntity, entity.getOffset());
                        HealthcareEntityPropertiesHelper.setLength(healthcareEntity, entity.getLength());
                        final List<EntityDataSource> entityDataSources =
                            Optional.ofNullable(entity.getLinks()).map(
                                links -> links.stream().map(
                                    link -> {
                                        final EntityDataSource dataSource = new EntityDataSource();
                                        EntityDataSourcePropertiesHelper.setName(dataSource, link.getDataSource());
                                        EntityDataSourcePropertiesHelper.setEntityId(dataSource, link.getId());
                                        return dataSource;
                                    }
                                ).collect(Collectors.toList()))
                                .orElse(new ArrayList<>());

                        HealthcareEntityPropertiesHelper.setDataSources(healthcareEntity,
                            IterableStream.of(entityDataSources));

                        final HealthcareAssertion assertion = entity.getAssertion();
                        if (assertion != null) {
                            HealthcareEntityPropertiesHelper.setAssertion(healthcareEntity,
                                toHealthcareEntityAssertion(assertion));
                        }
                        return healthcareEntity;
                    }).collect(Collectors.toList());
                AnalyzeHealthcareEntitiesResultPropertiesHelper.setEntities(analyzeHealthcareEntitiesResult,
                    IterableStream.of(healthcareEntities));

                // Healthcare Entity relations
                final List<HealthcareEntityRelation> healthcareEntityRelations =
                    documentEntities.getRelations().stream().map(
                        healthcareRelation -> {
                            final HealthcareEntityRelation entityRelation = new HealthcareEntityRelation();
                            // Healthcare relation type
                            final RelationType relationType = healthcareRelation.getRelationType();
                            if (relationType != null) {
                                HealthcareEntityRelationPropertiesHelper.setRelationType(entityRelation,
                                    HealthcareEntityRelationType.fromString(relationType.toString()));
                            }
                            // Healthcare entity relation roles
                            final List<HealthcareEntityRelationRole> relationRoles =
                                healthcareRelation.getEntities().stream().map(
                                    relationEntity -> {
                                        final HealthcareEntityRelationRole relationRole =
                                            new HealthcareEntityRelationRole();
                                        HealthcareEntityRelationRolePropertiesHelper.setName(relationRole,
                                            relationEntity.getRole());
                                        HealthcareEntityRelationRolePropertiesHelper.setEntity(relationRole,
                                            healthcareEntities.get(getHealthcareEntityIndex(relationEntity.getRef())));
                                        return relationRole;
                                    }).collect(Collectors.toList());
                            HealthcareEntityRelationPropertiesHelper.setRoles(entityRelation,
                                IterableStream.of(relationRoles));

                            return entityRelation;
                        }).collect(Collectors.toList());
                AnalyzeHealthcareEntitiesResultPropertiesHelper.setEntityRelations(analyzeHealthcareEntitiesResult,
                    IterableStream.of(healthcareEntityRelations));

                analyzeHealthcareEntitiesResults.add(analyzeHealthcareEntitiesResult);
            });
        // Document errors
        healthcareResult.getErrors().forEach(documentError ->
            analyzeHealthcareEntitiesResults.add(new AnalyzeHealthcareEntitiesResult(
                documentError.getId(),
                null,
                toTextAnalyticsError(documentError.getError())))
        );
        return IterableStream.of(analyzeHealthcareEntitiesResults);
    }

    public static HealthcareEntityAssertion toHealthcareEntityAssertion(HealthcareAssertion healthcareAssertion) {
        final Association association = healthcareAssertion.getAssociation();
        final Certainty certainty = healthcareAssertion.getCertainty();
        final Conditionality conditionality = healthcareAssertion.getConditionality();

        final HealthcareEntityAssertion entityAssertion = new HealthcareEntityAssertion();
        if (association != null) {
            HealthcareEntityAssertionPropertiesHelper.setAssociation(entityAssertion,
                EntityAssociation.fromString(association.toString()));
        }
        if (certainty != null) {
            HealthcareEntityAssertionPropertiesHelper.setCertainty(entityAssertion,
                toCertainty(certainty));
        }
        if (conditionality != null) {
            HealthcareEntityAssertionPropertiesHelper.setConditionality(entityAssertion,
                toConditionality(conditionality));
        }
        return entityAssertion;
    }

    private static EntityCertainty toCertainty(Certainty certainty) {
        EntityCertainty entityCertainty1 = null;
        switch (certainty) {
            case POSITIVE:
                entityCertainty1 = EntityCertainty.POSITIVE;
                break;
            case POSITIVE_POSSIBLE:
                entityCertainty1 = EntityCertainty.POSITIVE_POSSIBLE;
                break;
            case NEUTRAL_POSSIBLE:
                entityCertainty1 = EntityCertainty.NEUTRAL_POSSIBLE;
                break;
            case NEGATIVE_POSSIBLE:
                entityCertainty1 = EntityCertainty.NEGATIVE_POSSIBLE;
                break;
            case NEGATIVE:
                entityCertainty1 = EntityCertainty.NEGATIVE;
                break;
            default:
                break;
        }
        return entityCertainty1;
    }

    private static EntityConditionality toConditionality(Conditionality conditionality) {
        EntityConditionality conditionality1 = null;
        switch (conditionality) {
            case HYPOTHETICAL:
                conditionality1 = EntityConditionality.HYPOTHETICAL;
                break;
            case CONDITIONAL:
                conditionality1 = EntityConditionality.CONDITIONAL;
                break;
            default:
                break;
        }
        return conditionality1;
    }

    /**
     * Helper function that parse healthcare entity index from the given entity reference string.
     * The entity reference format is "#/results/documents/0/entities/3".
     *
     * @param entityReference the given healthcare entity reference string.
     *
     * @return the healthcare entity index.
     */
    private static Integer getHealthcareEntityIndex(String entityReference) {
        if (!CoreUtils.isNullOrEmpty(entityReference)) {
            int lastIndex = entityReference.lastIndexOf('/');
            if (lastIndex != -1) {
                return Integer.parseInt(entityReference.substring(lastIndex + 1));
            }
        }
        throw LOGGER.logExceptionAsError(
            new RuntimeException("Failed to parse healthcare entity index from: " + entityReference));
    }

    /**
     * Transfer {@link com.azure.ai.textanalytics.models.StringIndexType} into auto-generated {@link StringIndexType}.
     * If value is null, use the default type for java, UTF16CODE_UNIT.
     *
     * @param stringIndexType The public explored StringIndexType.
     *
     * @return The autogenerated internally used StringIndexType.
     */
    public static StringIndexType getNonNullStringIndexType(
        com.azure.ai.textanalytics.models.StringIndexType stringIndexType) {
        return stringIndexType == null ? StringIndexType.UTF16CODE_UNIT
                   : StringIndexType.fromString(stringIndexType.toString());
    }

    /**
     * Get the non-null {@link Context}. The default value is {@link Context#NONE}.
     *
     * @param context It offers a means of passing arbitrary data (key-value pairs) to pipeline policies.
     * Most applications do not need to pass arbitrary data to the pipeline and can pass Context.NONE or null.
     *
     * @return The Context.
     */
    public static Context getNotNullContext(Context context) {
        return context == null ? Context.NONE : context;
    }

    /**
     * Helper function which retrieves the size of an {@link Iterable}.
     *
     * @param documents The iterable of documents.
     * @return Count of documents in the iterable.
     */
    public static int getDocumentCount(Iterable<?> documents) {
        if (documents instanceof Collection) {
            return ((Collection<?>) documents).size();
        } else {
            final int[] count = new int[] { 0 };
            documents.forEach(ignored -> count[0] += 1);
            return count[0];
        }
    }
}
