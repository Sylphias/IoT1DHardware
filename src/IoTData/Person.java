package IoTData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ilya on 10/11/16.
 */
public class Person {
    private int id, age;
    private String name, ip_addr;
    private char gender;

    public Person(int id, int age, String name, char gender,String ip_addr) {
        this.id = id;
        this.age = age;
        this.name = name;
        this.gender = gender;
        this.ip_addr = ip_addr;
    }

    public Person( int age, String name, char gender, String ip_addr) {
        this.age = age;
        this.name = name;
        this.gender = gender;
        this.ip_addr = ip_addr;
    }


    public Person() {
    }

    public Person(String jsonString){
        Gson gson = new Gson();
        HashMap<String,String> personData = new HashMap<String, String>();
        Type type = new TypeToken<HashMap<String, String>>() {}.getType();
        personData = gson.fromJson(jsonString,type);
        this.name = personData.get("name");
        this.age = Integer.parseInt(personData.get("age"));
        this.gender = personData.get("gender").charAt(0);
        this.ip_addr = personData.get("ip_addr");
        this.id = Integer.parseInt(personData.get("id"));
    }

    public String getIp_addr() {
        return ip_addr;
    }

    public void setIp_addr(String ip_addr) {
        this.ip_addr = ip_addr;
    }

    public int getId() {
        return id;
    }

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    public char getGender() {
        return gender;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGender(char gender) {
        this.gender = gender;
    }

    public HashMap<String,String> toHashMap(){
        HashMap<String,String> personHash = new HashMap<String, String>();
        personHash.put("id",Integer.toString(this.id));
        personHash.put("name",this.name);
        personHash.put("age",Integer.toString(this.age));
        personHash.put("ip_addr",this.ip_addr);
        personHash.put("gender",Character.toString(this.gender));
        return personHash;
    }

}
