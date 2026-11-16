package com.allotech.controller;

import com.allotech.model.Categorie;
import com.allotech.service.CategorieService;
import com.allotech.util.AlertUtil;
import com.allotech.util.DataAccessException;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controleur du panneau "Gestion des categories" (categories_panel.fxml).
 * Il ne contient AUCUN SQL : il appelle seulement {@link CategorieService}.
 */
public class CategorieController {

    @FXML private TextField txtRecherche;
    @FXML private TableView<Categorie> tableCategories;
    @FXML private TableColumn<Categorie, Integer> colId;
    @FXML private TableColumn<Categorie, String> colNom;
    @FXML private TableColumn<Categorie, String> colStatut;
    @FXML private TextField txtNom;
    @FXML private CheckBox chkActif;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnBasculer;
    @FXML private Button btnSupprimer;
    @FXML private Label lblMessage;

    private final CategorieService service = new CategorieService();
    private final ObservableList<Categorie> donnees = FXCollections.observableArrayList();

    /** Appelee automatiquement par JavaFX apres le chargement du FXML. */
    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        colStatut.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().isActif() ? "Actif" : "Inactif"));
        tableCategories.setItems(donnees);

        tableCategories.getSelectionModel().selectedItemProperty()
                .addListener((obs, ancienne, nouvelle) -> afficherSelection(nouvelle));
        txtRecherche.textProperty().addListener((obs, ancien, nouveau) -> rafraichir(null));

        afficherSelection(null);
        rafraichir(null);
    }

    // ===================================================================
    //  ACTIONS DES BOUTONS
    // ===================================================================

    @FXML
    private void ajouter() {
        executer(() -> {
            Categorie ajoutee = service.ajouter(txtNom.getText());
            rafraichir(ajoutee.getId());
            succes("Catégorie « " + ajoutee.getNom() + " » ajoutée.");
        });
    }

    @FXML
    private void modifier() {
        Categorie choisie = tableCategories.getSelectionModel().getSelectedItem();
        if (choisie == null) {
            AlertUtil.info("Sélectionnez d'abord une catégorie dans le tableau.");
            return;
        }
        executer(() -> {
            Categorie modifiee = service.modifier(choisie.getId(), txtNom.getText(), chkActif.isSelected());
            rafraichir(modifiee.getId());
            succes("Catégorie « " + modifiee.getNom() + " » modifiée.");
        });
    }

    @FXML
    private void basculerActif() {
        Categorie choisie = tableCategories.getSelectionModel().getSelectedItem();
        if (choisie == null) {
            AlertUtil.info("Sélectionnez d'abord une catégorie dans le tableau.");
            return;
        }
        executer(() -> {
            if (choisie.isActif()) {
                service.desactiver(choisie.getId());
                succes("Catégorie « " + choisie.getNom() + " » désactivée.");
            } else {
                service.activer(choisie.getId());
                succes("Catégorie « " + choisie.getNom() + " » activée.");
            }
            rafraichir(choisie.getId());
        });
    }

    @FXML
    private void supprimer() {
        Categorie choisie = tableCategories.getSelectionModel().getSelectedItem();
        if (choisie == null) {
            AlertUtil.info("Sélectionnez d'abord une catégorie dans le tableau.");
            return;
        }
        if (!AlertUtil.confirmer("Supprimer la catégorie « " + choisie.getNom() + " » ?")) {
            return;
        }
        executer(() -> {
            boolean supprimee = service.supprimerOuDesactiver(choisie.getId());
            if (supprimee) {
                rafraichir(null);
                succes("Catégorie « " + choisie.getNom() + " » supprimée.");
            } else {
                rafraichir(choisie.getId());
                succes("Catégorie « " + choisie.getNom()
                        + " » utilisée par des techniciens : elle a été désactivée au lieu d'être supprimée.");
            }
        });
    }

    @FXML
    private void nouveau() {
        tableCategories.getSelectionModel().clearSelection();
        afficherSelection(null);
        lblMessage.setText("");
    }

    // ===================================================================
    //  OUTILS
    // ===================================================================

    /** Recharge le tableau (avec le filtre de recherche) et re-selectionne une ligne si demande. */
    private void rafraichir(Integer idASelectionner) {
        executer(() -> {
            donnees.setAll(service.rechercher(txtRecherche.getText()));
            if (idASelectionner != null) {
                donnees.stream()
                        .filter(c -> c.getId() == idASelectionner)
                        .findFirst()
                        .ifPresent(c -> tableCategories.getSelectionModel().select(c));
            }
        });
    }

    /** Remplit le formulaire avec la ligne choisie (ou le vide), et adapte les boutons. */
    private void afficherSelection(Categorie categorie) {
        boolean aucune = categorie == null;
        txtNom.setText(aucune ? "" : categorie.getNom());
        chkActif.setSelected(aucune || categorie.isActif());
        btnModifier.setDisable(aucune);
        btnSupprimer.setDisable(aucune);
        btnBasculer.setDisable(aucune);
        btnBasculer.setText(!aucune && !categorie.isActif() ? "Activer" : "Désactiver");
        btnAjouter.setDisable(!aucune);
    }

    private void succes(String message) {
        lblMessage.setStyle("-fx-text-fill: #2e7d32;");
        lblMessage.setText(message);
    }

    /** Execute une action en transformant les erreurs en fenetres d'erreur claires. */
    private void executer(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException e) {
            AlertUtil.erreur(e.getMessage());
        } catch (DataAccessException e) {
            AlertUtil.erreur(e);
        }
    }
}
