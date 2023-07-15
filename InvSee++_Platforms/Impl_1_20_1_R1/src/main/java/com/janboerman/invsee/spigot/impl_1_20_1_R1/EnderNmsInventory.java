package com.janboerman.invsee.spigot.impl_1_20_1_R1;

import com.janboerman.invsee.spigot.api.CreationOptions;
import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.spigot.api.template.EnderChestSlot;
import com.janboerman.invsee.spigot.internal.inventory.AbstractNmsInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;

import java.util.List;
import java.util.UUID;

class EnderNmsInventory extends AbstractNmsInventory<EnderChestSlot, EnderBukkitInventory, EnderNmsInventory> implements Container, MenuProvider {

	protected NonNullList<ItemStack> storageContents;

	EnderNmsInventory(UUID targetPlayerUuid, String targetPlayerName, NonNullList<ItemStack> storageContents, CreationOptions<EnderChestSlot> creationOptions) {
		super(targetPlayerUuid, targetPlayerName, creationOptions);
		this.storageContents = storageContents;
	}

	@Override
	protected EnderBukkitInventory createBukkit() {
		return new EnderBukkitInventory(this);
	}

	@Override
	public void setMaxStackSize(int size) {
		this.maxStack = size;
	}

	@Override
	public int getMaxStackSize() {
		return maxStack;
	}

	@Override
	public int defaultMaxStack() {
		return Container.LARGE_MAX_STACK_SIZE;
	}

	@Override
	public void shallowCopyFrom(EnderNmsInventory from) {
		this.setMaxStackSize(from.getMaxStackSize());
		this.storageContents = from.storageContents;
		setChanged();
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return new EnderNmsContainer(containerId, this, playerInventory, player, creationOptions);
	}

	@Override
	public Component getDisplayName() {
		//return new TextComponent("minecraft:generic_9x" + (storageContents.size() / 9));
		return CraftChatMessage.fromStringOrNull(creationOptions.getTitle().titleFor(Target.byGameProfile(targetPlayerUuid, targetPlayerName)));
	}

	@Override
	public void clearContent() {
		storageContents.clear();
	}

	@Override
	public int getContainerSize() {
		return storageContents.size();
	}

	@Override
	public List<ItemStack> getContents() {
		return storageContents;
	}

	@Override
	public ItemStack getItem(int slot) {
		if (slot < 0 || slot >= getContainerSize()) return ItemStack.EMPTY;
		
		return storageContents.get(slot);
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : storageContents) {
			if (!stack.isEmpty()) return false;
		}
		return true;
	}

	@Override
	public void onClose(CraftHumanEntity bukkitPlayer) {
		super.onClose(bukkitPlayer);
	}

	@Override
	public void onOpen(CraftHumanEntity bukkitPlayer) {
		super.onOpen(bukkitPlayer);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (slot < 0 || slot >= getContainerSize()) return ItemStack.EMPTY;
		
		ItemStack stack = ContainerHelper.removeItem(storageContents, slot, amount);
		if (!stack.isEmpty()) {
			this.setChanged();
		}
		return stack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if (slot < 0 || slot >= getContainerSize()) return ItemStack.EMPTY;
		
		ItemStack stack = storageContents.get(slot);
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		} else {
			storageContents.set(slot, ItemStack.EMPTY);
			return stack;
		}
	}

	@Override
	public void setChanged() {
		//probably don't need to do anything here.
	}

	@Override
	public void setItem(int slot, ItemStack itemStack) {
		if (slot < 0 || slot >= getContainerSize()) return;
		
		storageContents.set(slot, itemStack);
		if (!itemStack.isEmpty() && itemStack.getCount() > getMaxStackSize()) {
			itemStack.setCount(getMaxStackSize());
		}
		
		setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		//no chest lock
		return true;
	}


	// === workarounds for Mohist ===
	// For now, use the same SRG names as in 1.19.4, as they seem to be stable.

	public int m_6893_() {
		return getMaxStackSize();
	}

	public void m_6211_() {
		clearContent();
	}

	public AbstractContainerMenu m_7208_(int containerId, Inventory playerInventory, Player viewer) {
		return createMenu(containerId, playerInventory, viewer);
	}

	public Component m_5446_() {
		return getDisplayName();
	}

	public int m_6643_() {
		return getContainerSize();
	}

	public ItemStack m_8020_(int slot) {
		return getItem(slot);
	}

	public boolean m_7983_() {
		return isEmpty();
	}

	public ItemStack m_7407_(int slot, int amount) {
		return removeItem(slot, amount);
	}

	public ItemStack m_8016_(int slot) {
		return removeItemNoUpdate(slot);
	}

	public void m_6596_() {
		setChanged();
	}

	public void m_6836_(int slot, ItemStack itemStack) {
		setItem(slot, itemStack);
	}

	public boolean m_6542_(Player player) {
		return stillValid(player);
	}

}