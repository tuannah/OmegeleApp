package com.example.omegeleapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText editTextName, editTextAge, editTextInterests;
    Spinner spinnerNationality;
    Button btnStartChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ view
        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        spinnerNationality = findViewById(R.id.spinnerNationality);
        btnStartChat = findViewById(R.id.btnStartChat);

        // Tạo danh sách quốc tịch + cờ
        List<Country> countries = new ArrayList<>();
        countries.add(new Country("America", R.drawable.flag_usa));
        countries.add(new Country("VietNam", R.drawable.flag_vietnam));

        // Thêm các cờ khác nếu cần

        // Gán adapter cho spinner
        CountryAdapter adapter = new CountryAdapter(this, countries);
        spinnerNationality.setAdapter(adapter);

        // Xử lý khi nhấn nút "Start Text Chat"
        btnStartChat.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String age = editTextAge.getText().toString().trim();
            String interests = editTextInterests.getText().toString().trim();
            Country selectedCountry = (Country) spinnerNationality.getSelectedItem();
            String nationality = selectedCountry.getName();

            // Ví dụ: Toast để kiểm tra dữ liệu nhập
            Toast.makeText(MainActivity.this,
                    "Tên: " + name +
                            "\nTuổi: " + age +
                            "\nQuốc tịch: " + nationality +
                            "\nSở thích: " + interests,
                    Toast.LENGTH_LONG).show();


        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

    }
}
