package com.example.omegeleapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.omegeleapp.R;

public class VoiceCallSettingsActivity extends AppCompatActivity {
    
    private RadioGroup radioGroupQuality, radioGroupGender;
    private EditText etMinAge, etMaxAge, etMaxDuration;
    private Switch switchStartMuted, switchStartSpeaker;
    private Button btnSaveSettings;
    private ImageButton btnBack;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call_settings);
        
        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        initializeViews();
        loadSettings();
        setupListeners();
    }
    
    private void initializeViews() {
        radioGroupQuality = findViewById(R.id.radioGroupQuality);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        etMinAge = findViewById(R.id.etMinAge);
        etMaxAge = findViewById(R.id.etMaxAge);
        etMaxDuration = findViewById(R.id.etMaxDuration);
        switchStartMuted = findViewById(R.id.switchStartMuted);
        switchStartSpeaker = findViewById(R.id.switchStartSpeaker);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        btnBack = findViewById(R.id.btnBack);
    }
    
    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("VoiceCallSettings", MODE_PRIVATE);
        
        // Load audio quality setting
        int quality = prefs.getInt("audio_quality", 1); // 0=low, 1=medium, 2=high
        switch (quality) {
            case 0:
                radioGroupQuality.check(R.id.rbLowQuality);
                break;
            case 1:
                radioGroupQuality.check(R.id.rbMediumQuality);
                break;
            case 2:
                radioGroupQuality.check(R.id.rbHighQuality);
                break;
        }
        
        // Load gender preference
        int gender = prefs.getInt("gender_preference", 0); // 0=any, 1=male, 2=female
        switch (gender) {
            case 0:
                radioGroupGender.check(R.id.rbAnyGender);
                break;
            case 1:
                radioGroupGender.check(R.id.rbMaleOnly);
                break;
            case 2:
                radioGroupGender.check(R.id.rbFemaleOnly);
                break;
        }
        
        // Load age range
        int minAge = prefs.getInt("min_age", 18);
        int maxAge = prefs.getInt("max_age", 65);
        etMinAge.setText(String.valueOf(minAge));
        etMaxAge.setText(String.valueOf(maxAge));
        
        // Load call duration
        int maxDuration = prefs.getInt("max_duration", 30);
        etMaxDuration.setText(String.valueOf(maxDuration));
        
        // Load call settings
        boolean startMuted = prefs.getBoolean("start_muted", false);
        boolean startSpeaker = prefs.getBoolean("start_speaker", false);
        switchStartMuted.setChecked(startMuted);
        switchStartSpeaker.setChecked(startSpeaker);
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }
    
    private void saveSettings() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }
        
        SharedPreferences prefs = getSharedPreferences("VoiceCallSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Save audio quality
        int quality = 1; // default to medium
        int checkedQualityId = radioGroupQuality.getCheckedRadioButtonId();
        if (checkedQualityId == R.id.rbLowQuality) {
            quality = 0;
        } else if (checkedQualityId == R.id.rbHighQuality) {
            quality = 2;
        }
        editor.putInt("audio_quality", quality);
        
        // Save gender preference
        int gender = 0; // default to any
        int checkedGenderId = radioGroupGender.getCheckedRadioButtonId();
        if (checkedGenderId == R.id.rbMaleOnly) {
            gender = 1;
        } else if (checkedGenderId == R.id.rbFemaleOnly) {
            gender = 2;
        }
        editor.putInt("gender_preference", gender);
        
        // Save age range
        int minAge = Integer.parseInt(etMinAge.getText().toString());
        int maxAge = Integer.parseInt(etMaxAge.getText().toString());
        editor.putInt("min_age", minAge);
        editor.putInt("max_age", maxAge);
        
        // Save call duration
        int maxDuration = Integer.parseInt(etMaxDuration.getText().toString());
        editor.putInt("max_duration", maxDuration);
        
        // Save call settings
        editor.putBoolean("start_muted", switchStartMuted.isChecked());
        editor.putBoolean("start_speaker", switchStartSpeaker.isChecked());
        
        // Apply changes
        editor.apply();
        
        Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    private boolean validateInputs() {
        // Validate age range
        try {
            int minAge = Integer.parseInt(etMinAge.getText().toString());
            int maxAge = Integer.parseInt(etMaxAge.getText().toString());
            
            if (minAge < 13 || minAge > 100) {
                Toast.makeText(this, "Minimum age must be between 13 and 100", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            if (maxAge < 13 || maxAge > 100) {
                Toast.makeText(this, "Maximum age must be between 13 and 100", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            if (minAge > maxAge) {
                Toast.makeText(this, "Minimum age cannot be greater than maximum age", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid age numbers", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Validate call duration
        try {
            int maxDuration = Integer.parseInt(etMaxDuration.getText().toString());
            if (maxDuration < 1 || maxDuration > 120) {
                Toast.makeText(this, "Call duration must be between 1 and 120 minutes", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid call duration", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
} 