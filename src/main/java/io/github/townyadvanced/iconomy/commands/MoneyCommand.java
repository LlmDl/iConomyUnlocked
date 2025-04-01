package io.github.townyadvanced.iconomy.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.iConomy.ConversionAccount;
import com.iConomy.iConomy;

import io.github.townyadvanced.iconomy.iConomyUnlocked;
import io.github.townyadvanced.iconomy.settings.LangStrings;
import io.github.townyadvanced.iconomy.settings.Settings;
import io.github.townyadvanced.iconomy.system.Account;
import io.github.townyadvanced.iconomy.system.Holdings;
import io.github.townyadvanced.iconomy.util.Messaging;
import io.github.townyadvanced.iconomy.util.Permissions;
import io.github.townyadvanced.iconomy.util.StringMgmt;

public class MoneyCommand implements TabExecutor {
	Logger log = iConomyUnlocked.getPlugin().getLogger();
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		try {
			switch (cmd.getLabel().toLowerCase(Locale.ROOT)) {
			case "money" -> parseMoneyCommand(sender, args);
			}
		} catch (CommandException e) {
			Messaging.sendErrorMessage(sender, e.getMessage());
		}
		return true;
	}

	public void parseMoneyCommand(CommandSender sender, String[] split) throws CommandException {
		boolean isPlayer = sender instanceof Player;
		Player player = isPlayer ? (Player) sender : null;

		if (split.length == 0) {
			if (!isPlayer)
				throw new CommandException("Specify a player to view their balance.");

			showBalance(player, player, true);
			return;
		}

		String name = split[0];
		String command = split[0].toLowerCase(Locale.ROOT);
		split = StringMgmt.remFirstArg(split);
		switch (command) {
		case "create", "-c" -> parseMoneyCreateCommand(sender, split);
		case "empty", "-e" -> parseMoneyEmptyCommand(sender);
		case "grant", "-g" -> parseMoneyGrantCommand(player, sender, isPlayer, split);
		case "help", "?" -> getMoneyHelp(sender);
		case "hide", "-h" -> parseMoneyHideCommand(sender, split);
		case "pay", "-p" -> parseMoneyPayCommand(player, sender, isPlayer, split);
		case "purge", "-pf" -> parseMoneyPurgeCommand(sender);
		case "rank", "-r" -> parseMoneyRankCommand(player, sender, isPlayer, split);
		case "remove", "-v" -> parseMoneyRemoveCommand(sender, split);
		case "reset", "-x" -> parseMoneyResetCommand(player, sender, isPlayer, split);
		case "set" -> parseMoneySetCommand(player, sender, isPlayer, split);
		case "stats", "-s" -> parseMoneyStatsCommand(sender);
		case "top", "-t" -> parseMoneyTopCommand(player, sender, split);
		case "importiconomy" -> parseImportIconomyCommand(sender, split);
		default -> parseMoneyPlayerName(sender, name);
		}
	}

	private void showBalance(Player player, CommandSender viewing, boolean mine) {
		String balance = Settings.format(player.getUniqueId(), player.getName());
		if (mine)
			Messaging.sendMoneyPrefixedMsg(viewing, LangStrings.personalBalance(balance));
		else
			Messaging.sendMoneyPrefixedMsg(viewing, LangStrings.playerBalance(player.getName(), balance));
	}

	private void showBalance(Account account, CommandSender viewing, boolean mine) {
		String balance = Settings.format(account.getUUID(), account.getName());
		if (mine)
			Messaging.sendMoneyPrefixedMsg(viewing, LangStrings.personalBalance(balance));
		else
			Messaging.sendMoneyPrefixedMsg(viewing, LangStrings.playerBalance(account.getName(), balance));
	}

	private void parseMoneyCreateCommand(CommandSender sender, String[] args) throws CommandException {
		if (!Permissions.hasPermission(sender, "iConomy.admin.account.create"))
			return;

		if (args.length == 0) {
			getMoneyHelp(sender);
			return;
		}

		if (iConomyUnlocked.getAccounts().exists(args[0]))
			throw new CommandException(LangStrings.accountAlreadyExist());

		Player player = Bukkit.getPlayerExact(args[0]);
		if (player == null)
			throw new CommandException("Player cannot be found.");

		iConomyUnlocked.getAccounts().get(player.getUniqueId(), player.getName());
		Messaging.sendMoneyPrefixedMsg(sender, LangStrings.accountCreated(player.getName()));
	}

	private void parseMoneyEmptyCommand(CommandSender sender) {
		if (!Permissions.hasPermission(sender, "iConomy.admin.empty"))
			return;

//		Confirmation.runOnAccept(()-> {
			iConomyUnlocked.getAccounts().emptyDatabase();
			Messaging.send(sender, LangStrings.accountsEmptied());
//		}).sendTo(sender);
	}

	private void parseMoneyGrantCommand(Player player, CommandSender sender, boolean isPlayer, String[] args) throws CommandException {
		if (!Permissions.hasPermission(sender, "iConomy.admin.grant"))
			return;

		if (args.length < 2) {
			getMoneyHelp(sender);
			return;
		}

		boolean console = !isPlayer;
		Player check = Bukkit.getPlayerExact(args[0]);
		String name = check != null ? check.getName() : args[0];

		Account account = Account.getAccount(name);
		if (account == null)
			throw new CommandException(LangStrings.noAccountFound(name));

		boolean silent = args.length == 3 && StringMgmt.is(args[2], new String[] { "silent", "-s" }); 

		showGrant(sender, account, player, getValidAmount(args[1]), console, silent);
	}

	private void showGrant(CommandSender sender, Account account, Player controller, double amount, boolean console, boolean silent) {
		String name = account.getName();
		Holdings holdings = account.getHoldings();
		holdings.add(amount);

		double balance = holdings.balance();
		if (amount < 0.0D)
			iConomyUnlocked.getTransactions().insert("[System]", name, 0.0D, balance, 0.0D, 0.0D, amount);
		else
			iConomyUnlocked.getTransactions().insert("[System]", name, 0.0D, balance, 0.0D, amount, 0.0D);

		Player online = Bukkit.getPlayerExact(name);
		String format = Settings.format(amount);
		if (online != null && !silent) {
			String message = amount < 0.0D ? LangStrings.personalDebit(format) : LangStrings.personalCredit(format);
			Messaging.sendMoneyPrefixedMsg(online, message);
			showBalance(online, online, true);
		}

		if (controller != null) {
			String message = amount < 0.0D ? LangStrings.playerDebit(name, format) : LangStrings.playerCredit(name, format);
			Messaging.sendMoneyPrefixedMsg(online, message);
			Messaging.send(sender, message);
		}

		if (console)
			log.info("Player " + name + "'s account had " + (amount < 0.0D ? "negative " : "") + format + " grant to it.");
		else
			log.info("Player " + name + "'s account had " + (amount < 0.0D ? "negative " : "") + format + " granted to it by " + controller.getName() + ".");
	}

	private void parseMoneyHideCommand(CommandSender sender, String[] args) throws CommandException {
		if (!Permissions.hasPermission(sender, "iConomy.admin.hide"))
			return;

		if (args.length != 2) {
			getMoneyHelp(sender);
			return;
		}

		Player check = Bukkit.getPlayerExact(args[0]);
		String name = check != null ? check.getName() : args[0];

		Account account = Account.getAccount(name);
		if (account == null)
			throw new CommandException(LangStrings.noAccountFound(name));

		boolean hidden = StringMgmt.is(args[1], new String[] { "true", "t", "-t", "yes", "da", "-d" });
		account.setHidden(hidden);
		Messaging.send(sender, LangStrings.accountHiddenStatus((hidden ? "hidden" : "visible")));
	}

	private void parseMoneyPayCommand(Player player, CommandSender sender, boolean isPlayer, String[] args) throws CommandException {
		if (!Permissions.hasPermission(sender, "iConomy.payment"))
			return;

		if (!isPlayer)
			throw new CommandException("Command unavailable from console. Try money grant {name} {amount}.");

		if (args.length < 2) {
			getMoneyHelp(sender);
			return;
		}

		if (!iConomyUnlocked.getAccounts().exists(args[0]))
			throw new CommandException(LangStrings.noAccountFound(args[0]));

		double amount = getValidAmount(args[1]);
		if (amount < 0.01D)
			throw new CommandException("Invalid amount: `w" + amount);

		Account from = Account.getAccount(player.getUniqueId());
		Account to = Account.getAccount(args[0]);
		showPayment(player, from, to, amount);
	}

	private void showPayment(Player player, Account from, Account to, double amount) throws CommandException {
		Holdings fromHoldings = from.getHoldings();
		Holdings toHoldings = to.getHoldings();

		if (from.getName().equals(to.getName()))
			throw new CommandException(LangStrings.cannotSendSelf());

		if (amount < 0.0D || !fromHoldings.hasEnough(amount))
			throw new CommandException(LangStrings.notEnoughFunds());

		fromHoldings.subtract(amount);
		toHoldings.add(amount);

		double balanceFrom = fromHoldings.balance();
		double balanceTo = toHoldings.balance();
		iConomyUnlocked.getTransactions().insert(from.getName(), to.getName(), balanceFrom, balanceTo, 0.0D, 0.0D, amount);
		iConomyUnlocked.getTransactions().insert(to.getName(), from.getName(), balanceTo, balanceFrom, 0.0D, amount, 0.0D);

		// Show the sending player their payment info and balance.
		Messaging.sendMoneyPrefixedMsg(player, LangStrings.paymentTo(Settings.format(amount), to.getName()));
		showBalance(player, player, true);

		Player playerTo = Bukkit.getPlayerExact(to.getName());
		if (playerTo != null) {
			Messaging.sendMoneyPrefixedMsg(playerTo, LangStrings.paymentFrom(player.getName(), Settings.format(amount)));
			showBalance(playerTo, playerTo, true);
		}
	}

	private void parseMoneyPurgeCommand(CommandSender sender) {
		if (!Permissions.hasPermission(sender, "iConomy.admin.purge"))
			return;

//		Confirmation.runOnAccept(()-> {
		iConomyUnlocked.getAccounts().purge();
		Messaging.send(sender, LangStrings.accountsPurged());
//		}).sendTo(sender);
	}

	private void parseMoneyRankCommand(Player player, CommandSender sender, boolean isPlayer, String[] args) throws CommandException {
		if (!Permissions.hasPermission(sender, "iConomy.rank"))
			return;

		if (args.length == 0 && !isPlayer)
			throw new CommandException("To use this command from the console you must specify a player name.");

		if (args.length == 0 && isPlayer) {
			showRank(player, player.getName());
			return;
		}

		if (!iConomyUnlocked.getAccounts().exists(args[0]))
			throw new CommandException(LangStrings.noAccountFound(args[0]));

		showRank(sender, args[0]);
	}

	private void showRank(CommandSender viewing, String accountName) throws CommandException {
		Account account = Account.getAccount(accountName);
		if (account == null)
			throw new CommandException(LangStrings.noAccountFound(accountName));

		String rank = String.valueOf(account.getRank());
		boolean isSelf = viewing.getName().equalsIgnoreCase(accountName);

		String message = isSelf ? LangStrings.personalRank(rank) : LangStrings.playerRank(accountName, rank);
		Messaging.sendMoneyPrefixedMsg(viewing, message);
	}

	private void parseMoneyRemoveCommand(CommandSender sender, String[] args) throws CommandException {
		if (!Permissions.hasPermission(sender, "iConomy.admin.account.remove"))
			return;

		if (args.length == 0) {
			getMoneyHelp(sender);
			return;
		}

		Account account = Account.getAccount(args[0]);
		if (account == null)
			throw new CommandException(LangStrings.noAccountFound(args[0]));

		iConomyUnlocked.getAccounts().remove(account.getUUID());
		Messaging.send(sender, LangStrings.accountRemoved(args[0]));
	}

	private void parseMoneyResetCommand(Player player, CommandSender sender, boolean isPlayer, String[] args) throws CommandException {
		if (!Permissions.hasPermission(sender, "iConomy.admin.reset"))
			return;

		if (args.length == 0) {
			getMoneyHelp(sender);
			return;
		}

		Account account = Account.getAccount(args[0]);
		if (account == null)
			throw new CommandException(LangStrings.noAccountFound(args[0]));

		account.getHoldings().reset();
		iConomyUnlocked.getTransactions().insert(account.getName(), "[System]", 0.0D, 0.0D, 0.0D, 0.0D, account.getHoldings().balance());
		if (player != null)
			Messaging.send(sender,LangStrings.playerReset(account.getName()));

		if (isPlayer)
			log.info("Player " + account + "'s account has been reset.");
		else
			log.info("Player " + account + "'s account has been reset by " + player.getName() + ".");
	}

	private void parseMoneySetCommand(Player player, CommandSender sender, boolean isPlayer, String[] args) throws CommandException {
		if (!Permissions.hasPermission(player, "iConomy.admin.set"))
			return;

		if (args.length == 0) {
			getMoneyHelp(sender);
			return;
		}

		Account account = Account.getAccount(args[0]);
		if (account == null) 
			throw new CommandException(LangStrings.noAccountFound(args[0]));

		showSet(sender, account, player, getValidAmount(args[1]), isPlayer);
	}


	private double getValidAmount(String num) throws CommandException {
		double amount = 0.0;
		try {
			amount = Double.parseDouble(num);
		} catch (NumberFormatException e) {
			throw new CommandException("Invalid amount: `w" + num);
		}
		return amount;
	}

	private void showSet(CommandSender sender, Account account, Player controller, double amount, boolean console) {
		if (account == null)
			return;

		Player player = Bukkit.getPlayerExact(account.getName());
		Holdings holdings = account.getHoldings();
		holdings.set(amount);

		double balance = holdings.balance();

		iConomyUnlocked.getTransactions().insert("[System]", account.getName(), 0.0D, balance, amount, 0.0D, 0.0D);

		if (player != null && controller != null) {
			Messaging.sendMoneyPrefixedMsg(sender, LangStrings.personalSet(Settings.format(balance)));
			showBalance(account, player, true);
		}

		if (controller == null)
			Messaging.sendMoneyPrefixedMsg(sender, LangStrings.playerSet(account.getName(), Settings.format(balance)));

		if (console || controller == null)
			log.info("Player " + account + "'s account has been set to " + Settings.format(amount) + ".");
		else
			log.info("Player " + account + "'s account has been set to " + Settings.format(amount) + " by " + controller.getName() + ".");
	}

	private void parseMoneyStatsCommand(CommandSender sender) {
		if (!Permissions.hasPermission(sender, "iConomy.admin.stats"))
			return;

		Collection<Double> accountHoldings = iConomyUnlocked.getAccounts().values();
		Collection<Double> totalHoldings = accountHoldings;

		double TCOH = 0.0D;
		int accounts = accountHoldings.size();
		int totalAccounts = accounts;

		for (Object o : totalHoldings.toArray())
			TCOH += ((Double) o).doubleValue();

		Messaging.send(sender, LangStrings.statsHeader());
		Messaging.send(sender, LangStrings.statsTotal(Settings.getCurrencyName(), Settings.format(TCOH)));
		Messaging.send(sender, LangStrings.statsAverage(Settings.getCurrencyName(), Settings.format(totalAccounts != 0 ? TCOH / totalAccounts : 0)));
		Messaging.send(sender, LangStrings.statsAccounts(String.valueOf(accounts)));
	}

	private void parseMoneyTopCommand(Player player, CommandSender sender, String[] args) {
		if (!Permissions.hasPermission(player, "iConomy.list"))
			return;

		if (args.length == 0) {
			showTop(sender, 5);	
			return;
		}

		try {
			int top = Integer.parseInt(args[0]);
			showTop(sender, top > 100 ? 100 : top < 0 ? 5 : top);
		} catch (Exception e) {
			showTop(sender, 5);
		}
	}

	private void showTop(CommandSender viewing, int amount) {
		LinkedHashMap<String, Double> ranking = iConomyUnlocked.getAccounts().ranking(amount);

		Messaging.send(viewing, LangStrings.topHeader(String.valueOf(amount)));

		if (ranking == null || ranking.isEmpty()) {
			Messaging.send(viewing, LangStrings.topEmpty());
			return;
		}

		int count = 1;
		for (String account : ranking.keySet()) {
			Messaging.send(viewing, LangStrings.topLine(String.valueOf(count), account, Settings.format(ranking.get(account))));
			count++;
		}
	}

	private void parseMoneyPlayerName(CommandSender sender, String name) throws CommandException {
		if (!Permissions.hasPermission(sender, "iConomy.access"))
			return;

		Account account = Account.getAccount(name);
		if (account == null)
			throw new CommandException(LangStrings.noAccountFound(name));

		showBalance(account, sender, false);
	}

	private void parseImportIconomyCommand(CommandSender sender, String[] split) throws CommandException {
		if (!Permissions.hasPermission(sender, "iConomy.admin.importiconomy"))
			return;
	
		Plugin iconomy = Bukkit.getPluginManager().getPlugin("iConomy");
		if (iconomy == null || !iconomy.isEnabled())
			throw new CommandException("Could not find iConomy on the server.");

		Set<ConversionAccount> conversionAccounts = null; 
		try {
			conversionAccounts = iConomy.getConversionAccounts();
		} catch (NoSuchMethodError e) {
			throw new CommandException("You can only import from iConomy 5.26 and newer!");
		}

		int count = 0;
		for (ConversionAccount account : conversionAccounts) {
			if (!iConomyUnlocked.getAccounts().importAccount(account.getUuid(), account.getName(), account.getBalance(), account.isHidden())) {
				log.warning("Could not import account for " + account.getName());
			}
			count++;
		}

		Messaging.send(sender, "<green>Successfully imported " + count + " accounts from iConomy5.");
	}

	/**
	 * Help documentation for iConomy all in one method.
	 *
	 * Allows us to easily utilize all throughout the class without having multiple
	 * instances of the same help lines.
	 */
	private void getMoneyHelp(CommandSender sender) {
		Messaging.send(sender, "`w iConomyUnlocked");
		Messaging.send(sender, "`w <> Required, [] Optional");

		if (sender instanceof Player)
			Messaging.send(sender, "`G  /money `y Check your balance.");

		if (Permissions.hasPermission(sender, "iConomy.access", true))
			Messaging.send(sender, "`G  /money `g[player] `y Check someone's balance.");

		Messaging.send(sender, "`G  /money `g? `y For help & Information.");

		if (Permissions.hasPermission(sender, "iConomy.rank", true))
			Messaging.send(sender, "`G  /money `grank `G[`wplayer`G] `y Rank on the topcharts.");

		if (Permissions.hasPermission(sender, "iConomy.list", true))
			Messaging.send(sender, "`G  /money `gtop `G[`wamount`G] `y Richest players listing.");

		if (Permissions.hasPermission(sender, "iConomy.payment", true))
			Messaging.send(sender, "`G  /money `gpay `G<`wplayer`G> <`wamount`G> `y Send money to a player.");

		if (Permissions.hasPermission(sender, "iConomy.admin.grant", true)) {
			Messaging.send(sender, "`G  /money `ggrant `G<`wplayer`G> <`wamount`G> [`wsilent`G] `y Give money, optionally silent.");
			Messaging.send(sender, "`G  /money `ggrant `G<`wplayer`G> -<`wamount`G> [`wsilent`G] `y Take money, optionally silent.");
		}

		if (Permissions.hasPermission(sender, "iConomy.admin.set", true))
			Messaging.send(sender, "`G  /money `gset `G<`wplayer`G> <`wamount`G> `y Sets a players balance.");

		if (Permissions.hasPermission(sender, "iConomy.admin.hide", true))
			Messaging.send(sender, "`G  /money `ghide `G<`wplayer`G> `wtrue`G/`wfalse `y Hide or show an account.");

		if (Permissions.hasPermission(sender, "iConomy.admin.account.create", true))
			Messaging.send(sender, "`G  /money `gcreate `G<`wplayer`G> `y Create player account.");

		if (Permissions.hasPermission(sender, "iConomy.admin.account.remove", true))
			Messaging.send(sender, "`G  /money `gremove `G<`wplayer`G> `y Remove player account.");

		if (Permissions.hasPermission(sender, "iConomy.admin.reset", true))
			Messaging.send(sender, "`G  /money `greset `G<`wplayer`G> `y Reset player account.");

		if (Permissions.hasPermission(sender, "iConomy.admin.purge", true))
			Messaging.send(sender, "`G  /money `gpurge `y Remove all accounts with inital holdings.");

		if (Permissions.hasPermission(sender, "iConomy.admin.empty", true))
			Messaging.send(sender, "`G  /money `gempty `y Empties database.");

		if (Permissions.hasPermission(sender, "iConomy.admin.stats", true))
			Messaging.send(sender, "`G  /money `gstats `y Check all economic stats.");
	}

	private final List<String> SUB_CMDS = Arrays.asList("?", "rank", "top", "pay", "grant", "set", "hide", "create",
			"remove", "preset", "purge", "empty", "stats", "importiconomy");
	private final List<String> PLAYER_CMDS = Arrays.asList("rank", "pay", "grant", "set", "hide", "create", "remove",
			"reset");
	private final List<String> AMOUNT_CMDS = Arrays.asList("pay","grant","set");
	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
			@NotNull String[] args) {

		String subCmdArg = args[0].toLowerCase(Locale.ROOT);

		if (args.length == 1) {
			if (StringMgmt.filterByStart(SUB_CMDS, subCmdArg).size() > 0) {
				return SUB_CMDS.stream().filter(s -> s.startsWith(subCmdArg)).collect(Collectors.toList());
			} else {
				return null;
			}
		} else if (args.length == 2) {
			if (PLAYER_CMDS.contains(subCmdArg))
				return null;
			if (subCmdArg.equals("top"))
				return Arrays.asList("<amount>");
		} else if (args.length == 3) {
			if (AMOUNT_CMDS.contains(subCmdArg))
				return Arrays.asList("<amount>");
			if (subCmdArg.equals("hide"))
				return Arrays.asList("true", "false");
		} else if (args.length == 4 && subCmdArg.equals("grant")) {
				return Arrays.asList("silent");
		}

		return Arrays.asList("");
	}

	class CommandException extends Exception {
		private static final long serialVersionUID = -7119775025122677221L;
		public CommandException(String message) {
			super(message);
		}
	}
}
