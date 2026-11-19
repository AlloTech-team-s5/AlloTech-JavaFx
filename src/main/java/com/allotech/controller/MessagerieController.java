package com.allotech.controller;

import com.allotech.model.Conversation;
import com.allotech.model.Message;
import com.allotech.model.Offre;
import com.allotech.service.MessagerieService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class MessagerieController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private ListView<Conversation> listConversations;
    @FXML private Label lblConversationActive;
    @FXML private Label lblPrixConvenu;
    @FXML private TextField txtMontantOffre;
    @FXML private Label lblOffreActive;
    @FXML private ListView<String> listMessages;
    @FXML private TextField txtMessage;

    private final MessagerieService messagerieService = new MessagerieService();
    private int utilisateurConnecteId;
    private Conversation conversationCourante;
    private Offre offreActiveCourante;

    @FXML
    public void initialize() {
        listConversations.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Conversation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Conversation #" + item.getId() + " (Demande #" + item.getDemandeId() + ")");
                }
            }
        });

        listConversations.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectionnerConversation(newVal);
            }
        });
    }

    public void initUtilisateur(int utilisateurId) {
        this.utilisateurConnecteId = utilisateurId;
        List<Conversation> conversations = messagerieService.listerConversationsUtilisateur(utilisateurId);
        listConversations.setItems(FXCollections.observableArrayList(conversations));
        if (!conversations.isEmpty()) {
            listConversations.getSelectionModel().selectFirst();
        }
    }

    private void selectionnerConversation(Conversation conversation) {
        this.conversationCourante = conversation;
        lblConversationActive.setText("Conversation #" + conversation.getId() + " • Demande #" + conversation.getDemandeId());
        messagerieService.marquerMessagesLus(conversation.getId(), utilisateurConnecteId);
        rafraichirMessages();
        rafraichirOffres();
    }

    private void rafraichirMessages() {
        if (conversationCourante == null) {
            return;
        }
        List<Message> messages = messagerieService.listerMessages(conversationCourante.getId());
        List<String> lignes = messages.stream()
                .map(m -> {
                    String heure = m.getDateEnvoi() != null ? m.getDateEnvoi().format(FORMATTER) : "";
                    String auteur = (m.getExpediteurId() == utilisateurConnecteId)
                            ? "Moi"
                            : ("Utilisateur #" + m.getExpediteurId());
                    return "[" + heure + "] " + auteur + " : " + m.getContenu();
                })
                .toList();
        listMessages.setItems(FXCollections.observableArrayList(lignes));
    }

    private void rafraichirOffres() {
        if (conversationCourante == null) {
            return;
        }
        List<Offre> offres = messagerieService.listerOffresConversation(conversationCourante.getId());
        Optional<Offre> acceptee = offres.stream()
                .filter(o -> o.getStatut() != null && "ACCEPTEE".equals(o.getStatut().name()))
                .findFirst();

        if (acceptee.isPresent()) {
            lblPrixConvenu.setText(String.format(Locale.US, "Prix convenu : %.2f DH", acceptee.get().getMontant()));
        } else {
            lblPrixConvenu.setText("Prix convenu : —");
        }

        Optional<Offre> active = messagerieService.trouverOffreActive(conversationCourante.getId());
        if (active.isPresent()) {
            this.offreActiveCourante = active.get();
            lblOffreActive.setText(String.format(Locale.US, "Offre active : %.2f DH", offreActiveCourante.getMontant()));
        } else {
            this.offreActiveCourante = null;
            lblOffreActive.setText("Aucune offre active");
        }
    }

    @FXML
    private void envoyerMessage() {
        if (conversationCourante == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Conversation requise", "Sélectionnez une conversation.");
            return;
        }
        try {
            messagerieService.envoyerMessage(conversationCourante.getId(), utilisateurConnecteId, txtMessage.getText());
            txtMessage.clear();
            rafraichirMessages();
        } catch (IllegalArgumentException e) {
            afficherAlerte(Alert.AlertType.WARNING, "Message invalide", e.getMessage());
        } catch (RuntimeException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void proposerOffre() {
        if (conversationCourante == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Conversation requise", "Sélectionnez une conversation.");
            return;
        }
        try {
            double montant = Double.parseDouble(txtMontantOffre.getText().trim().replace(',', '.'));
            messagerieService.proposerOffre(conversationCourante.getId(), montant);
            txtMontantOffre.clear();
            rafraichirOffres();
            afficherAlerte(Alert.AlertType.INFORMATION, "Offre envoyée", "Votre offre de prix a été proposée.");
        } catch (NumberFormatException e) {
            afficherAlerte(Alert.AlertType.WARNING, "Montant invalide", "Veuillez saisir un montant numérique valide en DH.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            afficherAlerte(Alert.AlertType.WARNING, "Offre", e.getMessage());
        } catch (RuntimeException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void accepterOffre() {
        if (offreActiveCourante == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Aucune offre", "Aucune offre en attente à accepter.");
            return;
        }
        try {
            messagerieService.accepterOffre(offreActiveCourante.getId());
            rafraichirOffres();
            afficherAlerte(Alert.AlertType.INFORMATION, "Offre acceptée", "Le prix convenu a été enregistré.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            afficherAlerte(Alert.AlertType.WARNING, "Offre", e.getMessage());
        } catch (RuntimeException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void refuserOffre() {
        if (offreActiveCourante == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Aucune offre", "Aucune offre en attente à refuser.");
            return;
        }
        try {
            messagerieService.refuserOffre(offreActiveCourante.getId());
            rafraichirOffres();
            afficherAlerte(Alert.AlertType.INFORMATION, "Offre refusée", "L'offre a été refusée.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            afficherAlerte(Alert.AlertType.WARNING, "Offre", e.getMessage());
        } catch (RuntimeException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}