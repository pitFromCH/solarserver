package com.pit.solarserver.weatherdata;

import com.pit.solarserver.configuration.Constants;
import com.pit.solarserver.data.HourlyWeather;
import com.pit.solarserver.data.Weather;
import com.pit.solarserver.data.WeatherData;
import com.pit.solarserver.model.*;
import com.pit.solarserver.repository.SolarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy ', ' HH:mm");

    @Autowired
    private SolarRepository solarRepository;

    @Qualifier(Constants.TEMPERATUR_DATA_BEAN)
    @Autowired
    private TemperaturSensor temperaturSensor;

    @Qualifier(Constants.OPEN_WEATHER_DATA_BEAN)
    @Autowired
    private OpenWeatherData openWeatherData;

    @Qualifier(Constants.MESSURED_DATA_BEAN)
    @Autowired
    private CurrentMeasuredData currentMeasuredData;

    @Value("${solarserver.openweathermap.url}")
    private String url;

    @Value("${solarserver.switch.url}")
    public String switchURL;

    @Value("${solarserver.temperatursensor.roof}")
    private String roofTemperaturSensor;

    @Value("${solarserver.temperatursensor.boiler}")
    private String boilerTemperaturSensor;

    @Value("${solarserver.temperatursensor.solartube.hot}")
    private String solarTubeHotTemperaturSensor;

    @Value("${solarserver.temperatursensor.solartube.cold}")
    private String solarTubeColdTemperaturSensor;


    //<second> <minute> <hour> <day-of-month> <month> <day-of-week> <year> <command>
    //every hour
    //@Scheduled(cron = "0 0 * ? * *")
    //every minute
    @Scheduled(cron = "0 * * ? * *")
    public void collectWeatherData() throws URISyntaxException {
        //!!!Test
        //openWeatherData.updateWeatherDataModell();
        //calculateSolarModus();
        //!!!Test

        //get common data
        Date date = new Date(System.currentTimeMillis());
        Calendar calendar = GregorianCalendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        log.info("Collecting solar data, [date]="  + formatter.format(date) + ",  [hour]=" + hour);

        //read current temperature
        temperaturSensor.readAllTemperaturSensor();

        //read current weather data from open
        openWeatherData.updateWeatherDataModell();
        WeatherData weatherData = openWeatherData.getOpenWeatherData();

        if (weatherData != null) {
            //save current outdoor temp
            saveSolarValue(Constants.TYPE_OUTDOOR, (int) weatherData.getCurrent().getTemp(), date, hour);

            int rain = 0;
            if (weatherData.getHourly().get(0).getRain() != null) {
                rain = (int) (100 * weatherData.getHourly().get(0).getRain().get1h());
            }
            saveSolarValue(Constants.TYPE_RAIN, rain, date, hour);
        }

        int currentRoofTemperature =  temperaturSensor.getSensorTemperatur(roofTemperaturSensor);

        //Simation -> must be replaced
        int currentBoilerTemperature = readRandomTemperatur();
        int currentSolarTubeHotTemperature = readRandomTemperatur();
        int currentSolarTubeColdTemperature = readRandomTemperatur();

        currentMeasuredData.setCurrentBoilerTemperature(currentBoilerTemperature);
        currentMeasuredData.setCurrentRoofTemperature(currentRoofTemperature);
        currentMeasuredData.setCurrentSolarTubeColdTemperature(currentSolarTubeColdTemperature);
        currentMeasuredData.setCurrentSolarTubeHotTemperature(currentSolarTubeHotTemperature);

        saveSolarValue(Constants.TYPE_BOILER, currentBoilerTemperature, date, hour);
        saveSolarValue(Constants.TYPE_ROOF, currentRoofTemperature, date, hour);
        saveSolarValue(Constants.TYPE_SOLAR_TUBE_HOT, currentSolarTubeHotTemperature, date, hour);
        saveSolarValue(Constants.TYPE_SOLAR_TUBE_COLD, currentSolarTubeColdTemperature, date, hour);
    }


    private void saveSolarValue(int type,int value, Date date, int hour) {
        SolarData solarData = solarRepository.findSolarDataByDateAndAndType(date, type);
        if (solarData == null) {
            //create new row
            solarData = new SolarData();
            solarData.setDate(date);
            solarData.setType(type);
        }
        try {
            SolarData.class.getMethod("setT" + hour, int.class).invoke(solarData, value);
            solarRepository.saveAndFlush(solarData);
        } catch (NoSuchMethodException| InvocationTargetException| IllegalAccessException ex) {
            log.error(ex.getMessage());
        }
    }

    private int readRandomTemperatur() {
        //https://webtechie.be/post/2019-05-23-pi4j-adding-a-rest-interface-with-spring-boot/
        int min = 10;
        int max = 150;
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    //calculating boiler needs to heat up
    @Scheduled(cron = "0 15 20 ? * *")
    public void calculateSolarModus() {
        log.info("Calculating solar modus" );
        WeatherData weatherData = openWeatherData.getOpenWeatherData();
        ArrayList<HourlyWeather> hourlyWeatherlist = weatherData.getHourly();

        Calendar currentCalendar = GregorianCalendar.getInstance();
        Date currentDate = new Date(System.currentTimeMillis());
        currentCalendar.setTime(currentDate);
        currentCalendar.add(Calendar.DATE, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");


        int numberOfHourHeatingPercent = 0;
        int numberOfHourHeating = 0;
        int numberOfHourPossibleHeating = 0;

        int numberOfHourHeatingPercentNextDay = 0;
        int numberOfHourHeatingNextDay = 0;

        Calendar calendar = GregorianCalendar.getInstance();

        for (int i = 0; i<hourlyWeatherlist.size(); i++) {
            HourlyWeather hourlyWeather = hourlyWeatherlist.get(i);
            long dt = hourlyWeather.getDt();
            Date date = new Date(dt * 1000L);

            calendar.setTime(date);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);

            if ((hour >= Constants.START_SOLAR_SYSTEM_TIME) && (hour<= Constants.STOP_SOLAR_SYSTEM_TIME) ) {
                //time where the solar boiler could work
                numberOfHourPossibleHeating ++;
                ArrayList<Weather>  weatherList = hourlyWeather.getWeather();
                if (weatherList!=null && weatherList.size()==1 ) {
                    Weather weather = weatherList.get(0);
                    int weatherID = Integer.valueOf(weather.getId());
                    if (Constants.SOLAR_CLOUD_VALUES_LIST.contains(weatherID)) {
                        //check if forecast = next day
                        if (sdf.format(currentCalendar.getTime()).equals(sdf.format(calendar.getTime()))) {
                            int heatingPercentNexDay = 100 -  ((weatherID - 800) * 25) ;
                            numberOfHourHeatingPercentNextDay += heatingPercentNexDay;
                            numberOfHourHeatingNextDay ++;
                        }

                        int heatingPercent = 100 -  ((weatherID - 800) * 25) ;
                        numberOfHourHeatingPercent += heatingPercent;
                        numberOfHourHeating++;
                    }
                }
                log.info("Opendata forecast heating time  [date/time]="  + formatter.format(date));
            }

        }

        int numberOfHourEffectiveHeating = numberOfHourHeatingPercent / numberOfHourHeating;
        int numberOfHourEffectiveHeatingNextDay = numberOfHourHeatingPercentNextDay / numberOfHourHeatingNextDay;
        currentMeasuredData.setCurrent24hSunShining(numberOfHourEffectiveHeating);
        currentMeasuredData.setCurrent48hSunShining(numberOfHourEffectiveHeatingNextDay);

        int currentBoilerTemperature = currentMeasuredData.getCurrentBoilerTemperature();
        boolean switchElectronicBoilerOn = false;
        if (currentBoilerTemperature < 45 ) {
            if (numberOfHourEffectiveHeating < 5) {
                switchElectronicBoilerOn = true;
            }
        }

        if (currentBoilerTemperature < 40 ) {
            if (numberOfHourEffectiveHeatingNextDay < 4) {
                switchElectronicBoilerOn = true;
            }
        }

        if (switchElectronicBoilerOn) {
            currentMeasuredData.setSwitchOnBoilerElectronic(switchElectronicBoilerOn);
        }

        SolarSwitch solarSwitch = new SolarSwitch();
        solarSwitch.setSwitch(switchURL, switchElectronicBoilerOn);

    }

    //reset switch in the morning
    @Scheduled(cron = "0 15 06 ? * *")
    public void resetSolarModus() {
        currentMeasuredData.setSwitchOnBoilerElectronic(false);
        SolarSwitch solarSwitch = new SolarSwitch();
        solarSwitch.setSwitch(switchURL,false);
    }

}
