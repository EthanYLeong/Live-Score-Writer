## Observations

I've noticed that using the read method is not super consistent and aligned with real world time. The read method is supposed to take a quarter of a second worth of sample data from the buffer, and if there is less than a quarter of a second worth of data, then it will wait until that amount of data is available to read. By printing the amount of bytes in the buffer before and after the read method, it's observed that there is generally at most around half the buffer size worth of bytes before the read method is ran, meaning that the buffer is nowhere near close to overflowing, which technically means that we should be able to consistently wait a full 0.25 seconds between every read method. However, playing a metronome click after every read method which should click at 60 bpm and comparing it with an actual metronome showcases that the metronome click from the java project is not an even 60 bpm. It's also observed that the amount of bytes in the buffer before and after the read method happens stays the same for a lot of the cycles which doesn't make sense, as the read method should transfer all of the bytes that are read into a new array, clearing out most of the buffer.

Another observation is that with the use of the System.nanoTime() method, a pretty precise metronome can be made, but only if there are no other calculations happening. When a line is started and audio calculations are happening, the metronome gets considerably off, which makes sense, as more time is spent on each cycle on calculations and mess up how the timing of how often the function that runs the metronome is ran. This is made extremely clear when I have a system print method that prints the nano time in a while true loop with and without the read method. When I run the print statement by itself with no calculations, the are an unbelievable amount of prints. When I add in the read method, the amount of prints produced are far lower which obviously makes a lot of sense.

So just running the read method by itself with no other calculations, and tracking the amount of time between each read method, it should be 0.25 seconds between each read but it reports 0.25-0.25.1 consistently, with it reading jumps where it reads 0.252, 0.127, 0.376, 0.251. So using this for a metronome at least can get wonky. I plan to make a metronome in a separate class using nanotime/milliseconds with System methods, so it's not stuck on the inconsistent read method timing.

5/20/26 12:00 PM
So an issue I was experiencing was that the largestAmplitude that returned was always 1.0 even when I was playing pitches from my tuner, but it would spike up when I started talking, which made me think that it was some AI function with the microphone that filtered out any noise that wasn't a voice. I didn't have this issue previously, as I was on my old laptop without this function. I printed out all the mixers, realized there's only one mic, and found out how to disable the "audio enhancements" setting for the mic which now allows for any noise to be picked up.

5/24/26 12:09 PM
YIN Frequency works with violin, it's able to give more frequencies per time than Fourier Transform, AND the frequencies reported are far more accurate and consistent than FFT. The only thing I've noticed is that playing a 3rd finger C on the G string sometimes causes the reported frequency to flicker between a C4 and C5 in frequency.

The next step involves turning the sound into actual data that represent measures, and lining up the metronome click with the actual timing of the audio capture.

5/26/26 5:24 PM
I was being a bit silly and was trying to find the most common note every 0.25 seconds, so with 48 window frames where a note is detected, I need to find the most prominent across a period of 12 window frames, but I looked at sizes of 16 frames instead of 12, which caused the print of each common note to print slower than the metronome.

8:12 PM
I've noticed that whenever I start the app, the metronome would make numerous clicks in a quick instant and then eventually stabilize to the programmed pattern and timing of 60 bps. By using print statements, I realized that the entirety of the playNormalClick/playDownbeat methods will finish running, but no click noise would be played until moments later. I believe this is because it takes more time to open the files and run all the audio related methods on the method's first run, but exceptions from certain methods make it so I can't just move a few lines into the constructor or as a field.

6/8 11:24 AM
So creating the JavaFX object doesn't allow anything to run, and it says that JavaFX runtime components are missing, so I just have to run the static main method to boot up the JavaFX window. Also I'm changing the gradle configuration cache setting to false so I can run the gradlew clean run prompt because I don't think the build is accounting for when I changed the location of the viewer.html file.

12:12 PM
As far as I know, when I run the application through .\gradlew clean run, everything works fine, but if I hit the VS code run button, everything will run the same, but it will print that the viewer.html file was not found. The information I've found has said that the getResource method I run only checks the runtime classpath, and the classpath consists of the class files, resource files, and dependency files. Apparently, when I have gradle run the application, it copies over any resource files, but when VS code runs the application, it does not copy over the resource files, which means that the viewer.html file won't be found.

Questions:
5/30 2:01 PM
For the musicXML file java objects, why are so many of the things lists? Like why is it attributes.getTime().add(time);, why is there a list of time and all these other attributes if we only need one?

 Why are these all set instead of get like the other lists?             
            note.setPitch(pitch);
            pitch.setStep(Step.E);
            pitch.setOctave(4);

Understand enumerations fully and this
     public final class Level extends java.lang.Enum<Level> {
    // These are standard, public static variables!
    public static final Level LOW = new Level("LOW", 0);
    public static final Level MEDIUM = new Level("MEDIUM", 1);
    public static final Level HIGH = new Level("HIGH", 2);
}
