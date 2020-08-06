package com.pit.solarserver.model;

public class CurrentMeasuredData {

    private int currentRoofTemperature = 0;
    private int currentBoilerTemperature = 0;
    private int currentSolarTubeHotTemperature = 0;
    private int currentSolarTubeColdTemperature = 0;
    private boolean switchOnBoilerElectronic = false;
    private int current24hSunShining = 0;
    private int current48hSunShining = 0;

    public int getCurrentRoofTemperature() {
        return currentRoofTemperature;
    }

    public void setCurrentRoofTemperature(int currentRoofTemperature) {
        this.currentRoofTemperature = currentRoofTemperature;
    }

    public int getCurrentBoilerTemperature() {
        return currentBoilerTemperature;
    }

    public void setCurrentBoilerTemperature(int currentBoilerTemperature) {
        this.currentBoilerTemperature = currentBoilerTemperature;
    }

    public int getCurrentSolarTubeHotTemperature() {
        return currentSolarTubeHotTemperature;
    }

    public void setCurrentSolarTubeHotTemperature(int currentSolarTubeHotTemperature) {
        this.currentSolarTubeHotTemperature = currentSolarTubeHotTemperature;
    }

    public int getCurrentSolarTubeColdTemperature() {
        return currentSolarTubeColdTemperature;
    }

    public void setCurrentSolarTubeColdTemperature(int currentSolarTubeColdTemperature) {
        this.currentSolarTubeColdTemperature = currentSolarTubeColdTemperature;
    }

    public boolean isSwitchOnBoilerElectronic() {
        return switchOnBoilerElectronic;
    }

    public void setSwitchOnBoilerElectronic(boolean switchOnBoilerElectronic) {
        this.switchOnBoilerElectronic = switchOnBoilerElectronic;
    }

    public int getCurrent24hSunShining() {
        return current24hSunShining;
    }

    public void setCurrent24hSunShining(int current24hSunShining) {
        this.current24hSunShining = current24hSunShining;
    }

    public int getCurrent48hSunShining() {
        return current48hSunShining;
    }

    public void setCurrent48hSunShining(int current48hSunShining) {
        this.current48hSunShining = current48hSunShining;
    }
}
