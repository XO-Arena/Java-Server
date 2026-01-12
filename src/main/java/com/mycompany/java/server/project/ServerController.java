/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.java.server.project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 *
 * @author ANTER
 */
public class ServerController {

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

    @FXML
    private void handleStart(ActionEvent event) {
    }

    @FXML
    private void handleStop(ActionEvent event) {
    }
    
}
