package zy.blue7.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import zy.blue7.json.enity.JavaObj;
import zy.blue7.json.helper.Handler;

import java.util.List;

@SpringBootTest
class JsonApplicationTests {
    @Autowired
    Environment environment;

    static String strArr="";
    {
        strArr="[\n" +
                "    {\n" +
                "        \"Name\": \"zhangsan\",\n" +
                "        \"Age\": 19,\n" +
                "        \"Date\": \"2016-06-06 16:24:50\",\n" +
                "        \"Price\": 12.23,\n" +
                "        \"Hobby\": [\n" +
                "            [\n" +
                "                \"abd\",\n" +
                "                \"asd\"\n" +
                "            ],\n" +
                "            [\n" +
                "                \"abd\",\n" +
                "                \"asd\"\n" +
                "            ]\n" +
                "        ],\n" +
                "        \"Order\": [\n" +
                "            [\n" +
                "                [\n" +
                "                    \"abd\",\n" +
                "                    \"asd\"\n" +
                "                ]\n" +
                "            ]\n" +
                "        ],\n" +
                "        \"Student\": {\n" +
                "            \"Name\": \"qwe\"\n" +
                "        },\n" +
                "        \"Teachers\": [\n" +
                "            {\n" +
                "                \"Age\": 123\n" +
                "            },\n" +
                "            {\n" +
                "                \"Age\": 123\n" +
                "            }\n" +
                "        ],\n" +
                "        \"Users\": [\n" +
                "            [\n" +
                "                {\n" +
                "                    \"Name\": \"qwe\"\n" +
                "                }\n" +
                "            ]\n" +
                "        ],\n" +
                "        \"Password\": [\n" +
                "            [\n" +
                "                [\n" +
                "                    {\n" +
                "                        \"Name\": \"qwe\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"Name\": \"qwe\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            ]\n" +
                "        ],\n" +
                "        \"flag\":true\n" +
                "        ,\n" +
                "        \"float\":12.2,\n" +
                "        \"byte\":12,\n" +
                "        \"short\":143,\n" +
                "        \"long\":23123312\n" +
                "\n" +
                "    }\n" +
                "]";
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testJsonModule() throws Exception {
        Handler handler1=new Handler<JavaObj>();
        handler1.setEnvironment(environment);
        List<JavaObj> objs = (List<JavaObj>) handler1.handler(JavaObj.class, strArr, "javaObj");

        System.out.println(objs);
    }

}
