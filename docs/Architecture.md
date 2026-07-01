The ultimate goal of this project is to process raw audio captured from a microphone into written sheet music notation. There is a metronome that you play along with, and it captures the audio based on every beat into sheet music.

Below contains the general pipeline flow of the project.

Microphone -> Audio Capture -> Amplitude Threshold -> YIN Pitch Calculation -> Closest Note Calculation -> Most Common Note (of 12 samples) -> Measure Construction -> MusicXML Generation -> Updating Music Display

(Technically in App)

This project starts in the JavaFX file, which handles the UI displayed and is the connection point where the Metronome and AudioTranscriber files are created and used. A WebView page is created and displayed, and an html file located in this project is loaded onto the WebView page. This html file contains a method that is able to take in strings that represent MusicXML files and load and render them visually.

Once recording has started, the TargetDataLine will open, meaning audio from the microphone will be processed. Audio is captured at 49,152 samples per second, where each sample represents the amplitude of the audio at one singular point in time. We collect audio data in the form of a byte array of a size of 2048 bytes or 1024 samples. This ultimately means we iterate through the code 48 times. There are 8 bits for every 1 byte, and because 16 bits represent each sample, that means the data of our singular sample is split across 2 bytes. Through bit manipulation, we're able to use the data from 2 the bytes to recreate the sample in the form of a float, ultimately turning the byte array into a float array. As previously mentioned, each float represents an amplitude of the audio. We only want the code to find a note if there is enough volume, so we check to see if this window of samples contains an amplitude of a certain value. If there is enough amplitude, then the array float is passed into YIN, a pitch tracking algorithm that returns the pitch of the given window of samples. Afterwards, we're able to calculate the corresponding note letter and octave from the pitch and assign this data to a Note object (which corresponds to a MusicXML element).

We can now capture what note is played for each window, where there are 48 windows a second. However, the foundation for the rhythm of the sheet music we construct will be based on the subdivisions, like eigth notes or sixteenth notes. This means we want to know what the most prominent note is for the duration of the smallest subdivision is (which can span from like 0.250 seconds to 1 second), not just a window which is 48 milliseconds.



Based on the beats per minute, time signature, and divisions (number of divisions per quarter note), we're able to figure out how much time passes for the smallest subdivision, which 