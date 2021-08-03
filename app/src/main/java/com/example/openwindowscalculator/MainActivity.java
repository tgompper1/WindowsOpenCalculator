package com.example.openwindowscalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/*import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.NoSuchElementException;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsonable;
import com.github.cliftonlabs.json_simple.Jsoner;*/

/*public class Singleton {

    private static Singleton singleton = new Singleton( );

    *//* A private Constructor prevents any other
     * class from instantiating.
     *//*
    private Singleton() { }

    *//* Static 'instance' method *//*
    public static Singleton getInstance( ) {
        return singleton;
    }

    *//* Other methods protected by singleton-ness *//*
    protected static void demoMethod( ) {
        System.out.println("demoMethod for singleton");
    }
}*/

public class MainActivity extends AppCompatActivity {

    TextView waterDensity, windowOpen, textViewIndoorTemp, textViewOutdoorTemp, textViewIndoorHumidity, textViewOutdoorHumidity;
    EditText editTextIndoorTemp, editTextOutdoorTemp, editTextIndoorHumidity, editTextOutdoorHumidity;
    Button calculate, reset;
    String waterDensityVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        calculate = (Button)findViewById(R.id.calculate);
        reset = (Button)findViewById(R.id.reset);
        waterDensity = (TextView)findViewById(R.id.waterDensityField);
        windowOpen = (TextView)findViewById(R.id.windowOpenField);

        //String indoorTemp, indoorHumidity, outdoorTemp, outdoorHumidity; //temp c\
        calculate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                double indoorTemp, indoorHumidity, outdoorTemp, outdoorHumidity; //temp c\
                editTextIndoorTemp = (EditText)findViewById(R.id.editTextIndoorTemp);
                editTextIndoorHumidity = (EditText)findViewById(R.id.editTextIndoorHumidity);
                editTextOutdoorTemp = (EditText)findViewById(R.id.editTextOutdoorTemp);
                editTextOutdoorHumidity = (EditText)findViewById(R.id.editTextOutdoorHumidity);

                indoorTemp = Double.parseDouble(editTextIndoorTemp.getText().toString());
                indoorHumidity = Double.parseDouble(editTextIndoorHumidity.getText().toString());
                outdoorTemp = Double.parseDouble(editTextOutdoorTemp.getText().toString());
                outdoorHumidity = Double.parseDouble(editTextOutdoorHumidity.getText().toString());

                boolean windowOpenYN = isWindowOpen(indoorTemp, indoorHumidity, outdoorTemp, outdoorHumidity);

                if(windowOpenYN == true){
                    windowOpen.setText("You should open the windows");
                }
                else{
                    windowOpen.setText("You should turn on the AC");
                }

            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
        //indoorTemp=26;
        //indoorHumidity=55;
        //outdoorTemp = 24;
        //outdoorHumidity = 62;

        /*boolean windowOpenYN = isWindowOpen(indoorTemp, indoorHumidity, outdoorTemp, outdoorHumidity);

        if(windowOpenYN == true){
            windowOpen.setText("You should open the windows");
        }
        else{
            windowOpen.setText("You should turn on the AC");
        }*/
    }

    public boolean isWindowOpen(double iT, double iH, double oT, double oH){

        double indoorAirDensity, indoorWaterDensity, outdoorAirDensity, outdoorWaterDensity; //from table
        double indoorAirEnergy, indoorWaterEnergy, outdoorAirEnergy, outdoorWaterEnergy;
        double indoorTotal, outdoorTotal;
        double differenceIO;
        //double[][] waterDensityTable = new double[76][101]; //from -25 C to 50 C , humidity 0-100%
        //oH =oH/100;

        int iTCelsius = (int)(iT-32)*(5/9);
        int oTCelsius = (int)(oT-32)*(5/9);
        //getWaterDensity(45,24); //temp in celsius
        indoorWaterDensity = (getWaterDensity((int)iH, iTCelsius))*0.001;
        outdoorWaterDensity = (getWaterDensity((int)oH,oTCelsius))*0.001;

        indoorAirDensity = (101.325/287.058)/(iT + 273.15);
        outdoorAirDensity = (101.325/287.058)/(oT + 273.15);

        indoorWaterEnergy = indoorWaterDensity*1.864*(iT-10) + (indoorWaterDensity*2257);
        outdoorWaterEnergy = outdoorWaterDensity*1.864*(oT-10) + (outdoorWaterDensity*2257);

        indoorAirEnergy = indoorAirDensity*1.005*(iT-10);
        outdoorAirEnergy = outdoorAirDensity*1.005*(oT-10);

        indoorTotal = indoorAirEnergy + indoorWaterEnergy;
        outdoorTotal = outdoorAirEnergy + outdoorWaterEnergy;

        differenceIO = indoorTotal-outdoorTotal;

        if(differenceIO > 0 ){
            return true;
        }
        else
            return false;

    }



    public Double getWaterDensity(int Rh, int temp){
        int entryNum = (Rh+20 )+(101*(temp+25));
        waterDensity.setText("1");
        final double[] density = new double[1];
        String url = "https://spreadsheets.google.com/feeds/cells/1WbllKmxh9AyVMCu2llJxCwchsxSzeH-tcSWoEdsF_vQ/1/public/full?alt=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        waterDensity.setText("hello");

                        try{
                            waterDensity.setText(response.get("version").toString());
                            JSONObject feed = response.getJSONObject("feed");
                            JSONArray array = feed.getJSONArray("entry");
                            JSONObject entry = array.getJSONObject(entryNum);
                            //JSONObject cell = entry.get
                            waterDensity.setText(entry.getJSONObject("gs$cell").get("$t").toString());
                            density[0] = Double.parseDouble(entry.getJSONObject("gs$cell").get("$t").toString());

                        }
                        catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        waterDensity.setText("uhoh");
                        error.printStackTrace();
                        //TODO handle error
                    }
                });
            //Singleton mySingleton = new Singleton();
            Singleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
            //waterDensity.setText("9");
        return density[0];
    }


    /*public String getWaterDensity(int RH, int temp){
        String url = "https://spreadsheets.google.com/feeds/cells/1WbllKmxh9AyVMCu2llJxCwchsxSzeH-tcSWoEdsF_vQ/1/public/full?alt=json";
        waterDensity.setText("1");
        //(rh+1)+(temp*(rh+1)
        waterDensity = (TextView)findViewById(R.id.waterDensityField);
        waterDensity.setText("2");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    waterDensity.setText("3");
                    try {
                        JSONArray array = response.getJSONArray("entry");
                        JSONObject object = array.getJSONObject((RH+1)+(temp*(RH+1)));
                        JSONObject content = object.getJSONObject("content");
                        String density = content.getString("$t");
                        waterDensityVal = density;
                        waterDensity.setText(density);
                    }
                    catch(JSONException e){
                        e.printStackTrace();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        waterDensity.setText("4");
                        // TODO: Handle error
                    }
                });
        waterDensity.setText("5");
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
       // temp -= 26;
            return waterDensityVal;
    }*/


}