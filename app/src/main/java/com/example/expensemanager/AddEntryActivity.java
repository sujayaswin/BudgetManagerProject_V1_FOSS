package com.example.expensemanager;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AddEntryActivity extends AppCompatActivity {
    EditText etDate, etAmount, etNote;
    Spinner spCategory;
    RadioGroup rgType;
    RadioButton rbIncome, rbExpense;
    DatabaseHelper db;
    private SharedPreferences sharedPreferences;

    // Calendar instance to keep track of the selected date
    final Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);

        db = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("CategoryPrefs", Context.MODE_PRIVATE);

        // --- Initialize Views ---
        etDate = findViewById(R.id.etDate);
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        spCategory = findViewById(R.id.spCategory);
        rgType = findViewById(R.id.rgType);
        rbIncome = findViewById(R.id.rbIncome);
        rbExpense = findViewById(R.id.rbExpense);

        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        // --- Set Default Date ---
        updateLabel(); // Set the current date immediately

        // --- DATE PICKER LOGIC (NEW) ---
        // 1. prevent manual typing
        etDate.setFocusable(false);
        etDate.setClickable(true);

        // 2. Setup the DatePickerDialog listener
        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel(); // Update the text field
        };

        // 3. Show the dialog on click
        etDate.setOnClickListener(v -> {
            // Use custom theme for DatePicker to ensure visibility
            new DatePickerDialog(AddEntryActivity.this, R.style.CustomDatePickerTheme, date,
                    myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });
        // --- END DATE PICKER LOGIC ---

        // --- Setup Dynamic Category Spinner ---
        if (rbExpense.isChecked()) {
            setupCategorySpinner("EXPENSE");
        } else {
            setupCategorySpinner("INCOME");
        }

        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbExpense) {
                setupCategorySpinner("EXPENSE");
            } else if (checkedId == R.id.rbIncome) {
                setupCategorySpinner("INCOME");
            }
        });

        // --- Set Button Listeners ---
        btnSave.setOnClickListener(v -> save());
        btnCancel.setOnClickListener(v -> finish());
    }

    // Helper method to update the EditText with the format YYYY-MM-DD
    private void updateLabel() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etDate.setText(sdf.format(myCalendar.getTime()));
    }

    private void setupCategorySpinner(String type) {
        String key = type.equals("INCOME") ? "income_categories" : "expense_categories";
        Set<String> categorySet = sharedPreferences.getStringSet(key, new HashSet<>());
        ArrayList<String> categoryList = new ArrayList<>(categorySet);
        Collections.sort(categoryList);

        // Use custom layouts with black text color
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                R.layout.item_spinner, categoryList);
        categoryAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spCategory.setAdapter(categoryAdapter);
    }

    private void save() {
        String date = etDate.getText().toString().trim();
        String amtS = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (spCategory.getSelectedItem() == null) {
            Toast.makeText(this, "Please add a category first in Settings", Toast.LENGTH_LONG).show();
            return;
        }
        String category = spCategory.getSelectedItem().toString();

        String type = rbIncome.isChecked() ? "INCOME" : "EXPENSE";
        double amount = 0;
        try {
            amount = Double.parseDouble(amtS);
        } catch (Exception ex) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        long ts = System.currentTimeMillis();
        Expense e = new Expense(ts, date, type, category, amount, note);
        db.insert(e);
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
