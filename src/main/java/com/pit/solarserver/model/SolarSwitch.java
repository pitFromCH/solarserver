package com.pit.solarserver.model;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

public class SolarSwitch {

    private static Logger logger = LoggerFactory.getLogger(SolarSwitch.class);
    private static final String switchOnString = "on";
    private static final String switchOffString = "off";


    public void setSwitch(String url, boolean switchOn) {
        /** create gpio controller */
        final GpioController gpio = GpioFactory.getInstance();
        //https://pi4j.com/1.2/pins/model-3b-rev1.html
        final GpioPinDigitalOutput ledPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00);
        RestTemplate restTemplate = new RestTemplate();
        if (switchOn) {
            //set led
            ledPin.high();
            //write switch on to url
            String html = restTemplate.getForObject(url, String.class, switchOnString);
        } else {
            //set led
            ledPin.low();
            //write switch off to url
            String html = restTemplate.getForObject(url, String.class, switchOffString);
        }
    }

}
