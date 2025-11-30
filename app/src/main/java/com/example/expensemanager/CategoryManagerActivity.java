package com.example.expensemanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CategoryManagerActivity extends AppCompatActivity {

    // --- SharedPreferences Constants ---
    private static final String CATEGORY_PREFS = "CategoryPrefs";
    private static final String EXPENSE_CATEGORIES_KEY = "expense_categories";
    private static final String INCOME_CATEGORIES_KEY = "income_categories";

    // --- Expense UI and Data ---
    private EditText etNewExpenseCategory;
    private ListView lvExpenseCategories;
    private ArrayList<String> expenseCategoryList;
    private ArrayAdapter<String> expenseCategoryAdapter;

    // --- Income UI and Data ---
    private EditText etNewIncomeCategory;
    private ListView lvIncomeCategories;
    private ArrayList<String> incomeCategoryList;
    private ArrayAdapter<String> incomeCategoryAdapter;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manager);

        sharedPreferences = getSharedPreferences(CATEGORY_PREFS, Context.MODE_PRIVATE);

        // --- Setup Expense Views ---
        etNewExpenseCategory = findViewById(R.id.etNewExpenseCategory);
        lvExpenseCategories = findViewById(R.id.lvExpenseCategories);
        Button btnAddExpenseCategory = findViewById(R.id.btnAddExpenseCategory);

        // --- Setup Income Views ---
        etNewIncomeCategory = findViewById(R.id.etNewIncomeCategory);
        lvIncomeCategories = findViewById(R.id.lvIncomeCategories);
        Button btnAddIncomeCategory = findViewById(R.id.btnAddIncomeCategory);

        // --- Load data and setup adapters ---
        loadCategories();
        setupAdapters();

        // --- Set Listeners ---
        btnAddExpenseCategory.setOnClickListener(v -> addCategory(etNewExpenseCategory, expenseCategoryList, expenseCategoryAdapter, EXPENSE_CATEGORIES_KEY));
        btnAddIncomeCategory.setOnClickListener(v -> addCategory(etNewIncomeCategory, incomeCategoryList, incomeCategoryAdapter, INCOME_CATEGORIES_KEY));

        lvExpenseCategories.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteDialog(position, expenseCategoryList, expenseCategoryAdapter, EXPENSE_CATEGORIES_KEY);
            return true;
        });

        lvIncomeCategories.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteDialog(position, incomeCategoryList, incomeCategoryAdapter, INCOME_CATEGORIES_KEY);
            return true;
        });
    }

    private void loadCategories() {
        // Load expense categories
        Set<String> expenseSet = sharedPreferences.getStringSet(EXPENSE_CATEGORIES_KEY, null);
        if (expenseSet == null) {
            expenseCategoryList = new ArrayList<>(Arrays.asList("Food", "Transport", "Bills", "Shopping", "Entertainment"));
            saveCategoryList(expenseCategoryList, EXPENSE_CATEGORIES_KEY);
        } else {
            expenseCategoryList = new ArrayList<>(expenseSet);
        }

        // Load income categories
        Set<String> incomeSet = sharedPreferences.getStringSet(INCOME_CATEGORIES_KEY, null);
        if (incomeSet == null) {
            incomeCategoryList = new ArrayList<>(Arrays.asList("Salary", "Gift", "Bonus", "Freelance"));
            saveCategoryList(incomeCategoryList, INCOME_CATEGORIES_KEY);
        } else {
            incomeCategoryList = new ArrayList<>(incomeSet);
        }

        Collections.sort(expenseCategoryList);
        Collections.sort(incomeCategoryList);
    }

    private void setupAdapters() {
        // Use the custom layout R.layout.item_category for better visibility
        expenseCategoryAdapter = new ArrayAdapter<>(this, R.layout.item_category, expenseCategoryList);
        lvExpenseCategories.setAdapter(expenseCategoryAdapter);

        incomeCategoryAdapter = new ArrayAdapter<>(this, R.layout.item_category, incomeCategoryList);
        lvIncomeCategories.setAdapter(incomeCategoryAdapter);
    }

    private void saveCategoryList(ArrayList<String> categoryList, String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> categorySet = new HashSet<>(categoryList);
        editor.putStringSet(key, categorySet);
        editor.apply();
    }

    private void addCategory(EditText editText, ArrayList<String> categoryList, ArrayAdapter<String> adapter, String key) {
        String newCategory = editText.getText().toString().trim();
        if (newCategory.isEmpty()) {
            Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show();
        } else if (categoryList.stream().anyMatch(s -> s.equalsIgnoreCase(newCategory))) {
            Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show();
        } else {
            categoryList.add(newCategory);
            Collections.sort(categoryList);
            adapter.notifyDataSetChanged();
            saveCategoryList(categoryList, key);
            editText.setText("");
            Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteDialog(final int position, ArrayList<String> categoryList, ArrayAdapter<String> adapter, String key) {
        String categoryToDelete = categoryList.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '" + categoryToDelete + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    categoryList.remove(position);
                    adapter.notifyDataSetChanged();
                    saveCategoryList(categoryList, key);
                    Toast.makeText(CategoryManagerActivity.this, "Category deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
