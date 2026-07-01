package org.example;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.audiveris.proxymusic.Attributes;
import org.audiveris.proxymusic.Clef;
import org.audiveris.proxymusic.ClefSign;
import org.audiveris.proxymusic.Key;
import org.audiveris.proxymusic.Note;
import org.audiveris.proxymusic.ObjectFactory;
import org.audiveris.proxymusic.PartList;
import org.audiveris.proxymusic.PartName;
import org.audiveris.proxymusic.Pitch;
import org.audiveris.proxymusic.ScorePart;
import org.audiveris.proxymusic.ScorePartwise;
import org.audiveris.proxymusic.Step;
import org.audiveris.proxymusic.Time;

import be.tarsos.dsp.pitch.Yin;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;

import java.math.BigDecimal;
import java.math.BigInteger;

public class AudioTranscriberYIN {
    private Thread thread = new Thread(() -> musicTranscribe());
    TargetDataLine line;
    AudioFormat format = new AudioFormat(49152, 16, 1, true, false);
    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int numBytesRead;
    boolean isTranscriberActive = false;
    int sampleCounterMeasure = 0;
    int sampleCounterNote = 0;
    Note previousNote;
    ArrayList<Object> measureBuffer = new ArrayList<>();
    String noteAndOctave;
    Note note;
    int counter = 0;
    ArrayList<String> tempList = new ArrayList<String>();
    HashMap<Note, Integer> tempMap = new HashMap<>();
    Yin yin = new Yin(49152, 1024);
    int windowFrameCounter = 0;
    HashMap<Integer, String> notesMap = new HashMap<Integer, String>();
    JAXBContext context;
    Marshaller marshaller;
    ObjectFactory factory = new ObjectFactory();
    ScorePartwise.Part part = factory.createScorePartwisePart();
    ScorePartwise scorePartwise = factory.createScorePartwise();
    boolean marshallerError = false;
    boolean isMeasureConfigured = false;
    boolean isFirstMeasure = false;
    double currentTime;
    double previousTime;
    double timeToPrint = 0;
    int divisions = 4;
    double startTime = 0;
    double lastTime = 0;
    // interval for each division
    double interval = 250;
    double bpm = 60;
    boolean start = false;
    public static String timeSignatureNumerator = "4";
    public static String timeSignatureDenominator = "4";

    AudioTranscriberYIN() {

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

        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("LINE NOT SUPPORTED");
            isTranscriberActive = true;
        }
        System.nanoTime();
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format, 8192);
        } catch (LineUnavailableException ex) {
            System.out.println("UNAVAILABLE MICROPHONE");
            isTranscriberActive = true;
        }

        try {
            context = JAXBContext.newInstance(ScorePartwise.class);
            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        } catch (Exception e) {
            marshallerError = true;
            System.out.println("yeah so that didn't really work");
        }
    }

    public void start() {
        isTranscriberActive = true;
        thread.start();
    }

    private void musicTranscribe() {
        byte[] data = new byte[2048];
        line.start();
        previousTime = System.currentTimeMillis();

        System.out.println("Transcriber starts at " + previousTime % 1000000);
        while (isTranscriberActive) {

            if (start) {
                start = false;
                System.out.println("INTERVAL " + (((timeToPrint % 1000000000)) / 1000) + " REAL TIME "
                        + ((System.currentTimeMillis() % 1000000000) / 1000.0));
            }
            // System.out.println("TIME DIFFERENCE BETWEEN READS: " +
            // (System.nanoTime() - previousTime) / 1000000);
            // System.out.println(line.available());

            line.read(data, 0, data.length);

            // Combine two bytes into one value, as its 2 bytes per sample
            float[] sampleArray = convertToSampleArray(data);

            // Check to see if sample array is loud enough and contains an amplitude of
            // certain value
            boolean isAboveThreshold = isAboveAmplitudeThreshold(sampleArray);

            if (isAboveThreshold) {
                float frequency = yin.getPitch(sampleArray).getPitch();
                note = calculateClosestNote(frequency);

                // System.out.println("NOTE: " + noteAndOctave + " FREQUENCY: " + frequency);
            } else {
                note = factory.createNote();
                note.setRest(factory.createRest());
                noteAndOctave = "REST";
            }
            // 48 samples per second, but we want to find the most common note of a 0.25
            // second interval (12 samples)
            // for 16th note division assuming 60 bpm
            Note completedNote = findMostCommonNote(note);
            if (completedNote != null) {

                start = true;
                // System.out.println(
                // previousTime % 1000000 +
                // " -> " +
                // (completedNote.getRest() == null
                // ? completedNote.getPitch().getStep()
                // : "REST")
                // + " -> " + sampleCounterMeasure);
                if (completedNote.getRest() == null)
                    System.out.println(
                            "note: " + completedNote.getPitch().getStep() + completedNote.getPitch().getOctave());
                // System.out.println("PITCH: " + completedNote.getPitch().getStep() + "
                // COUNTER: "+ (sampleCounterMeasure - 1));
                ArrayList<Object> measureBuffer = processNoteForMeasure(completedNote);
                if (measureBuffer != null) {
                    // System.out.println("MEASURE");
                    // for (int i = 0; i < measureBuffer.size(); i += 2) {
                    // Note n = (Note) measureBuffer.get(i);
                    // int duration = Integer.parseInt((String) measureBuffer.get(i + 1));

                    // System.out.println(
                    // (n.getRest() == null ? n.getPitch().getStep() : "REST")
                    // + " x " + duration);
                    // }
                    ScorePartwise.Part.Measure measure = convertToJavaXMLMeasure(measureBuffer);
                    if (!isFirstMeasure) {
                        part.getMeasure().add(measure);
                    } else {
                        isFirstMeasure = false;
                    }
                    try {
                        JavaFX.getInstance().loadMusicXmlFile(convertJavaToXMLString(scorePartwise));
                    } catch (Exception e) {
                        System.out.println("Error: JAVA TO XML CONVERSION FAILED");
                        System.out.println(e);
                    }
                }
            }
        }
    }

    private boolean isAboveAmplitudeThreshold(float[] sampleArray) {
        double largestAmplitude = 0;
        for (int i = 0; i < sampleArray.length; i++) {
            if (Math.abs(sampleArray[i]) > largestAmplitude) {
                largestAmplitude = Math.abs(sampleArray[i]);
            }
        }
        // System.out.println(largestAmplitude);
        if (largestAmplitude > 200) {
            return true;
        } else {
            return false;
        }
    }

    private float[] convertToSampleArray(byte[] data) {
        float[] sampleArray = new float[data.length / 2];
        for (int i = 0, j = 0; i < data.length - 1; i += 2, j++) {
            int low = data[i] & 0xFF;
            int high = data[i + 1] & 0xFF;
            short combined = (short) ((high << 8) | low);
            float sample = (float) combined;
            sampleArray[j] = sample;
        }
        return sampleArray;
    }

    public Note calculateClosestNote(float frequency) {
        // TO DO: BUG WITH C4 AND C5 FLICKERING AT CONSTANT FREQUENCY
        // -1 is passed in when the algorithm could not detect a pitch from the audio
        // sample
        if (frequency == -1.0) {
            noteAndOctave = "REST";
            note = factory.createNote();
            note.setRest(factory.createRest());
            return note;
        } else {
            double octaveDifference = Math.log(frequency / 261.6256) / Math.log(2);
            double numOfSemitones = 12 * octaveDifference;
            double lower = Math.floor(numOfSemitones);
            double upper = Math.ceil(numOfSemitones);
            double closest = 0;
            String noteName = "";
            if (numOfSemitones - lower >= upper - numOfSemitones) {
                closest = upper;
            } else {
                closest = lower;
            }

            int semitoneDifference = (int) closest % 12;

            noteName = notesMap.get(semitoneDifference);

            int octave = 4;

            if (octaveDifference >= 0) {
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
            if (noteName.length() == 2) {
                pitch.setAlter(new BigDecimal(1));
            }
            pitch.setOctave(octave);
            // noteAndOctave = "<html>" + noteName + "<sub>" + (int)(octave) +
            // "</sub></html>";
            noteAndOctave = noteName + (int) (octave); // NEED TO REMOVE EVENTUALLY
            return note;
        }

    }

    private Note findMostCommonNote(Note note) {
        if (!tempMap.containsKey(note)) {
            tempMap.put(note, 0);
        }

        tempMap.put(note, tempMap.get(note) + 1);
        windowFrameCounter++;
        currentTime = System.currentTimeMillis();
        if (currentTime >= previousTime + interval) {
            start = true;
            timeToPrint = previousTime + interval;
            // System.out.println(
            // "lateness = " + (currentTime - (previousTime + interval)));
            previousTime += interval;
            int highestNoteCount = 0;
            Note mostCommonNote = factory.createNote();
            for (Note mapNote : tempMap.keySet()) {
                if (tempMap.get(mapNote) >= highestNoteCount) {
                    highestNoteCount = tempMap.get(mapNote);
                    mostCommonNote = mapNote;
                }
            }
            windowFrameCounter = 0;
            tempMap.clear();
            return mostCommonNote;
        } else {
            return null;
        }
    }

    // UNUSED
    void roundNoteLengthOfMeasure(ArrayList<String> measure) {
        // length of measure in .25s units
        int roundedMeasureLength = 0;
        for (int i = 1; i < measure.size(); i += 2) {
            int noteLength = Integer.valueOf(measure.get(i));
            if (noteLength >= 14) {
                roundedMeasureLength += 16;
            } else if (noteLength >= 10) {
                roundedMeasureLength += 12;
            } else if (noteLength >= 5) {
                roundedMeasureLength += 8;
            } else if (noteLength >= 3) {
                roundedMeasureLength += 4;
            } else if (noteLength == 2) {
                roundedMeasureLength += 2;
            }
        }
        // System.out.println(roundedMeasureLength);
    }

    public ArrayList<Object> processNoteForMeasure(Note note) {
        if (sampleCounterMeasure == 0) {
            previousNote = note;
            sampleCounterNote++;
            sampleCounterMeasure++;
            // If it's in x/8, the amount of divisions need to be halved, so we multiply by
            // (4/Integer.valueOf(timeSignatureDenominator))
        } else if (sampleCounterMeasure == Integer.valueOf(timeSignatureNumerator) * divisions
                * (4 / Float.valueOf(timeSignatureDenominator))) {
            startTime = System.currentTimeMillis();
            measureBuffer.add(previousNote);
            measureBuffer.add(Integer.toString(sampleCounterNote));
            ArrayList<Object> returnBuffer = (ArrayList<Object>) measureBuffer.clone();
            measureBuffer.clear();
            previousNote = note;
            sampleCounterMeasure = 1;
            sampleCounterNote = 1;
            return returnBuffer;
        } else {
            if (previousNote.getRest() == null && note.getRest() == null) {
                if (previousNote.getPitch().getOctave() == note.getPitch().getOctave()
                        && previousNote.getPitch().getStep() == note.getPitch().getStep()) {
                    sampleCounterNote++;
                } else {
                    measureBuffer.add(previousNote);
                    measureBuffer.add(Integer.toString(sampleCounterNote));
                    previousNote = note;
                    sampleCounterNote = 1;
                }
            } else if (previousNote.getRest() != null && note.getRest() != null) {
                sampleCounterNote++;
            } else {
                measureBuffer.add(previousNote);
                measureBuffer.add(Integer.toString(sampleCounterNote));
                previousNote = note;
                sampleCounterNote = 1;
            }
            sampleCounterMeasure++;
        }
        return null;
    }

    public ScorePartwise.Part.Measure convertToJavaXMLMeasure(ArrayList<Object> measureBuffer) {
        ScorePartwise.Part.Measure measure = factory.createScorePartwisePartMeasure();

        if (!isMeasureConfigured) {
            measure = part.getMeasure().get(0);
            isFirstMeasure = true;
            isMeasureConfigured = true;
        }

        for (int i = 0; i < measureBuffer.size() - 1; i += 2) {
            Note note = (Note) measureBuffer.get(i);
            int divisionCount = Integer.valueOf((String) measureBuffer.get(i + 1));
            note.setDuration(new BigDecimal(divisionCount));
            measure.getNoteOrBackupOrForward().add(note);
        }
        return measure;
    }

    public String getInitialXMLString() {
        PartList partList = factory.createPartList();
        scorePartwise.setPartList(partList);
        ScorePart scorePart = factory.createScorePart();
        partList.getPartGroupOrScorePart().add(scorePart);
        PartName partName = factory.createPartName();
        partName.setValue("the only part");
        scorePart.setPartName(partName);
        scorePart.setId("P1");

        ScorePartwise.Part.Measure measure = factory.createScorePartwisePartMeasure();
        part.getMeasure().add(measure);
        Attributes attributes = factory.createAttributes();
        measure.getNoteOrBackupOrForward().add(attributes);
        attributes.setDivisions(new BigDecimal(divisions));

        Key key = factory.createKey();
        attributes.getKey().add(key);
        key.setFifths(new BigInteger("0"));

        Time time = factory.createTime();
        attributes.getTime().add(time);
        time.getTimeSignature().add(factory.createTimeBeats(timeSignatureNumerator));
        time.getTimeSignature().add(factory.createTimeBeatType(timeSignatureDenominator));

        Clef clef = factory.createClef();
        attributes.getClef().add(clef);
        clef.setSign(ClefSign.G);
        clef.setLine(new BigInteger("2"));

        scorePartwise.getPart().add(part);
        part.setId(scorePart);
        try {
            return convertJavaToXMLString(scorePartwise);
        } catch (Exception e) {
            System.out.println("ERROR: UNABLE TO LOAD INITIAL MUSIC FILE");
            System.out.println(e);
        }
        return null;
    }

    public String convertJavaToXMLString(ScorePartwise scorePartwise) throws Exception {
        if (!marshallerError) {
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(scorePartwise, stringWriter);
            return stringWriter.toString();
        }
        throw new Exception("marshaller error true so no marshal or smth");
    }

    public void setBpm(double bpm) {
        this.bpm = bpm;
        interval = (60000.0 / (bpm * divisions * (4 / Float.valueOf(timeSignatureDenominator))));
        System.out.println(interval);
    }

    public void updateTimeSignature(String timeSignature) {
        System.out.println("RAHHHHH");
        Attributes attributes = (Attributes) part.getMeasure().get(0).getNoteOrBackupOrForward()
                .get(0);
        Time time = (Time) attributes.getTime().get(0);

        timeSignatureNumerator = timeSignature.substring(0, 1);
        timeSignatureDenominator = timeSignature.substring(2, 3);

        time.getTimeSignature().clear();
        time.getTimeSignature().add(factory.createTimeBeats(timeSignatureNumerator));
        time.getTimeSignature().add(factory.createTimeBeatType(timeSignatureDenominator));

        try {
            JavaFX.getInstance().loadMusicXmlFile(convertJavaToXMLString(scorePartwise));
        } catch (Exception e) {
            System.out.println("ERROR: FAILED TO UPDATE SHEET MUSIC TIME SIGNATURE VISUALLY");
            e.printStackTrace();
        }
    }

    public void updateDivisions(int divisions) {
        this.divisions = divisions;
        interval = (long) (60000 / (bpm * divisions * (4 / Float.valueOf(timeSignatureDenominator))));
        Attributes attributes = (Attributes) part.getMeasure().get(0).getNoteOrBackupOrForward()
                .get(0);
        attributes.setDivisions(new BigDecimal(divisions));

    }

    public void stop() {
        isTranscriberActive = false;
    }

}