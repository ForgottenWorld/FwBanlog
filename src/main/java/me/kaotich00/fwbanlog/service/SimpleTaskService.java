package me.kaotich00.fwbanlog.service;

import me.kaotich00.fwbanlog.task.BanScheduler;
import org.bukkit.Bukkit;

public class SimpleTaskService {

    private static SimpleTaskService taskService;
    private int banListenerTaskId;
    private int banDetectorTaskId;

    private SimpleTaskService() {
        if (taskService != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static SimpleTaskService getInstance() {
        if(taskService == null) {
            taskService = new SimpleTaskService();
        }
        return taskService;
    }

    public void stopBanTasks() {
        Bukkit.getScheduler().cancelTask(this.banListenerTaskId);
        Bukkit.getScheduler().cancelTask(this.banDetectorTaskId);
    }

    public void scheduleBanTasks() {
        this.banListenerTaskId = BanScheduler.scheduleBanListener();
        this.banDetectorTaskId = BanScheduler.scheduleBanDetector();
    }

}
