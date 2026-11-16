package com.allotech;

import com.allotech.util.AlertUtil;
import com.allotech.util.DBConnection;
import com.allotech.util.DataAccessException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Point d'entree de l'application (lancement : {@code mvn javafx:run}).
 *
 * <p><b>Version PROVISOIRE ecrite par P1 (iteration 2)</b> : elle verifie la connexion a MySQL
 * puis affiche l'ecran "Gestion des categories", pour pouvoir le tester.
 * P2 la remplacera par l'ecran de connexion et la navigation (SceneNavigator).</p>
 */
public class MainApp extends Application {

    @Override
    public void start(Stage fenetre) throws IOException {
        if (!verifierBaseDeDonnees()) {
            Platform.exit(); // le message d'erreur a deja ete affiche
            return;
        }

        Parent ecran = FXMLLoader.load(MainApp.class.getResource("/fxml/admin/categories_panel.fxml"));
        fenetre.setTitle("AlloTech - Gestion des catégories (version provisoire P1)");
        fenetre.setScene(new Scene(ecran, 700, 560));
        fenetre.show();
    }

    /** Teste MySQL avant d'ouvrir l'ecran, avec un message clair en cas de probleme. */
    private boolean verifierBaseDeDonnees() {
        try (Connection cnx = DBConnection.getConnection()) {
            return cnx.isValid(2);
        } catch (DataAccessException e) {
            AlertUtil.erreur(e);
        } catch (IllegalStateException e) {
            // config.properties absent ou cle manquante
            AlertUtil.erreur(e.getMessage());
        } catch (SQLException e) {
            AlertUtil.erreur("Erreur de base de données : " + e.getMessage());
        }
        return false;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
