package it.unical.mat.coach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import it.unical.mat.coach.data.Database;
import it.unical.mat.coach.data.EditDialog;
import it.unical.mat.coach.data.User;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements EditDialog.EditDialogListener{

    BottomNavigationView bottomNavigationView;

    /* User Info */
    private TextView weightView;
    private TextView heightView;
    private TextView genderView;
    private ImageButton edit_button;
    /* Bar chart */
    private BarChart barChart;
    private BarData barData;
    private BarDataSet barDataSet;
    private ArrayList barEntries;
    private ArrayList<String> labels;
    private User user;

    public ProfileActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        /* menu */
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.profile_navigation);
        handleMenu();
        edit_button = findViewById(R.id.edit_button);
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        user = (User) getIntent().getSerializableExtra("user");
        weightView = findViewById(R.id.weight_number);
        heightView = findViewById(R.id.height_number);
        genderView = findViewById(R.id.gender_value);
        /* user info*/
        weightView.setText(String.valueOf(user.getWeight()));
        heightView.setText(String.valueOf(user.getHeight()));
        genderView.setText(user.getGender());
        /* bar chart */
        barChart = findViewById(R.id.BarChart);
        getEntries();
        barDataSet = new BarDataSet(barEntries, "");
        barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.getDescription().setText("Km and Date");
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.setTouchEnabled(false);

        barDataSet.setColors(ColorTemplate.LIBERTY_COLORS);

        XAxis x = barChart.getXAxis();
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setGranularity(1f);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setLabelRotationAngle(-45);
    }

    private void handleMenu(){
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home_navigation:
                        goToHome();
                        break;
                    case R.id.profile_navigation:
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

    private void getEntries() {
        labels = new ArrayList<>();
        barEntries = new ArrayList<>();
        for (int i = 0; i < user.getWorkouts().size(); i++) {
            barEntries.add(new BarEntry(i, user.getWorkouts().get(i).getKm()));

            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String date = dateFormat.format(user.getWorkouts().get(i).getDate());
            labels.add(date);
        }
    }

    private void signOut(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    protected void goToWork(){
        Intent intent = new Intent(ProfileActivity.this, WorkActivity.class);
        intent.putExtra("email", user.getEmail());
        startActivity(intent);
    }

    protected void goToHome(){
        Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    public void openDialog() {
        EditDialog editDialog = new EditDialog();
        editDialog.show(getSupportFragmentManager(), "edit dialog");
    }

    @Override
    public void applyTexts(String weight, String height, String gender) {
        if(!weight.matches("")){
            weightView.setText(weight);
            user.setWeight(Integer.parseInt(weight));
        }
        if(!height.matches("")){
            heightView.setText(height);
            user.setHeight(Integer.parseInt(height));
        }
        if(!gender.matches("") && (gender.equals("M") || gender.equals("F"))){
            genderView.setText(gender);
            user.setGender(gender);
        }
        Database.getDatabase().getReference("users").child(user.getEmail()).setValue(user);
    }

}
