package jm.task.core.jdbc.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public final class Util implements AutoCloseable {
    private static final String URL = "jdbc:mysql://localhost:3306/testdb";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    private static final int POOL_SIZE = 1;
    private static volatile Util instance;
    private final BlockingQueue<ProxyConnection> freeConnections;
    private final Queue<ProxyConnection> givenAwayConnections;

    private Util() {
        freeConnections = new LinkedBlockingDeque<>(POOL_SIZE);
        givenAwayConnections = new ArrayDeque<>();
        for (int i = 0; i < POOL_SIZE; i++) {
            try {
                ProxyConnection proxyConnection = new ProxyConnection(DriverManager.getConnection(URL, USERNAME, PASSWORD));
                freeConnections.add(proxyConnection);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Util getInstance() {
        if (instance == null) {
            synchronized (Util.class) {
                if (instance == null) {
                    instance = new Util();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        ProxyConnection proxyConnection = null;
        try {
            proxyConnection = freeConnections.take();
            givenAwayConnections.offer(proxyConnection);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(e.getMessage());
        }
        return proxyConnection;
    }

    void releaseConnection(Connection connection) throws SQLException {
        if (connection instanceof ProxyConnection) {
            try {
                freeConnections.put((ProxyConnection) connection);
                givenAwayConnections.remove(connection);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println(e.getMessage());
                throw new SQLException(e);
            }
        } else {
            System.err.println("Connection is not proxy or null!");
        }
    }

    @Override
    public void close() throws SQLException {
        for (int i = 0; i < POOL_SIZE; i++) {
            try {
                freeConnections.take().reallyClose();
            } catch (SQLException e) {
                throw new SQLException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println(e.getMessage());
            }
        }
    }
}
