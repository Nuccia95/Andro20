package it.unical.mat.coach.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import it.unical.mat.coach.R;
import it.unical.mat.coach.data.Database;
import it.unical.mat.coach.data.User;
import it.unical.mat.coach.data.Workout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class WorkActivity extends AppCompatActivity implements SensorEventListener {

    BottomNavigationView bottomNavigationView;

    private TextView stepsView;
    private TextView kmView;
    private TextView calView;
    private TextView goalView;
    private ImageButton startButton;
    private ImageButton stopButton;
    private Button callButton;
    private static final int REQUEST_CALL = 1;

    private Chronometer chronometer;
    private ProgressBar progressBar;
    private User user;

    /* workout data */
    private boolean startedWorkout;
    private long currentSteps;
    private float currentKilometers;
    private float stepLength;
    private float cal;
    private float MET = 4.3f; //metabolic equivalent of task
    private float goal;

    private SensorManager sensorManager;
    private Sensor sensorSteps;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);
        /* get current user */
        String email = getIntent().getStringExtra("email");
        Database.getDatabase().getReference("users").child(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                ArrayList<Workout> workouts = new ArrayList<>();
                user.setWorkouts(workouts);
                stepLength = (float) (user.getHeight() * 0.415);
                Database.getDatabase().getReference("users").child(user.getEmail()).child("workouts").addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                    Workout w = ds.getValue(Workout.class);
                                    user.getWorkouts().add(w);
                                }
                                String goal_view = String.format("%.2f", getGoal()) + " Km";
                                goalView.setText(goal_view);
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

        startedWorkout = false;
        /* menu */
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.home_navigation);
        handleMenu();
        /* workout */
        chronometer = (Chronometer) findViewById(R.id.simpleChronometer);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setProgress(0);
        stepsView = findViewById(R.id.steps);
        kmView = findViewById(R.id.kilometers);
        calView = findViewById(R.id.cal);
        goalView = findViewById(R.id.goal_view);
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);
        callButton = findViewById(R.id.emergency_call);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Workout Started", Toast.LENGTH_SHORT).show();
                startedWorkout = true;
                startTime = Calendar.getInstance().getTimeInMillis();
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Workout Added", Toast.LENGTH_SHORT).show();
                startedWorkout = false;
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.stop();
                Workout workout = new Workout(currentKilometers, 0, Calendar.getInstance().getTime());
                user.getWorkouts().add(workout);
                Database.getDatabase().getReference("users").child(user.getEmail()).setValue(user);
                stepsView.setText("Steps");
                calView.setText("Cal");
                kmView.setText("Kilometers");
                progressBar.setProgress(0);
                cal = 0;
                currentKilometers = 0;
                currentSteps = 0;
                Toast.makeText(getApplicationContext(), "Workout Added", Toast.LENGTH_SHORT).show();
                goToHome();
            }
        });
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall();
            }
        });
        /* sensors */
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorSteps = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
    }

    private void handleMenu() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_navigation:
                        goToHome();
                        break;
                    case R.id.profile_navigation:
                        goToProfile();
                        break;
                    case R.id.work_navigation:
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
                        Intent intent = new Intent(WorkActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    protected void goToProfile() {
        Intent intent = new Intent(WorkActivity.this, ProfileActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    protected void goToHome() {
        Intent intent = new Intent(WorkActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorSteps, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this, sensorSteps);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(startedWorkout) {
            Sensor sensor = event.sensor;
            if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                long currentTime = Calendar.getInstance().getTimeInMillis();

                float duration = (currentTime - startTime) / 1000;
                currentSteps++;
                stepsView.setText(String.valueOf(currentSteps));

                currentKilometers = (currentSteps * stepLength) / 100000;
                int progress = (int) (100 * currentKilometers / goal);
                progressBar.setProgress(progress);
                kmView.setText(String.format("%.2f", currentKilometers));

                float distance = (currentKilometers * 1000f);
                float speed = distance / duration;

                MET = updateMET(speed);
                cal = MET * user.getWeight() * duration / 3600f;

                calView.setText(Integer.toString((int) cal));
            }
        }
    }

    private float updateMET(float speed) {
        float currentMET = 3;
        if (speed >= 1.1 && speed <= 1.6)
            currentMET = 4.3f;
        else if (speed >= 1.7 && speed <= 2)
            currentMET = 5.6f;
        else if (speed >= 2.1 && speed <= 2.3)
            currentMET = 7;
        else if (speed >= 2.4 && speed <= 2.7)
            currentMET = 8.5f;
        else if (speed > 2.7)
            currentMET = 10;
        return currentMET;
    }

    private float getGoal(){
        float lastKm = user.getWorkouts().get(user.getWorkouts().size() - 1).getKm();
        if(lastKm > 0 ) {
            float goal = (lastKm <= 5 ? 5 : lastKm + 0.2f);
            goal = (goal >= 15 ? 15 : goal);
            this.goal = goal;
        }
        return goal;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void makePhoneCall() {
        String number = user.getFriend_number();
        if (number.trim().length() > 0) {
            if (ContextCompat.checkSelfPermission(WorkActivity.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(WorkActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            } else {
                String dial = "tel:" + number;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }
        } else {
            Toast.makeText(WorkActivity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

}