module javafxmvc {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires java.desktop;
    requires java.logging;

    opens javafxmvc to javafx.graphics, javafx.fxml;
    opens javafxmvc.controller to javafx.fxml;
    opens javafxmvc.model.domain to javafx.base;
    exports javafxmvc;
}
