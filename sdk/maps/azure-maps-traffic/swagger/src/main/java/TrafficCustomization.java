import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

public class TrafficCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.maps.traffic.models");

        // customize TrafficFlowSegmentDataFlowSegmentDataCoordinates
        customizeTrafficFlowSegmentDataFlowSegmentDataCoordinates(models);

        // customize TrafficIncidentViewport
        customizeTrafficIncidentViewport(models);

        // customize TrafficIncidentViewportViewpResp
        customizeTrafficIncidentViewportViewpResp(models);

        // customize Point
        customizePoint(models);

        // customize TrafficFlowSegmentData
        customizeTrafficFlowSegmentData(models);

        // customize TrafficIncidentDetail
        customizeTrafficIncidentDetail(models);

        // customize TrafficIncidentPointOfInterest
        customizeTrafficIncidentPointOfInterest(models);

        // customize TrafficState
        customizeTrafficState(models);

        // customize TrafficIncidentViewportResponse
        customizeTrafficIncidentViewportResponse(models);
    }

    // Customizes the TrafficFlowSegmentDataFlowSegmentDataCoordinates class
    private void customizeTrafficFlowSegmentDataFlowSegmentDataCoordinates(PackageCustomization models) {
        models.getClass("TrafficFlowSegmentDataFlowSegmentDataCoordinates").customizeAst(ast -> {
            ast.addImport("java.util.List");
            ast.addImport("java.util.stream.Collectors");
            ast.addImport("java.util.Arrays");
            ast.addImport("com.azure.core.models.GeoPosition");

            ast.getClassByName("TrafficFlowSegmentDataFlowSegmentDataCoordinates").ifPresent(clazz -> {
                clazz.getConstructors().get(0)
                    .setModifiers(Modifier.Keyword.PRIVATE)
                    .setJavadocComment("Set default constructor to private");

                clazz.getMethodsByName("getCoordinates").forEach(Node::remove);

                clazz.addMethod("getCoordinates", Modifier.Keyword.PUBLIC)
                    .setType("List<GeoPosition>")
                    .setBody(StaticJavaParser.parseBlock("{ return this.coordinates.stream()" +
                        "       .map(item -> new GeoPosition(item.getLongitude(), item.getLatitude()))" +
                        "       .collect(Collectors.toList()); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Return the coordinates"))
                        .addBlockTag("return", "Returns a list of {@link GeoPosition} coordinates."));
            });
        });

        // Rename required as the class name is from handling an object definition within an object definition, so
        // the rename directive doesn't work for this.
        models.getClass("TrafficFlowSegmentDataFlowSegmentDataCoordinates")
            .rename("TrafficFlowSegmentDataPropertiesCoordinates");
    }

    // Customizes the TrafficIncidentViewport class
    private void customizeTrafficIncidentViewport(PackageCustomization models) {
        models.getClass("TrafficIncidentViewport").customizeAst(ast -> ast.getClassByName("TrafficIncidentViewport")
            .ifPresent(clazz -> {
                clazz.getConstructors().get(0)
                    .setModifiers(Modifier.Keyword.PRIVATE)
                    .setJavadocComment("Set default constructor to private");

                clazz.getMethodsByName("getViewpResp").get(0).setName("getViewportResponse");
            }));
    }

    // Customizes the TrafficIncidentViewportResponse class
    private void customizeTrafficIncidentViewportViewpResp(PackageCustomization models) {
        models.getClass("TrafficIncidentViewportViewpResp").rename("TrafficIncidentViewportResponse");
    }

     // Customizes the Point class
     private void customizePoint(PackageCustomization models) {
         models.getClass("MapsPoint").customizeAst(ast -> ast.getClassByName("MapsPoint").ifPresent(clazz ->
             clazz.getConstructors().get(0)
                 .setModifiers(Modifier.Keyword.PRIVATE)
                 .setJavadocComment("Set default constructor to private")));
     }

     // Customizes the TrafficFlowSegmentData class
     private void customizeTrafficFlowSegmentData(PackageCustomization models) {
         models.getClass("TrafficFlowSegmentData").customizeAst(ast -> ast.getClassByName("TrafficFlowSegmentData")
             .ifPresent(clazz -> {
                 clazz.getConstructors().get(0)
                     .setModifiers(Modifier.Keyword.PRIVATE)
                     .setJavadocComment("Set default constructor to private");

                 clazz.getMethodsByName("getFlowSegmentData").forEach(Node::remove);
                 clazz.addMethod("getFunctionalRoadClass", Modifier.Keyword.PUBLIC)
                     .setType("String")
                        .setBody(StaticJavaParser.parseBlock("{ return this.flowSegmentData.getFunctionalRoadClass(); }"))
                        .setJavadocComment(new Javadoc(JavadocDescription.parseText("Get the functionalRoadClass "
                            + "property: Functional Road Class. This indicates the road type: 0: Motorway, freeway or "
                            + "other major road. 1: Major road, less important than a motorway. 2: Other major road. "
                            + "3: Secondary road. 4: Local connecting road. 5: Local road of high importance. "
                            + "6: Local road."))
                            .addBlockTag("return", "the functionalRoadClass value."));

                 clazz.addMethod("getCurrentSpeed", Modifier.Keyword.PUBLIC)
                     .setType("Integer")
                     .setBody(StaticJavaParser.parseBlock("{ return this.flowSegmentData.getCurrentSpeed(); }"))
                     .setJavadocComment(new Javadoc(JavadocDescription.parseText("Get the currentSpeed property: The "
                         + "current average speed at the selected point, in the units requested. This is calculated "
                         + "from the currentTravelTime and the length of the selected segment."))
                         .addBlockTag("return", "the currentSpeed value."));

                clazz.addMethod("getFreeFlowSpeed", Modifier.Keyword.PUBLIC)
                    .setType("Integer")
                    .setBody(StaticJavaParser.parseBlock("{ return this.flowSegmentData.getFreeFlowSpeed(); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Get the freeFlowSpeed property: The "
                        + "free flow speed expected under ideal conditions, expressed in the units requested. This is "
                        + "related to the freeFlowTravelTime."))
                        .addBlockTag("return", "the freeFlowSpeed value."));

                clazz.addMethod("getCurrentTravelTime", Modifier.Keyword.PUBLIC)
                    .setType("Integer")
                    .setBody(StaticJavaParser.parseBlock("{ return this.flowSegmentData.getCurrentTravelTime(); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Get the currentTravelTime property: "
                        + "Current travel time in seconds, across this traffic segment, based on fused real-time "
                        + "measurements between the defined locations in the specified direction."))
                        .addBlockTag("return", "the currentTravelTime value."));

                clazz.addMethod("getFreeFlowTravelTime", Modifier.Keyword.PUBLIC)
                    .setType("Integer")
                    .setBody(StaticJavaParser.parseBlock("{ return this.flowSegmentData.getFreeFlowTravelTime(); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Get the freeFlowTravelTime property: "
                        + "The travel time in seconds, across this traffic segment, which would be expected under "
                        + "ideal free flow conditions."))
                        .addBlockTag("return", "the freeFlowTravelTime value."));

                clazz.addMethod("getConfidence", Modifier.Keyword.PUBLIC)
                    .setType("Float")
                    .setBody(StaticJavaParser.parseBlock("{ return this.flowSegmentData.getConfidence(); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Get the confidence property: The "
                        + "confidence is a measure of the quality of the provided travel time and speed. A value of 1 "
                        + "means full confidence, that the response contains the highest quality data. Lower values "
                        + "indicate the degree that the response may vary from the actual conditions on the road. Any "
                        + "value greater than 0.6 means the information was based on real-time probe input. A value of "
                        + "0.5 means the reported speed is based on historical info. A value between 0.5 and 0.6 has a "
                        + "calculated weighted average between historical and live speeds."))
                        .addBlockTag("return", "the confidence value."));

                clazz.addMethod("getCoordinates", Modifier.Keyword.PUBLIC)
                    .setType("TrafficFlowSegmentDataPropertiesCoordinates")
                    .setBody(StaticJavaParser.parseBlock("{ return this.flowSegmentData.getCoordinates(); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Get the coordinates property: Includes "
                        + "the coordinates describing the shape of the segment. Coordinates are shifted from the road "
                        + "depending on the zoom level to support high quality visualization in every scale."))
                        .addBlockTag("return", "the coordinates value."));

                clazz.addMethod("getVersion", Modifier.Keyword.PUBLIC)
                    .setType("String")
                    .setBody(StaticJavaParser.parseBlock("{ return this.flowSegmentData.getVersion(); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Get the version property: This "
                        + "indicates the software version that generated the response."))
                        .addBlockTag("return", "the version value."));

                clazz.addMethod("getOpenLrCode", Modifier.Keyword.PUBLIC)
                    .setType("String")
                    .setBody(StaticJavaParser.parseBlock("{ return this.flowSegmentData.getOpenLrCode(); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Get the openLrCode property: OpenLR "
                        + "code for segment. See [OpenLR](https://en.wikipedia.org/wiki/OpenLR) for more information "
                        + "on the use of Open LR codes."))
                        .addBlockTag("return", "the openLrCode value."));
             }));
     }

     // Customizes the TrafficIncidentDetail class
     private void customizeTrafficIncidentDetail(PackageCustomization models) {
         models.getClass("TrafficIncidentDetail").customizeAst(ast -> {
             ast.addImport("java.util.List");

             ast.getClassByName("TrafficIncidentDetail").ifPresent(clazz -> {
                 clazz.getConstructors().get(0)
                     .setModifiers(Modifier.Keyword.PRIVATE)
                     .setJavadocComment("Set default constructor to private");

                 clazz.getMethodsByName("getTm").forEach(Node::remove);
                 clazz.addMethod("getId", Modifier.Keyword.PUBLIC)
                     .setType("String")
                     .setBody(StaticJavaParser.parseBlock("{ return this.tm.getId(); }"))
                     .setJavadocComment(new Javadoc(JavadocDescription.parseText("Get the id property: ID of the traffic model for this incident."))
                         .addBlockTag("return", "the id value."));

                clazz.addMethod("getPointsOfInterest", Modifier.Keyword.PUBLIC)
                    .setType("List<TrafficIncidentPointOfInterest>")
                    .setBody(StaticJavaParser.parseBlock("{ return this.tm.getPointsOfInterest(); }"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText("Get the pointsOfInterest property: A single traffic incident, or a cluster of traffic incidents."))
                        .addBlockTag("return", "the pointsOfInterest value."));
             });
         });
     }

     // Customizes the TrafficIncidentPointOfInterest class
     private void customizeTrafficIncidentPointOfInterest(PackageCustomization models) {
         models.getClass("TrafficIncidentPointOfInterest").customizeAst(ast ->
             ast.getClassByName("TrafficIncidentPointOfInterest").ifPresent(clazz ->
                 clazz.getConstructors().get(0)
                     .setModifiers(Modifier.Keyword.PRIVATE)
                     .setJavadocComment("Set default constructor to private")));
     }

     // Customizes the TrafficState class
     private void customizeTrafficState(PackageCustomization models) {
         models.getClass("TrafficState").customizeAst(ast -> ast.getClassByName("TrafficState").ifPresent(clazz ->
             clazz.getConstructors().get(0)
                 .setModifiers(Modifier.Keyword.PRIVATE)
                 .setJavadocComment("Set default constructor to private")));
     }

     // Customizes the TrafficIncidentViewport class
     private void customizeTrafficIncidentViewportResponse(PackageCustomization models) {
        models.getClass("TrafficIncidentViewportResponse").customizeAst(ast ->
            ast.getClassByName("TrafficIncidentViewportResponse").ifPresent(clazz ->
                clazz.getConstructors().get(0)
                    .setModifiers(Modifier.Keyword.PRIVATE)
                    .setJavadocComment("Set default constructor to private")));
     }
}
