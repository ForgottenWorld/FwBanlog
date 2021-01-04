package me.kaotich00.fwbanlog.storage.sql;

import me.kaotich00.fwbanlog.FwBanlog;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionFactory {

    void init(FwBanlog plugin);

    void shutdown() throws Exception;

    Connection getConnection() throws SQLException;

}
