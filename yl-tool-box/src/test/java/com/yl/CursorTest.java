package com.yl;

import com.yl.entity.CursorPageBaseReq;
import com.yl.entity.CursorPageBaseResp;
import com.yl.entity.User;
import com.yl.utils.CursorSimpleUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName com.yl.CursorTest.java
 * @createTime 2023年11月21日 15:11:00
 */
@SpringBootTest()
@RunWith(SpringRunner.class)
public class CursorTest {

    @Resource
    private UserService userService;
    @Resource
    private UserMapper userMapper;
    @Test
    public void test1(){
        CursorPageBaseReq cursorPageBaseReq = new CursorPageBaseReq();
        cursorPageBaseReq.setCursor("2");
        cursorPageBaseReq.setPageSize(2);
        CursorPageBaseResp<User> resp = CursorSimpleUtils.getCursorPageByMysql(userService, cursorPageBaseReq, wrapper -> {
            wrapper.eq(User::getUserId, "1");
        }, User::getId);
    }
}
