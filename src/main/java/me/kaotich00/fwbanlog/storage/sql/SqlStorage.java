package me.kaotich00.fwbanlog.storage.sql;

import me.kaotich00.fwbanlog.FwBanlog;
import me.kaotich00.fwbanlog.storage.StorageMethod;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SqlStorage implements StorageMethod {

    private static final String INSERT_BAN = "INSERT INTO ban(`id_player_id`,`id_server_id`,`id_operator_id`,`start_date`,`is_perma`,`reason`,`description`,`image_password`,`created`,`end_date`,`is_applied`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    private static final String INSERT_PLAYER = "INSERT INTO player(`uuid`,`name`,`reputation`,`created`) VALUES (?,?,?,?)";
    private static final String SELECT_PLAYER = "SELECT * FROM player WHERE uuid = ?";
    private static final String SELECT_BAN = "SELECT * FROM ban WHERE start_date = ? AND id_player_id = ? AND id_server_id = ? AND id_operator_id = ?";
    private static final String SELECT_BAN_ENTRIES = "SELECT " +
        " bannedPlayer.name as target," +
        " ban.start_date," +
        " ban.reason," +
        " operator.name as source," +
        " ban.end_date as expiration" +
        " FROM ban ban" +
        " JOIN player bannedPlayer ON bannedPlayer.id = ban.id_player_id" +
        " JOIN player operator ON operator.id = ban.id_operator_id" +
        " WHERE is_applied = 0 OR is_applied IS NULL";
    private static final String UPDATE_BAN = "UPDATE ban SET is_applied = 1";

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
                SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String created = sm.format(banEntry.getCreated());
                String expiration = banEntry.getExpiration() == null ? null : sm.format(banEntry.getExpiration());
                String now = sm.format(new java.util.Date(System.currentTimeMillis()));

                ps.setInt(1, idPlayer);
                ps.setInt(2, idServer);
                ps.setInt(3, idOperator);
                ps.setString(4, created);
                ps.setBoolean(5, banEntry.getExpiration() == null ? true : false);
                ps.setString(6, banEntry.getReason());
                ps.setString(7, banEntry.getReason());
                ps.setString(8, "test");
                ps.setString(9, now);
                ps.setString(10, expiration);
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

    @Override
    public List<HashMap<String,Object>> applyBansAddedFromWeb() {
        List<HashMap<String, Object>> banEntries = new ArrayList<>();
        try (Connection c = getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(SELECT_BAN_ENTRIES)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String target = rs.getString("target");
                        String reason = rs.getString("reason");
                        Timestamp expirationTime = rs.getTimestamp("expiration");
                        Date expirationDate = null;
                        if(expirationTime != null) {
                            expirationDate = new Date(expirationTime.getTime());
                        }
                        Timestamp startTime = rs.getTimestamp("start_date");
                        Date startDate = new Date(startTime.getTime());
                        String source = rs.getString("source");

                        HashMap<String,Object> banInfo = new HashMap<>();
                        banInfo.put("target", target);
                        banInfo.put("reason", reason);
                        banInfo.put("expiration", expirationDate);
                        banInfo.put("source", source);
                        banInfo.put("start_date", startDate);

                        banEntries.add(banInfo);
                    }
                }
            }

            try (PreparedStatement ps = c.prepareStatement(UPDATE_BAN)) {
                ps.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return banEntries;
    }

}
