// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.openrewrite.util;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TextComment;
import org.openrewrite.marker.Markers;

public class FindAsyncApiUsageRecipe extends Recipe {
    transient AsyncUsageReport asyncUsageReport = new AsyncUsageReport(this);

    @Override
    public String getDisplayName() {
        return "Find Async API Usage";
    }

    @Override
    public String getDescription() {
        return "Finds all async API usage and outputs a data table.";
    }

    @Override
    public TreeVisitor<J, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                if (isAsyncApi(method)) {
                    asyncUsageReport.insertRow(ctx, new AsyncUsageReport.Row(
                        getCursor().firstEnclosing(J.CompilationUnit.class).getSourcePath().toString(),
                        method.getSimpleName()
                    ));
                    method = method.withComments(ListUtils.concat(
                        method.getComments(),
                        new TextComment(false, "This is an async api. Manual migration needed. See guidance.", "\n", Markers.EMPTY)
                    ));
                }
                return super.visitMethodInvocation(method, ctx);
            }

            /**
             * Method to check if a method is an async API
             *
             * async APIs:
             * - reactor.core.publisher.Flux
             * - reactor.core.publisher.Mono
             * - com.azure.core.http.rest.PagedFlux
             * - com.azure.core.http.rest.PagedFluxBase
             *
             * @param method The method to check
             * @return True if the method is an async API, false otherwise
             */

            private boolean isAsyncApi(J.MethodInvocation method) {
                JavaType.Method methodType = method.getMethodType();
                if (methodType == null) {
                    return false;
                }
                String returnTypeString = methodType.getReturnType().toString();
                String packageName = methodType.getDeclaringType().getPackageName();
                if (!packageName.startsWith("com.azure.")) {
                    return false;
                }

                return returnTypeString.startsWith("reactor.core.publisher.Flux<") ||
                    returnTypeString.startsWith("reactor.core.publisher.Mono<") ||
                    returnTypeString.startsWith("com.azure.core.http.rest.PagedFlux<") ||
                    returnTypeString.startsWith("com.azure.core.http.rest.PagedFluxBase<");
            }
        };
    }
}
