module at.htl.mobilitygamesadvisor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens at.htl.mobilitygamesadvisor to javafx.fxml;
    exports at.htl.mobilitygamesadvisor;
}
