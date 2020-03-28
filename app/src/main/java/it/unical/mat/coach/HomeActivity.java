package it.unical.mat.coach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import it.unical.mat.coach.data.User;

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
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback {

    BottomNavigationView bottomNavigationView;
    private TextView home_msg;
    /* userLocation */
    private User user;
    private static final int REQUEST_CODE = 101;
    private String city;
    private TextView cityView;
    /* weather */
    private TextView weatherView;
    private TextView humidityView;
    private TextView degreeView;
    private ImageView weatherImageView;
    private FusedLocationProviderClient mFusedLocationClient;
    private Weather weather;
    private String content;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        /* menu */
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.home_navigation);
        handleMenu();

        user = (User) getIntent().getSerializableExtra("user");
        home_msg = findViewById(R.id.home_msg);
        cityView = findViewById(R.id.city);
        weather = new Weather();
        weatherView = findViewById(R.id.weather_description);
        humidityView = findViewById(R.id.humidity);
        degreeView = findViewById(R.id.degree);
        weatherImageView = findViewById(R.id.weatherpic);
        home_msg.setText("Hello " + user.getName() + "!");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        user = (User) getIntent().getSerializableExtra("user");
    }

    private void handleMenu() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_navigation:
                        break;
                    case R.id.profile_navigation:
                        goToProfile();
                        break;
                    case R.id.work_navigation:
                        goToWork();
                        break;
                    case R.id.exit_navigation:
                        signOut();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private void signOut() {
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

    private void goToProfile() {
        Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    protected void goToWork() {
        Intent intent = new Intent(HomeActivity.this, WorkActivity.class);
        intent.putExtra("email", user.getEmail());
        startActivity(intent);
    }

    /*private void fetchLocation() {
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
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
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
                                //Get city data
                                String info = addresses.get(0).getAddressLine(0);
                                String info_location[] = info.split(",");
                                city = info_location[1];
                                String city_split[] = city.split(" ");
                                city = city_split[2] + ", " + city_split[3] + info_location[2];
                                cityView.setText(city);
                                try {
                                    String url = "https://openweathermap.org/data/2.5/weather?lat=" + String.valueOf(lat) + "&lon=" + String.valueOf(lon) + "&appid=b6907d289e10d714a6e88b30761fae22";
                                    content = weather.execute(url).get();
                                    //content is a json
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
    */

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }
        Task<Location> task = mFusedLocationClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
                    supportMapFragment.getMapAsync(HomeActivity.this);

                    // weather
                    Geocoder geocoder = new Geocoder(HomeActivity.this, Locale.getDefault());
                    List<Address> addresses = null;
                    try {
                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //Get city data
                    String info = addresses.get(0).getAddressLine(0);
                    String info_location[] = info.split(", ");
                    city = info_location[info_location.length - 2] + ", " + info_location[info_location.length - 1];
                    cityView.setText(city);

                    try {
                        String url = "https://openweathermap.org/data/2.5/weather?lat=" +
                                String.valueOf(location.getLatitude()) + "&lon=" + String.valueOf(location.getLongitude())
                                + "&appid=b6907d289e10d714a6e88b30761fae22";
                        content = weather.execute(url).get();
                        //content is a json
                        JSONObject jsonObject = new JSONObject(content);
                        updateWeatherStatus(jsonObject);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng latlng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latlng).title("I am here");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10));
        googleMap.addMarker(markerOptions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    fetchLocation();
                break;
        }
    }

    protected void updateWeatherStatus(JSONObject jsonObject) {
        //weatherData is an array
        String weatherData = "";
        String mainData = "";
        String iconCode = "";
        try {

            weatherData = jsonObject.getString("weather");
            mainData = jsonObject.getString("main");
            JSONArray weatherArray = new JSONArray(weatherData);

            for (int i = 0; i < weatherArray.length(); i++) {
                JSONObject weatherPart = weatherArray.getJSONObject(i);
                String weatherDescription = weatherPart.getString("main") + ", " + weatherPart.getString("description");
                weatherView.setText(weatherDescription);
                iconCode = weatherPart.getString("icon");
            }
            /* set weather icon */
            String iconUrl = "http://openweathermap.org/img/wn/" + iconCode + ".png";
            Picasso.get().load(iconUrl).into(weatherImageView);

            JSONObject mainPart = new JSONObject(mainData);
            String degree = mainPart.getString("temp") + "°C";
            String humidity = "Humidity: " + mainPart.getString("humidity") + "%";
            degreeView.setText(degree);
            humidityView.setText(humidity);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    class Weather extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... address) {
            /* String... means multiple address can be send. It acts as array */
            try {
                URL url = new URL(address[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //Establish connection with address
                connection.connect();
                /* retrieve data from url */
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                /* Retrieve data and return it as String */
                int data = isr.read();
                String content = "";
                char ch;
                while (data != - 1) {
                    ch = (char) data;
                    content = content + ch;
                    data = isr.read();
                }
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
