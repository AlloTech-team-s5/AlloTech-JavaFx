package com.allotech.controller;

import com.allotech.model.Avis;
import com.allotech.service.AvisService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;

import java.util.List;

public class AvisClientController {

    public record DemandeAccepteeItem(int demandeId, int technicienId, String libelle) {
        @Override
        public String toString() {
            return libelle;
        }
    }

    @FXML private ComboBox<DemandeAccepteeItem> comboDemandesAcceptees;
    @FXML private Button btnNote1;
    @FXML private Button btnNote2;
    @FXML private Button btnNote3;
    @FXML private Button btnNote4;
    @FXML private Button btnNote5;
    @FXML private Label lblNoteSelectionnee;
    @FXML private TextArea txtCommentaire;
    @FXML private ListView<String> listAvisDonnes;

    private final AvisService avisService = new AvisService();
    private int clientConnecteId;
    private int noteSelectionnee = 5;

    @FXML
    public void initialize() {
        comboDemandesAcceptees.setConverter(new StringConverter<>() {
            @Override
            public String toString(DemandeAccepteeItem item) {
                return item == null ? "" : item.libelle();
            }

            @Override
            public DemandeAccepteeItem fromString(String string) {
                return null;
            }
        });
        mettreAJourAffichageEtoiles();
    }

    public void initClient(int clientId, List<DemandeAccepteeItem> demandesAcceptees) {
        this.clientConnecteId = clientId;
        comboDemandesAcceptees.setItems(FXCollections.observableArrayList(demandesAcceptees));
        if (!demandesAcceptees.isEmpty()) {
            comboDemandesAcceptees.getSelectionModel().selectFirst();
        }
        chargerAvisDonnes();
    }

    @FXML private void selectionnerNote1() { noteSelectionnee = 1; mettreAJourAffichageEtoiles(); }
    @FXML private void selectionnerNote2() { noteSelectionnee = 2; mettreAJourAffichageEtoiles(); }
    @FXML private void selectionnerNote3() { noteSelectionnee = 3; mettreAJourAffichageEtoiles(); }
    @FXML private void selectionnerNote4() { noteSelectionnee = 4; mettreAJourAffichageEtoiles(); }
    @FXML private void selectionnerNote5() { noteSelectionnee = 5; mettreAJourAffichageEtoiles(); }

    private void mettreAJourAffichageEtoiles() {
        lblNoteSelectionnee.setText(noteSelectionnee + " / 5");
        Button[] boutons = {btnNote1, btnNote2, btnNote3, btnNote4, btnNote5};
        for (int i = 0; i < boutons.length; i++) {
            boolean active = (i + 1) <= noteSelectionnee;
            boutons[i].setStyle(active
                    ? "-fx-background-color: #4A1D4A; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;"
                    : "-fx-background-color: #F6D5F7; -fx-text-fill: #351C4D; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void publierAvis() {
        DemandeAccepteeItem demande = comboDemandesAcceptees.getValue();
        if (demande == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Sélection requise", "Veuillez choisir une demande acceptée.");
            return;
        }
        try {
            avisService.ajouterAvis(
                    demande.demandeId(),
                    clientConnecteId,
                    demande.technicienId(),
                    noteSelectionnee,
                    txtCommentaire.getText()
            );
            txtCommentaire.clear();
            afficherAlerte(Alert.AlertType.INFORMATION, "Avis publié",
                    "Votre avis a été enregistré et le score du technicien a été mis à jour.");
            chargerAvisDonnes();
        } catch (IllegalArgumentException | IllegalStateException e) {
            afficherAlerte(Alert.AlertType.WARNING, "Validation", e.getMessage());
        } catch (RuntimeException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void chargerAvisDonnes() {
        if (clientConnecteId <= 0) {
            return;
        }
        List<Avis> avisList = avisService.listerAvisClient(clientConnecteId);
        List<String> lignes = avisList.stream()
                .map(a -> "Demande #" + a.getDemandeId()
                        + " • Technicien #" + a.getTechnicienId()
                        + " • " + "★".repeat(a.getNote()) + " (" + a.getNote() + "/5)"
                        + (a.getCommentaire() != null ? " — " + a.getCommentaire() : ""))
                .toList();
        listAvisDonnes.setItems(FXCollections.observableArrayList(lignes));
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}