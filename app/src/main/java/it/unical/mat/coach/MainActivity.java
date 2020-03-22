package it.unical.mat.coach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase db;
    DatabaseReference dbUsers;
    GoogleSignInClient mGoogleSignInClient;
    SignInButton signInButton;
    final static int CODE = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Database*/
        db = FirebaseDatabase.getInstance();
        dbUsers = db.getReference("users");

        /*Google Sign In*/
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
        if(account == null){
            //the user has not yet signed in to your app, show signin button
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, CODE);
        //choose which google account do you want to use
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            User user = new User();
            user.setEmail(account.getEmail());
            user.setFirstName(account.getDisplayName().split(" ")[0]);
            user.setLastName(account.getDisplayName().split(" ")[1]);

            String email = user.getEmail().split("@")[0];
            Date date = Calendar.getInstance().getTime();
            Workout workout1 = new Workout(1, 10, date);
            Workout workout2 = new Workout(2, 20, date);
            Workout workout3 = new Workout(3.5f, 30, date);
            Workout workout4 = new Workout(5, 40, date);
            Workout workout5 = new Workout(5.6f, 50, date);
            Workout workout6 = new Workout(6.2f, 60, date);
            Workout workout7 = new Workout(7.2f, 60, date);
            List<Workout> workouts = new ArrayList<>();
            workouts.add(workout1);
            workouts.add(workout2);
            workouts.add(workout3);
            workouts.add(workout4);
            workouts.add(workout5);
            workouts.add(workout6);
            workouts.add(workout7);

            user.setWorkouts(workouts);
            //save
            dbUsers.child(email).setValue(user);

            dbUsers.child(email).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User u = dataSnapshot.getValue(User.class);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
            finish();
        } catch (ApiException e) {
            Log.w("error failed code ", e.getCause());
        }
    }


}
