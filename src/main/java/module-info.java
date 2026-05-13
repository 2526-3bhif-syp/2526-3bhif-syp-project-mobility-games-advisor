module at.htl.mobilitygamesadvisor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires uk.co.caprica.vlcj;
    requires uk.co.caprica.vlcj.javafx;
    requires jbcrypt;
    requires java.desktop;

    opens at.htl.mobilitygamesadvisor to javafx.fxml;
    opens at.htl.mobilitygamesadvisor.view to javafx.fxml;

    exports at.htl.mobilitygamesadvisor;
    exports at.htl.mobilitygamesadvisor.view;
    exports at.htl.mobilitygamesadvisor.model;
    exports at.htl.mobilitygamesadvisor.presenter;
    exports at.htl.mobilitygamesadvisor.util;
    opens at.htl.mobilitygamesadvisor.util to javafx.fxml;
}