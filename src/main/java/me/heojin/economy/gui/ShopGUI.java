package me.heojin.economy.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopGUI {

    public static final String SHOP_NAME = "§6§l[ 서버 상점 ]";

    public static void openShop(Player player, me.heojin.economy.EconomyPlugin plugin) {
        Inventory shop = Bukkit.createInventory(null, 27, SHOP_NAME);

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
        shop.setItem(4, profile);

        me.heojin.economy.manager.StockManager stockManager = plugin.getStockManager();

        // 1. 다이아몬드 (다이아몬드 거래소 시세 연동)
        double diamondPrice = stockManager.getStocks().containsKey("DIA_BANK") ? stockManager.getStocks().get("DIA_BANK").price : 10000;
        addItem(shop, 11, Material.DIAMOND, "§b§l다이아몬드", diamondPrice);

        // 2. 금사과 (레드스톤 전자 시세 연동)
        double applePrice = stockManager.getStocks().containsKey("REDSTONE_TECH") ? stockManager.getStocks().get("REDSTONE_TECH").price : 5000;
        addItem(shop, 13, Material.GOLDEN_APPLE, "§e§l금사과", applePrice);

        // 3. 네더라이트 주괴 (엔더 연구소 시세 연동)
        double netheritePrice = stockManager.getStocks().containsKey("ENDER_LABS") ? stockManager.getStocks().get("ENDER_LABS").price : 50000;
        addItem(shop, 15, Material.NETHERITE_INGOT, "§8§l네더라이트 주괴", netheritePrice);

        // 배경 유리판 채우기
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }

        for (int i = 0; i < 27; i++) {
            if (shop.getItem(i) == null) {
                shop.setItem(i, glass);
            }
        }

        player.openInventory(shop);
    }

    private static void addItem(Inventory inv, int slot, Material material, String name, double price) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§f가격: §e" + String.format("%,.0f", price) + "원");
            lore.add("§7클릭 시 구매합니다.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        inv.setItem(slot, item);
    }
}
