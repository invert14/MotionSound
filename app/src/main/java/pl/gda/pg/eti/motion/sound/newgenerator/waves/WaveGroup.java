package pl.gda.pg.eti.motion.sound.newgenerator.waves;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ps on 23.01.15.
 */
public class WaveGroup {

    List<SineWave> waves = new ArrayList<SineWave>();
    private double coefficient = 1.0;
    public int harmonicsCount;

    public WaveGroup() {
    }

    public double generate(double time, int sampleRate) {
        double value = 0;
        for (SineWave wave : waves)
            value += wave.generate(time, sampleRate);
        return value * coefficient;
    }

    public void addWave(SineWave wave) {
        waves.add(wave);
    }

    public void changeFrequency(double frequency) {
        for (SineWave wave : waves)
            wave.setFrequency(frequency);
    }

    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }
}
