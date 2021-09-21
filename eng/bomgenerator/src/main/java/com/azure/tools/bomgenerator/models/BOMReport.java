// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.bomgenerator.models;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BOMReport {
    private final StringBuilder sb;
    private final Map<BomDependency, List<ConflictingDependency>> dependencyConflicts;
    private final String reportFileName;

    public BOMReport(String reportFileName, Map<BomDependency, List<ConflictingDependency>> dependencyConflicts) {
        sb = new StringBuilder();
        this.dependencyConflicts = dependencyConflicts;
        this.reportFileName = reportFileName;
    }

    public void generateReport() {
        System.out.println("Starting to write HTML report (" + OffsetDateTime.now() + ")");

        out("<!DOCTYPE html>");
        out("<html>");
        out("  <head>");
        out("    <title>Dependency Issues Report</title>");
        out("    <meta charset=\"UTF-8\"/>");
        out("    <style>");

        // write out CSS inline
        try (
            BufferedReader r = new BufferedReader(new InputStreamReader(BOMReport.class.getResourceAsStream("report.css")))) {
            r.lines().forEach(line -> out("      " + line));
        } catch (Exception e) {
            // no-op
            System.out.println("Can't find file: " + BOMReport.class.getResource("report.css"));
        }

        out("    </style>");
        out("  </head>");
        out("  <body>");

        out("    <center>");

        // Summary table
        out("      <h1>Dependency Conflicts Report</h1>");
        out("      <p>This report analyzed all track 2 GA data plane libraries and found the following conflicts in its dependency management.<br/>" +
            "It is important to resolve these conflicts to ensure convergence of the dependencies.<br/></p>");

        out("    <table>");
        out("      <thead>");
        out("<tr>");
        out("<th>");
        out("Dropped dependency");
        out("</th>");
        out("<th>");
        out("Reasons");
        out("</th>");
        out("</tr>");
        dependencyConflicts.keySet().stream().sorted(Comparator.comparing(BomDependency::toString)).forEach(dependency -> {
                out("<tr>");
                out("<td>");
                out(dependency.toString());
                out("</td>");
                out("<td>");
                dependencyConflicts.get(dependency).forEach(conflict -> {
                    out(String.format("Dependency %s. Expected version %s",
                        conflict.getActualDependency().toString(),
                        conflict.getExpectedDependency().getVersion()));
                    out("<br/>");
                });
                out("</td>");
                out("</tr>");
        });
        out("      <small>Report generated at " + DateTimeFormatter.ofPattern("HH:mm:ss 'on' yyyy-MM-dd").format(LocalDateTime.now()) + "<br/>");
        out("    </center>");
        out("  </body>");
        out("</html>");

        // write out to the output file
        File outFile = new File(reportFileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            writer.write(sb.toString());
            System.out.println("HTML report written to " + outFile + " (" + OffsetDateTime.now() + ")");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void out(String s) {
        sb.append(s);
        sb.append("\r\n");
    }
}
