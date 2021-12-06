package com.rxf113.cusmethodargresolver;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author rxf113
 */
@RequestMapping("/test")
@Controller
public class DemoController {


    /**
     * 测试方法
     *
     * @return true/false
     */
    @PostMapping("/rxf113")
    @ResponseBody
    public Object setCurrentTime(@Rxf113(value = "name") List<String> name, @Rxf113(value = "age") Integer age) {
        System.out.println("name: " + name + "  " + "age: " + age);
        return "success!";
    }
}
