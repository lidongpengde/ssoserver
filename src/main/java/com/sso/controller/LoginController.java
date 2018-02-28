package com.sso.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Created by lidongpeng on 2018/2/26.
 */
@Controller
public class LoginController {
    Map<String,String> cache=new HashMap<String, String>();

    /**
     * 跳转到登录页面，如果已登录，则生成ticket，回跳来源地址
     * @param request
     * @return
     */
    @RequestMapping("/toLogin")
    public String toLogin(HttpServletRequest request){
        String querystr=request.getParameter("redirect");
        String gloabal=null;
        Cookie cookie=getCookieByName(request, "globalCookie");
        if (cookie!=null){
             gloabal=cookie.getValue();
             if (!StringUtils.isEmpty(gloabal)){
                 String redirect= (String) request.getSession().getAttribute("redirect");
                 StringBuffer buffer=new StringBuffer(redirect);
                 buffer.append("?");
                 //生成ticket
                 String ticket=UUID.randomUUID().toString().substring(0,6);
                 cache.put(ticket,gloabal);
                 buffer.append("ticket="+ticket);
                 return "redirect:"+buffer.toString();
             }
        }
        request.getSession().setAttribute("redirect",querystr);
        return "login";
    }

    /**
     * 1、验证密码 2、生成ticket 3、构建会跳地址携带ticket
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/authPrinciple")
    public String authPrinciple(HttpServletRequest request, HttpServletResponse response){
        String username=request.getParameter("username");
        String password=request.getParameter("password");
        //验证用户名密码,此处省略
        //构建回跳地址
        String redirect= (String) request.getSession().getAttribute("redirect");
        StringBuffer buffer=new StringBuffer(redirect);
        buffer.append("?");
        //生成ticket
        String ticket=UUID.randomUUID().toString().substring(0,6);
        Cookie cookie=new Cookie("globalCookie",username);
        response.addCookie(cookie);
        cache.put(ticket,username);
        buffer.append("ticket="+ticket);
        return "redirect:"+buffer.toString();
    }

    /**
     * 验证ticket，返回用户信息
     * @param ticket
     * @return
     */
    @RequestMapping(value = "/tickets/{ticket}", method = RequestMethod.POST)
    public final ResponseEntity<String> createServiceTicket(@PathVariable("ticket") final String ticket) {
        try {
            String result= cache.get(ticket);
            if (result!=null && result!=""){
                return new ResponseEntity<String>(result, HttpStatus.OK);
            }
        } catch (final Exception e) {

    }
        return new ResponseEntity<String>("验证token出错", HttpStatus.BAD_REQUEST);
    }
    public  Cookie getCookieByName(HttpServletRequest request, String name) {
         Cookie[] cookieMap = request.getCookies();
         if (cookieMap==null)
             return null;
         for (Cookie cookie:cookieMap){
             if (cookie.getName().equals(name)){
                 return cookie;
             }
         }
         return null;
    }
}
