package com.azure.openrewrite.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.Validated;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.jgit.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Value
@EqualsAndHashCode(callSuper = false)
public class AsyncBlockToSyncRecipe extends Recipe {

    @Option(
            displayName = "Async method chain",
            description = "The async method chain to be replaced with a sync method chain. Chain must end with a block() call.",
            example = "client.send(request).block()"
    )
    List<String> asyncMethodChain;

    @Option(
            displayName = "Sync method chain",
            description = "The sync method chain to replace the async method chain with.",
            example = "client.send(request)"
    )
    List<String> syncMethodChain;

    @Option(
            displayName = "Match overrides",
            description = "If true, the recipe will match method overrides in subclasses.",
            example = "true"
    )
    @Nullable
    Boolean matchOverrides;

    @JsonCreator
    public AsyncBlockToSyncRecipe(@JsonProperty("asyncBlock") List<String> asyncChain,
                                  @JsonProperty("syncBlock") List<String> syncChain,
                                  @JsonProperty("match") boolean match) {
        this.asyncMethodChain = asyncChain;
        this.syncMethodChain = syncChain;
        this.matchOverrides = match;
    }

    @Override
    public String getDisplayName() {
        return "AsyncBlockToSyncRecipe";
    }

    @Override
    public String getDescription() {
        return "Takes an async invocation chain that blocks immediately and replaces it with a sync invocation chain.";
    }


    @Override
    public JavaIsoVisitor<ExecutionContext> getVisitor() {
        List<MethodMatcher> asyncMethodMatchers = asyncMethodChain.stream()
                .map(matcher -> new MethodMatcher(matcher, matchOverrides))
                .collect(Collectors.toList());

        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                method = super.visitMethodInvocation(method, ctx);
                MethodMatcher matcher = new MethodMatcher("*..* *(..)");
                for (MethodMatcher asyncMatcher : asyncMethodMatchers) {
                    if (asyncMatcher.matches(method)) {
                        System.out.println("Matched async method: " + asyncMatcher.toString());
                    }
                }
                return method;
            }
        };
    }


}
