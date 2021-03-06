package pl.gda.pg.eti.motion.sound.newgenerator.waves;

/**
 * Created by ps on 23.01.15.
 */
public class SquareWave extends WaveGroup {

    public SquareWave() {
        super();
    }

    public void init(double frequency, int harmonicsCount) {
        this.harmonicsCount = harmonicsCount;
        for (int i = 0; i < harmonicsCount; i++) {
            if (i % 2 == 0) {
                SineWave wave = new SineWave(frequency * (i + 1));
                wave.setCoefficient(1.0 / (i + 1));
                waves.add(wave);
            }
        }
    }

    public void changeFrequency(double frequency) {
        int idx = 0;
        for (int i = 0; i < harmonicsCount; i++) {
            if (i % 2 == 0) {
                SineWave wave = waves.get(idx);
                wave.setFrequency(frequency * (i + 1));
                idx++;
            }
        }
    }
}
