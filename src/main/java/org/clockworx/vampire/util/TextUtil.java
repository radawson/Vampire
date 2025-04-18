package org.clockworx.vampire.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for text formatting and parsing.
 */
public class TextUtil {
    
    /**
     * The pattern for color codes.
     */
    private static final Pattern COLOR_PATTERN = Pattern.compile("&([0-9a-fk-or])");
    
    /**
     * Parses a string with color codes.
     * 
     * @param text The text to parse
     * @return The parsed text
     */
    public static String parse(String text) {
        if (text == null) return "";
        
        Matcher matcher = COLOR_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + matcher.group(1));
        }
        
        matcher.appendTail(buffer);
        return buffer.toString();
    }
    
    /**
     * Parses a string with color codes and format specifiers.
     * 
     * @param format The format string
     * @param args The arguments to format
     * @return The parsed text
     */
    public static String parse(String format, Object... args) {
        return parse(String.format(format, args));
    }
    
    /**
     * Gets the name of a material.
     * 
     * @param material The material
     * @return The name of the material
     */
    public static String getMaterialName(Material material) {
        if (material == null) return "Unknown";
        
        String name = material.name();
        name = name.toLowerCase();
        name = name.replace('_', ' ');
        
        StringBuilder result = new StringBuilder();
        boolean capitalize = true;
        
        for (char c : name.toCharArray()) {
            if (c == ' ') {
                result.append(c);
                capitalize = true;
            } else {
                result.append(capitalize ? Character.toUpperCase(c) : c);
                capitalize = false;
            }
        }
        
        return result.toString();
    }
} 