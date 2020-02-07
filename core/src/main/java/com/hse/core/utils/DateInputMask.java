package com.hse.core.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;

import com.hse.core.R;

import java.util.Calendar;

import static com.hse.core.common.ExtKt.color;

public class DateInputMask implements TextWatcher {

    private CharSequence current = "";
    private String ddmmyyyy = "ддммгггг";
    private Calendar cal = Calendar.getInstance();
    private EditText input;

    public DateInputMask(EditText input) {
        this.input = input;
        this.input.addTextChangedListener(this);
        this.input.setText(" ");
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().equals(current)) {
            return;
        }

        String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
        String cleanC = current.toString().replaceAll("[^\\d.]|\\.", "");

        int cl = clean.length();
        int sel = cl;
        for (int i = 2; i <= cl && i < 6; i += 2) {
            sel++;
        }
        if (clean.equals(cleanC)) sel--;

        if (clean.length() < 8) {
            clean = clean + ddmmyyyy.substring(clean.length());
        } else {

            int day = Integer.parseInt(clean.substring(0, 2));
            int mon = Integer.parseInt(clean.substring(2, 4));
            int year = Integer.parseInt(clean.substring(4, 8));

            mon = mon < 1 ? 1 : mon > 12 ? 12 : mon;
            cal.set(Calendar.MONTH, mon - 1);
            year = (year < 2019) ? 2019 : (year > 2040) ? 2040 : year;
            cal.set(Calendar.YEAR, year);

            day = (day > cal.getActualMaximum(Calendar.DATE)) ? cal.getActualMaximum(Calendar.DATE) : day;
            clean = String.format("%02d%02d%02d", day, mon, year);
        }

        clean = String.format("%s.%s.%s",
                clean.substring(0, 2),
                clean.substring(2, 4),
                clean.substring(4, 8));

        sel = sel < 0 ? 0 : sel;
        sel = sel < clean.length() ? sel : clean.length();
        current = clean;
        input.setText(current);
        input.getText().setSpan(new ForegroundColorSpan(color(R.color.textHint)), sel, input.getText().length(), 0);
        input.setSelection(sel);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}