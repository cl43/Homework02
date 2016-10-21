package edu.calvin.cs262.homework02;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Reads openweathermap's RESTful API for weather forecasts.
 * The code is based on Deitel's WeatherViewer (Chapter 17), simplified based on Murach's NewsReader (Chapter 10).
 * <p>
 * for CS 262, lab 6
 *
 * @author kvlinden
 * @version summer, 2016
 *
 * Chris Li, cl43, Lab06 answers
 * For cities that literally do not exist, it toasts "Failed to connect to service". However, the filters in this app do not seem to work. For example Tokyo, MI, unless the app includes restaurants and businesses as well.
 * The API key is a code that calls the API to identify the program. It keeps track of what the program does in order to prevent abuse.
 * The JSON response looks like what the output of the app is. It is a stream that makes grants easy access to read and write. For this app, it lists the days of the week, their weather, and the low and high temp.
 * After the system receives the JSON data via connection, it converts the information into an array to format it for display.
 * The Weather class handles the calendar dates of the app. It manages time-zone changes and what not.
 */
public class MainActivity extends AppCompatActivity {

    private EditText idText;
    private Button fetchButton;

    private List<Player> playerList = new ArrayList<>();
    private ListView itemsListView;

    /* This formater can be used as follows to format temperatures for display.
     *     numberFormat.format(SOME_DOUBLE_VALUE)
     */
    private NumberFormat numberFormat = NumberFormat.getInstance();

    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        idText = (EditText) findViewById(R.id.idText);
        fetchButton = (Button) findViewById(R.id.fetchButton);
        itemsListView = (ListView) findViewById(R.id.playerListView);

        // See comments on this formatter above.
        numberFormat.setMaximumFractionDigits(0);

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissKeyboard(idText);
                new GetPlayerTask().execute(createURL(idText.getText().toString()));
            }
        });
    }

    /**
     * Formats a URL for the webservice specified in the string resources.
     *
     * @param id the target city
     * @return URL formatted for openweathermap.com1
     */
    private URL createURL(String id) {
        try {
            String urlString;
            if(id.isEmpty()) {//if the input is empty then gives list, else appends id number to the url
                urlString = "http://cs262.cs.calvin.edu:8089/monopoly/players";
            }else{
                urlString = "http://cs262.cs.calvin.edu:8089/monopoly/player/"+id;
            }
            return new URL(urlString);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    /**
     * Deitel's method for programmatically dismissing the keyboard.
     *
     * @param view the TextView currently being edited
     */
    private void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Inner class for GETing the current player data from the calvin site
     */
    private class GetPlayerTask extends AsyncTask<URL, Void, JSONArray> {//change here

        @Override
        protected JSONArray doInBackground(URL... params) {
            HttpURLConnection connection = null;
            StringBuilder result = new StringBuilder();
            try {
                connection = (HttpURLConnection) params[0].openConnection();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    try {//tries to append result as a string into a JSON array.
                        return new JSONArray(result.toString());
                    }catch(JSONException e){
                        JSONArray array = new JSONArray();
                        array.put(new JSONObject(result.toString()));
                        return array;
                    }
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray player) {
            if (player != null) {
                //Log.d(TAG, player.toString());
                convertJSONtoArrayList(player);
                MainActivity.this.updateDisplay();
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Converts the JSON player data to an arraylist suitable for a listview adapter
     *
     * @param players
     */
    private void convertJSONtoArrayList(JSONArray players) {
        playerList.clear(); // clear old player data
        try {
            for (int i = 0; i < players.length(); i++) {
                JSONObject player = players.getJSONObject(i);
                playerList.add(new Player(
                        player.getInt("id"),
                        player.getString("emailaddress"),//grabs the id, string, and name from the JSON.
                        player.has("name") ? player.getString("name") : "no name"));//If no name is found, then names player "no name"
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refresh the weather data on the forecast ListView through a simple adapter
     */
    private void updateDisplay() {
        if (playerList == null) {
            Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
        }
        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        for (Player item : playerList) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("id", item.getID());
            map.put("name", item.getName());
            map.put("emailaddress", item.getEmail());//adds the id number, name, and email to map.
            data.add(map);
        }

        int resource = R.layout.player_item;
        String[] from = {"id", "name", "emailaddress"};//includes the info of the players into the display string
        int[] to = {R.id.IDTextView, R.id.nameTextView, R.id.emailTextView,};//Designates where the display shows up at the android screen

        SimpleAdapter adapter = new SimpleAdapter(this, data, resource, from, to);
        itemsListView.setAdapter(adapter);
    }

}
