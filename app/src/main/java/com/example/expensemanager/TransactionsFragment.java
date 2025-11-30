package com.example.expensemanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
        return view;
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
