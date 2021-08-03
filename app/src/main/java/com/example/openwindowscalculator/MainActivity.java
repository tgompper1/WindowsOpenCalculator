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


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    TextView waterDensity, windowOpen; //TODO get rid of waterDensity field once consistent
    EditText editTextIndoorTemp, editTextOutdoorTemp, editTextIndoorHumidity, editTextOutdoorHumidity;
    Button calculate, reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calculate = (Button) findViewById(R.id.calculate);
        reset = (Button) findViewById(R.id.reset);
        waterDensity = (TextView) findViewById(R.id.waterDensityField);
        windowOpen = (TextView) findViewById(R.id.windowOpenField);

        calculate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                double indoorTemp, indoorHumidity, outdoorTemp, outdoorHumidity; //temp given in F, humidities %
                editTextIndoorTemp = (EditText) findViewById(R.id.editTextIndoorTemp);
                editTextIndoorHumidity = (EditText) findViewById(R.id.editTextIndoorHumidity);
                editTextOutdoorTemp = (EditText) findViewById(R.id.editTextOutdoorTemp);
                editTextOutdoorHumidity = (EditText) findViewById(R.id.editTextOutdoorHumidity);

                /* set temps and humidities from user inputed values*/
                indoorTemp = Double.parseDouble(editTextIndoorTemp.getText().toString());
                indoorHumidity = Double.parseDouble(editTextIndoorHumidity.getText().toString());
                outdoorTemp = Double.parseDouble(editTextOutdoorTemp.getText().toString());
                outdoorHumidity = Double.parseDouble(editTextOutdoorHumidity.getText().toString());

                boolean windowOpenYN = isWindowOpen(indoorTemp, indoorHumidity, outdoorTemp, outdoorHumidity);

                if (windowOpenYN == true) {
                    windowOpen.setText("You should open the windows");
                } else {
                    windowOpen.setText("You should turn on the AC");
                }

            }
        });

        /* reset button*/
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
    }

    public boolean isWindowOpen(double iT, double iH, double oT, double oH) {

        double indoorAirDensity, indoorWaterDensity, outdoorAirDensity, outdoorWaterDensity; //water densities from table, air densities calculated
        double indoorAirEnergy, indoorWaterEnergy, outdoorAirEnergy, outdoorWaterEnergy;
        double indoorTotal, outdoorTotal;
        double differenceIO;

        /*convert given F temps to Celsius*/
        int iTCelsius = (int) (iT - 32) * (5 / 9);
        int oTCelsius = (int) (oT - 32) * (5 / 9);

        /*grab water densities from json data*/
        indoorWaterDensity = (getWaterDensity((int) iH, iTCelsius)) * 0.001;
        outdoorWaterDensity = (getWaterDensity((int) oH, oTCelsius)) * 0.001;

        /*calculate air densities*/
        indoorAirDensity = (101.325 / 287.058) / (iT + 273.15);
        outdoorAirDensity = (101.325 / 287.058) / (oT + 273.15);

        /*calculate water energies*/
        indoorWaterEnergy = indoorWaterDensity * 1.864 * (iT - 10) + (indoorWaterDensity * 2257);
        outdoorWaterEnergy = outdoorWaterDensity * 1.864 * (oT - 10) + (outdoorWaterDensity * 2257);

        /*calculate air energies*/
        indoorAirEnergy = indoorAirDensity * 1.005 * (iT - 10);
        outdoorAirEnergy = outdoorAirDensity * 1.005 * (oT - 10);

        /*calculate total energies*/
        indoorTotal = indoorAirEnergy + indoorWaterEnergy;
        outdoorTotal = outdoorAirEnergy + outdoorWaterEnergy;

        /*calculate difference between indoor and outdoor energies*/
        differenceIO = indoorTotal - outdoorTotal;

        if (differenceIO > 0) {
            return true;
        } else
            return false;

    }

    public Double getWaterDensity(int Rh, int temp) {

        /*find correct data entry*/
        int entryNum = (Rh + 20) + (101 * (temp + 25));

        //waterDensity.setText("1");

        /*density to be returned*/
        final double[] density = new double[1];

        String url = "https://spreadsheets.google.com/feeds/cells/1WbllKmxh9AyVMCu2llJxCwchsxSzeH-tcSWoEdsF_vQ/1/public/full?alt=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //waterDensity.setText("hello");
                        try {
                            waterDensity.setText(response.get("version").toString());
                            JSONObject feed = response.getJSONObject("feed");
                            JSONArray array = feed.getJSONArray("entry");
                            JSONObject entry = array.getJSONObject(entryNum);
                            waterDensity.setText(entry.getJSONObject("gs$cell").get("$t").toString());
                            density[0] = Double.parseDouble(entry.getJSONObject("gs$cell").get("$t").toString());

                        } catch (JSONException e) {
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

        Singleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

        return density[0];
    }

}