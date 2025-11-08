package io.github.townyadvanced.iconomy.providers;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.townyadvanced.iconomy.iConomyUnlocked;
import io.github.townyadvanced.iconomy.settings.Settings;
import io.github.townyadvanced.iconomy.system.Account;
import net.milkbowl.vault2.economy.AccountPermission;
import net.milkbowl.vault2.economy.Economy;
import net.milkbowl.vault2.economy.EconomyResponse;
import net.milkbowl.vault2.economy.EconomyResponse.ResponseType;

public class VaultUnlockedEconomy implements Economy {
	private final iConomyUnlocked plugin;
	
	public VaultUnlockedEconomy(iConomyUnlocked plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean isEnabled() {
		return plugin != null && plugin.isEnabled();
	}

	@Override
	public @NotNull String getName() {
		return "iConomyUnlocked";
	}

	@Override
	public boolean hasSharedAccountSupport() {
		return false;
	}

	@Override
	public boolean hasMultiCurrencySupport() {
		return false;
	}

	@Override
	public @NotNull int fractionalDigits(String pluginName) {
		return Settings.getVaultFractionalDigits();
	}

	@Override
	public @NotNull String format(BigDecimal amount) {
		return Settings.format(amount);
	}

	@Override
	public @NotNull String format(String pluginName, BigDecimal amount) {
		return format(amount);
	}

	@Override
	public @NotNull String format(BigDecimal amount, String currency) {
		return format(amount);
	}

	@Override
	public @NotNull String format(String pluginName, BigDecimal amount, String currency) {
		return format(amount);
	}

	@Override
	public boolean hasCurrency(String currency) {
		return currency.equalsIgnoreCase(Settings.getCurrencyName());
	}

	@Override
	public @NotNull String getDefaultCurrency(String pluginName) {
		return Settings.getCurrencyName();
	}

	@Override
	public @NotNull String defaultCurrencyNamePlural(String pluginName) {
		return Settings.getCurrencyName();
	}

	@Override
	public @NotNull String defaultCurrencyNameSingular(String pluginName) {
		return Settings.getCurrencyName();
	}

	@Override
	public Collection<String> currencies() {
		return Collections.singleton(Settings.getCurrencyName());
	}

	@Override
	public boolean createAccount(UUID accountID, String name) {
		return createAccount(accountID, name, false);
	}

	@Override
	public boolean createAccount(UUID accountID, String name, String worldName) {
		return createAccount(accountID, name);
	}

	@Override
	public boolean createAccount(@NotNull UUID accountID, @NotNull String name, boolean player) {
		return iConomyUnlocked.getAccounts().create(accountID, name, player);
	}

	@Override
	public boolean createAccount(@NotNull UUID accountID, @NotNull String name, @NotNull String worldName, boolean player) {
		return createAccount(accountID, name, player);
	}

	@Override
	public Map<UUID, String> getUUIDNameMap() {
		return iConomyUnlocked.getAccounts().getUUIDNameMap();
	}

	@Override
	public Optional<String> getAccountName(UUID accountID) {
		if (iConomyUnlocked.getAccounts().exists(accountID)) {
			return Optional.of(Account.getAccount(accountID).getName());
		}
		return Optional.empty();
	}

	@Override
	public boolean hasAccount(UUID accountID) {
		return iConomyUnlocked.getAccounts().exists(accountID);
	}

	@Override
	public boolean hasAccount(UUID accountID, String worldName) {
		return hasAccount(accountID);
	}

	@Override
	public boolean renameAccount(UUID accountID, String name) {
		return iConomyUnlocked.getAccounts().get(accountID).setName(name);
	}

	@Override
	public boolean renameAccount(String plugin, UUID accountID, String name) {
		return renameAccount(accountID, name);
	}

	@Override
	public boolean deleteAccount(String plugin, UUID accountID) {
		iConomyUnlocked.getAccounts().get(accountID).remove();
		return true;
	}

	@Override
	public boolean accountSupportsCurrency(String plugin, UUID accountID, String currency) {
		return currency.equalsIgnoreCase(Settings.getCurrencyName());
	}

	@Override
	public boolean accountSupportsCurrency(String plugin, UUID accountID, String currency, String world) {
		return currency.equalsIgnoreCase(Settings.getCurrencyName());
	}

	@Override
	public @NotNull BigDecimal getBalance(String pluginName, UUID accountID) {
		@Nullable
		Account account = Account.getAccount(accountID);
		if (account == null)
			return BigDecimal.ZERO;
		return BigDecimal.valueOf(account.getHoldings().balance());
	}

	@Override
	public @NotNull BigDecimal getBalance(String pluginName, UUID accountID, String world) {
		return getBalance(pluginName, accountID);
	}

	@Override
	public @NotNull BigDecimal getBalance(String pluginName, UUID accountID, String world, String currency) {
		return getBalance(pluginName, accountID);
	}

	@Override
	public boolean has(String pluginName, UUID accountID, BigDecimal amount) {
		return getBalance(pluginName, accountID).compareTo(amount) != -1;
	}

	@Override
	public boolean has(String pluginName, UUID accountID, String worldName, BigDecimal amount) {
		return has(pluginName, accountID, amount);
	}

	@Override
	public boolean has(String pluginName, UUID accountID, String worldName, String currency, BigDecimal amount) {
		return has(pluginName, accountID, amount);
	}

	@Override
	public @NotNull EconomyResponse withdraw(String pluginName, UUID accountID, BigDecimal amount) {
		Account account = Account.getAccount(accountID);
		if (account == null)
			return new EconomyResponse(amount, BigDecimal.ZERO, ResponseType.FAILURE, "No account found.");

		if (!account.getHoldings().hasEnough(amount.doubleValue()))
			return new EconomyResponse(amount, getBalance(pluginName, accountID), ResponseType.FAILURE, "Not enough funds.");

		account.getHoldings().subtract(amount.doubleValue());
		iConomyUnlocked.getTransactions().insert(account.getName(), "[Vault]", 0.0D, account.getHoldings().balance(), 0.0D, 0.0D, amount.doubleValue());
		return new EconomyResponse(amount, getBalance(pluginName, accountID), ResponseType.SUCCESS, null);
	}

	@Override
	public @NotNull EconomyResponse withdraw(String pluginName, UUID accountID, String worldName, BigDecimal amount) {
		return withdraw(pluginName, accountID, amount);
	}

	@Override
	public @NotNull EconomyResponse withdraw(String pluginName, UUID accountID, String worldName, String currency, BigDecimal amount) {
		return withdraw(pluginName, accountID, amount);
	}

	@Override
	public @NotNull EconomyResponse deposit(String pluginName, UUID accountID, BigDecimal amount) {
		Account account = Account.getAccount(accountID);
		if (account == null)
			return new EconomyResponse(amount, BigDecimal.ZERO, ResponseType.FAILURE, "No account found.");

		account.getHoldings().add(amount.doubleValue());
		iConomyUnlocked.getTransactions().insert("[Vault]", account.getName(), 0.0D, account.getHoldings().balance(), 0.0D, amount.doubleValue(), 0.0D);
		return new EconomyResponse(amount, getBalance(pluginName, accountID), ResponseType.SUCCESS, null);
	}

	@Override
	public @NotNull EconomyResponse deposit(String pluginName, UUID accountID, String worldName, BigDecimal amount) {
		return deposit(pluginName, accountID, amount);
	}

	@Override
	public @NotNull EconomyResponse deposit(String pluginName, UUID accountID, String worldName, String currency, BigDecimal amount) {
		return deposit(pluginName, accountID, amount);
	}

	@Override
	public boolean createSharedAccount(String pluginName, UUID accountID, String name, UUID owner) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAccountOwner(String pluginName, UUID accountID, UUID uuid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setOwner(String pluginName, UUID accountID, UUID uuid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAccountMember(String pluginName, UUID accountID, UUID uuid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAccountMember(String pluginName, UUID accountID, UUID uuid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAccountMember(String pluginName, UUID accountID, UUID uuid,
			AccountPermission... initialPermissions) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAccountMember(String pluginName, UUID accountID, UUID uuid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasAccountPermission(String pluginName, UUID accountID, UUID uuid, AccountPermission permission) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateAccountPermission(String pluginName, UUID accountID, UUID uuid, AccountPermission permission,
			boolean value) {
		// TODO Auto-generated method stub
		return false;
	}

}
