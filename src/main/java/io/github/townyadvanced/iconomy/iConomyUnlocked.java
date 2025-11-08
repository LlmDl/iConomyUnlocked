package io.github.townyadvanced.iconomy;

import java.util.List;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.townyadvanced.iconomy.commands.MoneyCommand;
import io.github.townyadvanced.iconomy.listener.PlayerJoinListener;
import io.github.townyadvanced.iconomy.providers.VaultEconomy;
import io.github.townyadvanced.iconomy.providers.VaultUnlockedEconomy;
import io.github.townyadvanced.iconomy.settings.Settings;
import io.github.townyadvanced.iconomy.settings.LangStrings;
import io.github.townyadvanced.iconomy.system.Accounts;
import io.github.townyadvanced.iconomy.system.BackEnd;
import io.github.townyadvanced.iconomy.system.Transactions;
import net.milkbowl.vault2.economy.Economy;

public class iConomyUnlocked extends JavaPlugin {

	private static iConomyUnlocked plugin;
	private static BackEnd backend = null;
	private static Accounts accounts = null;
	private static Transactions transactions = null;

	public iConomyUnlocked() {
		plugin = this;
	}

	@Override
	public void onLoad() {
		if (!registerEconomy()) {
			getLogger().severe("Neither Vault or VaultUnlocked were found. Please download Vault or VaultUnlocked to use iConomyUnlocked!");
			disableWithMessage("Could not register with VaultUnlocked!");
			return;
		}
	}

	@Override
	public void onEnable() {
		try {
			loadConfig();
			loadLangFile();

			backend = new BackEnd();
			backend.setupAccountTable();
			List<String> tablesUpdated = backend.updateTables();

			accounts = new Accounts();

			if (!tablesUpdated.isEmpty())
				accounts.updateAccountsForNewTables(tablesUpdated);

			transactions = new Transactions();
			backend.setupTransactionTable();

			registerCommands();
			registerListeners();
		} catch (Exception e) {
			disableWithMessage(e.getMessage());
			return;
		}
	}

	public void loadConfig() throws Exception {
		Settings.loadConfig(getDataFolder().toPath().resolve("config.yml"), getVersion());
	}

	public void loadLangFile() throws Exception {
		LangStrings.loadLangFile(getDataFolder().toPath().resolve("lang.yml"));
	}

	public String getVersion() {
		return this.getDescription().getVersion();
	}
	
	/**
	 * Register as a ServiceProvider, and with Vault and/or VaultUnlocked.
	 * 
	 * @return true if successful.
	 */
	private boolean registerEconomy() {
		final ServicesManager sm = this.getServer().getServicesManager();

		boolean vault2hooked = false;
		boolean vault1hooked = false;
		if (vaultUnlockedPresent()) {
			Class<Economy> vaultUnlocked = net.milkbowl.vault2.economy.Economy.class;
			sm.register(vaultUnlocked, new VaultUnlockedEconomy(this), this, ServicePriority.Highest);
			getLogger().info("Registered VaultUnlocked interface.");
			vault2hooked = getServer().getServicesManager().getRegistration(vaultUnlocked) != null;

			@SuppressWarnings("deprecation")
			Class<net.milkbowl.vault.economy.Economy> vault = net.milkbowl.vault.economy.Economy.class;
			sm.register(vault, new VaultEconomy(this), this, ServicePriority.Highest);
			getLogger().info("Registered Vault interface.");
			vault1hooked = getServer().getServicesManager().getRegistration(vault) != null;
		}

		if (vaultPresent()) {
			@SuppressWarnings("deprecation")
			Class<net.milkbowl.vault.economy.Economy> vault = net.milkbowl.vault.economy.Economy.class;
			sm.register(vault, new VaultEconomy(this), this, ServicePriority.Highest);
			getLogger().info("Registered Vault interface.");
			vault1hooked = getServer().getServicesManager().getRegistration(vault) != null;
		}

		return vault1hooked || vault2hooked;
	}

	private static Function<Plugin, Boolean> vaultVersionFun = (vault) -> vault.getDescription().getVersion().startsWith("1");

	private static boolean vaultUnlockedPresent() {
		Plugin vault = plugin.getServer().getPluginManager().getPlugin("Vault");
		return vault != null && !vaultVersionFun.apply(vault);
	}

	private static boolean vaultPresent() {
		Plugin vault = plugin.getServer().getPluginManager().getPlugin("Vault");
		return vault != null && vaultVersionFun.apply(vault);
	}

	private void registerCommands() {
		MoneyCommand cmd = new MoneyCommand();
		PluginCommand command = getCommand("money");
		command.setExecutor(cmd);
		command.setTabCompleter(cmd);
	}

	private void registerListeners() {
		Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
	}

	private void disableWithMessage(String message) {
		getLogger().severe(message);
		getLogger().severe("Disabling iConomyUnlocked...");
		Bukkit.getPluginManager().disablePlugin(this);
	}

	@Override
	public void onDisable() {
		try {
			backend.connectionPool().dispose();
			getLogger().info("Plugin disabled.");
		} catch (Exception e) {
			getLogger().severe("Plugin disabled.");
		} finally {
			transactions = null;
			accounts = null;
			backend = null;
		}
	}

	public static iConomyUnlocked getPlugin() {
		return plugin;
	}

	public static BackEnd getBackEnd() {
		return backend;
	}

	public static Accounts getAccounts() {
		return accounts;
	}

	public static Transactions getTransactions() {
		return transactions;
	}
}
