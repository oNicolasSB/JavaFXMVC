module javafxmvc {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens javafxmvc to javafx.graphics, javafx.fxml;
    opens javafxmvc.controller to javafx.fxml;
    exports javafxmvc;
}
