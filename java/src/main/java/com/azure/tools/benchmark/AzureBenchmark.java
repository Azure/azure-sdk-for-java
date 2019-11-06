package com.azure.tools.benchmark;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.cli.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.util.logging.Logger;

public class AzureBenchmark {
    private static Logger LOGGER = Logger.getLogger(BenchmarkTestBase.class.getName());
    public static int WARMUP_ITERATIONS = 2;

    public static void main(String[] args) throws RunnerException {

        org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();

        Option warmupOption = new Option("w", "warmup", false, "Warm up iterations to run");
        options.addOption(warmupOption);

        Option scopeOption = new Option("s", "scope", true, "Package scope to run tests of");
        options.addOption(scopeOption);

        Option outputFileOption = new Option("o", "output", true, "The output JSON file name");
        options.addOption(outputFileOption);


        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("storageperf", options);
            System.exit(1);
        }

        WARMUP_ITERATIONS = Integer.parseInt(cmd.getOptionValue("warmup", String.valueOf(WARMUP_ITERATIONS)));
        String scope = cmd.getOptionValue("scope", "com.azure.*");
        String output = cmd.getOptionValue("output", "benchmarkResults");

        Options opt = new OptionsBuilder()
                .include(scope)
                .forks(1)
                .verbosity(VerboseMode.SILENT)
                .warmupIterations(WARMUP_ITERATIONS)
                .build();

        new Runner(opt).run();
    }
}
