package javafxmvc.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;

public class VBoxMainController implements Initializable {

    @FXML
    private MenuItem menuItemCadastrosClientes;
    @FXML
    private MenuItem menuItemProcessosVendas;
    @FXML
    private MenuItem menuItemGraficosVendas;
    @FXML
    private MenuItem menuItemRelatorioQuantidadeProdutosEstoque;
    @FXML
    private AnchorPane anchorPane;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'initialize'");
    }

    @FXML
    public void handleMenuItemCadastrosCliente() throws IOException {
        AnchorPane a = (AnchorPane) FXMLLoader
                .load(getClass().getResource("/javafxmvc/view/AnchorPaneCadastrosClientes.fxml"));
        anchorPane.getChildren().setAll(a);
    }

}
