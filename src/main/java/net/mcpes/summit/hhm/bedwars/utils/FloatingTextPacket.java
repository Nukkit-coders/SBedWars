package net.mcpes.summit.hhm.bedwars.utils;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.level.Position;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.RemoveEntityPacket;
import cn.nukkit.network.protocol.SetEntityDataPacket;

import java.util.ArrayList;

public class FloatingTextPacket {
    public static long addFloatingText(ArrayList<Player> players, String title, String text, Position pos) {
        if (players.isEmpty()) return 1L;
        long eid = Entity.entityCount++;
        AddEntityPacket pk = new AddEntityPacket();
        pk.entityUniqueId = eid;
        pk.entityRuntimeId = eid;
        pk.type = 64;
        pk.speedX = 0.0F;
        pk.speedY = 0.0F;
        pk.speedZ = 0.0F;
        pk.yaw = 0.0F;
        pk.pitch = 0.0F;
        pk.x = (float) pos.x;
        pk.y = (float) pos.y;
        pk.z = (float) pos.z;
        long flags = 114688L;
        pk.metadata = (new EntityMetadata()).putLong(0, flags).putString(4, title + "\n" + text);
        Server.broadcastPacket(players, pk);
        return eid;
    }

    public static long addFloatingText(ArrayList<Player> players, long eid, String title, String text, Position pos) {
        if (players.isEmpty()) return 1L;
        AddEntityPacket pk = new AddEntityPacket();
        pk.entityUniqueId = eid;
        pk.entityRuntimeId = eid;
        pk.type = 64;
        pk.speedX = 0.0F;
        pk.speedY = 0.0F;
        pk.speedZ = 0.0F;
        pk.yaw = 0.0F;
        pk.pitch = 0.0F;
        pk.x = (float) pos.x;
        pk.y = (float) pos.y;
        pk.z = (float) pos.z;
        long flags = 114688L;
        pk.metadata = (new EntityMetadata()).putLong(0, flags).putString(4, title + "\n" + text);
        Server.broadcastPacket(players, pk);
        return eid;
    }

    public static void setFloatingText(ArrayList<Player> players, long eid, String title, String text) {
        if (players.isEmpty()) return;
        SetEntityDataPacket npk = new SetEntityDataPacket();
        long flags = 114688L;
        npk.metadata = (new EntityMetadata()).putLong(0, flags).putString(4, title + "\n" + text);
        npk.eid = eid;
        Server.broadcastPacket(players, npk);
    }

    public static boolean removeFloatingText(ArrayList<Player> players, long eid) {
        if (players.isEmpty()) return false;
        RemoveEntityPacket pk = new RemoveEntityPacket();
        pk.eid = eid;
        Server.broadcastPacket(players, pk);
        return true;
    }
}
