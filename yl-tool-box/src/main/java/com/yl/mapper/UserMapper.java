package com.yl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yl.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @author leishen
* @description 针对表【tb_user】的数据库操作Mapper
* @createDate 2023-11-24 16:51:24
* @Entity generator.entity.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




