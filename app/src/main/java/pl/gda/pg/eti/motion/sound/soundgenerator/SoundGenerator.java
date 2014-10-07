package pl.gda.pg.eti.motion.sound.soundgenerator;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

// originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
// and modified by Steve Pomeroy <steve@staticfree.info>
public class SoundGenerator {


    private static int numSamples;
    private static double sample[];
    private static final int sampleRate = 8000;
    private static byte generatedSnd[];


    public static void playTone(final double freqOfTone, final double freq2, final int duration) {

        // Use a new tread as this can take a while
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                genTone(freqOfTone, freq2, duration);
                playSound();
            }
        });
        thread.start();
    }

    private static void genTone(double freqOfTone, double freq2, int duration) {

        numSamples = sampleRate * duration;
        sample = new double[numSamples];
        generatedSnd = new byte[2 * numSamples];

        double freqdiff = freq2 - freqOfTone;
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            double interpolation = (double) i / (double) numSamples;
            double freq = interpolation * freqdiff + freqOfTone;
            sample[i] = Math.sin(2 * Math.PI * i / ((double)sampleRate / freq));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
    }

    private static void playSound() {
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
    }
}
