package cc.mycraft.hd.pakour;

import java.io.File;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class PlayerManager {	
	
	
	public static File PlayerDataFile;
	public static FileConfiguration Data;
	
	private final Player player;
	
	public static HashMap<String, Location> NormalSaveLoc;
	public static HashMap<String, Location> HardSaveLoc;
	public static HashMap<String, String> LastPlayRoom;
	public static HashMap<String, Integer> PlayerSaveLevel;
	
	
	public static void run() {
		PlayerDataFile =  new File(load.getInstance().getDataFolder()+"/Data.yml");
		Data = YamlConfiguration.loadConfiguration(PlayerDataFile);
		NormalSaveLoc = new HashMap<String,Location>();
		HardSaveLoc = new HashMap<String,Location>();
		PlayerSaveLevel = new HashMap<String,Integer>();
		LastPlayRoom = new HashMap<String,String>();
	}
	
	PlayerManager(Player p){
		this.player = p;
	}
	public void setFinishedMap() {
		String SectionString = "PlayerData."+this.player.getName()+".Normal.Finished.Main";
		int ThisTimeLevel = MapManager.PlayerLevel.get(this.player.getName());
		if(Data.getInt(SectionString)<ThisTimeLevel) {
			Data.set(SectionString, ThisTimeLevel);
		}
		SectionString = "PlayerData."+this.player.getName()+".Hard.Finished.Main";
		if(this.player.getMaxHealth()==2d && Data.getInt(SectionString)<ThisTimeLevel) {
			Data.set(SectionString, ThisTimeLevel);
		}
		this.clearSaveLoc();
		save();
	}
	public void setFinishedEXMap() {
		int ThisTimeLevel = MapManager.PlayerLevel.get(this.player.getName());
		String SectionString = "Fihished.Ex"+ThisTimeLevel+".Normal."+this.player.getName();
		Data.set(SectionString, true);
		if(this.player.getMaxHealth()==2d) {
			SectionString = "Fihished.Ex"+ThisTimeLevel+".Hard."+this.player.getName();
			Data.set(SectionString, true);
		}
		this.clearSaveLoc();
		save();
	}
	public static boolean canEntre(Player p,int Level) {
		String SectionString = "PlayerData."+p.getName()+".Normal.Finished.Main";
		if(p.getMaxHealth()==2d) {
			SectionString = "PlayerData."+p.getName()+".Hard.Finished.Main";
		}
		return Data.getInt(SectionString)+1>=Level;
	}
	
	public String getMapName() {
		return LastPlayRoom.get(this.player.getName());
	}
	
	public void clearSaveLoc() {
		if(this.player.getMaxHealth()==2d) {
			HardSaveLoc.remove(this.player.getName());
		} else {
			NormalSaveLoc.remove(this.player.getName());
		}
	}
	
	public boolean hasSaveLoc() {
		if(this.player.getMaxHealth()==2d) {
			return HardSaveLoc.containsKey(this.player.getName());
		}
		return NormalSaveLoc.containsKey(this.player.getName());
	}
	
	public static void setSaveLoc(Player p) {
		if(p.getMaxHealth()==2d) {
			HardSaveLoc.put(p.getName(), p.getLocation());
		} else {
			NormalSaveLoc.put(p.getName(), p.getLocation());
		}
		LastPlayRoom.put(p.getName(), MapManager.PlayerPlaying.get(p.getName()));
		PlayerSaveLevel.put(p.getName(), MapManager.PlayerLevel.get(p.getName()));
		p.sendMessage("已设置存档点！");
	}
	public Location getSaveLoc() {
		if(this.player.getMaxHealth()==2d) {
			return HardSaveLoc.get(this.player.getName());
		}
		return NormalSaveLoc.get(this.player.getName());
	}
	public static void save() {
		try {
			Data.save(PlayerDataFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public int getSaveLevel() {
		return PlayerSaveLevel.get(this.player.getName());
	}
	
	/*
	public static void setSaveLoc(Player p,Block b) {
		String SectionString = "PlayerData."+p.getName()+".Normal.SaveLoc.";
		if(p.getMaxHealth()==2d) {
			SectionString = "PlayerData."+p.getName()+".Hard.SaveLoc.";
		}
		Data.set(SectionString+"map", MapManager.PlayerPlaying.get(p));
		Data.set(SectionString+"level", MapManager.PlayerLevel.get(p));
		Data.set(SectionString+"world", b.getWorld().getName());
		Data.set(SectionString+"x", b.getLocation().getX());
		Data.set(SectionString+"y", b.getLocation().getY());
		Data.set(SectionString+"z", b.getLocation().getZ());
		save();
		p.sendMessage("已设置存档点！");
	}*/
	
	
	
	
	
	

}	
