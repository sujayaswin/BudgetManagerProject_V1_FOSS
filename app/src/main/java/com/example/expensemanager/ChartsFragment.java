package com.example.expensemanager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Map;

public class ChartsFragment extends Fragment {

    private PieChart pieChart;
    private TextView tvChartTitle;
    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_charts, container, false);
        pieChart = view.findViewById(R.id.pieChart);
        tvChartTitle = view.findViewById(R.id.tvChartTitle);
        db = new DatabaseHelper(requireContext());
        setupPieChart();
        return view;
    }

    private void setupPieChart() {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Expenses");
        pieChart.setCenterTextSize(24);
        pieChart.getDescription().setEnabled(false);

        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(true);
    }

    public void updateData(int year, int month) {
        if (getContext() == null) return;
        
        // Update Title
        // Note: Getting month name might be better, but for simplicity keeping it generic or handled by formatting
        tvChartTitle.setText("Expense Breakdown");

        Map<String, Double> categoryTotals = db.getCategoryTotalsForMonth(year, month);
        loadPieChartData(categoryTotals);
    }

    private void loadPieChartData(Map<String, Double> categoryTotals) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        if (categoryTotals != null) {
            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                if (entry.getValue() < 0) { // We only want to chart expenses
                    entries.add(new PieEntry(Math.abs(entry.getValue().floatValue()), entry.getKey()));
                }
            }
        }

        if (entries.isEmpty()) {
             pieChart.clear();
             pieChart.setNoDataText("No expense data available for this month.");
             return;
        }

        ArrayList<Integer> colors = new ArrayList<>();
        for (int color : ColorTemplate.MATERIAL_COLORS) {
            colors.add(color);
        }
        for (int color : ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expense Categories");
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            updateData(activity.getShowYear(), activity.getShowMonth());
        }
    }
}
