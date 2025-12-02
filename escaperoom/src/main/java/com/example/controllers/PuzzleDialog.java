package com.example.controllers;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Minimal PuzzleDialog helper. Replace this with your real Puzzle UI.
 */
public class PuzzleDialog {
    public static void showPuzzle(String puzzleId) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Puzzle: " + puzzleId);

        StackPane root = new StackPane(new Label("Puzzle UI not implemented: " + puzzleId));
        Scene s = new Scene(root, 400, 200);
        stage.setScene(s);
        stage.showAndWait();
    }
}
