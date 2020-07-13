package github.mjksabit.sabit.android.utils;

import java.io.IOException;

import github.mjksabit.sabit.core.Connection;

public class SConnection {

    private static Connection connection;

    public static void setConnection(Connection connection) throws IllegalStateException{
        if (SConnection.connection != null)
            throw new IllegalStateException("Connection Already Set");

        SConnection.connection = connection;
    }

    public static Connection getConnection() throws NullPointerException {
        if (connection==null)
            throw new NullPointerException("Connection not Set");
        return connection;
    }

    public static void endConnection() {
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection = null;
    }
}
