package com.azure.maps.service;

import com.azure.maps.service.models.IpAddressToLocationResult;
import com.azure.maps.service.models.ResponseFormat;
import com.fasterxml.jackson.core.JsonProcessingException;

public class GeolocationSample {
	public static void main(String[] args) throws JsonProcessingException{
		if (args.length != 1) {
			System.out.println("Usage GeolocationSample.java <ip>");
			return;
		}
		String ip = args[0];
		Geolocations geolocations = MapsCommon.createMapsClient().getGeolocations();
		getIPToLocationPreview(geolocations, ip);
	}

	public static void getIPToLocationPreview(Geolocations geolocations, String ip) throws JsonProcessingException {
		IpAddressToLocationResult result = geolocations.getIPToLocationPreview(ResponseFormat.JSON, ip);
		System.out.println("Got location by ip");
	    MapsCommon.print(result);
	}
}
