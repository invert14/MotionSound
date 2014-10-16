package pl.gda.pg.eti.motion.sound;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import pl.gda.pg.eti.motion.sound.soundgenerator.AudioDevice;

public class MainActivity extends ActionBarActivity implements SensorEventListener {

    private float lastX, lastY, lastZ;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    private final float NOISE_THRESHOLD = 1;

    private TextView currentX, currentY, currentZ;

    private AudioDevice audioDevice;

    private float soundFrequency = 440;
    private Thread soundThread;
    private boolean shouldSound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        getSensor();
        audioDevice = new AudioDevice();
    }

    private void initializeViews() {
        currentX = (TextView) findViewById(R.id.currentX);
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener((SensorEventListener) this);
        shouldSound = false;
    }

    private void getSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Toast failToast = Toast.makeText(getApplicationContext(), "Accelerometer not found :(", 2000);
            failToast.show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void toggleSound(View view) throws InterruptedException {

        if (!shouldSound) {
            shouldSound = true;
            Toast toast = Toast.makeText(getApplicationContext(), "You should hear a sound", 1500);
            toast.show();

            //SoundGenerator...

            soundThread =  new Thread(new Runnable() {
                @Override
                public void run() {

                    while(shouldSound) {
                        float increment = (float) (2 * Math.PI) * soundFrequency / audioDevice.getSampleRate(); // angular increment for each sample
                        float angle = 0;
                        float samples[] = new float[audioDevice.getBufferSize()];

                        for (int i = 0; i < samples.length; i++) {
                            samples[i] = (float) Math.sin(angle);
                            angle += increment;
                        }

                        audioDevice.writeSamples(samples);
                    }
                }
            });
            soundThread.start();

        }
        else {
            shouldSound = false;
        }



    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        cleanValues();
        updateCurrentValues();

        soundFrequency += (int)(lastX * 10);

        deltaX = Math.abs(lastX - sensorEvent.values[0]);
        deltaY = Math.abs(lastY - sensorEvent.values[1]);
        deltaZ = Math.abs(lastZ - sensorEvent.values[2]);

        if (deltaX < NOISE_THRESHOLD)
            deltaX = 0;
        if (deltaY < NOISE_THRESHOLD)
            deltaY = 0;
        if (deltaZ < NOISE_THRESHOLD)
            deltaZ = 0;

        lastX = sensorEvent.values[0];
        lastY = sensorEvent.values[1];
        lastZ = sensorEvent.values[2];
    }

    private void updateCurrentValues() {
        currentX.setText(Float.toString(deltaX));
        currentY.setText(Float.toString(deltaY));
        currentZ.setText(Float.toString(deltaZ));
    }

    private void cleanValues() {
        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
