package org.example;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;


import be.tarsos.dsp.pitch.Yin;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

public class AudioTranscriberYIN {
    Gui gui;
    TargetDataLine line;
    AudioFormat format = new AudioFormat(49152, 16, 1, true, false);
    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int numBytesRead;
    boolean stopped = false;
    boolean loudEnough = false;
    int sampleCounterMeasure = 0;
    int sampleCounterNote = 0;
    ArrayList<ArrayList> measureList = new ArrayList<>();
    String previousNote;
    ArrayList<String> currentMeasure = new ArrayList<>();
    String noteAndOctave;
    int counter = 0;
    ArrayList<String> tempList = new ArrayList<String>();
    HashMap<String, Integer> tempMap = new HashMap<>();
    Yin yin = new Yin(49152, 1024);
    int windowFrameCounter = 0;
    HashMap<Integer, String> notesMap = new HashMap<Integer, String>();
    int loopCounter = 0;

    AudioTranscriberYIN(Gui gui) {

    notesMap.put(0, "C");
    notesMap.put(1, "C#");
    notesMap.put(2, "D");
    notesMap.put(3, "D#");
    notesMap.put(4, "E");
    notesMap.put(5, "F");
    notesMap.put(6, "F#");
    notesMap.put(7, "G");
    notesMap.put(8, "G#");
    notesMap.put(9, "A");
    notesMap.put(10, "A#");
    notesMap.put(11, "B");
    notesMap.put(-1, "B");
    notesMap.put(-2, "A#");
    notesMap.put(-3, "A");
    notesMap.put(-4, "G#");
    notesMap.put(-5, "G");
    notesMap.put(-6, "F#");
    notesMap.put(-7, "F");
    notesMap.put(-8, "E");
    notesMap.put(-9, "D#");
    notesMap.put(-10, "D");
    notesMap.put(-11, "C#");

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

    }

    void start(){
        //
        byte[] data = new byte[2048];
        line.start();
        while (!stopped){

        //     // Copy bytes from the line's buffer to data byte array
        if (loopCounter % 48 == 0){
        System.out.println("CLICK");
        }
        loopCounter++;

        line.read(data, 0, data.length);

            float[] sampleArray = new float[1024];

            
            // Combine two bytes into one value, as its 2 bytes per sample
            for (int i = 0, j = 0; i < data.length - 1; i += 2, j++){
                int low  = data[i] & 0xFF;
                int high = data[i+1] & 0xFF;
                short combined = (short) ((high << 8) | low);
                float sample = (float) combined;
                sampleArray[j] = sample;
            }

            // Grab largest amplitude (volume)
            double largestAmplitude = 0;
            for (int i = 0; i < sampleArray.length; i++){
                if (Math.abs(sampleArray[i]) > largestAmplitude){
                    largestAmplitude = Math.abs(sampleArray[i]);
                }
            }
            // System.out.println(largestAmplitude);
            if (largestAmplitude > 200){
                loudEnough = true;
            } else {
                loudEnough = false;
            }

            // Update color based on loud
            gui.loudEnoughColor(loudEnough);


                if (loudEnough){
            // Feed array of amplitudes into fourier transform
            // Transforms sampleArray into array of real/complex numbers
                    float frequency = yin.getPitch(sampleArray).getPitch();
                    calculateClosestNote(frequency);
                    // System.out.println("frequency " + frequency + " note and octave: " + noteAndOctave);
                    gui.updateFrequency(frequency, noteAndOctave, 0);
                } else {
                    noteAndOctave = "REST";
                } 



                if (!tempMap.containsKey(noteAndOctave)){
                    tempMap.put(noteAndOctave, 0);
                }
                tempMap.put(noteAndOctave, tempMap.get(noteAndOctave) + 1);
                windowFrameCounter++;

                if (windowFrameCounter == 12){
                int  highestNoteCount = 0;
                String mostCommonNote = "";
                for (String note : tempMap.keySet()){
                    if (tempMap.get(note) >= highestNoteCount){
                        highestNoteCount = tempMap.get(note);
                        mostCommonNote = note;
                    }
                }
                windowFrameCounter = 0;
                tempMap.clear();
                measureCreator(mostCommonNote);
                }





            // A1 A2 A3 B4 C5 C6 D7 E8 E9 E10 E11 F12 E13 G14 G15 H16 G17
        }
    }

    public void measureCreator(String noteAndOctave){
            if (sampleCounterMeasure == 0){
                previousNote = noteAndOctave;
                sampleCounterNote++;
                sampleCounterMeasure++;
            } else if (sampleCounterMeasure == 16){
                currentMeasure.add(previousNote);
                currentMeasure.add(Integer.toString(sampleCounterNote));
                measureList.add(currentMeasure);
                System.out.println(currentMeasure);
                // roundNoteLengthOfMeasure(currentMeasure);
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
    }

    public void printMixers(){
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(info);
            for (Line thisLine : mixer.getTargetLines()){
                if (thisLine == line){
                    System.out.println(info.getName());
                }
            }
        }

    }

    // maybe return a note class instead of string?
    // supposed to represent the most prominent note out of the quarter of a second

    // public String getProminentNote(){

    // }

    public void calculateClosestNote (float frequency){
        // -1 is passed in when the algorithm could not detect a pitch from the audio sample
        if (frequency == -1.0){
            noteAndOctave = "REST FROM NO PITCH";
        } else {
            // num of cycles over time period / (length of each sample)
            double octaveDifference = Math.log(frequency/261.6256)/Math.log(2);
            double numOfSemitones = 12 * octaveDifference;
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
            
            double octave = 4;

            if (octaveDifference >= 0){
                octaveDifference = Math.floor(octaveDifference);
                octave += octaveDifference;
            } else {
                octaveDifference = Math.ceil(octaveDifference);
                octave += octaveDifference;
            }

            // noteAndOctave = "<html>" + noteName + "<sub>" + (int)(octave) + "</sub></html>";
            noteAndOctave = noteName + (int)(octave);

      }

    }

    void roundNoteLengthOfMeasure(ArrayList<String> measure){
        // length of measure in .25s units
        int roundedMeasureLength = 0;
        for (int i = 1; i < measure.size(); i += 2){
            int noteLength = Integer.valueOf(measure.get(i));
            if (noteLength >= 14){
                roundedMeasureLength += 16;
            } else if (noteLength >= 10){
                roundedMeasureLength += 12;
            } else if (noteLength >= 5){
                roundedMeasureLength += 8;
            } else if (noteLength >= 3){
                roundedMeasureLength += 4;
            } else if (noteLength == 2){
                roundedMeasureLength += 2;
            }
        }
        // System.out.println(roundedMeasureLength);
    }

}