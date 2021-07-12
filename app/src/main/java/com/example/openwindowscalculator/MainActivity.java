package com.example.openwindowscalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    TextView waterDensity, windowOpen;
    String waterDensityVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        waterDensity = (TextView)findViewById(R.id.waterDensityField);
        windowOpen = (TextView)findViewById(R.id.windowOpenField);

        double indoorTemp, indoorHumidity, outdoorTemp, outdoorHumidity; //temp c\

        indoorTemp=26;
        indoorHumidity=55;
        outdoorTemp = 24;
        outdoorHumidity = 62;

        boolean windowOpenYN = isWindowOpen(indoorTemp, indoorHumidity, outdoorTemp, outdoorHumidity);

        if(windowOpenYN == true){
            windowOpen.setText("You should open the windows");
        }
        else{
            windowOpen.setText("You should turn on the AC");
        }
    }

    public boolean isWindowOpen(double iT, double iH, double oT, double oH){

        double indoorAirDensity, indoorWaterDensity, outdoorAirDensity, outdoorWaterDensity; //from table
        double indoorAirEnergy, indoorWaterEnergy, outdoorAirEnergy, outdoorWaterEnergy;
        double indoorTotal, outdoorTotal;
        double differenceIO;
        //double[][] waterDensityTable = new double[76][101]; //from -25 C to 50 C , humidity 0-100%
        //oH =oH/100;

        getWaterDensity(55,26);
        indoorWaterDensity = 0.01265;//getWaterDensity(55, 26);
        outdoorWaterDensity = 0.01285;//getWaterDensity(62, 24);

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

    public String getWaterDensity(int RH, int temp){
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
                        // TODO: Handle error
                    }
                });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
       // temp -= 26;
            return waterDensityVal;
    }


}