package com.example.expensemanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    DatabaseHelper db;
    TextView tvMonth;
    int showYear, showMonth; // month is 1-12
    
    // SAF Launchers
    private ActivityResultLauncher<String> createDocumentLauncher;
    private ActivityResultLauncher<String[]> openDocumentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DatabaseHelper(this);

        // --- SET THE TOOLBAR ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // -----------------------

        initializeDefaultCategories();
        
        // --- Initialize SAF Launchers ---
        createDocumentLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("text/csv"), uri -> {
            if (uri != null) {
                try {
                    OutputStream out = getContentResolver().openOutputStream(uri);
                    if (out != null) {
                        db.exportCsv(out);
                        Toast.makeText(this, "Exported successfully", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        openDocumentLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                try {
                    InputStream in = getContentResolver().openInputStream(uri);
                    if (in != null) {
                        int count = db.importCsv(in);
                        Toast.makeText(this, "Imported " + count + " rows", Toast.LENGTH_LONG).show();
                        refresh();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });


        // --- Initialize Views ---
        tvMonth = findViewById(R.id.tvMonth);
        Button btnPrev = findViewById(R.id.btnPrev);
        Button btnNext = findViewById(R.id.btnNext);
        Button btnAdd = findViewById(R.id.btnAdd);
        
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        // --- Set Initial Date ---
        Calendar cal = Calendar.getInstance();
        showYear = cal.get(Calendar.YEAR);
        showMonth = cal.get(Calendar.MONTH) + 1; // Calendar month is 0-11, so we add 1

        // --- Setup ViewPager and Tabs ---
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Dashboard");
                            break;
                        case 1:
                            tab.setText("Transactions");
                            break;
                        case 2:
                            tab.setText("Charts");
                            break;
                    }
                }
        ).attach();

        // --- Set Click Listeners ---
        btnPrev.setOnClickListener(v -> changeMonth(-1));
        btnNext.setOnClickListener(v -> changeMonth(1));
        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AddEntryActivity.class)));
    }

    private void initializeDefaultCategories() {
        SharedPreferences sharedPreferences = getSharedPreferences("CategoryPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!sharedPreferences.contains("expense_categories")) {
            Set<String> expenseCategories = new HashSet<>(Arrays.asList("Food", "Transport", "Bills", "Shopping", "Entertainment"));
            editor.putStringSet("expense_categories", expenseCategories);
        }
        if (!sharedPreferences.contains("income_categories")) {
            Set<String> incomeCategories = new HashSet<>(Arrays.asList("Salary", "Gift", "Bonus", "Freelance"));
            editor.putStringSet("income_categories", incomeCategories);
        }
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_manage_categories) {
            startActivity(new Intent(this, CategoryManagerActivity.class));
            return true;
        } else if (itemId == R.id.action_export_csv) {
            doExport();
            return true;
        } else if (itemId == R.id.action_import_csv) {
            doImport();
            return true;
        } else if (itemId == R.id.action_privacy_policy) {
            startActivity(new Intent(this, PrivacyPolicyActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeMonth(int delta) {
        showMonth += delta;
        while (showMonth < 1) {
            showMonth += 12;
            showYear -= 1;
        }
        while (showMonth > 12) {
            showMonth -= 12;
            showYear += 1;
        }
        refresh();
    }

    private void refresh() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, showYear);
        cal.set(Calendar.MONTH, showMonth - 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-yyyy", Locale.US);
        tvMonth.setText(dateFormat.format(cal.getTime()).toUpperCase());

        // Notify fragments about the update
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof DashboardFragment) {
                ((DashboardFragment) fragment).updateData(showYear, showMonth);
            } else if (fragment instanceof TransactionsFragment) {
                ((TransactionsFragment) fragment).updateData(showYear, showMonth);
            } else if (fragment instanceof ChartsFragment) {
                ((ChartsFragment) fragment).updateData(showYear, showMonth);
            }
        }
    }

    private void doExport() {
        String fileName = "expenses_export_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new java.util.Date()) + ".csv";
        createDocumentLauncher.launch(fileName);
    }

    private void doImport() {
        openDocumentLauncher.launch(new String[]{"text/csv", "text/comma-separated-values", "application/csv", "text/plain"});
    }

    public int getShowYear() {
        return showYear;
    }

    public int getShowMonth() {
        return showMonth;
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new DashboardFragment();
                case 1:
                    return new TransactionsFragment();
                case 2:
                    return new ChartsFragment();
                default:
                    return new DashboardFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
