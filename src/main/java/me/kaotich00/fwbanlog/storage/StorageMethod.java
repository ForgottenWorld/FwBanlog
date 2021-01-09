package me.kaotich00.fwbanlog.storage;

import me.kaotich00.fwbanlog.FwBanlog;
import org.bukkit.BanEntry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public interface StorageMethod {

    FwBanlog getPlugin();

    void init();

    void shutdown();

    Connection getConnection() throws SQLException;

    CompletableFuture<Void> insertNewBan(BanEntry banEntry, int idServer, int idPlayer, int idOperator);

    CompletableFuture<Void> deleteBan();

    CompletableFuture<Integer> getPlayerId(String uuid);

    CompletableFuture<Boolean> doesBanExist(BanEntry banEntry, int idServer, int idPlayer, int idOperator);

}
