package pl.gda.pg.eti.motion.sound.newgenerator;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import pl.gda.pg.eti.motion.sound.newgenerator.waves.SawtoothWave;
import pl.gda.pg.eti.motion.sound.newgenerator.waves.SineWave;
import pl.gda.pg.eti.motion.sound.newgenerator.waves.SquareWave;
import pl.gda.pg.eti.motion.sound.newgenerator.waves.TriangleWave;
import pl.gda.pg.eti.motion.sound.newgenerator.waves.WaveGroup;


/**
 * Created by ps on 22.01.15.
 */
public class SoundGenerator implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensor;

    private final int sampleRate = 8000;
    private final int bufferLength = 16;
    private double timeOffset = 0.0;
    private double timeDelta = 0.5;
    private double baseFrequency = 880.0;
    private int harmonicsCount = 10;
    private double[] gravity = {0.0, 0.0, 0.0};
    private double[] linear_acceleration = {0.0, 0.0, 0.0};
    boolean active = false;
    AudioTrack audioTrack;
    SawtoothWave saw;
    WaveGroup sine;
    TriangleWave triangle;
    SquareWave square;
    WaveGroup currentWave;
    private CharSequence currentWaveType = "sine";
    private final Object lock = new Object();

    public SoundGenerator(Context context) {
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferLength,
                AudioTrack.MODE_STREAM);

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        prepareWaves(880.0);
        currentWave = sine;
    }

    public void prepareWaves(double frequency) {
        synchronized (lock) {
            prepareSineWave(frequency);
            prepareSawtoothWave(frequency);
            prepareSquareWave(frequency);
            prepareTriangleWave(frequency);
            changeWaveType(currentWaveType);
        }
    }

    private void prepareSineWave(double frequency) {
        sine = new WaveGroup();
        sine.addWave(new SineWave(frequency));
    }

    private void prepareSawtoothWave(double frequency) {
        saw = new SawtoothWave();
        saw.init(frequency, harmonicsCount);
    }

    private void prepareSquareWave(double frequency) {
        square = new SquareWave();
        square.init(frequency, harmonicsCount);
    }

    private void prepareTriangleWave(double frequency) {
        triangle = new TriangleWave();
        triangle.init(frequency, harmonicsCount);
    }

    public void toggle() {
        active = !active;
    }

    public void play() {
        audioTrack.play();
        while (true) {
            if (active) {
                audioTrack.write(prepareBuffer(), 0, bufferLength);
            }
        }
    }

    public void startThread() {
        Thread soundThread = new Thread() {
            @Override
            public void run() {
                play();
            }
        };
        soundThread.start();
    }

    short[] prepareBuffer() {
        synchronized (lock) {
            short[] buffer = new short[bufferLength];
            for (int i = 0; i < bufferLength; i++) {
                buffer[i] = (short) (sine.generate(timeOffset, sampleRate) * Short.MAX_VALUE);
                buffer[i] += (short) (square.generate(timeOffset, sampleRate) * Short.MAX_VALUE);
                buffer[i] += (short) (triangle.generate(timeOffset, sampleRate) * Short.MAX_VALUE);
                timeOffset += timeDelta;
            }
            return buffer;
        }
    }

    public void changeFrequency(double frequency) {
        synchronized (lock) {
            triangle.changeFrequency(frequency);
            square.changeFrequency(frequency);
            saw.changeFrequency(frequency);
            sine.changeFrequency(frequency);
        }
    }


    public void changeWaveType(CharSequence waveType) {
        currentWaveType = waveType;
        if (waveType.equals("sine")) {
            currentWave = sine;
        }
        if (waveType.equals("sawtooth")) {
            currentWave = saw;
        }
        if (waveType.equals("triangle")) {
            currentWave = triangle;
        }
        if (waveType.equals("square")) {
            currentWave = square;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double alpha = 0.8;

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];


//        Log.v("GRAVITY", String.valueOf(gravity[0]) + " " + String.valueOf(gravity[1]) + " " + String.valueOf(gravity[2]));
//        Log.v("ACCELERATION", String.valueOf(linear_acceleration[0]) + " " + String.valueOf(linear_acceleration[1]) + " " + String.valueOf(linear_acceleration[2]));

        updateCoefficients();

    }

    private void updateCoefficients() {
        double sineCoeff = checkRange(Math.abs(gravity[2]) / 10.0);
        double squareCoeff = checkRange(Math.abs(gravity[1]) / 10.0);
        double triangleCoeff = checkRange(Math.abs(gravity[0]) / 10.0);
        sine.setCoefficient(sineCoeff);
        square.setCoefficient(squareCoeff);
        triangle.setCoefficient(triangleCoeff);
    }

    private double checkRange(double value) {
        if (value > 1.0)
            return 1.0;
        if (value < 0.1)
            return 0.0;
        return value;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
