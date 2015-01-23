package pl.gda.pg.eti.motion.sound.newgenerator.waves;

/**
 * Created by ps on 22.01.15.
 */
public class SineWave {

    double coefficient = 1.0;
    private double frequency = 880.0;

    public SineWave(double frequency) {
        this.frequency = frequency;
    }

    public double generate(double time, int sampleRate) {
        return coefficient * Math.sin(2 * Math.PI * time / ((double) sampleRate / frequency));
    }

    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }
}
