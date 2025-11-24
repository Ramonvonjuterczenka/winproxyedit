package com.proxy;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class ProxyManagerController {

    @FXML
    private TextField systemProxyTextField;
    @FXML
    private TextArea systemProxyExceptionsTextArea;
    @FXML
    private TextField systemProxyAutoScriptUrlTextField;
    @FXML
    private TextField winHttpProxyTextField;
    @FXML
    private TextField gitGlobalHttpProxyTextField;
    @FXML
    private TextField gitGlobalHttpsProxyTextField;
    @FXML
    private TextField gitLocalHttpProxyTextField;
    @FXML
    private TextField gitLocalHttpsProxyTextField;
    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        loadProxySettings();
    }

    @FXML
    private void loadProxySettings() {
        try {
            updateStatus("Lade Einstellungen...", Color.BLACK);
            systemProxyTextField.setText(ProxyUtils.getSystemProxyServer());
            systemProxyExceptionsTextArea.setText(ProxyUtils.getSystemProxyExceptions());
            systemProxyAutoScriptUrlTextField.setText(ProxyUtils.getSystemAutoConfigUrl());
            winHttpProxyTextField.setText(ProxyUtils.getWinHttpProxy());
            gitGlobalHttpProxyTextField.setText(ProxyUtils.getGitProxy("global", "http"));
            gitGlobalHttpsProxyTextField.setText(ProxyUtils.getGitProxy("global", "https"));
            gitLocalHttpProxyTextField.setText(ProxyUtils.getGitProxy("local", "http"));
            gitLocalHttpsProxyTextField.setText(ProxyUtils.getGitProxy("local", "https"));
            updateStatus("Einstellungen erfolgreich geladen.", Color.GREEN);
        } catch (ProxyException e) {
            updateStatus("Fehler beim Laden der Einstellungen: " + e.getMessage(), Color.RED);
        }
    }

    @FXML
    private void saveProxySettings() {
        try {
            updateStatus("Speichere Einstellungen...", Color.BLACK);
            ProxyUtils.setSystemProxy(systemProxyTextField.getText(), systemProxyExceptionsTextArea.getText());
            ProxyUtils.setSystemAutoConfigUrl(systemProxyAutoScriptUrlTextField.getText());
            ProxyUtils.setWinHttpProxy(winHttpProxyTextField.getText());
            ProxyUtils.setGitProxy("global", "http", gitGlobalHttpProxyTextField.getText());
            ProxyUtils.setGitProxy("global", "https", gitGlobalHttpsProxyTextField.getText());
            ProxyUtils.setGitProxy("local", "http", gitLocalHttpProxyTextField.getText());
            ProxyUtils.setGitProxy("local", "https", gitLocalHttpsProxyTextField.getText());
            updateStatus("Einstellungen erfolgreich gespeichert.", Color.GREEN);
        } catch (ProxyException e) {
            updateStatus("Fehler beim Speichern der Einstellungen: " + e.getMessage(), Color.RED);
        }
    }

    private void updateStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setTextFill(color);
    }
}
