package com.azure.ai.inkrecognizer;

/**
 * The InkStroke interface represents an ink stroke (a collection of ink points from the time a user places the writing
 * instrument on the writing surface until the the instrument is lifted. Clients of the Ink Recognizer services are
 * expected to implement this interface on the data store used to store ink data so the InkRecognizer object can use it
 * to translate the ink to JSON for delivery to the Ink Recognizer service.
 * @author Microsoft
 * @version 1.0
 */
public interface InkStroke {

    /**
     * Retrieves the points contained in the strokes.
     * @return A list of points in the stroke.
     */
    Iterable<InkPoint> getInkPoints();

    /**
     * Retrieves the InkStrokeKind. The default is InkStrokeKind.UNKNOWN.
     * @return The StrokeKind that was assigned to the stroke.
     */
    InkStrokeKind getKind();

    /**
     * Retrieves the unique identifier for the stroke. This is required to ensure that there's no ambiguity when
     * processing the strokes.
     * @return The unique identifier for the stroke.
     */
    long getId();

    /**
     * The language assigned to the stroke. If the language returns an empty string, The language set on the
     * InkRecognizerClient object is used.
     * @return The language to use for ink model.
     */
    String getLanguage();

}