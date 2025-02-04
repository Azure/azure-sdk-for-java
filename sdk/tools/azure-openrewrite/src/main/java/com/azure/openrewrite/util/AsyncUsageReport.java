package com.azure.openrewrite.util;

import org.openrewrite.DataTable;
import org.openrewrite.Column;
import org.openrewrite.Recipe;

public class AsyncUsageReport extends DataTable<AsyncUsageReport.Row> {
    public AsyncUsageReport(Recipe recipe) {
        super(recipe, "Async Api Usage Report","Records usage of async APIs in the codebase.");
    }

    public static class Row {
        @Column(displayName = "File", description = "The file containing the async API usage.")
        private String file;

        @Column(displayName = "API", description = "The async API being used.")
        private String api;

        public Row(String file, String api) {
            this.file = file;
            this.api = api;
        }

        // Getters and setters
    }
}
