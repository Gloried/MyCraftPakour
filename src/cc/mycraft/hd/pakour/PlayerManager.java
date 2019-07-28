package cc.mycraft.hd.pakour;

import java.io.File;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class PlayerManager {	
	
	
	public static File PlayerDataFile;
	public static FileConfiguration Data;
	
	private static int PlayerNum;
	private static Location SaveLoc;
	private final Player player;
	
	
	public static void run() {
		PlayerDataFile =  new File(load.getInstance().getDataFolder()+"/Data.yml");
		Data = YamlConfiguration.loadConfiguration(PlayerDataFile);
	}
	
	PlayerManager(Player p){
		this.player = p;
	}
	public void setFinishedMap() {

		String SectionString = "PlayerData."+this.player.getName()+".Normal.Finished.Main";
		int ThisTimeLevel = MapManager.PlayerLevel.get(this.player);
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
		int ThisTimeLevel = MapManager.PlayerLevel.get(this.player);
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
		//Bukkit.getLogger().info(Level+1 + "|" +Data.getInt(SectionString));
		return Data.getInt(SectionString)+1>=Level;
	}
	
	public String getMapName() {
		String Section = "PlayerData."+this.player.getName()+".Normal.SaveLoc.";
		if(this.player.getMaxHealth()==2d) {
			Section = "PlayerData."+this.player.getName()+".Hard.SaveLoc.";
		}
		return Data.getString(Section+"map");
	}
	
	public void clearSaveLoc() {
		String SectionString = "PlayerData."+this.player.getName()+".Normal.SaveLoc";
		if(this.player.getMaxHealth()==2d) {
			SectionString = "PlayerData."+this.player.getName()+".Hard.SaveLoc";
		}
		Data.set(SectionString, null);
	}
	
	public boolean hasSaveLoc() {
		String SectionString = "PlayerData."+this.player.getName()+".Normal.SaveLoc.";
		if(this.player.getMaxHealth()==2d) {
			SectionString = "PlayerData."+this.player.getName()+".Hard.SaveLoc.";
		}
		return Data.contains(SectionString);
	}
	
	public void setSaveLoc() {
		String SectionString = "PlayerData."+this.player.getName()+".Normal.SaveLoc.";
		if(this.player.getMaxHealth()==2d) {
			SectionString = "PlayerData."+this.player.getName()+".Hard.SaveLoc.";
		}
		Data.set(SectionString+"map", MapManager.PlayerPlaying.get(this.player));
		Data.set(SectionString+"level", MapManager.PlayerLevel.get(this.player));
		Data.set(SectionString+"world", this.player.getLocation().getWorld().getName());
		Data.set(SectionString+"x", this.player.getLocation().getX());
		Data.set(SectionString+"y", this.player.getLocation().getY());
		Data.set(SectionString+"z", this.player.getLocation().getZ());
		save();
		this.player.sendMessage("已设置存档点！");
	}
	public static void setSaveLoc(Player p) {
		String SectionString = "PlayerData."+p.getName()+".Normal.SaveLoc.";
		if(p.getMaxHealth()==2d) {
			SectionString = "PlayerData."+p.getName()+".Hard.SaveLoc.";
		}
		Data.set(SectionString+"map", MapManager.PlayerPlaying.get(p));
		Data.set(SectionString+"level", MapManager.PlayerLevel.get(p));
		Data.set(SectionString+"world", p.getLocation().getWorld().getName());
		Data.set(SectionString+"x", p.getLocation().getX());
		Data.set(SectionString+"y", p.getLocation().getY());
		Data.set(SectionString+"z", p.getLocation().getZ());
		save();
		p.sendMessage("已设置存档点！");
	}
	public Location getSaveLoc() {
		String SectionString = "PlayerData."+this.player.getName()+".Normal.SaveLoc.";
		if(this.player.getMaxHealth()==2d) {
			SectionString = "PlayerData."+this.player.getName()+".Hard.SaveLoc.";
		}
		return new Location(Bukkit.getWorld(Data.getString(SectionString+"world")), 
							Data.getDouble(SectionString+"x"), 
							Data.getDouble(SectionString+"y"), 
							Data.getDouble(SectionString+"z"));
	}
	public int getSaveLevel() {
		String SectionString = "PlayerData."+this.player.getName()+".Normal.SaveLoc.level";
		if(this.player.getMaxHealth()==2d) {
			SectionString = "PlayerData."+this.player.getName()+".Hard.SaveLoc.level";
		}
		return Data.getInt(SectionString);
	}
	
	
	
	public static void save() {
		try {
			Data.save(PlayerDataFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	

}	
