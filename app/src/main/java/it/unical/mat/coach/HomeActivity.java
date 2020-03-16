package it.unical.mat.coach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import it.unical.mat.coach.data.WeatherStatus;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private TextView home_msg;
    private Button sign_out_button;
    private Button start_work_button;
    /* UserLocation */
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private Button fetch;
    private TextView location_msg;
    private String city;
    private String country;
    private FusedLocationProviderClient mFusedLocationClient;
    /* Weather */
    private Weather weather;
    private String content;
    private TextView temperature_msg;
    private static final String WEATHER_KEY = "270542b4b246f260340f8626b61a1188";
    private WeatherStatus weatherStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        /* findViewByIds */
        sign_out_button = findViewById(R.id.sign_out_button);
        start_work_button = findViewById(R.id.start_work_button);
        fetch = findViewById(R.id.fetch);
        home_msg = findViewById(R.id.home_msg);
        location_msg = findViewById(R.id.location_msg);
        temperature_msg = findViewById(R.id.temperature_msg);

        home_msg.setText("Ciao " + getIntent().getStringExtra("name") + "!");
        sign_out_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        start_work_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWork();
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchLocation();
            }
        });

        weather = new Weather();
        weatherStatus = new WeatherStatus();
    }

    private void signOut(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    private void fetchLocation() {
        if (ContextCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Required UserLocation Permission")
                        .setMessage("You have to give this permission to acess this feature")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(HomeActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(HomeActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }
        } else {
            // Permission has already been granted
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                //Pass lat e lon to geocoder in order to get the current location
                                double lat = location.getLatitude();
                                double lon = location.getLongitude();
                                Geocoder geocoder = new Geocoder(HomeActivity.this, Locale.getDefault());
                                List<Address> addresses = null;
                                try {
                                    addresses = geocoder.getFromLocation(lat, lon, 1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                    String info = addresses.get(0).getAddressLine(0);
                                    String info_location [] = info.split(",");

                                    city = info_location[1];
                                    String city_split [] = city.split(" ");

                                    city = city_split[2] + ", " + city_split[3];
                                    country = info_location[2];

                                    location_msg.setText("LOCATION\n latitude: "+ lat + " longitude: " + lon + "\ncity: "+ city + " country: " + country);
                                try{
                                    String url = "https://openweathermap.org/data/2.5/weather?lat="+ String.valueOf(lat) +"&lon=" + String.valueOf(lon) + "&appid=b6907d289e10d714a6e88b30761fae22";
                                    content = weather.execute(url).get();
                                    //content is a json
                                    Log.i("contentData",content);
                                    JSONObject jsonObject = new JSONObject(content);
                                    updateWeatherStatus(jsonObject);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        }
    }

    protected void updateWeatherStatus(JSONObject jsonObject){
        //weatherData is an array
        String weatherData = "";
        String mainData = "";
        try {
            weatherData = jsonObject.getString("weather");
            mainData = jsonObject.getString("main");
            JSONArray weatherArray = new JSONArray(weatherData);

            for(int i = 0; i<weatherArray.length(); i++){
                JSONObject weatherPart = weatherArray.getJSONObject(i);
                weatherStatus.setMain(weatherPart.getString("main"));
                weatherStatus.setDescription(weatherPart.getString("description"));
                weatherStatus.setIcon(weatherPart.getString("icon") + ".png");
            }

            JSONObject mainPart = new JSONObject(mainData);
            weatherStatus.setTemperature(mainPart.getString("temp") + "°C");
            weatherStatus.setHumidity(mainPart.getString("humidity") + "%");

            temperature_msg.setText("TEMPERATURE\n" + "main: " + weatherStatus.getMain() + " description: " + weatherStatus.getDescription() +
                    "\ntemperature: " + weatherStatus.getTemperature() + " humidity: " + weatherStatus.getHumidity() + " icon: " +
                    weatherStatus.getIcon());


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void startWork(){
        Intent intent = new Intent(HomeActivity.this, WorkActivity.class);
        long startTime = Calendar.getInstance().getTime().getTime();
        intent.putExtra("startTime", startTime);
        startActivity(intent);
        finish();
    }

    class Weather extends AsyncTask <String, Void, String>{
        @Override
        protected String doInBackground(String... address) {
            //String... means multiple address can be send. It acts as array
            try {
                URL url = new URL(address[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //Establish connection with address
                connection.connect();

                //retrieve data from url
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);

                //Retrieve data and return it as String
                int data = isr.read();
                String content = "";
                char ch;
                while (data != -1){
                    ch = (char) data;
                    content = content + ch;
                    data = isr.read();
                }
                Log.i("Content",content);
                return content;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
