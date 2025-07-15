package com.fu.linhtk.omegle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class CountryAdapter extends ArrayAdapter<Country> {
    private final LayoutInflater inflater;
    private final List<Country> countryList;

    public CountryAdapter(@NonNull Context context, @NonNull List<Country> countries) {
        super(context, 0, countries);
        this.inflater = LayoutInflater.from(context);
        this.countryList = countries;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return createCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return createCustomView(position, convertView, parent);
    }

    private View createCustomView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_country, parent, false);
            holder = new ViewHolder();
            holder.imageFlag = convertView.findViewById(R.id.imageFlag);
            holder.textCountry = convertView.findViewById(R.id.textCountry);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Country country = countryList.get(position);
        holder.imageFlag.setImageResource(country.getFlagResId());
        holder.textCountry.setText(country.getName());

        return convertView;
    }

    static class ViewHolder {
        ImageView imageFlag;
        TextView textCountry;
    }
}

