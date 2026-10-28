package com.blakebr0.cucumber.client.handler;

import com.blakebr0.cucumber.iface.ICustomBow;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class BowFOVHandler {
	@SubscribeEvent
	public void onFOVUpdate(FOVUpdateEvent event) {
		var entity = event.getEntity();
		var stack = entity.getActiveItemStack();

		if (!stack.isEmpty()) {
			var item = stack.getItem();
			if (item instanceof ICustomBow bow && bow.hasFOVChange()) {
				float f = MathHelper.clamp((stack.getMaxItemUseDuration() - entity.getItemInUseCount()) * bow.getDrawSpeedMulti(stack) / 20.0F, 0, 1.0F);

				event.setNewfov(event.getNewfov() - (f * f * 0.15F));
			}
		}
	}
}
