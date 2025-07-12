package com.atakmap.android.plugintemplate.araaftorPlugin.api;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherApiClient {

    private static final String API_KEY = "Your_API_key";
    private static final String API_URL = "https://api.weatherapi.com/v1/current.json";


    public double[] getWeatherData(double latitude, double longitude) {
        try {
            String urlString = API_URL + "?key=" + API_KEY + "&q=" + latitude + "," + longitude + "&aqi=no";
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject myResponse = new JSONObject(response.toString());
            JSONObject currentWeather = myResponse.getJSONObject("current");
            double tempC = currentWeather.getDouble("temp_c");
            double windKph = currentWeather.getDouble("wind_kph");
            int day = currentWeather.getInt("is_day");

            connection.disconnect();

            return new double[]{tempC, windKph, day};

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
