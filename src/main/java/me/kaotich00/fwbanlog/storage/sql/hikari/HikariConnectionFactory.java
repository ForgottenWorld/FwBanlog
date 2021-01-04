package me.kaotich00.fwbanlog.storage.sql.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.kaotich00.fwbanlog.FwBanlog;
import me.kaotich00.fwbanlog.storage.sql.ConnectionFactory;
import me.kaotich00.fwbanlog.storage.util.StorageCredentials;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class HikariConnectionFactory implements ConnectionFactory {

    protected final StorageCredentials configuration;
    private HikariDataSource hikari;

    public HikariConnectionFactory(StorageCredentials configuration) {
        this.configuration = configuration;
    }

    protected String getDrivers() {
        return null;
    }

    protected void addConnectionProperties(HikariConfig config, Map<String, String> properties) {
        for (Map.Entry<String, String> property : properties.entrySet()) {
            config.addDataSourceProperty(property.getKey(), property.getValue());
        }
    }

    protected void addConnectionInfo(HikariConfig config) {
        String address = this.configuration.getHost();
        String[] addressSplit = address.split(":");
        address = addressSplit[0];
        String port = addressSplit.length > 1 ? addressSplit[1] : "3306";

        config.setDataSourceClassName(getDrivers());
        config.addDataSourceProperty("serverName", address);
        config.addDataSourceProperty("port", port);
        config.addDataSourceProperty("databaseName", this.configuration.getDatabase());
        config.addDataSourceProperty("useSSL", false);
        config.setUsername(this.configuration.getUsername());
        config.setPassword(this.configuration.getPassword());
    }

    @Override
    public void init(FwBanlog plugin) {
        HikariConfig config = null;
        try {
            config = new HikariConfig();
        } catch (LinkageError e) {
            e.printStackTrace();
        }

        config.setPoolName("fwauctionhouse-hikari");
        addConnectionInfo(config);
        config.setInitializationFailTimeout(-1);

        this.hikari = new HikariDataSource(config);
    }

    @Override
    public void shutdown() {
        if (this.hikari != null) {
            this.hikari.close();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (this.hikari == null) {
            throw new SQLException("Unable to get a connection from the pool. (hikari is null)");
        }
        Connection connection = this.hikari.getConnection();
        if (connection == null) {
            throw new SQLException("Unable to get a connection from the pool. (getConnection returned null)");
        }
        return connection;
    }

}
