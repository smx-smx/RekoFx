module org.reko.rekofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires afterburner.fx;
    requires kotlin.reflect;
    requires kotlinx.serialization.core;
    requires kotlinx.serialization.json;


    //opens org.reko.rekofx.gui to javafx.fxml, afterburner.fx;
    opens org.reko.rekofx.gui;
    opens org.reko.rekofx.services;
    exports org.reko.rekofx.gui;
    exports org.reko.rekofx.services;

}