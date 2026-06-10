package org.example;

public class App {
    public static void main(String[] args) throws Exception {
        Metronome metronome = new Metronome();
        Gui gui = new Gui();
        AudioTranscriberYIN audioTranscriber = new AudioTranscriberYIN(gui);
        metronome.start();
        audioTranscriber.start();
        JavaFX.main(args);
    }
}
