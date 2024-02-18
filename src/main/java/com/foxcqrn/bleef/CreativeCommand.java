package com.foxcqrn.bleef;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CreativeCommand extends Command {

    public CreativeCommand() {
        super("creative");
    }

    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer)sender;
            p.sendMessage(new ComponentBuilder("Teleporting to creative world...").color(ChatColor.GREEN).create());
            p.connect(ProxyServer.getInstance().getServerInfo("creative"));
        }
    }
}

