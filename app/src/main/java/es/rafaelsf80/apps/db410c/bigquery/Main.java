package es.rafaelsf80.apps.db410c.bigquery;

import android.content.Context;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.cloud.AuthCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FormatOptions;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.WriteChannelConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Main extends AppCompatActivity implements SensorEventListener {

    // Credentials file: this file is stored in the assets/ directory. Replace it with yours.
    private final String CREDENTIALS_FILE = "doneval-cloud-d164a2981f94.json";
    private SensorManager sensorManager;
    TextView tvSensorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tvSensorList = (TextView) findViewById(R.id.tv_sensor_list);
        // Get all sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        for (Sensor sensor : sensors) {
            Log.d("Sensors", "" + sensor.getName());
        }

        // Register magnetic sensor
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        synchronized (this) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

                tvSensorList.setText("X: " + Float.toString(sensorEvent.values[0]) +
                        "Y: " + Float.toString(sensorEvent.values[1]) +
                        "Z: " + Float.toString(sensorEvent.values[2]));

                final String JSON = "{\"X\": " + Float.toString(sensorEvent.values[0]) +
                        ", \"Y\": " + Float.toString(sensorEvent.values[1]) +
                        ", \"Z\": " + Float.toString(sensorEvent.values[2]) + "}";

                // Call BigQuery Streaming API
                new BigQueryTask().execute(JSON);
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Ignoring this for now
    }

    private class BigQueryTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("Main", "Starting BigQuery API request ...");
        }

        @Override
        protected String doInBackground(String... params) {

//            String JSON_CONTENT_TEST =
//                    "{" +
//                            "\"X\": \"1.0\", " +
//                            "\"Y\": \"2.0\", " +
//                            "\"Z\": \"3.0\"" +
//                            "}";
            String JSON_CONTENT = params[0];
            try {
                AssetManager am = Main.this.getAssets();
                InputStream isCredentialsFile = am.open(CREDENTIALS_FILE);
                BigQuery bigquery = BigQueryOptions.builder()
                        .authCredentials(AuthCredentials.createForJson(isCredentialsFile))
                        .projectId("decent-envoy-503")
                        .build().service();

                Log.d("Main", "bigquery: " + bigquery.toString());
                TableId tableId = TableId.of("android_app", "test");
                Table table = bigquery.getTable(tableId);

                int num = 0;
                Log.d("Main", "Sending: " + JSON_CONTENT);
                WriteChannelConfiguration configuration = WriteChannelConfiguration.builder(tableId)
                        .formatOptions(FormatOptions.json())
                        .build();
                try (WriteChannel channel = bigquery.writer(configuration)) {
                    num = channel.write(ByteBuffer.wrap(JSON_CONTENT.getBytes(StandardCharsets.UTF_8)));
                    channel.close();
                } catch (IOException e) {
                    Log.d("Main", e.toString());
                }
                Log.d("Main", "Loading " + Integer.toString(num) + " bytes into table " + tableId);


            } catch (Exception e) {
                Log.d("Main", "Exception: " + e.toString());
            }
            return "Done";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String msg) {
            super.onPostExecute(msg);
            Log.d("Main", "onPostExecute: " + msg);

        }
    }
}
