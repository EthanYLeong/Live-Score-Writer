package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.concurrent.Worker;
import org.audiveris.proxymusic.Attributes;
import org.audiveris.proxymusic.Time;

import be.tarsos.dsp.resample.SoundTouchRateTransposer;

public class JavaFX extends Application {

    private WebView webView;
    private WebEngine webEngine;
    private Button startButton;
    private VBox mainLayout;
    private TextField bpmField;
    private ComboBox<Integer> divisionsBox;
    private ComboBox<String> timeSignatureBox;
    private Label bpmLabel;
    private Label timeSignatureLabel;
    private Label divisionsLabel;
    private HBox secondRow;
    private static JavaFX instance;
    private Metronome metronome = new Metronome();
    private AudioTranscriberYIN audioTranscriber = new AudioTranscriberYIN();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        startButton = new Button("Start Recording");
        startButton.setOnAction(event -> handleStartTrigger());

        bpmField = new TextField("60");
        bpmField.textProperty().addListener((obs, oldValue, newValue) -> {
            try {
                double bpm = Double.parseDouble(newValue);
                metronome.setBpm(bpm);
                audioTranscriber.setBpm(bpm);
            } catch (Exception e) {
                System.out.println("MUST PASS IN VALID NUMBER");
            }
        });

        timeSignatureBox = new ComboBox<>();
        timeSignatureBox.getItems().addAll("2/4", "3/4", "4/4", "5/4", "3/8", "6/8", "9/8");
        timeSignatureBox.setValue("4/4");
        timeSignatureBox.setOnAction(event -> {
            audioTranscriber.updateTimeSignature(timeSignatureBox.getValue());
            updateDivisionsField();
        });

        divisionsBox = new ComboBox<>();
        divisionsBox.getItems().addAll(1, 2, 4);
        divisionsBox.setValue(4);
        divisionsBox.setOnAction(event -> {
            if (divisionsBox.getValue() != null)
                audioTranscriber.updateDivisions(divisionsBox.getValue());
        });

        bpmLabel = new Label("BPM");
        timeSignatureLabel = new Label("Time Signature");
        divisionsLabel = new Label("Divisions");

        mainLayout = new VBox(10);
        secondRow = new HBox(10);
        startButton.setPrefSize(200, 40);
        bpmField.setMaxSize(200, 40);
        secondRow.getChildren().addAll(bpmLabel, bpmField, timeSignatureLabel, timeSignatureBox, divisionsLabel,
                divisionsBox);

        instance = this;
        WebView visualBrowser = createContent();
        mainLayout.getChildren().addAll(startButton, secondRow, visualBrowser);
        Scene scene = new Scene(mainLayout, 950, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private WebView createContent() {
        this.webView = new WebView();
        this.webEngine = webView.getEngine();
        java.net.URL templateUrl = getClass().getResource("/viewer.html");

        if (templateUrl != null) {
            webEngine.load(templateUrl.toExternalForm());
        } else {
            System.out.println("Error: Could not find viewer.html in resources folder !!");
            System.out.println(JavaFX.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation());
        }
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                System.out.println("page open");
                String mockMusicXml = audioTranscriber.getInitialXMLString();
                loadMusicXmlFile(mockMusicXml);
            }
        });
        return webView;

    }

    public void loadMusicXmlFile(String musicXml) {
        Platform.runLater(() -> webEngine.executeScript("renderMusicXmlFromJava(`" + musicXml + "`)"));
    }

    public static JavaFX getInstance() {
        return instance;
    }

    private void handleStartTrigger() {
        startButton.setDisable(true);
        startButton.setText("Recording Active");

        metronome.start();
        audioTranscriber.start();
    }

    private void updateDivisionsField() {
        System.out.println("START");
        if (AudioTranscriberYIN.timeSignatureDenominator.equals("4")) {
            divisionsBox.getItems().clear();
            divisionsBox.getItems().addAll(1, 2, 4);
            divisionsBox.setValue(1);
        } else if (AudioTranscriberYIN.timeSignatureDenominator.equals("8")) {
            divisionsBox.getItems().clear();
            divisionsBox.getItems().addAll(1, 2);
            divisionsBox.setValue(1);
        }
        System.out.println("END");
    }

}
