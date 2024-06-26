// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route.models;

import java.time.OffsetDateTime;
import java.util.List;

import com.azure.core.annotation.Fluent;

/**
 * Base Route Options
 *
 * @param <T> the type of the derived class.
 */
@Fluent
public abstract class BaseRouteOptions<T extends BaseRouteOptions<T>>  {
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

    /**
     * Creates a new instance of {@link BaseRouteOptions}.
     */
    public BaseRouteOptions() {
    }

    /**
     * Returns the constant speed fuel consumption.
     *
     * @return the constant speed fuel consumption.
     */
    public String getConstantSpeedConsumptionInLitersPerHundredKm() {
        return constantSpeedConsumptionInLitersPerHundredKm;
    }

    /**
     * Returns the departure time.
     *
     * @return the departure time.
     */
    public OffsetDateTime getDepartAt() {
        return this.departAt;
    }

    /**
     * Returns the vehicle axle weight.
     *
     * @return the vehicle axle weight.
     */
    public Integer getVehicleAxleWeight() {
        return this.vehicleAxleWeight;
    }

    /**
     * Returns the vehicle width in meters.
     *
     * @return the vehicle width.
     */
    public Double getVehicleWidth() {
        return this.vehicleWidth;
    }

    /**
     * Returns the vehicle height in meters.
     *
     * @return the vehicle height.
     */
    public Double getVehicleHeight() {
        return this.vehicleHeight;
    }

    /**
     * Returns the vehicle length in meters.
     *
     * @return the vehicle length.
     */
    public Double getVehicleLength() {
        return this.vehicleLength;
    }

    /**
     * Returns the vehicle max speed.
     *
     * @return the vehicle max speed.
     */
    public Integer getVehicleMaxSpeed() {
        return this.vehicleMaxSpeed;
    }

    /**
     * Returns the vehicle weight.
     *
     * @return the vehicle weight.
     */
    public Integer getVehicleWeight() {
        return this.vehicleWeight;
    }

    /**
     * Returns whether this is a commercial vehicle.
     *
     * @return whether this is a commercial vehicle.
     */
    public Boolean isCommercialVehicle() {
        return this.isCommercialVehicle;
    }

    /**
     * Returns the windingness.
     *
     * @return {@code WindingnessLevel}
     */
    public WindingnessLevel getWindingness() {
        return this.windingness;
    }

    /**
     * Return the incline level.
     *
     * @return {@code InclineLevel}
     */
    public InclineLevel getInclineLevel() {
        return this.inclineLevel;
    }

    /**
     * Returns the travel mode.
     *
     * @return {@code TravelMode}
     */
    public TravelMode getTravelMode() {
        return this.travelMode;
    }

    /**
     * Returns the list of route types to avoid.
     *
     * @return list of {@code RouteAvoidTypes}
     */
    public List<RouteAvoidType> getAvoidRouteTypes() {
        return this.avoid;
    }

    /**
     * Returns whether to use traffic data to calculate the route.
     *
     * @return whether to use traffic data.
     */
    public Boolean isGetUseTrafficData() {
        return this.useTrafficData;
    }

    /**
     * Returns the type of route.
     *
     * @return {@code RouteType}.
     */
    public RouteType getRouteType() {
        return this.routeType;
    }

    /**
     * Returns the vehicle load type.
     *
     * @return {@code VehicleLoadType}
     */
    public VehicleLoadType getVehicleLoadType() {
        return this.vehicleLoadType;
    }

    /**
     * Returns the vehicle engine type.
     *
     * @return {@code VehicleEngineType}
     */
    public VehicleEngineType getVehicleEngineType() {
        return this.vehicleEngineType;
    }

    /**
     * Returns the current fuel in liters.
     *
     * @return the current fuel in liters.
     */
    public Double getCurrentFuelInLiters() {
        return this.currentFuelInLiters;
    }

    /**
     * Returns the auxiliary power in liters per hour.
     *
     * @return the auxiliary power in liters per hour.
     */
    public Double getAuxiliaryPowerInLitersPerHour() {
        return this.auxiliaryPowerInLitersPerHour;
    }

    /**
     * Returns the fuel energy density.
     *
     * @return the fuel energy density.
     */
    public Double getFuelEnergyDensityInMegajoulesPerLiter() {
        return this.fuelEnergyDensityInMegajoulesPerLiter;
    }

    /**
     * Returns the acceleration efficiency.
     *
     * @return the acceleration efficiency.
     */
    public Double getAccelerationEfficiency() {
        return this.accelerationEfficiency;
    }

    /**
     * Returns the deceleration efficiency.
     *
     * @return the deceleration efficiency.
     */
    public Double getDecelerationEfficiency() {
        return this.decelerationEfficiency;
    }

    /**
     * Returns the uphill efficiency.
     *
     * @return the uphill efficiency.
     */
    public Double getUphillEfficiency() {
        return this.uphillEfficiency;
    }

    /**
     * Returns the downhill efficiency.
     *
     * @return the downhill efficiency.
     */
    public Double getDownhillEfficiency() {
        return this.downhillEfficiency;
    }

    /**
     * Returns the constant speed energy consumption.
     *
     * @return the constant speed energy consumption.
     */
    public String getConstantSpeedConsumptionInKwHPerHundredKm() {
        return this.constantSpeedConsumptionInKwHPerHundredKm;
    }

    /**
     * Returns the current charge.
     *
     * @return the current charge.
     */
    public Double getCurrentChargeInKwH() {
        return this.currentChargeInKwH;
    }

    /**
     * Returns the max charge.
     *
     * @return the max charge.
     */
    public Double getMaxChargeInKwH() {
        return this.maxChargeInKwH;
    }

    /**
     * Returns the auxiliary power.
     *
     * @return the auxiliary power.
     */
    public Double getAuxiliaryPowerInKw() {
        return this.auxiliaryPowerInKw;
    }

    /**
     * Sets the departure time.
     *
     * @param departAt the departure time.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setDepartAt(OffsetDateTime departAt) {
        this.departAt = departAt;
        return (T) this;
    }

    /**
     * Sets the vehicle axle weight.
     *
     * @param vehicleAxleWeight the vehicle axle weight.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setVehicleAxleWeight(Integer vehicleAxleWeight) {
        this.vehicleAxleWeight = vehicleAxleWeight;
        return (T) this;
    }

    /**
     * Sets the vehicle width.
     *
     * @param vehicleWidth the vehicle width.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setVehicleWidth(Double vehicleWidth) {
        this.vehicleWidth = vehicleWidth;
        return (T) this;
    }

    /**
     * Sets the vehicle height.
     *
     * @param vehicleHeight the vehicle height.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setVehicleHeight(Double vehicleHeight) {
        this.vehicleHeight = vehicleHeight;
        return (T) this;
    }

    /**
     * Sets the vehicle length.
     *
     * @param vehicleLength the vehicle length.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setVehicleLength(Double vehicleLength) {
        this.vehicleLength = vehicleLength;
        return (T) this;
    }

    /**
     * Sets the vehicle max speed.
     *
     * @param vehicleMaxSpeed the vehicle max speed.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setVehicleMaxSpeed(Integer vehicleMaxSpeed) {
        this.vehicleMaxSpeed = vehicleMaxSpeed;
        return (T) this;
    }

    /**
     * Sets the vehicle weight.
     *
     * @param vehicleWeight the vehicle weight.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setVehicleWeight(Integer vehicleWeight) {
        this.vehicleWeight = vehicleWeight;
        return (T) this;
    }

    /**
     * Sets whether this is a commercial vehicle.
     *
     * @param isCommercialVehicle whether this is a commercial vehicle.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setCommercialVehicle(Boolean isCommercialVehicle) {
        this.isCommercialVehicle = isCommercialVehicle;
        return (T) this;
    }

    /**
     * Sets the windingness.
     *
     * @param windingness {@code WindingnessLevel}
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setWindingness(WindingnessLevel windingness) {
        this.windingness = windingness;
        return (T) this;
    }

    /**
     * Sets the incline level.
     *
     * @param inclineLevel {@code InclineLevel}
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setInclineLevel(InclineLevel inclineLevel) {
        this.inclineLevel = inclineLevel;
        return (T) this;
    }

    /**
     * Sets the travel mode.
     *
     * @param travelMode {@code TravelMode}
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setTravelMode(TravelMode travelMode) {
        this.travelMode = travelMode;
        return (T) this;
    }

    /**
     * Sets the list of route types to avoid.
     *
     * @param avoid list of {@code RouteAvoidTypes}
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setAvoid(List<RouteAvoidType> avoid) {
        this.avoid = avoid;
        return (T) this;
    }

    /**
     * Sets whether to use traffic data to calculate the route.
     *
     * @param useTrafficData whether to use traffic data to calculate the route.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setUseTrafficData(Boolean useTrafficData) {
        this.useTrafficData = useTrafficData;
        return (T) this;
    }

    /**
     * Sets the type of route.
     *
     * @param routeType {@code RouteType}
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setRouteType(RouteType routeType) {
        this.routeType = routeType;
        return (T) this;
    }

    /**
     * Sets the vehicle load type.
     *
     * @param vehicleLoadType {@code VehicleLoadType}
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setVehicleLoadType(VehicleLoadType vehicleLoadType) {
        this.vehicleLoadType = vehicleLoadType;
        return (T) this;
    }

    /**
     * Sets the vehicle engine type.
     *
     * @param vehicleEngineType {@code VehicleEngineType}
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setVehicleEngineType(VehicleEngineType vehicleEngineType) {
        this.vehicleEngineType = vehicleEngineType;
        return (T) this;
    }

    /**
     * Sets the constant speed fuel consumption.
     *
     * @param constantSpeedConsumptionInLitersPerHundredKm the constant speed fuel consumption.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setConstantSpeedConsumptionInLitersPerHundredKm(String constantSpeedConsumptionInLitersPerHundredKm) {
        this.constantSpeedConsumptionInLitersPerHundredKm = constantSpeedConsumptionInLitersPerHundredKm;
        return (T) this;
    }

    /**
     * Sets the current fuel in liters.
     *
     * @param currentFuelInLiters the current fuel in liters.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setCurrentFuelInLiters(Double currentFuelInLiters) {
        this.currentFuelInLiters = currentFuelInLiters;
        return (T) this;
    }

    /**
     * Sets the auxiliary power in liters per hour.
     *
     * @param auxiliaryPowerInLitersPerHour the auxiliary power in liters per hour.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setAuxiliaryPowerInLitersPerHour(Double auxiliaryPowerInLitersPerHour) {
        this.auxiliaryPowerInLitersPerHour = auxiliaryPowerInLitersPerHour;
        return (T) this;
    }

    /**
     * Sets the fuel energy density.
     *
     * @param fuelEnergyDensityInMegajoulesPerLiter the fuel energy density.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setFuelEnergyDensityInMegajoulesPerLiter(Double fuelEnergyDensityInMegajoulesPerLiter) {
        this.fuelEnergyDensityInMegajoulesPerLiter = fuelEnergyDensityInMegajoulesPerLiter;
        return (T) this;
    }

    /**
     * Sets the acceleration efficiency.
     *
     * @param accelerationEfficiency the acceleration efficiency.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setAccelerationEfficiency(Double accelerationEfficiency) {
        this.accelerationEfficiency = accelerationEfficiency;
        return (T) this;
    }

    /**
     * Sets the deceleration efficiency.
     *
     * @param decelerationEfficiency the deceleration efficiency.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setDecelerationEfficiency(Double decelerationEfficiency) {
        this.decelerationEfficiency = decelerationEfficiency;
        return (T) this;
    }

    /**
     * Sets the uphill efficiency.
     *
     * @param uphillEfficiency the uphill efficiency.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setUphillEfficiency(Double uphillEfficiency) {
        this.uphillEfficiency = uphillEfficiency;
        return (T) this;
    }

    /**
     * Sets the downhill efficiency.
     *
     * @param downhillEfficiency the downhill efficiency.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setDownhillEfficiency(Double downhillEfficiency) {
        this.downhillEfficiency = downhillEfficiency;
        return (T) this;
    }

    /**
     * Sets the constant speed energy consumption.
     *
     * @param constantSpeedConsumptionInKwHPerHundredKm the constant speed energy consumption.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setConstantSpeedConsumptionInKwHPerHundredKm(String constantSpeedConsumptionInKwHPerHundredKm) {
        this.constantSpeedConsumptionInKwHPerHundredKm = constantSpeedConsumptionInKwHPerHundredKm;
        return (T) this;
    }

    /**
     * Sets the current charge.
     *
     * @param currentChargeInKwH the current charge.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setCurrentChargeInKwH(Double currentChargeInKwH) {
        this.currentChargeInKwH = currentChargeInKwH;
        return (T) this;
    }

    /**
     * Sets the max charge.
     *
     * @param maxChargeInKwH the max charge.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setMaxChargeInKwH(Double maxChargeInKwH) {
        this.maxChargeInKwH = maxChargeInKwH;
        return (T) this;
    }

    /**
     * Sets the auxiliary power.
     *
     * @param auxiliaryPowerInKw the auxiliary power.
     * @return a reference to this {@code BaseRouteOptions}
     */
    @SuppressWarnings("unchecked")
    public T setAuxiliaryPowerInKw(Double auxiliaryPowerInKw) {
        this.auxiliaryPowerInKw = auxiliaryPowerInKw;
        return (T) this;
    }
}
