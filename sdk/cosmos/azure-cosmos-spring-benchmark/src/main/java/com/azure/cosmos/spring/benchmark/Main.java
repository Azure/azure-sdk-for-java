package com.azure.cosmos.spring.benchmark;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.CosmosClientBuilder;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Main implements CommandLineRunner {
    private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ReactiveUserRepository reactiveUserRepository;
    private AzureKeyCredential azureKeyCredential;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        //ConfigurableApplicationContext appContext = SpringApplication.run(Main.class, args);
        //reactiveUserRepository = appContext.getBean(ReactiveUserRepository.class);

        try {
            LOGGER.debug("Parsing the arguments ...");
            Configuration cfg = new Configuration();
            cfg.tryGetValuesFromSystem();

            JCommander jcommander = new JCommander(cfg, args);
            if (cfg.isHelp()) {
                // prints out the usage help
                jcommander.usage();
                return;
            }

            /*if (cfg.isClientTelemetryEnabled()) {
                System.setProperty("COSMOS.CLIENT_TELEMETRY_ENDPOINT", cfg.getClientTelemetryEndpoint());
                System.setProperty("COSMOS.CLIENT_TELEMETRY_SCHEDULING_IN_SECONDS", String.valueOf(cfg.getClientTelemetrySchedulingInSeconds()));
            }*/

            validateConfiguration(cfg);

            /*
             *   Start Setup
             */
            azureKeyCredential = new AzureKeyCredential(cfg.getMasterKey());
            CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
                .endpoint(cfg.getServiceEndpoint())
                .credential(azureKeyCredential)
                .preferredRegions(cfg.getPreferredRegionsList())
                .consistencyLevel(cfg.getConsistencyLevel())
                .contentResponseOnWriteEnabled(cfg.isContentResponseOnWriteEnabled())
                .clientTelemetryEnabled(cfg.isClientTelemetryEnabled());
            /*
             *   End Setup
             */

            if (cfg.isSync()) {
                throw new IllegalArgumentException("Only async is currently supported");
                //syncBenchmark(cfg);
            } else {
                asyncBenchmark(cfg, cosmosClientBuilder, reactiveUserRepository);
            }
        } catch (ParameterException e) {
            // if any error in parsing the cmd-line options print out the usage help
            System.err.println("INVALID Usage: " + e.getMessage());
            System.err.println("Try '-help' for more information.");
            throw e;
        }
    }

    private static void validateConfiguration(Configuration cfg) {
        switch (cfg.getOperationType()) {
            case WriteLatency:
                break;
            default:
                throw new IllegalArgumentException("Only 'WriteLatency' is currently supported");
        }
    }

    private static void asyncBenchmark(Configuration cfg, CosmosClientBuilder cosmosClientBuilder,
                                       ReactiveUserRepository reactiveUserRepository) throws Exception {
        LOGGER.info("Async benchmark ...");
        AsyncBenchmark<?> benchmark = null;
        try {
            switch (cfg.getOperationType()) {
                case WriteLatency:
                    benchmark = new AsyncWriteBenchmark(cfg, cosmosClientBuilder, reactiveUserRepository);
                    break;

                default:
                    throw new IllegalArgumentException("Only 'WriteLatency' is currently supported");
            }

            LOGGER.info("Starting {}", cfg.getOperationType());
            benchmark.run();
        } finally {
            if (benchmark != null) {
                benchmark.shutdown();
            }
        }
    }
}
