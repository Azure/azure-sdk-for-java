// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests;

import com.azure.ai.contentunderstanding.ContentUnderstandingAsyncClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.models.AnalysisContent;
import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.AudioVisualContent;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerConfig;
import com.azure.ai.contentunderstanding.models.ContentArrayField;
import com.azure.ai.contentunderstanding.models.ContentBooleanField;
import com.azure.ai.contentunderstanding.models.ContentDateField;
import com.azure.ai.contentunderstanding.models.ContentField;
import com.azure.ai.contentunderstanding.models.ContentIntegerField;
import com.azure.ai.contentunderstanding.models.ContentJsonField;
import com.azure.ai.contentunderstanding.models.ContentNumberField;
import com.azure.ai.contentunderstanding.models.ContentObjectField;
import com.azure.ai.contentunderstanding.models.ContentRange;
import com.azure.ai.contentunderstanding.models.ContentStringField;
import com.azure.ai.contentunderstanding.models.ContentTimeField;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.ai.contentunderstanding.models.DocumentFigure;
import com.azure.ai.contentunderstanding.models.KnowledgeSource;
import com.azure.ai.contentunderstanding.models.ProcessingLocation;
import com.azure.ai.contentunderstanding.models.TranscriptPhrase;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards source and JVM descriptors published in the official version 1.0.0 artifact.
 */
public class GaApiCompatibilityTest {

    @Test
    public void canonicalModelHierarchyRemainsAvailable() throws NoSuchMethodException {
        assertSame(AnalysisContent.class, DocumentContent.class.getSuperclass());
        assertSame(AnalysisContent.class, AudioVisualContent.class.getSuperclass());
        assertTrue(ContentField.class.isAssignableFrom(ContentStringField.class));
        assertFalse(Modifier.isAbstract(AnalysisContent.class.getModifiers()));
        assertFalse(Modifier.isAbstract(ContentField.class.getModifiers()));
        assertFalse(Modifier.isAbstract(DocumentFigure.class.getModifiers()));
        assertFalse(Modifier.isAbstract(KnowledgeSource.class.getModifiers()));

        assertProtected(AnalysisContent.class.getDeclaredConstructor(String.class));
        assertProtected(ContentField.class.getDeclaredConstructor());
        assertProtected(DocumentFigure.class.getDeclaredConstructor(String.class));
        assertTrue(Modifier.isPublic(KnowledgeSource.class.getDeclaredConstructor().getModifiers()));
    }

    @Test
    public void releasedModelMembersRemainAvailable() throws NoSuchMethodException {
        AnalysisInput input = new AnalysisInput();
        assertSame(input, input.setContentRange(ContentRange.pages(2, 3)));
        Method rawRangeGetter = AnalysisInput.class.getDeclaredMethod("getContentRange");
        assertPackagePrivate(rawRangeGetter);
        assertEquals(String.class, rawRangeGetter.getReturnType());
        assertPackagePrivate(AnalysisInput.class.getDeclaredMethod("setContentRange", String.class));
        assertEquals(AnalysisInput.class,
            AnalysisInput.class.getMethod("setContentRange", ContentRange.class).getReturnType());

        ContentAnalyzerConfig config = new ContentAnalyzerConfig().setOcrEnabled(true)
            .setLayoutEnabled(true)
            .setFigureDescriptionEnabled(true)
            .setFigureAnalysisEnabled(true)
            .setFormulaEnabled(true)
            .setFaceBlurringDisabled(true)
            .setSegmentEnabled(true);
        assertEquals(Boolean.TRUE, config.isOcrEnabled());
        assertEquals(Boolean.TRUE, config.isLayoutEnabled());
        assertEquals(Boolean.TRUE, config.isFigureDescriptionEnabled());
        assertEquals(Boolean.TRUE, config.isFigureAnalysisEnabled());
        assertEquals(Boolean.TRUE, config.isFormulaEnabled());
        assertEquals(Boolean.TRUE, config.isFaceBlurringDisabled());
        assertEquals(Boolean.TRUE, config.isSegmentEnabled());

        assertEquals(String.class, ContentAnalyzerAnalyzeOperationStatus.class.getMethod("getId").getReturnType());
        assertEquals(AnalysisResult.class,
            ContentAnalyzerAnalyzeOperationStatus.class.getMethod("getResult").getReturnType());
    }

    @Test
    public void releasedFieldDescriptorsRemainAvailable() throws NoSuchMethodException {
        assertEquals(String.class, ContentStringField.class.getMethod("getValue").getReturnType());
        assertEquals(LocalDate.class, ContentDateField.class.getMethod("getValue").getReturnType());
        assertEquals(String.class, ContentTimeField.class.getMethod("getValue").getReturnType());
        assertEquals(Double.class, ContentNumberField.class.getMethod("getValue").getReturnType());
        assertEquals(Long.class, ContentIntegerField.class.getMethod("getValue").getReturnType());
        assertEquals(Boolean.class, ContentBooleanField.class.getMethod("getValue").getReturnType());
        assertEquals(List.class, ContentArrayField.class.getMethod("getValue").getReturnType());
        assertEquals(Map.class, ContentObjectField.class.getMethod("getValue").getReturnType());
        assertEquals(BinaryData.class, ContentJsonField.class.getMethod("getValue").getReturnType());

        Method rawSourceGetter = ContentField.class.getDeclaredMethod("getSource");
        assertPackagePrivate(rawSourceGetter);
        assertEquals(String.class, rawSourceGetter.getReturnType());
        assertEquals(List.class, ContentField.class.getMethod("getSources").getReturnType());

        assertPackagePrivate(AudioVisualContent.class.getDeclaredMethod("getStartTimeMs"));
        assertPackagePrivate(AudioVisualContent.class.getDeclaredMethod("getKeyFrameTimesMs"));
        assertPackagePrivate(TranscriptPhrase.class.getDeclaredMethod("getStartTimeMs"));
    }

    @Test
    public void releasedClientDescriptorsRemainAvailable() throws NoSuchMethodException {
        assertClientDescriptors(ContentUnderstandingClient.class, SyncPoller.class);
        assertClientDescriptors(ContentUnderstandingAsyncClient.class, PollerFlux.class);
    }

    @Test
    public void releasedNullRangeCallsRemainUnambiguous() {
        ContentUnderstandingClient client = null;
        ContentUnderstandingAsyncClient asyncClient = null;

        assertThrows(NullPointerException.class, () -> client.beginAnalyzeBinary("analyzer", null, null, null, null));
        assertThrows(NullPointerException.class,
            () -> asyncClient.beginAnalyzeBinary("analyzer", null, null, null, null));
    }

    private static void assertClientDescriptors(Class<?> clientType, Class<?> pollerType) throws NoSuchMethodException {
        assertPackagePrivate(clientType.getDeclaredMethod("beginAnalyze", String.class, List.class, String.class,
            Map.class, ProcessingLocation.class));
        assertPackagePrivate(clientType.getDeclaredMethod("beginAnalyze", String.class, List.class, String.class));
        assertPackagePrivate(clientType.getDeclaredMethod("beginAnalyzeBinary", String.class, BinaryData.class,
            String.class, String.class, String.class, ProcessingLocation.class));
        assertPackagePrivate(clientType.getDeclaredMethod("beginAnalyzeBinary", String.class, BinaryData.class,
            String.class, String.class));

        assertEquals(pollerType, clientType.getMethod("beginAnalyze", String.class, List.class).getReturnType());
        assertEquals(pollerType,
            clientType.getMethod("beginAnalyze", String.class, List.class, Map.class, ProcessingLocation.class)
                .getReturnType());
        assertEquals(pollerType,
            clientType.getMethod("beginAnalyzeBinary", String.class, BinaryData.class).getReturnType());
        assertEquals(pollerType,
            clientType
                .getMethod("beginAnalyzeBinary", String.class, BinaryData.class, ContentRange.class, String.class,
                    ProcessingLocation.class)
                .getReturnType());
    }

    private static void assertPackagePrivate(Method method) {
        assertFalse(Modifier.isPublic(method.getModifiers()));
        assertFalse(Modifier.isProtected(method.getModifiers()));
        assertFalse(Modifier.isPrivate(method.getModifiers()));
    }

    private static void assertProtected(java.lang.reflect.Constructor<?> constructor) {
        assertTrue(Modifier.isProtected(constructor.getModifiers()));
    }
}
