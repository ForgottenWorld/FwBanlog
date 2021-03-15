package me.kaotich00.fwbanlog.task;

import me.kaotich00.fwbanlog.FwBanlog;
import me.kaotich00.fwbanlog.storage.Storage;
import me.kaotich00.fwbanlog.storage.StorageFactory;
import me.kaotich00.fwbanlog.storage.StorageMethod;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BanScheduler {

    public static int scheduleBanTask() {
        FileConfiguration defaultConfig = FwBanlog.getDefaultConfig();
        Long period = defaultConfig.getLong("sync_frequency") * 20;

        int taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(FwBanlog.getPlugin(FwBanlog.class), () -> {
            updateWebBanlog();
            updateLocalBanlog();
            checkPlayersToUnban();
        } , period, period );

        return taskId;
    }

    public static void updateWebBanlog() {
        FileConfiguration defaultConfig = FwBanlog.getDefaultConfig();
        Integer serverId = defaultConfig.getInt("serverId");
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
                OfflinePlayer bannedOfflinePlayer = Bukkit.getOfflinePlayerIfCached(nickname);
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
                        OfflinePlayer operatorOfflinePlayer = Bukkit.getOfflinePlayerIfCached(operator);
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
    }

    public static void updateLocalBanlog() {
        CompletableFuture.supplyAsync(() -> {
            List<HashMap<String, Object>> banEntries = StorageFactory.getInstance().getStorageMethod().getListOfBanToApply();
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
    }

    public static void checkPlayersToUnban() {
        CompletableFuture.supplyAsync(() -> {
            List<String> playersToUnban = StorageFactory.getInstance().getStorageMethod().getListOfPlayersToUnban();
            return playersToUnban;
        }).thenAccept(playersToUnban -> {
            for(String playerName: playersToUnban) {
                BanEntry banEntry = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(playerName);
                if(banEntry != null) {
                    Bukkit.getBanList(BanList.Type.NAME).pardon(playerName);
                    Bukkit.getServer().broadcastMessage("[FwBanlog] The user " + playerName + " has been pardoned");
                }
            }
        });
    }

}
