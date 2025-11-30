package com.example.expensemanager;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        // Optional: Add a back button to the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Privacy Policy");
        }
    }

    // Handle the action bar back button press
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
    