package com.allotech.controller;

import com.allotech.model.Avis;
import com.allotech.service.AvisService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.List;
import java.util.Locale;

public class AvisTechnicienController {

    @FXML private Label lblNombreAvis;
    @FXML private Label lblScoreMoyen;
    @FXML private ListView<String> listAvisRecus;

    private final AvisService avisService = new AvisService();

    public void initTechnicien(int technicienId) {
        List<Avis> avisList = avisService.listerAvisTechnicien(technicienId);
        double score = avisService.recalculerScoreTechnicien(technicienId);

        lblNombreAvis.setText(avisList.size() + " avis vérifiés");
        lblScoreMoyen.setText(String.format(Locale.US, "%.2f ★ / 5", score));

        List<String> lignes = avisList.stream()
                .map(a -> "★".repeat(a.getNote()) + " (" + a.getNote() + "/5)"
                        + (a.getDateAvis() != null ? " • " + a.getDateAvis() : "")
                        + (a.getCommentaire() != null ? " — " + a.getCommentaire() : " — Sans commentaire"))
                .toList();
        listAvisRecus.setItems(FXCollections.observableArrayList(lignes));
    }
}