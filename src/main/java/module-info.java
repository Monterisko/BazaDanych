module org.example.bazadanych {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;


    opens org.example.bazadanych to javafx.fxml;
    exports org.example.bazadanych;
}