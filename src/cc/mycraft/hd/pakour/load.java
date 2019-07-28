package cc.mycraft.hd.pakour;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import cc.mycraft.ns.actionbar.Title;
import net.milkbowl.vault.economy.Economy;






public class load extends JavaPlugin implements Listener{
	/*
	 * 跑酷游戏逻辑
	 * 
	 * 0 插件运行后，根据配置文件将所有地图放到一个HashMap<String,boolean>里，玩家进入后将相应key的值设置为true。
	 * 1 玩家进入跑酷游戏大厅，大厅和所有关卡都在同一个世界。
	 * 2 玩家选择一关的某个房间进入，选择时检查玩家是否已通关前一关，否则不予进入。同时检查该房间有没有人在游玩，有则不予进入。
	 * 3 玩家踩上黄金压力板之后存储这个存储位置
	 * 4 玩家通关(按按钮之后)，记录玩家的关卡进度
	 * 5 玩家死亡后退出地图，使用/pk bak指令回到上一个出生点。若相应房间有人，则取消。
	 * 
	 * 
	 * */
	
	private HashMap<String,Block> ClickedBlock = new HashMap<String,Block>();
	public static HashMap<String,Boolean> FinishedPlayers = new HashMap<String,Boolean>();
	public static HashMap<String,Boolean> FinishedEXPlayers = new HashMap<String,Boolean>();
	public static HashMap<String,Long> HealCooldown = new HashMap<String,Long>();
	public static Location SpawnLocation;
	public static Economy econ;
	public static boolean EcoLoad = false;

	private static load main;
	public static List<String> RoomList; 
	
	@Override
	public void onEnable() {
		main = this;
		saveDefaultConfig();
		SpawnLocation = new Location(Bukkit.getWorld(getConfig().getString("PakourLobby.World")), 
							getConfig().getDouble("PakourLobby.x"), 
							getConfig().getDouble("PakourLobby.y"), 
							getConfig().getDouble("PakourLobby.z"));
		RoomList = (List<String>)getConfig().getList("MapLoad");
		MapManager.run();
		PlayerManager.run();
		
		if(Bukkit.getServer().getPluginManager().getPlugin("Vault")!=null) {
			RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
			econ = (Economy)rsp.getProvider();
			EcoLoad = true;
			Bukkit.getLogger().info("Vault Found! Enable rewards!");
		}
		
		getServer().getPluginManager().registerEvents(this, this);
		super.onEnable();
	}
	
	public void onDisable() {
		super.onDisable();
	}

	
	public static load getInstance() {
		return main;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		MapManager.PlayerExit(e.getEntity());
	}
	
	public void BackToLobby(Player p) {
		if(!MapManager.isPlaying(p)) {
			p.sendMessage("你没有加入跑酷房间。");
			return;
		}
		p.teleport(SpawnLocation);
		MapManager.PlayerExit(p);
	}
	@EventHandler
	public void onPlayerClickInventory(InventoryClickEvent e) {
		MapManager.onPlayerClickIcon(e);
	}
	@EventHandler
	public void onPlayerQuitServer(PlayerQuitEvent e) {
		MapManager.PlayerExit(e.getPlayer());
	}
	
	public static void healPlayer(Player p) {
		p.setHealth(p.getMaxHealth());
		p.setFoodLevel(20);
	}
	
	@EventHandler
	public void onPlayerWalkOnGoldTapBoard(PlayerInteractEvent e) {
		Player p =  e.getPlayer();
		if(!MapManager.isPlaying(p)||e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
			return;
		}
		if(e.getAction().equals(Action.PHYSICAL)&&e.getClickedBlock().getType().equals(Material.GOLD_PLATE)) {
			//重复触发存档点不进行存档，减少磁盘读写。
			healPlayer(p);
			if(ClickedBlock.containsKey(p.getName()) && ClickedBlock.get(p.getName()).equals(e.getClickedBlock())) {
				return;
			}
			ClickedBlock.put(p.getName(), e.getClickedBlock());
			PlayerManager.setSaveLoc(p);
			return;
		}
		if(e.getClickedBlock().getType().equals(Material.EMERALD_BLOCK)) {
			//完成关卡，全服播报。
			if(!MapManager.isCorrectBlock(p, "End" ,e.getClickedBlock())) {
				return;
			}
			if(FinishedPlayers.containsKey(p.getName())) {
				return;
			}
			String boardcastMsg = "&6&l热烈祝贺"+p.getName()+"通关跑酷第"+MapManager.PlayerLevel.get(p)+"关!";
			if(p.getMaxHealth()==2d) {
				boardcastMsg = "&6&l热烈祝贺"+p.getName()+"大佬通关困难跑酷第"+MapManager.PlayerLevel.get(p)+"关!";
			}
			BoardCast(boardcastMsg);
			FinishedPlayers.put(p.getName(), true);
			p.sendMessage("主线关卡完成，5秒后回到大厅。");
			PlayerManager PM = new PlayerManager(p);
			Title T = new Title("§6恭喜通关", "§e获得￥"+MapManager.giveReward(p, 1)+"通关奖励!", 10, 60, 10);
			T.send(p);
			PM.setFinishedMap();
			DelayTeleport(p,5);
			return;
		}
		if(e.getClickedBlock().getType().equals(Material.DIAMOND_BLOCK)) {
			//完成关卡，全服播报。
			if(!MapManager.isCorrectBlock(p, "Ex" ,e.getClickedBlock())) {
				return;
			}
			if(FinishedEXPlayers.containsKey(p.getName())) {
				return;
			}
			String boardcastMsg = "&6&l热烈祝贺"+p.getName()+"通关跑酷第"+MapManager.PlayerLevel.get(p)+"关Ex路线!";
			if(p.getMaxHealth()==2d) {
				boardcastMsg = "&6&l热烈祝贺"+p.getName()+"大佬通关困难跑酷第"+MapManager.PlayerLevel.get(p)+"关Ex路线!";
			}
			BoardCast(boardcastMsg);
			FinishedEXPlayers.put(p.getName(), true);
			p.sendMessage("Ex关卡完成，5秒后回到大厅。");
			PlayerManager PM = new PlayerManager(p);
			Title T = new Title("§6恭喜通关EX", "§e获得￥"+MapManager.giveReward(p, 2)+"通关奖励!", 10, 60, 10);
			T.send(p);
			PM.setFinishedEXMap();
			DelayTeleport(p,5);
			return;
		}
	}

	private void DelayTeleport(Player p,long Second) {
		int TaskID = Bukkit.getScheduler().scheduleAsyncDelayedTask(this, 
				new Runnable(){
					public void run() {
						BackToLobby(p);
					}
			}, Second*20l);
	}
	
	public static void BoardCast(String msg) {
		msg = msg.replaceAll("&", "§");
		for(Player p : Bukkit.getOnlinePlayers()) {
			p.sendMessage(msg);
		}
	}
	
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		Player p = (Player) s;
		if(!(s instanceof Player)){
			return false;
		}
		if(!label.equalsIgnoreCase("pk")&&!label.equalsIgnoreCase("pakour")) {
			return false;
		}
		if(args.length<1) {
			p.sendMessage("/pk join 关卡  -  加入某一关");
			p.sendMessage("/pk leave  -  回到大厅");
			return true;
		}
		switch (args[0].toLowerCase()) {
		case "join": 
			if(args.length!=2) {
				return false;
			}
			MapManager.showEnterGui(p, args[1]);
			return true;
		case "leave": 
			BackToLobby(p);
			return true;
		case "back": 
			MapManager.backToSavePoint(p);
			return true;
		case "additem": 
			if(!p.hasPermission("mycraft.cmd.admin")) {
				p.sendMessage("§c权限不足.");
				return true;
			}
			if(args.length!=2) {
				return false;
			}
			MapManager.addRoomItem(p, args[1]);
			return true;
		case "setreward": 
			if(!p.hasPermission("mycraft.cmd.admin")) {
				p.sendMessage("§c权限不足.");
				return true;
			}
			if(args.length!=3) {
				return false;
			}
			MapManager.setReward(p, args[1], args[2]);
			return true;
		case "setparent": 
			if(!p.hasPermission("mycraft.cmd.admin")) {
				p.sendMessage("§c权限不足.");
				return true;
			}
			if(args.length!=3) {
				return false;
			}
			MapManager.setParent(p, args[1], args[2]);
			return true;
		case "createroom": 
			if(!p.hasPermission("mycraft.cmd.admin")) {
				p.sendMessage("§c权限不足.");
				return true;
			}
			if(args.length!=2) {
				return false;
			}
			MapManager.createRoom(p, args[1]);
			return true;
		case "setpoint": 
			if(!p.hasPermission("mycraft.cmd.admin")) {
				p.sendMessage("§c权限不足.");
				return true; 
			}
			if(args.length!=3) {
				return false;
			}
			MapManager.setCheckPoint(p, args[1], args[2]);
			return true;
		case "removeitem": 
			if(!p.hasPermission("mycraft.cmd.admin")) {
				p.sendMessage("§c权限不足.");
				return true;
			}
			if(args.length!=2) {
				return false;
			}
			MapManager.removeItem(p, args[1]);
			return true;
		case "generatebarrier": 
			GenerateBarrier.GenerateRandomBarrier();
			return true;
		case "heal": 
			if(!MapManager.isPlaying(p)) {
				p.sendMessage("§c在跑酷关卡内才能使用。");
				return true;
			}
			long now = new Date().getTime();
			if(HealCooldown.containsKey(p.getName())) {
				long need = now - HealCooldown.get(p.getName());
				if(need < 10000) {
					p.sendMessage("§e治疗术未冷却，冷却时间还剩"+(10000-need)/1000+"秒");
					return true;
				}
			}
			p.sendMessage("§a你使用了治疗术，恢复满血满饥饿。");
			HealCooldown.put(p.getName(), now);
			healPlayer(p);
			return true;
		default:
			return false;
		}
	}
}