// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.templating;

import io.clientcore.annotation.processor.mocks.MockFiler;
import io.clientcore.annotation.processor.mocks.MockJavaFileObject;
import io.clientcore.annotation.processor.mocks.MockProcessingEnvironment;
import io.clientcore.annotation.processor.mocks.MockTemplateInput;
import io.clientcore.annotation.processor.models.TemplateInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Integration test to verify that all formatting improvements work together
 * to produce high-quality, checkstyle-compliant generated code.
 */
public class FormattingIntegrationTest {

    private JavaParserTemplateProcessor processor;
    private TemplateInput templateInput = new MockTemplateInput();
    private ProcessingEnvironment processingEnv;
    private MockJavaFileObject mockFileObject;

    @BeforeEach
    public void setUp() {
        processor = new JavaParserTemplateProcessor();
        mockFileObject = new MockJavaFileObject();
        Filer filer = new MockFiler(mockFileObject);
        processingEnv = new MockProcessingEnvironment(filer, null, null);
    }

    @Test
    public void testComprehensiveFormattingQuality() {
        // Process the template to generate code
        processor.process(templateInput, processingEnv);
        
        // Get the generated code content
        String generatedCode = mockFileObject.getContent();
        assertNotNull(generatedCode, "Generated code should not be null");
        assertFalse(generatedCode.trim().isEmpty(), "Generated code should not be empty");
        
        // Split into lines for detailed analysis
        String[] lines = generatedCode.split("\n");
        List<String> lineList = Arrays.asList(lines);
        
        // 1. Verify proper file structure
        verifyFileStructure(lineList);
        
        // 2. Verify formatting consistency
        verifyFormattingConsistency(lineList);
        
        // 3. Verify JavaDoc quality
        verifyJavaDocQuality(lineList);
        
        // 4. Verify checkstyle compliance
        verifyCheckstyleCompliance(lineList);
        
        // 5. Verify overall code quality
        verifyOverallQuality(generatedCode);
    }

    private void verifyFileStructure(List<String> lines) {
        // Verify copyright header is at the top
        assertTrue(lines.get(0).contains("Copyright (c) Microsoft Corporation"),
            "First line should be copyright header");
        assertTrue(lines.get(1).contains("Licensed under the MIT License"),
            "Second line should be license header");
        
        // Verify package declaration comes after headers
        boolean foundPackage = false;
        for (String line : lines) {
            if (line.startsWith("package ")) {
                foundPackage = true;
                break;
            }
        }
        assertTrue(foundPackage, "Should contain package declaration");
        
        // Verify imports section exists
        boolean foundImports = false;
        for (String line : lines) {
            if (line.startsWith("import ")) {
                foundImports = true;
                break;
            }
        }
        assertTrue(foundImports, "Should contain import statements");
    }

    private void verifyFormattingConsistency(List<String> lines) {
        // Check line length compliance
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            assertTrue(line.length() <= 120, 
                "Line " + (i + 1) + " exceeds 120 characters: " + line.length());
        }
        
        // Check indentation consistency (multiples of 4 spaces)
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                int leadingSpaces = 0;
                for (char c : line.toCharArray()) {
                    if (c == ' ') {
                        leadingSpaces++;
                    } else {
                        break;
                    }
                }
                assertTrue(leadingSpaces % 4 == 0, 
                    "Indentation should be multiple of 4 spaces: " + leadingSpaces + " in line: " + line);
            }
        }
    }

    private void verifyJavaDocQuality(List<String> lines) {
        boolean foundClassJavaDoc = false;
        boolean foundMethodJavaDoc = false;
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            
            // Check for class-level JavaDoc
            if (line.equals("/**") && i + 1 < lines.size()) {
                String nextLine = lines.get(i + 1).trim();
                if (nextLine.contains("Implementation of")) {
                    foundClassJavaDoc = true;
                    // Verify it mentions auto-generation
                    boolean foundAutoGeneration = false;
                    for (int j = i; j < Math.min(i + 10, lines.size()); j++) {
                        if (lines.get(j).contains("automatically generated")) {
                            foundAutoGeneration = true;
                            break;
                        }
                    }
                    assertTrue(foundAutoGeneration, "Class JavaDoc should mention auto-generation");
                }
            }
            
            // Check for method-level JavaDoc
            if (line.equals("/**") && i + 1 < lines.size()) {
                String nextLine = lines.get(i + 1).trim();
                if (nextLine.contains("Performs a")) {
                    foundMethodJavaDoc = true;
                }
            }
        }
        
        assertTrue(foundClassJavaDoc, "Should have class-level JavaDoc");
        assertTrue(foundMethodJavaDoc, "Should have method-level JavaDoc");
    }

    private void verifyCheckstyleCompliance(List<String> lines) {
        for (String line : lines) {
            String trimmed = line.trim();
            
            // Verify @param formatting (single space after parameter name)
            if (trimmed.startsWith("@param ")) {
                assertTrue(trimmed.matches("@param \\S+ .*"),
                    "JavaDoc @param should have exactly one space after parameter name: " + line);
            }
            
            // Verify @return formatting (single space before description)
            if (trimmed.startsWith("@return ")) {
                assertTrue(trimmed.matches("@return .*"),
                    "JavaDoc @return should have exactly one space before description: " + line);
                assertTrue(trimmed.length() > "@return ".length(),
                    "JavaDoc @return should have description: " + line);
            }
        }
    }

    private void verifyOverallQuality(String generatedCode) {
        // Verify no trailing whitespace
        String[] lines = generatedCode.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            assertFalse(line.endsWith(" ") || line.endsWith("\t"),
                "Line " + (i + 1) + " should not have trailing whitespace");
        }
        
        // Verify proper empty line usage (no consecutive empty lines)
        int consecutiveEmptyLines = 0;
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                consecutiveEmptyLines++;
                assertTrue(consecutiveEmptyLines <= 2, 
                    "Should not have more than 2 consecutive empty lines");
            } else {
                consecutiveEmptyLines = 0;
            }
        }
        
        // Verify the code compiles (basic syntax check)
        assertTrue(generatedCode.contains("public class"), "Should contain a public class");
        assertTrue(generatedCode.contains("public static"), "Should contain static factory method");
        
        // Count braces to ensure they're balanced
        long openBraces = generatedCode.chars().filter(ch -> ch == '{').count();
        long closeBraces = generatedCode.chars().filter(ch -> ch == '}').count();
        assertTrue(openBraces == closeBraces, "Braces should be balanced");
    }
}