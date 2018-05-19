package com.mackness.springboot.session.redis.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import redis.clients.jedis.Jedis;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestApplicationTests {
	
	@LocalServerPort private int port;
	
	private Jedis jedis;
    private TestRestTemplate testRestTemplate;
    private TestRestTemplate testRestTemplateWithAuth;
    private String baseUrl = "http://localhost:";
    private String securityConfigMapping = "/";
	
	@Test
	public void contextLoads() {
	}
	
	@Before
    public void clearRedisData() {
        testRestTemplate = new TestRestTemplate();
        testRestTemplateWithAuth = new TestRestTemplate("test", "123");
        jedis = new Jedis("localhost", 6379);
        jedis.flushAll();
    }
	
	private String getSecurityTestUrl(){
		return baseUrl + port + securityConfigMapping;
	}

	@Test
	public void testRedisIsEmpty() {
	    Set<String> result = jedis.keys("*");
	    assertEquals(0, result.size());
	}
	
	@Test
	public void testUnauthenticatedCantAccess() {
	    ResponseEntity<String> result = testRestTemplate.getForEntity(getSecurityTestUrl(), String.class);
	    assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
	}
	
	@Test
	public void testRedisControlsSession() {
	    ResponseEntity<String> result = testRestTemplateWithAuth.getForEntity(getSecurityTestUrl(), String.class);
	    assertEquals("hello test", result.getBody()); //login worked
	 
	    Set<String> redisResult = jedis.keys("*");
	    assertTrue(redisResult.size() > 0); //redis is populated with session data
	 
	    String sessionCookie = result.getHeaders().get("Set-Cookie").get(0).split(";")[0];
	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Cookie", sessionCookie);
	    HttpEntity<String> httpEntity = new HttpEntity<>(headers);
	 
	    result = testRestTemplate.exchange(getSecurityTestUrl(), HttpMethod.GET, httpEntity, String.class);
	    assertEquals("hello test", result.getBody()); //access with session works worked
	 
	    jedis.flushAll(); //clear all keys in redis
	 
	    result = testRestTemplate.exchange(getSecurityTestUrl(), HttpMethod.GET, httpEntity, String.class);
	    assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());//access denied after sessions are removed in redis
	}
	
}
