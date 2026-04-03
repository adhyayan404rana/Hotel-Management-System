module com.hotel {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens com.hotel.view    to javafx.graphics, javafx.fxml;
    opens com.hotel.model   to javafx.base;

    exports com.hotel.view;
    exports com.hotel.model;
    exports com.hotel.dao;
    exports com.hotel.service;
    exports com.hotel.util;
}
