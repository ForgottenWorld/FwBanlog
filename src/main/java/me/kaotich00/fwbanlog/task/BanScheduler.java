package me.kaotich00.fwbanlog.task;

import me.kaotich00.fwbanlog.FwBanlog;
import me.kaotich00.fwbanlog.storage.Storage;
import me.kaotich00.fwbanlog.storage.StorageFactory;
import me.kaotich00.fwbanlog.storage.StorageMethod;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BanScheduler {

    public static int scheduleBanListener() {
        FileConfiguration defaultConfig = FwBanlog.getDefaultConfig();
        Long period = defaultConfig.getLong("sync_frequency") * 20;
        Integer serverId = defaultConfig.getInt("serverId");

        int taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(FwBanlog.getPlugin(FwBanlog.class), () -> {
            BanList banList = Bukkit.getServer().getBanList(BanList.Type.NAME);
            for(BanEntry banEntry: banList.getBanEntries()) {
                if(banEntry == null) {
                    return;
                }

                Storage storage = StorageFactory.getInstance();

                String nickname         = banEntry.getTarget();
                Player bannedPlayer     = Bukkit.getPlayer(nickname);
                UUID bannedPlayerUuid   = null;
                if(bannedPlayer == null) {
                    OfflinePlayer bannedOfflinePlayer = Bukkit.getOfflinePlayer(nickname);
                    if(bannedOfflinePlayer != null) {
                        bannedPlayerUuid = bannedOfflinePlayer.getUniqueId();
                    }
                } else {
                    bannedPlayerUuid = bannedPlayer.getUniqueId();
                }

                StorageMethod storageMethod = storage.getStorageMethod();
                storageMethod.getPlayerId(String.valueOf(bannedPlayerUuid)).thenAccept(playerId -> {
                    int idServer        = serverId;
                    String operator     = ChatColor.stripColor(banEntry.getSource());

                    UUID operatorUUID = null;
                    if(!operator.equalsIgnoreCase("Console")) {
                        Player operatorPlayer = Bukkit.getPlayer(operator);
                        if(operatorPlayer == null) {
                            OfflinePlayer operatorOfflinePlayer = Bukkit.getOfflinePlayer(operator);
                            if(operatorOfflinePlayer != null) {
                                operatorUUID = operatorOfflinePlayer.getUniqueId();
                            }
                        } else {
                            operatorUUID = operatorPlayer.getUniqueId();
                        }
                    }

                    storageMethod.getPlayerId(String.valueOf(operatorUUID)).thenAccept(operatorId -> {
                        storageMethod.doesBanExist(banEntry, idServer, playerId, operatorId).thenAccept(doesBanExist -> {
                            if(!doesBanExist) {
                                storage.getStorageMethod().insertNewBan(banEntry, idServer, playerId, operatorId);
                            }
                        });
                    });
                });
            }
        }, period, period );
        return taskId;
    }

}
