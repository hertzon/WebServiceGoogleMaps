package com.coltrack.webservicegooglemaps;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {
    TextView textView_latitud;
    TextView textView_longitud;
    Button button_resultado;
    TextView textView_resultado;
    String LOGTAG="Debug";
    String devuelve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView_latitud=(TextView)findViewById(R.id.editText_latitud);
        textView_longitud=(TextView)findViewById(R.id.editText_longitud);
        button_resultado=(Button)findViewById(R.id.button_resultado);
        textView_resultado=(TextView)findViewById(R.id.textView_resultado);

        button_resultado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObtenerWebService webService1=new ObtenerWebService();
                webService1.execute(textView_latitud.getText().toString(),textView_longitud.getText().toString());

            }
        });

    }
    public class ObtenerWebService extends AsyncTask<String,Integer,String>{
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
            //super.onPostExecute(aVoid);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}
