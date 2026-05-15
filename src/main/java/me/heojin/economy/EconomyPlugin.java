package me.heojin.economy;

import me.heojin.economy.command.EconomyCommand;
import me.heojin.economy.listener.GUIListener;
import me.heojin.economy.manager.EconomyManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyPlugin extends JavaPlugin {
    private EconomyManager economyManager;
    private me.heojin.economy.manager.StockManager stockManager;

    @Override
    public void onEnable() {
        // Ensure data folder exists
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Initialize Managers
        economyManager = new EconomyManager(this);
        stockManager = new me.heojin.economy.manager.StockManager(this);

        // Register Commands
        getCommand("money").setExecutor(new EconomyCommand(this));
        getCommand("pay").setExecutor(new EconomyCommand(this));
        getCommand("shop").setExecutor(new EconomyCommand(this));
        getCommand("stock").setExecutor(new EconomyCommand(this));

        // Register Listeners
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new me.heojin.economy.listener.EconomyListener(this), this);

        getLogger().info("EconomyPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save data on disable
        if (economyManager != null) {
            economyManager.saveData();
        }
        if (stockManager != null) {
            stockManager.saveData();
        }
        getLogger().info("EconomyPlugin has been disabled!");
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public me.heojin.economy.manager.StockManager getStockManager() {
        return stockManager;
    }
}
