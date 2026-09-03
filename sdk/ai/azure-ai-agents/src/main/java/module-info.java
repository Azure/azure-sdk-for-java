
module com.azure.ai.agents {
    requires transitive com.azure.core;
    requires openai.java.core;
    requires openai.java.client.okhttp;

    exports com.azure.ai.agents;
    exports com.azure.ai.agents.models;

    opens com.azure.ai.agents.models to com.azure.core;
    opens com.azure.ai.agents.implementation.models to com.azure.core;
}
