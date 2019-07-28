package cc.mycraft.hd.pakour;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MapManager {
	
	/*
	 * config设置格式
	 * 
	 * EnterGroup:
	 *   '1':
	 *     - 'xxx1'
	 *     - 'xxx2'
	 *     - 'xxx3'
	 *   '2':
	 *     - 'xxx4'
	 *     - 'xxx5'
	 * 
	 * MapList:
	 *   xxx1:
	 *     World: xxx
	 *     Spawn:
	 *       x: xx
	 *       y: xx
	 *       z: xx
	 *     End:
	 *       x: xx
	 *       y: xx
	 *       z: xx
	 *     Ex:
	 *       x: xx
	 *       y: xx
	 *       z: xx
	 *       
	 *   xxx2:
	 *     World: xxx
	 *     Spawn:
	 *       x: xx
	 *       y: xx
	 *       z: xx
	 *     End:
	 *       x: xx
	 *       y: xx
	 *       z: xx
	 *     Ex:
	 *       x: xx
	 *       y: xx
	 *       z: xx
	 * 
	 * */
	
	public static HashMap<String,Boolean> PlayingMap = new HashMap<String,Boolean>();
	public static HashMap<Player,String> PlayerPlaying = new HashMap<Player,String>();
	public static HashMap<Player,Integer> PlayerLevel = new HashMap<Player,Integer>();
	public static FileConfiguration config;
	
	public static void run() {
		config = load.getInstance().getConfig();
		for(String mapName : load.RoomList) {
			PlayingMap.put(mapName,false);
		}
		Bukkit.getLogger().info("已加载房间："+load.RoomList.toString());
	}
	
	public static boolean isMapPlaying(String MapName) {
		return PlayingMap.get(MapName);
	}
	
	public static void setPlayingState(String MapName,boolean b) {
		PlayingMap.replace(MapName,b);
	}
	
	public static boolean isPlaying(Player p) {
		return PlayerPlaying.containsKey(p);
	}
	
	public static void showEnterGui(Player p,String PlayerEnter) {
		int Level = 1;
		try {
			Level = Integer.parseInt(PlayerEnter);
		} catch (Exception e) {
			p.sendMessage("§c关卡必须是数字。");
			return;
		}
		if(!config.contains("EnterGroup."+PlayerEnter)) {
			p.sendMessage("§c不存在的关卡。");
			return;
		}
		if(!PlayerManager.canEntre(p, Level)) {
			p.sendMessage("§c你需要通关前一关才能进行这一关挑战。");
			return;
		}
		List<String> GroupList = (List<String>)config.getList("EnterGroup."+PlayerEnter);
		String UIName = "§0进入关卡："+PlayerEnter;
		Inventory inv = Bukkit.getServer().createInventory(p, 9, UIName);
		int i = 0;
		for(String s : GroupList) {
			inv.setItem(i, canEnterIcon(s));
			i++;
			if(i==9) {
				break;
			}
		}
		p.openInventory(inv);
	}
	public static void setCheckPoint(Player p,String MapName,String CheckPointType) {
		switch (CheckPointType.toLowerCase()) {
		case "spawn": CheckPointType = "Spawn";break;
		case "end": CheckPointType = "End";break;
		case "ex": CheckPointType = "Ex";break;
		default:
			p.sendMessage("检查点只有spawn、end、ex三种类型。");
			return;
		}
		if(!config.contains("MapList."+MapName)) {
			p.sendMessage("不存在的房间。");
			return;
		}
		config.set("MapList."+MapName+"."+CheckPointType+".x", p.getLocation().getBlockX());
		config.set("MapList."+MapName+"."+CheckPointType+".y", p.getLocation().getBlockY());
		config.set("MapList."+MapName+"."+CheckPointType+".z", p.getLocation().getBlockZ());
		p.sendMessage("已设置"+MapName+"的"+CheckPointType+"点坐标。");
		load.getInstance().saveConfig();
	}
	public static void setParent(Player p,String MapName,String parent) {
		if(!config.contains("MapList."+MapName)) {
			p.sendMessage("不存在的房间。");
			return;
		}
		config.set("MapList."+MapName+".Parent", parent);
		p.sendMessage("已设置"+MapName+"的父房间为"+parent);
		load.getInstance().saveConfig();
	}
	public static void createRoom(Player p,String MapName) {
		if(config.contains("MapList."+MapName)) {
			p.sendMessage("已存在的房间。");
			return;
		}
		config.set("MapList."+MapName+".World", p.getWorld().getName());
		config.set("MapList."+MapName+".Spawn.x", p.getLocation().getBlockX());
		config.set("MapList."+MapName+".Spawn.y", p.getLocation().getBlockY());
		config.set("MapList."+MapName+".Spawn.z", p.getLocation().getBlockZ());
		p.sendMessage(MapName+"创建完成，房间所在世界和Spawn点已设置。");
		load.getInstance().saveConfig();
	}
	
	public static void addRoomItem(Player p,String MapName) {
		ItemStack item = p.getItemInHand();
		Bukkit.getLogger().info(MapName);
		if(!config.contains("MapList."+MapName)) {
			p.sendMessage("不存在的房间。");
			return;
		}
		for(int i=1;i<9;i++) {
			if(config.contains("MapList."+MapName+".Items."+i)) {
				continue;
			}
			config.set("MapList."+MapName+".Items."+i, item);
			p.sendMessage("道具已添加到"+MapName+"的第"+i+"个道具。");
			break;
		}
		load.getInstance().saveConfig();
	}
	
	public static int giveReward(Player p,double multiply) {
		String MapName = PlayerPlaying.get(p);
		if(!load.EcoLoad) {
			p.sendMessage("经济系统未加载，无法获得奖励。");
			return 0;
		}
		if(!config.contains("MapList."+MapName)) {
			p.sendMessage("不存在的房间。");
			return 0;
		}
		String MapSection = "MapList."+MapName+".Reward";
		if(!config.contains(MapSection)) {
			if(!config.contains("MapList."+MapName+".Parent")) {
				p.sendMessage("再接再厉！");
				return 0;
			}
			MapSection = "MapList."+config.getString("MapList."+MapName+".Parent")+".Reward";
		}
		load.econ.depositPlayer(p, config.getInt(MapSection)*multiply);
		return (int) (config.getInt(MapSection)*multiply);
	}
	
	public static void removeItem(Player p,String MapName) {
		if(!config.contains("MapList."+MapName)) {
			p.sendMessage("不存在的房间。");
			return;
		}
		config.set("MapList."+MapName+".Items", null);
		load.getInstance().saveConfig();
	}
	public static void setReward(Player p,String MapName,String MoneyStr) {
		int Money = 0;
		try {
			Money = Integer.parseInt(MoneyStr);
		} catch (Exception e) {
			p.sendMessage("Money只能是数字。");
			return;
		}
		if(!config.contains("MapList."+MapName)) {
			p.sendMessage("不存在的房间。");
			return;
		}
		config.set("MapList."+MapName+".Reward", Money);
		p.sendMessage("已将"+MapName+"的通关奖励设置为"+MoneyStr+",Ex通关的奖励为2倍主线通关奖励。");
		load.getInstance().saveConfig();
	}
	
	public static void givePlayerItems(Player p,String MapName) {
		if(!config.contains("MapList."+MapName)) {
			p.sendMessage("不存在的房间。");
			return;
		}
		//对应这个房间没有道具
		if(!config.contains("MapList."+MapName+".Items")) {
			if(!config.contains("MapList."+MapName+".Parent")) {
				return;
			}
			MapName = config.getString("MapList."+MapName+".Parent");
		}
		p.getInventory().clear();
		for(int i=1;i<9;i++) {
			if(!config.contains("MapList."+MapName+".Items."+i)) {
				break;
			}
			p.getInventory().addItem(config.getItemStack("MapList."+MapName+".Items."+i));
		}
	}
	
	public static void onPlayerClickIcon(InventoryClickEvent e) {
		if(!e.getInventory().getTitle().startsWith("§0进入关卡")) {
			return;
		}
		e.setCancelled(true);
		final Player p = (Player) e.getWhoClicked();
		ItemStack i = e.getClickedInventory().getItem(e.getRawSlot());
		if(i==null) {
			return;
		}
		if(i.getType().equals(Material.BARRIER)) {
			return;
		}
		String MapName = i.getItemMeta().getDisplayName().substring(8);
		if(PlayingMap.get(MapName)) {
			p.sendMessage("§e哦吼，有人抢在你之前进入了这个房间，重新打开这个界面再看看吧。");
			return;
		}
		int Level = Integer.parseInt(e.getInventory().getName().substring(7));
		p.teleport(getMapSpawnLocation(MapName));
		PlayingMap.replace(MapName, true);
		PlayerPlaying.put(p, MapName);
		PlayerLevel.put(p, Level);
		load.FinishedEXPlayers.remove(p.getName());
		load.FinishedPlayers.remove(p.getName());
		givePlayerItems(p, MapName);
		load.healPlayer(p);
		if(!p.getGameMode().equals(GameMode.ADVENTURE)&&!p.hasPermission("mycraft.admin")) {
			p.setGameMode(GameMode.ADVENTURE);
		}
	}
	
	
	public static boolean isCorrectBlock(Player p,String Where,Block b) {
		String MapName = PlayerPlaying.get(p);
		if(config.contains("MapList."+MapName+".Parent")) {
			String parent = "MapList."+config.getString("MapList."+MapName+".Parent");
			int xOffset = config.getInt(parent+"."+Where+".x") - config.getInt(parent+".Spawn.x");
			int yOffset = config.getInt(parent+"."+Where+".y") - config.getInt(parent+".Spawn.y");
			int zOffset = config.getInt(parent+"."+Where+".z") - config.getInt(parent+".Spawn.z");
			return (b.getLocation().getBlockX() == config.getInt("MapList."+MapName+".Spawn.x") + xOffset)
					&&(b.getLocation().getBlockY() == config.getInt("MapList."+MapName+".Spawn.y") + yOffset)
					&&(b.getLocation().getBlockZ() == config.getInt("MapList."+MapName+".Spawn.z") + zOffset);
		}
		return (b.getLocation().getBlockX() == config.getInt("MapList."+PlayerPlaying.get(p)+"."+Where+".x"))
				&&(b.getLocation().getBlockY() == config.getInt("MapList."+PlayerPlaying.get(p)+"."+Where+".y"))
				&&(b.getLocation().getBlockZ() == config.getInt("MapList."+PlayerPlaying.get(p)+"."+Where+".z"));
	}
	
	
	
	public static void backToSavePoint(Player p) {
		PlayerManager PM = new PlayerManager(p);
		String MapName = PM.getMapName();
		if(!PM.hasSaveLoc()) {
			p.sendMessage("§e你还没有这个难度下的存档点，无法返回。");
			return;
		}
		//房间里没人且自己不在别的房间玩
		if(!PlayingMap.get(MapName)&&!PlayerPlaying.containsKey(p)) {
			p.teleport(PM.getSaveLoc());
			PlayingMap.replace(MapName, true);
			PlayerPlaying.put(p, MapName);
			PlayerLevel.put(p, PM.getSaveLevel());
			givePlayerItems(p, MapName);
			return;
		}
		//自己还在房间里玩
		if(PlayingMap.get(MapName)&&PlayerPlaying.get(p).equals(MapName)) {
			p.teleport(PM.getSaveLoc());
		}
	}
	
	
	public static void PlayerExit(Player p) {
		if(PlayerPlaying.containsKey(p)) {
			PlayingMap.replace(PlayerPlaying.get(p), false);
			PlayerPlaying.remove(p);
		}
		p.getInventory().clear();
		load.FinishedEXPlayers.remove(p.getName());
		load.FinishedPlayers.remove(p.getName());
	}
	
	
/*
  		if(config.contains("MapList."+MapName+".Parent")) {
			String parent = "MapList."+config.getString("MapList."+MapName+".Parent");
			double xOffset = config.getDouble(parent+".Spawn.x")-config.getDouble("MapList."+MapName+".Spawn.x");
			double yOffset = config.getDouble(parent+".Spawn.y")-config.getDouble("MapList."+MapName+".Spawn.y");
			double zOffset = config.getDouble(parent+".Spawn.y")-config.getDouble("MapList."+MapName+".Spawn.z");
		}
  
 * */
	private static Location getMapSpawnLocation(String MapName) {
		return new Location(Bukkit.getWorld(config.getString("MapList."+MapName+".World")), 
							config.getDouble("MapList."+MapName+".Spawn.x"), 
							config.getDouble("MapList."+MapName+".Spawn.y"), 
							config.getDouble("MapList."+MapName+".Spawn.z"));
	}
	
	
	
	private static ItemStack canEnterIcon(String MapName) {
		//如果地图有人玩
		if(PlayingMap.get(MapName)) {
			return CantEnter(MapName);
		}
		return CanEnter(MapName);
		
	}
	private static ItemStack CanEnter(String MapName){
		ItemStack i = new ItemStack(Material.STRUCTURE_VOID);
		ItemMeta im = i.getItemMeta();
		String ItemName = "§b点击进入§0"+MapName;
		String[] ItemLore = {"§7这个房间可供游玩",};
		i.setAmount(1);
		im.setDisplayName(ItemName);
		im.setLore(Arrays.asList(ItemLore));
		i.setItemMeta(im);
		return i;
	}
	private static ItemStack CantEnter(String MapName){
		ItemStack i = new ItemStack(Material.BARRIER);
		ItemMeta im = i.getItemMeta();
		String ItemName = "§c暂不可用§0"+MapName;
		String[] ItemLore = {"§7这个房间有人在玩了"};
		i.setAmount(1);
		im.setDisplayName(ItemName);
		im.setLore(Arrays.asList(ItemLore));
		i.setItemMeta(im);
		return i;
	}

}
