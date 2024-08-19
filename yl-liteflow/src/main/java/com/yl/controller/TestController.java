package com.yl.controller;


import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * <p> 测试api </p>
 *
 * @author zhengqingya
 * @description
 * @date 2021/10/5 2:36 下午
 */
@RestController
public class TestController {

    @Resource
    private FlowExecutor flowExecutor;

    @GetMapping("testLiteFlow")
    public Object testLiteFlow() {
        LiteflowResponse response = this.flowExecutor.execute2Resp("chain1", "arg");
        return response;
    }


}
