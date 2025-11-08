package io.github.townyadvanced.iconomy.settings;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.iConomy.util.StringMgmt;

import io.github.townyadvanced.commentedconfiguration.CommentedConfiguration;
import io.github.townyadvanced.iconomy.iConomyUnlocked;
import io.github.townyadvanced.iconomy.util.FileMgmt;

public class Settings {
	private static CommentedConfiguration config, newConfig;
	static DecimalFormat FORMAT = new DecimalFormat("###,###,###.##");

	public static void loadConfig(Path configPath, String version) throws Exception {
		if (FileMgmt.checkOrCreateFile(configPath.toString())) {

			// read the config.yml into memory
			config = new CommentedConfiguration(configPath);
			if (!config.load())
				throw new Exception("Failed to load config.yml.");

			setDefaults(iConomyUnlocked.getPlugin().getVersion(), configPath);
			config.save();
		}
	}

	public static void addComment(String root, String... comments) {

		newConfig.addComment(root.toLowerCase(), comments);
	}

	private static void setNewProperty(String root, Object value) {

		if (value == null) {
			value = "";
		}
		newConfig.set(root.toLowerCase(), value.toString());
	}

	@SuppressWarnings("unused")
	private static void setProperty(String root, Object value) {

		config.set(root.toLowerCase(), value.toString());
	}
	
	/**
	 * Builds a new config reading old config data.
	 */
	private static void setDefaults(String version, Path configPath) {

		newConfig = new CommentedConfiguration(configPath);
		newConfig.load();

		for (ConfigNodes root : ConfigNodes.values()) {
			if (root.getComments().length > 0)
				addComment(root.getRoot(), root.getComments());
			if (root.getRoot() == ConfigNodes.VERSION.getRoot())
				setNewProperty(root.getRoot(), version);
			else
				setNewProperty(root.getRoot(), (config.get(root.getRoot().toLowerCase()) != null) ? config.get(root.getRoot().toLowerCase()) : root.getDefault());
		}

		config = newConfig;
		newConfig = null;
	}
	
	public static String getString(String root, String def) {

		String data = config.getString(root.toLowerCase(), def);
		if (data == null) {
			sendError(root.toLowerCase() + " from config.yml");
			return "";
		}
		return data;
	}

	private static void sendError(String msg) {

		iConomyUnlocked.getPlugin().getLogger().severe("Error could not read " + msg);
	}
	
	public static boolean getBoolean(ConfigNodes node) {

		return Boolean.parseBoolean(config.getString(node.getRoot().toLowerCase(), node.getDefault()));
	}

	public static double getDouble(ConfigNodes node) {

		try {
			return Double.parseDouble(config.getString(node.getRoot().toLowerCase(), node.getDefault()).trim());
		} catch (NumberFormatException e) {
			sendError(node.getRoot().toLowerCase() + " from config.yml");
			return 0.0;
		}
	}

	public static int getInt(ConfigNodes node) {

		try {
			return Integer.parseInt(config.getString(node.getRoot().toLowerCase(), node.getDefault()).trim());
		} catch (NumberFormatException e) {
			sendError(node.getRoot().toLowerCase() + " from config.yml");
			return 0;
		}
	}

	public static String getString(ConfigNodes node) {

		return config.getString(node.getRoot().toLowerCase(), node.getDefault());
	}

	public static List<String> getStrArr(ConfigNodes node) {

		return Arrays.stream(getString(node).split(",")).collect(Collectors.toList());
	}

	public static String format(BigDecimal money) {
		return format(FORMAT.format(money.doubleValue()));
	}

	public static String format(double money) {
		return format(BigDecimal.valueOf(money));
	}

	private static String format(String money) {
		return String.format(Settings.getEconomyFormat(), money);
	}

	public static String format(UUID uuid, String name) {
		return format(iConomyUnlocked.getAccounts().get(uuid, name).getHoldings().balance());
	}

	public static String getCurrencyName() {
		return getString(ConfigNodes.CURRENCY_SETTINGS_NAME);
	}

	public static String getCurrencyNameSingular() {
		return getString(ConfigNodes.CURRENCY_SETTINGS_NAME_SINGULAR);
	}

	public static String getCurrencyNamePlural() {
		return getString(ConfigNodes.CURRENCY_SETTINGS_NAME_PLURAL);
	}

	public static String getEconomyFormat() {
		return getString(ConfigNodes.CURRENCY_SETTINGS_FORMAT);
	}

	public static int getVaultFractionalDigits() {
		return getInt(ConfigNodes.CURRENCY_SETTINGS_VAULT_FRACTIONAL_DIGITS);
	}
	
	public static double getDefaultBalance() {
		return getDouble(ConfigNodes.CURRENCY_SETTINGS_DEFAULT_BALANCE);
	}

	public static String getDBType() {
		return getString(ConfigNodes.DATABASE_TYPE);
	}

	public static String getDBName() {
		return getString(ConfigNodes.DATABASE_NAME);
	}

	public static String getDBTable() {
		return getString(ConfigNodes.DATABASE_TABLE);
	}

	public static String getMysqlUser() {
		return getString(ConfigNodes.DATABASE_MYSQL_USER);
	}

	public static String getMysqlPass() {
		return getString(ConfigNodes.DATABASE_MYSQL_PASS);
	}

	public static String getMysqlHostname() {
		return getString(ConfigNodes.DATABASE_MYSQL_HOSTNAME);
	}

	public static String getMysqlPort() {
		return getString(ConfigNodes.DATABASE_MYSQL_PORT);
	}

	public static String getMysqlFlags() {
		return getString(ConfigNodes.DATABASE_MYSQL_FLAGS);
	}

	public static boolean transactionLoggingEnabled() {
		return getBoolean(ConfigNodes.TRANSACTION_LOGGING_ENABLED);
	}

	public static List<String> getNonPlayerAccountPrefixes() {
		return getStrArr(ConfigNodes.ACCOUNT_SETTINGS_NON_PLAYER_ACCOUNT_NAME_PREFIXES);
	}

	public static boolean isNonPlayerAccountName(String name) {
		return StringMgmt.filterByStart(getNonPlayerAccountPrefixes(), name).size() > 0;
	}
}
