package me.kaotich00.fwbanlog.storage;

import me.kaotich00.fwbanlog.FwBanlog;
import me.kaotich00.fwbanlog.storage.sql.SqlStorage;
import me.kaotich00.fwbanlog.storage.sql.hikari.MySQLConnectionFactory;
import me.kaotich00.fwbanlog.storage.util.StorageCredentials;
import org.bukkit.configuration.file.FileConfiguration;

public class StorageFactory {

    public static Storage storage;

    public static Storage getInstance() {
        if( storage != null ) {
            return storage;
        }
        storage = new Storage(FwBanlog.getPlugin(FwBanlog.class), getStorageFromConfig());
        return storage;
    }

    private static StorageMethod getStorageFromConfig() {
        FileConfiguration defaultConfig = FwBanlog.getDefaultConfig();
        String host = defaultConfig.getString("address");
        String database = defaultConfig.getString("database");
        String username = defaultConfig.getString("username");
        String password = defaultConfig.getString("password");
        StorageCredentials credentials = new StorageCredentials(host,database,username,password);

        return new SqlStorage(FwBanlog.getPlugin(FwBanlog.class), new MySQLConnectionFactory(credentials));
    }

}
