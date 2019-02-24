package com.pointcx.jvm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

public class ClassUtilTest {


    static class User{
        static final Date NOW = new Date(System.currentTimeMillis());
        static String staticField;
        public String name;
        private int age;
        protected String email;

        public static enum Type {
            NULL(0), STRING(1), INT(2), BOOLEAN(3);
            int value;

            Type(int value) {
                this.value = value;
            }

            public int getValue() {
                return value;
            }
        }

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

        public int add(int a, int b){
            return a+b;
        }

        public long add(long a, long b){
            return a+b;
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

        boolean eq = ClassUtil.invoke("java", "equals", new Class[]{Object.class}, "java");
        System.out.println("java.equals(java) = " + eq);
    }

    @Test
    public void invoke1() {
    }

    @Test
    public void newInstance() {
        Date value = ClassUtil.newInstance("java.util.Date");
        System.out.println(value);
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

    @Test
    public void getEnumConstant(){
        Object typeString = ClassUtil.getEnumConstant(User.Type.class, "STRING");
        System.out.println(typeString);

        Object value = ClassUtil.getStaticFieldValue(User.class, "Type");
        System.out.println(value);
    }

    @Test
    public void getMethodReturnType(){
        List<Method> methods = ClassUtil.findMethod(User.class, "add");
        for(Method method : methods){
            System.out.println(method.getReturnType());
        }
    }
}