package it.cratePlugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;

public class Utils {

    private static final LegacyComponentSerializer SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    public static Component color(String text) {
        return SERIALIZER.deserialize(text);
    }

    public static String colorStr(String text) {
        return text.replace("&", "§");
    }

    public static void spawnWinFirework(Player player) {
        Location loc = player.getLocation();
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.setPower(1);
        meta.addEffect(FireworkEffect.builder()
                .withColor(Color.ORANGE, Color.YELLOW, Color.RED)
                .withFade(Color.WHITE)
                .with(FireworkEffect.Type.BURST)
                .trail(true)
                .flicker(true)
                .build());
        fw.setFireworkMeta(meta);
        // Detonate immediately after 1 tick
        fw.getScheduler().runDelayed(it.cratePlugin.CratePlugin.getInstance(),
                task -> fw.detonate(), null, 1L);
    }

    private static final Random RANDOM = new Random();

    public static int randomInt(int bound) {
        return RANDOM.nextInt(bound);
    }
}
