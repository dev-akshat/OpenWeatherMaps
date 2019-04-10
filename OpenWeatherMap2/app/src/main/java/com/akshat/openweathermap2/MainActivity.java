package com.akshat.openweathermap2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.akshat.openweathermap2.src.WeatherData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    ArrayAdapter adapter, adpt;
    ArrayList<String> arrayList;
    Spinner spinner;
    TextView tvCity, tvTemp, tvWeather, tvCord;
    ImageView imgView;
    String cityName = "Bhopal";
    final int MY_PERMISSIONS_ACCESS_NETWORK_STATE = 1;
    final int MY_PERMISSIONS_INTERNET = 1;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private final String MY_FILE = "cities.json";
    SortedMap<String,Long> city_map ;
    long id;
    boolean first_time=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        findViews();
        getJSONData();
        initSpinner();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name=arrayList.get(position);
                long city_id=city_map.get(name);
                if(!first_time)
                    showWeather(city_id);
                else
                    first_time=false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private Map<String,Long> getJSONData() {
        //RECEIVING JSON STRING
        String jsonStr = readMyFile();
        city_map= new TreeMap<String,Long>();
        try {
            //CREATE JSON ARRAY FROM STRING
            JSONArray allJSONArray = new JSONArray(jsonStr);
            //EXTRACTING FIRST JSON OBJECT FROM ARRAY
            for (int i = 0; i < allJSONArray.length(); i++) {
                JSONObject jsonObj = allJSONArray.getJSONObject(i);
                long id = jsonObj.getLong("id");
                String city_name =
                        jsonObj.getString("name")
                        + ","
                        + jsonObj.getString("country");
                city_map.put(city_name,id);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return city_map;
    }

    private String readMyFile() {
        StringBuffer stringBuffer = new StringBuffer();
        if (isExternalStorageAvailable()) {
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/my_owm_files");
            File myFile = new File(directory.getAbsolutePath() + "/" + MY_FILE);
            try {
                FileInputStream inputStream = new FileInputStream(myFile);
                int ch;
                while ((ch = inputStream.read()) != -1)
                    stringBuffer.append((char) ch);
                Toast.makeText(this, "Read Op Successful", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            Toast.makeText(this, "External Storage UnAvailable", Toast.LENGTH_LONG).show();
        return stringBuffer.toString();
    }

    private boolean isExternalStorageAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private boolean isExternalStorageReadOnly() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    private void checkPermissions() {
        //NETWORK ACCESS PERMISSION
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE
        ) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                    this,
                    "Not allowed to access network",
                    Toast.LENGTH_SHORT
            ).show();
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    MY_PERMISSIONS_ACCESS_NETWORK_STATE);
            return;
        } else
            Toast.makeText(
                    this,
                    "Allowed to access network",
                    Toast.LENGTH_SHORT
            ).show();
        //INTERNET PERMISSION
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
        ) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                    this,
                    "Not allowed to access internet",
                    Toast.LENGTH_SHORT
            ).show();
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.INTERNET},
                    MY_PERMISSIONS_INTERNET
            );
            return;
        } else
            Toast.makeText(
                    this,
                    "Allowed to use Internet",
                    Toast.LENGTH_SHORT
            ).show();
        //EXTERNAL STORAGE PERMISSION
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permition not granted to write external storage", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            return;
        }
        Toast.makeText(this, "Permition Already granted to access external storage", Toast.LENGTH_SHORT).show();

    }

    private void initSpinner() {
        Set <Map.Entry<String,Long>> set=city_map.entrySet();
        for(Map.Entry<String,Long> me: set)
            arrayList.add(String.valueOf(me.getKey()));
        adpt = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                arrayList
        );
        spinner.setAdapter(adpt);
    }

    private void getList() {

    }

    private void findViews() {
        arrayList = new ArrayList<String>();
        spinner = findViewById(R.id.spinner);
        tvCity = findViewById(R.id.tvCity);
        tvTemp = findViewById(R.id.tvTemp);
        tvWeather = findViewById(R.id.tvWeather);
        tvCord = findViewById(R.id.tvCord);
        imgView = findViewById(R.id.img);
    }

    public void showWeather(long city_id) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        if (networkInfo != null && networkInfo.isConnected()) {
            MyNetworkTask myNetworkTask = new MyNetworkTask();
            myNetworkTask.execute(
                    String.valueOf(String.valueOf(city_id))
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
                        "http://api.openweathermap.org/data/2.5/weather?id="
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
                Log.i("$$$$$EXCEPTION$$$$","$$$$$$$$$$MalformedURLException$$$$$$$$$$");
                e.printStackTrace();
            } catch (IOException e) {
                Log.i("$$$$$EXCEPTION$$$$","$$$$$$$$$$IOException$$$$$$$$$$");
                e.printStackTrace();
            } catch (JSONException e) {
                Log.i("$$$$$EXCEPTION$$$$","$$$$$$$$$$JSONException$$$$$$$$$$");
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
            return weatherData;
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
                            + "\t"
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
                            + "\t"
                            + "Lon"
                            + weatherData.getLng()
            );
            imgView.setImageBitmap(
                    weatherData.getImg()
            );
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
    }
}
