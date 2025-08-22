package org.example;

import org.example.entity.User;
import org.example.plugin.PageParam;
import org.example.plugin.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yufengyang
 * @Package org.example
 * @date 2025/8/14 23:34
 * @school hnist
 */
@RestController
@RequestMapping("/users")
public class Controller {
    @Autowired
    private TestService testService;

    @GetMapping(value = "/find",params = "id")
    public void findUsers(@RequestParam("id") long id){
        List<User> users = testService.findusers(id);
        System.out.println(users);
    }

    @GetMapping(value = "/find",params = {"status","name"})
    public void findUsers(@RequestParam("status") int status,@RequestParam("name") String name){
                List<User> users = testService.findusersByname(status,name);
                System.out.println(users);

    }
    @PostMapping("/add")
    public  void addUsers(@RequestBody User user){
        testService.addUser(user);
    }

    @GetMapping("/page1")
    public PageResult<User> page1(@RequestParam int pageNum,@RequestParam int pagesize){
        PageParam pageParam = new PageParam(pageNum, pagesize);
        PageResult<User> pageResult = testService.page(pageParam);
        return pageResult;
    }
    @GetMapping("/page2")
    public PageResult<User> page2(@RequestParam int pageNum,@RequestParam int pagesize){
        PageParam pageParam = new PageParam(pageNum, pagesize);
        PageResult<User> pageResult = testService.page2(pageParam);
        return pageResult;
    }

}
