package me.kaotich00.fwbanlog;

import me.kaotich00.fwbanlog.service.SimpleTaskService;
import me.kaotich00.fwbanlog.storage.Storage;
import me.kaotich00.fwbanlog.storage.StorageFactory;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class FwBanlog extends JavaPlugin {

    public static FileConfiguration defaultConfig;

    @Override
    public void onEnable() {
        ConsoleCommandSender sender = Bukkit.getConsoleSender();

        sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=====================[" + ChatColor.RED + " Fw" + ChatColor.DARK_RED + "Banlog " + ChatColor.DARK_GRAY + "]======================");

        sender.sendMessage(ChatColor.GRAY + "   >> " + ChatColor.RESET + " Loading configuration...");
        loadConfiguration();

        sender.sendMessage(ChatColor.GRAY + "   >> " + ChatColor.RESET + " Initializing database...");
        initStorage();

        sender.sendMessage(ChatColor.GRAY + "   >> " + ChatColor.RESET + " Registering listeners...");
        registerListeners();

        sender.sendMessage(ChatColor.GRAY + "   >> " + ChatColor.RESET + " Scheduling tasks...");
        scheduleTasks();

        sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH +  "====================================================");
    }

    @Override
    public void onDisable() {
        shutdownStorage();
    }

    private void loadConfiguration() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        defaultConfig = getConfig();
    }

    public static FileConfiguration getDefaultConfig() {
        return defaultConfig;
    }

    public void reloadDefaultConfig() {
        reloadConfig();
        defaultConfig = getConfig();
    }

    public void initStorage() {
        Storage storage = StorageFactory.getInstance();
        storage.init();
    }

    public void shutdownStorage() {
        Storage storage = StorageFactory.getInstance();
        storage.shutdown();
    }

    public void registerListeners() {
    }

    public void scheduleTasks() {
        SimpleTaskService simpleTaskService = SimpleTaskService.getInstance();
        simpleTaskService.scheduleBanTasks();
    }
}
