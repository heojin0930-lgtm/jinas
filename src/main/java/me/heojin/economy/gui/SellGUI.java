package me.heojin.economy.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SellGUI {
    public static final String SELL_NAME = "§2§l[ 서버 판매 상점 ]";

    public static void openSellGUI(Player player, me.heojin.economy.EconomyPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, SELL_NAME);

        // 내 정보 (현재 잔액 표시)
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        ItemStack profile = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta profileMeta = profile.getItemMeta();
        if (profileMeta != null) {
            profileMeta.setDisplayName("§e§l[ 내 정보 ]");
            List<String> lore = new ArrayList<>();
            lore.add("§f현재 접속: §b" + player.getName());
            lore.add("§f보유 잔액: §6" + String.format("%,.0f", balance) + "원");
            profileMeta.setLore(lore);
            profile.setItemMeta(profileMeta);
        }
        inv.setItem(4, profile);

        me.heojin.economy.manager.StockManager stockManager = plugin.getStockManager();

        // Selling items list (주식 시세의 90% 가격으로 매입)
        double diamondPrice = (stockManager.getStocks().containsKey("DIA_BANK") ? stockManager.getStocks().get("DIA_BANK").price : 10000) * 0.9;
        double goldPrice = (stockManager.getStocks().containsKey("REDSTONE_TECH") ? stockManager.getStocks().get("REDSTONE_TECH").price : 7000) * 0.9;
        double ironPrice = (stockManager.getStocks().containsKey("IRON_WORKS") ? stockManager.getStocks().get("IRON_WORKS").price : 1500) * 0.9;

        addItem(inv, 10, Material.DIAMOND, "§b§l다이아몬드 판매", diamondPrice);
        addItem(inv, 12, Material.GOLD_INGOT, "§e§l금 주괴 판매", goldPrice);
        addItem(inv, 14, Material.IRON_INGOT, "§f§l철 주괴 판매", ironPrice);
        addItem(inv, 16, Material.COAL, "§8§l석탄 판매", 40); // 석탄은 고정가

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

    private static void addItem(Inventory inv, int slot, Material material, String name, double price) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§f개당 판매가: §a" + String.format("%,.0f", price) + "원");
            lore.add("§7클릭 시 인벤토리의 해당 아이템을");
            lore.add("§7모두 판매합니다.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        inv.setItem(slot, item);
    }
}
