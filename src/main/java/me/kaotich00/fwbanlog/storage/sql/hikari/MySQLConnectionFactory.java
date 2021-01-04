package me.kaotich00.fwbanlog.storage.sql.hikari;

import com.zaxxer.hikari.HikariConfig;
import me.kaotich00.fwbanlog.storage.util.StorageCredentials;

import java.util.Map;

public class MySQLConnectionFactory extends HikariConnectionFactory {

    public MySQLConnectionFactory(StorageCredentials credentials) {
        super(credentials);
    }

    @Override
    protected String getDrivers() {
        return "com.mysql.jdbc.jdbc2.optional.MysqlDataSource";
    }

    @Override
    protected void addConnectionProperties(HikariConfig config, Map<String, String> properties) {
        properties.putIfAbsent("cachePrepStmts", "true");
        properties.putIfAbsent("prepStmtCacheSize", "250");
        properties.putIfAbsent("prepStmtCacheSqlLimit", "2048");
        properties.putIfAbsent("useServerPrepStmts", "true");
        properties.putIfAbsent("useLocalSessionState", "true");
        properties.putIfAbsent("rewriteBatchedStatements", "true");
        properties.putIfAbsent("cacheResultSetMetadata", "true");
        properties.putIfAbsent("cacheServerConfiguration", "true");
        properties.putIfAbsent("elideSetAutoCommits", "true");
        properties.putIfAbsent("maintainTimeStats", "false");
        properties.putIfAbsent("alwaysSendSetIsolation", "false");
        properties.putIfAbsent("cacheCallableStmts", "true");

        super.addConnectionProperties(config, properties);
    }

}
