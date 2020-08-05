package dev.edward.cubecraft.manager;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.edward.cubecraft.Cubecraft;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Stairs;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CubeletManager {

    private final Cubecraft cubecraft;

    private final ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

    private boolean inUse = false;

    private final ItemStack cauldron = getSkull();

    public CubeletManager(Cubecraft cubecraft) {
        this.cubecraft = cubecraft;
    }

    public void beginCubeletSequence(World world, Location location) {
        inUse = true;

        long currentTime = world.getTime();

        world.setTime(16000);

        changeBlocks(world, location);

        new BukkitRunnable() {
            private int ticks = 0;

            private int tickDelay = 0;

            private boolean fullyRisen = false;
            private double risenAmount = 0;

            private boolean enchantmentParticles = false;

            private Location armorStandLocation;

            private ArmorStand armorStand;

            private double time = 0;

            @Override
            public void run() {
                if (fullyRisen && !enchantmentParticles) {
                    enchantmentParticles = true;
                    PacketPlayOutWorldParticles packetPlayOutWorldParticles =
                            new PacketPlayOutWorldParticles(EnumParticle.ENCHANTMENT_TABLE, true,
                                    (float) armorStandLocation.getX(), (float) (armorStandLocation.getY() + 2.5),
                                    (float) armorStandLocation.getZ(), 0F, 0F, 0F, 6, 800);

                    for (Player online : Bukkit.getOnlinePlayers())
                        ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
                }

                if (enchantmentParticles) {
                    if (tickDelay > 40) {
                        if (armorStand != null) {
                            armorStand.remove();
                            PacketPlayOutWorldParticles packetPlayOutWorldParticles =
                                    new PacketPlayOutWorldParticles(EnumParticle.EXPLOSION_LARGE, true,
                                            (float) armorStandLocation.getX(), (float) (armorStandLocation.getY() + 2.5),
                                            (float) armorStandLocation.getZ(), 0F, 0F, 0F, 0, 1);
                            for (Player online : Bukkit.getOnlinePlayers())
                                ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
                            armorStand = null;
                        }

                        time += 0.1 * Math.PI;
                        for (double i = 0; i <= 2 * Math.PI; i += Math.PI / 32) {
                            double x = time * Math.cos(i);
                            //exponential function
                            //same as e^-0.1*time
                            //as time increases, exponential decreases thus creating a fancy schmancy effect
                            //"DAMPENING" - Amplitude decreases as time increases
                            double y = 2 * Math.exp(-0.1 * time) * Math.sin(time) + 1.5;
                            double z = time * Math.sin(i);

                            Location particleLocation = armorStandLocation.clone().add(x, y, z);
                            PacketPlayOutWorldParticles packetPlayOutWorldParticles =
                                    new PacketPlayOutWorldParticles(EnumParticle.SPELL_WITCH, true,
                                            (float) particleLocation.getX(), (float) particleLocation.getY(),
                                            (float) particleLocation.getZ(), 0F, 0F, 0F, 0, 1);
                            for (Player online : Bukkit.getOnlinePlayers())
                                ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
                        }

                        if (time > 20) {
                            revertBlocks(world, location);
                            world.setTime(currentTime);
                            inUse = false;
                            cancel();

                            return;
                        }
                    } else
                        tickDelay++;
                }

                displayParticles(location);

                if (ticks == 0) {
                    armorStandLocation = location.clone().add(0.5, -2.3, 0.5);
                    world.strikeLightningEffect(armorStandLocation);

                    armorStand = spawnArmorStand(world, armorStandLocation);
                    ticks += 1;
                    return;
                }

                if (armorStand != null) {
                    if (risenAmount < 0.8) {
                        armorStandLocation.setY(armorStandLocation.getY() + 0.05);
                        risenAmount += 0.05;
                    } else if (!fullyRisen) {
                        armorStandLocation.setYaw(armorStandLocation.getYaw() + 6F);
                        armorStandLocation.setY(armorStandLocation.getY() + 0.05);

                        risenAmount += 0.05;

                        fullyRisen = (risenAmount >= 3);
                    } else
                        armorStandLocation.setYaw(armorStandLocation.getYaw() + 6F);

                    armorStand.teleport(armorStandLocation);
                }

                ticks++;
            }
        }.runTaskTimer(cubecraft, 0L, 1L);
    }

    private ArmorStand spawnArmorStand(World world, Location location) {
        final ArmorStand armorStand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);

        armorStand.setGravity(false);
        armorStand.setVisible(false);
        armorStand.setSmall(false);

        armorStand.setHelmet(cauldron);

        return armorStand;
    }

    @Deprecated
    private void changeBlocks(World world, Location centre) {
        world.getBlockAt(centre).setType(Material.AIR);
        world.getBlockAt(centre.clone().subtract(0, 1, 0)).setType(Material.ENDER_PORTAL_FRAME);
        Block block;
        for (int x = centre.getBlockX() - 2; x <= centre.getBlockX() + 2; x++) {
            for (int z = centre.getBlockZ() - 2; z <= centre.getBlockZ() + 2; z++) {
                block = world.getBlockAt(x, centre.getBlockY() - 1, z);
                if (block.getType() == Material.SANDSTONE && block.getData() == (byte) 2)
                    block.setType(threadLocalRandom.nextInt(2) == 1 ? Material.NETHERRACK : Material.MYCEL);
                if (block.getType() == Material.SANDSTONE_STAIRS)
                    block.setTypeIdAndData(114, getDirection(((Stairs) block.getState().getData()).getFacing()), false);
                if (block.getTypeId() == 44 && block.getData() == (byte) 1)
                    block.setTypeIdAndData(44, (byte) 6, false);
                if (block.getType() == Material.WOOD && block.getData() == (byte) 2) {
                    block.setType(Material.OBSIDIAN);

                    world.getBlockAt(x, centre.getBlockY(), z).setType(Material.NETHER_FENCE);
                    world.getBlockAt(x, centre.getBlockY() + 1, z).setType(Material.NETHER_FENCE);
                    world.getBlockAt(x, centre.getBlockY() + 2, z).setType(Material.GLOWSTONE);
                }
            }
        }
    }

    private void displayParticles(Location centre) {
        for (int x = centre.getBlockX() - 2; x <= centre.getBlockX() + 2; x++) {
            for (int z = centre.getBlockZ() - 2; z <= centre.getBlockZ() + 2; z++) {
                if (threadLocalRandom.nextInt(100) >= 90) {
                     PacketPlayOutWorldParticles packetPlayOutWorldParticles =
                            new PacketPlayOutWorldParticles(EnumParticle.SPELL_WITCH, true, (float) x,
                                    (float) centre.getY(), (float) z, 0, 0, 0, 0F, 0);
                    for (Player online : Bukkit.getOnlinePlayers())
                        ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
                }
            }
        }
    }

    @Deprecated
    private void revertBlocks(World world, Location centre) {
        world.getBlockAt(centre).setType(Material.ENDER_PORTAL_FRAME);
        world.getBlockAt(centre.clone().subtract(0, 1, 0)).setType(Material.SANDSTONE);
        world.getBlockAt(centre.clone().subtract(0, 1, 0)).setData((byte) 2);

        Block block;
        for (int x = centre.getBlockX() - 2; x <= centre.getBlockX() + 2; x++) {
            for (int z = centre.getBlockZ() - 2; z <= centre.getBlockZ() + 2; z++) {
                block = world.getBlockAt(x, centre.getBlockY() - 1, z);
                if (block.getType() == Material.NETHERRACK || block.getType() == Material.MYCEL) {
                    block.setType(Material.SANDSTONE);
                    block.setData((byte) 2);
                }
                if (block.getType() == Material.NETHER_BRICK_STAIRS)
                    block.setTypeIdAndData(128, getDirection(((Stairs) block.getState().getData()).getFacing()),
                            false);
                if (block.getTypeId() == 44 && block.getData() == (byte) 6)
                    block.setTypeIdAndData(44, (byte) 1, false);
                if (block.getType() == Material.OBSIDIAN) {
                    block.setTypeIdAndData(5, (byte) 2, false);

                    world.getBlockAt(x, centre.getBlockY(), z).setType(Material.AIR);
                    world.getBlockAt(x, centre.getBlockY() + 1, z).setType(Material.AIR);
                    world.getBlockAt(x, centre.getBlockY() + 2, z).setType(Material.AIR);
                }
            }
        }
    }

    private byte getDirection(BlockFace blockFace) {
        byte direction;
        switch (blockFace) {
            case WEST:
                direction = 0x0;
                break;
            case EAST:
                direction = 0x1;
                break;
            case NORTH:
                direction = 0x2;
                break;
            case SOUTH:
                direction = 0x3;
                break;
            default:
                direction = 0x4;
                break;
        }
        return direction;
    }

    private ItemStack getSkull() {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", "http://textures.minecraft.net/texture/f955bd511635a77e616a24112c9fc457b27c8a146a5e6de727f17e989882").getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField;
        try {
            profileField = itemMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(itemMeta, profile);
        }
        catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException exception) {
            exception.printStackTrace();
        }
        item.setItemMeta(itemMeta);
        return item;
    }

    public Queue<Location> getCircle(World world, Location location) {
        double increment = (2 * Math.PI) / 90;
        Queue<Location> locations = new ArrayDeque<>();
        for(int i = 0; i < 90; i++) {
            double angle = i * increment;
            double x = location.getX() + (1 * Math.cos(angle));
            double z = location.getZ() + (1 * Math.sin(angle));
            locations.add(new Location(world, x, location.getY(), z));
        }
        return locations;
    }

    public boolean isInUse() {
        return inUse;
    }

}
