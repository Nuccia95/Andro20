package it.unical.mat.coach.data;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Database {

    private static FirebaseDatabase db;

    public static FirebaseDatabase getDatabase(){
        if(db == null)
            db = FirebaseDatabase.getInstance();
        return db;
    }

}
