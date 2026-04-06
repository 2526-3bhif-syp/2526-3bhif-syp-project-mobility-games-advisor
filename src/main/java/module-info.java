module at.htl.mobilitygamesadvisor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires org.postgresql.jdbc;

    opens at.htl.mobilitygamesadvisor to javafx.fxml;
    opens at.htl.mobilitygamesadvisor.view to javafx.fxml;

    exports at.htl.mobilitygamesadvisor;
    exports at.htl.mobilitygamesadvisor.view;
    exports at.htl.mobilitygamesadvisor.model;
    exports at.htl.mobilitygamesadvisor.presenter;
}