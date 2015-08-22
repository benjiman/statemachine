package com.benjiweber.statemachine;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class StateMachine {
    public static <X> X start(X x) {
        return x;
    }
    public static <CURRENT,NEXT> NEXT fake(Class<NEXT> ifaceType, CURRENT current) {
        try {
            return (NEXT) Proxy.newProxyInstance(ifaceType.getClassLoader(), new Class[]{ifaceType}, (proxy, method, args) -> {
                Class[] argTypes = args == null ? new Class[0] : asList(args).stream().map(Object::getClass).collect(toList()).toArray(new Class[0]);
                Class<?> aClass = proxy.getClass().getInterfaces()[0];
                Method superMethod = aClass.getMethod(method.getName(), argTypes);
                return superMethod.invoke(proxy, args);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
