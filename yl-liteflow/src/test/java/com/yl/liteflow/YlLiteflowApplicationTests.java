package com.yl.liteflow;

import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class YlLiteflowApplicationTests {


	@Autowired
	private FlowExecutor flowExecutor;

	@Test
	public void testConfig(){
		LiteflowResponse response = flowExecutor.execute2Resp("chain1", "arg");
	}

}
