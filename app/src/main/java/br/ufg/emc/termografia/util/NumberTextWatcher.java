package br.ufg.emc.termografia.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import java.text.DecimalFormatSymbols;

public class NumberTextWatcher implements TextWatcher {
    private boolean isRunning = false;
    private final int decimalPlaces;

    public NumberTextWatcher(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable editable) {
        if (isRunning) return;

        isRunning = true;
        editable.replace(0, editable.length(), format(editable.toString()));
        isRunning = false;
    }

    public String format(String input) {
        return NumberTextWatcher.format(input, decimalPlaces);
    }

    public static String format(String input, int decimalPlaces) {
        String digits = input.replaceAll("[\\D]", "");
        while (digits.startsWith("0")) digits = digits.substring(1);

        StringBuilder builder = new StringBuilder(digits);
        while (builder.length() < decimalPlaces + 1) builder.insert(0,"0");
        digits = builder.toString();

        int length = digits.length();
        return digits.substring(0, length - decimalPlaces)  + '.' + digits.substring(length - decimalPlaces);
    }
}
