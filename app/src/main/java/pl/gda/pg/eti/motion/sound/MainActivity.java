package pl.gda.pg.eti.motion.sound;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import pl.gda.pg.eti.motion.sound.shakedetector.ShakeDectector;
import pl.gda.pg.eti.motion.sound.shakedetector.ShakeDetectActivityListener;
import pl.gda.pg.eti.motion.sound.soundgenerator.AudioDevice;

public class MainActivity extends ActionBarActivity implements SensorEventListener {

    private float lastX, lastY, lastZ;

    private ShakeDectector shakeDectector;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    private final float NOISE_THRESHOLD = 0.1f;

    private TextView currentX, currentY, currentZ, currentShaking;
    private Button soundButton;
    private SeekBar shakeSensitivity;

    private AudioDevice audioDevice;

    private float soundFrequency = 440;
    private float tiltSoundFrequency = 440;
    private boolean shakeSound = false;
    private boolean tiltSound = false;
    private boolean accSound = false;
    private boolean whipSound = false;
    private boolean laserSound = false;
    private float shakeCoeff;
    private float angle = 0;
    private float increment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        getSensor();
        audioDevice = new AudioDevice();
        increment = (float) (2 * Math.PI) * soundFrequency / audioDevice.getSampleRate();
        shakeDectector = new ShakeDectector(this);
        shakeDectector.addListener(new ShakeDetectActivityListener() {
            @Override
            public void shakeDetected() {
                shakeCoeff = shakeDectector.getLastShakeValue();
                updateShakingTextView(shakeCoeff);
            }
        });
    }

    private void updateShakingTextView(float shaking) {
        currentShaking.setText(Float.toString(shaking));
    }

    private void initializeViews() {
        currentX = (TextView) findViewById(R.id.currentX);
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);
        currentShaking = (TextView) findViewById(R.id.currentShaking);
        soundButton = (Button) findViewById(R.id.soundButton);
        shakeSensitivity = (SeekBar) findViewById(R.id.shakeSensitivitySlider);
        shakeSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "" + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
                shakeDectector.setSensitivity(seekBar.getProgress());
            }
        });
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        shakeDectector.onResume();
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        shakeDectector.onPause();
        shakeSound = false;
        tiltSound = false;
        accSound = false;
    }

    private void getSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
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

    public void activateShakeDetectorSound(){

        if (!shakeSound) {
            shakeSound = true;
            tiltSound = false;
            accSound = false;
//            soundButton.setText("Switch sound OFF");

            Thread shakeSoundThread =  new Thread(new Runnable() {
                @Override
                public void run() {

                    while(shakeSound) {
                        float samples[] = new float[audioDevice.getBufferSize()];

                        for (int i = 0; i < samples.length; i++) {
                            samples[i] = (float) Math.sin(angle) * shakeCoeff;
                            angle += increment;
                        }

                        audioDevice.writeSamples(samples);
                        if (shakeCoeff >= 0.01)
                            shakeCoeff -= 0.01;
                        else
                            shakeCoeff = 0.00f;
                        currentShaking.post(new Runnable() {
                            public void run() {
                                currentShaking.setText(Float.toString(shakeCoeff));
                            }
                        });
                        //MainActivity.this.updateShakingTextView(shakeCoeff);
                    }
                }
            });
            shakeSoundThread.start();

        }
        else {
            soundButton.setText("Switch sound ON");
            shakeSound = false;
        }
    }

    public void activateTiltSound(){

        if (!tiltSound) {
            tiltSound = true;
            shakeSound = false;
            accSound = false;
            Toast toast = Toast.makeText(getApplicationContext(), "You should hear a sound", 1500);
            toast.show();

            Thread tiltSoundThread =  new Thread(new Runnable() {
                @Override
                public void run() {

                    while(tiltSound) {
                        float increment = (float) (2 * Math.PI) * tiltSoundFrequency / audioDevice.getSampleRate(); // angular increment for each sample
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
            tiltSoundThread.start();

        }
        else {
            tiltSound = false;
        }
    }

    public void activateAccSound(){

        if (!accSound) {
            accSound = true;
            shakeSound = false;
            tiltSound = false;
            Toast toast = Toast.makeText(getApplicationContext(), "You should hear a sound", 1500);
            toast.show();

            Thread accSoundThread =  new Thread(new Runnable() {
                @Override
                public void run() {

                    while(accSound) {
                        if (whipSound) {
                            whipSound = false;
                            MediaPlayer.create(getApplicationContext(), R.raw.slap21).start();
                        }
                        if (laserSound) {
                            laserSound = false;
                            MediaPlayer.create(getApplicationContext(), R.raw.laser).start();
                        }
                    }
                }
            });
            accSoundThread.start();

        }
        else {
            accSound = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        cleanValues();
        updateCurrentValues();

        tiltSoundFrequency += (int)(lastX * 10);

        deltaX = Math.abs(lastX - sensorEvent.values[0]);
        deltaY = Math.abs(lastY - sensorEvent.values[1]);
        deltaZ = Math.abs(lastZ - sensorEvent.values[2]);

        if (deltaX < NOISE_THRESHOLD)
            deltaX = 0;
        if (deltaY < NOISE_THRESHOLD)
            deltaY = 0;
        if (deltaZ < NOISE_THRESHOLD)
            deltaZ = 0;

        if (deltaZ > 20)
            whipSound = true;

        if (deltaX > 20)
            laserSound = true;

        lastX = sensorEvent.values[0];
        lastY = sensorEvent.values[1];
        lastZ = sensorEvent.values[2];
    }

    private void printAccValues(float x, float y, float z) {
        currentX.setText(Float.toString(x));
        currentY.setText(Float.toString(y));
        currentZ.setText(Float.toString(z));
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

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.shakeDetectionRadioButton:
                if (checked) {
                    Toast.makeText(getApplicationContext(), "shake", Toast.LENGTH_SHORT).show();
                    activateShakeDetectorSound();
                }
                break;
            case R.id.tiltDetectionRadioButton:
                if (checked) {
                    Toast.makeText(getApplicationContext(), "tilt", Toast.LENGTH_SHORT).show();
                    activateTiltSound();
                }
                break;
            case R.id.soundGeneratorRadioButton:
                if (checked) {
                    Toast.makeText(getApplicationContext(), "sound", Toast.LENGTH_SHORT).show();
                    activateAccSound();
                }
                break;
        }
    }
}
