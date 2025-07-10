package com.atakmap.android.plugintemplate.araaftorPlugin.api;

import android.os.AsyncTask;

import com.atakmap.android.plugintemplate.araaftorPlugin.interfaces.WeatherDataListener;

public class GetWeatherDataTask extends AsyncTask<Double, Void, double[]> {
    private WeatherDataListener listener;
    public GetWeatherDataTask(WeatherDataListener listener) {
        this.listener = listener;
    }

    @Override
    protected double[] doInBackground(Double... params) {
        WeatherApiClient weatherApiClient = new WeatherApiClient();
        return weatherApiClient.getWeatherData(params[0], params[1]);
    }

    @Override
    protected void onPostExecute(double[] result) {
        if (result != null) {
            listener.onWeatherDataReceived(result);
        }
    }
}
