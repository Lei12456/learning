package com.yl.redis.controller;


import com.yl.redis.lock.SeckillService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Resource
    private SeckillService seckillService;

    @PostMapping("/test")
    public String seckill(){
        boolean seckill = seckillService.seckill(1, 1);
        return "";
    }

}
