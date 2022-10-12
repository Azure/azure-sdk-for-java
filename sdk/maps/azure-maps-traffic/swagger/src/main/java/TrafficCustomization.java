import java.util.Arrays;

import com.azure.autorest.customization.ClassCustomization;
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
    }

    // Customizes the TrafficFlowSegmentDataFlowSegmentData class
    private void customizeTrafficFlowSegmentDataFlowSegmentData(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TrafficFlowSegmentDataFlowSegmentData");
        classCustomization.rename("TrafficFlowSegmentDataProperties");
    }

    // Customizes the TrafficFlowSegmentDataFlowSegmentData class
    private void customizeTrafficIncidentViewportViewpResp(PackageCustomization models) {
        ClassCustomization classCustomization = models.getClass("TrafficIncidentViewportViewpResp");
        classCustomization.rename("TrafficIncidentViewportResponse");
    }
}