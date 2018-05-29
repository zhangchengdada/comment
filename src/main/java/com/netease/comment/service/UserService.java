package com.netease.comment.service;

import com.netease.comment.mapper.UserDao;
import com.netease.comment.model.UserDomain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by wb.zhangcheng
 */
@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    public int addUser(UserDomain user) {

        return userDao.insert(user);
    }


    public List<UserDomain> findAllUser(int pageNum, int pageSize) {

        return userDao.selectUsers();
    }
}