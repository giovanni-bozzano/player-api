package api.player.asm.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;

public interface IClientPlayerEntityAccessor
{
    Minecraft getMinecraft();

    Vec3d getMotionMultiplier();
}
