package com.yl;

import com.aliyuncs.exceptions.ClientException;
import com.yl.tongyitingwu.TongYiTingWuFiletransTaskApi;
import jakarta.annotation.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = {ThirdPartyApiApplication.class})
@RunWith(SpringRunner.class)
public class TongYiTingWuTest {

    @Resource
    private TongYiTingWuFiletransTaskApi tongYiTingWuFiletransTaskApi;


    @Test
    public void submitFiletransTaskTest() {
        try {
            tongYiTingWuFiletransTaskApi.summitTask();
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
        System.out.println("test");
    }

    @Test
    public void getTaskInfo() {
        try {
            tongYiTingWuFiletransTaskApi.getTaskInfo("519aa06d607f42e8ada443bf4e481ffa");
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
        System.out.println("test");
    }
}
