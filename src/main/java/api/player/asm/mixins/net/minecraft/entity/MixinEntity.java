package api.player.asm.mixins.net.minecraft.entity;

import api.player.asm.interfaces.IEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class MixinEntity implements IEntityAccessor
{
    @Shadow
    private float eyeHeight;
    @Shadow
    protected boolean firstUpdate;
    @Shadow
    private EntitySize size;

    @Shadow
    protected abstract float getEyeHeightForge(Pose pose, EntitySize size);

    @Override
    public boolean getFirstUpdate()
    {
        return this.firstUpdate;
    }

    @Override
    public float getInternalEyeHeightForge(Pose pose, EntitySize size)
    {
        return this.getEyeHeightForge(pose, size);
    }

    @Override
    public EntitySize getSize()
    {
        return this.size;
    }

    @Override
    public void setEyeHeight(float eyeHeight)
    {
        this.eyeHeight = eyeHeight;
    }

    @Override
    public void setSize(EntitySize size)
    {
        this.size = size;
    }
}
