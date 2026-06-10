package org.example;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;



import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import org.audiveris.proxymusic.Attributes;
import org.audiveris.proxymusic.Clef;
import org.audiveris.proxymusic.ClefSign;
import org.audiveris.proxymusic.Identification;
import org.audiveris.proxymusic.Key;
import org.audiveris.proxymusic.Note;
import org.audiveris.proxymusic.NoteType;
import org.audiveris.proxymusic.ObjectFactory;
import org.audiveris.proxymusic.PartList;
import org.audiveris.proxymusic.PartName;
import org.audiveris.proxymusic.Pitch;
import org.audiveris.proxymusic.ScorePart;
import org.audiveris.proxymusic.ScorePartwise;
import org.audiveris.proxymusic.Step;
import org.audiveris.proxymusic.Time;
import org.audiveris.proxymusic.TypedText;

import be.tarsos.dsp.pitch.Yin;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import org.audiveris.proxymusic.ScorePartwise.Part;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.AttributedCharacterIterator.Attribute;

public class AudioTranscriberYIN {
    private Thread thread = new Thread(() -> musicTranscribe());
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
    Note previousNote;
    ArrayList<Object> currentMeasure = new ArrayList<>();
    String noteAndOctave;
    Note note;
    int counter = 0;
    ArrayList<String> tempList = new ArrayList<String>();
    HashMap<Note, Integer> tempMap = new HashMap<>();
    Yin yin = new Yin(49152, 1024);
    int windowFrameCounter = 0;
    HashMap<Integer, String> notesMap = new HashMap<Integer, String>();
    int loopCounter = 0;

    ObjectFactory factory = new ObjectFactory();
    ScorePartwise.Part part = factory.createScorePartwisePart();
    ScorePartwise scorePartwise = factory.createScorePartwise();


    AudioTranscriberYIN(Gui gui) {
    musicFileSetup();

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

    public void start(){
        thread.start();
    }

    private void musicTranscribe(){
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
                    note = factory.createNote();
                    note.setRest(factory.createRest());
                    noteAndOctave = "REST";
                } 



                if (!tempMap.containsKey(note)){
                    tempMap.put(note, 0);
                }

                tempMap.put(note, tempMap.get(note) + 1);
                windowFrameCounter++;
                if (windowFrameCounter == 12){
                int  highestNoteCount = 0;
                Note mostCommonNote = factory.createNote();
                for (Note note : tempMap.keySet()){
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

    public void measureCreator(Note noteAndOctave){
            if (sampleCounterMeasure == 0){
                previousNote = noteAndOctave;
                sampleCounterNote++;
                sampleCounterMeasure++;
            } else if (sampleCounterMeasure == 16){
                currentMeasure.add(previousNote);
                currentMeasure.add(Integer.toString(sampleCounterNote));
                measureList.add(currentMeasure);
                System.out.println(currentMeasure);

                updateMusicFile(currentMeasure);
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

                    // updateMusicFile(currentMeasure);
                }
                sampleCounterMeasure++;
            } 
    }

    // maybe return a note class instead of string?
    // supposed to represent the most prominent note out of the quarter of a second



    public void updateMusicFile(ArrayList<Object> currentMeasure){
        ScorePartwise.Part.Measure measure = factory.createScorePartwisePartMeasure();
        for (int i = 0; i < currentMeasure.size() - 1; i += 2){
        Note note = (Note) currentMeasure.get(i);
        int divisionCount = Integer.valueOf((String) currentMeasure.get(i+1));
        // note.getPitch().getStep().toString();
        note.setDuration(new BigDecimal(divisionCount));
        measure.getNoteOrBackupOrForward().add(note);
        }
        part.getMeasure().add(measure);

        try {
            JAXBContext context = JAXBContext.newInstance(ScorePartwise.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(scorePartwise, stringWriter);
            JavaFX.getInstance().loadMusicXmlFile(stringWriter.toString());
        } catch (JAXBException e){
            System.out.println("Error: " + e);
        }
    }

    public void calculateClosestNote (float frequency){
        // -1 is passed in when the algorithm could not detect a pitch from the audio sample
        if (frequency == -1.0){
            noteAndOctave = "REST FROM NO PITCH";
        } else {
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


            int octave = 4;

            if (octaveDifference >= 0){
                octaveDifference = Math.floor(octaveDifference);
                octave += octaveDifference;
            } else {
                octaveDifference = Math.ceil(octaveDifference);
                octave += octaveDifference;
            }

            Note note = factory.createNote();
            Pitch pitch = factory.createPitch();
            note.setPitch(pitch);
            pitch.setStep(Step.valueOf(noteName.substring(0, 1)));
            if (noteName.length() == 2){
                pitch.setAlter(new BigDecimal(1));
            }
            pitch.setOctave(octave);
            note.setRest(null);
            this.note = note;
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

    void musicFileSetup(){
        PartList partList = factory.createPartList();
        scorePartwise.setPartList(partList);
        ScorePart scorePart = factory.createScorePart();
        partList.getPartGroupOrScorePart().add(scorePart);
        PartName partName = factory.createPartName();
        partName.setValue("the only part");
        scorePart.setPartName(partName);
        scorePart.setId("P1");

        scorePartwise.getPart().add(part);
        part.setId(scorePart);

        ScorePartwise.Part.Measure measure = factory.createScorePartwisePartMeasure();
        part.getMeasure().add(measure);

        Attributes attributes = factory.createAttributes();
        measure.getNoteOrBackupOrForward().add(attributes);
        attributes.setDivisions(new BigDecimal(4));

        Key key = factory.createKey();
        attributes.getKey().add(key);
        // ????
        key.setFifths(new BigInteger("0"));

        Time time = factory.createTime();
        attributes.getTime().add(time);
        time.getTimeSignature().add(factory.createTimeBeats("4"));
        time.getTimeSignature().add(factory.createTimeBeatType("4"));

        Clef clef = factory.createClef();
        attributes.getClef().add(clef);
        clef.setSign(ClefSign.G);
        clef.setLine(new BigInteger("2"));
    }

}