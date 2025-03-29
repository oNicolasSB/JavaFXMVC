package javafxmvc.controller;

import java.io.IOException;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
import javafxmvc.model.dao.ItemDeVendaDAO;
import javafxmvc.model.dao.ProdutoDAO;
import javafxmvc.model.dao.VendaDAO;
import javafxmvc.model.database.Database;
import javafxmvc.model.database.DatabaseFactory;
import javafxmvc.model.domain.Cliente;
import javafxmvc.model.domain.ItemDeVenda;
import javafxmvc.model.domain.Produto;
import javafxmvc.model.domain.Venda;

public class AnchorPaneProcessosVendasController implements Initializable {

    @FXML
    private TableView<Venda> tableViewVendas;
    @FXML
    private TableColumn<Venda, Integer> tableColumnVendaCodigo;
    @FXML
    private TableColumn<Venda, LocalDate> tableColumnVendaData;
    @FXML
    private TableColumn<Venda, Venda> tableColumnVendaCliente;
    @FXML
    private Button buttonInserir;
    @FXML
    private Button buttonAlterar;
    @FXML
    private Button buttonRemover;
    @FXML
    private Label labelVendaCodigo;
    @FXML
    private Label labelVendaData;
    @FXML
    private Label labelVendaValor;
    @FXML
    private Label labelVendaPago;
    @FXML
    private Label labelVendaCliente;

    private List<Venda> listVendas;
    private ObservableList<Venda> observableListVendas;

    // database:
    private final Database database = DatabaseFactory.getDatabase("postgresql");
    private final Connection connection = database.conectar();
    private final VendaDAO vendaDAO = new VendaDAO();
    private final ItemDeVendaDAO itemDeVendaDAO = new ItemDeVendaDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        vendaDAO.setConnection(connection);
        carregarTableViewVendas();

        selecionarItemTableViewVendas(null);

        tableViewVendas.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> selecionarItemTableViewVendas(newValue));
    }

    private void carregarTableViewVendas() {
        tableColumnVendaCodigo.setCellValueFactory(new PropertyValueFactory<>("cdVenda"));
        tableColumnVendaData.setCellValueFactory(new PropertyValueFactory<>("data"));
        tableColumnVendaCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));

        listVendas = vendaDAO.listar();

        observableListVendas = FXCollections.observableArrayList(listVendas);
        tableViewVendas.setItems(observableListVendas);
    }

    private void selecionarItemTableViewVendas(Venda venda) {
        if (venda != null) {
            labelVendaCodigo.setText(String.valueOf(venda.getCdVenda()));
            labelVendaData.setText(String.valueOf(venda.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            labelVendaValor.setText(String.format("%.2f", venda.getValor()));
            labelVendaPago.setText(String.valueOf(venda.getPago()));
            labelVendaCliente.setText(venda.getCliente().toString());
        } else {
            labelVendaCodigo.setText("");
            labelVendaData.setText("");
            labelVendaValor.setText("");
            labelVendaPago.setText("");
            labelVendaCliente.setText("");
        }
    }

    @FXML
    private void handleButtonInserir() throws IOException, SQLException {
        Venda venda = new Venda();
        boolean buttonConfirmarClicked = showFXMLAnchorPaneProcessosVendasDialog(venda);
        if (buttonConfirmarClicked) {
            try {
                connection.setAutoCommit(false);
                vendaDAO.setConnection(connection);
                vendaDAO.inserir(venda);
                itemDeVendaDAO.setConnection(connection);
                produtoDAO.setConnection(connection);
                for (ItemDeVenda listItemDeVenda : venda.getItensDeVenda()) {
                    Produto produto = listItemDeVenda.getProduto();
                    listItemDeVenda.setVenda(vendaDAO.buscarUltimaVenda());
                    itemDeVendaDAO.inserir(listItemDeVenda);
                    produto.setQuantidade(produto.getQuantidade() -
                            listItemDeVenda.getQuantidade());
                    produtoDAO.alterar(produto);
                }
                connection.commit();
                carregarTableViewVendas();
            } catch (SQLException ex) {
                try {
                    connection.rollback();
                } catch (SQLException ex1) {

                    Logger.getLogger(AnchorPaneProcessosVendasController.class.getName()).log(Level.SEVERE, null, ex1);
                }

                Logger.getLogger(AnchorPaneProcessosVendasController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @FXML
    private void handleButtonAlterar() throws IOException, SQLException {
        // Obtém a venda selecionada na tabela
        Venda venda = tableViewVendas.getSelectionModel().getSelectedItem();
        if (venda != null) {
            // Exibe a janela de diálogo para editar a venda
            boolean buttonConfirmarClicked = showFXMLAnchorPaneProcessosVendasDialog(venda);
            if (buttonConfirmarClicked) {
                try {
                    // Inicia uma transação no banco de dados
                    connection.setAutoCommit(false);
                    vendaDAO.setConnection(connection);
                    itemDeVendaDAO.setConnection(connection);
                    produtoDAO.setConnection(connection);

                    // Atualiza os itens da venda e seus produtos relacionados
                    var teste = venda.getItensDeVenda().stream()
                            .filter(i -> i.getCdItemDeVenda() == 0)
                            .collect(Collectors.toList());

                    for (ItemDeVenda listItemDeVenda : venda.getItensDeVenda().stream()
                            .filter(i -> i.getCdItemDeVenda() == 0)
                            .collect(Collectors.toList())) {
                        Produto produto = listItemDeVenda.getProduto();
                        // Ajusta a quantidade do produto de acordo com a alteração
                        produto.setQuantidade(produto.getQuantidade() - listItemDeVenda.getQuantidade());
                        produtoDAO.alterar(produto);
                        itemDeVendaDAO.inserir(listItemDeVenda);
                    }

                    // Atualiza os dados da venda
                    vendaDAO.alterar(venda);

                    // Finaliza a transação
                    connection.commit();

                    // Recarrega a tabela de vendas
                    carregarTableViewVendas();
                } catch (SQLException ex) {
                    // Reverte a transação em caso de erro
                    connection.rollback();
                    Logger.getLogger(AnchorPaneProcessosVendasController.class.getName()).log(Level.SEVERE, null, ex);

                    // Exibe uma mensagem de erro
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Ocorreu um erro ao alterar a venda. Tente novamente.");
                    alert.show();
                }
            }
        } else {
            // Exibe uma mensagem de erro caso nenhuma venda tenha sido selecionada
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Por favor, escolha uma venda na tabela para alterar.");
            alert.show();
        }
    }

    @FXML
    private void handleButtonRemover() throws IOException, SQLException {
        Venda venda = tableViewVendas.getSelectionModel().getSelectedItem();
        if (venda != null) {
            connection.setAutoCommit(false);
            vendaDAO.setConnection(connection);
            itemDeVendaDAO.setConnection(connection);
            produtoDAO.setConnection(connection);
            for (ItemDeVenda listItemDeVenda : venda.getItensDeVenda()) {
                Produto produto = listItemDeVenda.getProduto();
                produto.setQuantidade(produto.getQuantidade() + listItemDeVenda.getQuantidade());
                produtoDAO.alterar(produto);
                itemDeVendaDAO.remover(listItemDeVenda);
            }
            vendaDAO.remover(venda);
            connection.commit();
            carregarTableViewVendas();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Por favor, escolha uma venda na Tabela!");
            alert.show();
        }
    }

    private boolean showFXMLAnchorPaneProcessosVendasDialog(Venda venda) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(AnchorPaneProcessosVendasDialogController.class
                .getResource("/javafxmvc/view/AnchorPaneProcessosVendasDialog.fxml"));
        AnchorPane page = (AnchorPane) loader.load();

        Stage dialoStage = new Stage();
        dialoStage.setTitle("Registros de vendas");
        Scene scene = new Scene(page);
        dialoStage.setScene(scene);

        AnchorPaneProcessosVendasDialogController controller = loader.getController();
        controller.setDialogStage(dialoStage);
        controller.setVenda(venda);
        dialoStage.showAndWait();
        return controller.isButtonConfirmarClicked();
    }
}
