// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.openrewrite;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;


import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.MethodCall;
import org.openrewrite.java.tree.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Add try-catch to method recipe places all calls to methods matching the provided method
 * pattern in a try-catch block based off the template provided.
 * Recipe will attempt to use the JavaParser first, but will also run on code that cannot be
 * parsed by Open Rewrites Java parser.
 * Recipe does not check if the method throws the supplied exception, only that the method is
 * in a suitable try-catch block.
 * If the template is not syntactically correct, the recipe will not make any changes.
 */

@Value
@EqualsAndHashCode(callSuper = false)
public class AddTryCatchToMethodCallRecipe extends Recipe {

    @Option(displayName = "Method pattern",
            description = "A method pattern used to find matching method declaration.",
            example = "*..* hello(..)")
    @NonNull
    String methodPattern;

    @Option(displayName = "Catch template",
            description = "The code snippet to be executed in the catch block",
            example = "catch (IOException e) { e.printStackTrace(); }")
    @NonNull
    String catchTemplateString;

    @Option(displayName = "Exclude owner of method",
            description = "When enabled, the owner (class) from which the method originates from will not be matched during search.",
            required = false)
    @NonNull
    boolean excludeOwner;

    @Option(displayName = "Fully qualified exception name",
            description = "The fully qualified type name for the caught exception",
            example = "java.io.IOException")
    @NonNull
    String fullyQualifiedExceptionName;

    /**
     * All recipes must be serializable. This is verified by RewriteTest.rewriteRun() in your tests.
     * Json creator allows your recipes to be used from a yaml file.
     */
    @JsonCreator
    public AddTryCatchToMethodCallRecipe(@NonNull @JsonProperty("methodPattern") String methodPattern,
                                         @NonNull @JsonProperty("catchTemplateString") String catchTemplateString,
                                         @NonNull @JsonProperty("fullyQualifiedExceptionName") String fullyQualifiedExceptionName,
                                         @Nullable @JsonProperty("excludeOwner") boolean excludeOwner) {
        this.methodPattern = methodPattern;
        this.excludeOwner = excludeOwner;
        this.catchTemplateString = catchTemplateString;
        this.fullyQualifiedExceptionName = fullyQualifiedExceptionName;
    }


    @Override
    public @NlsRewrite.DisplayName @NotNull String getDisplayName() {
        return "Add try-catch to method";
    }

    @Override
    public @NlsRewrite.Description @NotNull String getDescription() {
        return "Surrounds calls to the target method in a custom try-catch block.";
    }

    /**
     * Method to return the visitor that performs the checks and changes
     *
     * @return Returns the visitor that performs the checks and changes
     */
    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AddTryCatchVisitor();
    }

    /**
     * Visitor that performs the checks and changes
     */
    private class AddTryCatchVisitor extends JavaIsoVisitor<ExecutionContext> {

        private final MethodMatcher methodMatcher = new MethodMatcher(methodPattern, true);

        /**
         * Overridden visitBlock method performs the changes to methods filtered by visitMethodCall.
         */
        @Override
        public J.@NotNull Block visitBlock(J.@NotNull Block block, @NotNull ExecutionContext context) {
            J.Block body = super.visitBlock(block, context);

            // Get the method that needs to be changed
            MethodCall method = getCursor().pollMessage("METHOD");
            if (method == null) {
                return body;
            }

            //Get the parents of the method
            Tree parent = getCursor().pollMessage("PARENT");
            // Get the first statement parent of method
            Statement parentStatement = getCursor().pollMessage("STATEMENT");

            JavaTemplate tryCatchTemplate = JavaTemplate.builder("try { int a = null; a = 3; } " + catchTemplateString)
                    .imports(fullyQualifiedExceptionName)
                    .build();

            // Create an empty block to apply the try-catch template based off the cursor values from the main body
            // This should create the correct formatting.
            J.Block b = J.Block.createEmptyBlock();
            b = tryCatchTemplate.apply(new Cursor(getCursor(),b), b.getCoordinates().firstStatement());
            int parentIndex;

            // Extract the try-catch block and dummy elements
            J.Try _try = (J.Try) b.getStatements().get(0);
            J.VariableDeclarations dummyVarDec = (J.VariableDeclarations) _try.getBody().getStatements().get(0);
            J.Assignment dummyAssignment = (J.Assignment) _try.getBody().getStatements().get(1);

            if (_try.getCatches().isEmpty()) {
                // The catch template was incorrect, recipe is unsafe.
                return body;
            }
            // The original list of statements to alter
            List<Statement> bodyStatements = body.getStatements();

            // Method is the first element on its line.
            if (parent == null) {
                // Cast method as a statement and update the indentation (prefix)

                Statement methodStatement = method.withPrefix(dummyVarDec.getPrefix());
                parentIndex = body.getStatements().indexOf(methodStatement);
                // Make it the only statement in the try block
                _try = _try.withBody(_try.getBody().withStatements(ListUtils.insert(
                        new ArrayList<>(), methodStatement, 0 )));

                // Update the statements
                bodyStatements.set(parentIndex, _try);
            }
            else if (parent instanceof J.Assignment) {
                parentIndex = body.getStatements().indexOf(parent);
                J.Assignment new_assignment = ((J.Assignment) parent).withPrefix(dummyAssignment.getPrefix());

                _try = _try.withBody(_try.getBody().withStatements(ListUtils.insert(
                        new ArrayList<>(), new_assignment, 0 )));

                bodyStatements.set(parentIndex, _try);
            }
            else if (parentStatement instanceof J.VariableDeclarations) {

                J.VariableDeclarations parentVd = (J.VariableDeclarations) parentStatement;
                parentIndex = body.getStatements().indexOf(parentVd);

                if (parentVd.getVariables().size() != 1) {
                    // Recipe can only handle a variable declaration with a single named variable at this time
                    // Could be changed.
                    return body;
                }

                J.VariableDeclarations.NamedVariable namedVariable = parentVd.getVariables().get(0);
                Expression expression = namedVariable.getInitializer();

                assert expression != null;
                // Repurpose the dummyAssignment variable
                dummyAssignment = dummyAssignment.withVariable(namedVariable.getName().unwrap());
                dummyAssignment = dummyAssignment.withAssignment(expression);

                _try = _try.withBody(_try.getBody().withStatements(ListUtils.insert(
                       new ArrayList<>(), dummyAssignment, 0 )));

                // Make the original declaration initialise with '= null'
                namedVariable = namedVariable.withInitializer(dummyVarDec.getVariables().get(0).getInitializer());

                parentVd = parentVd.withVariables(ListUtils.insert(
                        new ArrayList<>(), namedVariable, 0 ));

                // Replace the old VariableDeclarations
                bodyStatements.set(parentIndex, parentVd);
                // Add the try below it
                bodyStatements.add(parentIndex +1, _try);

            }
            else { // Wrap method call in try catch for all other cases
                int i = 0;
                int index = -1;
                for (Statement statement : body.getStatements()) {
                    if (statement.print().contains(method.print())) {
                        index = i;
                    }
                    i ++;
                }
                String tryCatchBlock = String.format(
                        "try { %s; } %s", body.getStatements().get(index), catchTemplateString
                );
                JavaTemplate tryCatchNewTemplate = JavaTemplate.builder(tryCatchBlock)
                        .imports(fullyQualifiedExceptionName)
                        .build();

                // Create an empty block to apply the try-catch template based off the cursor values from the main body
                // This should create the correct formatting.
                J.Block newBlock = J.Block.createEmptyBlock();
                newBlock = tryCatchNewTemplate.apply(new Cursor(getCursor(),newBlock), newBlock.getCoordinates().firstStatement());

                bodyStatements.remove(index);
                for (Statement statement : newBlock.getStatements()) {
                    bodyStatements.add(index, statement);
                }
            }

            // Update the body block with the new set of statements and return.
            body = body.withStatements(bodyStatements);
            // Add the import if needed
            maybeAddImport(fullyQualifiedExceptionName,false);
            return body;
        }
        public boolean containsSubstringWithParenthesis(String input, String substring) {
            String regex = substring + "\\s*\\(";
            return input.matches(".*" + regex + ".*");
        }


        /**
         * Method to find method calls that need to be wrapped
         */
        private <M extends MethodCall> @Nullable M visitMethodCall(M methodCall, Supplier<M> visitSuper) {
            if (!methodMatcher.matches(methodCall)) {
                if (!excludeOwner) {
                    // Make no changes
                    return visitSuper.get();
                }
                if (!containsSubstringWithParenthesis(methodCall.toString(), methodPattern.replaceAll("\\(.*?\\)", "").
                        replaceAll("\\*", "").trim().split(" ")[1])){
                    // Make no changes
                    return visitSuper.get();
                }
            }
            // If match found, check that it is not already handled by a try block
            try {
                // Get the first upstream try block. Will throw exception if there are none
                J.Try _try = getCursor().dropParentUntil(it -> it instanceof J.Try).getValue();

                // Get the first enclosing block
                J.Block block = getCursor().dropParentUntil(it -> it instanceof J.Block).getValue();

                // Check to see if this try block is the parent of the enclosing block
                if (_try.getBody().equals(block)) {
                    // Check if the correct exception is caught
                    boolean isCaught = _try.getCatches().stream().anyMatch(
                            _catch -> Objects.requireNonNull(_catch.getParameter().getType())
                                    .isAssignableFrom(Pattern.compile(fullyQualifiedExceptionName)));

                    // Make no changes if exception already caught
                    if (isCaught) {
                        return visitSuper.get();
                    }
                }
            } catch (IllegalStateException ignored) {}
            // If the method matches and exception is not caught set messages for block
            getCursor().putMessageOnFirstEnclosing(J.Block.class, "METHOD", methodCall);

            Tree parent = getCursor().getParentTreeCursor().getValue();
            if (! (parent instanceof J.Block)) {
                // If the method is part of a nested statement flag the direct tree parent
                getCursor().putMessageOnFirstEnclosing(J.Block.class, "PARENT", parent);
                try {
                    // And the first parent that is a statement
                    Statement statement = getCursor().dropParentUntil(it -> it instanceof Statement).getValue();
                    getCursor().putMessageOnFirstEnclosing(J.Block.class, "STATEMENT", statement);
                } catch (IllegalStateException ignored) {}
            }

            return visitSuper.get();
        }

        /**
         * The Suppliers that traverse the LST and redirect all types of method calls through visitMethodCall.
         */

        @Override
        public J.NewClass visitNewClass(J.NewClass newClass, ExecutionContext context) {
            return visitMethodCall(newClass, () -> super.visitNewClass(newClass, context));
        }

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
            return visitMethodCall(method, () -> super.visitMethodInvocation(method, context));
        }

        @Override
        public J.MemberReference visitMemberReference(J.MemberReference memberRef, ExecutionContext context) {
            return visitMethodCall(memberRef, () -> super.visitMemberReference(memberRef, context));
        }

    } // end catchUncheckedVisitor

}
