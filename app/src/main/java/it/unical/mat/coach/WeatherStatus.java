package it.unical.mat.coach;

public class WeatherStatus {

    private String main;
    private String description;
    private String temperature;
    private String icon;
    private String humidity;

    public WeatherStatus(){
        this.main = "";
        this.description = "";
        this.temperature = "";
        this.icon = "";
        this.humidity = "";
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }
}
