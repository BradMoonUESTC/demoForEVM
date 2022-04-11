package com.XY;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MySQLDemoFunctionByteCode {
 
    // MySQL 8.0 以下版本 - JDBC 驱动名及数据库 URL
//    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
//    static final String DB_URL = "jdbc:mysql://localhost:3306/RUNOOB";
 
    // MySQL 8.0 以上版本 - JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/evm_data?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
 
 
    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "root";
    static final String PASS = "Nerbonic123";

    public static String castToSHA3(String str){
        //transferFrom(address _from, address _to, uint256 _value) returns (bool success)
        Keccak.Digest256 digest256 = new Keccak.Digest256();
        String str_ret=Hex.toHexString(digest256.digest(str.getBytes())).substring(0, 8);
        return str_ret;
    }
    //函数的签名
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
        
            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
        
            // 执行查询
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            //*******输入要查看的函数

            String function="setStatus(Status)";
//            23b872dd0000000000000000000000001b5bbc9c3648633355573da30e0afceacf9d794f0000000000000000000000005c9b862cc418fb
//        6a9ca0ea153d1be3a2e907a84e0000000000000000000000000000000000000000000000119e47f21381f40000
            //function="2";
            System.out.println(castToSHA3(function));
            String function_name=function.substring(0,function.indexOf('('));
            String sql;
            sql = "INSERT INTO function__bytecode (function_name,function_name_with_param,bytecode,whole_bytecode) VALUES"
            + "("
            + "'"+function_name+"',"
            + "'"+function+"',"
            + "'"+castToSHA3(function)+"',"
            + "'')";
            //stmt.execute(sql);

            // 完成后关闭
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        System.out.println("Goodbye!");
    }
}