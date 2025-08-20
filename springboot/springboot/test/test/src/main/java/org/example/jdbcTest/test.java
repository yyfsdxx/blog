package org.example.jdbcTest;

import org.example.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yufengyang
 * @Package org.example.jdbcTest
 * @date 2025/8/14 19:29
 * @school hnist
 */
public class test {
    static final String URL = "jdbc:mysql://localhost:3306/test2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Tokyo";
    static final String USER = "root";
    static final String PWD  = "root";
    public static void main(String[] args) throws Exception {
        List<User> users = findUsersByNameLike("a");
        users.forEach(System.out::println);

        // 演示插入（带事务）
        long newId = insertUser("Davidsm", 29);
        System.out.println("Inserted id = " + newId);
    }

    private static long insertUser(String name, int age)throws Exception {
        String sql = "INSERT INTO t_user(name, age) VALUES(?, ?)";
        Connection connection = DriverManager.getConnection(URL, USER, PWD);
        PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setString(1,name);
        preparedStatement.setInt(2,age);
        preparedStatement.executeUpdate();
        ResultSet keys = preparedStatement.getGeneratedKeys();
        if(keys.next()){
            long id = 0;
            id = keys.getLong(1);
            return id;
        }
        return 0;
    }

    private static List<User> findUsersByNameLike(String keyword) throws Exception{
        String sql = "SELECT id, name, age FROM t_user WHERE name LIKE CONCAT('%', ?, '%') ORDER BY id DESC";
        List<User> list = new ArrayList<>();

        Connection connection = DriverManager.getConnection(URL, USER, PWD);
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1,keyword);
        ResultSet set = preparedStatement.executeQuery();

        while (set.next()){
            User user = new User();
            user.setId(set.getLong("id"));
            user.setName(set.getString("name"));
            user.setAge(set.getInt("age"));
            list.add(user);
        }
        return list;

    }
}
