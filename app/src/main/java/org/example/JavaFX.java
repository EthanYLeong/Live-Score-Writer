package org.example;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.concurrent.Worker;

public class JavaFX extends Application {

    private WebView webView;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(createContent(), 950, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private WebView createContent(){
        this.webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        java.net.URL templateUrl = getClass().getResource("/viewer.html");

        if (templateUrl != null){
            webEngine.load(templateUrl.toExternalForm());
        } else {
            System.out.println("Error: Could not find viewer.html in resources folder !!");
System.out.println(getClass().getResource("/viewer.html"));        }

    webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
        if (newState == Worker.State.SUCCEEDED){
            System.out.println("page open");
            String mockMusicXml = "<?xml version='1.0' encoding='UTF-8'?><score-partwise></score-partwise>";
            webEngine.executeScript("renderMusicXmlFromJava(`" + mockMusicXml + "`)");        }
    });
            return webView;

}
}
