package com.sulphate.chatcolor2.utils;

import com.sulphate.chatcolor2.managers.CustomColoursManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeneralUtils {

    private final ConfigUtils configUtils;
    private final CustomColoursManager customColoursManager;
    private final Messages M;

    public GeneralUtils(ConfigUtils configUtils, CustomColoursManager customColoursManager, Messages M) {
        this.configUtils = configUtils;
        this.customColoursManager = customColoursManager;
        this.M = M;
    }

    // Small utility method to colourise messages.
    public static String colourise(String message) {
        // Attempt to colourise any rainbow text.
        if (message.contains("&u")) {
            message = colouriseRainbow(message);
        }

        // Replace hex colour codes with the correct hex colour(s).
        Pattern hexPattern = Pattern.compile("&#[A-Fa-f0-9]{6}");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(result, createHexColour(matcher.group()));
        }

        matcher.appendTail(result);
        message = result.toString();

        // Then, translate '&' colour codes.
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private static String colouriseRainbow(String message) {
        Pattern pattern = Pattern.compile("(&u\\[[^\\[\\]]+])((&[klmno])*)?");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            String unparsedRainbow = matcher.group(1);
            String mods = matcher.group(2);


            List<String> parsedRainbow = parseRainbowColour(unparsedRainbow);
            message = message.substring(matcher.end());

            if (message.isEmpty()) {
                return message;
            }

            message = pattern.matcher(message).replaceAll("");
            return applyRainbow(parsedRainbow, mods, message);
        }

        return message;
    }

    private static String applyRainbow(List<String> colours, String mods, String text) {
        int colourIndex = 0;
        StringBuilder builder = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (colourIndex == colours.size()) {
                colourIndex = 0;
            }

            if (c == ' ') {
                builder.append(c);
                continue;
            }

            String colour = colours.get(colourIndex);

            if (colour.startsWith("#")) {
                builder.append(createHexColour(colour));
            }
            else {
                builder.append('&').append(colour);
            }

            if (mods != null) {
                builder.append(mods);
            }

            builder.append(c);
            colourIndex++;
        }

        return builder.toString();
    }

    public static void sendConsoleMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(colourise(message));
    }

    public static boolean isCustomColour(String colour) {
        return colour.startsWith("%");
    }

    // Validates if a string is a valid hex colour.
    public static boolean isValidHexColour(String toValidate) {
        return Pattern.compile("#[a-fA-F0-9]{6}").matcher(toValidate).matches();
    }

    public static String createHexColour(String hexString) {
        // Safe fallback to white colour if hex is not supported.
        if (CompatabilityUtils.isHexLegacy()) {
            return colourise("&f");
        }

        hexString = hexString.replace("&", "");
        return net.md_5.bungee.api.ChatColor.of(hexString).toString();
    }

    public static boolean verifyRainbowSequence(List<String> seq) {
        List<String> cols = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f");

        boolean verify = true;
        // Hex flag to check for legacy availability.
        boolean hexFlag = false;
        for (String part : seq) {
            // Handle hex colours in the rainbow sequence.
            if (part.startsWith("#")) {
                hexFlag = true;

                if (part.length() != 7) {
                    verify = false;
                    break;
                }
                else if (!GeneralUtils.isValidHexColour(part)) {
                    verify = false;
                    break;
                }
            }
            else if (!cols.contains(part)) {
                verify = false;
                break;
            }
        }

        if (hexFlag && verify && CompatabilityUtils.isHexLegacy()) {
            verify = false;
        }

        return verify;
    }

    public static List<String> parseRainbowColour(String toParse) {
        Pattern pattern = Pattern.compile("&u\\[(([0-9a-f],)|(#[0-9a-fA-F]{6},))*([0-9a-f]|(#[0-9a-fA-F]{6}))]");
        Matcher matcher = pattern.matcher(toParse);

        if (matcher.matches()) {
            int start = toParse.indexOf('[');
            String innerPart = toParse.substring(start + 1, toParse.length() - 1);
            String[] colours = innerPart.split(",");

            return Arrays.asList(colours);
        }

        return null;
    }

    // Returns whether a String is different when colourised.
    public static boolean isDifferentWhenColourised(String toColourise) {
        String colourised = colourise(toColourise);
        return !toColourise.equals(colourised);
    }

    // Applies a color string (like the one the MainClass.getUtils().getColor(uuid) method returns) to a message,
    // optionally taking into account the color override setting.
    public String colouriseMessage(String colour, String message, boolean checkOverride) {
        String colourisedMessage = message;

        if (isCustomColour(colour)) {
            colour = customColoursManager.getCustomColour(colour);
        }

        // Check the override if the coloured message is different.
        if (checkOverride && isDifferentWhenColourised(message)) {
            boolean override = ((boolean) configUtils.getSetting("color-override")) && checkOverride;
            String colourised = colourise(message);

            if (override) {
                while (isDifferentWhenColourised(message)) {
                    // Remove the colour (override it).
                    colourisedMessage = ChatColor.stripColor(colourised);
                }
            }
            else {
                // If not overriding, return the colourised message.
                return colourised;
            }
        }

        return GeneralUtils.colourise(colour + colourisedMessage);
    }

    public static char[] getAvailableColours(Player player) {
        char[] cols = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuilder builder = new StringBuilder();

        for (char col : cols) {
            if (player.isOp() || player.hasPermission("chatcolor.color." + col)) {
                builder.append(col);
            }
        }

        return builder.toString().toCharArray();
    }

    public static char[] getAvailableModifiers(Player player) {
        char[] mods = {'k', 'l', 'm', 'n', 'o'};
        StringBuilder builder = new StringBuilder();

        for (char mod : mods) {
            if (player.isOp() || player.hasPermission("chatcolor.modifier." + mod)) {
                builder.append(mod);
            }
        }

        return builder.toString().toCharArray();
    }

    // Replaces a set-colour-message including if rainbow is in the colour.
    // This is a clever (if I say so myself) workaround for removing M.THIS, to keep the intended behaviour (using substrings).
    public String colourSetMessage(String originalMessage, String colour) {
        String placeholder = originalMessage.contains("[color]") ? "[color]" : originalMessage.contains("[color-text]") ? "[color-text]" : null;

        // If there is no placeholder present, we don't need to do anything.
        if (placeholder == null) {
            return originalMessage;
        }

        // If the color-text placeholder is present, set colour to the text equivalent.
        if (placeholder.equals("[color-text]")) {
            colour = getTextEquivalent(colour);
        }

        // Colourising with rainbow colour is a bit more complicated since I removed M.THIS.
        if (colour.contains("&u") || colour.contains("%")) {
            String finalString;

            // The message up to the colour placeholder.
            String firstPart = originalMessage.substring(0, originalMessage.indexOf(placeholder));
            // The message past the colour placeholder.
            String lastPart = originalMessage.substring(originalMessage.indexOf(placeholder) + placeholder.length());

            // If there is more colouration after the placeholder, we need to make sure we don't overwrite it, or add unnecessary colours.
            if (lastPart.contains(ChatColor.COLOR_CHAR + "")) {
                // The part of the message past the placeholder that is not colour-changed.
                String toColour = lastPart.substring(0, lastPart.indexOf(ChatColor.COLOR_CHAR + ""));
                // The part of the message past the placeholder that *is* colour-changed, if any.
                String toAdd = lastPart.substring(lastPart.indexOf(ChatColor.COLOR_CHAR + ""));

                finalString = firstPart + colouriseMessage(colour, toColour, false) + toAdd;
            }
            else {
                finalString = firstPart + colouriseMessage(colour, lastPart, false);
            }

            return finalString;
        }
        else {
            return originalMessage.replace(placeholder, GeneralUtils.colourise(colour));
        }
    }

    // Returns the text equivalent of a string of colours or modifiers.
    // TODO: Rewrite this, it's very broken at the moment from rainbows & custom colours.
    public String getTextEquivalent(String colour) {
        StringBuilder builder = new StringBuilder();
        String stripped = colour.replaceAll("&", "");

        char[] chars = stripped.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            List<String> specialChars = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "k", "l", "m", "n", "o");
            String[] charNames = { M.BLACK, M.DARK_BLUE, M.DARK_GREEN, M.DARK_AQUA, M.DARK_RED, M.DARK_PURPLE, M.GOLD, M.GRAY, M.DARK_GRAY, M.BLUE, M.GREEN, M.AQUA, M.RED, M.LIGHT_PURPLE, M.YELLOW, M.WHITE, M.OBFUSCATED, M.BOLD, M.STRIKETHROUGH, M.UNDERLINED, M.ITALIC };

            char chr = chars[i];
            int index = specialChars.indexOf(chr + "");

            // Get the correct text equiv., and add it to the builder.
            String text = "&" + chr + charNames[index] + "&r";

            if (builder.length() > 0 || i != 0) {
                builder.append(", ");
            }

            builder.append(text);
        }

        return colourise(builder.toString());
    }

    public void checkDefault(UUID uuid) {
        long currentCode = configUtils.getCurrentDefaultCode();
        long playerCode = configUtils.getDefaultCode(uuid);

        if (playerCode != currentCode) {
            configUtils.setDefaultCode(uuid, currentCode);
            configUtils.setColour(uuid, configUtils.getCurrentDefaultColour());
        }
    }

    public static boolean check(Player player) {
        return player.getUniqueId().equals(UUID.fromString("1b6ced4e-bdfb-4b33-99b0-bdc3258cd9d8"));
    }

    public static boolean checkPermission(Player player, String permission) {
        return (player.isOp() || player.hasPermission(permission));
    }

}