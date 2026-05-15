package me.heojin.economy.gui;

import me.heojin.economy.EconomyPlugin;
import me.heojin.economy.manager.StockManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class StockGUI {
    public static void openStockGUI(Player player, EconomyPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, "§0주식 시장");

        StockManager stockManager = plugin.getStockManager();
        int slot = 10;

        for (StockManager.StockData stock : stockManager.getStocks().values()) {
            Material material = Material.valueOf(stock.displayItem);
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName("§e" + stock.name);
                List<String> lore = new ArrayList<>();
                lore.add("§7현재가: §f" + String.format("%,.0f", stock.price) + "원");
                lore.add("§7보유량: §b" + stockManager.getOwnedShares(player.getUniqueId(), stock.id) + "주");
                lore.add("");
                lore.add("§8[ 시세 차트 (최근 10회) ]");
                
                // 시세 히스토리 그래프 생성
                StringBuilder graph = new StringBuilder("§f");
                List<Double> history = stock.priceHistory;
                for (int i = 0; i < history.size(); i++) {
                    double p = history.get(i);
                    if (i > 0) {
                        double prev = history.get(i-1);
                        if (p > prev) graph.append("§c▲");
                        else if (p < prev) graph.append("§b▼");
                        else graph.append("§7-");
                    } else {
                        graph.append("§7•");
                    }
                }
                lore.add(graph.toString());
                
                // 최근 3개 가격 상세 표시
                int start = Math.max(0, history.size() - 3);
                StringBuilder detail = new StringBuilder("§7");
                for (int i = start; i < history.size(); i++) {
                    detail.append(String.format("%,.0f", history.get(i)));
                    if (i < history.size() - 1) detail.append(" → ");
                }
                lore.add(detail.toString());
                lore.add("");
                lore.add("§a[좌클릭] §f1주 매수");
                lore.add("§c[우클릭] §f1주 매도");
                lore.add("§e[Shift+클릭] §f전량 매도");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            inv.setItem(slot, item);
            slot += 2; // Spread them out
            if (slot > 16) break;
        }

        // Fill background
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
        }

        player.openInventory(inv);
    }
}
