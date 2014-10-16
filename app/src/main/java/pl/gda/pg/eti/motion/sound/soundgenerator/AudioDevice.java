package pl.gda.pg.eti.motion.sound.soundgenerator;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioDevice
{
    public int getSampleRate() { return 4096; }
    public int getBufferSize() { return 256; }

    AudioTrack track;
    short[] buffer = new short[getBufferSize()];


    public AudioDevice()
    {
        int minSize = AudioTrack.getMinBufferSize( getSampleRate(), AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT );
        track = new AudioTrack( AudioManager.STREAM_MUSIC, getSampleRate(),
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                minSize, AudioTrack.MODE_STREAM);
        track.play();
    }

    public void writeSamples(float[] samples)
    {
        fillBuffer( samples );
        track.write( buffer, 0, samples.length );
    }

    private void fillBuffer( float[] samples )
    {
        if( buffer.length < samples.length )
            buffer = new short[samples.length];

        for( int i = 0; i < samples.length; i++ )
            buffer[i] = (short)(samples[i] * Short.MAX_VALUE);
    }
}
