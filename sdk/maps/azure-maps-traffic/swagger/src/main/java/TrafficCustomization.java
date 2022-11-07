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

        // customize TrafficFlowSegmentDataFlowSegmentData
        customizeTrafficFlowSegmentDataFlowSegmentData(models);

        // customize TrafficIncidentViewportViewpResp
        customizeTrafficIncidentViewportViewpResp(models);

        // customize Point
        customizePoint(models);

        // customize TrafficFlowSegmentData
        customizeTrafficFlowSegmentData(models);

        // customize TrafficFlowSegmentDataProperties
        customizeTrafficFlowSegmentDataProperties(models);

        // customize TrafficFlowSegmentDataPropertiesCoordinates
        customizeTrafficFlowSegmentDataPropertiesCoordinates(models);

        // customize TrafficIncidentDetail
        customizeTrafficIncidentDetail(models);

        // customize TrafficIncidentDetailTm
        customizeTrafficIncidentDetailTm(models);

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

    // Customizes the TrafficFlowSegmentDataFlowSegmentData class
    private void customizeTrafficFlowSegmentDataFlowSegmentData(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TrafficFlowSegmentDataFlowSegmentData");
        classCustomization.rename("TrafficFlowSegmentDataProperties");
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
     }

     // Customizes the TrafficFlowSegmentDataProperties class
     private void customizeTrafficFlowSegmentDataProperties(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TrafficFlowSegmentDataProperties");
        classCustomization.addConstructor(
            "private TrafficFlowSegmentDataProperties() {\n" +
            "}")
            .getJavadoc()
            .setDescription("Set default constructor to private");
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
     }

     // Customizes the TrafficIncidentDetailTm class
     private void customizeTrafficIncidentDetailTm(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TrafficIncidentDetailTm");
        classCustomization.addConstructor(
            "private TrafficIncidentDetailTm() {\n" +
            "}")
            .getJavadoc()
            .setDescription("Set default constructor to private");
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