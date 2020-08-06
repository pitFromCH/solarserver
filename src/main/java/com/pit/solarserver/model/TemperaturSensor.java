package com.pit.solarserver.model;


import com.pi4j.io.w1.W1Master;
import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.temperature.TemperatureScale;
import com.pit.solarserver.service.SolarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class TemperaturSensor {

    private static Logger logger = LoggerFactory.getLogger(TemperaturSensor.class);

    private W1Master w1Master = new W1Master();
    private HashMap<String, Double> sensorTemperaturList = new HashMap<String, Double>();

    public TemperaturSensor() {
        w1Master = new W1Master();
        logger.info(w1Master.toString());
    }

    public void readAllTemperaturSensor() {
        List<TemperatureSensor> deviceList = w1Master.getDevices(TemperatureSensor.class);

        for (int i =0; i<deviceList.size(); i++) {
            TemperatureSensor temperatureSensor = deviceList.get(i);
            String name = temperatureSensor.getName();
            Double temperatur = temperatureSensor.getTemperature(TemperatureScale.CELSIUS);
            sensorTemperaturList.put(name, temperatur);
            logger.info("Temperatur sensor " + name + " has temperatur " + temperatur);
        }
    }

    public int getSensorTemperatur(String sensorName) {
        int temperatur = 0;
        Double sensorTemperatur = sensorTemperaturList.get(sensorName);
        if (sensorTemperatur != null) {
            temperatur = sensorTemperatur.intValue();
        }
        return temperatur;
    }
}
