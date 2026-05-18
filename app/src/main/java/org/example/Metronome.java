package org.example;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle;

public class Metronome {

    static final File normalClick = new File("metronome-hit-low.wav");
    static final File downbeat = new File("metronome-hit-bright.wav");
    double lastRecordedClick;
    double counter = 0;
    Thread t = new Thread(() -> playMetronome());

    Metronome(){
        lastRecordedClick = System.currentTimeMillis();
        t.start();
    }
    public void playMetronome(){
        while (true){
            double currentTime = System.currentTimeMillis();
            if (currentTime >= lastRecordedClick + 250){
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
