package it.unical.mat.coach.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import it.unical.mat.coach.R;
import it.unical.mat.coach.data.Database;
import it.unical.mat.coach.data.User;
import it.unical.mat.coach.data.Workout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    private FirebaseDatabase db;
    private  GoogleSignInClient mGoogleSignInClient;
    private SignInButton signInButton;
    final static int CODE = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /* firebase */
        db = Database.getDatabase();
        /* google sign in*/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE) {
            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, CODE);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            final GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            final DatabaseReference usersReference = db.getReference("users");
            final String key = account.getEmail().split("@")[0];

            usersReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if(user == null){
                        user = new User();
                        user.setEmail(key);
                        user.setName(account.getDisplayName());
                        /* default info for test */
                        user.setWeight(50);
                        user.setHeight(160);
                        user.setGender("F");
                        user.setFriend_number("3291698240");
                        Date d1 = new Date(2020, 4, 3);
                        Date d2 = new Date(2020, 4, 5);
                        Date d3 = new Date(2020, 4, 7);
                        Date d4 = new Date(2020, 4, 9);
                        Date d5 = new Date(2020, 4, 11);
                        if(account.getPhotoUrl() != null)
                            user.setPic(account.getPhotoUrl().toString());
                        Workout workout = new Workout(0, 0, null);
                        Workout workout1 = new Workout(2, 5, d1);
                        Workout workout2 = new Workout(3, 5, d2);
                        Workout workout3 = new Workout(5, 5, d3);
                        Workout workout4 = new Workout(5.2f, 5, d4);
                        ArrayList<Workout> workouts = new ArrayList<>();
                        workouts.add(workout);
                        workouts.add(workout1);
                        workouts.add(workout2);
                        workouts.add(workout3);
                        workouts.add(workout4);
                        user.setWorkouts(workouts);
                        ArrayList<Integer> workoutDays = new ArrayList<>();
                        workoutDays.add(0);
                        workoutDays.add(3);
                        user.setWorkoutDays(workoutDays);
                        usersReference.child(key).setValue(user);
                    }
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    finish();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } catch (ApiException e) {
            Log.w("error failed code ", e.getCause());
        }
    }
}
