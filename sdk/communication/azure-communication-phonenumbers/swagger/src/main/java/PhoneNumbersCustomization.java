// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

/**
 * Contains customizations for PhoneNumbersCustomization configuration.
 */
public class PhoneNumbersCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
      PackageCustomization models = customization.getPackage("com.azure.communication.phonenumbers.models");

      customizeAvailablePhoneNumber(models);
      customizePhoneNumbersReservation(models);
    }

    // Customizes the PhoneNumbersReservation class
    private void customizePhoneNumbersReservation(PackageCustomization models) {
      models.getClass("PhoneNumbersReservation").customizeAst(ast -> {  
      ast.getClassByName("PhoneNumbersReservation").ifPresent(clazz -> {
        clazz.addMethod("setPhoneNumbers", Modifier.Keyword.PRIVATE)
                    .setType("PhoneNumbersReservation")
                    .addParameter("Map<String, AvailablePhoneNumber>", "phoneNumbers")
                    .setBody(StaticJavaParser.parseBlock("{ this.phoneNumbers = phoneNumbers;\r\n" + //
                                            "        return this; }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Set the phoneNumbers property: The phone numbers in the reservation."))
                    .addBlockTag("param", "phoneNumbers the phone numbers in the reservation.")
                    .addBlockTag("return", "the PhoneNumbersReservation object itself."));

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
                  .setJavadocComment(new Javadoc(JavadocDescription.parseText("Get the error property: The error object."))
                      .addBlockTag("return", "the error value."));
              clazz.getFieldByName("error").get().getVariable(0).setType("ResponseError");
              clazz.getMethodsByName("fromJson").get(0)
              .setBody(StaticJavaParser.parseBlock("{ " +
              " return jsonReader.readObject(reader -> { " +
              " AvailablePhoneNumber deserializedAvailablePhoneNumber = new AvailablePhoneNumber(); " +
             " while (reader.nextToken() != JsonToken.END_OBJECT) { " +
              " String fieldName = reader.getFieldName(); " +
                " reader.nextToken(); " +
              " if (\"countryCode\".equals(fieldName)) { " +
                  " deserializedAvailablePhoneNumber.countryCode = reader.getString();" +
              " } else if (\"capabilities\".equals(fieldName)) { " +
                  " deserializedAvailablePhoneNumber.capabilities = PhoneNumberCapabilities.fromJson(reader); " +
              " } else if (\"phoneNumberType\".equals(fieldName)) { " +
                  " deserializedAvailablePhoneNumber.phoneNumberType = PhoneNumberType.fromString(reader.getString()); " +
              " } else if (\"assignmentType\".equals(fieldName)) { " +
                  " deserializedAvailablePhoneNumber.assignmentType " +
                      "= PhoneNumberAssignmentType.fromString(reader.getString()); " +
              " } else if (\"id\".equals(fieldName)) { " +
                  " deserializedAvailablePhoneNumber.id = reader.getString(); " +
              " } else if (\"phoneNumber\".equals(fieldName)) { " +
                  " deserializedAvailablePhoneNumber.phoneNumber = reader.getString(); " +
              " } else if (\"cost\".equals(fieldName)) { " +
                  " deserializedAvailablePhoneNumber.cost = PhoneNumberCost.fromJson(reader); " +
              " } else if (\"status\".equals(fieldName)) { " +
                  " deserializedAvailablePhoneNumber.status " +
                      "= PhoneNumberAvailabilityStatus.fromString(reader.getString()); " +
              " } else if (\"isAgreementToNotResellRequired\".equals(fieldName)) { " +
                  " deserializedAvailablePhoneNumber.isAgreementToNotResellRequired " +
                      "= reader.getNullable(JsonReader::getBoolean); " +
              " } else if (\"error\".equals(fieldName)) { " +
                  " deserializedAvailablePhoneNumber.error = ResponseError.fromJson(reader); " +
              " } else { " +
                  " reader.skipChildren(); " +
              " } " +   
            " } " +
            " return deserializedAvailablePhoneNumber; " +
            " }); " +
            " }"));
          });
      });
  } 
}
