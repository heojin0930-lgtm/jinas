package me.heojin.economy.command;

import me.heojin.economy.EconomyPlugin;
import me.heojin.economy.gui.EconomyGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EconomyCommand implements CommandExecutor {
    private final EconomyPlugin plugin;

    public EconomyCommand(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("money")) {
            if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("확인")) || (args.length == 1 && args[0].equalsIgnoreCase("balance"))) {
                // Show balance
                double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
                player.sendMessage(ChatColor.GOLD + "현재 잔액: " + ChatColor.WHITE + String.format("%,.0f", balance) + "원");
                return true;
            }

            if (args[0].equalsIgnoreCase("gui")) {
                EconomyGUI.openGUI(player, plugin);
                return true;
            }

            // Admin: Give money
            if (args[0].equalsIgnoreCase("지급") || args[0].equalsIgnoreCase("give")) {
                if (!player.isOp()) {
                    player.sendMessage(ChatColor.RED + "이 명령어를 사용할 권한이 없습니다.");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "사용법: /돈 지급 <플레이어> <금액>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "해당 플레이어를 찾을 수 없습니다.");
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[2]);
                    plugin.getEconomyManager().addBalance(target.getUniqueId(), amount);
                    plugin.getEconomyManager().saveData();
                    player.sendMessage(ChatColor.GREEN + target.getName() + "님에게 " + String.format("%,.0f", amount) + "원을 지급했습니다.");
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "올바른 금액을 입력해주세요.");
                }
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("pay")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "사용법: /pay <플레이어> <금액>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "해당 플레이어를 찾을 수 없습니다. (현재 접속 중이어야 함)");
                return true;
            }

            if (target.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "자기 자신에게는 송금할 수 없습니다.");
                return true;
            }

            try {
                double amount = Double.parseDouble(args[1]);
                if (amount <= 0) {
                    player.sendMessage(ChatColor.RED + "금액은 0보다 커야 합니다.");
                    return true;
                }

                if (plugin.getEconomyManager().withdrawBalance(player.getUniqueId(), amount)) {
                    plugin.getEconomyManager().addBalance(target.getUniqueId(), amount);
                    
                    String formattedAmount = String.format("%,.0f", amount);
                    player.sendMessage(ChatColor.GREEN + target.getName() + "님에게 " + ChatColor.WHITE + formattedAmount + "원" + ChatColor.GREEN + "을 보냈습니다.");
                    target.sendMessage(ChatColor.GREEN + player.getName() + "님으로부터 " + ChatColor.WHITE + formattedAmount + "원" + ChatColor.GREEN + "을 받았습니다.");
                    
                    // Save data immediately for safety
                    plugin.getEconomyManager().saveData();
                } else {
                    player.sendMessage(ChatColor.RED + "잔액이 부족합니다.");
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "올바른 금액을 입력해주세요.");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("shop")) {
            me.heojin.economy.gui.ShopGUI.openShop(player, plugin);
            return true;
        }

        if (command.getName().equalsIgnoreCase("stock")) {
            me.heojin.economy.gui.StockGUI.openStockGUI(player, plugin);
            return true;
        }

        if (command.getName().equalsIgnoreCase("sell")) {
            me.heojin.economy.gui.SellGUI.openSellGUI(player, plugin);
            return true;
        }

        return false;
    }
}
