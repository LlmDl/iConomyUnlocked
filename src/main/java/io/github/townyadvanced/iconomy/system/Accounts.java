package io.github.townyadvanced.iconomy.system;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.jetbrains.annotations.Nullable;

import io.github.townyadvanced.iconomy.iConomyUnlocked;
import io.github.townyadvanced.iconomy.settings.Settings;

public class Accounts {

	Logger log = iConomyUnlocked.getPlugin().getLogger();
	private String SQLTable = Settings.getDBTable();

	/**
	 * Check if an Account exists with this uuid.
	 * 
	 * @param uuid the UUID to check
	 * @return true if an Account exists.
	 */
    public boolean exists(UUID uuid) {
    	
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        boolean exists = false;
        try {
            conn = iConomyUnlocked.getBackEnd().getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + SQLTable + " WHERE uuid = ? LIMIT 1");
            ps.setString(1, uuid.toString());
            rs = ps.executeQuery();
            exists = rs.next();
        } catch (Exception ex) {
            exists = false;
        } finally {
        	iConomyUnlocked.getBackEnd().close(conn, ps, rs);
        }
        return exists;
    }
	
	/**
	 * Check if an Account exists with this name.
	 * 
	 * @param name the name to check
	 * @return true if an Account exists.
	 */
    public boolean exists(String name) {
    	
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        boolean exists = false;
        try {
            conn = iConomyUnlocked.getBackEnd().getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + SQLTable + " WHERE username = ? LIMIT 1");
            ps.setString(1, name);
            rs = ps.executeQuery();
            exists = rs.next();
        } catch (Exception ex) {
            exists = false;
        } finally {
        	iConomyUnlocked.getBackEnd().close(conn, ps, rs);
        }
        return exists;
    }

    /**
     * Create an Account.
     * 
     * @param uuid the Account uuid.
     * @param name the Account name.
     * @return true if successful.
     */
    public boolean create(UUID uuid, String name, boolean nonPlayer) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = iConomyUnlocked.getBackEnd().getConnection();
            ps = conn.prepareStatement("INSERT INTO " + SQLTable + "(uuid, username, balance, hidden, nonplayer) VALUES (?, ?, ?, 0, ?)");
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.setDouble(3, Settings.getDefaultBalance());
            ps.setBoolean(4, nonPlayer);
            ps.executeUpdate();
        } catch (Exception e) {
            return false;
        } finally {
        	iConomyUnlocked.getBackEnd().close(conn, ps);
        }
        return true;
    }

    /**
     * Imports an Account.
     * 
     * @param uuid the Account uuid.
     * @param name the Account name.
     * @param balance the balance.
     * @oaram hidden whether it is hidden.
     * @return true if successful.
     */
    public boolean importAccount(String uuidraw, String name, double balance, boolean hidden) {
		UUID uuid = UUID.fromString(uuidraw);
		if (uuid == null)
			return false;

		if (exists(uuid)) {
			Account account = Account.getAccount(uuid);
			account.setName(name);
			account.getHoldings().set(balance);
			account.setHidden(hidden);
			account.setNonPlayer(Settings.isNonPlayerAccountName(name));
			return true;
		}

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = iConomyUnlocked.getBackEnd().getConnection();
            ps = conn.prepareStatement("INSERT INTO " + SQLTable + "(uuid, username, balance, hidden, nonplayer) VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.setDouble(3, balance);
            ps.setBoolean(4, hidden);
            ps.setBoolean(5, Settings.isNonPlayerAccountName(name));
            ps.executeUpdate();
        } catch (Exception e) {
            return false;
        } finally {
        	iConomyUnlocked.getBackEnd().close(conn, ps);
        }
        return true;
    }

    
    /**
     * Remove the user Account with this uuid.
     * 
     * @param uuid the UUID of the account.
     * @return true if successful.
     */
    public boolean remove(UUID uuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = iConomyUnlocked.getBackEnd().getConnection();
            ps = conn.prepareStatement("DELETE FROM " + SQLTable + " WHERE uuid = ? LIMIT 1");
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (Exception e) {
            return false;
        } finally {
        	iConomyUnlocked.getBackEnd().close(conn, ps);
        }
        return true;
    }

    /**
     * Remove ALL matching Accounts with this uuid..
     * 
     * @param uuid the UUID of the account.
     * @return true if successful.
     */
    public boolean removeCompletely(String name) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = iConomyUnlocked.getBackEnd().getConnection();
            ps = conn.prepareStatement("DELETE FROM " + SQLTable + " WHERE uuid = ? LIMIT 1");
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (Exception e) {
            return false;
        } finally {
        	iConomyUnlocked.getBackEnd().close(conn, ps);
        }
        return true;
    }

    /**
     * Delete all accounts with default holdings
     * 
     * @return true if successful.
     */
    public boolean purge() {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = iConomyUnlocked.getBackEnd().getConnection();
            ps = conn.prepareStatement("DELETE FROM " + SQLTable + " WHERE balance = ?");
            ps.setDouble(1, Settings.getDefaultBalance());
            ps.executeUpdate();
        } catch (Exception e) {
            return false;
        } finally {
        	iConomyUnlocked.getBackEnd().close(conn, ps);
        }
        return true;
    }

    /**
     * Removes all accounts from the database.
     * ## Do not use this ##
     * 
     * @return true if successful.
     */
    public boolean emptyDatabase() {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = iConomyUnlocked.getBackEnd().getConnection();
            ps = conn.prepareStatement("TRUNCATE TABLE " + SQLTable);
            ps.executeUpdate();
        } catch (Exception e) {
            return false;
        } finally {
        	iConomyUnlocked.getBackEnd().close(conn, ps);
        }
        return true;
    }

    /**
     * Fetch a list of all Account balances.
     * 
     * @return a list of balances.
     */
    public List<Double> values() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<Double> Values = new ArrayList<Double>();
        try {
            conn = iConomyUnlocked.getBackEnd().getConnection();
            ps = conn.prepareStatement("SELECT balance FROM " + SQLTable);
            rs = ps.executeQuery();

            while (rs.next())
                Values.add(Double.valueOf(rs.getDouble("balance")));
            
        } catch (Exception e) {
            return null;
        } finally {
        	iConomyUnlocked.getBackEnd().close(conn, ps, rs);
        }
        return Values;
    }

    /**
     * Fetch X top non-hidden account names with balances.
     * 
     * @param amount the number of accounts to return.
     * @return a map of top accounts.
     */
    public LinkedHashMap<String, Double> ranking(int amount) {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        LinkedHashMap<String, Double> Ranking = new LinkedHashMap<String, Double>();
        try {
            conn = iConomyUnlocked.getBackEnd().getConnection();
            ps = conn.prepareStatement("SELECT username,balance FROM " + SQLTable + " WHERE hidden = 0 "
                    + (Settings.hideNonPlayerAccountsInRankings() ? "AND nonplayer = 0 " : "")
                    + "ORDER BY balance DESC LIMIT ?");
            ps.setInt(1, amount);
            rs = ps.executeQuery();

            while (rs.next())
                Ranking.put(rs.getString("username"), Double.valueOf(rs.getDouble("balance")));
        } catch (Exception e) {
            log.warning(e.getMessage());
            return null;
        } finally {
        	iConomyUnlocked.getBackEnd().close(conn, ps, rs);
        }
        return Ranking;
    }

    public Map<UUID, String> getUUIDNameMap() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        Map<UUID, String> map = new ConcurrentHashMap<>(); 
        try {
            conn = iConomyUnlocked.getBackEnd().getConnection();
            ps = conn.prepareStatement("SELECT uuid,username FROM " + SQLTable);
            rs = ps.executeQuery();

            while (rs.next())
                map.put(UUID.fromString(rs.getString("uuid")), rs.getString("username"));
        } catch (Exception e) {
            log.warning(e.getMessage());
            return null;
        } finally {
        	iConomyUnlocked.getBackEnd().close(conn, ps, rs);
        }
        return map;
    }
    
    /**
     * Get an Account by uuid and name.
     * Creates one if it doesn't exist.
     * 
     * @param uuid the uuid of the Account.
     * @param name the name of the Account.
     * @return an Account or null if unable.
     */
	public Account get(UUID uuid, String name) {
		return get(uuid, name, false);
	}

	/**
	 * Get an Account by uuid and name, with special awareness for players who have
	 * changed their name since their last log in. Creates one if it doesn't exist.
	 * 
	 * @param uuid            the uuid of the Account.
	 * @param name            the name of the Account.
	 * @param playerJoinEvent true when fired from a player join event.
	 * @return an Account or null if unable.
	 */
	public Account get(UUID uuid, String name, boolean playerJoinEvent) {
		if (exists(uuid)) {
			if (!playerJoinEvent) {
				return new Account(uuid, name);
			} else {
				Account account = Account.getAccount(uuid);
				String oldName = account.getName();
				if (!oldName.equals(name)) {
					account.setName(name);
					iConomyUnlocked.getPlugin().getLogger().info(
						String.format("iConomyUnlocked has found a player with UUID %s has changed their name from %s to %s.", uuid.toString(), oldName, name));
					iConomyUnlocked.getPlugin().getLogger().info("iConomyUnlocked's database will be altered to reflect this change.");
				}
				return account;
			}
        }
        if (!create(uuid, name, Settings.isNonPlayerAccountName(name))) {
            return null;
        }

        return new Account(uuid, name);
    }

    @Nullable
	public Account get(String name) {
		int id = 0;
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        boolean exists = false;
        try {
            conn = iConomyUnlocked.getBackEnd().getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + SQLTable + " WHERE username = ? LIMIT 1");
            ps.setString(1, name);
            rs = ps.executeQuery();
            exists = rs.next();
        	if (exists)
        		id = rs.getInt("id");
        } catch (Exception ex) {
            exists = false;
        } finally {
        	iConomyUnlocked.getBackEnd().close(conn, ps, rs);
        }
        if (exists) {
        	UUID uuid = getUUID(id);
        	if (uuid != null)
        		return get(uuid, name);
        }
        return null;
	}

    @Nullable
	public Account get(UUID uuid) {
		int id = 0;
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        boolean exists = false;
        try {
            conn = iConomyUnlocked.getBackEnd().getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + SQLTable + " WHERE uuid = ? LIMIT 1");
            ps.setString(1, uuid.toString());
            rs = ps.executeQuery();
            exists = rs.next();
        	if (exists)
        		id = rs.getInt("id");
        } catch (Exception ex) {
            exists = false;
        } finally {
        	iConomyUnlocked.getBackEnd().close(conn, ps, rs);
        }
        if (exists) {
        	String name = getName(id);
        	if (!name.isEmpty())
        		return get(uuid, name);
        }
        return null;
	}

	private UUID getUUID(int id) {

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		UUID uuid = null;
		try {
			conn = iConomyUnlocked.getBackEnd().getConnection();
			ps = conn.prepareStatement("SELECT * FROM " + SQLTable + " WHERE id = ? LIMIT 1");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next())
				uuid = UUID.fromString(rs.getString("uuid"));
		} catch (Exception ex) {
			return null;
		} finally {
			iConomyUnlocked.getBackEnd().close(conn, ps, rs);
		}
		return uuid;
	}

	private String getName(int id) {

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		String name = "";
		try {
			conn = iConomyUnlocked.getBackEnd().getConnection();
			ps = conn.prepareStatement("SELECT * FROM " + SQLTable + " WHERE id = ? LIMIT 1");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next())
				name = rs.getString("username");
		} catch (Exception ex) {
			return null;
		} finally {
			iConomyUnlocked.getBackEnd().close(conn, ps, rs);
		}
		return name;
	}

	public void updateAccountsForNewTables(List<String> tablesUpdated) {

		if (tablesUpdated.contains("nonplayer")) {
			// non player account status
			for (String name : new ArrayList<>(getUUIDNameMap().values())) {
				if (!Settings.isNonPlayerAccountName(name))
					continue;
				log.info("Account " + name + " is being marked as a non player account.");
				get(name).setNonPlayer(true);
			}
		}
	}
}
