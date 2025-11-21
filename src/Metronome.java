import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Metronome {

    public static void main(String[] args){
        String filepath = "metronome-85688.wav";
        playMusic(filepath);

    }

    public static void playMusic(String location){
        try 
        {
            File musicPath = new File(location);

            if (musicPath.exists()){
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
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
