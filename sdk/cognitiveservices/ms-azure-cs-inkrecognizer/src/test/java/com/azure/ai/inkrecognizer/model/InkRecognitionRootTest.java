package com.azure.ai.inkrecognizer.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class InkRecognitionRootTest {

    private JsonNode jsonInkRecognitionUnits;
    private InkRecognitionRoot root;
    private final int inkWordsCount = 11;
    private final int inkDrawingsCount = 2;
    private final int writingRegionsCount = 3;
    private final int paragraphsCount = 5;
    private final int linesCount = 6;
    private final int bulletsCount = 2;
    private final int inkListCount = 2;
    private final int unknownsCount = 1;

    @Before
    public void setUp() throws Exception {

        jsonInkRecognitionUnits = TestUtils.getJsonRecognitionUnits(TestUtils.ALL_INK_RECOGNITION_UNIT_KINDS_RESPONSE_FILE);
        root = new InkRecognitionRoot(jsonInkRecognitionUnits);

    }

    @Test
    public void inkWordsTest() {

        Iterator<InkWord> inkWords = root.inkWords().iterator();
        int i;
        for (i = 0; i < inkWordsCount && inkWords.hasNext(); ++i) {
            // Check for one of the fields as the rest fields are checked in the unit tests
            Assert.assertEquals(InkRecognitionUnitKind.INK_WORD, inkWords.next().kind());
        }
        Assert.assertEquals(inkWordsCount, i);
        Assert.assertFalse(inkWords.hasNext());

    }

    @Test
    public void inkDrawingsTest() {

        Iterator<InkDrawing> inkDrawings = root.inkDrawings().iterator();
        int i;
        for (i = 0; i < inkDrawingsCount && inkDrawings.hasNext(); ++i) {
            // Check for one of the fields as the rest fields are checked in the unit tests
            Assert.assertEquals(InkRecognitionUnitKind.INK_DRAWING, inkDrawings.next().kind());
        }
        Assert.assertEquals(inkDrawingsCount, i);
        Assert.assertFalse(inkDrawings.hasNext());

    }

    @Test
    public void getRecognitionUnitsTestForDrawings() {

        Iterator<InkRecognitionUnit> inkDrawings = root.getRecognitionUnits(InkRecognitionUnitKind.INK_DRAWING).iterator();
        int i;
        for (i = 0; i < inkDrawingsCount && inkDrawings.hasNext(); ++i) {
            // Check for one of the fields as the rest fields are checked in the unit tests
            Assert.assertEquals(InkRecognitionUnitKind.INK_DRAWING, inkDrawings.next().kind());
        }
        Assert.assertEquals(inkDrawingsCount, i);
        Assert.assertFalse(inkDrawings.hasNext());

    }

    @Test
    public void getRecognitionUnitsTestForWritingRegions() {

        Iterator<InkRecognitionUnit> writingRegions = root.getRecognitionUnits(InkRecognitionUnitKind.WRITING_REGION).iterator();
        int i;
        for (i = 0; i < writingRegionsCount && writingRegions.hasNext(); ++i) {
            // Check for one of the fields as the rest fields are checked in the unit tests
            Assert.assertEquals(InkRecognitionUnitKind.WRITING_REGION, writingRegions.next().kind());
        }
        Assert.assertEquals(writingRegionsCount, i);
        Assert.assertFalse(writingRegions.hasNext());

    }

    @Test
    public void getRecognitionUnitsTestForParagraphs() {

        Iterator<InkRecognitionUnit> paragraphs = root.getRecognitionUnits(InkRecognitionUnitKind.PARAGRAPH).iterator();
        int i;
        for (i = 0; i < paragraphsCount && paragraphs.hasNext(); ++i) {
            // Check for one of the fields as the rest fields are checked in the unit tests
            Assert.assertEquals(InkRecognitionUnitKind.PARAGRAPH, paragraphs.next().kind());
        }
        Assert.assertEquals(paragraphsCount, i);
        Assert.assertFalse(paragraphs.hasNext());

    }

    @Test
    public void getRecognitionUnitsTestForLines() {

        Iterator<InkRecognitionUnit> lines = root.getRecognitionUnits(InkRecognitionUnitKind.LINE).iterator();
        int i;
        for (i = 0; i < linesCount && lines.hasNext(); ++i) {
            // Check for one of the fields as the rest fields are checked in the unit tests
            Assert.assertEquals(InkRecognitionUnitKind.LINE, lines.next().kind());
        }
        Assert.assertEquals(linesCount, i);
        Assert.assertFalse(lines.hasNext());

    }

    @Test
    public void getRecognitionUnitsTestForInkWords() {

        Iterator<InkRecognitionUnit> inkWords = root.getRecognitionUnits(InkRecognitionUnitKind.INK_WORD).iterator();
        int i;
        for (i = 0; i < inkWordsCount && inkWords.hasNext(); ++i) {
            // Check for one of the fields as the rest fields are checked in the unit tests
            Assert.assertEquals(InkRecognitionUnitKind.INK_WORD, inkWords.next().kind());
        }
        Assert.assertEquals(inkWordsCount, i);
        Assert.assertFalse(inkWords.hasNext());

    }

    @Test
    public void getRecognitionUnitsTestForInkBullets() {

        Iterator<InkRecognitionUnit> bullets = root.getRecognitionUnits(InkRecognitionUnitKind.INK_BULLET).iterator();
        int i;
        for (i = 0; i < bulletsCount && bullets.hasNext(); ++i) {
            // Check for one of the fields as the rest fields are checked in the unit tests
            Assert.assertEquals(InkRecognitionUnitKind.INK_BULLET, bullets.next().kind());
        }
        Assert.assertEquals(bulletsCount, i);
        Assert.assertFalse(bullets.hasNext());

    }

    @Test
    public void getRecognitionUnitsTestForInkLists() {

        Iterator<InkRecognitionUnit> inkLists = root.getRecognitionUnits(InkRecognitionUnitKind.INK_LIST).iterator();
        int i;
        for (i = 0; i < inkListCount && inkLists.hasNext(); ++i) {
            // Check for one of the fields as the rest fields are checked in the unit tests
            Assert.assertEquals(InkRecognitionUnitKind.INK_LIST, inkLists.next().kind());
        }
        Assert.assertEquals(inkListCount, i);
        Assert.assertFalse(inkLists.hasNext());

    }

    @Test
    public void getRecognitionUnitsTestForUnknowns() {

        Iterator<InkRecognitionUnit> unknowns = root.getRecognitionUnits(InkRecognitionUnitKind.UNKNOWN).iterator();
        int i;
        for (i = 0; i < unknownsCount && unknowns.hasNext(); ++i) {
            // Check for one of the fields as the rest fields are checked in the unit tests
            Assert.assertEquals(InkRecognitionUnitKind.UNKNOWN, unknowns.next().kind());
        }
        Assert.assertEquals(unknownsCount, i);
        Assert.assertFalse(unknowns.hasNext());

    }

    @Test
    public void recognitionUnitsTest() {

        Iterator<InkRecognitionUnit> units = root.recognitionUnits().iterator();
        int actualRecognitionUnitsCount = 0;
        while (units.hasNext()) {
            units.next();
            actualRecognitionUnitsCount++;
        }
        int expectedRecognitionUnitsCount = bulletsCount
                + inkWordsCount
                + linesCount
                + paragraphsCount
                + writingRegionsCount
                + inkDrawingsCount
                + inkListCount
                + unknownsCount;
        Assert.assertEquals(expectedRecognitionUnitsCount, actualRecognitionUnitsCount);

    }

    @Test
    public void findWordTest() {

        String stringToSearch = "how";
        int stringHowOccurrence = 2;
        Iterator<InkWord> inkWords = root.findWord(stringToSearch).iterator();
        int i;
        for (i = 0; i < stringHowOccurrence && inkWords.hasNext(); ++i) {
            InkWord inkWord = inkWords.next();
            Assert.assertEquals(InkRecognitionUnitKind.INK_WORD, inkWord.kind());
            Assert.assertTrue(inkWord.recognizedText().equalsIgnoreCase(stringToSearch));
        }
        Assert.assertEquals(stringHowOccurrence, i);
        Assert.assertFalse(inkWords.hasNext());

    }

    // Tests for the other ink recognition unit kinds dependent on root being set
    @Test
    public void parentTest() {

        for (InkRecognitionUnit unit : root.recognitionUnits()) {
            InkRecognitionUnit parent = unit.parent();
            if (parent != null) {
                Iterator<InkRecognitionUnit> children = parent.children().iterator();
                boolean childLinkedToParent = false;
                while (children.hasNext()) {
                    if (children.next().id() == unit.id()) {
                        childLinkedToParent = true;
                    }
                }
                Assert.assertTrue(childLinkedToParent);
            }
        }

    }

    @Test
    public void childrenTest() {

        for (InkRecognitionUnit unit : root.recognitionUnits()) {
            for (InkRecognitionUnit inkRecognitionUnit : unit.children()) {
                Assert.assertEquals(inkRecognitionUnit.parent().id(), unit.id());
            }
        }

    }

    // Line
    @Test
    public void bulletTestForLine() {

        Iterator<InkRecognitionUnit> lines = root.getRecognitionUnits(InkRecognitionUnitKind.LINE).iterator();
        int actualBulletCount = 0;
        while (lines.hasNext()) {
            Line line = (Line) lines.next();
            InkBullet bullet = line.bullet();
            if (bullet != null) {
                actualBulletCount++;
            }
        }
        Assert.assertEquals(bulletsCount, actualBulletCount);

    }

    @Test
    public void wordTestForLine() {

        Iterator<InkRecognitionUnit> lines = root.getRecognitionUnits(InkRecognitionUnitKind.LINE).iterator();
        int actualInkWordCount = 0;
        while (lines.hasNext()) {
            Line line = (Line) lines.next();
            for (InkWord inkWord : line.words()) {
                actualInkWordCount++;
            }
        }
        Assert.assertEquals(inkWordsCount, actualInkWordCount);

    }

    // Ink List
    @Test
    public void recognizedTextTestForInkList() {

        Iterator<InkRecognitionUnit> inkLists = root.getRecognitionUnits(InkRecognitionUnitKind.INK_LIST).iterator();
        Set<String> expectedTexts = new TreeSet<>();
        expectedTexts.add("Hey");
        expectedTexts.add("How");
        Set<String> actualTexts = new TreeSet<>();
        while (inkLists.hasNext()) {
            actualTexts.add(((InkList) inkLists.next()).recognizedText());
        }
        Assert.assertEquals(expectedTexts, actualTexts);

    }

    // Paragraph
    @Test
    public void recognizedTextTestForParagraph() {

        Iterator<InkRecognitionUnit> paragraphs = root.getRecognitionUnits(InkRecognitionUnitKind.PARAGRAPH).iterator();
        Set<String> expectedTexts = new TreeSet<>();
        expectedTexts.add(". .\n");
        expectedTexts.add("Hey\nHow\n");
        expectedTexts.add("can there\n");
        expectedTexts.add("be a way\n");
        expectedTexts.add("how Sun\n");
        Set<String> actualTexts = new TreeSet<>();
        while (paragraphs.hasNext()) {
            actualTexts.add(((Paragraph) paragraphs.next()).recognizedText());
        }
        Assert.assertEquals(expectedTexts, actualTexts);

    }

    @Test
    public void linesTestForParagraph() {

        Iterator<InkRecognitionUnit> paragraphs = root.getRecognitionUnits(InkRecognitionUnitKind.PARAGRAPH).iterator();
        int actualLineCount = 0;
        while (paragraphs.hasNext()) {
            Paragraph paragraph = (Paragraph) paragraphs.next();
            for (Line line : paragraph.lines()) {
                actualLineCount++;
            }
        }
        // To account for the listItems
        Assert.assertEquals(linesCount - inkListCount, actualLineCount);

    }

    // Writing Region
    @Test
    public void recognizedTextTestForWritingRegion() {

        Iterator<InkRecognitionUnit> writingRegions = root.getRecognitionUnits(InkRecognitionUnitKind.WRITING_REGION).iterator();
        Set<String> expectedTexts = new TreeSet<>();
        expectedTexts.add("Hey\nHow\ncan there\nbe a way\n\n");
        expectedTexts.add("how Sun\n\n");
        expectedTexts.add(". .\n\n");
        Set<String> actualTexts = new TreeSet<>();
        while (writingRegions.hasNext()) {
            actualTexts.add(((WritingRegion) writingRegions.next()).recognizedText());
        }
        Assert.assertEquals(expectedTexts, actualTexts);

    }

    @Test
    public void paragraphsForWritingRegion() {

        Iterator<InkRecognitionUnit> writingRegions = root.getRecognitionUnits(InkRecognitionUnitKind.WRITING_REGION).iterator();
        int actualParagraphsCount = 0;
        while (writingRegions.hasNext()) {
            WritingRegion writingRegion = (WritingRegion) writingRegions.next();
            for (Paragraph paragraph : writingRegion.paragraphs()) {
                actualParagraphsCount++;
            }
        }
        Assert.assertEquals(paragraphsCount, actualParagraphsCount);

    }

    @Test(expected = Exception.class)
    public void missingCategoryInResponse() throws Exception {

        JsonNode malformedJsonInkRecognitionUnits = TestUtils.getJsonRecognitionUnits(TestUtils.MALFORMED_RESPONSE_MISSING_CATEGORY_FILE);
        InkRecognitionRoot root = new InkRecognitionRoot(malformedJsonInkRecognitionUnits);

    }

    @Test(expected = Exception.class)
    public void missingIdInResponse() throws Exception {

        JsonNode malformedJsonInkRecognitionUnits = TestUtils.getJsonRecognitionUnits(TestUtils.MALFORMED_RESPONSE_MISSING_ID_FILE);
        InkRecognitionRoot root = new InkRecognitionRoot(malformedJsonInkRecognitionUnits);

    }

    // Invalid object tests
    @Test(expected = Exception.class)
    public void malformedRequest() throws Exception {

        JsonNode malformedJsonInkRecognitionUnits = TestUtils.getJsonRecognitionUnits(TestUtils.MALFORMED_INK_RECOGNITION_UNITS_FILE);
        InkRecognitionRoot root = new InkRecognitionRoot(malformedJsonInkRecognitionUnits);

    }

    @Test(expected = Exception.class)
    public void nullRequest() throws Exception {

        InkRecognitionRoot root = new InkRecognitionRoot(null);

    }

    @After
    public void tearDown() {

        jsonInkRecognitionUnits = null;

    }

}