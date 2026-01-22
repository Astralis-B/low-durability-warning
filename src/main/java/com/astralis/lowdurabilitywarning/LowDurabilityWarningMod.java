package com.astralis.lowdurabilitywarning;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;
import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LowDurabilityWarningMod implements ClientModInitializer {

	// Configuración del mod
	public static LowDurabilityConfig config = new LowDurabilityConfig();

	// Seguimiento de daño por ítem
	private static Map<String, Integer> lastDamages = new HashMap<>();

	// Control de sonidos
	private static float soundPitch = 1.0f;
	private static int soundRepeats = 0;

	// Cooldown de alertas
	private static int alertCooldownTicks = 0;
	private static final int ALERT_COOLDOWN = 40;

	// Programador para sonidos con delay
	private static final ScheduledExecutorService scheduler =
			Executors.newScheduledThreadPool(1);

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// Cargar configuración
			config = ConfigManager.load();

			// Verificar condiciones básicas
			if (client.player == null) return;
			if (client.isPaused()) return;
			if (!config.enableWarnings) return;

			// Actualizar cooldown
			if (alertCooldownTicks > 0) {
				alertCooldownTicks--;
			}

			// Verificar herramientas en mano principal
			if (config.includeTools) {
				checkItemDurability(client.player.getMainHandStack(), client, "MAINHAND");
			}

			// Verificar armadura equipada
			if (config.includeArmor) {
				checkItemDurability(client.player.getEquippedStack(EquipmentSlot.HEAD), client, "HEAD");

				ItemStack chest = client.player.getEquippedStack(EquipmentSlot.CHEST);
				if (!(chest.getItem() == Items.ELYTRA && !config.includeElytra)) {
					checkItemDurability(chest, client, "CHEST");
				}

				checkItemDurability(client.player.getEquippedStack(EquipmentSlot.LEGS), client, "LEGS");
				checkItemDurability(client.player.getEquippedStack(EquipmentSlot.FEET), client, "FEET");
			}

			// Verificar elytra específicamente
			if (config.includeElytra) {
				ItemStack chest = client.player.getEquippedStack(EquipmentSlot.CHEST);
				if (chest.getItem() == Items.ELYTRA) {
					checkItemDurability(chest, client, "ELYTRA");
				}
			}

			// Reproducir sonidos programados
			if (soundRepeats > 0) {
				final int repeats = soundRepeats;
				final float pitch = soundPitch;
				soundRepeats = 0;

				for (int i = 0; i < repeats; i++) {
					int delay = i * config.soundDelayMillis;
					scheduler.schedule(() -> client.execute(() -> client.player.playSound(
							SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(),
							config.volume,
							pitch
					)), delay, TimeUnit.MILLISECONDS);
				}
			}
		});
	}

	private static void checkItemDurability(ItemStack stack, MinecraftClient client, String slot) {
		if (stack.isEmpty() || !stack.isDamageable()) return;
		if (config.onlyEnchanted && !stack.hasEnchantments()) return;

		int currentDamage = stack.getDamage();
		int remaining = stack.getMaxDamage() - currentDamage;
		String key = slot + ":" + stack.getItem().toString();
		int previousDamage = lastDamages.getOrDefault(key, -1);

		if (previousDamage == -1) {
			lastDamages.put(key, currentDamage);
			return;
		}

		if (currentDamage > previousDamage) {
			if (remaining == config.firstWarning ||
					remaining == config.secondWarning ||
					remaining == config.thirdWarning) {

				if (alertCooldownTicks == 0) {
					String color;
					float pitch;
					int repeats;

					if (remaining == config.firstWarning) {
						color = "§e";
						pitch = config.pitch;
						repeats = config.soundRepeats;
					} else if (remaining == config.secondWarning) {
						color = "§6";
						pitch = config.pitch + 0.3f;
						repeats = config.soundRepeats;
					} else {
						color = "§c";
						pitch = config.pitch + 0.6f;
						repeats = config.soundRepeats;
					}

					String label = stack.getName().getString();
					client.player.sendMessage(
							Text.literal(color + "⚠ " + label + " " + config.alertText + ": " + remaining),
							true
					);

					for (int i = 0; i < repeats; i++) {
						int delay = i * config.soundDelayMillis;
						scheduler.schedule(() -> client.execute(() -> client.player.playSound(
								SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(),
								config.volume,
								pitch
						)), delay, TimeUnit.MILLISECONDS);
					}

					alertCooldownTicks = ALERT_COOLDOWN;
				}
			}
		}

		lastDamages.put(key, currentDamage);
	}

	public static void triggerRealAlerts() {
		final MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) return;

		// Primera alerta
		client.player.sendMessage(
				Text.literal("§e⚠ Tool " + config.alertText + ": " + config.firstWarning),
				true
		);

		// Segunda alerta con delay
		scheduler.schedule(() -> client.execute(() -> client.player.sendMessage(
				Text.literal("§6⚠ Armor " + config.alertText + ": " + config.secondWarning),
				true
		)), 2000, TimeUnit.MILLISECONDS);

		// Tercera alerta con delay
		scheduler.schedule(() -> client.execute(() -> client.player.sendMessage(
				Text.literal("§c⚠ Elytra " + config.alertText + ": " + config.thirdWarning),
				true
		)), 4000, TimeUnit.MILLISECONDS);
	}
}