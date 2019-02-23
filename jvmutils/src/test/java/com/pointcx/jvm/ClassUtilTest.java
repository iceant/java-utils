package com.pointcx.jvm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class ClassUtilTest {


    static class User{
        static final Date NOW = new Date(System.currentTimeMillis());
        static String staticField;
        public String name;
        private int age;
        protected String email;

        public User() {

        }

        static User newUser(String name, int age, String email){
            return new User(name, age, email);
        }


        public User(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        @Override
        public String toString() {
            return "User{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", email='" + email + '\'' +
                    '}';
        }
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void invoke() {
//        System.currentTimeMillis();
        long currentTimeMillis = ClassUtil.invokeStaticMethod(System.class, "currentTimeMillis");
        System.out.println(currentTimeMillis);

        User user = ClassUtil.invokeStaticMethod(User.class, "newUser", new Class[]{String.class, int.class, String.class}, "CHEN PENG", 12, "NIHAO");
        System.out.println(user);
    }

    @Test
    public void invoke1() {
    }

    @Test
    public void newInstance() {
    }

    @Test
    public void newInstance1() {
    }

    @Test
    public void listClassPath() {
    }

    @Test
    public void getFieldByFieldName() {
    }

    @Test
    public void getFieldValue() {
        Date now = ClassUtil.getStaticFieldValue(User.class, "NOW");
        System.out.println(now);

        ClassUtil.setStaticField(User.class, "staticField", "CHEN PENG");
        String staticField = ClassUtil.getStaticFieldValue(User.class, "staticField");
        System.out.println(staticField);
    }

    @Test
    public void setFieldValue() {
        User user = ClassUtil.newInstance(User.class);
        ClassUtil.setFieldValue(user, "name", "Chen Peng");
        ClassUtil.setFieldValue(user, "age", 12);
        ClassUtil.setFieldValue(user, "email", "pizer.chen@gmail.com");
        System.out.println(user);

        User user2 = ClassUtil.newInstance(User.class, new Class[]{String.class, int.class, String.class}, "Chen MiaoXi", 7, "121");
        System.out.println(user2);
    }
}