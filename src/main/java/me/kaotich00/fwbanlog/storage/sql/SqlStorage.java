package me.kaotich00.fwbanlog.storage.sql;

import me.kaotich00.fwbanlog.FwBanlog;
import me.kaotich00.fwbanlog.storage.StorageMethod;

import java.sql.Connection;
import java.sql.SQLException;

public class SqlStorage implements StorageMethod {

    private static final String INSERT_LISTING = "INSERT INTO listing(`seller_uuid`,`seller_nickname`,`amount`,`unit_price`,`status`,`item_stack`,`additional_data`,`minecraft_enum`,`item_name`) VALUES (?,?,?,?,1,?,?,?,?)";

    private ConnectionFactory connectionFactory;
    private final FwBanlog plugin;

    public SqlStorage(FwBanlog plugin, ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.plugin = plugin;
    }

    @Override
    public FwBanlog getPlugin() {
        return this.plugin;
    }

    @Override
    public void init() {
        this.connectionFactory.init(this.plugin);
    }

    @Override
    public void shutdown() {
        try {
            this.connectionFactory.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.connectionFactory.getConnection();
    }

}
