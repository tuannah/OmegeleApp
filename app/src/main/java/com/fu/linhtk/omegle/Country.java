package com.fu.linhtk.omegle;

public class Country {
    String name;
    int flagResId;

    public Country(String name, int flagResId) {
        this.name = name;
        this.flagResId = flagResId;
    }

    public String getName() {
        return name;
    }

    public int getFlagResId() {
        return flagResId;
    }
}

