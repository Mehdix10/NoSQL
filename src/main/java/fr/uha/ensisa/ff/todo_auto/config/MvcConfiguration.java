package fr.uha.ensisa.ff.todo_auto.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import fr.uha.ensisa.ff.todo_auto.dao.MongoDAO;
import fr.uha.ensisa.ff.todo_auto.dao.TodoDAO;
import fr.uha.ensisa.ff.todo_auto.dao.dummy.DummyTodoDAO;

@Configuration
@ComponentScan(basePackages="fr.uha.ensisa.ff.todo_auto")
@EnableWebMvc
public class MvcConfiguration implements WebMvcConfigurer {
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/", "/index.html").addResourceLocations("/resources/index.html");
		registry.addResourceHandler("/login*").addResourceLocations("/resources/login_page.html");
		registry.addResourceHandler("/*.css").addResourceLocations("/resources/");
		registry.addResourceHandler("/bootstrap/**").addResourceLocations("/resources/bootstrap/").setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));
		registry.addResourceHandler("/awsome/**").addResourceLocations("/resources/awsome/").setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.setUseSuffixPatternMatch(false);
	}

	@Bean
	public MultipartResolver multipartResolver(){
		return new CommonsMultipartResolver();
	}
	
	@Bean
	public TodoDAO getTodoDAO() {
		// Change to real DAO
		return new MongoDAO("mongodb://localhost:27017/");
	}
}
