// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.javadoc.Javadoc;
import org.slf4j.Logger;

import static com.github.javaparser.javadoc.description.JavadocDescription.parseText;

/**
 * Contains customizations for PhoneNumbersCustomization configuration.
 */
public class PhoneNumbersCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.communication.phonenumbers.models");

        customizeAvailablePhoneNumber(models);
        customizePhoneNumbersReservation(models);
        customizeBrowsePhoneNumbersOptions(models);
        customizePhoneNumberSearchResult(models);
        customizeToJson(models);
    }

    // Customizes the PhoneNumbersReservation class
    private void customizePhoneNumbersReservation(PackageCustomization models) {
        models.getClass("PhoneNumbersReservation").customizeAst(ast -> {
            ast.addImport("com.azure.communication.phonenumbers.implementation.accesshelpers.PhoneNumbersReservationAccessHelper");

            ast.getClassByName("PhoneNumbersReservation").ifPresent(clazz -> {
                clazz.addMethod("setPhoneNumbers", Modifier.Keyword.PRIVATE)
                    .setType("PhoneNumbersReservation")
                    .addParameter("Map<String, AvailablePhoneNumber>", "phoneNumbers")
                    .setBody(StaticJavaParser.parseBlock("{ this.phoneNumbers = phoneNumbers; return this; }"))
                    .setJavadocComment(new Javadoc(parseText("Set the phoneNumbers property: The phone numbers in the reservation."))
                        .addBlockTag("param", "phoneNumbers the phone numbers in the reservation.")
                        .addBlockTag("return", "the PhoneNumbersReservation object itself."));

                clazz.getMethodsByName("getId").forEach(Node::remove);

                clazz.addMethod("getId", Modifier.Keyword.PUBLIC)
                    .setType("String")
                    .setBody(StaticJavaParser.parseBlock("{ return this.id.toString(); }"))
                    .setJavadocComment(new Javadoc(parseText("Get the id property: The id of the reservation."))
                        .addBlockTag("return", "the id value."));

                // Add Accessor to PhoneNumbersReservation
                clazz.setMembers(clazz.getMembers().addFirst(StaticJavaParser.parseBodyDeclaration("static {"
                    + "PhoneNumbersReservationAccessHelper.setAccessor(new PhoneNumbersReservationAccessHelper.PhoneNumbersReservationAccessor() {"
                    + "    @Override"
                    + "    public void setPhoneNumbers(PhoneNumbersReservation reservation, Map<String, AvailablePhoneNumber> phoneNumbers) {"
                    + "        reservation.setPhoneNumbers(phoneNumbers);"
                    + "    }"
                    + "}); }")));
            });
        });
    }

    // Customizes the AvailablePhoneNumber class
    private void customizeAvailablePhoneNumber(PackageCustomization models) {
        models.getClass("AvailablePhoneNumber").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.ResponseError");

            ast.getClassByName("AvailablePhoneNumber").ifPresent(clazz -> {
                clazz.getMethodsByName("getError").get(0)
                    .setType("ResponseError")
                    .setBody(StaticJavaParser.parseBlock("{ return this.error; }"))
                    .setJavadocComment(new Javadoc(parseText("Get the error property: The error object."))
                        .addBlockTag("return", "the error value."));

                clazz.getFieldByName("error").ifPresent(field -> field.getVariable(0).setType("ResponseError"));

                clazz.getMethodsByName("fromJson").get(0).setBody(StaticJavaParser.parseBlock("{ return jsonReader.readObject(reader -> { "
                    + "    AvailablePhoneNumber deserializedAvailablePhoneNumber = new AvailablePhoneNumber();"
                    + "    while (reader.nextToken() != JsonToken.END_OBJECT) {"
                    + "        String fieldName = reader.getFieldName();"
                    + "        reader.nextToken();"
                    + "        if (\"countryCode\".equals(fieldName)) {"
                    + "            deserializedAvailablePhoneNumber.countryCode = reader.getString();"
                    + "        } else if (\"capabilities\".equals(fieldName)) {"
                    + "            deserializedAvailablePhoneNumber.capabilities = PhoneNumberCapabilities.fromJson(reader);"
                    + "        } else if (\"phoneNumberType\".equals(fieldName)) {"
                    + "            deserializedAvailablePhoneNumber.phoneNumberType = PhoneNumberType.fromString(reader.getString());"
                    + "        } else if (\"assignmentType\".equals(fieldName)) {"
                    + "            deserializedAvailablePhoneNumber.assignmentType = PhoneNumberAssignmentType.fromString(reader.getString());"
                    + "        } else if (\"id\".equals(fieldName)) {"
                    + "            deserializedAvailablePhoneNumber.id = reader.getString();"
                    + "        } else if (\"phoneNumber\".equals(fieldName)) {"
                    + "            deserializedAvailablePhoneNumber.phoneNumber = reader.getString();"
                    + "        } else if (\"cost\".equals(fieldName)) {"
                    + "            deserializedAvailablePhoneNumber.cost = PhoneNumberCost.fromJson(reader);"
                    + "        } else if (\"status\".equals(fieldName)) {"
                    + "            deserializedAvailablePhoneNumber.status = PhoneNumberAvailabilityStatus.fromString(reader.getString());"
                    + "        } else if (\"isAgreementToNotResellRequired\".equals(fieldName)) {"
                    + "            deserializedAvailablePhoneNumber.isAgreementToNotResellRequired = reader.getNullable(JsonReader::getBoolean);"
                    + "        } else if (\"error\".equals(fieldName)) {"
                    + "            deserializedAvailablePhoneNumber.error = ResponseError.fromJson(reader);"
                    + "        } else {"
                    + "            reader.skipChildren();"
                    + "        }"
                    + "    }"
                    + "    return deserializedAvailablePhoneNumber;"
                    + " }); }"));
            });
        });
    }

    // Customizes the BrowsePhoneNumbersOptions class
    private void customizeBrowsePhoneNumbersOptions(PackageCustomization models) {
        models.getClass("BrowsePhoneNumbersOptions").customizeAst(ast -> ast.getClassByName("BrowsePhoneNumbersOptions")
            .ifPresent(clazz -> {
                // Remove the default constructor
                clazz.getConstructors().forEach(Node::remove);

                clazz.addConstructor(Modifier.Keyword.PRIVATE);

                // Remove a specific method by name
                clazz.getMethodsByName("setCountryCode").forEach(Node::remove);
                clazz.getMethodsByName("setPhoneNumberType").forEach(Node::remove);

                // Add a new constructor with required parameters
                clazz.addConstructor(Modifier.Keyword.PUBLIC)
                    .addParameter("String", "countryCode")
                    .addParameter("PhoneNumberType", "phoneNumberType")
                    .setBody(StaticJavaParser.parseBlock("{ this.countryCode = countryCode; this.phoneNumberType = phoneNumberType; }"))
                    .setJavadocComment(new Javadoc(parseText("Creates an instance of BrowsePhoneNumbersOptions with required parameters."))
                        .addBlockTag("param", "countryCode The ISO 3166-2 country code, e.g., US.")
                        .addBlockTag("param", "phoneNumberType The type of phone number."));
            }));
    }

    // Customizes the PhoneNumberSearchResult class
    private void customizePhoneNumberSearchResult(PackageCustomization models) {
        // Remove the specified import
        models.getClass("PhoneNumberSearchResult").customizeAst(ast -> ast.getImports()
            .removeIf(importDeclaration -> importDeclaration.getNameAsString()
                .equals("com.azure.communication.phonenumbers.implementation.models.PhoneNumberSearchResultError")));
    }

    // Combined toJson customization for all relevant models
    private void customizeToJson(PackageCustomization models) {
        // AvailablePhoneNumber
        models.getClass("AvailablePhoneNumber").customizeAst(ast -> {
            ast.getClassByName("AvailablePhoneNumber").ifPresent(clazz -> {
                clazz.getMethodsByName("toJson").forEach(method -> 
                    method.setBody(StaticJavaParser.parseBlock(
                        "{\n" +
                        "    jsonWriter.writeStartObject();\n" +
                        "    if (this.countryCode != null) { jsonWriter.writeStringField(\"countryCode\", this.countryCode); }\n" +
                        "    if (this.capabilities != null) { jsonWriter.writeFieldName(\"capabilities\"); this.capabilities.toJson(jsonWriter); }\n" +
                        "    if (this.phoneNumberType != null) { jsonWriter.writeStringField(\"phoneNumberType\", this.phoneNumberType.toString()); }\n" +
                        "    if (this.assignmentType != null) { jsonWriter.writeStringField(\"assignmentType\", this.assignmentType.toString()); }\n" +
                        "    if (this.id != null) { jsonWriter.writeStringField(\"id\", this.id); }\n" +
                        "    if (this.phoneNumber != null) { jsonWriter.writeStringField(\"phoneNumber\", this.phoneNumber); }\n" +
                        "    if (this.cost != null) { jsonWriter.writeFieldName(\"cost\"); this.cost.toJson(jsonWriter); }\n" +
                        "    if (this.status != null) { jsonWriter.writeStringField(\"status\", this.status.toString()); }\n" +
                        "    if (this.isAgreementToNotResellRequired != null) { jsonWriter.writeBooleanField(\"isAgreementToNotResellRequired\", this.isAgreementToNotResellRequired); }\n" +
                        "    return jsonWriter.writeEndObject();\n" +
                        "}")));
            });
        });
        // PhoneNumbersReservation
        models.getClass("PhoneNumbersReservation").customizeAst(ast -> {
            ast.getClassByName("PhoneNumbersReservation").ifPresent(clazz -> {
                clazz.getMethodsByName("toJson").forEach(method -> 
                    method.setBody(StaticJavaParser.parseBlock(
                        "{\n" +
                        "    jsonWriter.writeStartObject();\n" +
                        "    if (this.id != null) { jsonWriter.writeStringField(\"id\", this.id.toString()); }\n" +
                        "    if (this.expiresAt != null) { jsonWriter.writeStringField(\"expiresAt\", this.expiresAt.toString()); }\n" +
                        "    if (this.phoneNumbers != null) {\n" +
                        "        jsonWriter.writeMapField(\"phoneNumbers\", this.phoneNumbers, (writer, value) -> {\n" +
                        "            if (value != null) { value.toJson(writer); } else { writer.writeNull(); }\n" +
                        "        });\n" +
                        "    }\n" +
                        "    if (this.status != null) { jsonWriter.writeStringField(\"status\", this.status.toString()); }\n" +
                        "    return jsonWriter.writeEndObject();\n" +
                        "}")));
            });
        });
        // PhoneNumberCost
        models.getClass("PhoneNumberCost").customizeAst(ast -> {
            ast.getClassByName("PhoneNumberCost").ifPresent(clazz -> {
                clazz.getMethodsByName("toJson").forEach(method -> 
                    method.setBody(StaticJavaParser.parseBlock(
                        "{\n" +
                        "    jsonWriter.writeStartObject();\n" +
                        "    jsonWriter.writeNumberField(\"amount\", this.amount);\n" +
                        "    if (this.currencyCode != null) { jsonWriter.writeStringField(\"currencyCode\", this.currencyCode); }\n" +
                        "    if (this.billingFrequency != null) { jsonWriter.writeStringField(\"billingFrequency\", this.billingFrequency.toString()); }\n" +
                        "    return jsonWriter.writeEndObject();\n" +
                        "}")));
            });
        });
    }
}
