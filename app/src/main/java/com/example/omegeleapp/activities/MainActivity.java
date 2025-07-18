package com.example.omegeleapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.omegeleapp.R;

import java.util.ArrayList;
import java.util.List;

import com.example.omegeleapp.models.Country;
import com.example.omegeleapp.adapters.CountryAdapter;
import com.example.omegeleapp.activities.VoiceCallWaitingActivity;
import com.example.omegeleapp.utils.LogoutManager;

/**
 * MainActivity - Màn hình chính của ứng dụng Omegle
 * 
 * Chức năng chính:
 * - Hiển thị form nhập thông tin người dùng (tên, tuổi, sở thích, quốc tịch)
 * - Lưu trữ và tải lại thông tin người dùng từ SharedPreferences
 * - Chuyển hướng đến màn hình cuộc gọi thoại
 * - Validate dữ liệu đầu vào trước khi cho phép tiếp tục
 */
public class MainActivity extends AppCompatActivity {

    // UI components
    private EditText editTextName; // Ô nhập tên
    private EditText editTextAge; // Ô nhập tuổi
    private Spinner spinnerNationality; // Dropdown chọn quốc tịch
    private Button btnStartVoiceCall; // Nút bắt đầu cuộc gọi thoại
    private Button btnLogout; // Nút đăng xuất

    // Data
    private List<Country> countryList; // Danh sách quốc gia
    private CountryAdapter countryAdapter; // Adapter cho spinner quốc gia

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ẩn action bar để có giao diện fullscreen
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        // Khởi tạo UI components
        initializeViews();

        // Khởi tạo danh sách quốc gia
        initializeCountryList();

        // Tải lại thông tin người dùng đã lưu
        loadUserData();

        // Thiết lập event listeners
        setupEventListeners();
    }

    /**
     * Khởi tạo các view components
     * Ánh xạ các view từ layout XML
     */
    private void initializeViews() {
        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        spinnerNationality = findViewById(R.id.spinnerNationality);
        btnStartVoiceCall = findViewById(R.id.btnStartVoiceCall);
        btnLogout = findViewById(R.id.btnLogout);
    }

    /**
     * Khởi tạo danh sách quốc gia và adapter
     * Tạo danh sách các quốc gia phổ biến cho dropdown
     */
    private void initializeCountryList() {
        countryList = new ArrayList<>();

        // Thêm các quốc gia phổ biến
        countryList.add(new Country("Việt Nam", R.drawable.flag_vietnam));
        countryList.add(new Country("Hoa Kỳ", R.drawable.flag_usa));

        // Tạo adapter và gán cho spinner
        countryAdapter = new CountryAdapter(this, countryList);
        spinnerNationality.setAdapter(countryAdapter);
    }

    /**
     * Thiết lập các event listener cho các button
     */
    private void setupEventListeners() {
        // Event listener cho nút bắt đầu cuộc gọi thoại
        btnStartVoiceCall.setOnClickListener(v -> {
            if (validateInput()) {
                saveUserData();
                startVoiceCall();
            }
        });

        // Event listener cho nút đăng xuất
        btnLogout.setOnClickListener(v -> LogoutManager.showLogoutDialog(this, () -> {
            // Quay về LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }));
    }

    /**
     * Validate dữ liệu đầu vào
     * Kiểm tra tên và tuổi có hợp lệ không
     * 
     * @return true nếu dữ liệu hợp lệ, false nếu không
     */
    private boolean validateInput() {
        String name = editTextName.getText().toString().trim();
        String age = editTextAge.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên của bạn", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (age.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tuổi của bạn", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int ageValue = Integer.parseInt(age);
            if (ageValue < 13 || ageValue > 100) {
                Toast.makeText(this, "Tuổi phải từ 13 đến 100", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Tuổi phải là số", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Lưu thông tin người dùng vào SharedPreferences
     * Lưu tên, tuổi và quốc tịch để sử dụng sau này
     */
    private void saveUserData() {
        String name = editTextName.getText().toString().trim();
        String age = editTextAge.getText().toString().trim();
        Country selectedCountry = (Country) spinnerNationality.getSelectedItem();
        String nationality = selectedCountry.getName();

        // Sử dụng SharedPreferences để lưu trữ dữ liệu
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("name", name);
        editor.putString("age", age);
        editor.putString("country", nationality);
        editor.apply(); // Lưu dữ liệu
    }

    /**
     * Tải lại thông tin người dùng từ SharedPreferences
     * Hiển thị dữ liệu đã lưu trước đó vào các ô nhập liệu
     */
    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String name = prefs.getString("name", "");
        String age = prefs.getString("age", "");

        // Điền dữ liệu vào các ô nhập liệu
        editTextName.setText(name);
        editTextAge.setText(age);
    }

    /**
     * Chuyển đến màn hình chờ cuộc gọi thoại
     */
    private void startVoiceCall() {
        Intent intent = new Intent(this, VoiceCallWaitingActivity.class);
        startActivity(intent);
    }
}
