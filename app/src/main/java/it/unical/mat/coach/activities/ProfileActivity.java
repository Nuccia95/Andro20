package it.unical.mat.coach.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import it.unical.mat.coach.R;
import it.unical.mat.coach.data.Database;
import it.unical.mat.coach.dialogs.EditDaysDialog;
import it.unical.mat.coach.dialogs.EditProfileDialog;
import it.unical.mat.coach.data.User;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity implements EditProfileDialog.EditDialogListener, EditDaysDialog.EditDialogListener{

    BottomNavigationView bottomNavigationView;
    /* user info */
    private TextView nameView;
    private TextView weightView;
    private TextView heightView;
    private TextView genderView;
    private TextView numberView;
    private ImageButton edit_button;
    private ImageButton days_button;
    private ImageView picView;
    /* bar chart */
    private BarChart barChart;
    private BarData barData;
    private BarDataSet barDataSet;
    private ArrayList barEntries;
    private ArrayList<String> labels;
    private User user;

    /* share facebook post */
    private ImageButton share_button;
    private CallbackManager callbackManager;
    private ShareDialog shareDialog;
    private Target target;

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
                openEditDialog();
            }
        });

        days_button = findViewById(R.id.days_button);
        days_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWorkoutDaysDialog();
            }
        });

        /* facebook */
        callbackManager = CallbackManager.Factory.create();
        share_button = findViewById(R.id.share_button);
        shareDialog = new ShareDialog(this);
        target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                SharePhoto sharePhoto = new SharePhoto.Builder().setBitmap(bitmap).build();
                if(shareDialog.canShow(SharePhotoContent.class)){
                    SharePhotoContent content = new SharePhotoContent.Builder().addPhoto(sharePhoto).build();
                    shareDialog.show(content);
                }
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Picasso.get()
                        .load(R.drawable.post)
                        .into(target);

                shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {
                        Toast.makeText(ProfileActivity.this, "You shared a post", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onCancel() {
                        Toast.makeText(ProfileActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(ProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        /* user info*/
        user = (User) getIntent().getSerializableExtra("user");
        nameView = findViewById(R.id.name_view);
        weightView = findViewById(R.id.weight_number);
        heightView = findViewById(R.id.height_number);
        genderView = findViewById(R.id.gender_value);
        numberView = findViewById(R.id.friend_number_value);
        picView = findViewById(R.id.profile_picture);
        if(user.getPic() != null)
            Picasso.get().load(user.getPic()).into(picView);
        nameView.setText(user.getName());
        weightView.setText(String.valueOf(user.getWeight()));
        heightView.setText(String.valueOf(user.getHeight()));
        genderView.setText(user.getGender());
        numberView.setText(user.getFriend_number());
        barChart = findViewById(R.id.BarChart);
        setBarChart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
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

    protected void goToWork(){
        Intent intent = new Intent(ProfileActivity.this, WorkActivity.class);
        intent.putExtra("email", user.getEmail());
        startActivity(intent);
        finish();
    }

    protected void goToHome(){
        Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void signOut(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    /* bar chart */
    private void setBarChart(){
        getEntries();
        barDataSet = new BarDataSet(barEntries, "");
        barDataSet.setColors(Color.WHITE);
        barData = new BarData(barDataSet);
        barData.setValueTextColor(Color.TRANSPARENT);
        barData.setBarWidth(0.3f);
        barChart.setData(barData);
        barChart.getDescription().setText("Km and Date");
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.setTouchEnabled(false);
        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisRight().setTextColor(Color.TRANSPARENT);
        barChart.getXAxis().setTextColor(Color.WHITE);
        barChart.getLegend().setEnabled(false);
        barChart.animateXY(2000, 2500);

        XAxis x = barChart.getXAxis();
        x.setLabelCount(labels.size());
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setGranularity(0.5f);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
    }

    private void getEntries() {
        labels = new ArrayList<>();
        barEntries = new ArrayList<>();
        user.getWorkouts().remove(0);

       if(user.getWorkouts().size() >= 5){
            for (int i = user.getWorkouts().size() - 5; i < user.getWorkouts().size(); i++) {
                barEntries.add(new BarEntry(i, user.getWorkouts().get(i).getKm()));
                DateFormat dateFormat = new SimpleDateFormat("dd-MM");
                String date = dateFormat.format(user.getWorkouts().get(i).getDate());
                labels.add(date);
            }
        }else{
           for (int i = 0; i < user.getWorkouts().size(); i++) {
               barEntries.add(new BarEntry(i, user.getWorkouts().get(i).getKm()));
               DateFormat dateFormat = new SimpleDateFormat("dd-MM");
               String date = dateFormat.format(user.getWorkouts().get(i).getDate());
               labels.add(date);
           }
       }
    }

    public void openEditDialog() {
        EditProfileDialog editDialog = new EditProfileDialog();
        editDialog.show(getSupportFragmentManager(), "edit dialog");
    }

    public void openWorkoutDaysDialog(){
        EditDaysDialog daysDialog = new EditDaysDialog();
        daysDialog.show(getSupportFragmentManager(), "workout days dialog");
    }

    @Override
    public void applyTexts(String weight, String height, String gender, String number) {
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
        if(!user.getFriend_number().matches("")){
            numberView.setText(number);
            user.setFriend_number(number);
        }

        Database.getDatabase().getReference("users").child(user.getEmail()).setValue(user);
        Toast.makeText(getApplicationContext(),"Info Updated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public  void setDays(boolean[] checkedDays){
        user.setWorkoutDays(new ArrayList<Integer>());
        if(checkedDays!=null)
            for(int i=0; i<checkedDays.length; i++){
                    if(checkedDays[i]){
                        switch (i){
                            case 0:
                                user.getWorkoutDays().add(2);
                                break;
                            case 1:
                                user.getWorkoutDays().add(3);
                                break;
                            case 2:
                                user.getWorkoutDays().add(4);
                                break;
                            case 3:
                                user.getWorkoutDays().add(5);
                                break;
                            case 4:
                                user.getWorkoutDays().add(6);
                                break;
                            case 5:
                                user.getWorkoutDays().add(7);
                                break;
                            case 6:
                                user.getWorkoutDays().add(1);
                                break;
                            default:
                                break;
                        }
                    }
            }
        Database.getDatabase().getReference("users").child(user.getEmail()).setValue(user);
        Toast.makeText(getApplicationContext(),"Days Updated", Toast.LENGTH_SHORT).show();
    }

}