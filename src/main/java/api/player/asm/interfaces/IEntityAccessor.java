package api.player.asm.interfaces;

import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;

public interface IEntityAccessor
{
    boolean getFirstUpdate();

    float getInternalEyeHeightForge(Pose pose, EntitySize size);

    EntitySize getSize();

    void setEyeHeight(float eyeHeight);

    void setSize(EntitySize size);
}
