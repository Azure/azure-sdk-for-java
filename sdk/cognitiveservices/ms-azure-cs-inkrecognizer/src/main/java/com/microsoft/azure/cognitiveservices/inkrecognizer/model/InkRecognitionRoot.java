// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.cognitiveservices.inkrecognizer.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The InkRecognitionRoot class is the root of the model result tree from the Ink Recognizer service. It can be
 * retrieved from the InkRecognitionResult class. The class can be used to retrieve details of the model results from
 * the service.
 * @author Microsoft
 * @version 1.0
 */
public class InkRecognitionRoot {

    private final HashMap<Long, InkRecognitionUnit> recognizedUnits = new HashMap<>();
    private final List<Long> wordList = new ArrayList<>();
    private final List<Long> recognizedDrawings = new ArrayList<>();

    public InkRecognitionRoot(JsonNode jsonRecognitionUnits) throws Exception {

        for (JsonNode jsonRecognitionUnit : jsonRecognitionUnits) {

            String category = jsonRecognitionUnit.get("category").asText();
            long id = jsonRecognitionUnit.get("id").asLong();

            switch (category) {

                case "inkWord":
                    InkWord word = new InkWord(jsonRecognitionUnit, this);
                    recognizedUnits.put(id, word);
                    wordList.add(id);
                    break;

                case "inkBullet":
                    InkBullet bullet = new InkBullet(jsonRecognitionUnit, this);
                    recognizedUnits.put(id, bullet);
                    break;

                case "line":
                    Line line = new Line(jsonRecognitionUnit, this);
                    recognizedUnits.put(id, line);
                    break;

                case "listItem":
                    InkList inkList = new InkList(jsonRecognitionUnit, this);
                    recognizedUnits.put(id, inkList);
                    break;

                case "paragraph":
                    Paragraph paragraph = new Paragraph(jsonRecognitionUnit, this);
                    recognizedUnits.put(id, paragraph);
                    break;

                case "writingRegion":
                    WritingRegion writingRegion = new WritingRegion(jsonRecognitionUnit, this);
                    recognizedUnits.put(id, writingRegion);
                    break;

                case "inkDrawing":
                    InkDrawing drawing = new InkDrawing(jsonRecognitionUnit, this);
                    recognizedUnits.put(id, drawing);
                    recognizedDrawings.add(id);
                    break;

                default:
                    InkRecognitionUnit unknownUnit = new InkRecognitionUnit(jsonRecognitionUnit, this);
                    recognizedUnits.put(id, unknownUnit);

            }

        }

    }

    /**
     * Retrieves all the ink word objects found in the tree returned by the Ink Recognizer service.
     * @return A collection of InkWord model units.
     */
    public Iterable<InkWord> inkWords() {
        List<InkWord> inkWords = new ArrayList<>();
        for (Long wordId : wordList) {
            InkRecognitionUnit recognizedUnit = (recognizedUnits.get(wordId));
            if (recognizedUnit instanceof InkWord) {
                inkWords.add((InkWord) recognizedUnit);
            }
        }
        return inkWords;
    }

    /**
     * Retrieves all the ink drawing objects found in the tree returned by the Ink Recognizer service.
     * @return A collection of InkDrawing objects.
     */
    public Iterable<InkDrawing> inkDrawings() {
        List<InkDrawing> inkDrawings = new ArrayList<>();
        for (Long drawingId : recognizedDrawings) {
            InkRecognitionUnit recognizedUnit = recognizedUnits.get(drawingId);
            if (recognizedUnit instanceof InkDrawing) {
                inkDrawings.add((InkDrawing) recognizedUnit);
            }
        }
        return inkDrawings;
    }

    /**
     * Retrieves all InkRecognitionUnit objects found (matching the kind specified) in the tree returned by the Ink
     * Recognizer service.
     * @param category - parameter specifies the category of the model units to return. If the there are no units that
     * match the requested category, an empty list is returned.
     * @return A collection of all the relevant InkRecognitionUnit objects.
     */
    public Iterable<InkRecognitionUnit> getRecognitionUnits(InkRecognitionUnitKind category) {
        List<InkRecognitionUnit> recognitionUnits = new ArrayList<>();
        for (InkRecognitionUnit unit : recognizedUnits.values()) {
            if (unit.kind() == category) {
                recognitionUnits.add(unit);
            }
        }
        return recognitionUnits;
    }

    /**
     * Retrieves all InkRecognitionUnit objects found in the tree returned by the Ink Recognizer service.
     * @return A collection of InkRecognitionUnit objects.
     */
    public Iterable<InkRecognitionUnit> recognitionUnits() {
        return recognizedUnits.values();
    }

    /**
     * This function attempts to find specified words in the list of recognized words returned by the service.
     * @param wordToSearch -  The word to search for.
     * @return A collection of InkWord objects with text that match the word used in the search request.
     */
    public Iterable<InkWord> findWord(String wordToSearch) {
        List<InkWord> inkWords = new ArrayList<>();
        for (Long wordId : wordList) {
            InkRecognitionUnit recognizedUnit = recognizedUnits.get(wordId);
            if (recognizedUnit instanceof InkWord) {
                InkWord inkWord = (InkWord) recognizedUnit;
                if (inkWord.recognizedText().equalsIgnoreCase(wordToSearch)) {
                    inkWords.add(inkWord);
                }
            }
        }
        return inkWords;
    }

    Iterable<InkRecognitionUnit> recognitionUnitsByIds(List<Long> unitIds) {
        if (unitIds == null) {
            return new ArrayList<>();
        } else {
            List<InkRecognitionUnit> recognitionUnits = new ArrayList<>();
            for (Long unitId : unitIds) {
                if (recognizedUnits.containsKey(unitId)) {
                    recognitionUnits.add(recognizedUnits.get(unitId));
                }
            }
            return recognitionUnits;
        }

    }

    InkRecognitionUnit recognitionUnitById(Long id) {
        return recognizedUnits.get(id);
    }

}
