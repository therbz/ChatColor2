package com.sulphate.chatcolor2.commands;

import com.sulphate.chatcolor2.main.MainClass;
import com.sulphate.chatcolor2.schedulers.ConfirmScheduler;
import com.sulphate.chatcolor2.utils.CCStrings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.util.Arrays;
import java.util.List;

public class ChatColorCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!MainClass.getPluginEnabled()) {
            return true;
        }

        int argsno = args.length;

        if (sender instanceof Player) {

            Player s = (Player) sender;
            if (!checkCommand(args, s)) {
                return true;
            }

            List<String> cmds = Arrays.asList("help", "commandshelp", "permissionshelp", "settingshelp", "set", "reset", "reloadmessages", "enable", "available");
            if (cmds.contains(args[0].toLowerCase())) {
                switch (args[0].toLowerCase()) {
                    case "help":
                    case "commandshelp": {
                        handleCommandsHelp(s);
                        return true;
                    }
                    case "permissionshelp": {
                        handlePermissionsHelp(s);
                        return true;
                    }
                    case "settingshelp": {
                        handleSettingsHelp(s);
                        return true;
                    }
                    case "set": {
                        handleSet(args, s);
                        return true;
                    }
                    case "reset": {
                        s.sendMessage(CCStrings.confirm);
                        ConfirmScheduler cs = new ConfirmScheduler();
                        MainClass.get().addConfirmee(s, cs);
                        cs.confirm(s, "reset", null);
                        return true;
                    }
                    case "reloadmessages": {
                        MainClass.getUtils().loadMessages();
                        CCStrings.reloadMessages();
                        s.sendMessage(CCStrings.reloadedmessages);
                        return true;
                    }
                    case "enable": {
                        if (MainClass.getPluginEnabled()) {
                            s.sendMessage(CCStrings.alreadyenabled);
                            return true;
                        } else {
                            try {
                                MainClass.get().checkConfig();
                                MainClass.getUtils().loadAllData();
                            } catch (Exception e) {
                                s.sendMessage(CCStrings.internalerror);
                                s.sendMessage(CCStrings.failedtoenable);
                                return true;
                            }
                        }
                        s.sendMessage(CCStrings.successfullyenabled);
                        return true;
                    }
                    case "available": {
                        String comma = "§7, ";
                        char[] cols = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
                        char[] mods = {'k', 'l', 'm', 'n', 'o'};

                        String colstring;
                        String modstring;

                        StringBuilder colbuilder = new StringBuilder();
                        for (int i = 0; i < cols.length; i++) {
                            if (hasPermission("chatcolor.color." + cols[i], s)) {
                                colbuilder.append(ChatColor.COLOR_CHAR).append(cols[i]).append(cols[i]);

                                if (i != cols.length - 1) {
                                    colbuilder.append(comma);
                                }
                            }
                        }
                        colstring = colbuilder.toString();

                        StringBuilder modbuilder = new StringBuilder();
                        for (int i = 0; i < mods.length; i++) {
                            if (hasPermission("chatcolor.modifier." + mods[i], s)) {
                                modbuilder.append(ChatColor.COLOR_CHAR).append("b").append(ChatColor.COLOR_CHAR).append(mods[i]).append(mods[i]);

                                if (i != mods.length - 1) {
                                    modbuilder.append(comma);
                                }
                            }
                        }
                        modstring = modbuilder.toString();

                        s.sendMessage(CCStrings.prefix + "Here are your available colors and modifiers:");
                        s.sendMessage(" §7- §eColors: " + colstring);
                        s.sendMessage(" §7- §eModifiers: " + modstring);
                        return true;
                    }
                }
            }

            if (MainClass.getUtils().getUUID(args[0]) != null) {
                String uuid = MainClass.getUtils().getUUID(args[0]);
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    if (i == 1) {
                        sb.append(getColor(args[i]));
                        continue;
                    }
                    sb.append(getModifier(args[i]));
                }
                String color = sb.toString();
                if (color.contains("rainbow")) {
                    char[] seq = getCurrentRainbowSequence();
                    StringBuilder sb2 = new StringBuilder();
                    String mods = color.replace("rainbow", "");
                    for (char c : seq) {
                        sb2.append("&" + c + mods + c);
                    }
                    String end = colorString(sb2.toString());
                    MainClass.getUtils().setColor(uuid, colorString(color));
                    s.sendMessage(CCStrings.setotherscolor.replace("[player]", args[0]) + end);
                    if ((boolean) MainClass.getUtils().getSetting("notify-others") && Bukkit.getPlayer(args[0]) != null) {
                        Bukkit.getPlayer(args[0]).sendMessage(CCStrings.playersetyourcolor.replace("[player]", s.getName()) + end);
                    }
                    return true;
                }
                MainClass.getUtils().setColor(uuid, colorString(color));
                s.sendMessage(CCStrings.setotherscolor.replace("[player]", args[0]) + colorString(color) + CCStrings.colthis);
                if ((boolean) MainClass.getUtils().getSetting("notify-others") && Bukkit.getPlayer(args[0]) != null) {
                    Bukkit.getPlayer(args[0]).sendMessage(CCStrings.playersetyourcolor.replace("[player]", s.getName()) + colorString(color) + CCStrings.colthis);
                }
                return true;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i == 0) {
                    sb.append(getColor(args[i]));
                    continue;
                }
                sb.append(getModifier(args[i]));
            }
            String color = sb.toString();
            String uuid = s.getUniqueId().toString();
            if (color.contains("rainbow")) {
                char[] seq = getCurrentRainbowSequence();
                StringBuilder sb2 = new StringBuilder();
                String mods = color.replace("rainbow", "");
                for (char c : seq) {
                    sb2.append("&" + c + mods + c);
                }
                String end = colorString(sb2.toString());
                MainClass.getUtils().setColor(uuid, colorString(color));
                s.sendMessage(CCStrings.setowncolor + end);
                return true;
            }
            MainClass.getUtils().setColor(uuid, colorString(color));
            s.sendMessage(CCStrings.setowncolor + colorString(color) + CCStrings.colthis);
            return true;

        } else {
            if (argsno < 2) {
                sender.sendMessage(CCStrings.notenoughargs);
                return true;
            }
            if (argsno > 6) {
                sender.sendMessage(CCStrings.toomanyargs);
                return true;
            }
            if (MainClass.getUtils().getUUID(args[0]) != null) {
                String uuid = MainClass.getUtils().getUUID(args[0]);
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    if (i == 1) {
                        sb.append(getColor(args[i]));
                        continue;
                    }
                    sb.append(getColor(args[i]));
                }
                String color = sb.toString();
                if (color.contains("rainbow")) {
                    char[] seq = getCurrentRainbowSequence();
                    StringBuilder sb2 = new StringBuilder();
                    String mods = color.replace("rainbow", "");
                    for (char c : seq) {
                        sb2.append("&" + c + mods + c);
                    }
                    String end = colorString(sb2.toString());
                    MainClass.getUtils().setColor(uuid, colorString(color));
                    sender.sendMessage(CCStrings.setotherscolor.replace("[player]", args[0]) + end);
                    if ((boolean) MainClass.getUtils().getSetting("notify-others") && Bukkit.getPlayer(args[0]) != null) {
                        Bukkit.getPlayer(args[0]).sendMessage(CCStrings.playersetyourcolor.replace("[player]", "CONSOLE") + end);
                    }
                    return true;
                }
                MainClass.getUtils().setColor(uuid, colorString(color));
                sender.sendMessage(CCStrings.setotherscolor.replace("[player]", args[0]) + colorString(color) + CCStrings.colthis);
                if ((boolean) MainClass.getUtils().getSetting("notify-others") && Bukkit.getPlayer(args[0]) != null) {
                    Bukkit.getPlayer(args[0]).sendMessage(CCStrings.playersetyourcolor.replace("[player]", "CONSOLE") + colorString(color) + CCStrings.colthis);
                }
                return true;
            } else {
                sender.sendMessage(CCStrings.playernotjoined);
            }
        }
        return true;

    }

    // Checks the command given, including any permissions / invalid commands.
    public boolean checkCommand(String[] args, Player player) {

        String uuid = player.getUniqueId().toString();

        if (args.length == 0) {
            String color = MainClass.getUtils().getColor(uuid);
            if (color.contains("rainbow")) {
                char[] seq = getCurrentRainbowSequence();
                StringBuilder sb = new StringBuilder();
                String mods = color.replace("rainbow", "");
                for (char c : seq) {
                    sb.append("&" + c + mods + c);
                }
                String end = sb.toString();
                player.sendMessage(CCStrings.currentcolor + end);
                return false;
            }
            player.sendMessage(CCStrings.currentcolor + color + CCStrings.colthis);
            return false;
        }

        if (!hasPermission("chatcolor.use", player)) {
            player.sendMessage(CCStrings.nopermissions);
            return false;
        }

        // args is at least 1 in length.
        List<String> cmds = Arrays.asList("set", "reloadmessages", "reset", "enable", "help", "permissionshelp", "commandshelp", "settingshelp", "available");
        if (cmds.contains(args[0])) {
            if (args[0].equalsIgnoreCase("set") && args.length < 3) {
                player.sendMessage(CCStrings.notenoughargs);
                return false;
            }

            List<String> settings = Arrays.asList("auto-save", "color-override", "notify-others", "join-message", "confirm-timeout", "default-color", "rainbow-sequence", "command-name");
            if (args[0].equalsIgnoreCase("set") && !settings.contains(args[1])) {
                player.sendMessage(CCStrings.invalidsetting + colorString("&e" + args[1]));
                return false;
            }
            if ((args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("reset")) && MainClass.get().getConfirmees().containsKey(player)) {
                player.sendMessage(CCStrings.alreadyconfirming);
                return false;
            }
            if (!hasPermission("chatcolor.admin." + args[0], player) && !(args[0].equals("commandshelp") || args[0].equals("available"))) {
                player.sendMessage(CCStrings.nopermissions);
                return false;
            }

            return true;
        }

        if (MainClass.getUtils().getUUID(args[0]) != null) {
            if (!hasPermission("chatcolor.change.others", player)) {
                player.sendMessage(CCStrings.nopermissions);
                return false;
            }
            if (args.length > 7) {
                player.sendMessage(CCStrings.toomanyargs);
                return false;
            }
            if (args.length < 2) {
                player.sendMessage(CCStrings.notenoughargs);
                return false;
            }
            if (getColor(args[1]) != null) {
                for (int i = 1; i < args.length; i++) {
                    if (i == 1) {
                        if (!hasPermission("chatcolor.color." + args[1], player)) {
                            player.sendMessage(CCStrings.nocolorperms + args[1] + args[1]);
                            return false;
                        }
                        continue;
                    }
                    if (getModifier(args[i]) == null) {
                        player.sendMessage(CCStrings.invalidmodifier + args[i]);
                        return false;
                    }
                    if (!hasPermission("chatcolor.modifier." + args[i], player)) {
                        player.sendMessage(CCStrings.nomodperms + args[i] + args[i]);
                        return false;
                    }
                }
                return true;
            }
            if (args[1].length() == 1) {
                player.sendMessage(CCStrings.invalidcolor + args[1]);
            }
            player.sendMessage(CCStrings.invalidcommand);
            return false;
        }

        if (getColor(args[0]) != null) {
            if (!hasPermission("chatcolor.change.self", player)) {
                player.sendMessage(CCStrings.nopermissions);
                return false;
            }
            if (args.length > 6) {
                player.sendMessage(CCStrings.toomanyargs);
                return false;
            }
            for (int i = 0; i < args.length; i++) {
                if (i == 0) {
                    if (!hasPermission("chatcolor.color." + args[0], player)) {
                        player.sendMessage(CCStrings.nocolorperms + args[0] + args[0]);
                        return false;
                    }
                    continue;
                }
                if (getModifier(args[i]) == null) {
                    player.sendMessage(CCStrings.invalidmodifier + args[i]);
                    return false;
                }
                if (!hasPermission("chatcolor.modifier." + args[i], player)) {
                    player.sendMessage(CCStrings.nomodperms + args[i] + args[i]);
                    return false;
                }
            }
            return true;
        }
        if (args[0].length() == 1) {
            player.sendMessage(CCStrings.invalidcolor + args[0]);
            return false;
        }

        player.sendMessage(CCStrings.invalidcommand);
        return false;

    }

    public boolean hasPermission(String permission, Player player) {
        int d1 = permission.indexOf(".");
        String root = permission.substring(0, d1 + 1);
        String s = permission.replace(root, "");
        int d2 = s.indexOf(".");
        String cat = s.substring(0, d2 + 1);
        if (!player.hasPermission(permission) && !player.hasPermission(root + cat + ".*") && !player.hasPermission(root + ".*")) {
            return false;
        }
        return true;
    }

    //This is how the help command will be handled.
    public void handleCommandsHelp(Player player) {
        player.sendMessage(CCStrings.prefix + "Displaying command help!");
        player.sendMessage(colorString(" &7- &eMain Command: &c/chatcolor <color> [modifiers]"));
        player.sendMessage("");
        player.sendMessage(colorString(" &eOther Commands:"));
        player.sendMessage(colorString(" &7- &eCommands Help: &c/chatcolor commandshelp"));
        player.sendMessage(colorString(" &7- &eSee Available Colors: &c/chatcolor available"));
        if (hasPermission("chatcolor.admin.permissionshelp", player)) {
            player.sendMessage(colorString(" &7- &ePermissions Help: &c/chatcolor permissionshelp"));
        }
        if (hasPermission("chatcolor.admin.settingshelp", player)) {
            player.sendMessage(colorString(" &7- &eSettings Help: &c/chatcolor settingshelp"));
        }
        if (hasPermission("chatcolor.admin.reloadmessages", player)) {
            player.sendMessage(colorString(" &7- &eReload Messages: &c/chatcolor reloadmessages"));
        }
        if (hasPermission("chatcolor.admin.reset", player)) {
            player.sendMessage(colorString(" &7- &eReset Config: &c/chatcolor reset"));
        }
        if (hasPermission("chatcolor.admin.set", player)) {
            player.sendMessage(colorString(" &7- &eSet Settings: &c/chatcolor set <setting> <value>"));
        }
        if (hasPermission("chatcolor.admin.enable", player)) {
            player.sendMessage(colorString(" &7- &eEnable Plugin: &c/chatcolor enable"));
        }
        player.sendMessage("");
        player.sendMessage(colorString("&eValid Colors:"));
        player.sendMessage(colorString("&00&11&22&33&44&55&66&77&88&99"));
        player.sendMessage(colorString("&aa&bb&cc&dd&ee&ff"));
        player.sendMessage("");
        player.sendMessage(colorString("&eAlternatively:"));
        player.sendMessage(colorString("&0black, &1dark.blue, &2green, &3dark.aqua,"));
        player.sendMessage(colorString("&4red, &5purple, &6gold, &7grey, &8dark.grey, &9blue"));
        player.sendMessage(colorString("&alight.green, &baqua, &clight.red, &dmagenta, &eyellow, &fwhite"));
        player.sendMessage("");
        player.sendMessage(colorString("&eValid modifiers:"));
        player.sendMessage(colorString("&ck, &c&ll&r, &c&mm&r, &c&nn&r, &c&oo"));
        player.sendMessage("");
        player.sendMessage(colorString("&eAlternatively:"));
        player.sendMessage(colorString("&cobfuscated, &c&lbold&r, &c&mstrikethrough&r, &c&nunderlined&r, &c&oitalic"));

        // Send the author messages.
        player.sendMessage("");
        player.sendMessage(CCStrings.authormessage1);
        player.sendMessage(CCStrings.authormessage2);
    }

    public void handlePermissionsHelp(Player player) {
        player.sendMessage(CCStrings.prefix + "Displaying permissions help!");
        player.sendMessage(colorString(" &7- &eMain Permission: &cchatcolor.use"));
        player.sendMessage(colorString(" &7- &eAll Perms: &cchatcolor.*"));
        player.sendMessage("");
        player.sendMessage(colorString("&eAdmin Permissions:"));
        player.sendMessage(colorString(" &7- &ePermissions Help: &cchatcolor.admin.permissionshelp"));
        player.sendMessage(colorString(" &7- &eReload Messages: &cchatcolor.admin.reloadmessages"));
        player.sendMessage(colorString(" &7- &eReset Config: &cchatcolor.admin.reset"));
        player.sendMessage(colorString(" &7- &eSet Settings: &cchatcolor.admin.set"));
        player.sendMessage(colorString(" &7- &eEnable Plugin: &cchatcolor.admin.enable"));
        player.sendMessage(colorString(" &7- &eAll Admin Perms: &cchatcolor.admin.*"));
        player.sendMessage("");
        player.sendMessage(colorString("&eColor Permissions:"));
        player.sendMessage(colorString(" &7- &ePermission: &cchatcolor.color.<color>"));
        player.sendMessage(colorString(" &7- &eExample: &cchatcolor.color.a"));
        player.sendMessage(colorString("&eNote: &cchatcolor.color.rainbow &ecan be used, but no other words."));
        player.sendMessage("");
        player.sendMessage(colorString("&eModifier Permissions:"));
        player.sendMessage(colorString(" &7- &ePermission: &cchatcolor.modifier.<modifier>"));
        player.sendMessage(colorString(" &7- &eExample: &cchatcolor.modifier.k"));
        player.sendMessage(colorString("&eNote: No words may be used."));
        player.sendMessage("");
        player.sendMessage(colorString("&eOther Permissions:"));
        player.sendMessage(colorString(" &7- &eChange Own Color: &cchatcolor.change.self"));
        player.sendMessage(colorString(" &7- &eChange Other's Color: &cchatcolor.change.others"));

        // Send the author messages.
        player.sendMessage("");
        player.sendMessage(CCStrings.authormessage1);
        player.sendMessage(CCStrings.authormessage2);
    }

    public void handleSettingsHelp(Player player) {
        player.sendMessage(CCStrings.prefix + "Displaying settings help!");
        player.sendMessage(colorString(" &7- &eauto-save: &cAuto save data every 5 minutes."));
        player.sendMessage(colorString("   &eUsage: &b/chatcolor set auto-save <true/false>"));
        player.sendMessage("");
        player.sendMessage(colorString(" &7- &ecolor-override: &cOverride '&' symbols in chat."));
        player.sendMessage(colorString("   &eUsage: &b/chatcolor set color-override <true/false>"));
        player.sendMessage("");
        player.sendMessage(colorString(" &7- &econfirm-timeout: &cSet time for confirming settings."));
        player.sendMessage(colorString("   &eUsage: &b/chatcolor set confirm-timeout <seconds>"));
        player.sendMessage("");
        player.sendMessage(colorString(" &7- &edefault-color: &cChange the default color."));
        player.sendMessage(colorString("   &eUsage: &b/chatcolor set default-color <color> <modifiers..>"));
        player.sendMessage("");
        player.sendMessage(colorString(" &7- &ejoin-message: &cTell players their color on join."));
        player.sendMessage(colorString("   &eUsage: &b/chatcolor set join-message <true/false>"));
        player.sendMessage("");
        player.sendMessage(colorString(" &7- &enotify-others: &cTell others if you change their color."));
        player.sendMessage(colorString("   &eUsage: &b/chatcolor set notify-others <true/false>"));
        player.sendMessage("");
        player.sendMessage(colorString(" &7- &erainbow-sequence: &cChange the rainbow chatcolor sequence."));
        player.sendMessage(colorString("   &eUsage: &b/chatcolor set rainbow-sequence <sequence>"));
        player.sendMessage("");

        // Send the author messages.
        player.sendMessage("");
        player.sendMessage(CCStrings.authormessage1);
        player.sendMessage(CCStrings.authormessage2);
    }

    public void handleSet(String[] args, Player player) {
        switch (args[1]) {

            case "auto-save": {
                boolean val;
                try {
                    val = Boolean.parseBoolean(args[2]);
                } catch (Exception e) {
                    player.sendMessage(CCStrings.needsboolean);
                    return;
                }
                boolean autosave = (boolean) MainClass.getUtils().getSetting("auto-save");
                if (val == autosave) {
                    player.sendMessage(CCStrings.alreadyset);
                    return;
                }
                String col = val ? "§aTRUE" : "§cFALSE";
                String oppcol = val ? "§cFALSE" : "§aTRUE";
                player.sendMessage(CCStrings.prefix + "§c" + args[1] + CCStrings.iscurrently + oppcol);
                player.sendMessage(CCStrings.tochange + col);
                player.sendMessage(CCStrings.confirm);
                ConfirmScheduler cs = new ConfirmScheduler();
                MainClass.get().addConfirmee(player, cs);
                cs.confirm(player, "auto-save", val);
                return;
            }

            case "color-override": {
                boolean val;
                try {
                    val = Boolean.parseBoolean(args[2]);
                } catch (Exception e) {
                    player.sendMessage(CCStrings.needsboolean);
                    return;
                }
                boolean override = (boolean) MainClass.getUtils().getSetting("color-override");
                if (val == override) {
                    player.sendMessage(CCStrings.alreadyset);
                    return;
                }
                String col = val ? "§aTRUE" : "§cFALSE";
                String oppcol = val ? "§cFALSE" : "§aTRUE";
                player.sendMessage(CCStrings.prefix + "§c" + args[1] + CCStrings.iscurrently + oppcol);
                player.sendMessage(CCStrings.tochange + col);
                player.sendMessage(CCStrings.confirm);
                ConfirmScheduler cs = new ConfirmScheduler();
                MainClass.get().addConfirmee(player, cs);
                cs.confirm(player, "color-override", val);
                return;
            }

            case "notify-others": {
                boolean val = false;
                try {
                    val = Boolean.parseBoolean(args[2]);
                } catch (Exception e) {
                    player.sendMessage(CCStrings.needsboolean);
                    return;
                }
                boolean notify = (boolean) MainClass.getUtils().getSetting("notify-others");
                if (val == notify) {
                    player.sendMessage(CCStrings.alreadyset);
                    return;
                }
                String col = val ? "§aTRUE" : "§cFALSE";
                String oppcol = val ? "§cFALSE" : "§aTRUE";
                player.sendMessage(CCStrings.prefix + "§c" + args[1] + CCStrings.iscurrently + oppcol);
                player.sendMessage(CCStrings.tochange + col);
                player.sendMessage(CCStrings.confirm);
                ConfirmScheduler cs = new ConfirmScheduler();
                MainClass.get().addConfirmee(player, cs);
                cs.confirm(player, "notify-others", val);
                return;
            }

            case "join-message": {
                boolean val = false;
                try {
                    val = Boolean.parseBoolean(args[2]);
                } catch (Exception e) {
                    player.sendMessage(CCStrings.needsboolean);
                    return;
                }
                boolean notify = (boolean) MainClass.getUtils().getSetting("join-message");
                if (val == notify) {
                    player.sendMessage(CCStrings.alreadyset);
                    return;
                }
                String col = val ? "§aTRUE" : "§cFALSE";
                String oppcol = val ? "§cFALSE" : "§aTRUE";
                player.sendMessage(CCStrings.prefix + "§c" + args[1] + CCStrings.iscurrently + oppcol);
                player.sendMessage(CCStrings.tochange + col);
                player.sendMessage(CCStrings.confirm);
                ConfirmScheduler cs = new ConfirmScheduler();
                MainClass.get().addConfirmee(player, cs);
                cs.confirm(player, "join-message", val);
                return;
            }

            case "confirm-timeout": {
                int val = 0;
                try {
                    val = Integer.parseInt(args[2]);
                } catch (Exception e) {
                    player.sendMessage(CCStrings.needsinteger);
                    return;
                }
                player.sendMessage(CCStrings.prefix + "§c" + args[1] + CCStrings.iscurrently + "§c" + (int) MainClass.getUtils().getSetting("confirm-timeout") + " seconds");
                player.sendMessage(CCStrings.tochange + "§c" + val);
                player.sendMessage(CCStrings.confirm);
                ConfirmScheduler cs = new ConfirmScheduler();
                MainClass.get().addConfirmee(player, cs);
                cs.confirm(player, "confirm-timeout", val);
            }

            case "default-color": {
                String color = getColor(args[2]);
                if (color == null) {
                    player.sendMessage(CCStrings.invalidcolor);
                    return;
                }
                for (int i = 3; i < args.length; i++) {
                    String mod = getModifier(args[i]);
                    if (mod == null) {
                        player.sendMessage(CCStrings.invalidmodifier);
                        return;
                    } else {
                        color = color + mod;
                    }
                }
                String defcol = (String) MainClass.getUtils().getSetting("default-color");
                player.sendMessage(CCStrings.prefix + "§c" + args[1] + CCStrings.iscurrently + defcol.replace('&', ChatColor.COLOR_CHAR) + CCStrings.colthis);
                player.sendMessage(CCStrings.tochange + color.replace('&', ChatColor.COLOR_CHAR) + CCStrings.colthis);
                player.sendMessage(CCStrings.confirm);
                ConfirmScheduler cs = new ConfirmScheduler();
                MainClass.get().addConfirmee(player, cs);
                cs.confirm(player, "default-color", color);
                return;
            }

            case "rainbow-sequence": {
                String seq = args[2];
                if (!verifyRainbowSequence(seq, false)) {
                    player.sendMessage(CCStrings.prefix + "§e" + args[2] + " §cis an invalid color sequence!");
                    player.sendMessage(CCStrings.help);
                    return;
                }
                char[] rseq = getCurrentRainbowSequence();
                StringBuilder sb = new StringBuilder();
                for (char chr : rseq) {
                    sb.append("&" + chr + chr);
                }
                String curseq = colorString(sb.toString());

                char[] newseq = args[2].toCharArray();
                StringBuilder sq = new StringBuilder();
                for (char chr : newseq) {
                    sq.append("&" + chr + chr);
                }
                String rcs = colorString(sq.toString());
                player.sendMessage(CCStrings.prefix + "§c" + args[1] + CCStrings.iscurrently + curseq);
                player.sendMessage(CCStrings.tochange + rcs);
                player.sendMessage(CCStrings.confirm);
                ConfirmScheduler cs = new ConfirmScheduler();
                MainClass.get().addConfirmee(player, cs);
                cs.confirm(player, "rainbow-sequence", seq);
                return;
            }

            case "command-name": {
                String cmd = args[2];
                for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
                    if (p.getDescription().getCommands() != null) {
                        if (p.getDescription().getCommands().containsKey(cmd)) {
                            player.sendMessage(CCStrings.commandexists);
                            return;
                        }
                    }
                }
                player.sendMessage(CCStrings.prefix + colorString("&c" + args[1]) + CCStrings.iscurrently + colorString("&c/") + (String) MainClass.getUtils().getSetting("command-name"));
                player.sendMessage(CCStrings.tochange + colorString("&c/" + cmd));
                player.sendMessage(CCStrings.confirm);
                ConfirmScheduler cs = new ConfirmScheduler();
                MainClass.get().addConfirmee(player, cs);
                cs.confirm(player, "command-name", cmd);
            }

        }
    }

    public String colorString(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public boolean verifyRainbowSequence(String seq, boolean replace) {

        boolean verify = true;
        List<String> cols = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f");
        String[] chars = seq.split("");
        for (String s : chars) {
            if (!cols.contains(s)) {
                verify = false;
            }
        }
        if (replace && !verify) {
            MainClass.getUtils().setSetting("rainbow-sequence", "abcde");
        }
        return verify;
    }

    public String getColor(String str) {
        String s = str.toLowerCase();
        if (s.equalsIgnoreCase("rainbow")) {
            return s;
        }
        List<String> words = Arrays.asList("black", "dark.blue", "green", "dark.aqua", "red", "purple", "gold", "gray", "dark.grey", "blue", "light.green", "aqua", "light.red", "magenta", "yellow", "white");
        if (words.contains(s)) {
            for (Integer i = 0; i < words.size(); i++) {
                if (i == 10) {
                    break;
                }
                String st = words.get(i);
                if (s.equalsIgnoreCase(st)) {
                    return "&" + i.toString();
                }
            }
            if (s.equalsIgnoreCase("light.green")) {
                return "&a";
            } else if (s.equalsIgnoreCase("aqua")) {
                return "&b";
            } else if (s.equalsIgnoreCase("light.red")) {
                return "&c";
            } else if (s.equalsIgnoreCase("magenta")) {
                return "&d";
            } else if (s.equalsIgnoreCase("yellow")) {
                return "&e";
            } else if (s.equalsIgnoreCase("white")) {
                return "&f";
            }
        }
        List<String> other = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f");
        if (other.contains(s)) {
            return "&" + s;
        } else {
            return null;
        }
    }

    public String getModifier(String str) {
        String s = str.toLowerCase();
        if (s.equalsIgnoreCase("obfuscated")) {
            return "&k";
        } else if (s.equalsIgnoreCase("bold")) {
            return "&l";
        } else if (s.equalsIgnoreCase("strikethrough")) {
            return "&m";
        } else if (s.equalsIgnoreCase("underlined")) {
            return "&n";
        } else if (s.equalsIgnoreCase("italic")) {
            return "&o";
        } else {
            List<String> other = Arrays.asList("k", "l", "m", "n", "o");
            if (other.contains(s)) {
                return "&" + s;
            }
            return null;
        }
    }

    private char[] getCurrentRainbowSequence() {
        verifyRainbowSequence((String) MainClass.getUtils().getSetting("rainbow-sequence"), true);
        String seqstr = (String) MainClass.getUtils().getSetting("rainbow-sequence");
        return seqstr.toCharArray();
    }

}
