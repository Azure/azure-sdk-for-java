// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.openrewrite.util;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

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
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {

                if (isAsyncApi(method)) {
                    asyncUsageReport.insertRow(ctx, new AsyncUsageReport.Row(
                            getCursor().firstEnclosing(J.CompilationUnit.class).getSourcePath().toString(),
                            method.getSimpleName()
                    ));
                }

                return super.visitMethodDeclaration(method, ctx);
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

            private boolean isAsyncApi(J.MethodDeclaration method) {
                JavaType returnType = method.getType();

                // exclude non-public methods
                if (!method.hasModifier(J.Modifier.Type.Public)) {
                    return false;
                }

                J.Package p = getCursor().firstEnclosing(J.CompilationUnit.class).getPackageDeclaration();
                if (p == null) {
                    return false;
                }
                String packageName = p.getExpression().toString();
                System.out.println("package: " + packageName);

                // exclude implementation and test packages
                if (packageName.contains("implementation") || packageName.contains("test")) {
                    return false;
                }

                String returnTypeString = returnType.toString();
                System.out.println("name: " + method.getName());
                System.out.println("type: " + method.getType());
                return returnTypeString.startsWith("reactor.core.publisher.Flux<") ||
                    returnTypeString.startsWith("reactor.core.publisher.Mono<") ||
                    returnTypeString.startsWith("com.azure.core.http.rest.PagedFlux<") ||
                    returnTypeString.startsWith("com.azure.core.http.rest.PagedFluxBase<");
            }
        };
    }
}
