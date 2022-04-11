package com.XY;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MySQLDemoMessageChain {
 
    // MySQL 8.0 以下版本 - JDBC 驱动名及数据库 URL
//    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
//    static final String DB_URL = "jdbc:mysql://localhost:3306/RUNOOB";
 
    // MySQL 8.0 以上版本 - JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/evm_data?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
 
 
    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "root";
    static final String PASS = "Nerbonic123";

    //查看执行次数超过一定的数量的消息调用
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
            String sql;
            sql = "SELECT distinct blocknum,origin FROM evmtest0105_5";
            ResultSet rs = stmt.executeQuery(sql);

            List<Integer> list_blocknum=new ArrayList<>();
            List<String> list_origin=new ArrayList<>();
            List<Integer> list_blocknum_all=new ArrayList<>();
            List<String> list_origin_all=new ArrayList<>();

            String sql2="SELECT * FROM evmtest0105_5";
            while(rs.next()){
                int blocknum=rs.getInt("blocknum");
                String origin=rs.getString("origin");
                list_origin.add(origin);
                list_blocknum.add(blocknum);
            }
            ResultSet rss=stmt.executeQuery(sql2);
            while(rss.next()){
                int blocknum=rss.getInt("blocknum");
                String origin=rss.getString("origin");
                list_origin_all.add(origin);
                list_blocknum_all.add(blocknum);
            }

            for(int i=0;i<list_blocknum.size();i++){
                int count=0;
                String origin=list_origin.get(i);
                int blocknum=list_blocknum.get(i);
                for(int j=0;j<list_blocknum_all.size();j++){

                    int blocknumcmp=list_blocknum_all.get(j);
                    String origincmp=list_origin_all.get(j);
                    if(blocknumcmp-blocknum>2){break;}
                    if(blocknumcmp==blocknum && origincmp.equals(origin)){
                        count++;
                    }
                }
                //System.out.print(i+",");
                if(count>100){
                    //System.out.println(" ");
                    System.out.println(list_blocknum.get(i)+","+list_origin.get(i)+","+count);
                }
            }

            // 完成后关闭
            rs.close();
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