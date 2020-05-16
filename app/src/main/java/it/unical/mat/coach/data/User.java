package it.unical.mat.coach.data;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {

    private String name;
    private String email;
    private String gender;
    private String friend_number;
    private int weight;
    private int height;
    private String pic;
    private List<Workout> workouts;
    private List<Integer> workoutDays;

    public User(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public List<Workout> getWorkouts() {
        if(workouts == null)
            return  new ArrayList<Workout>();
        return workouts;
    }

    public void setWorkouts(List<Workout> workouts) {
        this.workouts = workouts;
    }

    public List<Integer> getWorkoutDays() {
        return workoutDays;
    }

    public void setWorkoutDays(List<Integer> workoutDays) {
        this.workoutDays = workoutDays;
    }

    public String getFriend_number() { return friend_number; }

    public void setFriend_number(String friend_number) {  this.friend_number = friend_number; }
}
