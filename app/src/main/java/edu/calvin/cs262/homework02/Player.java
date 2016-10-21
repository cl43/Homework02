package edu.calvin.cs262.homework02;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * Weather forecast object (POJO), one per day, based on openweathermap's RESTful API.
 * Based on Deitel's WeatherViewer app (chapter 17).
 *
 * @author deitel
 * @author kvlinden
 * @version spring, 2017
 */
public class Player {

    private int id;
    private String name, email;

    public Player(int id,String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    public String getID() { return ""+id; }//Accessors for player info
    public String getEmail() {
        return email;
    }
    public String getName() { return name; }

}
