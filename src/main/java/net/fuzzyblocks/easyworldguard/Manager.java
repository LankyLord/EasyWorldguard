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
package net.fuzzyblocks.easyworldguard;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class Manager {

    private EasyWorldguard plugin;
    private WorldGuardPlugin worldGuard;
    private WorldEditPlugin worldEdit;

    public Manager(EasyWorldguard plugin) {
        this.plugin = plugin;
        this.worldGuard = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        try {
            this.worldEdit = worldGuard.getWorldEdit();
        } catch (CommandException e) {
            e.printStackTrace();
        }
    }

    private Selection getSelection(String playerName) {
        return worldEdit.getSelection(Bukkit.getPlayer(playerName));
    }

    private RegionManager getRegionManager(World world) {
        return worldGuard.getRegionManager(world);
    }

    public boolean claimRegion(String playerName, String regionName) {
        Selection selection = this.getSelection(playerName);
        World world = selection.getWorld();
        RegionManager regionManager = getRegionManager(world);
        if (regionManager.getRegion(regionName) == null) {
            DefaultDomain owners = new DefaultDomain();

            owners.addPlayer(playerName);

            BlockVector maximumPoint = selection.getNativeMaximumPoint().toBlockVector();
            BlockVector minimumPoint = selection.getNativeMinimumPoint().toBlockVector();

            maximumPoint.setY(world.getMaxHeight());
            minimumPoint.setY(0);

            //TODO: Add check for area
            ProtectedCuboidRegion protectedRegion = new ProtectedCuboidRegion(regionName, maximumPoint, minimumPoint);
            regionManager.addRegion(protectedRegion);
            regionManager.getRegion(regionName).setOwners(owners);
            return true;
        }
        return false;
    }

    public boolean deleteRegion(String playerName, String regionName) {
        Player player = Bukkit.getPlayer(playerName);
        RegionManager regionManager = worldGuard.getRegionManager(player.getWorld());
        ProtectedRegion region = regionManager.getRegion(regionName);

        if (region != null && region.getOwners().contains(playerName)) {
            regionManager.removeRegion(regionName);
            return true;
        }
        return false;
    }

    public boolean addMember(String ownerName, String memberName, String regionName) {
        Player owner = Bukkit.getPlayer(ownerName);
        RegionManager regionManager = worldGuard.getRegionManager(owner.getWorld());
        ProtectedRegion region = regionManager.getRegion(regionName);

        if (region != null && region.getOwners().contains(ownerName)) {
            region.getMembers().addPlayer(memberName);
            return true;
        }
        return false;
    }

    public boolean giveRegion(String ownerName, String newOwner, String regionName) {
        Player owner = Bukkit.getPlayer(ownerName);
        RegionManager regionManager = worldGuard.getRegionManager(owner.getWorld());
        ProtectedRegion region = regionManager.getRegion(regionName);

        if (region != null && region.getOwners().contains(ownerName)) {
            region.getOwners().removaAll();
            region.getOwners().addPlayer(newOwner);
            region.getMembers().addPlayer(ownerName);
            return true;
        }
        return false;
    }

    public HashSet<String> getOwnedRegions(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        RegionManager regionManager = worldGuard.getRegionManager(player.getWorld());
        HashSet<String> ownedRegions = new HashSet<>();
        for (ProtectedRegion region : regionManager.getRegions().values()) {
            if (region.getOwners().contains(playerName)) {
                ownedRegions.add(region.getId());
            }
        }
        return ownedRegions;
    }
}
//TODO: Add way of resizing region
//TODO: Override worldguard wand with own equivalent
//TODO: Possibly add automated region naming
//TODO: Add check for maximum region count