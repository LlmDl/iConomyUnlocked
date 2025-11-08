package io.github.townyadvanced.iconomy.providers;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import io.github.townyadvanced.iconomy.iConomyUnlocked;
import io.github.townyadvanced.iconomy.settings.Settings;
import io.github.townyadvanced.iconomy.system.Holdings;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

@SuppressWarnings("deprecation")
public class VaultEconomy implements Economy {
	private final iConomyUnlocked plugin;
	
	public VaultEconomy(iConomyUnlocked plugin) {
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
    public boolean createPlayerAccount(String playerName) {

        if (hasAccount(playerName)) {
            return false;
        }
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null)
        	return false;
        createPlayerAccount(player);
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {

        if (hasAccount(player)) {
            return false;
        }
        iConomyUnlocked.getAccounts().create(player.getUniqueId(), player.getName(), Settings.isNonPlayerAccountName(player.getName()));
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String arg1) {
        return createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String arg1) {
        return createPlayerAccount(player);
    }

    @Override
    public String currencyNamePlural() {

        try {
            return Settings.getCurrencyNamePlural();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String currencyNameSingular() {

        try {
            return Settings.getCurrencyNameSingular();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {

        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null)
        	return new EconomyResponse(amount, 0.0, EconomyResponse.ResponseType.FAILURE, "No player found with that name.");
    	
        return depositPlayer(player, amount);

    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        double balance;
        EconomyResponse.ResponseType type;
        String errorMessage = null;

        Holdings holdings = iConomyUnlocked.getAccounts().get(player.getUniqueId()).getHoldings();
        balance = holdings.balance() + amount;
        holdings.add(amount);
        type = EconomyResponse.ResponseType.SUCCESS;

        iConomyUnlocked.getTransactions().insert("[Vault]", player.getName(), 0.0D, balance, 0.0D, amount, 0.0D);

        return new EconomyResponse(amount, balance, type, errorMessage);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String world, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String world, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public String format(double amount) {

        return Settings.format(amount);
    }

    @Override
    public int fractionalDigits() {

        return Settings.getVaultFractionalDigits();
    }

    @Override
    public double getBalance(String playerName) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null)
        	return 0.0;
        return iConomyUnlocked.getAccounts().get(playerName).getHoldings().balance();
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return iConomyUnlocked.getAccounts().get(player.getUniqueId()).getHoldings().balance();
    }

    @Override
    public double getBalance(String playerName, String world) {

        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {

        return getBalance(player);
    }

    @Override
    public List<String> getBanks() {

        return new ArrayList<String>();
    }

    @Override
    public boolean has(String playerName, double amount) {

        return getBalance(playerName) >= amount;

    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {

        return getBalance(player) >= amount;

    }

    @Override
    public boolean has(String playerName, String world, double amount) {

        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, String world, double amount) {

        return getBalance(player) >= amount;
    }

    @Override
    public boolean hasAccount(String playerName) {

        return iConomyUnlocked.getAccounts().exists(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {

        return iConomyUnlocked.getAccounts().exists(player.getUniqueId());
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {

        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null)
        	return new EconomyResponse(amount, 0.0, EconomyResponse.ResponseType.FAILURE, "No player found with that name.");

        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        double balance;
        EconomyResponse.ResponseType type;
        String errorMessage = null;

        Holdings holdings = iConomyUnlocked.getAccounts().get(player.getUniqueId()).getHoldings();
        if (holdings.hasEnough(amount)) {
        	double previousBalance = holdings.balance();
            holdings.subtract(amount);
            balance = previousBalance - amount;
            type = EconomyResponse.ResponseType.SUCCESS;

//            iConomy.getTransactions().insert(playerName, "[Vault]", 0.0D, balance, 0.0D, 0.0D, amount);

            return new EconomyResponse(amount, balance, type, errorMessage);
        } else {
            amount = 0;
            balance = getBalance(player);
            type = EconomyResponse.ResponseType.FAILURE;
            errorMessage = "Insufficient funds";
            return new EconomyResponse(amount, balance, type, errorMessage);
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String world, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String world, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse bankBalance(String arg0) {

        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
    }

    @Override
    public EconomyResponse bankDeposit(String arg0, double arg1) {

        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
    }

    @Override
    public EconomyResponse bankHas(String arg0, double arg1) {

        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
    }

    @Override
    public EconomyResponse bankWithdraw(String arg0, double arg1) {

        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single account banks!");
    }

    @Override
    public EconomyResponse createBank(String arg0, OfflinePlayer arg1) {

        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single account banks!");
    }

    @Override
    public EconomyResponse isBankMember(String arg0, String arg1) {

        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
    }

    @Override
    public EconomyResponse isBankMember(String arg0, OfflinePlayer arg1) {

        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
    }

    @Override
    public EconomyResponse isBankOwner(String arg0, String arg1) {

        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
    }

    @Override
    public EconomyResponse isBankOwner(String arg0, OfflinePlayer arg1) {

        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
    }

    @Override
    public EconomyResponse deleteBank(String arg0) {

        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support bank accounts!");
    }}
