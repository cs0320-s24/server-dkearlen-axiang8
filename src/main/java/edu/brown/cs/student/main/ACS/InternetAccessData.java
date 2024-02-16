package edu.brown.cs.student.main.ACS;

import java.time.LocalDate;
import java.time.LocalTime;

public record InternetAccessData(
    String state, String county, LocalDate date, LocalTime time, String percentage) {


}
