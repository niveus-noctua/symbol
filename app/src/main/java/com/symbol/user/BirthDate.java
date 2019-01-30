package com.symbol.user;

import java.time.LocalDate;
import java.time.Period;

public class BirthDate {
    private int day;
    private int month;
    private int year;
    private int age;

    public BirthDate(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    private int calculateAge() {
        LocalDate birthDate = LocalDate.of(year, month, day);
        LocalDate currentDate = LocalDate.now();
        age = Period.between(birthDate, currentDate).getYears();
        return age;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getAge() {
        return calculateAge();
    }
}
