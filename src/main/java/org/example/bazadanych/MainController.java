package org.example.bazadanych;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.sql.*;
import java.util.ArrayList;

public class MainController {
    @FXML
    private VBox mainContainer;

    public void readTables(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/osoby";
            String login = "root";
            String password = "";
            Connection conn = DriverManager.getConnection(url,login,password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW TABLES;");
            Label label = new Label("Wybierz tabelę którą chcesz edytować:");
            mainContainer.setAlignment(Pos.CENTER);
            label.setFont(new Font("Arial", 20));
            mainContainer.getChildren().add(label);
            HBox tablesContainer = new HBox();
            while(rs.next()){
                String tableName = rs.getString("Tables_in_osoby");
                Button button = new Button(tableName);
                button.setMinHeight(100);
                button.setMaxHeight(100);
                button.setMinWidth(50);
                button.setPrefWidth(200);
                button.setMaxWidth(Double.MAX_VALUE);
                button.setOnMouseClicked(event -> clickTable(((Button) event.getSource()).getText()));
                tablesContainer.getChildren().add(button);
            }
            tablesContainer.setAlignment(Pos.CENTER);
            tablesContainer.setPadding(new Insets(20,0,0,0));
            mainContainer.getChildren().add(tablesContainer);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void clickTable(String tableName){
        try {
            String url = "jdbc:mysql://localhost:3306/osoby";
            String login = "root";
            String password = "";
            Connection conn = DriverManager.getConnection(url,login,password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM " + tableName + " ;");
            mainContainer.getChildren().clear();
            mainContainer.setAlignment(Pos.TOP_LEFT);
            TitledPane titledPane = new TitledPane();
            VBox.setVgrow(titledPane, Priority.ALWAYS);
            titledPane.setText(tableName);
            titledPane.setCollapsible(false);
            HBox hBox = new HBox();
            VBox.setVgrow(hBox, Priority.ALWAYS);
            VBox vBox = new VBox();
            ArrayList<String> columnsName = new ArrayList<>();
            while (rs.next()){
                columnsName.add(rs.getString("Field"));
            }

            HBox hBox2 = new HBox();
            rs = stmt.executeQuery("SELECT * FROM " + tableName + " ;");
            readTable(rs, columnsName,hBox2);
            vBox.getChildren().add(hBox2);
            Button addRow = new Button("Dodaj wiersz");
            addRow.setOnMouseClicked(event -> {
                HBox hBox1 = new HBox();
                Button addButton = new Button("Dodaj wiersz");
                addButton.setMinWidth(100);
                addButton.setMaxWidth(100);
                addButton.setTranslateX(5);
                addButton.setTranslateY(16);
                Button cancelButton = new Button("X");
                cancelButton.setTranslateY(16);
                cancelButton.setTranslateX(5);
                cancelButton.setMinWidth(30);
                cancelButton.setAlignment(Pos.CENTER);
                addButton.setOnMouseClicked(event1 -> {
                    try {
                        Statement stmts = conn.createStatement();
                        String sql = "INSERT INTO " + tableName + " (" + tables(columnsName) + ") VALUES (" + values(hBox1) + " );";
                        stmts.execute(sql);
                        ResultSet rss = stmt.executeQuery("SELECT * FROM " + tableName + " ;");
                        readTable(rss, columnsName, hBox2);
                        clearTextField(hBox2);
                    } catch (SQLException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Błąd");
                        alert.setHeaderText(null);
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                    }
                });
                cancelButton.setOnMouseClicked(event1 -> hBox1.getChildren().clear());
                int idx = 0;
                while (idx < columnsName.size()){
                    Label label = new Label(columnsName.get(idx));
                    VBox vBox2 = new VBox();
                    idx++;
                    TextField textField = new TextField();
                    vBox2.getChildren().addAll(label, textField);
                    vBox2.setAlignment(Pos.CENTER);
                    hBox1.getChildren().add(vBox2);
                }
                hBox1.getChildren().addAll(addButton, cancelButton);
                vBox.getChildren().add(hBox1);
            });
            Button deleteRow = new Button("Usuń wiersz");
            deleteRow.setOnMouseClicked(event -> {
                for(Node c: hBox2.getChildren()){
                    if(c instanceof TableView){
                        TableView<String[]> tableView = (TableView) c;
                        TableView.TableViewSelectionModel<String[]> selectionModel = tableView.getSelectionModel();
                        String[] selectedRow = selectionModel.getSelectedItem();
                        if (selectedRow != null) {
                            try {
                                Statement stmts = conn.createStatement();
                                String sql = "DELETE FROM " + tableName + " WHERE id = " + selectedRow[0] + ";";
                                stmts.execute(sql);
                                tableView.getItems().remove(selectedRow);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Informacja");
                            alert.setHeaderText(null);
                            alert.setContentText("Brak zaznaczonego wiersza");
                            alert.showAndWait();
                        }
                    }
                }
            });
            Button editButton = new Button("Modyfikuj wiersz");
            editButton.setOnMouseClicked(event -> {
                for(Node c: hBox2.getChildren()) {
                    if (c instanceof TableView) {
                        TableView<String[]> tableView = (TableView) c;
                        TableView.TableViewSelectionModel<String[]> selectionModel = tableView.getSelectionModel();
                        String[] selectedRow = selectionModel.getSelectedItem();
                        HBox hBox1 = new HBox();
                        Button edButton = new Button("Zatwierdź edycję wiersza");
                        edButton.setMinWidth(150);
                        edButton.setMaxWidth(150);
                        edButton.setTranslateX(5);
                        edButton.setTranslateY(16);
                        Button cancelButton = new Button("X");
                        cancelButton.setTranslateY(16);
                        cancelButton.setTranslateX(5);
                        cancelButton.setMinWidth(30);
                        cancelButton.setAlignment(Pos.CENTER);
                        cancelButton.setOnMouseClicked(event1 -> hBox1.getChildren().clear());
                        edButton.setOnMouseClicked(event1 -> {
                            if (selectedRow != null) {
                                try {
                                    Statement stmts = conn.createStatement();
                                    String sql = "UPDATE " + tableName + " SET " + editSQL(columnsName, hBox1) + " WHERE id = " + selectedRow[0] + ";";
                                    stmts.execute(sql);
                                    tableView.getItems().removeAll();
                                    ResultSet rss = stmt.executeQuery("SELECT * FROM " + tableName + " ;");
                                    readTable(rss, columnsName, hBox2);
                                    hBox1.getChildren().clear();
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Informacja");
                                alert.setHeaderText(null);
                                alert.setContentText("Brak zaznaczonego wiersza");
                                alert.showAndWait();
                            }

                        });
                        if (selectedRow != null) {
                            int idx = 0;
                            while (idx < columnsName.size()){
                                Label label = new Label(columnsName.get(idx));
                                VBox vBox2 = new VBox();
                                TextField textField = new TextField(selectedRow[idx]);
                                idx++;
                                vBox2.getChildren().addAll(label, textField);
                                vBox2.setAlignment(Pos.CENTER);
                                hBox1.getChildren().add(vBox2);
                            }
                            hBox1.getChildren().addAll(edButton, cancelButton);
                            vBox.getChildren().add(hBox1);
                        } else {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Informacja");
                            alert.setHeaderText(null);
                            alert.setContentText("Brak zaznaczonego wiersza");
                            alert.showAndWait();
                        }

                    }
                }
            });
            Button button = new Button("Wróć");
            button.setOnMouseClicked(event -> {
                mainContainer.getChildren().clear();
                readTables();
            });
            HBox hBox3 = new HBox();
            hBox3.setPadding(new Insets(20,0,0,0));
            hBox3.getChildren().addAll(addRow, deleteRow, editButton, button);
            vBox.getChildren().addAll(hBox, hBox3);
            VBox.setVgrow(vBox, Priority.ALWAYS);
            VBox.setVgrow(hBox3, Priority.ALWAYS);
            VBox.setVgrow(titledPane, Priority.ALWAYS);
            titledPane.setContent(vBox);
            mainContainer.getChildren().add(titledPane);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String tables(ArrayList<String> columns){
        StringBuilder result  = new StringBuilder();
        for (String s: columns){
            result.append(s).append(", ");
        }
        return result.substring(0, result.toString().length() - 2);
    }
    private String values(HBox hbox){
        StringBuilder result  = new StringBuilder();
        for (Node c: hbox.getChildren()){
            if (c instanceof VBox){
                for (Node ch: ((VBox) c).getChildren()){
                    if (ch instanceof TextField){
                        if (((TextField) ch).getText().matches("^[a-zA-Z]*$"))
                            result.append("'" + ((TextField) ch).getText()).append("', ");
                        else
                            result.append("'" + ((TextField) ch).getText() + "', ");
                    }
                }
            }
        }
        return result.substring(0, result.toString().length() - 2);
    }
    private String editSQL(ArrayList<String> columns, HBox hbox){
        StringBuilder result = new StringBuilder();
        int idx = 0;
        for (Node c: hbox.getChildren()) {
            if (c instanceof VBox) {
                for (Node ch : ((VBox) c).getChildren()) {
                    if (ch instanceof TextField) {
                        if (((TextField) ch).getText().matches("^[a-zA-Z]*$"))
                            result.append(columns.get(idx)).append("='").append(((TextField) ch).getText()).append("', ");
                        else
                            result.append(columns.get(idx)).append("=").append(((TextField) ch).getText()).append(", ");
                        idx++;
                    }
                }
            }
        }
        return result.substring(0, result.toString().length() - 2);
    }

    private void clearTextField(HBox hbox){
        for (Node c: hbox.getChildren()){
            if (c instanceof VBox){
                for (Node ch: ((VBox) c).getChildren()){
                    if (ch instanceof TextField){
                        ((TextField) ch).setText("");
                    }
                }
            }
        }
    }

    private void readTable(ResultSet rs, ArrayList<String> columnsName, HBox hBox2) throws SQLException {
        hBox2.getChildren().clear();
        HBox.setHgrow(hBox2, Priority.ALWAYS);
        VBox.setVgrow(hBox2, Priority.ALWAYS);
        TableView<String[]> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        HBox.setHgrow(tableView, Priority.ALWAYS);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        for (int i = 0; i < columnsName.size(); i++) {
            final int columnIndex = i;
            TableColumn<String[], String> column = new TableColumn<>(columnsName.get(i));
            column.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[columnIndex]));
            column.setStyle("-fx-alignment: CENTER;");
            tableView.getColumns().add(column);
        }
        hBox2.getChildren().add(tableView);
        ArrayList<String[]> rowsToAdd = new ArrayList<>();
        while (rs.next()){
            String[] row = new String[columnsName.size()];
            for (int i = 0; i < columnsName.size(); i++) {
                String columnName = columnsName.get(i);
                if (rs.getString(columnName) != null) {
                    row[i] = rs.getString(columnName);
                }
            }
            rowsToAdd.add(row);
        }

        for (String[] row : rowsToAdd) {
            tableView.getItems().add(row);
        }

    }
}