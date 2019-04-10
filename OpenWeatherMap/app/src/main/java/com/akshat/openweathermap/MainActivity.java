package com.akshat.openweathermap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akshat.openweathermap.src.WeatherData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    TextView tvCity, tvTemp, tvWeather, tvCord;
    EditText etName;
    ImageView imgView;
    final int MY_PERMISSIONS_ACCESS_NETWORK_STATE = 1;
    final int MY_PERMISSIONS_INTERNET = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Not allowed to access network", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, MY_PERMISSIONS_ACCESS_NETWORK_STATE);
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Not allowed to access internet", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, MY_PERMISSIONS_INTERNET);
            return;
        } else
            Toast.makeText(this, "Allowed to use Internet", Toast.LENGTH_SHORT).show();
        findViews();
    }

    private void findViews() {
        tvCity = findViewById(R.id.tvCity);
        tvTemp = findViewById(R.id.tvTemp);
        tvWeather = findViewById(R.id.tvWeather);
        tvCord = findViewById(R.id.tvCord);
        etName = findViewById(R.id.etName);
        imgView = findViewById(R.id.img);
    }

    public void getDetails(View view) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        if (networkInfo != null && networkInfo.isConnected()) {
            MyNetworkTask myNetworkTask = new MyNetworkTask();
            myNetworkTask.execute(
                    etName.getText().toString()
            );
        } else
            Toast.makeText(this, "!!No Network Connection!!", Toast.LENGTH_LONG).show();
    }


    private class MyNetworkTask extends AsyncTask<String, Void, WeatherData> {

        @Override
        protected WeatherData doInBackground(String... strings) {
            HttpURLConnection connection = null;
            WeatherData weatherData = new WeatherData();
            try {
                URL url = new URL(
                        "http://api.openweathermap.org/data/2.5/weather?q="
                                + strings[0]
                                + "&appid=ef3f749618a659bdae0d87433a123037"
                );
                connection = (HttpURLConnection) url.openConnection();
                StringBuilder stringBuilder = new StringBuilder();
                InputStream is = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(is)
                );
                String line;
                while ((line = bufferedReader.readLine()) != null)
                    stringBuilder
                            .append(line)
                            .append("\r\n");
                String jsonString = stringBuilder.toString();
                JSONObject allData = new JSONObject(jsonString);
                JSONObject coordObj = allData.getJSONObject("coord");
                weatherData.setLat(
                        coordObj.getDouble("lat")
                );
                weatherData.setLng(
                        coordObj.getDouble("lon")
                );
                JSONObject sysObj = allData.getJSONObject("sys");
                String cityName =
                        allData.getString("name")
                                + "," +
                                sysObj.getString("country");
                weatherData.setCity(cityName);
                JSONObject mainObj = allData.getJSONObject("main");
                weatherData.setTemp_max(
                        mainObj.getDouble(
                                "temp_max"
                        )
                );
                weatherData.setTemp_min(
                        mainObj.getDouble(
                                "temp_min"
                        )
                );
                JSONArray weatherArr = allData.getJSONArray("weather");
                JSONObject weatherObj = weatherArr.getJSONObject(0);
                weatherData.setWeather(
                        weatherObj.getString(
                                "description"
                        )
                );
                String icon = weatherObj.getString("icon");
                Bitmap bmp = getImage(icon + ".png");
                weatherData.setImg(bmp);

            } catch (MalformedURLException e) {
                Toast.makeText(MainActivity.this, "MalformedURLException: LINE::129", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "IOException: LINE::132", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(MainActivity.this, "JSONException: LINE::135", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
            return weatherData;
        }

        private Bitmap getImage(String iconStr) {
            Bitmap img = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(
                        "http://api.openweathermap.org/img/w/"
                                + iconStr
                );
                connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                img = BitmapFactory.decodeStream(inputStream);
            } catch (MalformedURLException e) {
                Toast.makeText(MainActivity.this, "MalformedURLException: LINE::155", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "IOException: LINE::158", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
            return img;
        }

        @Override
        protected void onPostExecute(WeatherData weatherData) {
            super.onPostExecute(weatherData);
            tvCity.setText(
                    "City: "
                            + weatherData.getCity()
            );
            tvTemp.setText(
                    "Temp: "
                            + "Max "
                            + (weatherData.getTemp_max() - 273.15)
                            +"\t"
                            + "Min "
                            + (weatherData.getTemp_min() - 273.15)
            );
            tvWeather.setText(
                    "Weather: "
                            + weatherData.getWeather()
            );
            tvCord.setText(
                    "Coord: "
                            + "Lat "
                            + weatherData.getLat()
                            +"\t"
                            + "Lon"
                            + weatherData.getLng()
            );
            imgView.setImageBitmap(
                    weatherData.getImg()
            );
        }
    }
}
