package com.example.expensemanager;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
    private List<String> categoryNames = new ArrayList<>();
    private int currentYear, currentMonth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tvIncome = view.findViewById(R.id.tvIncome);
        tvExpense = view.findViewById(R.id.tvExpense);
        tvBalance = view.findViewById(R.id.tvBalance);
        lvCategoryTotals = view.findViewById(R.id.lvCategoryTotals);

        db = new DatabaseHelper(requireContext());

        lvCategoryTotals.setOnItemLongClickListener((parent, view1, position, id) -> {
            if (position < categoryNames.size()) {
                showCategoryTransactionsPopup(categoryNames.get(position));
            }
            return true;
        });

        return view;
    }

    private void showCategoryTransactionsPopup(String category) {
        ArrayList<Expense> transactions = db.listForMonthAndCategory(currentYear, currentMonth, category);
        
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_category_transactions, null);
        ListView lvTransactions = dialogView.findViewById(R.id.lvTransactions);
        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        
        tvTitle.setText("Transactions: " + category);
        
        TransactionAdapter adapter = new TransactionAdapter(requireContext(), transactions);
        lvTransactions.setAdapter(adapter);
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
        });

        dialog.show();
    }

    public void updateData(int year, int month) {
        if (getContext() == null) return;
        
        this.currentYear = year;
        this.currentMonth = month;

        double income = db.totalForMonthAndType(year, month, "INCOME");
        double expense = db.totalForMonthAndType(year, month, "EXPENSE");

        double balance = income - expense;
        tvIncome.setText(String.format(Locale.US, "%.2f", income));
        tvExpense.setText(String.format(Locale.US, "%.2f", expense));
        tvBalance.setText(String.format(Locale.US, "%.2f", balance));

        // Dynamically set balance color
        if (balance > 0) {
            tvBalance.setTextColor(Color.parseColor("#388E3C")); // Green
        } else if (balance < 0) {
            tvBalance.setTextColor(Color.parseColor("#D32F2F")); // Red
        } else {
            tvBalance.setTextColor(Color.parseColor("#1976D2")); // Blue for zero
        }

        // Category Totals
        Map<String, Double> categoryTotals = db.getCategoryTotalsForMonth(year, month);
        List<String> displayList = new ArrayList<>();
        categoryNames.clear();
        if (categoryTotals != null) {
            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                categoryNames.add(entry.getKey());
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
