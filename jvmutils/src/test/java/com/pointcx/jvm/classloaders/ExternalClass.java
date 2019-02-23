package com.pointcx.jvm.classloaders;

public class ExternalClass {
    public String getName(){
        return getClass().getName();
    }
}
