package it.unical.mat.coach;

import androidx.appcompat.app.AppCompatActivity;
import it.unical.mat.coach.data.User;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

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

        user = (User) getIntent().getSerializableExtra("user");
        barChart = findViewById(R.id.BarChart);
        getEntries();

        barDataSet = new BarDataSet(barEntries, "");
        barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.getDescription().setText("Km and Date");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(18f);

        XAxis x = barChart.getXAxis();
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setGranularity(1f);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setLabelRotationAngle(-45);
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


}
