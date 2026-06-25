package org.example;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

public class Metronome {

    static final File normalClick = new File("metronome-hit-low.wav");
    static final File downbeat = new File("metronome-hit-bright.wav");
    static final File silence = new File("comedic-silence-90574.wav");
    private Clip normalClip;
    private Clip downbeatClip;
    private Clip silenceClip;
    private AudioInputStream normalStream;
    private AudioInputStream downbeatStream;
    private AudioInputStream silenceStream;

    double lastRecordedClick;
    double counter = 0;
    double interval = 1000;
    private Thread thread = new Thread(() -> {
        playMetronome();
    });

    Metronome() {
        try {
            normalClip = AudioSystem.getClip();
            downbeatClip = AudioSystem.getClip();
            silenceClip = AudioSystem.getClip();
            normalStream = AudioSystem.getAudioInputStream(normalClick);
            downbeatStream = AudioSystem.getAudioInputStream(downbeat);
            silenceStream = AudioSystem.getAudioInputStream(silence);
            AudioFormat format = normalStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            normalClip = (Clip) AudioSystem.getLine(info);
            downbeatClip = (Clip) AudioSystem.getLine(info);
            silenceClip = (Clip) AudioSystem.getLine(info);
            normalClip.open(normalStream);
            downbeatClip.open(downbeatStream);
            normalClip.start();
            downbeatClip.start();
            silenceClip.open(silenceStream);
            silenceClip.start();

        } catch (Exception e) {
            System.out.println("ERROR: COULD NOT CREATE CLIPS FOR METRONOME CLICK");
            e.printStackTrace();
        }
        System.out.println("CONSTRUCTOR");
        // // offset in order to match the sound of the actual click to the
        // // beginning of the read method in audiotranscriber
    }

    public void start() {
        lastRecordedClick = System.currentTimeMillis() - interval;
        thread.start();
    }

    public void playMetronome() {
        while (true) {
            long currentTime = System.currentTimeMillis();
            if (currentTime >= lastRecordedClick + interval) {
                if (counter % Integer.valueOf(AudioTranscriberYIN.timeSignatureNumerator) == 0) {
                    playDownbeat();
                } else {
                    playNormalClick();
                }
                counter++;
                lastRecordedClick += interval;
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void playNormalClick() {
        try {
            if (normalClick.exists()) {
                normalClip.setFramePosition(0);
                normalClip.start();
            } else {
                System.out.println("cant find ur dumb ahh file");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void playDownbeat() {
        try {
            if (downbeat.exists()) {
                downbeatClip.setFramePosition(0);
                downbeatClip.start();
            } else {
                System.out.println("cant find ur dumb ahh file");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void setBpm(double bpm) {
        if (bpm <= 0)
            return;
        interval = (long) 60000 / bpm;
    }

}
