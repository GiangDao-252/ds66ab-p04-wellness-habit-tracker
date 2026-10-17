package com.oop.project.db;

import java.sql.Connection; // Import the Connection interface from the java.sql package
import java.sql.DriverManager; // Import the DriverManager class from the java.sql package
import java.sql.SQLException; // Import the SQLException class from the java.sql package

//final: không cho các lớp khác kế thừa bằng extends ConnectionFactory
public final class ConnectionFactory {

    private ConnectionFactory() {
        // Prevent creating instances of this utility class.
    }

    public static Connection open() throws SQLException {
        String url = requireEnvironmentVariable("WELLNESS_DB_URL");
        String user = requireEnvironmentVariable("WELLNESS_DB_USER");
        String password = requireEnvironmentVariable("WELLNESS_DB_PASSWORD");

        return DriverManager.getConnection(url, user, password);
    }

    //hàm khai báo hàm đọc cấu hình bắt buộc
    private static String requireEnvironmentVariable(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            //mém ngoại lệ khi thiếu cấu hình: throw ném ngoại lệ đó ra ngoài, dừng luồng thực hiện bình thường của hàm
            throw new IllegalStateException(
                    "Missing environment variable: " + name
            );
        }

        return value;
    }
}
