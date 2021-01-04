package me.kaotich00.fwbanlog.storage.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class StorageCredentials {

    private final String host;
    private final String database;
    private final String username;
    private final String password;
    private final int maxPoolSize;
    private final int minIdleConnections;
    private final int maxLifetime;
    private final int connectionTimeout;

    public StorageCredentials(String host, String database, String username, String password) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
        this.maxPoolSize = 10;
        this.minIdleConnections = 10;
        this.maxLifetime = 1800000;
        this.connectionTimeout = 5000;
    }

    public String getHost() {
        return this.host;
    }

    public String getDatabase() {
        return this.database;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public int getMaxPoolSize() {
        return this.maxPoolSize;
    }

    public int getMinIdleConnections() {
        return this.minIdleConnections;
    }

    public int getMaxLifetime() {
        return this.maxLifetime;
    }

    public int getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public Connection toConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" + this.host + "/" + this.database + "?useSSL=false", this.username, this.password);
    }

}
