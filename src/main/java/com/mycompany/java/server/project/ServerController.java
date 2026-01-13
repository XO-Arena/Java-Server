package com.mycompany.java.server.project;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class ServerController implements Initializable {

    @FXML
    private Button startButton;

    @FXML
    private Button stopButton;

    @FXML
    private Label statusIndicator;

    @FXML
    private Label statusLabel;

    @FXML
    private Label onlineCountLabel;

    @FXML
    private Label offlineCountLabel;

    @FXML
    private Label totalCountLabel;

    @FXML
    private PieChart usersPieChart;

    private Server server;
    private Thread serverThread;
    private Timer updateTimer;
    private static final int SERVER_PORT = 4646;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize button states
        stopButton.setDisable(true);
        updateStatus(false);
        updateStatistics();
    }

    @FXML
    private void handleStart(ActionEvent event) {
        if (server == null || !serverThread.isAlive()) {
            // Create and start server
            server = new Server(SERVER_PORT);
            serverThread = new Thread(() -> server.start());
            serverThread.setDaemon(true);
            serverThread.start();

            // Update UI
            startButton.setDisable(true);
            stopButton.setDisable(false);
            updateStatus(true);
            startStatisticsUpdate();

            System.out.println("Server started successfully");
        }
    }

    @FXML
    private void handleStop(ActionEvent event) {

        if (server != null) {
            server.stop();
            stopStatisticsUpdate();

            server = null;
            serverThread = null;

            startButton.setDisable(false);
            stopButton.setDisable(true);
            updateStatus(false);

            System.out.println("Server stopped");
        }
    }

    private void updateStatus(boolean isRunning) {
        Platform.runLater(() -> {
            if (isRunning) {
                statusLabel.setText("Running");
                statusIndicator.setText("●");
                statusIndicator.setStyle("-fx-text-fill: #4CAF50;"); // Green
            } else {
                statusLabel.setText("Stopped");
                statusIndicator.setText("●");
                statusIndicator.setStyle("-fx-text-fill: #F44336;"); // Red
            }
        });
    }

    private void startStatisticsUpdate() {
        updateTimer = new Timer(true);
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateStatistics();
            }
        }, 0, 10000);
    }

    private void stopStatisticsUpdate() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }

    private void updateStatistics() {
        // Get statistics
        int totalUsers = ServerContext.getTotalRegisteredUsers();
        int onlineUsers = ServerContext.getOnlineUsers("").size();
        int offlineUsers = totalUsers - onlineUsers;

        // Update UI on JavaFX thread
        Platform.runLater(() -> {
            // Update labels
            onlineCountLabel.setText(String.valueOf(onlineUsers));
            offlineCountLabel.setText(String.valueOf(offlineUsers));
            totalCountLabel.setText(String.valueOf(totalUsers));

            // Update pie chart
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Online (" + onlineUsers + ")", onlineUsers),
                    new PieChart.Data("Offline (" + offlineUsers + ")", offlineUsers)
            );

            usersPieChart.setData(pieChartData);
            usersPieChart.setLegendVisible(true);
            usersPieChart.setLabelsVisible(true);

            // Apply colors to pie chart
            applyPieChartColors();
        });
    }
    // chart colors 
    private void applyPieChartColors() {
        if (usersPieChart.getData().size() >= 2) {
            // Online
            usersPieChart.getData().get(0).getNode()
                    .setStyle("-fx-pie-color: #22c55e;"); // Green

            // Offline
            usersPieChart.getData().get(1).getNode()
                    .setStyle("-fx-pie-color: #ef4444;"); // Red
        }
    }
}
