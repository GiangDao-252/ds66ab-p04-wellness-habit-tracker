package com.oop.project.db;

import java.sql.Connection;
import java.sql.SQLException;

public final class Transactions {

    private Transactions() {
    }

    @FunctionalInterface
    public interface Work<T> {

        T run(Connection connection) throws SQLException;
    }

    public static <T> T execute(Work<T> work)
            throws SQLException {

        try (Connection connection = ConnectionFactory.open()) {
            connection.setAutoCommit(false);

            try {
                T result = work.run(connection);

                connection.commit();

                return result;

            } catch (SQLException | RuntimeException | Error e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }
        }
    }
}
