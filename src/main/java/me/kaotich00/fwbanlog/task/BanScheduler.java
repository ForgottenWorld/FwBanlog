package me.kaotich00.fwbanlog.task;

import me.kaotich00.fwbanlog.FwBanlog;
import me.kaotich00.fwbanlog.storage.Storage;
import me.kaotich00.fwbanlog.storage.StorageFactory;
import me.kaotich00.fwbanlog.storage.StorageMethod;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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
                UUID finalBannedPlayerUuid = bannedPlayerUuid;
                CompletableFuture.supplyAsync(() -> {
                    Integer playerId = storageMethod.getPlayerId(String.valueOf(finalBannedPlayerUuid));
                    return playerId;
                }).thenAccept(playerId -> {
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

                    UUID finalOperatorUUID = operatorUUID;
                    CompletableFuture.supplyAsync(() -> {
                        Integer operatorId = storageMethod.getPlayerId(String.valueOf(finalOperatorUUID));
                        return operatorId;
                    }).thenAccept(operatorId -> {
                        CompletableFuture.supplyAsync(() -> {
                            Boolean doesBanExist = storageMethod.doesBanExist(banEntry, idServer, playerId, operatorId);
                            return doesBanExist;
                        }).thenAccept(doesBanExist -> {
                            if(!doesBanExist) {
                                CompletableFuture.runAsync(() -> {
                                    storage.getStorageMethod().insertNewBan(banEntry, idServer, playerId, operatorId);
                                });
                            }
                        });
                    });
                });
            }
        }, period, period );
        return taskId;
    }

    public static int scheduleBanDetector() {
        FileConfiguration defaultConfig = FwBanlog.getDefaultConfig();
        Long period = defaultConfig.getLong("sync_frequency") * 20;

        int taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(FwBanlog.getPlugin(FwBanlog.class), () -> {
            CompletableFuture.supplyAsync(() -> {
                List<HashMap<String, Object>> banEntries = StorageFactory.getInstance().getStorageMethod().applyBansAddedFromWeb();
                return banEntries;
            }).thenAccept(banEntries -> {
                for(HashMap<String,Object> entry: banEntries) {
                    String target = (String) entry.get("target");
                    String reason = (String) entry.get("reason");
                    Date expiration = (java.sql.Date) entry.get("expiration");
                    String source = (String) entry.get("source");
                    Date start = (java.sql.Date) entry.get("start_date");

                    BanEntry banEntry = Bukkit.getServer().getBanList(BanList.Type.NAME).addBan(target, reason, expiration, source);
                    banEntry.setCreated(start);
                    banEntry.save();

                    Bukkit.getServer().broadcastMessage("[FwBanlog] The user " + target + " has been banned by " + source + " for " + reason);

                    Bukkit.getScheduler().scheduleSyncDelayedTask(FwBanlog.getPlugin(FwBanlog.class), () -> {
                        Player bannedPlayer = Bukkit.getPlayer(target);
                        if(bannedPlayer != null) {
                            bannedPlayer.kickPlayer(reason);
                        }
                    }, 1L);
                }
            });
        }, period, period );
        return taskId;
    }

}
