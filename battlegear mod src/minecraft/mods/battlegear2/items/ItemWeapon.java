package mods.battlegear2.items;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import mods.battlegear2.api.weapons.Attributes;
import mods.battlegear2.api.weapons.IBattlegearWeapon;
import mods.battlegear2.utils.BattlegearConfig;
import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;

import java.util.Locale;
//Made this extend the sword class (allows them to be enchanted)
public abstract class ItemWeapon extends ItemSword implements IBattlegearWeapon, Attributes {

    protected final ToolMaterial material;
	protected String name;
	protected float baseDamage;
	
	public ItemWeapon(ToolMaterial material, String named) {
		super(material);
		//May be unsafe, but will allow others to add weapons using custom materials (also more efficient)
		this.material = material;
        this.setCreativeTab(BattlegearConfig.customTab);
		
		if(material == ToolMaterial.EMERALD){
			this.name = named+".diamond";
		}else{
			this.name = named+"."+material.name().toLowerCase(Locale.ENGLISH);
		}
		
		this.setUnlocalizedName("battlegear2:"+name);
		
		this.baseDamage = 4 + material.getDamageVsEntity();
	}

	public ToolMaterial getMaterial() {
		return this.material;
	}
	
	@Override
	public Multimap getAttributeModifiers(ItemStack stack) {
		Multimap map = HashMultimap.create();
		map.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(itemModifierUUID, "Weapon modifier", (double)this.baseDamage, 0));
        return map;
    }

	@Override
	public boolean canHarvestBlock(Block par1Block)
	{
		return false;
	}

	@Override
	public final ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer){
		return par1ItemStack;
	}

	@Override
	public final EnumAction getItemUseAction(ItemStack par1ItemStack){
		return EnumAction.NONE;
	}

	@Override
	public final int getMaxItemUseDuration(ItemStack itemStack){
		return 0;
	}
}
