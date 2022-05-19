package com.jc.gateway.controller;
 
import com.alibaba.fastjson.JSONObject;
import com.jc.gateway.base.Result;
import com.jc.gateway.base.ResultCodes;
import com.jc.gateway.entity.dto.UserDto;
import com.jc.gateway.entity.token.TokenCache;
import com.jc.gateway.service.UserService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class UserController {
	private Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	UserService userService;
	
	@RequestMapping("/hi")
	public String hi(){
		return "hi shiro";
	}

	@Autowired
	TokenCache tokenCache;

	@Autowired
	ApplicationContext applicationContext;

	/**
	 * 方式一：返回ModelAndView
	 */
	@RequestMapping("/index")
	public ModelAndView index() {
		ModelAndView view = new ModelAndView();
		// 设置跳转的视图 默认映射到 src/main/resources/templates/{viewName}.html
		view.setViewName("index");
		// 设置属性
		view.addObject("title", "我的templates页面");
		view.addObject("desc", "欢迎进入我的csdn博客");
		Author author = new Author();
		author.setAge(18);
		author.setEmail("xhw_vae@163.com");
		author.setName("way");
		view.addObject("author", author);
		return view;
	}

	/**
	 * 方式二：返回String
	 * 注意：此方式不可以使用@RestController，@RestController 等价
	 *      于 @Controller 加上 @ResponseBody，@ResponseBody表示
	 *      该方法的返回不会被解析为跳转, 而是直接写入http响应正文。
	 */
	@RequestMapping("/index1")
	public String index1(HttpServletRequest request) {
		// TODO 与上面的写法不同，但是结果一致。
		// 设置属性
		request.setAttribute("title", "我的templates页面");
		request.setAttribute("desc", "欢迎进入我的csdn博客");
		Author author = new Author();
		author.setAge(18);
		author.setEmail("xhw_vae@163.com");
		author.setName("way");
		request.setAttribute("author", author);
		// 返回的 index 则会映射到 src/main/resources/templates/index.html
		return "index";
	}

	@RequestMapping("/login")
	public ResponseEntity<Object> login(@RequestParam String username,@RequestParam char[] password, HttpServletRequest request) {
		UserDto loginInfo = new UserDto(username,password);
		//使用shiro编写认证操作
		//获取Subject
		Subject subject = SecurityUtils.getSubject();
		//封装用户数据
		UsernamePasswordToken token = new UsernamePasswordToken(loginInfo.getUsername(), loginInfo.getPassword());
		//执行登录方法
		JSONObject jsonObject = new JSONObject();
		try {
			//只要执行login方法，就会去执行UserRealm中的认证逻辑
			subject.login(token);

			//如果没有异常，代表登录成功
			//Shiro认证通过后会将user信息放到subject内，生成token并返回
			UserDto user = (UserDto) subject.getPrincipal();
			String newToken = userService.generateJwtToken(user.getUsername(),loginInfo.getPassword());
			//response.setHeader("x-auth-token", newToken);
			jsonObject.put("x-auth-token",newToken);
			//模拟放入redis场景
			Map<String,String> tokenMap = new HashMap<>();
			tokenMap.put(user.getUsername(),newToken);
			tokenCache.addTokens(tokenMap);
			TokenCache bean = applicationContext.getBean(TokenCache.class);
			return new ResponseEntity<>(new Result(ResultCodes.SUCCESS.getCode(),"登录成功",jsonObject), HttpStatus.OK);
		} catch (UnknownAccountException e) {
			//登录失败
			e.printStackTrace();
			jsonObject.put("msg","登录失败");
			return new ResponseEntity<>(new Result(ResultCodes.INTERNAL_ERROR.getCode(),"登录失败",jsonObject), HttpStatus.OK);

		} catch (AuthenticationException e) {
			e.printStackTrace();
			jsonObject.put("msg","账号或密码错误");
			return new ResponseEntity<>(new Result(ResultCodes.NO_LOGIN.getCode(),"账号或密码错误",jsonObject), HttpStatus.OK);
		}
	}

	@RequestMapping("/toLogin")
	public String toLogin(Model model) {
		model.addAttribute("msg", "请登录");
		return "login";
	}

	@Data
	class Author {
		private int age;
		private String name;
		private String email;
		// 省略 get set
	}
}