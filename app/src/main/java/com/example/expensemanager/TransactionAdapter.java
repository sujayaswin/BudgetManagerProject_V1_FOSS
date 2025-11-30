package com.example.expensemanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class TransactionAdapter extends ArrayAdapter<Expense> {
    public TransactionAdapter(Context ctx, List<Expense> items) {
        super(ctx, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Expense e = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_expense, parent, false);
        }
        TextView tvDate = convertView.findViewById(R.id.tvDate);
        TextView tvCat = convertView.findViewById(R.id.tvCat);
        TextView tvAmt = convertView.findViewById(R.id.tvAmt);
        TextView tvNote = convertView.findViewById(R.id.tvNote);

        tvDate.setText(e.date);
        tvCat.setText(e.category + " (" + e.type + ")");
        tvAmt.setText(String.format(java.util.Locale.US, "%.2f", e.amount));
        tvNote.setText(e.note == null ? "" : e.note);
        return convertView;
    }
}
