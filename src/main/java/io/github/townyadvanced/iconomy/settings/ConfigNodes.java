package io.github.townyadvanced.iconomy.settings;

public enum ConfigNodes {

	VERSION_HEADER("version", "", ""),
	VERSION(
			"version.version",
			"",
			"# This is the current version.  Please do not edit."),
	CURRENCY_SETTINGS_ROOT("currency_settings","",""),

	CURRENCY_SETTINGS_NAME("currency_settings.name",
			"Dollars",
			"",
			"# The name of your currency as it will appear in plugins using VaultUnlocked."),
	CURRENCY_SETTINGS_NAME_SINGULAR("currency_settings.name_singular",
			"Dollar",
			"",
			"# The name of your currency as it will appear in plugins using VaultUnlocked."),
	CURRENCY_SETTINGS_NAME_PLURAL("currency_settings.name_plural",
			"Dollars",
			"",
			"# The name of your currency as it will appear in plugins using VaultUnlocked."),
	CURRENCY_SETTINGS_FORMAT("currency_settings.format",
			"$%s",
			"",
			"# The format used to display currency."),
	CURRENCY_SETTINGS_VAULT_FRACTIONAL_DIGITS("currency_settings.vault_displayed_decimal_places",
			"0",
			"",
			"# Default of zero, should display 0 decimal places. -1 or 2 can display decimal places."),
	CURRENCY_SETTINGS_DEFAULT_BALANCE("currency_settings.default_balance",
			"30",
			"",
			"# Default balance of a new account."),

	ACCOUNT_SETTINGS("account_settings","","",""),
	ACCOUNT_SETTINGS_NON_PLAYER_ACCOUNT_NAME_PREFIXES("account_settings.non_player_account_name_prefixes",
			"town-,nation-,towny-server",
			"",
			"# A comma-separated list of prefixes that non-player accounts would have."),
	
	DATABASE_ROOT("database","","",""),
	DATABASE_TYPE("database.type",
			"H2",
			"",
			"# Either H2 or MYSQL"),
	DATABASE_NAME("database.name",
			"minecraft",
			"",
			"# The name of the database."),
	DATABASE_TABLE("database.table",
			"iConomyUnlocked",
			"",
			"# The name of the database table."),
	DATABASE_MYSQL_ROOT("database.mysql","","",
			"# MySQL settings (unused by H2.)"),
	
	DATABASE_MYSQL_USER("database.mysql.username",
			"root",
			"",
			"# The username of the mysql database."),
	DATABASE_MYSQL_PASS("database.mysql.password",
			"pass",
			"",
			"# The password of the mysql database."),
	DATABASE_MYSQL_HOSTNAME("database.mysql.hostname",
			"localhost",
			"",
			"# The hostname of the mysql database."),
	DATABASE_MYSQL_PORT("database.mysql.port",
			"3306",
			"",
			"# The port of the mysql database."),
	DATABASE_MYSQL_FLAGS("database.mysql.flags",
			"?verifyServerCertificate=false&useSSL=false",
			"",
			"# The flags applied to the mysql database connection."),
	
	TRANSACTION_LOGGING_ROOT("transaction_logging","","",""),
	TRANSACTION_LOGGING_ENABLED("transaction_logging.enabled",
			"false",
			"",
			"# When enabled, iConomyUnlocked will create a _Transactions table where transactions are logged to.");

	private final String Root;
	private final String Default;
	private String[] comments;

	ConfigNodes(String root, String def, String... comments) {

		this.Root = root;
		this.Default = def;
		this.comments = comments;
	}

	/**
	 * Retrieves the root for a config option
	 *
	 * @return The root for a config option
	 */
	public String getRoot() {

		return Root;
	}

	/**
	 * Retrieves the default value for a config path
	 *
	 * @return The default value for a config path
	 */
	public String getDefault() {

		return Default;
	}

	/**
	 * Retrieves the comment for a config path
	 *
	 * @return The comments for a config path
	 */
	public String[] getComments() {

		if (comments != null) {
			return comments;
		}

		String[] comments = new String[1];
		comments[0] = "";
		return comments;
	}

}
