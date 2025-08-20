package org.example.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.entity.User;

import java.util.ArrayList;

/**
 * @author yufengyang
 * @Package org.example.mapper
 * @date 2025/8/14 22:59
 * @school hnist
 */
public interface TestMapper {

   public ArrayList<User> findByid(@Param("id") long id);

   public ArrayList<User> findBynamelike(@Param("name") String name);

   public ArrayList<User> findByname(@Param("name")String name);

   void addUser(User user);
}
