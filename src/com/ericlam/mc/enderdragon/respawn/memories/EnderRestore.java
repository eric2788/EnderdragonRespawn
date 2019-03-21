package com.ericlam.mc.enderdragon.respawn.memories;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class EnderRestore {
    private List<Location> obdsidian;
    private List<Location> enderCrystal;
    private Location enderDragon;
    private World world;
    private List<Location> ironBars;

    public EnderRestore(List<Location> obdsidian, List<Location> enderCrystal, List<Location> ironBars, Location enderDragon, World world) {
        this.obdsidian = obdsidian;
        this.enderCrystal = enderCrystal;
        this.ironBars = ironBars;
        this.enderDragon = enderDragon;
        this.world = world;
    }

    public EnderRestore(World world){
        this.obdsidian = new ArrayList<>();
        this.enderCrystal = new ArrayList<>();
        this.ironBars = new ArrayList<>();
        this.world = world;
    }

    public List<Location> getIronBars() {
        return ironBars;
    }

    public void addIronBars(Location loc){
        if (!this.ironBars.contains(loc)) this.ironBars.add(loc);
    }

    public World getWorld() {
        return world;
    }

    public List<Location> getObdsidian() {
        return obdsidian;
    }

    public void addObsidian(Location loc){
        if (!this.obdsidian.contains(loc)) this.obdsidian.add(loc);
    }

    public List<Location> getEnderCrystal() {
        return enderCrystal;
    }

    public void addEnderCrystal(Location loc){
        if (!this.enderCrystal.contains(loc)) this.enderCrystal.add(loc);
    }

    public Location getEnderDragon() {
        return enderDragon;
    }

    public void setEnderDragon(Location enderDragon) {
        this.enderDragon = enderDragon;
    }
}
