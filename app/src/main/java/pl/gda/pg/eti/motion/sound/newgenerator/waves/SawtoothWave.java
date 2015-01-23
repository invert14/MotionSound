package pl.gda.pg.eti.motion.sound.newgenerator.waves;

/**
 * Created by ps on 23.01.15.
 */
public class SawtoothWave extends WaveGroup {

    public SawtoothWave() {
        super();
    }

    public void init(double frequency, int harmonicsCount) {
        this.harmonicsCount = harmonicsCount;
        for (int i = 0; i < harmonicsCount; i++) {
            SineWave wave = new SineWave(frequency * (i + 1));
            wave.setCoefficient(1.0 / (i + 1));
            waves.add(wave);
        }
    }

    public void changeFrequency(double frequency) {
        for (int i = 0; i < harmonicsCount; i++) {
            SineWave wave = waves.get(i);
            wave.setFrequency(frequency * (i + 1));
        }
    }
}
