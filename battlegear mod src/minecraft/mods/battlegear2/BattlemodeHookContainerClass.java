package mods.battlegear2;

import mods.battlegear2.api.IArrowCatcher;
import mods.battlegear2.api.IOffhandDual;
import mods.battlegear2.api.IShield;
import mods.battlegear2.api.weapons.IExtendedReachWeapon;
import mods.battlegear2.api.weapons.OffhandAttackEvent;
import mods.battlegear2.inventory.InventoryPlayerBattle;
import mods.battlegear2.packet.BattlegearShieldFlashPacket;
import mods.battlegear2.packet.BattlegearSyncItemPacket;
import mods.battlegear2.utils.BattlegearUtils;
import mods.battlegear2.utils.EnumBGAnimations;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet53BlockChange;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class BattlemodeHookContainerClass {

    @ForgeSubscribe
    public void onEntityJoin(EntityJoinWorldEvent event){
        if(event.entity instanceof EntityPlayer){
            PacketDispatcher.sendPacketToPlayer(
                    new BattlegearSyncItemPacket((EntityPlayer)event.entity).generatePacket(),
                    (Player)event.entity);

        }
    }

    @ForgeSubscribe
    public void attackEntity(AttackEntityEvent event){
        ItemStack mainhand = event.entityPlayer.getCurrentEquippedItem();
        if(mainhand != null){
            if(mainhand.getItem() instanceof IExtendedReachWeapon){
                float reachMod = ((IExtendedReachWeapon) mainhand.getItem()).getReachModifierInBlocks(mainhand);
                if(reachMod < 0){
                    if(reachMod + 4 < event.entityPlayer.getDistanceToEntity(event.target)){
                        //event.setCanceled(true);
                        event.setResult(Result.DENY);
                    }
                }
            }
        }

        if(event.entityPlayer.specialActionTimer > 0){
            event.setCanceled(true);
        }

    }

    @ForgeSubscribe
    public void playerInterect(PlayerInteractEvent event) {
        if(event.entityPlayer.specialActionTimer > 0){
            event.setCanceled(true);
            event.setResult(Result.DENY);
            event.useBlock = Result.DENY;
            event.useItem = Result.DENY;
            event.entityPlayer.isSwingInProgress = false;
        } else if (event.entityPlayer.isBattlemode()) {
            ItemStack mainHandItem = event.entityPlayer.getCurrentEquippedItem();
            ItemStack offhandItem = event.entityPlayer.inventory.getStackInSlot(event.entityPlayer.inventory.currentItem + 3);

            switch (event.action) {
                case LEFT_CLICK_BLOCK:
                    break;
                case RIGHT_CLICK_BLOCK:

                    if (offhandItem != null && offhandItem.getItem() instanceof IOffhandDual) {
                        event.useItem = Result.DENY;
                        boolean shouldSwing = ((IOffhandDual) offhandItem.getItem()).offhandClickBlock(event, mainHandItem, offhandItem);

                        if (shouldSwing) {
                            event.entityPlayer.swingOffItem();
                            Battlegear.proxy.sendAnimationPacket(EnumBGAnimations.OffHandSwing, event.entityPlayer);
                        }

                    }else if (offhandItem != null && offhandItem.getItem() instanceof IShield){
                        event.useItem = Result.DENY;
                    } else if (offhandItem != null && offhandItem.getItem() instanceof ItemBlock){
                        event.useItem = Result.DENY;
                        event.useBlock = Result.DENY;

                        int blockId = event.entityLiving.worldObj.getBlockId(event.x, event.y, event.z);
                        if(offhandItem.tryPlaceItemIntoWorld(event.entityPlayer, event.entityPlayer.worldObj,
                                event.x, event.y, event.z, event.face, 0, 0, 0)){
                        	
                            if(event.entityPlayer.capabilities.isCreativeMode){
                                offhandItem.stackSize++;
                            }

                            if(offhandItem.stackSize <= 0){
                                ForgeEventFactory.onPlayerDestroyItem(event.entityPlayer, offhandItem);
                                event.entityPlayer.inventory.setInventorySlotContents(event.entityPlayer.inventory.currentItem+InventoryPlayerBattle.WEAPON_SETS, null);
                            }

                            if(FMLCommonHandler.instance().getEffectiveSide().isServer()){
                                PacketDispatcher.sendPacketToAllAround(event.x, event.y, event.z, 32, event.entityPlayer.dimension,
                                        new Packet53BlockChange(event.x, event.y, event.z, event.entityPlayer.worldObj));
                            }
                        }
                    }else{
                        event.entityPlayer.swingOffItem();
                        Battlegear.proxy.sendAnimationPacket(EnumBGAnimations.OffHandSwing, event.entityPlayer);
                    }
                    break;

                case RIGHT_CLICK_AIR:

                    if (mainHandItem == null || BattlegearUtils.isMainHand(mainHandItem, offhandItem)) {

                        event.useItem = Result.DENY;
                        event.setCanceled(true);

                        if (offhandItem != null && offhandItem.getItem() instanceof IOffhandDual) {
                            boolean shouldSwing = ((IOffhandDual) offhandItem.getItem()).offhandClickAir(event, mainHandItem, offhandItem);

                            if (shouldSwing) {
                                event.entityPlayer.swingOffItem();
                                Battlegear.proxy.sendAnimationPacket(EnumBGAnimations.OffHandSwing, event.entityPlayer);
                            }

                        } else if (offhandItem != null && offhandItem.getItem() instanceof IShield){
                            event.useItem = Result.DENY;
                        }else{
                            event.entityPlayer.swingOffItem();
                            Battlegear.proxy.sendAnimationPacket(EnumBGAnimations.OffHandSwing, event.entityPlayer);
                        }
                        break;
                    } else {
                        break;
                    }
            }
        }

    }

    @ForgeSubscribe
    public void playerIntereactEntity(EntityInteractEvent event) {
        if(event.entityPlayer.specialActionTimer > 0){
            event.setCanceled(true);
            event.setResult(Result.DENY);
            event.entityPlayer.isSwingInProgress = false;
        } else if (event.entityPlayer.isBattlemode()) {

            ItemStack mainHandItem = event.entityPlayer.getCurrentEquippedItem();
            ItemStack offhandItem = event.entityPlayer.inventory.getStackInSlot(event.entityPlayer.inventory.currentItem + 3);

            if (offhandItem != null && offhandItem.getItem() instanceof IOffhandDual) {

                if(mainHandItem == null || BattlegearUtils.isMainHand(mainHandItem, offhandItem)){
                    OffhandAttackEvent offAttackEvent = new OffhandAttackEvent(event);

                    ((IOffhandDual) offhandItem.getItem()).offhandAttackEntity(offAttackEvent, mainHandItem, offhandItem);

                    if (offAttackEvent.swingOffhand) {
                        event.entityPlayer.swingOffItem();
                        Battlegear.proxy.sendAnimationPacket(EnumBGAnimations.OffHandSwing, event.entityPlayer);
                    }

                    if (offAttackEvent.shouldAttack) {
                        //event.entityPlayer.attackTargetEntityWithCurrentOffItem(event.target);

                        BattlegearUtils.attackTargetEntityWithCurrentOffItem(event.entityPlayer, event.target);


                        event.setCanceled(true);
                        event.setResult(Result.DENY);
                    }
                }

            } else if (offhandItem != null && offhandItem.getItem() instanceof IShield){
                event.setCanceled(true);
                event.setResult(Result.DENY);
            }else{
                if(mainHandItem == null || BattlegearUtils.isMainHand(mainHandItem, offhandItem)){
                    event.setCanceled(true);
                    event.setResult(Result.DENY);
                    event.entityPlayer.swingOffItem();
                    BattlegearUtils.attackTargetEntityWithCurrentOffItem(event.entityPlayer, event.target);

                    Battlegear.proxy.sendAnimationPacket(EnumBGAnimations.OffHandSwing, event.entityPlayer);
                }
            }


        }
    }



    @ForgeSubscribe
    public void shieldHook(LivingHurtEvent event){

        if(event.entity instanceof EntityPlayer){

            EntityPlayer player = (EntityPlayer)event.entity;

            if(player.specialActionTimer > 0){
                event.setCanceled(true);
            } else if(player.isBlockingWithShield()){
                ItemStack shield = player.inventory.getStackInSlot(player.inventory.currentItem + 3);
                if(((IShield)shield.getItem()).canBlock(shield, event.source)){

                    boolean shouldBlock = true;
                    Entity opponent = event.source.getEntity();
                    if(opponent != null){
                        double d0 = opponent.posX - event.entity.posX;
                        double d1;

                        for (d1 = opponent.posZ - player.posZ; d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D)
                        {
                            d0 = (Math.random() - Math.random()) * 0.01D;
                        }

                        float yaw = (float)(Math.atan2(d1, d0) * 180.0D / Math.PI) - player.rotationYaw;
                        yaw = yaw - 90;

                        while(yaw < -180){
                            yaw+= 360;
                        }
                        while(yaw >= 180){
                            yaw-=360;
                        }

                        float blockAngle = ((IShield) shield.getItem()).getBlockAngle(shield);

                        shouldBlock = yaw < blockAngle && yaw > -blockAngle;

                        //player.knockBack(opponent, 50, 100, 100);
                    }


                    if(shouldBlock){
                        event.setCanceled(true);

                        PacketDispatcher.sendPacketToAllAround(player.posX, player.posY, player.posZ, 32, player.dimension,
                                new BattlegearShieldFlashPacket(player, event.ammount).generatePacket());
                    	player.worldObj.playSoundAtEntity(player, "battlegear2:shield", 1, 1);
                    	
                        if(event.source.isProjectile()){
                            if(event.source instanceof EntityDamageSourceIndirect){
                                if(event.source.getEntity() instanceof EntityArrow){
                                    event.source.getEntity().setDead();
                                    if(shield.getItem() instanceof IArrowCatcher){
                                        ((IArrowCatcher)shield.getItem()).setArrowCount(shield, ((IArrowCatcher) shield.getItem()).getArrowCount(shield)+1);
                                        ((InventoryPlayerBattle)player.inventory).hasChanged = true;

                                        player.setArrowCountInEntity(player.getArrowCountInEntity()-1);

                                    }
                                }
                            }
                        }


                        if(!player.capabilities.isCreativeMode){
                            shield.damageItem((int)event.ammount, player);
                            if(shield.getItemDamage() <= 0){
                                player.inventory.setInventorySlotContents(player.inventory.currentItem + 3, null);
                                //TODO Render item break
                            }
                        }
                        ((InventoryPlayerBattle)player.inventory).hasChanged = true;
                    }
                }
            }

        }
    }

}
