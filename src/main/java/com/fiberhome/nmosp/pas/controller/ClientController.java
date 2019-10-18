/**
 * Sample Skeleton for 'Client.fxml' Controller Class
 */
package com.fiberhome.nmosp.pas.controller;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ClientController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="btnQuery"
    private Button btnQuery; // Value injected by FXMLLoader

    @FXML
    void onbtnQueryClick(ActionEvent event) {

    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert btnQuery != null : "fx:id=\"btnQuery\" was not injected: check your FXML file 'Client.fxml'.";

    }
}
