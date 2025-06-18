// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import io.clientcore.annotation.processor.exceptions.MissingSubstitutionException;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.annotation.processor.models.Substitution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the PathBuilder class.
 */
public class PathBuilderTest {

    @Test
    public void buildsPathWithHostSubstitution() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        String result = PathBuilder.buildPath("https://{endpoint}/keys", context);
        assertEquals("\"https://\" + myEndpoint + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithPathSubstitution() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path1", "myPath"));
        String result = PathBuilder.buildPath("https://{endpoint}/keys/{path1}", context);
        assertEquals("\"https://\" + myEndpoint + \"/keys/\" + myPath", result);
    }

    @Test
    public void buildsPathWithEmptySubstitutions() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));

        try {
            PathBuilder.buildPath("https://{endpoint}/keys/{path1}", context);
        } catch (MissingSubstitutionException e) {
            assertEquals("Could not find substitution for 'path1' in method 'null'", e.getMessage());
        }
    }

    @Test
    public void buildsPathWithNullSubstitutions() {
        try {
            PathBuilder.buildPath("https://{endpoint}/keys/{path1}", null);
        } catch (NullPointerException e) {
            assertEquals("method cannot be null", e.getMessage());
        }
    }

    @Test
    public void buildsPathWithMultipleSubstitutions() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path1", "myPath"));
        context.addSubstitution(new Substitution("path2", "myPath2"));
        String result = PathBuilder.buildPath("https://{endpoint}/keys/{path1}/{path2}", context);
        assertEquals("\"https://\" + myEndpoint + \"/keys/\" + myPath + \"/\" + myPath2", result);
    }

    @Test
    public void buildsPathWithNoSubstitutions() {
        HttpRequestContext context = new HttpRequestContext();
        String result = PathBuilder.buildPath("https://keys", context);
        assertEquals("\"https://keys\"", result);
    }

    @Test
    public void buildsPathWithNoQueryParameters() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        String result = PathBuilder.buildPath("https://{endpoint}/keys", context);
        assertEquals("\"https://\" + myEndpoint + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithMultipleSameSubstitutions() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path", "myPath"));
        String result = PathBuilder.buildPath("https://{endpoint}/keys/{path}/{path}", context);
        assertEquals("\"https://\" + myEndpoint + \"/keys/\" + myPath + \"/\" + myPath", result);
    }

    @Test
    public void buildsPathWithClashingSubstitutionNames() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        assertThrows(IllegalArgumentException.class,
            () -> context.addSubstitution(new Substitution("endpoint", "myEndpoint2")));
    }

    @Test
    public void buildsPathWithMissingSubstitution() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        assertThrows(MissingSubstitutionException.class,
            () -> PathBuilder.buildPath("https://{endpoint}/keys/{path1}", context));
    }

    @Test
    public void buildsPathWithMissingQueryParameter() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addQueryParam("key1", "value1", false, false, false);
        assertThrows(MissingSubstitutionException.class,
            () -> PathBuilder.buildPath("https://{endpoint}/keys?key2={value2}", context));
    }

    @Test
    public void buildsPathWithEmptySubstitutionValue() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", ""));
        String result = PathBuilder.buildPath("https://{endpoint}/keys", context);
        assertEquals("\"https://\" +  + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithSubstitutionNotSurroundedBySlashes() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        String result = PathBuilder.buildPath("https://{endpoint}.azure.com/keys", context);
        assertEquals("\"https://\" + myEndpoint + \".azure.com/keys\"", result);
    }

    @Test
    public void buildsPathWithSubstitutionFollowedByDot() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("domain", "azure"));
        String result = PathBuilder.buildPath("https://{endpoint}.{domain}.com/keys", context);
        assertEquals("\"https://\" + myEndpoint + \".\" + azure + \".com/keys\"", result);
    }

    @Test
    public void buildsPathWithSubstitutionFollowedByColon() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("protocol", "protocol"));
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        String result = PathBuilder.buildPath("{protocol}://{endpoint}/keys", context);
        assertEquals("protocol + \"://\" + myEndpoint + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithSubstitutionFollowedByQuestionMark() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("query", "myQuery"));
        String result = PathBuilder.buildPath("https://{endpoint}/keys?{query}", context);
        assertEquals("\"https://\" + myEndpoint + \"/keys?\" + myQuery", result);
    }

    @Test
    public void buildsPathWithSubstitutionFollowedBySlash() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path", "myPath"));
        String result = PathBuilder.buildPath("https://{endpoint}/{path}/keys", context);
        assertEquals("\"https://\" + myEndpoint + \"/\" + myPath + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithSubstitutionFollowedByHyphen() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path", "myPath"));
        String result = PathBuilder.buildPath("https://{endpoint}-{path}/keys", context);
        assertEquals("\"https://\" + myEndpoint + \"-\" + myPath + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithSubstitutionFollowedByUnderscore() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path", "myPath"));
        String result = PathBuilder.buildPath("https://{endpoint}_{path}/keys", context);
        assertEquals("\"https://\" + myEndpoint + \"_\" + myPath + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithSubstitutionFollowedByPercent() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path", "myPath"));
        String result = PathBuilder.buildPath("https://{endpoint}%{path}/keys", context);
        assertEquals("\"https://\" + myEndpoint + \"%\" + myPath + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithSubstitutionFollowedByPlus() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path", "myPath"));
        String result = PathBuilder.buildPath("https://{endpoint}+{path}/keys", context);
        assertEquals("\"https://\" + myEndpoint + \"+\" + myPath + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithSubstitutionFollowedByNumber() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path", "myPath"));
        String result = PathBuilder.buildPath("https://{endpoint}1{path}/keys", context);
        assertEquals("\"https://\" + myEndpoint + \"1\" + myPath + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithSubstitutionFollowedBySpecialCharacter() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path", "myPath"));
        String result = PathBuilder.buildPath("https://{endpoint}*{path}/keys", context);
        assertEquals("\"https://\" + myEndpoint + \"*\" + myPath + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithSubstitutionFollowedBySpace() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path", "myPath"));
        String result = PathBuilder.buildPath("https://{endpoint} {path}/keys", context);
        assertEquals("\"https://\" + myEndpoint + \" \" + myPath + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithSubstitutionFollowedByLetter() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path", "myPath"));
        String result = PathBuilder.buildPath("https://{endpoint}a{path}/keys", context);
        assertEquals("\"https://\" + myEndpoint + \"a\" + myPath + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithSubstitutionFollowedByUnicodeCharacter() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path", "myPath"));
        String result = PathBuilder.buildPath("https://{endpoint}\u00A9{path}/keys", context);
        assertEquals("\"https://\" + myEndpoint + \"\u00A9\" + myPath + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithSubstitutionFollowedBySpecialAndAlphanumericCharacters() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path", "myPath"));
        String result = PathBuilder.buildPath("https://{endpoint}*1a{path}/keys", context);
        assertEquals("\"https://\" + myEndpoint + \"*1a\" + myPath + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithSubstitutionFollowedBySpecialAlphanumericAndUnicodeCharacters() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path", "myPath"));
        String result = PathBuilder.buildPath("https://{endpoint}*1a\u00A9{path}/keys", context);
        assertEquals("\"https://\" + myEndpoint + \"*1a\u00A9\" + myPath + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithSubstitutionFollowedBySpecialAlphanumericUnicodeCharactersAndSpaces() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path", "myPath"));
        String result = PathBuilder.buildPath("https://{endpoint}*1a\u00A9 {path}/keys", context);
        assertEquals("\"https://\" + myEndpoint + \"*1a\u00A9 \" + myPath + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithMultipleSubstitutionsFollowedByDifferentCharacters() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path1", "myPath1"));
        context.addSubstitution(new Substitution("path2", "myPath2"));
        context.addSubstitution(new Substitution("path3", "myPath3"));
        String result = PathBuilder.buildPath("https://{endpoint}*1a{path1}\u00A9 {path2}/keys/{path3}", context);
        assertEquals("\"https://\" + myEndpoint + \"*1a\" + myPath1 + \"\u00A9 \" + myPath2 + \"/keys/\" + myPath3",
            result);
    }

    @Test
    public void buildsPathWithSubstitutionValueContainingSpecialCharacter() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint*"));
        String result = PathBuilder.buildPath("https://{endpoint}/keys", context);
        assertEquals("\"https://\" + myEndpoint* + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithNestedSubstitutions() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path", "myPath"));
        assertThrows(MissingSubstitutionException.class,
            () -> PathBuilder.buildPath("https://{{endpoint}/keys/{path}", context));
    }

    @Test
    public void buildsPathWithMissingClosingBrace() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path", "myPath"));
        assertThrows(MissingSubstitutionException.class,
            () -> PathBuilder.buildPath("https://{endpoint/keys/{path}", context));
    }

    @Test
    public void buildsPathWithMissingOpeningBrace() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path", "myPath"));
        assertThrows(MissingSubstitutionException.class,
            () -> PathBuilder.buildPath("https://endpoint}/keys/{path}", context));
    }

    @Test
    public void buildsPathWithSubstitutionContainingOpeningBrace() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint{", "myEndpoint"));
        assertThrows(MissingSubstitutionException.class,
            () -> PathBuilder.buildPath("https://{endpoint{/keys", context));
    }

    @Test
    public void buildsPathWithSubstitutionContainingClosingBrace() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint}", "myEndpoint"));
        assertThrows(MissingSubstitutionException.class,
            () -> PathBuilder.buildPath("https://{endpoint}/keys", context));
    }

    @Test
    public void buildsPathWithSubstitutionContainingBothBraces() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint{}", "myEndpoint"));
        assertThrows(MissingSubstitutionException.class,
            () -> PathBuilder.buildPath("https://{endpoint{}}/keys", context));
    }

    @Test
    public void buildsPathWithSubstitutionContainingMultipleBraces() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint{{}}", "myEndpoint"));
        assertThrows(MissingSubstitutionException.class,
            () -> PathBuilder.buildPath("https://{endpoint{{}}}/keys", context));
    }

    @Test
    public void buildsPathWithoutProtocolWithNoSubstitutions() {
        HttpRequestContext context = new HttpRequestContext();
        assertThrows(MissingSubstitutionException.class, () -> PathBuilder.buildPath("{endpoint}/keys", context));
    }

    @Test
    public void buildsPathWithoutProtocolWithSubstitutions() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("path", "myPath"));
        String result = PathBuilder.buildPath("{endpoint}/keys/{path}", context);
        assertEquals("myEndpoint + \"/keys/\" + myPath", result);
    }

    // TODO: Currently, the context adds subsitition using the parameter name as key so i
    // Is this a valid case?
    //    @Test
    //    public void buildsPathWithHostAndPathUsingSameSubstitutionName() {
    //        HttpRequestContext context = new HttpRequestContext();
    //        context.addSubstitution(new Substitution("sub1", "hostSub1"));
    //        context.addSubstitution(new Substitution("sub1", "pathSub1"));
    //
    //        String result = PathBuilder.buildPath("https://{sub1}.host.com/keys/{sub1}", context);
    //        assertEquals("\"https://\" + hostSub1 + \".host.com/keys/\" + pathSub1", result);
    //    }

    @Test
    public void buildsPathWithNullQueryParameterValue() {
        HttpRequestContext context = new HttpRequestContext();
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addQueryParam("key1", null, false, false, false);
        String result = PathBuilder.buildPath("https://{endpoint}/keys", context);
        assertEquals("\"https://\" + myEndpoint + \"/keys\"", result);
    }

    @Test
    public void buildsPathWithNextLinkSpecialCase() {
        HttpRequestContext context = new HttpRequestContext();
        context.setMethodName("linkNext");
        context.addSubstitution(new Substitution("nextLink", "nextLinkVar"));
        String result = PathBuilder.buildPath("{nextLink}", context);
        assertEquals("nextLinkVar", result);
    }

    @Test
    public void buildsPathWithNextLinkSpecialCaseMissingSubstitution() {
        HttpRequestContext context = new HttpRequestContext();
        context.setMethodName("linkNext");
        MissingSubstitutionException ex
            = assertThrows(MissingSubstitutionException.class, () -> PathBuilder.buildPath("{nextLink}", context));
        assertEquals("Could not find substitution for 'nextLink' in method 'linkNext'", ex.getMessage());
    }

    @Test
    public void buildsPathWithEndpointAndNextLink() {
        HttpRequestContext context = new HttpRequestContext();
        context.setMethodName("linkNext");
        context.addSubstitution(new Substitution("endpoint", "myEndpoint"));
        context.addSubstitution(new Substitution("nextLink", "nextLinkVar"));
        String result = PathBuilder.buildPath("{nextLink}", context);
        assertEquals("nextLinkVar", result);
    }
}
