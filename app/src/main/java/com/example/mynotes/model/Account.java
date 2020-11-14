package com.example.mynotes.model;

public class Account {
    private String username;
    private String password;
    private String email;
    public final static String CLASSNAME = "Account";

    public Account() {
    }

    public Account(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }


    /**
     * 该重写方法已可将Account类直接输出为json格式的字符串
     */
    @Override
    public String toString() {
        String string = "{" +
                "CLASSNAME : \"" + CLASSNAME + "\" , " +
                "username : \"" + username + "\" , " +
                "password : \"" + password + "\" , " +
                "email : \"" + email +
                "\" }";
        return string;
    }
}