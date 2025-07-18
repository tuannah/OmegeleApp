package com.example.omegeleapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import com.example.omegeleapp.R;
import com.example.omegeleapp.models.Country;

/**
 * CountryAdapter - Adapter tùy chỉnh cho Spinner hiển thị danh sách quốc gia
 * 
 * Chức năng:
 * - Hiển thị danh sách quốc gia với hình ảnh cờ và tên quốc gia
 * - Sử dụng ViewHolder pattern để tối ưu hiệu suất
 * - Tùy chỉnh giao diện cho cả view chính và dropdown view
 * - Kế thừa từ ArrayAdapter để tương thích với Spinner
 */
public class CountryAdapter extends ArrayAdapter<Country> {
    private final LayoutInflater inflater;    // Đối tượng để inflate layout XML
    private final List<Country> countryList;  // Danh sách các quốc gia

    /**
     * Constructor tạo adapter mới
     * @param context Context của activity
     * @param countries Danh sách các quốc gia cần hiển thị
     */
    public CountryAdapter(@NonNull Context context, @NonNull List<Country> countries) {
        super(context, 0, countries);
        this.inflater = LayoutInflater.from(context);
        this.countryList = countries;
    }

    /**
     * Tạo view cho item được chọn trong Spinner
     * @param position Vị trí của item trong danh sách
     * @param convertView View được tái sử dụng (có thể null)
     * @param parent ViewGroup chứa view này
     * @return View đã được tùy chỉnh
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return createCustomView(position, convertView, parent);
    }

    /**
     * Tạo view cho dropdown list của Spinner
     * @param position Vị trí của item trong danh sách
     * @param convertView View được tái sử dụng (có thể null)
     * @param parent ViewGroup chứa view này
     * @return View đã được tùy chỉnh
     */
    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return createCustomView(position, convertView, parent);
    }

    /**
     * Tạo view tùy chỉnh cho mỗi item trong danh sách
     * Sử dụng ViewHolder pattern để tối ưu hiệu suất
     * 
     * @param position Vị trí của item
     * @param convertView View được tái sử dụng
     * @param parent ViewGroup chứa view
     * @return View đã được tùy chỉnh với dữ liệu
     */
    private View createCustomView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // Kiểm tra xem có thể tái sử dụng view không
        if (convertView == null) {
            // Tạo view mới từ layout XML
            convertView = inflater.inflate(R.layout.item_country, parent, false);
            holder = new ViewHolder();
            // Ánh xạ các view con
            holder.imageFlag = convertView.findViewById(R.id.imageFlag);
            holder.textCountry = convertView.findViewById(R.id.textCountry);
            convertView.setTag(holder);  // Lưu holder vào tag để tái sử dụng
        } else {
            // Tái sử dụng view và lấy holder từ tag
            holder = (ViewHolder) convertView.getTag();
        }

        // Lấy dữ liệu quốc gia tại vị trí position
        Country country = countryList.get(position);
        // Gán dữ liệu vào các view
        holder.imageFlag.setImageResource(country.getFlagResId());  // Hiển thị cờ
        holder.textCountry.setText(country.getName());              // Hiển thị tên quốc gia

        return convertView;
    }

    /**
     * ViewHolder class để tối ưu hiệu suất
     * Lưu trữ reference đến các view con để tránh findViewById nhiều lần
     */
    static class ViewHolder {
        ImageView imageFlag;    // ImageView hiển thị cờ quốc gia
        TextView textCountry;   // TextView hiển thị tên quốc gia
    }
}
