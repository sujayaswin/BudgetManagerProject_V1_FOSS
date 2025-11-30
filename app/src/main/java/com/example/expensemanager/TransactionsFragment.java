package com.example.expensemanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class TransactionsFragment extends Fragment {

    private ListView lvTransactions;
    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);
        lvTransactions = view.findViewById(R.id.lvTransactions);
        db = new DatabaseHelper(requireContext());

        lvTransactions.setOnItemLongClickListener((parent, view1, position, id) -> {
            Expense expense = (Expense) parent.getItemAtPosition(position);
            showDeleteDialog(expense);
            return true;
        });

        return view;
    }

    private void showDeleteDialog(Expense expense) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.delete(expense.id);
                    Toast.makeText(getContext(), "Transaction deleted", Toast.LENGTH_SHORT).show();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).refresh();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void updateData(int year, int month) {
        if (getContext() == null) return;

        ArrayList<Expense> list = db.listForMonth(year, month);
        TransactionAdapter adapter = new TransactionAdapter(requireContext(), list);
        lvTransactions.setAdapter(adapter);
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
