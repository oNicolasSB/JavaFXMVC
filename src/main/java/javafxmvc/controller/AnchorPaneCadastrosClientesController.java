package javafxmvc.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafxmvc.model.dao.ClienteDAO;
import javafxmvc.model.database.Database;
import javafxmvc.model.database.DatabaseFactory;
import javafxmvc.model.domain.Cliente;

public class AnchorPaneCadastrosClientesController implements Initializable {

    @FXML
    private TableView<Cliente> tableViewClientes;
    @FXML
    private TableColumn<Cliente, String> tableColumnClienteNome;
    @FXML
    private TableColumn<Cliente, String> tableColumnClienteCPF;
    @FXML
    private Button buttonInserir;
    @FXML
    private Button buttonAlterar;
    @FXML
    private Button buttonRemover;
    @FXML
    private Label labelClienteCodigo;
    @FXML
    private Label labelClienteNome;
    @FXML
    private Label labelClienteCPF;
    @FXML
    private Label labelClienteTelefone;

    private List<Cliente> listClientes;
    private ObservableList<Cliente> observableListClientes;

    private final Database database = DatabaseFactory.getDatabase("postgresql");
    private final Connection connection = database.conectar();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        clienteDAO.setConnection(connection);
        carregarTableViewCliente();

        tableViewClientes.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> selecionarItemTableViewClientes(newValue));
    }

    private void selecionarItemTableViewClientes(Cliente cliente) {
        if (cliente != null) {

            labelClienteCodigo.setText(String.valueOf(cliente.getCdCliente()));
            labelClienteNome.setText(cliente.getNome());
            labelClienteCPF.setText(cliente.getCpf());
            labelClienteTelefone.setText(cliente.getTelefone());
        } else {
            labelClienteCodigo.setText("");
            labelClienteNome.setText("");
            labelClienteCPF.setText("");
            labelClienteTelefone.setText("");
        }
    }

    public void carregarTableViewCliente() {
        tableColumnClienteNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        tableColumnClienteCPF.setCellValueFactory(new PropertyValueFactory<>("cpf"));

        listClientes = clienteDAO.listar();

        observableListClientes = FXCollections.observableArrayList(listClientes);
        tableViewClientes.setItems(observableListClientes);
    }

    @FXML
    public void handleButtonInserir() throws IOException {
        var cliente = new Cliente();

        boolean buttonConfirmarClicked = showFXMLAnchorPaneCadastrosClientesDialog(cliente);

        if (buttonConfirmarClicked) {
            clienteDAO.inserir(cliente);
            carregarTableViewCliente();
        }
    }

    @FXML
    public void handleButtonAlterar() throws IOException {
        var cliente = tableViewClientes.getSelectionModel().getSelectedItem();

        if (cliente != null) {
            boolean buttonConfirmarClicked = showFXMLAnchorPaneCadastrosClientesDialog(cliente);
            if (buttonConfirmarClicked) {
                clienteDAO.alterar(cliente);
                carregarTableViewCliente();
            }
        } else {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Selecione um cliente");
            alert.show();
        }
    }

    @FXML
    public void handleButtonRemover() throws IOException {
        var cliente = tableViewClientes.getSelectionModel().getSelectedItem();

        if (cliente != null) {
            clienteDAO.remover(cliente);
            carregarTableViewCliente();
        } else {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Selecione um cliente");
            alert.show();
        }
    }

    private boolean showFXMLAnchorPaneCadastrosClientesDialog(Cliente cliente) throws IOException {

        var loader = new FXMLLoader();
        loader.setLocation(AnchorPaneCadastrosClientesDialogController.class
                .getResource("/javafxmvc/view/AnchorPaneCadastrosClientesDialog.fxml"));
        var page = (AnchorPane) loader.load();

        var dialogStage = new Stage();

        dialogStage.setTitle("Cadastro de cliente");
        var scene = new Scene(page);
        dialogStage.setScene(scene);

        AnchorPaneCadastrosClientesDialogController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setCliente(cliente);

        dialogStage.showAndWait();

        return controller.isButtonConfirmarClicked();
    }

}
