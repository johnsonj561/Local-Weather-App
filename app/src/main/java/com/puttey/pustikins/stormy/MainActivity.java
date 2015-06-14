package com.puttey.pustikins.stormy;

import android.content.Context;
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

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends ActionBarActivity{

    public static final String TAG = "DEBUG";

    private CurrentWeather mCurrentWeather;
    private double latitude;
    private double longitude;
    private String locationAddress;
    private GPSTracker gps;
    private AddressLocation mAddressLocation;

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
        mAddressLocation.getAddressFromLocation(latitude,longitude,getApplicationContext(), new GeocoderHandler());
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
                            mCurrentWeather = getCurrentDetails(jsonData);
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
        mTemperatureLabel.setText(mCurrentWeather.getTemperature() + "");
        mTimeLabel.setText("At " + mCurrentWeather.getFormattedTime() + " it will be");
        mHumidityValue.setText(mCurrentWeather.getHumidity() + "");
        mPrecipValue.setText(mCurrentWeather.getPrecipChance() + "%");
        mSummaryLabel.setText(mCurrentWeather.getSummary());
        mLocationLabel.setText(locationAddress);
        Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId(), null);
        mIconImageView.setImageDrawable(drawable);
    }

    /**
     * Obtain current weather info from JSON data request and assign to CurrentWeather object
     * @param jsonData JSON data received from forecast.io request
     * @return CurrentWeather object
     * @throws JSONException
     */
    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);
        JSONObject currently = forecast.getJSONObject("currently");
        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimeZone(timezone);
        Log.d(TAG, currentWeather.getFormattedTime());
        return currentWeather;
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
