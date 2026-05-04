import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle;

public class Metronome {

    static final File normalClick = new File("metronome-hit-low.wav");
    static final File downbeat = new File("metronome-hit-bright.wav");
    double lastRecordedTime;
    

    Metronome(){
        lastRecordedTime = System.currentTimeMillis();
        main();
    }
    public void main(){
        while (true){
            double currentTime = System.currentTimeMillis();
            if (currentTime >= lastRecordedTime + 250){
                playNormalClick();
                lastRecordedTime = currentTime;
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
