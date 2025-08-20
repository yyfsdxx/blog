package org.example;

import org.example.entity.User;
import org.example.mapper.TestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yufengyang
 * @Package org.example
 * @date 2025/8/14 23:36
 * @school hnist
 */
@Service
public class TestService {
    @Autowired
    private TestMapper testMapper;
    public List<User> findusers(long id) {
        ArrayList<User> list = new ArrayList<>();
        list = testMapper.findByid(id);
        return list;
    }

    public List<User> findusersByname(int status, String name) {
        if(status == 1){
            List<User> list = testMapper.findBynamelike(name);
            return list;
        }
        ArrayList<User> list = testMapper.findByname(name);
        return list;
    }

    public void addUser(User user) {
        testMapper.addUser(user);
    }
}
