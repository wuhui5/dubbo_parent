package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.dao.UserDao;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;


@Service(loadbalance = "roundrobin")
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Override
    public User findById(Integer id) {
        System.out.println("222222222222222222222222");
        /*try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        return userDao.findById(id);
    }
}
