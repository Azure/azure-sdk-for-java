package com.azure.data.schemaregistry.apacheavro;

public class Person {
    public static final String JSON_SCHEMA_D4 = "{"
        + "    \"$id\": \"https://example.com/person.schema.json\", "
        + "     \"$schema\": \"http://json-schema.org/draft-04/schema#\","
        + "     \"type\": \"object\","
        + "     \"title\": \"Person\","
        + "     \"properties\": {"
        + "         \"name\": {"
        + "             \"type\": \"string\","
        + "             \"description\": \"Name.\""
        + "         },"
        + "         \"age\": {\"
        + "             \"description\": \"Age in years.\","
        + "             \"type\": \"integer\","
        + "             \"minimum\": 0"
        + "         }"
        + "     }"
        + "}";

    private String name;

    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
