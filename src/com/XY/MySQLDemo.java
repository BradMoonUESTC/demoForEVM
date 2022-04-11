package com.XY;

import javax.print.DocFlavor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MySQLDemo {
 
    // MySQL 8.0 以下版本 - JDBC 驱动名及数据库 URL
//    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
//    static final String DB_URL = "jdbc:mysql://localhost:3306/RUNOOB";
 
    // MySQL 8.0 以上版本 - JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/evm_data?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
 
 
    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "root";
    static final String PASS = "Nerbonic123";
 
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
            sql = "SELECT blocknum,callee,input FROM evmtest0105_5";
            ResultSet rs = stmt.executeQuery(sql);

            List<String> list = new ArrayList<>();
            List<Integer> list_blocknum=new ArrayList<>();
            List<String> list_input=new ArrayList<>();
            while(rs.next()){
                String callee= rs.getString("callee");
                int blocknum=rs.getInt("blocknum");
                String input=rs.getString("input");
                list.add(callee);
                list_blocknum.add(blocknum);
                list_input.add(input);
            }
            int flag_blocknum = 0,flag_blocknumj = 0;
            // 展开结果集数据库
            System.out.println(list.size());
            for(int i=0;i<list.size();i++){
                // 通过字段检索
                String callee= list.get(i);
                callee=callee.substring(2,callee.length());
                if(callee.equals("0000000000000000000000000000000000000001")||callee.equals("0000000000000000000000000000000000000002")||callee.equals("0000000000000000000000000000000000000003")){continue;}
                String calleeupper=callee.toUpperCase(Locale.ROOT);
                String calleelower=callee.toLowerCase(Locale.ROOT);
                int blocknum=list_blocknum.get(i);

                for(int j=0;j<list_input.size();j++){
                    String input=list_input.get(j);
                    if(input==null||input.isEmpty()){
                        continue;
                    }
                    int blocknumj=list_blocknum.get(j);
                    if(blocknumj>blocknum||blocknumj-blocknum<-10){
                        break;
                    }
//                    else if(blocknumj==blocknum){
//                        continue;
//                    }
                    if(input.indexOf(callee)!=-1||input.indexOf(calleelower)!=-1||input.indexOf(calleeupper)!=-1){
                        if(blocknumj==flag_blocknumj&&blocknum==flag_blocknum){
                            continue;
                        }
                        flag_blocknum=blocknum;
                        flag_blocknumj=blocknumj;
                        System.out.println(callee+","+input);
                        System.out.println(blocknum+","+blocknumj);
                        System.out.println("-----------------------------------------");
                    }
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