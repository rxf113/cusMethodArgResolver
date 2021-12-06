package com.rxf113.cusmethodargresolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.util.ParameterMap;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.MethodParameter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author rxf113
 */
@Component
public class CusHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver, SmartInitializingSingleton {

    private final ObjectMapper objectMapper;

    /**
     * 注入requestMappingHandlerAdapter，拿到框架定义好的 HandlerMethodArgumentResolver集合
     * 然后将自己的实现类添加进去
     */
    private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @Override
    public void afterSingletonsInstantiated() {
        //获取已经定义好的 argumentResolvers，然后将自定义的 Resolver 放在第一位，不然会有其他Resolver先处理
        List<HandlerMethodArgumentResolver> argumentResolvers = requestMappingHandlerAdapter.getArgumentResolvers();
        ArrayList<HandlerMethodArgumentResolver> resolvers = new ArrayList<>(Objects.requireNonNull(argumentResolvers));
        resolvers.add(0, this);
        requestMappingHandlerAdapter.setArgumentResolvers(resolvers);
    }


    public CusHandlerMethodArgumentResolver(ObjectMapper objectMapper, RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
        this.objectMapper = objectMapper;
        this.requestMappingHandlerAdapter = requestMappingHandlerAdapter;
    }


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Rxf113.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        ParameterMap<String, String[]> parameterMap = (ParameterMap<String, String[]>) webRequest.getParameterMap();
        parameterMap.setLocked(false);
        //获取注解中value.为参数的key
        Rxf113 rxf113 = parameter.getParameterAnnotation(Rxf113.class);
        String paramKey = Objects.requireNonNull(rxf113).value();


        String[] val = parameterMap.get(paramKey);

        if (val != null) {
            //按照参数类型转换
            return objectMapper.readValue(val[0], parameter.getParameterType());
        }

        //获取body中的json
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.state(servletRequest != null, "No HttpServletRequest");
        ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(servletRequest);
        try (InputStream inputStream = inputMessage.getBody();
             InputStreamReader reader = new InputStreamReader(inputStream)) {
            if (inputStream.available() > 0) {
                //获取到整个json，放入parameterMap中
                @SuppressWarnings("unchecked")
                Map<String, Object> map = objectMapper.readValue(reader, Map.class);
                map.forEach((k, v) -> parameterMap.put(k, new String[]{String.valueOf(v)}));
                return map.get(paramKey);
            }
        }
        parameterMap.setLocked(true);
        return null;
    }
}
