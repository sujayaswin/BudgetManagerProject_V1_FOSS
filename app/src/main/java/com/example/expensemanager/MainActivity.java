package com.example.expensemanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;
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
        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (dir == null) dir = getExternalFilesDir(null);
            
            if (dir != null) {
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        Toast.makeText(this, "Could not create directory: " + dir.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                String fileName = "expenses_export_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new java.util.Date()) + ".csv";
                File out = new File(dir, fileName);
                db.exportCsv(out);
                Toast.makeText(this, "Exported to " + out.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Could not access storage.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, "Export failed: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void doImport() {
        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (dir == null) dir = getExternalFilesDir(null);
            
            if (dir != null) {
                File f = new File(dir, "import.csv");
                if (!f.exists()) {
                    Toast.makeText(this, "Place a file named import.csv in Downloads: " + dir.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    return;
                }
                int added = db.importCsv(f);
                Toast.makeText(this, "Imported " + added + " rows", Toast.LENGTH_LONG).show();
                refresh();
            } else {
                 Toast.makeText(this, "Could not access storage.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, "Import failed: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
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
