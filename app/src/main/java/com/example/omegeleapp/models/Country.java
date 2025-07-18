package com.example.omegeleapp.models;

/**
 * Country - Lớp đại diện cho một quốc gia
 * 
 * Chức năng:
 * - Lưu trữ thông tin về tên quốc gia và ID của hình ảnh cờ
 * - Được sử dụng trong Spinner để hiển thị danh sách quốc gia cho người dùng chọn
 * - Cung cấp getter methods để truy cập thông tin quốc gia
 */
public class Country {
    private String name;        // Tên quốc gia (ví dụ: "Việt Nam", "Hoa Kỳ")
    private int flagResId;      // ID của hình ảnh cờ trong thư mục drawable

    /**
     * Constructor tạo một đối tượng Country mới
     * @param name Tên quốc gia
     * @param flagResId ID của hình ảnh cờ (R.drawable.flag_xxx)
     */
    public Country(String name, int flagResId) {
        this.name = name;
        this.flagResId = flagResId;
    }

    /**
     * Lấy tên quốc gia
     * @return Tên quốc gia dưới dạng String
     */
    public String getName() {
        return name;
    }

    /**
     * Lấy ID của hình ảnh cờ
     * @return ID của hình ảnh cờ trong thư mục drawable
     */
    public int getFlagResId() {
        return flagResId;
    }
}
