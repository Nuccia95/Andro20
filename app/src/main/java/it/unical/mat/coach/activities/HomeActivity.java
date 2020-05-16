package it.unical.mat.coach.activities;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import it.unical.mat.coach.R;
import it.unical.mat.coach.data.Database;
import it.unical.mat.coach.utils.NearbyPlacesShower;
import it.unical.mat.coach.utils.ReminderBroadcast;
import it.unical.mat.coach.data.User;
import it.unical.mat.coach.data.Workout;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback {

    BottomNavigationView bottomNavigationView;
    private TextView home_msg;
    /* user */
    private User user;
    private static final int REQUEST_CODE = 101;
    /* weather */
    private TextView weatherView;
    private TextView humidityView;
    private TextView degreeView;
    private TextView suggestion;
    private ImageView weatherImageView;
    private Weather weather;
    /* location */
    private ImageButton locationButton;
    private FusedLocationProviderClient mFusedLocationClient;
    private String content;
    private GoogleMap gMap;
    private int PROXIMITY_RADIUS = 10000;
    private String city;
    private TextView cityView;
    private Location currentLocation;
    private boolean sunny;
    private ArrayList<String> places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        /* menu */
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.home_navigation);
        handleMenu();
        createNotificationChannel();

        user = (User) getIntent().getSerializableExtra("user");
        setReminder();
        home_msg = findViewById(R.id.home_msg);
        cityView = findViewById(R.id.city);
        weather = new Weather();
        weatherView = findViewById(R.id.weather_description);
        humidityView = findViewById(R.id.humidity);
        degreeView = findViewById(R.id.degree);
        locationButton = findViewById(R.id.location_button);
        suggestion = findViewById(R.id.suggestion);
        weatherImageView = findViewById(R.id.weatherpic);
        home_msg.setText("Hi " + user.getName() + "!");
        places = new ArrayList<String>();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("onresume", "ON RESUME CALLED");
        Database.getDatabase().getReference("users").child(user.getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                ArrayList<Workout> workouts = new ArrayList<>();
                user.setWorkouts(workouts);
                Database.getDatabase().getReference("users").child(user.getEmail()).child("workouts").addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                    Workout w = ds.getValue(Workout.class);
                                    user.getWorkouts().add(w);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        }
                );
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
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
    /* goTo */
    private void goToProfile(){
        Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    protected void goToWork() {
        Intent intent = new Intent(HomeActivity.this, WorkActivity.class);
        intent.putExtra("email", user.getEmail());
        startActivity(intent);
    }

    private void signOut() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    /* notification */
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "1ReminderChannel";
            String description = "channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void setReminder(){
       for (int day: user.getWorkoutDays()) {
            if(day >= 1)
                scheduleAlarm(day);
        }
    }

    private void scheduleAlarm(int dayOfWeek) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        // Check we aren't setting it in the past which would trigger it to fire instantly
        if(calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 7);
        }
        // Set this to whatever you were planning to do at the given time
        Intent intent = new Intent(this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    /* location */
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
                    if(info_location[info_location.length - 2] != null && info_location[info_location.length - 1] != null)
                        city = info_location[info_location.length - 2] + ", " + info_location[info_location.length - 1];
                    else
                        city = "";
                    cityView.setText(city);
                    try {
                        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" +
                                String.valueOf(location.getLatitude()) + "&lon=" + String.valueOf(location.getLongitude())
                                + "&appid=f55e43ed04bda0c6a674505ba7a645ce";
                        Log.i("url", url);
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
        gMap = googleMap;
        LatLng latlng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latlng).title("I am here");
        gMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10));
        gMap.addMarker(markerOptions);

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url;
                    Log.i("sunny", String.valueOf(isSunny()));
                if(isSunny()) {
                    url = getUrl(currentLocation.getLatitude(), currentLocation.getLongitude(), "park");
                }else {
                    url = getUrl(currentLocation.getLatitude(), currentLocation.getLongitude(), "gym");
                }
                Object[] DataTransfer = new Object[2];
                DataTransfer[0] = gMap;
                DataTransfer[1] = url;
                NearbyPlacesShower nearbyPlacesShower = new NearbyPlacesShower();
                nearbyPlacesShower.execute(DataTransfer);
            }
        });
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyDRQ4hhx1TDRuJ6Uizxx-2KEWWcNEMK1kI");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
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

    public ArrayList getPlaces() {
        return places;
    }

    public void setPlaces(ArrayList places) {
        this.places = places;
    }


    /* weather */
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
                String description = weatherPart.getString("description");
                String weatherDescription = weatherPart.getString("main") + ", " + weatherPart.getString("description");
                weatherView.setText(weatherDescription);
                setSuggestion(description);
                iconCode = weatherPart.getString("icon");
            }
            /* set weather icon */
            String iconUrl = "http://openweathermap.org/img/wn/" + iconCode + ".png";
            Picasso.get().load(iconUrl).into(weatherImageView);

            JSONObject mainPart = new JSONObject(mainData);
            float tmp = Float.parseFloat(mainPart.getString("temp"));
            tmp -= 273;
            String humidity = "Humidity: " + mainPart.getString("humidity") + "%";
            degreeView.setText(String.format("%.1f", tmp) + "°C");
            humidityView.setText(humidity);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public  void setSuggestion(String description){
        Log.i("description", description);
        if(description.contains("clear sky") || description.contains("clouds")){
            suggestion.setText("According to the weather today\n is better to go to a park!");
            setSunny(true);
        }
        else if(description.contains("rain") || description.contains("thunderstorm") ||
                description.contains("snow") || description.contains("mist") || description.contains("drizzle")){
            suggestion.setText("According to the weather today\n is better to go to the gym!");
            setSunny(false);
        }
    }

    public boolean isSunny() {
        return sunny;
    }

    public void setSunny(boolean sunny) {
        this.sunny = sunny;
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