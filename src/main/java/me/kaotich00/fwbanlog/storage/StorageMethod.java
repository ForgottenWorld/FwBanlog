package me.kaotich00.fwbanlog.storage;

import me.kaotich00.fwbanlog.FwBanlog;
import org.bukkit.BanEntry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface StorageMethod {

    FwBanlog getPlugin();

    void init();

    void shutdown();

    Connection getConnection() throws SQLException;

    void insertNewBan(BanEntry banEntry, int idServer, int idPlayer, int idOperator);

    void deleteBan();

    Integer getPlayerId(String uuid);

    Boolean doesBanExist(BanEntry banEntry, int idServer, int idPlayer, int idOperator);

    List<HashMap<String,Object>> applyBansAddedFromWeb();

}
