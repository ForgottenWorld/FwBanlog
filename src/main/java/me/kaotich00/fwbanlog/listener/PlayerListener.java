package me.kaotich00.fwbanlog.listener;

import me.kaotich00.fwbanlog.FwBanlog;
import me.kaotich00.fwbanlog.storage.Storage;
import me.kaotich00.fwbanlog.storage.StorageFactory;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.Date;
import java.util.UUID;

public class PlayerListener implements Listener {

    int serverId;

    public PlayerListener() {
        FileConfiguration defaultConfig = FwBanlog.getDefaultConfig();
        this.serverId = Integer.parseInt(defaultConfig.getString("serverId"));
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event){

        Player kickedPlayer = event.getPlayer();
        BanEntry banEntry = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(kickedPlayer.getName());

        if(banEntry == null) {
            return;
        }

        Storage storage = StorageFactory.getInstance();

        String nickname     = kickedPlayer.getName();
        UUID playerUUID     = kickedPlayer.getUniqueId();

        // Get the player ID from UUID or create a new one
        storage.getStorageMethod().getPlayerId(String.valueOf(playerUUID)).thenAccept(playerId -> {
            int idServer        = this.serverId;
            String operator     = ChatColor.stripColor(banEntry.getSource());
            UUID operatorUUID   = !operator.equalsIgnoreCase("Console") ? Bukkit.getPlayer(operator).getUniqueId() : null;
            storage.getStorageMethod().getPlayerId(String.valueOf(operatorUUID)).thenAccept(operatorId -> {
                storage.getStorageMethod().insertNewBan(banEntry, idServer, playerId, operatorId);
            });
        });

    }

}
