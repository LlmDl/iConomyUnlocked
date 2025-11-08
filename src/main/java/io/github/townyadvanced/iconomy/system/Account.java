package io.github.townyadvanced.iconomy.system;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import io.github.townyadvanced.iconomy.iConomyUnlocked;
import io.github.townyadvanced.iconomy.events.AccountRemoveEvent;
import io.github.townyadvanced.iconomy.settings.Settings;

public class Account {
	private UUID uuid;
	private String name;
	private String SQLTable = Settings.getDBTable();
	Logger log = iConomyUnlocked.getPlugin().getLogger();

	public Account(UUID uuid, String name) {
		this.uuid = uuid;
		this.name = name;
	}

	public static Account getAccountOrThrow(UUID uuid) throws Exception {
		Account account = iConomyUnlocked.getAccounts().get(uuid);
		if (account == null)
			throw new Exception(String.format("No account found using the UUID %s", uuid));
		return account;
	}

	@Nullable
	public static Account getAccount(UUID uuid) {
		return iConomyUnlocked.getAccounts().get(uuid);
	}

	public static Account getAccountOrThrow(String name) throws Exception {
		Account account = iConomyUnlocked.getAccounts().get(name);
		if (account == null)
			throw new Exception(String.format("No account found using the name %s", name));
		return account;
	}

	@Nullable
	public static Account getAccount(String name) {
		return iConomyUnlocked.getAccounts().get(name);
	}

	/**
	 * Get the id of this Account.
	 * 
	 * @return id
	 */
	public int getId() {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		int id = -1;
		try {
			conn = iConomyUnlocked.getBackEnd().getConnection();
			ps = conn.prepareStatement("SELECT * FROM " + SQLTable + " WHERE username = ? LIMIT 1");
			ps.setString(1, this.name);
			rs = ps.executeQuery();

			if (rs.next())
				id = rs.getInt("id");
		} catch (Exception ex) {
			id = -1;
		} finally {
			iConomyUnlocked.getBackEnd().close(conn, ps, rs);
		}
		return id;
	}

	/**
	 * Get this Account name.
	 * 
	 * @return the name of this Account.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Get this Account UUID.
	 * 
	 * @return the UUID of this Account.
	 */
	public UUID getUUID() {
		return this.uuid;
	}
	/**
	 * Get the Holdings of this Account.
	 * 
	 * @return
	 */
	public Holdings getHoldings() {
		return new Holdings(this.uuid, this.name);
	}

	/**
	 * Get the Hidden state of this Account.
	 * 
	 * @return true if hidden.
	 */
	public boolean isHidden() {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			conn = iConomyUnlocked.getBackEnd().getConnection();
			ps = conn.prepareStatement("SELECT hidden FROM " + SQLTable + " WHERE username = ? LIMIT 1");
			ps.setString(1, this.name);
			rs = ps.executeQuery();

			if (rs != null && rs.next()) {
				boolean bool = rs.getBoolean("hidden");
				return bool;
			}
		} catch (Exception ex) {
			log.warning("Failed to check status: " + ex);
		} finally {
			iConomyUnlocked.getBackEnd().close(conn, ps, rs);
		}
		return false;
	}

	/**
	 * Set the Hidden flag on this account.
	 * 
	 * @param hidden the hidden state to set.
	 * @return true if successful
	 */
	public boolean setHidden(boolean hidden) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = iConomyUnlocked.getBackEnd().getConnection();

			ps = conn.prepareStatement("UPDATE " + SQLTable + " SET hidden = ? WHERE username = ?");
			ps.setBoolean(1, hidden);
			ps.setString(2, this.name);

			ps.executeUpdate();
		} catch (Exception ex) {
			log.warning("Failed to update status: " + ex);
			return false;
		} finally {
			iConomyUnlocked.getBackEnd().close(conn, ps);
		}
		return true;
	}

	/**
	 * Returns the ranking number of an account
	 *
	 * @param name
	 * @return Integer
	 */
	public int getRank() {
		int i = 1;

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			conn = iConomyUnlocked.getBackEnd().getConnection();
			ps = conn.prepareStatement("SELECT * FROM " + SQLTable + " WHERE hidden = 0 "
					+ (Settings.hideNonPlayerAccountsInRankings() ? "AND nonplayer = 0 " : "")
					+ "ORDER BY balance DESC");
			rs = ps.executeQuery();

			while (rs.next()) {
				if (rs.getString("username").equalsIgnoreCase(this.name)) {
					return i;
				}
				i++;
			}
		} catch (Exception ex) {
		} finally {
			iConomyUnlocked.getBackEnd().close(conn, ps, rs);
		}

		return -1;
	}

	/**
	 * Remove this account.
	 */
	public void remove() {

		AccountRemoveEvent event = new AccountRemoveEvent(this.name);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = iConomyUnlocked.getBackEnd().getConnection();
			ps = conn.prepareStatement("DELETE FROM " + SQLTable + " WHERE uuid = ?");
			ps.setString(1, uuid.toString());
			ps.executeUpdate();
		} catch (Exception ex) {
			log.warning("Failed to remove account: " + ex);
		} finally {
			iConomyUnlocked.getBackEnd().close(conn, ps);
		}
	}

	public boolean setName(String name) {
		this.name = name;
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = iConomyUnlocked.getBackEnd().getConnection();

			ps = conn.prepareStatement("UPDATE " + SQLTable + " SET username = ? WHERE uuid = ?");
			ps.setString(1, this.name);
			ps.setString(2, this.uuid.toString());

			ps.executeUpdate();
		} catch (Exception ex) {
			log.warning("Failed to update status: " + ex);
			return false;
		} finally {
			iConomyUnlocked.getBackEnd().close(conn, ps);
		}
		return true;
	}

	public boolean isNonPlayer() {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			conn = iConomyUnlocked.getBackEnd().getConnection();
			ps = conn.prepareStatement("SELECT nonplayer FROM " + SQLTable + " WHERE username = ? LIMIT 1");
			ps.setString(1, this.name);
			rs = ps.executeQuery();

			if (rs != null && rs.next()) {
				boolean bool = rs.getBoolean("nonplayer");
				return bool;
			}
		} catch (Exception ex) {
			log.warning("Failed to check status: " + ex);
		} finally {
			iConomyUnlocked.getBackEnd().close(conn, ps, rs);
		}
		return false;
	}

	public boolean setNonPlayer(boolean b) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = iConomyUnlocked.getBackEnd().getConnection();

			ps = conn.prepareStatement("UPDATE " + SQLTable + " SET nonplayer = ? WHERE uuid = ?");
			ps.setBoolean(1, b);
			ps.setString(2, this.uuid.toString());

			ps.executeUpdate();
		} catch (Exception ex) {
			log.warning("Failed to update status: " + ex);
			return false;
		} finally {
			iConomyUnlocked.getBackEnd().close(conn, ps);
		}
		return true;
	}
}
