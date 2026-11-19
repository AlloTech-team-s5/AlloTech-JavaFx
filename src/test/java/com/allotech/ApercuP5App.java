package com.allotech;

import com.allotech.controller.AvisClientController;
import com.allotech.controller.AvisClientController.DemandeAccepteeItem;
import com.allotech.controller.AvisTechnicienController;
import com.allotech.controller.MessagerieController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.util.List;

public class ApercuP5App extends Application {

    private static final int CLIENT_ID = 2; // ou 3
    private static final int TECHNICIEN_ID = 4;
    private static final int DEMANDE_ACCEPTEE_ID = 1;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader msgLoader = new FXMLLoader(getClass().getResource("/fxml/client/messagerie_client.fxml"));
        Parent msgRoot = msgLoader.load();
        MessagerieController msgCtrl = msgLoader.getController();
        msgCtrl.initUtilisateur(CLIENT_ID);

        FXMLLoader avisClientLoader = new FXMLLoader(getClass().getResource("/fxml/client/avis_client.fxml"));
        Parent avisClientRoot = avisClientLoader.load();
        AvisClientController avisClientCtrl = avisClientLoader.getController();
        avisClientCtrl.initClient(
                CLIENT_ID,
                List.of(new DemandeAccepteeItem(
                        DEMANDE_ACCEPTEE_ID,
                        TECHNICIEN_ID,
                        "Demande #" + DEMANDE_ACCEPTEE_ID + " (Technicien #" + TECHNICIEN_ID + ")"
                ))
        );

        FXMLLoader avisTechLoader = new FXMLLoader(getClass().getResource("/fxml/technicien/avis_technicien.fxml"));
        Parent avisTechRoot = avisTechLoader.load();
        AvisTechnicienController avisTechCtrl = avisTechLoader.getController();
        avisTechCtrl.initTechnicien(TECHNICIEN_ID);

        TabPane tabs = new TabPane(
                new Tab("Messagerie & Offres", msgRoot),
                new Tab("Avis Client", avisClientRoot),
                new Tab("Avis Technicien", avisTechRoot)
        );
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        stage.setTitle("AlloTech — Aperçu Module P5");
        stage.setScene(new Scene(tabs, 980, 620));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}