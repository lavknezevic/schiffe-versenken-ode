module at.newsfx.fhtechnikum.schiffe_versenken_ode {
    requires javafx.controls;
    requires javafx.fxml;


    opens at.newsfx.fhtechnikum.schiffe_versenken_ode to javafx.fxml;
    exports at.newsfx.fhtechnikum.schiffe_versenken_ode;
}