package me.kaotich00.fwbanlog.storage;

import me.kaotich00.fwbanlog.FwBanlog;

import java.sql.Connection;
import java.sql.SQLException;

public interface StorageMethod {

    FwBanlog getPlugin();

    void init();

    void shutdown();

    Connection getConnection() throws SQLException;

}
