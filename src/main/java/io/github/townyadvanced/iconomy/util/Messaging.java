package io.github.townyadvanced.iconomy.util;

import org.bukkit.command.CommandSender;

import io.github.townyadvanced.iconomy.settings.LangStrings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Messaging {

	public static void send(CommandSender sender, String message) {
		if (sender == null)
			return;
		sender.sendMessage(colorize(message));
	}

	public static void sendErrorMessage(CommandSender sender, String message) {
		send(sender, "<dark_red>" + message);
	}

	public static void sendMoneyPrefixedMsg(CommandSender sender, String message) {
		send(sender, LangStrings.moneyPrefix() + message);
	}

	/**
	 * Converts color codes into a Minimessage/Adventure component.
	 *
	 * @param original Original string to be parsed against group of color names.
	 *
	 * @return <code>Component</code> - The parsed Component after conversion.
	 */
	private static Component colorize(String original) {
		original = original.replace("`r", "<red>");
		original = original.replace("<rose>", "<red>");
		original = original.replace("`R", "<dark_red>");
		original = original.replace("<red>", "<dark_red>");
		original = original.replace("`y", "<yellow>");
		original = original.replace("`Y", "<gold>");
		original = original.replace("`g", "<green>");
		original = original.replace("<lime>", "<green>");
		original = original.replace("`G", "<dark_green>");
		original = original.replace("`a", "<aqua>");
		original = original.replace("`A", "<dark_aqua>");
		original = original.replace("<teal>", "<dark_aqua>");
		original = original.replace("`b", "<blue>");
		original = original.replace("`B", "<dark_blue>");
		original = original.replace("<navy>", "<dark_blue>");
		original = original.replace("`p", "<light_purple>");
		original = original.replace("<pink>", "<light_purple>");
		original = original.replace("`P", "<dark_purple>");
		original = original.replace("<purple>", "<dark_purple>");
		original = original.replace("`k", "<black>");
		original = original.replace("`s", "<gray>");
		original = original.replace("<silver>", "<gray>");
		original = original.replace("`S", "<dark_gray>");
		original = original.replace("`w", "<white>");
		original = original.replace("<white>", "<white>");
		return MiniMessage.miniMessage().deserialize(original);
	}

}
