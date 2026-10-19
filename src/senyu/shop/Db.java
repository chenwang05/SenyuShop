package senyu.shop;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public class Db {
    private final SenyuShopPlugin plugin;
    private Connection conn;

    public Db(SenyuShopPlugin senyuShopPlugin) {
        this.plugin = senyuShopPlugin;
    }

    public synchronized void open() throws Exception {
        Class.forName("org.sqlite.JDBC");
        File file = this.plugin.getDataFolder();
        if (!file.exists() && !file.mkdirs()) {
            throw new IllegalStateException("无法创建数据目录");
        }
        this.conn = DriverManager.getConnection("jdbc:sqlite:" + new File(file, "data.db").getAbsolutePath());
        try (Statement statement = this.conn.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute("CREATE TABLE IF NOT EXISTS shop_items(id INTEGER PRIMARY KEY AUTOINCREMENT,item BLOB NOT NULL,buy_price REAL NOT NULL DEFAULT 0,sell_price REAL NOT NULL DEFAULT 0,sort INTEGER NOT NULL DEFAULT 0,category TEXT NOT NULL DEFAULT '其他')");
            statement.execute("CREATE TABLE IF NOT EXISTS listings(id INTEGER PRIMARY KEY AUTOINCREMENT,seller VARCHAR(36) NOT NULL,seller_name VARCHAR(32) NOT NULL,item BLOB NOT NULL,price REAL NOT NULL,status VARCHAR(16) NOT NULL DEFAULT 'open',buyer VARCHAR(36),buyer_name VARCHAR(32),created INTEGER NOT NULL,expire INTEGER NOT NULL)");
            try {
                statement.execute("ALTER TABLE shop_items ADD COLUMN category TEXT NOT NULL DEFAULT '其他'");
            }
            catch (SQLException ignored) { }
        }
    }

    public synchronized List<ShopItem> listShopItems() throws SQLException {
        ArrayList<ShopItem> arrayList = new ArrayList<ShopItem>();
        try (PreparedStatement preparedStatement = this.conn.prepareStatement("SELECT id,item,buy_price,sell_price,sort,category FROM shop_items ORDER BY sort,id");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                arrayList.add(new ShopItem(resultSet.getLong(1), ItemStack.deserializeBytes((byte[])resultSet.getBytes(2)), resultSet.getDouble(3), resultSet.getDouble(4), resultSet.getInt(5), resultSet.getString(6)));
            }
        }
        return arrayList;
    }

    public synchronized void addShopItem(ItemStack itemStack, double d, double d2, String string) throws SQLException {
        try (PreparedStatement preparedStatement = this.conn.prepareStatement("INSERT INTO shop_items(item,buy_price,sell_price,category) VALUES(?,?,?,?)")) {
            preparedStatement.setBytes(1, itemStack.serializeAsBytes());
            preparedStatement.setDouble(2, d);
            preparedStatement.setDouble(3, d2);
            preparedStatement.setString(4, string);
            preparedStatement.executeUpdate();
        }
    }

    public synchronized boolean setCategory(long l, String string) throws SQLException {
        try (PreparedStatement preparedStatement = this.conn.prepareStatement("UPDATE shop_items SET category=? WHERE id=?")) {
            preparedStatement.setString(1, string);
            preparedStatement.setLong(2, l);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public synchronized void setPrice(long l, Double d, Double d2) throws SQLException {
        if (d != null) {
            try (PreparedStatement preparedStatement = this.conn.prepareStatement("UPDATE shop_items SET buy_price=? WHERE id=?")) {
                preparedStatement.setDouble(1, d);
                preparedStatement.setLong(2, l);
                preparedStatement.executeUpdate();
            }
        }
        if (d2 != null) {
            try (PreparedStatement preparedStatement = this.conn.prepareStatement("UPDATE shop_items SET sell_price=? WHERE id=?")) {
                preparedStatement.setDouble(1, d2);
                preparedStatement.setLong(2, l);
                preparedStatement.executeUpdate();
            }
        }
    }

    public synchronized boolean removeShopItem(long l) throws SQLException {
        try (PreparedStatement preparedStatement = this.conn.prepareStatement("DELETE FROM shop_items WHERE id=?")) {
            preparedStatement.setLong(1, l);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public synchronized int deleteByCategory(String string) throws SQLException {
        try (PreparedStatement preparedStatement = this.conn.prepareStatement("DELETE FROM shop_items WHERE category=?")) {
            preparedStatement.setString(1, string);
            return preparedStatement.executeUpdate();
        }
    }

    public synchronized void addListing(String string, String string2, ItemStack itemStack, double d, long l, long l2) throws SQLException {
        try (PreparedStatement preparedStatement = this.conn.prepareStatement("INSERT INTO listings(seller,seller_name,item,price,status,created,expire) VALUES(?,?,?,?,'open',?,?)")) {
            preparedStatement.setString(1, string);
            preparedStatement.setString(2, string2);
            preparedStatement.setBytes(3, itemStack.serializeAsBytes());
            preparedStatement.setDouble(4, d);
            preparedStatement.setLong(5, l);
            preparedStatement.setLong(6, l2);
            preparedStatement.executeUpdate();
        }
    }

    public synchronized List<Listing> openListings(int n, int n2) throws SQLException {
        ArrayList<Listing> arrayList = new ArrayList<Listing>();
        try (PreparedStatement preparedStatement = this.conn.prepareStatement("SELECT id,seller,seller_name,item,price,status,buyer,buyer_name,created,expire FROM listings WHERE status='open' ORDER BY created DESC LIMIT ? OFFSET ?")) {
            preparedStatement.setInt(1, n);
            preparedStatement.setInt(2, n2);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    arrayList.add(this.row(resultSet));
                }
            }
        }
        return arrayList;
    }

    public synchronized List<Listing> myListings(String string) throws SQLException {
        ArrayList<Listing> arrayList = new ArrayList<Listing>();
        try (PreparedStatement preparedStatement = this.conn.prepareStatement("SELECT id,seller,seller_name,item,price,status,buyer,buyer_name,created,expire FROM listings WHERE seller=? AND status IN ('open','sold') ORDER BY created DESC LIMIT 45")) {
            preparedStatement.setString(1, string);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    arrayList.add(this.row(resultSet));
                }
            }
        }
        return arrayList;
    }

    public synchronized int myOpenCount(String string) throws SQLException {
        try (PreparedStatement preparedStatement = this.conn.prepareStatement("SELECT COUNT(*) FROM listings WHERE seller=? AND status='open'")) {
            preparedStatement.setString(1, string);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    public synchronized Listing getListing(long l) throws SQLException {
        try (PreparedStatement preparedStatement = this.conn.prepareStatement("SELECT id,seller,seller_name,item,price,status,buyer,buyer_name,created,expire FROM listings WHERE id=?");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            preparedStatement.setLong(1, l);
            if (resultSet.next()) {
                return this.row(resultSet);
            }
            return null;
        }
    }

    public synchronized boolean markSold(long l, String string, String string2) throws SQLException {
        try (PreparedStatement preparedStatement = this.conn.prepareStatement("UPDATE listings SET status='sold',buyer=?,buyer_name=? WHERE id=? AND status='open'")) {
            preparedStatement.setString(1, string);
            preparedStatement.setString(2, string2);
            preparedStatement.setLong(3, l);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public synchronized boolean cancelListing(long l, String string) throws SQLException {
        try (PreparedStatement preparedStatement = this.conn.prepareStatement("UPDATE listings SET status='cancelled' WHERE id=? AND seller=? AND status='open'")) {
            preparedStatement.setLong(1, l);
            preparedStatement.setString(2, string);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public synchronized int sweepExpired() throws SQLException {
        try (Statement statement = this.conn.createStatement()) {
            return statement.executeUpdate("UPDATE listings SET status='expired' WHERE status='open' AND expire<" + System.currentTimeMillis());
        }
    }

    private Listing row(ResultSet resultSet) throws SQLException {
        try {
            return new Listing(resultSet.getLong(1), resultSet.getString(2), resultSet.getString(3), ItemStack.deserializeBytes((byte[])resultSet.getBytes(4)), resultSet.getDouble(5), resultSet.getString(6), resultSet.getString(7), resultSet.getString(8), resultSet.getLong(9), resultSet.getLong(10));
        }
        catch (Exception exception) {
            throw new SQLException("物品反序列化失败(可能版本变更): id=" + resultSet.getLong(1), exception);
        }
    }

    public synchronized void close() {
        try {
            if (this.conn != null) {
                this.conn.close();
            }
        }
        catch (Exception ignored) { }
    }

    public record Listing(long id, String seller, String sellerName, ItemStack item, double price, String status, String buyer, String buyerName, long created, long expire) {
    }

    public record ShopItem(long id, ItemStack item, double buy, double sell, int sort, String category) {
    }
}
