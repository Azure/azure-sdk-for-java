// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.openrewrite.recipe;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.openrewrite.java.Assertions.java;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

@Disabled("Incorrect tests. Need to look into.")
public class ExceptionTypesTest extends RecipeTestBase {
    /**
     * ExceptionTypesTest tests exception migrations from azure-core v1
     * to azure-core-v2 and client-core.
     * Recipes used: ChangeType
     * From:
     * com.azure.core.exception
     *      ClientAuthenticationException
     *      HttpResponseException
     *      ResourceModifiedException
     *      ResourceNotFoundException
     * To:
     * com.azure.core.v2.exception
     *      ClientAuthenticationException
     *      ResourceModifiedException
     *      ResourceNotFoundException
     * io.clientcore.core.http.exception
     *      HttpResponseException
     *
     */


    /* Testing ChangeType recipes */
    @Test
    public void testClientAuthenticationExceptionChanged() {
        @Language("java") String before = "import com.azure.core.exception.ClientAuthenticationException;";
        before += "\npublic class Testing {";
        before += "\n  public void testMethod() {";
        before += "\n    try {";
        before += "\n      // Some code that may throw ClientAuthenticationException";
        before += "\n    } catch (ClientAuthenticationException e) {";
        before += "\n      // Handle exception";
        before += "\n    }";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import com.azure.core.v2.exception.ClientAuthenticationException;";
        after += "\n\npublic class Testing {";
        after += "\n  public void testMethod() {";
        after += "\n    try {";
        after += "\n      // Some code that may throw ClientAuthenticationException";
        after += "\n    } catch (ClientAuthenticationException e) {";
        after += "\n      // Handle exception";
        after += "\n    }";
        after += "\n  }";
        after += "\n}";

        rewriteRun(
                java(before,after)
        );
    }


    @Test
    public void testHttpResponseExceptionChanged() {
        @Language("java") String before = "import com.azure.core.exception.HttpResponseException;";
        before += "\npublic class Testing {";
        before += "\n  public void testMethod() {";
        before += "\n    try {";
        before += "\n      // Some code that may throw HttpResponseException";
        before += "\n    } catch (HttpResponseException e) {";
        before += "\n      // Handle exception";
        before += "\n    }";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.http.exception.HttpResponseException;";
        after += "\n\npublic class Testing {";
        after += "\n  public void testMethod() {";
        after += "\n    try {";
        after += "\n      // Some code that may throw HttpResponseException";
        after += "\n    } catch (HttpResponseException e) {";
        after += "\n      // Handle exception";
        after += "\n    }";
        after += "\n  }";
        after += "\n}";

        rewriteRun(
                java(before,after)
        );
    }


    @Test
    public void testResourceModifiedExceptionChanged() {
        @Language("java") String before = "import com.azure.core.exception.ResourceModifiedException;";
        before += "\npublic class Testing {";
        before += "\n  public void testMethod() {";
        before += "\n    try {";
        before += "\n      // Some code that may throw ResourceModifiedException";
        before += "\n    } catch (ResourceModifiedException e) {";
        before += "\n      // Handle exception";
        before += "\n    }";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import com.azure.core.v2.exception.ResourceModifiedException;";
        after += "\n\npublic class Testing {";
        after += "\n  public void testMethod() {";
        after += "\n    try {";
        after += "\n      // Some code that may throw ResourceModifiedException";
        after += "\n    } catch (ResourceModifiedException e) {";
        after += "\n      // Handle exception";
        after += "\n    }";
        after += "\n  }";
        after += "\n}";

        rewriteRun(
                java(before,after)
        );
    }


    @Test
    public void testResourceNotFoundExceptionChanged() {
        @Language("java") String before = "import com.azure.core.exception.ResourceNotFoundException;";
        before += "\npublic class Testing {";
        before += "\n  public void testMethod() {";
        before += "\n    try {";
        before += "\n      // Some code that may throw ResourceNotFoundException";
        before += "\n    } catch (ResourceNotFoundException e) {";
        before += "\n      // Handle exception";
        before += "\n    }";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import com.azure.core.v2.exception.ResourceNotFoundException;";
        after += "\n\npublic class Testing {";
        after += "\n  public void testMethod() {";
        after += "\n    try {";
        after += "\n      // Some code that may throw ResourceNotFoundException";
        after += "\n    } catch (ResourceNotFoundException e) {";
        after += "\n      // Handle exception";
        after += "\n    }";
        after += "\n  }";
        after += "\n}";

        rewriteRun(
                java(before,after)
        );
    }

    /**
     * Will fail and need updating if all azure-core v1 exceptions are migrated,
     * or if all exceptions are migrated to the same directory.
     */
    @Test
    public void testBundledImportsChanged() {
        @Language("java") String before = "import com.azure.core.exception.*;";
        before += "\npublic class Testing {";
        before += "\n  public void testMethod() {";
        before += "\n    try {";
        before += "\n      // Some code that may throw ClientAuthenticationException";
        before += "\n      throw new ClientAuthenticationException(null,null);";
        before += "\n    } catch (ClientAuthenticationException e) {";
        before += "\n      // Handle exception";
        before += "\n    }";
        before += "\n    try {";
        before += "\n      // Some code that may throw HttpResponseException";
        before += "\n      throw new HttpResponseException(null,null);";
        before += "\n    } catch (HttpResponseException e) {";
        before += "\n      // Handle exception";
        before += "\n    }";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import com.azure.core.exception.*;" +
                "\nimport com.azure.core.v2.exception.ClientAuthenticationException;" +
                "\nimport io.clientcore.core.http.exception.HttpResponseException;";
        after += "\n\npublic class Testing {";
        after += "\n  public void testMethod() {";
        after += "\n    try {";
        after += "\n      // Some code that may throw ClientAuthenticationException";
        after += "\n      throw new ClientAuthenticationException(null,null);";
        after += "\n    } catch (ClientAuthenticationException e) {";
        after += "\n      // Handle exception";
        after += "\n    }";
        after += "\n    try {";
        after += "\n      // Some code that may throw HttpResponseException";
        after += "\n      throw new HttpResponseException(null,null);";
        after += "\n    } catch (HttpResponseException e) {";
        after += "\n      // Handle exception";
        after += "\n    }";
        after += "\n  }";
        after += "\n}";

        rewriteRun(
                java(before,after)
        );
    }
}
