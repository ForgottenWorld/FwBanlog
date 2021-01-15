package me.kaotich00.fwbanlog.storage.sql;

import me.kaotich00.fwbanlog.FwBanlog;
import me.kaotich00.fwbanlog.storage.StorageMethod;
import org.bukkit.BanEntry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.UUID;

public class SqlStorage implements StorageMethod {

    private static final String INSERT_BAN = "INSERT INTO ban(`id_player_id`,`id_server_id`,`id_operator_id`,`start_date`,`is_perma`,`reason`,`description`,`image_password`,`created`,`end_date`,`is_applied`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    private static final String INSERT_PLAYER = "INSERT INTO player(`uuid`,`name`,`reputation`,`created`) VALUES (?,?,?,?)";
    private static final String SELECT_PLAYER = "SELECT * FROM player WHERE uuid = ?";
    private static final String SELECT_BAN = "SELECT * FROM ban WHERE start_date = ? AND id_player_id = ? AND id_server_id = ? AND id_operator_id = ?";

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
    public void insertNewBan(BanEntry banEntry, int idServer, int idPlayer, int idOperator) {
        try (Connection c = getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(INSERT_BAN)) {
                ps.setInt(1, idPlayer);
                ps.setInt(2, idServer);
                ps.setInt(3, idOperator);
                ps.setTimestamp(4, new Timestamp(banEntry.getCreated().getTime()));
                ps.setBoolean(5, banEntry.getExpiration() == null ? true : false);
                ps.setString(6, banEntry.getReason());
                ps.setString(7, banEntry.getReason());
                ps.setString(8, "test");
                ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
                ps.setTimestamp(10, banEntry.getExpiration() == null ? null : new Timestamp(banEntry.getExpiration().getTime()));
                ps.setBoolean(11, true);
                ps.execute();            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteBan() {

    }

    @Override
    public Integer getPlayerId(String uuid) {

        if(uuid.equals("null")) {
            return -1;
        }

        boolean doesExist = false;

        try (Connection c = getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(SELECT_PLAYER)) {
                ps.setString(1, uuid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int idPlayer = rs.getInt("id");
                        return idPlayer;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

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

        try (Connection c = getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(SELECT_PLAYER)) {
                ps.setString(1, uuid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int idPlayer = rs.getInt("id");
                        return idPlayer;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Boolean doesBanExist(BanEntry banEntry, int idServer, int idPlayer, int idOperator) {

        try (Connection c = getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(SELECT_BAN)) {
                SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = sm.format(banEntry.getCreated());

                ps.setString(1, formattedDate);
                ps.setInt(2, idPlayer);
                ps.setInt(3, idServer);
                ps.setInt(4, idOperator);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

}
