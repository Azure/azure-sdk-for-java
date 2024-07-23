// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

/**
 * Customization class for Queue Storage.
 */
public class SearchCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.maps.search.models");
        PackageCustomization implementationModels = customization.getPackage("com.azure.maps.search.implementation.models");

        // customize AddressRanges
        customizeAddressRanges(models);

        // customize EntryPoint
        customizeEntryPoint(models);

        // customize SearchAddressResult
        customizeSearchAddressResult(models);

        // customize SearchAddressResultItem
        customizeSearchAddressResultItem(models);

        // customize SearchSummary
        customizeSearchSummary(models);

        // customize ReverseSearchAddressResult
        customizeReverseSearchAddressResult(models);

        // customize ReverseSearchAddressResultItem
        customizeReverseSearchAddressResultItem(models);

        // customize ReverseSearchCrossStreetAddressResultItem
        customizeReverseSearchCrossStreetAddressResultItem(models);

        // customizePolygon
        customizePolygon(models);

        // customize operating hours time
        customizeOperatingHoursTime(implementationModels);

        // customize operating hours time range
        customizeOperatingHoursTimeRange(models);

        // customize data source
        customizeDataSource(models);

        // customize geometry identifier
        customizeGeometryIdentifier(models);

        // customize Reverse Search Address Batch Item Private Response
        customizeReverseSearchAddressBatchItemPrivateResponse(implementationModels);

        // customize Search Address Batch Item Private Response
        customizeSearchAddressBatchItemPrivateResponse(implementationModels);

        // customize Error Detail
        customizeErrorDetail(implementationModels);

        // customize MapsSearchAddress
        customizeMapsSearchAddress(models);
    }

    // Customizes the MapsSearchAddress class by changing the type of BoundingBox
    private void customizeMapsSearchAddress(PackageCustomization models) {
        models.getClass("MapsSearchAddress").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.GeoBoundingBox");

            ast.getClassByName("MapsSearchAddress").ifPresent(clazz -> {
                clazz.getMethodsByName("getBoundingBox").get(0)
                    .setType("GeoBoundingBox")
                    .setBody(StaticJavaParser.parseBlock("{ return com.azure.maps.search.implementation.helpers.Utility.toGeoBoundingBox(this.boundingBox); }"));

                clazz.getMethodsByName("getCountryCodeISO3").get(0).setName("getCountryCodeIso3");
            });
        });
    }


    // Customizes the AddressRanges class
    private void customizeAddressRanges(PackageCustomization models) {
        models.getClass("AddressRanges").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.GeoPosition");

            ast.getClassByName("AddressRanges").ifPresent(clazz -> {
                clazz.getMethodsByName("setRangeRight").forEach(Node::remove);
                clazz.getMethodsByName("setRangeLeft").forEach(Node::remove);
                clazz.getMethodsByName("setTo").forEach(Node::remove);
                clazz.getMethodsByName("setFrom").forEach(Node::remove);

                clazz.getMethodsByName("getTo").get(0)
                    .setType("GeoPosition")
                    .setBody(StaticJavaParser.parseBlock("{ return new GeoPosition(this.to.getLon(), this.to.getLat()); }"));

                clazz.getMethodsByName("getFrom").get(0)
                    .setType("GeoPosition")
                    .setBody(StaticJavaParser.parseBlock("{ return new GeoPosition(this.from.getLon(), this.from.getLat()); }"));
            });
        });
    }

    // Customizes the EntryPoint class
    private void customizeEntryPoint(PackageCustomization models) {
        models.getClass("EntryPoint").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.GeoPosition");

            ast.getClassByName("EntryPoint").ifPresent(clazz -> {
                clazz.getMethodsByName("setPosition").forEach(Node::remove);

                clazz.getMethodsByName("getPosition").get(0)
                    .setType("GeoPosition")
                    .setBody(StaticJavaParser.parseBlock("{ return new GeoPosition(this.position.getLon(), this.position.getLat()); }"));
            });
        });
    }

    // Customizes the SearchAddressResult class
    private void customizeSearchAddressResult(PackageCustomization models) {
        models.getClass("SearchAddressResult").customizeAst(ast -> {
            ast.addImport("com.azure.maps.search.implementation.helpers.SearchAddressResultPropertiesHelper");

            ast.getClassByName("SearchAddressResult")
                .ifPresent(clazz -> clazz.addMember(new InitializerDeclaration(true, StaticJavaParser.parseBlock(String.join("\n",
                    "{",
                    "SearchAddressResultPropertiesHelper.setAccessor(new SearchAddressResultPropertiesHelper.SearchAddressResultAccessor() {",
                    "    @Override",
                    "    public void setSummary(SearchAddressResult searchAddressResult, SearchSummary summary) {",
                    "        searchAddressResult.setSummary(summary);",
                    "    }",
                    "",
                    "    @Override",
                    "    public void setResults(SearchAddressResult searchAddressResult, List<SearchAddressResultItem> results) {",
                    "        searchAddressResult.setResults(results);",
                    "    }\n",
                    "});",
                    "}"
                )))));
        });
    }

    // Customizes the SearchAddressResultItem class
    private void customizeSearchAddressResultItem(PackageCustomization models) {
        models.getClass("SearchAddressResultItem").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.GeoPosition");
            ast.addImport("com.azure.core.models.GeoBoundingBox");

            ast.getClassByName("SearchAddressResultItem").ifPresent(clazz -> {
                clazz.getMethodsByName("setPosition").forEach(Node::remove);

                clazz.getMethodsByName("getPosition").get(0)
                    .setType("GeoPosition")
                    .setBody(StaticJavaParser.parseBlock("{ return new GeoPosition(this.position.getLon(), this.position.getLat()); }"));

                clazz.getMethodsByName("getViewport").get(0)
                    .setName("getBoundingBox")
                    .setType("GeoBoundingBox")
                    .setBody(StaticJavaParser.parseBlock("{ return new GeoBoundingBox(this.viewport.getTopLeft().getLon(), this.viewport.getTopLeft().getLat(), this.viewport.getBottomRight().getLon(), this.viewport.getBottomRight().getLat()); }"));

                clazz.getMethodsByName("getDataSources").get(0).setName("getDataSource");
            });
        });
    }

    // Customizes the SearchSummary class
    private void customizeSearchSummary(PackageCustomization models) {
        models.getClass("SearchSummary").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.GeoPosition");

            ast.getClassByName("SearchSummary").ifPresent(clazz -> clazz.getMethodsByName("getGeoBias").get(0)
                .setType("GeoPosition")
                .setBody(StaticJavaParser.parseBlock("{ return this.geoBias != null ? new GeoPosition(this.geoBias.getLon(), this.geoBias.getLat()) : null; }")));
        });
    }

    // Customizes the ReverseSearchAddressResult class
    private void customizeReverseSearchAddressResult(PackageCustomization models) {
        models.getClass("ReverseSearchAddressResult").customizeAst(ast -> {
            ast.addImport("com.azure.maps.search.implementation.helpers.ReverseSearchAddressResultPropertiesHelper");

            ast.getClassByName("ReverseSearchAddressResult")
                .ifPresent(clazz -> clazz.addMember(new InitializerDeclaration(true, StaticJavaParser.parseBlock(String.join("\n",
                    "{",
                    "ReverseSearchAddressResultPropertiesHelper.setAccessor(new ReverseSearchAddressResultPropertiesHelper.ReverseSearchAddressResultAccessor() {",
                    "    @Override",
                    "    public void setSummary(ReverseSearchAddressResult reverseSearchAddressResult, SearchSummary summary) {",
                    "        reverseSearchAddressResult.setSummary(summary);",
                    "    }",
                    "",
                    "    @Override",
                    "    public void setAddresses(ReverseSearchAddressResult reverseSearchAddressResult, List<ReverseSearchAddressResultItem> results) {",
                    "        reverseSearchAddressResult.setAddresses(results);",
                    "    }\n",
                    "});",
                    "}"
                )))));
        });
    }

    // Customizes the ReverseSearchAddressResultItem class
    private void customizeReverseSearchAddressResultItem(PackageCustomization models) {
        models.getClass("ReverseSearchAddressResultItem").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.GeoPosition");

            ast.getClassByName("ReverseSearchAddressResultItem").ifPresent(clazz -> clazz.getMethodsByName("getPosition").get(0)
                .setType("GeoPosition")
                .setBody(StaticJavaParser.parseBlock("{ return com.azure.maps.search.implementation.helpers.Utility.fromCommaSeparatedString(this.position); }")));
        });
    }

    // Customizes the ReverseSearchCrossStreetAddressResultItem class
    private void customizeReverseSearchCrossStreetAddressResultItem(PackageCustomization models) {
        models.getClass("ReverseSearchCrossStreetAddressResultItem").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.GeoPosition");

            ast.getClassByName("ReverseSearchCrossStreetAddressResultItem").ifPresent(clazz -> clazz.getMethodsByName("getPosition").get(0)
                .setType("GeoPosition")
                .setBody(StaticJavaParser.parseBlock("{ return com.azure.maps.search.implementation.helpers.Utility.fromCommaSeparatedString(this.position); }")));
        });
    }

    // Customizes the Polygon class
    private void customizePolygon(PackageCustomization models) {
        models.getClass("MapsPolygon").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.GeoObject");

            ast.getClassByName("MapsPolygon").ifPresent(clazz -> {
                clazz.getMethodsByName("getGeometryData").get(0)
                    .setName("getGeometry")
                    .setType("GeoObject")
                    .setBody(StaticJavaParser.parseBlock("{ return com.azure.maps.search.implementation.helpers.Utility.toGeoObject(this.geometryData); }"));

                clazz.getMethodsByName("setGeometryData").forEach(Node::remove);

                clazz.getMethodsByName("getProviderID").get(0).setName("getProviderId");
            });
        });
    }

    // Customizes the OperatingHoursTime class
    private void customizeOperatingHoursTime(PackageCustomization implementationModels) {
        implementationModels.getClass("OperatingHoursTime").customizeAst(ast -> ast.getClassByName("OperatingHoursTime")
            .ifPresent(clazz -> {
                clazz.getConstructors().get(0).setModifiers(Modifier.Keyword.PRIVATE);
                clazz.addConstructor(Modifier.Keyword.PUBLIC)
                    .addParameter("String", "date")
                    .addParameter("Integer", "hour")
                    .addParameter("Integer", "minute")
                    .setBody(StaticJavaParser.parseBlock("{ this.date = date; this.hour = hour; this.minute = minute; }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("OperatingHoursTime Constructor"))
                        .addBlockTag("param", "date", "The date in the format of yyyy-mm-dd represented by a string")
                        .addBlockTag("param", "hour", "int representing the hour")
                        .addBlockTag("param", "minute", "int representing the minute"));
            }));
    }

    // Customizes the OperatingHoursTimeRange class
    private void customizeOperatingHoursTimeRange(PackageCustomization models) {
        models.getClass("OperatingHoursTimeRange").customizeAst(ast -> {
            ast.addImport("java.time.LocalDateTime");

            ast.getClassByName("OperatingHoursTimeRange").ifPresent(clazz -> {
                clazz.getConstructors().get(0).setModifiers(Modifier.Keyword.PRIVATE);

                // add constructor
                clazz.addConstructor(Modifier.Keyword.PUBLIC)
                    .addParameter("LocalDateTime", "startTime")
                    .addParameter("LocalDateTime", "endTime")
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "this.startTime = new OperatingHoursTime(startTime.toLocalDate().toString(), startTime.getHour(), startTime.getMinute());"
                        + "this.endTime = new OperatingHoursTime(endTime.toLocalDate().toString(), endTime.getHour(), endTime.getMinute());"
                        + "}"))
                    .setJavadocComment(
                        new Javadoc(JavadocDescription.parseText("OperatingHoursTimeRange constructor")).addBlockTag(
                                "param", "startTime",
                                "The point in the next 7 days range when a given POI is being opened, or the beginning of the range if it was opened before the range.")
                            .addBlockTag("param", "endTime",
                                "The point in the next 7 days range when a given POI is being closed, or the beginning of the range if it was closed before the range."));

                // get start time
                clazz.getMethodsByName("getStartTime")
                    .get(0)
                    .setType("LocalDateTime")
                    .setBody(StaticJavaParser.parseBlock("{" + "String[] date = this.startTime.toString().split(\"-\");"
                        + "int year = Integer.parseInt(date[0]);" + "int month = Integer.parseInt(date[1]);"
                        + "int day = Integer.parseInt(date[2]);"
                        + "return LocalDateTime.of(year, month, day, this.startTime.getHour(), this.startTime.getMinute());"
                        + "}"));

                // set start time
                clazz.getMethodsByName("setStartTime")
                    .get(0)
                    .setParameters(new NodeList<>(new Parameter().setName("startTime").setType("LocalDateTime")))
                    .setBody(StaticJavaParser.parseBlock(
                        "{ this.startTime = new OperatingHoursTime(startTime.toLocalDate().toString(), startTime.getHour(), startTime.getMinute()); return this; }"));

                // get end time
                clazz.getMethodsByName("getEndTime")
                    .get(0)
                    .setType("LocalDateTime")
                    .setBody(StaticJavaParser.parseBlock("{" + "String[] date = this.endTime.toString().split(\"-\");"
                        + "int year = Integer.parseInt(date[0]);" + "int month = Integer.parseInt(date[1]);"
                        + "int day = Integer.parseInt(date[2]);"
                        + "return LocalDateTime.of(year, month, day, this.endTime.getHour(), this.endTime.getMinute());"
                        + "}"));

                // set end time
                clazz.getMethodsByName("setEndTime")
                    .get(0)
                    .setParameters(new NodeList<>(new Parameter().setName("endTime").setType("LocalDateTime")))
                    .setBody(StaticJavaParser.parseBlock(
                        "{ this.endTime = new OperatingHoursTime(endTime.toLocalDate().toString(), endTime.getHour(), endTime.getMinute()); return this; }"));
            });
        });
    }

    // Customizes the GeometryIdentifier class
    private void customizeGeometryIdentifier(PackageCustomization models) {
        models.getClass("GeometryIdentifier").customizeAst(ast -> ast.getClassByName("GeometryIdentifier").ifPresent(clazz -> {
            clazz.getAnnotationByName("Immutable").ifPresent(Node::remove);
            clazz.addAnnotation(new MarkerAnnotationExpr("Fluent"));

            clazz.addMethod("setId", Modifier.Keyword.PUBLIC)
                .setType("GeometryIdentifier")
                .addParameter("String", "id")
                .setBody(StaticJavaParser.parseBlock("{ this.id = id; return this; }"))
                .setJavadocComment(new Javadoc(JavadocDescription.parseText("Set the id property: Pass this as geometryId to the [Get Search Polygon] (https://docs.microsoft.com/rest/api/maps/search/getsearchpolygon) API to fetch geometry information for this result."))
                    .addBlockTag("param", "id", "The geometryId")
                    .addBlockTag("return", "the updated GeometryIdentifier object"));
        }));
    }

    // Customizes the DataSource class
    private void customizeDataSource(PackageCustomization models) {
        models.getClass("DataSource").customizeAst(ast -> ast.getClassByName("DataSource").ifPresent(clazz -> {
            clazz.addConstructor(Modifier.Keyword.PUBLIC)
                .addParameter("String", "geometry")
                .setBody(StaticJavaParser.parseBlock("{ this.geometry = new GeometryIdentifier().setId(geometry); }"))
                .setJavadocComment(new Javadoc(JavadocDescription.parseText("Creates an instance of DataSource class."))
                    .addBlockTag("param", "geometry", "this is geometry id"));

            clazz.getMethodsByName("getGeometry").get(0)
                .setType("String")
                .setBody(StaticJavaParser.parseBlock("{ return this.geometry.toString(); }"));

            clazz.getMethodsByName("setGeometry").get(0)
                .setParameters(new NodeList<>(new Parameter().setName("geometry").setType("String")))
                .setBody(StaticJavaParser.parseBlock("{ this.geometry = new GeometryIdentifier().setId(geometry); return this; }"));
        }));
    }

    // Customizes the ReverseSearchAddressBatchItemPrivateResponse class
    private void customizeReverseSearchAddressBatchItemPrivateResponse(PackageCustomization implementationModels) {
        implementationModels.getClass("ReverseSearchAddressBatchItemPrivateResponse").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.ResponseError");
            ast.addImport("com.azure.maps.search.implementation.helpers.ReverseSearchAddressResultPropertiesHelper");

            ast.getClassByName("ReverseSearchAddressBatchItemPrivateResponse").ifPresent(clazz -> {
                clazz.getMethodsByName("getError").get(0)
                    .setType("ResponseError")
                    .setBody(StaticJavaParser.parseBlock("{ if (this.error == null) { return new ResponseError(\"\", \"\"); } return new ResponseError(this.error.getCode(), this.error.getMessage()); }"));

                clazz.getMethodsByName("setError").get(0)
                    .setParameters(new NodeList<>(new Parameter().setName("error").setType("ResponseError")))
                    .setBody(StaticJavaParser.parseBlock("{ this.error = new ErrorDetail().setCode(error.getCode()).setMessage(error.getMessage()); return this; }"));

                MethodDeclaration fromJson = clazz.getMethodsByName("fromJson").get(0);
                String body = fromJson.getBody().get().toString();
                body = body.replace("deserializedReverseSearchAddressBatchItemPrivateResponse.setSummary(SearchSummary.fromJson(reader));",
                    "ReverseSearchAddressResultPropertiesHelper.setSummary(deserializedReverseSearchAddressBatchItemPrivateResponse, SearchSummary.fromJson(reader));");
                body = body.replace("deserializedReverseSearchAddressBatchItemPrivateResponse.setAddresses(addresses);",
                    "ReverseSearchAddressResultPropertiesHelper.setAddresses(deserializedReverseSearchAddressBatchItemPrivateResponse, addresses);");
                fromJson.setBody(StaticJavaParser.parseBlock(body));
            });
        });
    }

    // Customizes the SearchAddressBatchItemPrivateResponse class
    private void customizeSearchAddressBatchItemPrivateResponse(PackageCustomization implementationModels) {
        implementationModels.getClass("SearchAddressBatchItemPrivateResponse").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.ResponseError");
            ast.addImport("com.azure.maps.search.implementation.helpers.SearchAddressResultPropertiesHelper");

            ast.getClassByName("SearchAddressBatchItemPrivateResponse").ifPresent(clazz -> {
                clazz.getMethodsByName("getError").get(0)
                    .setType("ResponseError")
                    .setBody(StaticJavaParser.parseBlock("{ if (this.error == null) { return new ResponseError(\"\", \"\"); } return new ResponseError(this.error.getCode(), this.error.getMessage()); }"));

                clazz.getMethodsByName("setError").get(0)
                    .setParameters(new NodeList<>(new Parameter().setName("error").setType("ResponseError")))
                    .setBody(StaticJavaParser.parseBlock("{ this.error = new ErrorDetail().setCode(error.getCode()).setMessage(error.getMessage()); return this; }"));

                MethodDeclaration fromJson = clazz.getMethodsByName("fromJson").get(0);
                String body = fromJson.getBody().get().toString();
                body = body.replace("deserializedSearchAddressBatchItemPrivateResponse.setSummary(SearchSummary.fromJson(reader));",
                    "SearchAddressResultPropertiesHelper.setSummary(deserializedSearchAddressBatchItemPrivateResponse, SearchSummary.fromJson(reader));");
                body = body.replace("deserializedSearchAddressBatchItemPrivateResponse.setResults(results);",
                    "SearchAddressResultPropertiesHelper.setResults(deserializedSearchAddressBatchItemPrivateResponse, results);");
                fromJson.setBody(StaticJavaParser.parseBlock(body));
            });
        });
    }

    // customize error detail
    private void customizeErrorDetail(PackageCustomization implementationModels) {
        implementationModels.getClass("ErrorDetail").customizeAst(ast -> ast.getClassByName("ErrorDetail").ifPresent(clazz -> {
            clazz.addMethod("setCode", Modifier.Keyword.PUBLIC)
                .setType("ErrorDetail")
                .addParameter("String", "code")
                .setBody(StaticJavaParser.parseBlock("{ this.code = code; return this; }"))
                .setJavadocComment(new Javadoc(JavadocDescription.parseText("Set the code property: The code object."))
                    .addBlockTag("param", "code", "the code value to set.")
                    .addBlockTag("return", "the ErrorDetail object itself."));

            clazz.addMethod("setMessage", Modifier.Keyword.PUBLIC)
                .setType("ErrorDetail")
                .addParameter("String", "message")
                .setBody(StaticJavaParser.parseBlock("{ this.message = message; return this; }"))
                .setJavadocComment(new Javadoc(JavadocDescription.parseText("Set the message property: The message object."))
                    .addBlockTag("param", "message", "the message value to set.")
                    .addBlockTag("return", "the ErrorDetail object itself."));
        }));
    }
}
