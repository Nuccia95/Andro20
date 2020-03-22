package it.unical.mat.coach.data;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {

    private String firstName;
    private String lastName;
    private String email;
    private List<Workout> workouts;

    public User(){

    }

    public User(String firstName, String lastName, String email, List<Workout> workouts) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.workouts = workouts;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Workout> getWorkouts() {
        return workouts;
    }

    public void setWorkouts(List<Workout> workouts) {
        this.workouts = workouts;
    }
}
