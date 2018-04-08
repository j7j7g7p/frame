package com.servlet.entity;

import java.util.Date;

import com.servlet.annotation.Entity;
import com.servlet.annotation.Format;
import com.servlet.base.DateTypeHandler;

@Entity
public class User {

	private Integer id;
	private String username;
	private String password;
	private String name;
	private Integer age;
	@Format(value = DateTypeHandler.class)
	private Date birthday;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public User(Integer id, String username, String password, String name, Integer age) {
		super();
		this.id = id;
		this.username = username;
		this.password = password;
		this.name = name;
		this.age = age;
	}

	public User() {
		super();
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", password=" + password + ", name=" + name + ", age="
				+ age + ", birthday=" + birthday + "]";
	}
}
