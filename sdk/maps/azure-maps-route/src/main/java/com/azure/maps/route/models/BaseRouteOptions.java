package com.azure.maps.route.models;

import java.time.OffsetDateTime;
import java.util.List;

public class BaseRouteOptions<T extends BaseRouteOptions<T>>  {
    private OffsetDateTime departAt;
    private Integer vehicleAxleWeight;
    private Double vehicleWidth;
    private Double vehicleHeight;
    private Double vehicleLength;
    private Integer vehicleMaxSpeed;
    private Integer vehicleWeight;
    private Boolean isCommercialVehicle;
    private WindingnessLevel windingness;
    private InclineLevel inclineLevel;
    private TravelMode travelMode;
    private List<RouteAvoidType> avoid;
    private Boolean useTrafficData;
    private RouteType routeType;
    private VehicleLoadType vehicleLoadType;
    private VehicleEngineType vehicleEngineType;
    private String constantSpeedConsumptionInLitersPerHundredKm;
    private Double currentFuelInLiters;
    private Double auxiliaryPowerInLitersPerHour;
    private Double fuelEnergyDensityInMegajoulesPerLiter;
    private Double accelerationEfficiency;
    private Double decelerationEfficiency;
    private Double uphillEfficiency;
    private Double downhillEfficiency;
    private String constantSpeedConsumptionInKwHPerHundredKm;
    private Double currentChargeInKwH;
    private Double maxChargeInKwH;
    private Double auxiliaryPowerInKw;

    public String getConstantSpeedConsumptionInLitersPerHundredKm() {
        return constantSpeedConsumptionInLitersPerHundredKm;
    }

    public OffsetDateTime getDepartAt() {
        return this.departAt;
    }

    public Integer getVehicleAxleWeight() {
        return this.vehicleAxleWeight;
    }

    public Double getVehicleWidth() {
        return this.vehicleWidth;
    }

    public Double getVehicleHeight() {
        return this.vehicleHeight;
    }

    public Double getVehicleLength() {
        return this.vehicleLength;
    }

    public Integer getVehicleMaxSpeed() {
        return this.vehicleMaxSpeed;
    }

    public Integer getVehicleWeight() {
        return this.vehicleWeight;
    }

    public Boolean isCommercialVehicle() {
        return this.isCommercialVehicle;
    }

    public WindingnessLevel getWindingness() {
        return this.windingness;
    }

    public InclineLevel getInclineLevel() {
        return this.inclineLevel;
    }

    public TravelMode getTravelMode() {
        return this.travelMode;
    }

    public List<RouteAvoidType> getAvoid() {
        return this.avoid;
    }

    public Boolean getUseTrafficData() {
        return this.useTrafficData;
    }

    public RouteType getRouteType() {
        return this.routeType;
    }

    public VehicleLoadType getVehicleLoadType() {
        return this.vehicleLoadType;
    }

    public VehicleEngineType getVehicleEngineType() {
        return this.vehicleEngineType;
    }

    public Double getCurrentFuelInLiters() {
        return this.currentFuelInLiters;
    }

    public Double getAuxiliaryPowerInLitersPerHour() {
        return this.auxiliaryPowerInLitersPerHour;
    }

    public Double getFuelEnergyDensityInMegajoulesPerLiter() {
        return this.fuelEnergyDensityInMegajoulesPerLiter;
    }

    public Double getAccelerationEfficiency() {
        return this.accelerationEfficiency;
    }

    public Double getDecelerationEfficiency() {
        return this.decelerationEfficiency;
    }

    public Double getUphillEfficiency() {
        return this.uphillEfficiency;
    }

    public Double getDownhillEfficiency() {
        return this.downhillEfficiency;
    }

    public String getConstantSpeedConsumptionInKwHPerHundredKm() {
        return this.constantSpeedConsumptionInKwHPerHundredKm;
    }

    public Double getCurrentChargeInKwH() {
        return this.currentChargeInKwH;
    }

    public Double getMaxChargeInKwH() {
        return this.maxChargeInKwH;
    }

    public Double getAuxiliaryPowerInKw() {
        return this.auxiliaryPowerInKw;
    }

    @SuppressWarnings("unchecked")
    public T setDepartAt(OffsetDateTime departAt) {
        this.departAt = departAt;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setVehicleAxleWeight(Integer vehicleAxleWeight) {
        this.vehicleAxleWeight = vehicleAxleWeight;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setVehicleWidth(Double vehicleWidth) {
        this.vehicleWidth = vehicleWidth;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setVehicleHeight(Double vehicleHeight) {
        this.vehicleHeight = vehicleHeight;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setVehicleLength(Double vehicleLength) {
        this.vehicleLength = vehicleLength;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setVehicleMaxSpeed(Integer vehicleMaxSpeed) {
        this.vehicleMaxSpeed = vehicleMaxSpeed;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setVehicleWeight(Integer vehicleWeight) {
        this.vehicleWeight = vehicleWeight;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setIsCommercialVehicle(Boolean isCommercialVehicle) {
        this.isCommercialVehicle = isCommercialVehicle;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setWindingness(WindingnessLevel windingness) {
        this.windingness = windingness;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setInclineLevel(InclineLevel inclineLevel) {
        this.inclineLevel = inclineLevel;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setTravelMode(TravelMode travelMode) {
        this.travelMode = travelMode;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setAvoid(List<RouteAvoidType> avoid) {
        this.avoid = avoid;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setUseTrafficData(Boolean useTrafficData) {
        this.useTrafficData = useTrafficData;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setRouteType(RouteType routeType) {
        this.routeType = routeType;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setVehicleLoadType(VehicleLoadType vehicleLoadType) {
        this.vehicleLoadType = vehicleLoadType;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setVehicleEngineType(VehicleEngineType vehicleEngineType) {
        this.vehicleEngineType = vehicleEngineType;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setConstantSpeedConsumptionInLitersPerHundredKm(String constantSpeedConsumptionInLitersPerHundredKm) {
        this.constantSpeedConsumptionInLitersPerHundredKm = constantSpeedConsumptionInLitersPerHundredKm;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setCurrentFuelInLiters(Double currentFuelInLiters) {
        this.currentFuelInLiters = currentFuelInLiters;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setAuxiliaryPowerInLitersPerHour(Double auxiliaryPowerInLitersPerHour) {
        this.auxiliaryPowerInLitersPerHour = auxiliaryPowerInLitersPerHour;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setFuelEnergyDensityInMegajoulesPerLiter(Double fuelEnergyDensityInMegajoulesPerLiter) {
        this.fuelEnergyDensityInMegajoulesPerLiter = fuelEnergyDensityInMegajoulesPerLiter;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setAccelerationEfficiency(Double accelerationEfficiency) {
        this.accelerationEfficiency = accelerationEfficiency;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setDecelerationEfficiency(Double decelerationEfficiency) {
        this.decelerationEfficiency = decelerationEfficiency;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setUphillEfficiency(Double uphillEfficiency) {
        this.uphillEfficiency = uphillEfficiency;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setDownhillEfficiency(Double downhillEfficiency) {
        this.downhillEfficiency = downhillEfficiency;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setConstantSpeedConsumptionInKwHPerHundredKm(String constantSpeedConsumptionInKwHPerHundredKm) {
        this.constantSpeedConsumptionInLitersPerHundredKm = constantSpeedConsumptionInKwHPerHundredKm;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setCurrentChargeInKwH(Double currentChargeInKwH) {
        this.currentChargeInKwH = currentChargeInKwH;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setMaxChargeInKwH(Double maxChargeInKwH) {
        this.maxChargeInKwH = maxChargeInKwH;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setAuxiliaryPowerInKw(Double auxiliaryPowerInKw) {
        this.auxiliaryPowerInKw = auxiliaryPowerInKw;
        return (T) this;
    }
}
