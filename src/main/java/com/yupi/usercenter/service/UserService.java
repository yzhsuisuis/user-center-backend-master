package com.yupi.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.usercenter.domain.User;


import javax.servlet.http.HttpServletRequest;

/**
* @author yangz
* @description 针对表【user】的数据库操作Service
* @createDate 2024-08-28 22:44:19
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    long userRegister(String userAccount,String userPassword,String checkPassword,String planetCode);

    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getSafetyUser(User orignUser);

    int userLogout(HttpServletRequest request);
}
