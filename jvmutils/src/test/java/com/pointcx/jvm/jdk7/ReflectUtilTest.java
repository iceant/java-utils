package com.pointcx.jvm.jdk7;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ReflectUtilTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void invokeStaticMethod() {
        System.currentTimeMillis();
        long currentTimeMillis = ReflectUtil.invokeStaticMethod(System.class, "currentTimeMillis", long.class);
        System.out.println("invokeStaticMethod:System.currentTimeMillis()="+currentTimeMillis);
    }

    @Test
    public void invoke(){
        String result = (String) ReflectUtil.invoke("jovo", "replace", String.class, new Class[]{char.class, char.class}, Character.valueOf('o'), 'a');
        System.out.println("String.replace(char, char) -> 'jovo'.replace(o, a) = "+result);

        long time = ReflectUtil.invoke(new Date(), "getTime", long.class);
        System.out.println("new Date().getTime()="+time);

        boolean ret = ReflectUtil.invoke("java", "equals", boolean.class, new Class[]{Object.class}, "java");
        System.out.println("java.equals(java)?"+ret);

        List array = ReflectUtil.invokeStaticMethod(Arrays.class, "asList", List.class, new Class[]{Object[].class}, 1, 2, 3);
        System.out.println(array);
    }

    @Test
    public void testNewInstance(){
        Date ret = ReflectUtil.newInstance(Date.class);
        System.out.println(ret);

        Date date = ReflectUtil.newInstance(Date.class.getName(), ClassLoader.getSystemClassLoader(), new Class[]{int.class, int.class, int.class}, 2019-1900, 11, 1);
        System.out.println(date);
    }
}