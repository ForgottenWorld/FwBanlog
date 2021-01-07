package me.kaotich00.fwbanlog.storage.sql;

import me.kaotich00.fwbanlog.FwBanlog;
import me.kaotich00.fwbanlog.storage.StorageMethod;
import org.bukkit.BanEntry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SqlStorage implements StorageMethod {

    private static final String INSERT_BAN = "INSERT INTO ban(`id_player_id`,`id_server_id`,`id_operator_id`,`start_date`,`is_perma`,`reason`,`description`,`image_password`,`created`,`end_date`,`is_applied`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    private static final String INSERT_PLAYER = "INSERT INTO player(`uuid`,`name`,`reputation`,`created`) VALUES (?,?,?,?)";
    private static final String SELECT_PLAYER = "SELECT * FROM player WHERE uuid = ?";

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

    @Override
    public CompletableFuture<Void> insertNewBan(BanEntry banEntry, int idServer, int idPlayer, int idOperator) {
        CompletableFuture<Void> playerIdFuture = new CompletableFuture<>();
        try (Connection c = getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(INSERT_BAN)) {
                ps.setInt(1, idPlayer);
                ps.setInt(2, idServer);
                ps.setInt(3, idOperator);
                ps.setDate(4, new java.sql.Date(banEntry.getCreated().getTime()));
                ps.setBoolean(5, banEntry.getExpiration() == null ? true : false);
                ps.setString(6, banEntry.getReason());
                ps.setString(7, banEntry.getReason());
                ps.setString(8, "test");
                ps.setDate(9, new java.sql.Date(System.currentTimeMillis()));
                ps.setDate(10, banEntry.getExpiration() == null ? null : new java.sql.Date(banEntry.getExpiration().getTime()));
                ps.setBoolean(11, true);
                ps.execute();

                playerIdFuture.complete(null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playerIdFuture;
    }

    @Override
    public CompletableFuture<Void> deleteBan() {
        return null;
    }

    @Override
    public CompletableFuture<Integer> getPlayerId(String uuid) {
        CompletableFuture<Integer> playerIdFuture = new CompletableFuture<>();

        if(uuid.equals("null")) {
            playerIdFuture.complete(-1);
        }

        boolean doesExist = false;

        try (Connection c = getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(SELECT_PLAYER)) {
                ps.setString(1, uuid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int idPlayer = rs.getInt("id");
                        doesExist = true;
                        playerIdFuture.complete(idPlayer);
                    }
                }
                return playerIdFuture;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(!doesExist) {
            Player player = Bukkit.getServer().getPlayer(UUID.fromString(uuid));
            try (Connection c = getConnection()) {
                try (PreparedStatement ps = c.prepareStatement(INSERT_PLAYER)) {
                    ps.setString(1, player.getUniqueId().toString());
                    ps.setString(2, player.getName());
                    ps.setInt(3, 100);
                    ps.setDate(4, new java.sql.Date(System.currentTimeMillis()));
                    ps.execute();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try (Connection c = getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(SELECT_PLAYER)) {
                ps.setString(1, uuid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int idPlayer = rs.getInt("id");
                        playerIdFuture.complete(idPlayer);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playerIdFuture;
    }

}
