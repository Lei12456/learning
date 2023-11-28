package com.yl.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yl.entity.User;
import com.yl.mapper.UserMapper;
import com.yl.service.UserService;
import org.springframework.stereotype.Service;

/**
* @author leishen
* @description 针对表【tb_user】的数据库操作Service实现
* @createDate 2023-11-24 16:51:24
*/
@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

}




