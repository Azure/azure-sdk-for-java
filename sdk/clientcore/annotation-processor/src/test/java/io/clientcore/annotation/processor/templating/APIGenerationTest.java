// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.templating;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.annotation.processor.models.Substitution;
import io.clientcore.annotation.processor.models.TemplateInput;
import io.clientcore.core.http.models.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * This class tests the methods generated from the provided ServiceInterface Template.
 */
public class APIGenerationTest {

    private JavaParserTemplateProcessor processor;
    private TemplateInput templateInput;

    @BeforeEach
    public void setUp() {
        processor = new JavaParserTemplateProcessor();
        templateInput = new TemplateInput();
    }

    @Test
    public void testPublicAPIUserMethodGeneration() {
        //@HttpRequestInformation(
        //        method = HttpMethod.GET,
        //        path = "/users/{userId}",
        //        expectedStatusCodes = {200}
        //    )
        //    User getUser(@PathParam("userId") String userId);
        HttpRequestContext getUserMethodContext = new HttpRequestContext();

        getUserMethodContext.setHttpMethod(HttpMethod.GET);
        getUserMethodContext.setPath("/users/{userId}");
        getUserMethodContext.setExpectedStatusCodes(new int[] { 200 });
        getUserMethodContext.setMethodName("getUser");
        getUserMethodContext.setMethodReturnType("User");
        getUserMethodContext.addSubstitution(new Substitution("String", "userId", false));
        getUserMethodContext
            .setBody(new HttpRequestContext.Body("multipart/form-data", "BinaryData", "audioTranscriptionOptions"));
        templateInput.setHttpRequestContexts(Collections.singletonList(getUserMethodContext));

        MethodDeclaration getUserMethodGenerationSpec = new MethodDeclaration();
        processor.configurePublicMethod(getUserMethodGenerationSpec, getUserMethodContext);
        assertEquals("getUser", getUserMethodGenerationSpec.getNameAsString());
        assertEquals("User", getUserMethodGenerationSpec.getTypeAsString());
        // assert code block contains the expected method body
        StringBuilder actual = new StringBuilder();
        for (Statement statement : getUserMethodGenerationSpec.getBody().get().getStatements()) {
            actual.append(statement.toString());
        }

        assertEquals("return getUser();", actual.toString());
    }
}
