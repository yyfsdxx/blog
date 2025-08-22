package org.example;

import org.example.entity.User;
import org.example.mapper.TestMapper;
import org.example.plugin.PageParam;
import org.example.plugin.PageResult;
import org.example.plugin.PaginationContext;
import org.example.plugin2.Page;
import org.example.plugin2.PageHelper;
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
    private PageResult<User> pageResult;
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

    public PageResult<User> page(PageParam pageParam) {
        PaginationContext.setPageHolder(pageParam);
        ArrayList<User> allPage = testMapper.findAllPage();
        long total = PaginationContext.getTotalHolder();
        PaginationContext.clearAll();
        return new PageResult<User>(total,allPage);
    }

    public PageResult<User> page2(PageParam pageParam) {
        PageHelper.startPage(pageParam);
        Page<User> page = testMapper.findAllPage2();
        System.out.println(page);
        PageResult<User> pageResult = new PageResult<>(page.getTotal(),page.getResult());
        return pageResult;
    }
}
