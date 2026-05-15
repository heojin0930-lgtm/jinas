package me.heojin.economy.gui;

import me.heojin.economy.EconomyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class EconomyGUI {
    public static final String GUI_NAME = ChatColor.DARK_BLUE + "경제 시스템";

    public static void openGUI(Player player, EconomyPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_NAME);

        // Get Balance and Stock Value
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        double stockValue = 0;
        me.heojin.economy.manager.StockManager stockManager = plugin.getStockManager();
        for (me.heojin.economy.manager.StockManager.StockData stock : stockManager.getStocks().values()) {
            int owned = stockManager.getOwnedShares(player.getUniqueId(), stock.id);
            stockValue += stock.price * owned;
        }
        double totalAssets = balance + stockValue;

        // Balance Item
        ItemStack balanceItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta balanceMeta = balanceItem.getItemMeta();
        if (balanceMeta != null) {
            balanceMeta.setDisplayName(ChatColor.GOLD + "§l[ 내 자산 현황 ]");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "플레이어: " + player.getName());
            lore.add("");
            lore.add("§f보유 현금: §e" + String.format("%,.0f", balance) + "원");
            lore.add("§f주식 가치: §b" + String.format("%,.0f", stockValue) + "원");
            lore.add("§8--------------------");
            lore.add("§f총 자산: §a§l" + String.format("%,.0f", totalAssets) + "원");
            balanceMeta.setLore(lore);
            balanceItem.setItemMeta(balanceMeta);
        }

        inv.setItem(13, balanceItem);

        // Decoration
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
        }

        player.openInventory(inv);
    }
}
