package me.heojin.economy.manager;

import me.heojin.economy.EconomyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StockManager {
    private final EconomyPlugin plugin;
    private final File stockFile;
    private final File portfolioFile;
    private FileConfiguration stockConfig;
    private FileConfiguration portfolioConfig;

    private final Map<String, StockData> stocks = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> portfolios = new HashMap<>();

    public StockManager(EconomyPlugin plugin) {
        this.plugin = plugin;
        this.stockFile = new File(plugin.getDataFolder(), "stocks.yml");
        this.portfolioFile = new File(plugin.getDataFolder(), "portfolios.yml");
        loadData();
        startPriceUpdater();
    }

    public void loadData() {
        // Load Stocks
        if (!stockFile.exists()) {
            plugin.saveResource("stocks.yml", false);
        }
        stockConfig = YamlConfiguration.loadConfiguration(stockFile);
        if (stockConfig.contains("stocks")) {
            for (String id : stockConfig.getConfigurationSection("stocks").getKeys(false)) {
                String name = stockConfig.getString("stocks." + id + ".name");
                double price = stockConfig.getDouble("stocks." + id + ".price");
                String item = stockConfig.getString("stocks." + id + ".display_item");
                StockData stockData = new StockData(id, name, price, item);
                // Load history if exists, else add current price
                if (stockConfig.contains("stocks." + id + ".history")) {
                    stockData.priceHistory.addAll(stockConfig.getDoubleList("stocks." + id + ".history"));
                } else {
                    stockData.priceHistory.add(price);
                }
                stocks.put(id, stockData);
            }
        }

        // Load Portfolios
        if (!portfolioFile.exists()) {
            try {
                portfolioFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        portfolioConfig = YamlConfiguration.loadConfiguration(portfolioFile);
        if (portfolioConfig.contains("portfolios")) {
            for (String uuidStr : portfolioConfig.getConfigurationSection("portfolios").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                Map<String, Integer> playerStocks = new HashMap<>();
                for (String stockId : portfolioConfig.getConfigurationSection("portfolios." + uuidStr).getKeys(false)) {
                    playerStocks.put(stockId, portfolioConfig.getInt("portfolios." + uuidStr + "." + stockId));
                }
                portfolios.put(uuid, playerStocks);
            }
        }
    }

    public void saveData() {
        // Save Stocks
        for (StockData stock : stocks.values()) {
            stockConfig.set("stocks." + stock.id + ".price", stock.price);
            stockConfig.set("stocks." + stock.id + ".history", stock.priceHistory);
        }
        try {
            stockConfig.save(stockFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save Portfolios
        for (Map.Entry<UUID, Map<String, Integer>> entry : portfolios.entrySet()) {
            String uuid = entry.getKey().toString();
            for (Map.Entry<String, Integer> stockEntry : entry.getValue().entrySet()) {
                portfolioConfig.set("portfolios." + uuid + "." + stockEntry.getKey(), stockEntry.getValue());
            }
        }
        try {
            portfolioConfig.save(portfolioFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startPriceUpdater() {
        // Every 5 minutes (6000 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                Random random = new Random();
                for (StockData stock : stocks.values()) {
                    // Random fluctuation between -5% and +5%
                    double change = 0.95 + (1.05 - 0.95) * random.nextDouble();
                    stock.price = Math.max(10, stock.price * change);
                    
                    // Update history (Keep last 10 entries)
                    stock.priceHistory.add(stock.price);
                    if (stock.priceHistory.size() > 10) {
                        stock.priceHistory.remove(0);
                    }
                }
                saveData();
                Bukkit.broadcastMessage("§e[주식] §f주식 시장의 가격이 변동되었습니다! §7(/주식)");
            }
        }.runTaskTimer(plugin, 6000L, 6000L);
    }

    public Map<String, StockData> getStocks() {
        return stocks;
    }

    public int getOwnedShares(UUID uuid, String stockId) {
        return portfolios.getOrDefault(uuid, new HashMap<>()).getOrDefault(stockId, 0);
    }

    public boolean buyStock(UUID uuid, String stockId, int amount) {
        StockData stock = stocks.get(stockId);
        if (stock == null) return false;

        double totalCost = stock.price * amount;
        if (plugin.getEconomyManager().withdrawBalance(uuid, totalCost)) {
            Map<String, Integer> playerStocks = portfolios.computeIfAbsent(uuid, k -> new HashMap<>());
            playerStocks.put(stockId, playerStocks.getOrDefault(stockId, 0) + amount);
            saveData();
            return true;
        }
        return false;
    }

    public boolean sellStock(UUID uuid, String stockId, int amount) {
        StockData stock = stocks.get(stockId);
        if (stock == null) return false;

        Map<String, Integer> playerStocks = portfolios.getOrDefault(uuid, new HashMap<>());
        int owned = playerStocks.getOrDefault(stockId, 0);

        if (owned >= amount) {
            playerStocks.put(stockId, owned - amount);
            double totalReturn = stock.price * amount;
            plugin.getEconomyManager().addBalance(uuid, totalReturn);
            saveData();
            return true;
        }
        return false;
    }

    public static class StockData {
        public String id;
        public String name;
        public double price;
        public String displayItem;
        public List<Double> priceHistory = new ArrayList<>();

        public StockData(String id, String name, double price, String displayItem) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.displayItem = displayItem;
        }
    }
}
