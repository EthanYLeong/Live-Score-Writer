import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;

import org.jtransforms.fft.DoubleFFT_1D;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class AudioTranscriber {

    Gui gui;

    TargetDataLine line;
    AudioFormat format = new AudioFormat(88200, 16, 1, true, false);
    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int numBytesRead;
    boolean stopped = false;
    DoubleFFT_1D FFT = new DoubleFFT_1D(22050);
    boolean loudEnough = false;
    int sampleCounterMeasure = 0;
    int sampleCounterNote = 0;
    ArrayList<ArrayList> measureList = new ArrayList<>();
    String previousNote;
    ArrayList<String> currentMeasure = new ArrayList<>();
    String noteAndOctave;


    HashMap<Integer, String> notesMap = new HashMap<Integer, String>();

    AudioTranscriber(Gui gui) {
        notesMap.put(0, "A");
        notesMap.put(-11, "A#");
        notesMap.put(1, "A#");
        notesMap.put(-10, "B");
        notesMap.put(2, "B");
        notesMap.put(-9, "C");
        notesMap.put(3, "C");
        notesMap.put(-8, "C#");
        notesMap.put(4, "C#");
        notesMap.put(-7, "D");
        notesMap.put(5, "D");
        notesMap.put(-6, "D#");
        notesMap.put(6, "D#");
        notesMap.put(-5, "E");
        notesMap.put(7, "E");
        notesMap.put(-4, "F");
        notesMap.put(8, "F");
        notesMap.put(-3, "F#");
        notesMap.put(9, "F#");
        notesMap.put(-2, "G");
        notesMap.put(10, "G");
        notesMap.put(-1, "G#");
        notesMap.put(11, "G#");

        this.gui = gui;

        if (!AudioSystem.isLineSupported(info)) {
            // ADD ERROR MESSAGE?
            stopped = true;
        }
        // Obtain and open the line.
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
        } catch (LineUnavailableException ex) {
            // ADD ERROR MESSAGE ?
            stopped = true;
        }

        start();
    }

    void start(){
        // buffer size is half a second worth of samples
        // so data is a quarter of a second of sample
        byte[] data = new byte[line.getBufferSize()/2];
        line.start();
        while (!stopped){

            // Copy bytes from the line's buffer to data byte array
            line.read(data, 0, data.length);

            double[] sampleArray = new double[(data.length/2) + 1];

            // Combine two bytes into one value, as its 2 bytes per sample
            for (int i = 0, counter = 0; i < data.length - 1; i += 2, counter++){
                int low  = data[i] & 0xFF;
                int high = data[i+1] & 0xFF;
                short combined = (short) ((high << 8) | low);
                double sample = (double) combined;
                sampleArray[counter] = sample;
            }



            // Grab largest amplitude (volume)
            double largestAmplitude = 0;
            for (int i = 0; i < sampleArray.length; i++){
                if (Math.abs(sampleArray[i]) > largestAmplitude){
                    largestAmplitude = Math.abs(sampleArray[i]);
                }
            }


            if (largestAmplitude > 750){
                loudEnough = true;
            } else {
                loudEnough = false;
            }

            // Update color based on loud
            gui.loudEnoughColor(loudEnough);

            // Only run pitch detection if a sample has amplitude (volume) of certain size
            // TODO: turn all of the pitch detection into methods, and only run method if it is loud enough
            // if it is not loud enough, run measure creator with noteandocatve as rest
            if (!loudEnough){
                continue;
            }

            // Feed array of amplitudes into fourier transform
            // Transforms sampleArray into array of complex numbers
            FFT.realForward(sampleArray);

            double strongestMagnitude = 0;
            int dominantFrequencyBin = 1;

            // start at index 2, as 1 and 0 contain info about mean 
            // frequency bins start at index 2
            for (int i = 2; i < sampleArray.length/2; i += 2){
                double re = sampleArray[i];
                double im = sampleArray[i+1];
                int frequencyBin = i/2; 
                double magnitude = Math.sqrt((re * re) + (im * im));
                if (magnitude > strongestMagnitude){
                    strongestMagnitude = magnitude;
                    dominantFrequencyBin = frequencyBin;
                }
            }

            // num of cycles over time period / (length of each sample)
            double frequency = (dominantFrequencyBin)/0.25;

            double numOfSemitones = 12 * (Math.log((frequency/440))/Math.log(2));
            double lower = Math.floor(numOfSemitones);
            double upper = Math.ceil(numOfSemitones);
            double closest = 0;
            String noteName = "";
            if (numOfSemitones - lower >= upper - numOfSemitones){
                closest = upper;
            } else {
                closest = lower;
            }

            int semitoneDifference = (int) closest % 12;

            noteName = notesMap.get(semitoneDifference);

            double correctPitchFrequency = 440 * Math.pow(1.05946, closest);
            double cents = 1200 * (Math.log(frequency/correctPitchFrequency)/Math.log(2));
            cents = Math.floor(cents * 10)/10;
            double octave = 4;
            // had to artificially lower C4 in order for C6 to be caputred
            double octaveDifference = Math.log(frequency/261.6256)/Math.log(2);
            if (octaveDifference >= 0){
                octaveDifference = Math.floor(octaveDifference);
                octave += octaveDifference;
            } else {
                octaveDifference = 1/octaveDifference;
                octaveDifference = Math.ceil(octaveDifference);
                octave -= octaveDifference;
            }


            noteAndOctave = "<html>" + noteName + "<sub>" + (int)(octave) + "</sub></html>";

            System.out.println(noteAndOctave);


            gui.updateFrequency(frequency, noteAndOctave, cents);
            

            if (sampleCounterMeasure == 0){
                previousNote = noteAndOctave;
                sampleCounterNote++;
                sampleCounterMeasure++;
            } else if (sampleCounterMeasure == 16){
                currentMeasure.add(previousNote);
                currentMeasure.add(Integer.toString(sampleCounterNote));
                measureList.add(currentMeasure);
                System.out.println(currentMeasure);
                currentMeasure = new ArrayList<>();
                previousNote = noteAndOctave;
                sampleCounterMeasure = 1;
                sampleCounterNote = 1;
            } else {
                if (previousNote.equals(noteAndOctave)){
                    sampleCounterNote++;  
                } else {
                    currentMeasure.add(previousNote);
                    currentMeasure.add(Integer.toString(sampleCounterNote));
                    previousNote = noteAndOctave;
                    sampleCounterNote = 1;
                }
                sampleCounterMeasure++;
            } 

            // out.write(data, 0, numBytesRead); 
            // System.out.println(out);

            // A1 A2 A3 B4 C5 C6 D7 E8 E9 E10 E11 F12 E13 G14 G15 H16 G17
        }
    }

}