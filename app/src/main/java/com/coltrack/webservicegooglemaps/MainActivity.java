package com.coltrack.webservicegooglemaps;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView textView_resultado;
    String LOGTAG="Debug";
    String devuelve;
    Location location;
    LocationManager locationManager;
    LocationListener locationListener;
    AlertDialog alert = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView_resultado=(TextView)findViewById(R.id.textView_resultado);
        ////////////////////////////////////////////////////////////////////////////////
        //Gestion de GPS
        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
        //Revisamos si esta encendido el GPS
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            AlertNoGPS();
        }
        ////////////////////////////////////////////////////////////////////////////////
//        List<String> listaProvedores=locationManager.getAllProviders();
//        for (int i=0;i<listaProvedores.size();i++){
//            Log.i(LOGTAG,"Lista Provedores: "+i+":"+listaProvedores.get(i));
//        }
        //Para version posterior al API23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),"Debes autorizar los permisos de GPS...",Toast.LENGTH_SHORT).show();
                return;
            } else {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } else {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        ////////////////////////////////////////////////////////////////////////////////

        if (location!=null){
            Log.i(LOGTAG,"Mostrando ubicacion inicial...");
            mostrarLocalizacion(location);
        }
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mostrarLocalizacion(location);
                GpsStatus gpsStatus = locationManager.getGpsStatus(null);
                if(gpsStatus != null) {
                    Iterable<GpsSatellite>satellites = gpsStatus.getSatellites();
                    Iterator<GpsSatellite> sat = satellites.iterator();
                    String lSatellites = null;
                    int i = 0;
                    while (sat.hasNext()) {
                        GpsSatellite satellite = sat.next();
                        lSatellites = "Satellite" + (i++) + ": "
                                + satellite.getPrn() + ","
                                + satellite.usedInFix() + ","
                                + satellite.getSnr() + ","
                                + satellite.getAzimuth() + ","
                                + satellite.getElevation()+ "\n\n";

                        Log.i(LOGTAG, "nsatelites" + lSatellites);
                    }
                }else{
                    Log.i(LOGTAG,"GpsStatus null");
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

    }

    private void AlertNoGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Servicio de ubicacion desactivada, ¿Desea activarla?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        alert = builder.create();
        alert.show();
    }

    public void mostrarLocalizacion(Location loc) {
        if (loc!=null){
            ObtenerWebServiceGoogleMaps hiloconexion=new ObtenerWebServiceGoogleMaps();
            hiloconexion.execute(String.valueOf(loc.getLatitude()),String.valueOf(loc.getLongitude()));
        }


    }

    public class ObtenerWebServiceGoogleMaps extends AsyncTask<String,Integer,String>{
        @Override
        protected String doInBackground(String... params) {
            //https://maps.googleapis.com/maps/api/geocode/json?latlng=
            String cadena="http://maps.googleapis.com/maps/api/geocode/json?latlng=";
            //cadena=cadena+params[0]+","+params[1]+"&key=AIzaSyCSzohGT6qwwNt5L-KvzGlk26AQgnnrd_U";
            cadena=cadena+params[0]+","+params[1]+"&sensor=none";
            Log.i(LOGTAG,"Cadena: "+cadena);


            try {
                URL url = new URL(cadena);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //Abrir la conexión
                connection.setRequestProperty("User-Agent", "Mozilla/5.0" +
                        " (Linux; Android 1.5; es-ES) Ejemplo HTTP");
                //connection.setHeader("content-type", "application/json");

                int respuesta = connection.getResponseCode();
                StringBuilder result = new StringBuilder();

                if (respuesta == HttpURLConnection.HTTP_OK){


                    InputStream in = new BufferedInputStream(connection.getInputStream());  // preparo la cadena de entrada

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));  // la introduzco en un BufferedReader

                    // El siguiente proceso lo hago porque el JSONOBject necesita un String y tengo
                    // que tranformar el BufferedReader a String. Esto lo hago a traves de un
                    // StringBuilder.

                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);        // Paso toda la entrada al StringBuilder
                    }

                    //Creamos un objeto JSONObject para poder acceder a los atributos (campos) del objeto.
                    JSONObject respuestaJSON = new JSONObject(result.toString());   //Creo un JSONObject a partir del StringBuilder pasado a cadena
                    //Accedemos al vector de resultados
                    JSONArray resultJSON = respuestaJSON.getJSONArray("results");   // results es el nombre del campo en el JSON

                    //Vamos obteniendo todos los campos que nos interesen.
                    //En este caso obtenemos la primera dirección de los resultados.
                    String direccion="SIN DATOS PARA ESA LONGITUD Y LATITUD";
                    if (resultJSON.length()>0){
                        direccion = resultJSON.getJSONObject(0).getString("formatted_address");    // dentro del results pasamos a Objeto la seccion formated_address
                    }
                    devuelve = "Dirección: " + direccion;   // variable de salida que mandaré al onPostExecute para que actualice la UI
                    Log.i(LOGTAG,"Respuesta Direccion: "+direccion);
                }



            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return devuelve;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String aVoid) {
            textView_resultado.setText(aVoid);
            Log.i(LOGTAG,"GPS:"+aVoid);
            //super.onPostExecute(aVoid);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            } else {
                locationManager.removeUpdates(locationListener);
            }
        } else {
            locationManager.removeUpdates(locationListener);
        }


    }
    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }



    }
}
