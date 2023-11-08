import java.util.Arrays;

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.ConstructorCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.MethodCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.implementation.ls.EclipseLanguageClient;

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

        // customize TrafficFlowSegmentDataPropertiesCoordinates
        customizeTrafficFlowSegmentDataPropertiesCoordinates(models);

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
        ClassCustomization classCustomization = models.getClass("TrafficFlowSegmentDataFlowSegmentDataCoordinates");
        MethodCustomization mc = classCustomization.getMethod("getCoordinates");
        mc.rename("toDelete");
        classCustomization.addMethod(
            "public List<GeoPosition> getCoordinates() {\n" +
            "   return this.coordinates.stream()\n" +
            "       .map(item -> new GeoPosition(item.getLongitude(), item.getLatitude()))\n" +
            "       .collect(Collectors.toList());\n" +
            "}")
            .getJavadoc()
            .setDescription("Return the coordinates")
            .setReturn("Returns a list of {@link GeoPosition} coordinates.");
        classCustomization.addImports("java.util.List");
        classCustomization.addImports("java.util.stream.Collectors");
        classCustomization.addImports("java.util.Arrays");
        classCustomization.addImports("com.azure.core.models.GeoPosition");
        // Without the renaming / deleting pair, an exception was being thrown by the ClassCustomization
        // class, which seemed to get lost in the class file.
        classCustomization.removeMethod("toDelete");
        // Rename the class
        ClassCustomization nameCustomization = models.getClass("TrafficFlowSegmentDataFlowSegmentDataCoordinates");
        nameCustomization.rename("TrafficFlowSegmentDataPropertiesCoordinates"); 
    }

    // Customizes the TrafficIncidentViewport class
    private void customizeTrafficIncidentViewport(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TrafficIncidentViewport");
        MethodCustomization mc = classCustomization.getMethod("getViewpResp");
        mc.rename("getViewportResponse");
        classCustomization.addConstructor(
            "private TrafficIncidentViewport() {\n" +
            "}")
            .getJavadoc()
            .setDescription("Set default constructor to private");
    }

    // Customizes the TrafficIncidentViewportResponse class
    private void customizeTrafficIncidentViewportViewpResp(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TrafficIncidentViewportViewpResp");
        classCustomization.rename("TrafficIncidentViewportResponse");
    }

     // Customizes the Point class
     private void customizePoint(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("MapsPoint");
        classCustomization.addConstructor(
            "private MapsPoint() {\n" +
            "}")
            .getJavadoc()
            .setDescription("Set default constructor to private");
     }

     // Customizes the TrafficFlowSegmentData class
     private void customizeTrafficFlowSegmentData(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TrafficFlowSegmentData");
        classCustomization.addConstructor(
            "private TrafficFlowSegmentData() {\n" +
            "}")
            .getJavadoc()
            .setDescription("Set default constructor to private");
        classCustomization.removeMethod("getFlowSegmentData");
        classCustomization.addMethod(
            "public String getFunctionalRoadClass() {\n" +
            "   return this.flowSegmentData.getFunctionalRoadClass();\n" +
            "}")
            .getJavadoc()
            .setDescription("Get the functionalRoadClass property: Functional Road Class. This indicates the road type: 0: Motorway, freeway or other major road. 1: Major road, less important than a motorway. 2: Other major road. 3: Secondary road. 4: Local connecting road. 5: Local road of high importance. 6: Local road.")
            .setReturn("the functionalRoadClass value.");
        classCustomization.addMethod(
            "public Integer getCurrentSpeed() {\n" +
            "   return this.flowSegmentData.getCurrentSpeed();\n" +
            "}")
            .getJavadoc()
            .setDescription("Get the currentSpeed property: The current average speed at the selected point, in the units requested. This is calculated from the currentTravelTime and the length of the selected segment.")
            .setReturn("the currentSpeed value.");
        classCustomization.addMethod(
            "public Integer getFreeFlowSpeed() {\n" +
            "    return this.flowSegmentData.getFreeFlowSpeed();\n" +
            "}")
            .getJavadoc()
            .setDescription("Get the freeFlowSpeed property: The free flow speed expected under ideal conditions, expressed in the units requested. This is related to the freeFlowTravelTime.")
            .setReturn("the freeFlowSpeed value.");
        classCustomization.addMethod(
            "public Integer getCurrentTravelTime() {\n" +
            "   return this.flowSegmentData.getCurrentTravelTime();\n" +
            "}")
            .getJavadoc()
            .setDescription("Get the currentTravelTime property: Current travel time in seconds, across this traffic segment, based on fused real-time measurements between the defined locations in the specified direction.")
            .setReturn("the currentTravelTime value.");
        classCustomization.addMethod(
            "public Integer getFreeFlowTravelTime() {\n" +
            "   return this.flowSegmentData.getFreeFlowTravelTime();\n" +
            "}")
            .getJavadoc()
            .setDescription("Get the freeFlowTravelTime property: The travel time in seconds, across this traffic segment, which would be expected under ideal free flow conditions.")
            .setReturn("the freeFlowTravelTime value.");
        classCustomization.addMethod(
            "public Float getConfidence() {\n" +
            "   return this.flowSegmentData.getConfidence();\n" +
            "}")
            .getJavadoc()
            .setDescription("Get the confidence property: The confidence is a measure of the quality of the provided travel time and speed. A value of 1 means full confidence, that the response contains the highest quality data. Lower values indicate the degree that the response may vary from the actual conditions on the road. Any value greater than 0.6 means the information was based on real-time probe input. A value of 0.5 means the reported speed is based on historical info. A value between 0.5 and 0.6 has a calculated weighted average between historical and live speeds.")
            .setReturn("the confidence value.");
        classCustomization.addMethod(
            "public TrafficFlowSegmentDataPropertiesCoordinates getCoordinates() {\n" +
            "   return this.flowSegmentData.getCoordinates();\n" +
            "}")
            .getJavadoc()
            .setDescription("Get the coordinates property: Includes the coordinates describing the shape of the segment. Coordinates are shifted from the road depending on the zoom level to support high quality visualization in every scale.")
            .setReturn("the coordinates value.");
        classCustomization.addMethod(
            "public String getVersion() {\n" +
            "   return this.flowSegmentData.getVersion();\n" +
            "}")
            .getJavadoc()
            .setDescription("Get the version property: This indicates the software version that generated the response.")
            .setReturn("the version value.");
        classCustomization.addMethod(
            "public String getOpenLrCode() {\n" +
            "   return this.flowSegmentData.getOpenLrCode();\n" +
            "}")
            .getJavadoc()
            .setDescription("Get the openLrCode property: OpenLR code for segment. See [OpenLR](https://en.wikipedia.org/wiki/OpenLR) for more information on the use of Open LR codes.")
            .setReturn("the openLrCode value.");
     }

     // Customizes the TrafficFlowSegmentDataPropertiesCoordinates class
     private void customizeTrafficFlowSegmentDataPropertiesCoordinates(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TrafficFlowSegmentDataPropertiesCoordinates");
        classCustomization.addConstructor(
            "private TrafficFlowSegmentDataPropertiesCoordinates() {\n" +
            "}")
            .getJavadoc()
            .setDescription("Set default constructor to private");
     }

     // Customizes the TrafficIncidentDetail class
     private void customizeTrafficIncidentDetail(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TrafficIncidentDetail");
        classCustomization.addConstructor(
            "private TrafficIncidentDetail() {\n" +
            "}")
            .getJavadoc()
            .setDescription("Set default constructor to private");
        classCustomization.removeMethod("getTm");
        classCustomization.addMethod(
            "public String getId() {\n" +
            "    return this.tm.getId();\n" +
            "}")
            .getJavadoc()
            .setDescription("Get the id property: ID of the traffic model for this incident.")
            .setReturn("the id value.");
        classCustomization.addMethod(
            "public List<TrafficIncidentPointOfInterest> getPointsOfInterest() {\n" +
            "    return this.tm.getPointsOfInterest();\n" +
            "}")
            .getJavadoc()
            .setDescription("Get the pointsOfInterest property: A single traffic incident, or a cluster of traffic incidents.")
            .setReturn("the pointsOfInterest value.");
        classCustomization.addImports("java.util.List");
     }

     // Customizes the TrafficIncidentPointOfInterest class
     private void customizeTrafficIncidentPointOfInterest(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TrafficIncidentPointOfInterest");
        classCustomization.addConstructor(
            "private TrafficIncidentPointOfInterest() {\n" +
            "}")
            .getJavadoc()
            .setDescription("Set default constructor to private");
     }

     // Customizes the TrafficState class
     private void customizeTrafficState(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TrafficState");
        classCustomization.addConstructor(
            "private TrafficState() {\n" +
            "}")
            .getJavadoc()
            .setDescription("Set default constructor to private");
     }

     // Customizes the TrafficIncidentViewport class
     private void customizeTrafficIncidentViewportResponse(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TrafficIncidentViewportResponse");
        classCustomization.addConstructor(
            "private TrafficIncidentViewportResponse() {\n" +
            "}")
            .getJavadoc()
            .setDescription("Set default constructor to private");
     }
}