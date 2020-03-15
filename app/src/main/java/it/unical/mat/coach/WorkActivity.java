package it.unical.mat.coach;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Calendar;

public class WorkActivity extends AppCompatActivity implements SensorEventListener {


    private TextView stepsView;
    private TextView kmView;
    private TextView calView;

    private long currentSteps;
    private static final int averageStepMan = 78; //cm possiamo capire se l'utente è maschio o femmina
    private static final int averageStepWoman = 70;
    private double MET = 4.3; //metabolic equivalent of task
    private float km;
    private double cal;


    private SensorManager sensorManager;
    private Sensor sensorSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

        stepsView = findViewById(R.id.steps);
        kmView = findViewById(R.id.kilometers);
        calView = findViewById(R.id.cal);

        stepsView.setText("0");
        kmView.setText("0,00");
        calView.setText("0");

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorSteps = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
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
        Sensor sensor = event.sensor;
        float[] values = event.values;
        long startTime = getIntent().getLongExtra("startTime", 0);
        long currentTime = Calendar.getInstance().getTime().getTime();

        int duration = (int)(currentTime - startTime) / 1000; //seconds

        if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            currentSteps ++;
            stepsView.setText(String.valueOf(currentSteps));

            float currentKilometers = updateKilometersRun(currentSteps);

            long distance =  (long) currentKilometers * 1000;  //meters
            float speed = distance / duration; //m/s
            updateMET(speed);

            //updateCal
            int weight = 55;
            cal = MET * weight * duration;
            cal = (int) cal;
            calView.setText(String.format("%d", cal));
        }
    }

    private void updateMET(float speed){
        double currentMET = 10;
        /*if(speed >= 1.1 && speed <= 1.6)
            currentMET = 4.3;
        else if(speed >= 1.7 && speed <= 2)
            currentMET = 5.6;
        else if(speed >= 2.1 && speed <= 2.3)
            currentMET = 7;
        else if(speed >= 2.4 && speed <= 2.7)
            currentMET = 8.5;
        else if(speed > 2.7)
            currentMET = 10;*/
        MET = (currentMET + MET) /2;
    }

    private float updateKilometersRun(long steps){
        boolean man = true;
        if(man)
            km = (float) steps * averageStepMan / (float) 100000;
        else
            km = (float) steps * averageStepWoman/ (float) 100000;

        kmView.setText(String.format("%.2f", km));
        return km;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
