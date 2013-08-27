/*
 * Copyright (c) 2013, LankyLord
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.fuzzyblocks.easyworldguard.managers;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.fuzzyblocks.easyworldguard.EasyWorldguard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WandManager {

    private EasyWorldguard plugin;
    private WorldGuardPlugin worldGuard;
    private WorldEditPlugin worldEdit;

    public WandManager(EasyWorldguard plugin) {
        this.plugin = plugin;
        this.worldGuard = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        try {
            this.worldEdit = worldGuard.getWorldEdit();
        } catch (CommandException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show region data to a player for the block given
     *
     * @param playerName The player to receive the data
     * @param block      Block at which point region data is gathered
     */
    public void showRegionData(String playerName, Block block) {
        Player player = Bukkit.getPlayer(playerName);
        Vector vector = BukkitUtil.toVector(block);
        com.sk89q.worldguard.protection.managers.RegionManager wgRegionManager = worldGuard.getRegionManager(player.getWorld());
        ApplicableRegionSet regionSet = wgRegionManager.getApplicableRegions(vector);
        com.sk89q.worldguard.LocalPlayer localPlayer = worldGuard.wrapPlayer(player);

        if (regionSet.size() > 0) {
            player.sendMessage(ChatColor.YELLOW + "Can you build? " + (regionSet.canBuild(localPlayer) ? "Yes" : "No"));

            StringBuilder builder = new StringBuilder();
            for (Iterator<ProtectedRegion> iterator = regionSet.iterator(); iterator.hasNext(); ) {
                builder.append(iterator.next().getId());
                if (iterator.hasNext())
                    builder.append(", ");
            }

            player.sendMessage(ChatColor.YELLOW + "Regions at this block: " + builder.toString());
        } else
            player.sendMessage(ChatColor.YELLOW + "There are no regions here, if it's your build, why not make one?");
    }

    /**
     * Give player a wand for use with checking region info
     *
     * @param playerName The player to receive the wand
     */
    public void givePlayerWand(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        Material wand = Material.getMaterial(worldGuard.getGlobalStateManager().get(player.getWorld()).regionWand);
        ItemStack itemStack = new ItemStack(wand);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName("Region Check");
        List<String> itemLore = new ArrayList<>();
        itemLore.add("Rightclick to check for");
        itemLore.add("information on regions");
        itemMeta.setLore(itemLore);
        itemStack.setItemMeta(itemMeta);

        player.getInventory().addItem(itemStack);
    }
}
