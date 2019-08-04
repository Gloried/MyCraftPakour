# MyCraftPakour 我的手艺跑酷插件

---
包含PlayerManager玩家管理器用以管理玩家存档点、MapManager用以管理跑酷房间、load主类用以监听玩家事件

|指令|权限|作用|
|:----|:----:|:----:|
|/pk join 关卡数|mycraft.cmd.player|打开加入GUI|
|/pk leave|mycraft.cmd.player|离开关卡回到大厅|
|/pk createroom 房间名|mycraft.cmd.admin|创建一个房间，房间的出生点坐标即你的位置坐标|
|/pk setpoint 房间名 spawn/end/ex|mycraft.cmd.admin|将你的位置坐标设置为某个房间的spawn/end/ex检查点坐标|
|/pk additem 房间名|mycraft.cmd.admin|将你手上的道具添加到某房间的道具列表中|
|/pk setparent 房间名 父房间名|mycraft.cmd.admin|将某房间的父房间设置为[父房间名]，父、子房间除了出生点不一样，其他的地图、道具、检查点离出生点的偏移都一样|

---
工作流程：
![avatar](https://github.com/Gloried/MyCraftPakour/blob/master/pk_program.jpg)

