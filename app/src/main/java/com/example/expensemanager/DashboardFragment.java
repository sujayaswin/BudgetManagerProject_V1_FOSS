package com.example.expensemanager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private TextView tvIncome, tvExpense, tvBalance;
    private ListView lvCategoryTotals;
    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tvIncome = view.findViewById(R.id.tvIncome);
        tvExpense = view.findViewById(R.id.tvExpense);
        tvBalance = view.findViewById(R.id.tvBalance);
        lvCategoryTotals = view.findViewById(R.id.lvCategoryTotals);

        db = new DatabaseHelper(requireContext());

        return view;
    }

    public void updateData(int year, int month) {
        if (getContext() == null) return;

        double income = db.totalForMonthAndType(year, month, "INCOME");
        double expense = db.totalForMonthAndType(year, month, "EXPENSE");

        tvIncome.setText(String.format(Locale.US, "%.2f", income));
        tvExpense.setText(String.format(Locale.US, "%.2f", expense));
        tvBalance.setText(String.format(Locale.US, "%.2f", income - expense));

        // Colors are set in XML, but we can enforce them or add logic here if needed.
        // For now, relying on XML is cleaner.
        // tvIncome.setTextColor(Color.parseColor("#388E3C"));
        // tvExpense.setTextColor(Color.parseColor("#D32F2F"));
        // tvBalance.setTextColor(Color.parseColor("#212121"));

        // Category Totals
        Map<String, Double> categoryTotals = db.getCategoryTotalsForMonth(year, month);
        List<String> displayList = new ArrayList<>();
        if (categoryTotals != null) {
            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                displayList.add(entry.getKey() + ": " + String.format(Locale.US, "%.2f", Math.abs(entry.getValue())));
            }
        }
        
        // Use custom layout item_category which has black text color
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.item_category, displayList);
        lvCategoryTotals.setAdapter(adapter);
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
