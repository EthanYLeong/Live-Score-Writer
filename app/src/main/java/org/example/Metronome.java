package org.example;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;



public class Metronome {

    static final File normalClick = new File("metronome-hit-low.wav");
    static final File downbeat = new File("metronome-hit-bright.wav");
    double lastRecordedClick;
    double counter = 0;
    Thread t = new Thread(() -> {System.out.println("start metronome");playMetronome();
        });

    Metronome(){

        System.out.println("CONSTRUCTOR");
        // offset in order to match the sound of the actual click to the
        // beginning of the read method in audiotranscriber
        lastRecordedClick = System.currentTimeMillis() - 50;
    }
    public void playMetronome(){
        while (true){
            double currentTime = System.currentTimeMillis();
            if (currentTime >= lastRecordedClick + 1000){
                if (counter % 4 == 0){
                    playDownbeat();
                } else {
                    playNormalClick();
                }
                counter++;
                lastRecordedClick = currentTime;
            }
        }
    }

    public static void playNormalClick(){
        try 
        {
            if (normalClick.exists()){
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(normalClick);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
            } else{
                System.out.println("cant find ur dumb ahh file");
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }


    public static void playDownbeat(){
        try 
        {
            if (downbeat.exists()){
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(downbeat);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
            } else{
                System.out.println("cant find ur dumb ahh file");
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
}
