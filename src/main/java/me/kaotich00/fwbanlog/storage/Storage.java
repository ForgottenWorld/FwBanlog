package me.kaotich00.fwbanlog.storage;

import me.kaotich00.fwbanlog.FwBanlog;

public class Storage {

    private final FwBanlog plugin;
    private final StorageMethod storageMethod;

    public Storage(FwBanlog plugin, StorageMethod storageMethod) {
        this.plugin = plugin;
        this.storageMethod = storageMethod;
    }

    public StorageMethod getStorageMethod() {
        return this.storageMethod;
    }

    public void init() {
        try {
            this.storageMethod.init();
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to init storage implementation");
            e.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            this.storageMethod.shutdown();
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to shutdown storage implementation");
            e.printStackTrace();
        }
    }

}
