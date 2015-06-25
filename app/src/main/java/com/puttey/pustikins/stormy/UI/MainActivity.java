package com.puttey.pustikins.stormy.UI;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.puttey.pustikins.stormy.Location.AddressLocation;
import com.puttey.pustikins.stormy.Location.GPSTracker;
import com.puttey.pustikins.stormy.R;
import com.puttey.pustikins.stormy.Weather.Current;
import com.puttey.pustikins.stormy.Weather.Day;
import com.puttey.pustikins.stormy.Weather.Forecast;
import com.puttey.pustikins.stormy.Weather.Hour;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends ActionBarActivity{

    public static final String TAG = "DEBUG";

    private Forecast mForecast;
    private double latitude;
    private double longitude;
    private String locationAddress;
    private GPSTracker gps;
    private AddressLocation mAddressLocation;
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";


    @InjectView(R.id.timeLabel) TextView mTimeLabel;
    @InjectView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @InjectView(R.id.humidityValue) TextView mHumidityValue;
    @InjectView(R.id.precipValue) TextView mPrecipValue;
    @InjectView(R.id.summaryLabel) TextView mSummaryLabel;
    @InjectView(R.id.imageIcon)ImageView mIconImageView;
    @InjectView(R.id.refreshImageView) ImageView mRefreshImageView;
    @InjectView(R.id.locationLabel) TextView mLocationLabel;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        mProgressBar.setVisibility(View.INVISIBLE);
        mAddressLocation = new AddressLocation();
        gps = new GPSTracker(this);

        getForecast();

        mRefreshImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                getForecast();
            }
        });
    }

    /**
     * Update latitude and longitude values from our GPSTracker object
     */
    private void getLocation(){
        latitude = gps.getLatitude();
        longitude = gps.getLongitude();
        mAddressLocation.getAddressFromLocation(latitude, longitude, getApplicationContext(), new GeocoderHandler());
    }

    /**
     * Request forecast data from forecast.io
     *
     */
    private void getForecast(){
        getLocation();
        String apiKey = "472001a2959a838aef8d58aae84c18cd";
        String forecastUrl = "https://api.forecast.io/forecast/" + apiKey +
                "/" + latitude + "," + longitude;

        if(isNetworkAvailable()){
            toggleRefresh();    //display progress bar
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback(){
                @Override
                public void onFailure(Request request, IOException e){
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException{
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            toggleRefresh();
                        }
                    });
                    try{
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()){
                            mForecast = parseForecastDetails(jsonData);
                            runOnUiThread(new Runnable(){
                                @Override
                                public void run(){
                                    updateDisplay();
                                }
                            });
                        } else{
                            alertUserAboutError();
                        }
                    } catch (IOException e){
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e){
                        Log.v(TAG, "Exception caught: ", e);
                    }
                }
            });
        }
        else{
            Toast.makeText(this, "Network is unavailable", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Toggle visibility of refresh button and progressbar
     */
    private void toggleRefresh(){
        if(mProgressBar.getVisibility() == View.INVISIBLE){
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        }
        else{
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Update UI with current weather data
     */
    private void updateDisplay(){
        mTemperatureLabel.setText(mForecast.getCurrent().getTemperature() + "");
        mTimeLabel.setText("At " + mForecast.getCurrent().getFormattedTime() + " it will be");
        mHumidityValue.setText(mForecast.getCurrent().getHumidity() + "");
        mPrecipValue.setText(mForecast.getCurrent().getPrecipChance() + "%");
        mSummaryLabel.setText(mForecast.getCurrent().getSummary());
        mLocationLabel.setText(locationAddress);
        Drawable drawable = getResources().getDrawable(mForecast.getCurrent().getIconId(), null);
        mIconImageView.setImageDrawable(drawable);
    }


    private Forecast parseForecastDetails(String jsonData)throws JSONException{
        Forecast forecast = new Forecast();
        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));

        return forecast;
    }

    private Day[] getDailyForecast(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");
        Day[] days = new Day[data.length()];
        for(int i = 0; i < data.length(); i++){
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();
            day.setSummary(jsonDay.getString("summary"));
            day.setIcon(jsonDay.getString("icon"));
            day.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            day.setTime(jsonDay.getLong("time"));
            day.setTimezone(timezone);
        }

        return new Day[0];

    }

    /**
     * Parses JSONObject and returns array of Hourly forecast
     * @param jsonData
     * @return
     * @throws JSONException
     */
    private Hour[] getHourlyForecast(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");
        Hour[] hours = new Hour[data.length()];
        for(int i = 0; i < data.length(); i ++){
            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();
            hour.setSummary(jsonHour.getString("summary"));
            hour.setTemperature(jsonHour.getDouble("temperature"));
            hour.setIcon(jsonHour.getString("icon"));
            hour.setTime(jsonHour.getLong("time"));
            hour.setTimezone(timezone);
            hours[i] = hour;
        }
        return hours;
    }

    /**
     * Obtain current weather info from JSON data request and assign to Current object
     * @param jsonData JSON data received from forecast.io request
     * @return Current object
     * @throws JSONException
     */
    private Current getCurrentDetails(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);
        JSONObject currently = forecast.getJSONObject("currently");
        Current current = new Current();
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTimeZone(timezone);
        Log.d(TAG, current.getFormattedTime());
        return current;
    }

    /**
     * Test network connection
     * @return true if connected to network and network is not null
     */
    private boolean isNetworkAvailable(){
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    /**
     * Display Alert Dialog with Error Message
     */
    private void alertUserAboutError(){
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    @OnClick(R.id.dailyButton)
    public void startDailyActivity(View view){
        Intent intent = new Intent(this, DailyForecastActivity.class);
        intent.putExtra(DAILY_FORECAST, mForecast.getDailyForecast());
        startActivity(intent);
    }

    @OnClick(R.id.hourlyButton)
    public void startHoulyActivity(View view){
        Intent intent = new Intent(this, HourlyForecastActivity.class);
        intent.putExtra(HOURLY_FORECAST, mForecast.getHourlyForecast());
        startActivity(intent);
    }

    /**
     *Geocode Handler object to append address to mLocationLabel
     */
    private class GeocoderHandler extends Handler{
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = "";
            }
        }
    }

}
