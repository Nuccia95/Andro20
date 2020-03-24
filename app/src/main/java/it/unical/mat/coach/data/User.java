package it.unical.mat.coach.data;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {

    private String firstName;
    private String lastName;
    private String email;
    private String gender;
    private float weight;
    private int height;
    private List<Workout> workouts;

    public User(){

    }

    public User(String firstName, String lastName, String email, String gender, int weight, int height, List<Workout> workouts) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.gender = gender;
        this.weight = weight;
        this.height = height;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<Workout> getWorkouts() {
        return workouts;
    }

    public void setWorkouts(List<Workout> workouts) {
        this.workouts = workouts;
    }
}
