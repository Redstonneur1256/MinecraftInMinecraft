package fr.redstonneur1256.commands;

import fr.redstonneur1256.MinecraftInMinecraft;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        MinecraftInMinecraft plugin = JavaPlugin.getPlugin(MinecraftInMinecraft.class);
        plugin.clearBlocks();
        plugin.reloadConfig();
        plugin.updateBlocks();
        commandSender.sendMessage("Configuration reloaded successfully !");
        return false;
    }
}
