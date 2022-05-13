package com.jc.gateway.controller;
 
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@Slf4j
public class UserController {
	private Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@RequestMapping("/hi")
	public String hi(){
		return "hi shiro";
	}

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
	public String login(String username, String password, Model model) {
		//使用shiro编写认证操作
		//获取Subject
		Subject subject = SecurityUtils.getSubject();
		//封装用户数据
		UsernamePasswordToken token = new UsernamePasswordToken(username, password);
		//执行登录方法
		try {
			//只要执行login方法，就会去执行UserRealm中的认证逻辑
			subject.login(token);

			//如果没有异常，代表登录成功
			//跳转到textThymeleaf页面，代表主页
			return "redirect:/index";
		} catch (UnknownAccountException e) {
			logger.info(username + "用户名不存在");
			//登录失败
			model.addAttribute("msg", "用户名不存在");
			return "login";

		} catch (IncorrectCredentialsException e) {
			logger.info(username + "密码错误");
			model.addAttribute("msg", "密码错误");
			return "login";
		}
	}

	@Data
	class Author {
		private int age;
		private String name;
		private String email;
		// 省略 get set
	}
}