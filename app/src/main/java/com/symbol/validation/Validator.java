package com.symbol.validation;

import android.util.Patterns;

import com.symbol.symbol.R;

import java.util.regex.Pattern;

public final class Validator {
    private Validator() {

    }

    public final static boolean validateEmail(String email) {
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true;
        } else {
            return false;
        }
    }

    public final static boolean validatePassword(String password) {
        Pattern pattern = Pattern.compile("[" + "a-zA-z" + "\\d" +"]" + "*");
        if (pattern.matcher(password).matches()) {
            return true;
        } else {
            return false;
        }
    }

    public final static boolean validateName(String name) {
        Pattern cyrillicPattern = Pattern.compile("[" + "a-zA-z" + "\\d" +"]" + "*");
        Pattern latinPattern = Pattern.compile("[" + "а-яА-я" + "ё" + "\\d" + "]" + "*");
        if (cyrillicPattern.matcher(name).matches() || latinPattern.matcher(name).matches()) {
            return true;
        } else {
            return false;
        }
    }
}
