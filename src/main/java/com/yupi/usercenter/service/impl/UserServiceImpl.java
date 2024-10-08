package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.yupi.usercenter.exception.BussinessException;
import com.yupi.usercenter.common.ErrorCode;
import com.yupi.usercenter.domain.User;
import com.yupi.usercenter.service.UserService;
import com.yupi.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yupi.usercenter.constant.UserConstant.User_LOGIN_STATE;

/**
* @author yangz
* @description 针对表【user】的数据库操作Service实现
* @createDate 2024-08-28 22:44:19
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{
    @Resource
    private UserMapper userMapper;

    private static final String SALT = "yupi";

    //充当键值对的key部分


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode) {
        if(StringUtils.isAllBlank(userAccount,userPassword,checkPassword,planetCode))
        {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        if(userAccount.length()<4)
        {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }
        if(userPassword.length()<8 || checkPassword.length()<8)
        {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }
        if(planetCode.length()>5)
        {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"星球编号过长");
        }

        /*
        * 校验特殊字符
        * */
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find())
        {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"账号存在不合法字符");
        }

        /*
        *
        * 密码和验证密码是否相同
        * */
        if(!userPassword.equals(checkPassword))
        {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"验证密码和原密码不同");
        }
        /*
        * 用户不能重复
        * */
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount",userAccount);
        Long aLong = userMapper.selectCount(userQueryWrapper);

        if(aLong>=1)
        {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"用户名重复");
        }
        /*
        * 对星球编号进行判重
        * */
        userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("planetCode",planetCode);
        aLong = userMapper.selectCount(userQueryWrapper);
        if(aLong>=1)
        {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"星球编号重复");
        }


        /*
        * 对密码进行加密
        * */

        String newPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(newPassword);
        user.setPlanetCode(planetCode);
        boolean save = this.save(user);
        if(!save)
        {
            throw new BussinessException(ErrorCode.NULL_ERROR,"注册失败");
        }


        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if(StringUtils.isAllBlank(userAccount,userPassword))
        {
            return null;
        }
        if(userAccount.length()<4)
        {
            return null;
        }
        if(userPassword.length()<8 )
        {
            return null;
        }

        /*
         * 校验特殊字符
         * */
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find())
        {
            return null;
        }


        /*
         * 验证密码是否输入正确
         * */
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        String newPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        userQueryWrapper.eq("userAccount",userAccount);
        userQueryWrapper.eq("userPassword",newPassword);
        User user = userMapper.selectOne(userQueryWrapper);//记住这里是selectOne,不是selectcount
        if(user==null)
        {
            log.info("userLogin failed,userAccount cannt mattcher userPassword");
            return null;
        }
        request.getSession().setAttribute(User_LOGIN_STATE,user);

        User safetyUser = getSafetyUser(user);
        return safetyUser;

    }
    @Override
    public User getSafetyUser(User originUser)
    {
        if(originUser==null)
        {
            /*
            * 在这里进行一个判断是因为,获取用户登录态的时候,如果没有拿到的话需要进行一个判断
            * 否者就会为空
            * */
            return null;
        }

        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUpdateTime(originUser.getUpdateTime());
        return safetyUser;

    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(User_LOGIN_STATE);
        return 1;
    }
}




