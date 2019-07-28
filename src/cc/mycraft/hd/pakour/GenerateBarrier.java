package cc.mycraft.hd.pakour;


import java.util.HashMap;
import java.util.Random;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;



public class GenerateBarrier {
	
	
	public static Random r = new Random();
	
    private static Thread T;
    
    private HashMap<Integer, Integer> GeneratedAmount;
    private int generateRate;
    private int maxOffset;
    private int nonGenerateOffset;
    private World world;
    
    GenerateBarrier(int GenerateRate,int MaxOffset,World GenerateWorld){
    	this.generateRate = GenerateRate;
    	this.maxOffset = MaxOffset;
    	this.nonGenerateOffset = 0;
    	this.world = GenerateWorld;
    	this.GeneratedAmount = new HashMap<Integer,Integer>();
    }
    
    public void Generate(int BlockX,int BlockY,int StartZ,int EndZ,int GenerateRoom,int MaxOverlay) {
    	for(int i=StartZ;i<EndZ;i+=GenerateRoom) {
    		this.nonGenerateOffset++;
    		//不生成的情况，判定尝试次数是否超过Max
    		if(r.nextInt(101)>this.generateRate) {
    			if(this.nonGenerateOffset>=maxOffset) {
    				setupBarrier(BlockX,BlockY,i,MaxOverlay);
    			}
    			continue;
    		}
    		setupBarrier(BlockX,BlockY,i,MaxOverlay);
    	}
    	Bukkit.getLogger().info("生成完成：x="+BlockX+"  y="+BlockY);
    }
    
    //检查同行列Z坐标生成的重合障碍数是否太大导致封路
    private void setupBarrier(int BlockX,int BlockY,int BlockZ,int MaxOverlay) {
    	if(this.GeneratedAmount.containsKey(BlockZ)) {
			if(this.GeneratedAmount.get(BlockZ)>=MaxOverlay) {
				return;
			}
			this.GeneratedAmount.replace(BlockZ, this.GeneratedAmount.get(BlockZ)+1);
		} else {
			this.GeneratedAmount.put(BlockZ, 1);
		}
		Barrier1(this.world,BlockX,BlockY,BlockZ);
		this.nonGenerateOffset=0;
    }
    
    public void ClearBarrier(int StartX,int EndX,int StartY,int EndY,int StartZ,int EndZ) {
    	for(int i=StartX;i<EndX;i++) {
    		for(int j=StartY;j<EndY;j++) {
    			for(int k=StartZ;j<EndZ;j++) {
    				this.world.getBlockAt(i,j,k).setType(Material.AIR);
    			}
    		}
    	}
    }
    
    public void ClearGeneratedAmountHashMap() {
    	this.GeneratedAmount.clear();
    }

	public static void GenerateRandomBarrier() {
		int BlockY,Startx,Startz,Endz,StartXOffset,OffsetAmount,GenerateRate,GenerateRoom;
		GenerateRoom = 7;
		StartXOffset = 6;
		OffsetAmount = 2;
		GenerateRate = 16;
		Startx = 0;
		Startz = 0;
		BlockY = 30;
		Endz = 500;
		
		GenerateBarrier GB = new GenerateBarrier(GenerateRate,4,Bukkit.getWorld("T"));
		
		for(int i =0;i<=OffsetAmount;i++) {
			int GenerateXL = Startx - i*StartXOffset;
			int GenerateXR = Startx + i*StartXOffset;
			if(i==0) {
				GB.Generate(i, BlockY, Startz, Endz, GenerateRoom, 3);
			} else {
				GB.Generate(GenerateXL, BlockY, Startz, Endz, GenerateRoom, 3);
				GB.Generate(GenerateXR, BlockY, Startz, Endz, GenerateRoom, 3);
			}
		}
		GB.ClearGeneratedAmountHashMap();
	}
	
	public static void Barrier1(World world,int x,int y,int z) {
		world.getBlockAt(x, y, z).setType(Material.EMERALD_BLOCK);
		world.getBlockAt(x+1, y, z).setType(Material.STONE);
		world.getBlockAt(x+2, y, z).setType(Material.STONE);
		world.getBlockAt(x+3, y, z).setType(Material.COBBLE_WALL);
		world.getBlockAt(x-1, y, z).setType(Material.STONE);
		world.getBlockAt(x-2, y, z).setType(Material.STONE);
		world.getBlockAt(x-3, y, z).setType(Material.COBBLE_WALL);
		
		world.getBlockAt(x, y+1, z).setType(Material.STONE);
		world.getBlockAt(x+1, y+1, z).setType(Material.STONE);
		world.getBlockAt(x+2, y+1, z).setType(Material.STONE);
		world.getBlockAt(x+3, y+1, z).setType(Material.COBBLE_WALL);
		world.getBlockAt(x-1, y+1, z).setType(Material.STONE);
		world.getBlockAt(x-2, y+1, z).setType(Material.STONE);
		world.getBlockAt(x-3, y+1, z).setType(Material.COBBLE_WALL);
		
		world.getBlockAt(x, y+2, z).setType(Material.STONE);
		world.getBlockAt(x+1, y+2, z).setType(Material.STONE);
		world.getBlockAt(x+2, y+2, z).setType(Material.STONE);
		world.getBlockAt(x+3, y+2, z).setType(Material.COBBLE_WALL);
		world.getBlockAt(x-1, y+2, z).setType(Material.STONE);
		world.getBlockAt(x-2, y+2, z).setType(Material.STONE);
		world.getBlockAt(x-3, y+2, z).setType(Material.COBBLE_WALL);

	}
	
}
