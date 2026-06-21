package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.concurrent.Worker;

public class JavaFX extends Application {

    private WebView webView;
    private WebEngine webEngine;
    private Button startButton;
    private VBox mainLayout;
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
        mainLayout = new VBox(10);
        startButton.setPrefSize(200, 40);
        instance = this;
        WebView visualBrowser = createContent();
        mainLayout.getChildren().addAll(startButton, visualBrowser);
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

}
