package zy.blue7.json.enity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author blue7
 * @date 2020/8/17 9:44
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JavaObj {
    private boolean flag;
    private String name;
    private Integer age;
    private Date date;
    private Double price;
    private List<List<String>> hobby;
    private  Student student;
    private List<Teachers> teachers;
    private List<List<Users>> users;
    private List<List<List<String>>> order;
    private List<List<List<Password>>> password;
    private float float1;
    private byte byte1;
    private short short1;
    private long long1;
}
