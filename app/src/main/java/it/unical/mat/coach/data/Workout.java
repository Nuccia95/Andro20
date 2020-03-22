package it.unical.mat.coach.data;

import java.io.Serializable;
import java.util.Date;

public class Workout implements Serializable {

    private float km;
    private int goal;
    private Date date;

    public Workout() {
    }

    public Workout(float km, int goal, Date date) {
        this.km = km;
        this.goal = goal;
        this.date = date;
    }

    public float getKm() {
        return km;
    }

    public void setKm(float km) {
        this.km = km;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
