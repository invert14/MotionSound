package pl.gda.pg.eti.motion.sound.newgenerator;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ps on 22.01.15.
 */
public class SoundGenerator {

    private final int sampleRate = 8000;
    private final int bufferLength = 16;
    private double timeOffset = 0.0;
    private double timeDelta = 0.5;
    private double baseFrequency = 880.0;
    private int harmonicsCount = 30;
    boolean active = false;
    AudioTrack audioTrack;
    List<SineWave> saw = new ArrayList<SineWave>();
    List<SineWave> sine = new ArrayList<SineWave>();
    List<SineWave> square = new ArrayList<SineWave>();
    List<SineWave> triangle = new ArrayList<SineWave>();
    List<SineWave> waves;
    private CharSequence currentWaveType = "sine";
    private final Object lock = new Object();

    public SoundGenerator() {
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferLength,
                AudioTrack.MODE_STREAM);
        prepareWaves(880.0);
        waves = sine;
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
        sine.clear();
        sine.add(new SineWave(frequency));
    }

    private void prepareSawtoothWave(double frequency) {
        saw.clear();
        for (int i = 0; i < harmonicsCount; i++) {
            SineWave wave = new SineWave(frequency * (i + 1));
            wave.setCoefficient(1.0 / (i + 1));
            saw.add(wave);
        }
    }

    private void prepareSquareWave(double frequency) {
        square.clear();
        for (int i = 0; i < harmonicsCount; i++) {
            if (i % 2 == 0) {
                SineWave wave = new SineWave(frequency * (i + 1));
                wave.setCoefficient(1.0 / (i + 1));
                square.add(wave);
            }
        }
    }

    private void prepareTriangleWave(double frequency) {
        triangle.clear();
        for (int i = 0; i < harmonicsCount; i++) {
            if (i % 2 == 0) {
                SineWave wave = new SineWave(frequency * (i + 1));
                wave.setCoefficient(1.0 / Math.pow(i + 1, 2.0));
                triangle.add(wave);
            }
        }
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
                buffer[i] = 0;
                for (SineWave wave : waves)
                    buffer[i] += (short) (wave.generate(timeOffset, sampleRate) * Short.MAX_VALUE);
                timeOffset += timeDelta;
            }
            return buffer;
        }
    }


    public void changeWaveType(CharSequence waveType) {
        currentWaveType = waveType;
        if (waveType.equals("sine")) {
            waves = sine;
        }
        if (waveType.equals("sawtooth")) {
            waves = saw;
        }
        if (waveType.equals("triangle")) {
            waves = triangle;
        }
        if (waveType.equals("square")) {
            waves = square;
        }
    }
}
