package com.XY;

import java.math.BigInteger;
import java.sql.*;
import java.util.*;

public class MySQLDemoReentrancyDataAdjoin {

    // MySQL 8.0 以下版本 - JDBC 驱动名及数据库 URL
//    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
//    static final String DB_URL = "jdbc:mysql://localhost:3306/RUNOOB";

    // MySQL 8.0 以上版本 - JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/evm_data?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";


    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "root";
    static final String PASS = "Nerbonic123";


    public static class Data {
        private String callee;
        private String caller;
        private String origin;
        private int blocknum;
        private String input;
        private Double ether;

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public Double getEther() {
            return ether;
        }

        public void setEther(double ether) {
            this.ether = ether;
        }

        public Data(String callee, String caller, String origin, int blocknum,String input,double ether) {
            this.callee = callee;
            this.caller = caller;
            this.origin = origin;
            this.blocknum = blocknum;
            this.input=input;
            this.ether=ether;
        }

        public String getCallee() {
            return callee;
        }

        public void setCallee(String callee) {
            this.callee = callee;
        }

        public String getCaller() {
            return caller;
        }

        public void setCaller(String caller) {
            this.caller = caller;
        }

        public String getOrigin() {
            return origin;
        }

        public void setOrigin(String origin) {
            this.origin = origin;
        }

        public int getBlocknum() {
            return blocknum;
        }

        public void setBlocknum(int blocknum) {
            this.blocknum = blocknum;
        }
    }

    //对比两个DATA是否相同（callee，caller，input）
    public static boolean compareData(Data data1,Data data2){
        if(data1.getCallee().equals(data2.getCaller())
                &&data1.getCaller().equals(data2.getCaller())
//                &&data1.getInput().equals(data2.getInput())
        ){
            return true;
        }else {
            return false;
        }
    }

    //region 根据list查看有多少不同的caller和callee
    public static List<String> getNodes(List<Data> list_temp) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < list_temp.size(); i++) {
            //如果当前list列表中不存在caller的数据，再添加
            if (list.indexOf(list_temp.get(i).getCaller()) == -1) {
                list.add(list_temp.get(i).getCaller());
            }
            //如果当前list列表中不存在callee的数据，再添加
            if (list.indexOf(list_temp.get(i).getCallee()) == -1) {
                list.add(list_temp.get(i).getCallee());
            }
        }
        return list;
    }
    //endregion
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String dbName="evmtest0105_8";
            //region 获取排序好的blocknum和origin去重数据并加入list
            String sql;
            sql = "SELECT distinct blocknum,origin FROM "+dbName+" order by blocknum asc";
            ResultSet rs = stmt.executeQuery(sql);

            List<Integer> list_blocknum = new ArrayList<>();
            List<String> list_origin = new ArrayList<>();
            while (rs.next()) {
                int blocknum = rs.getInt("blocknum");
                String origin = rs.getString("origin");
                list_origin.add(origin);
                list_blocknum.add(blocknum);
            }
            //endregion

            System.out.println("已获得所有区块和交易发起地址并排序，数据量：" + list_blocknum.size());

            //region 获取所有数据并加入list
            List<Integer> list_blocknum_all = new ArrayList<>();
            List<String> list_origin_all = new ArrayList<>();
            List<String> list_callee_all = new ArrayList<>();
            List<String> list_caller_all = new ArrayList<>();
            List<String> list_input_all=new ArrayList<>();
            List<Double> list_value_all=new ArrayList<>();
//            List<Integer> list_id = new ArrayList<>();
            String sql2 = "SELECT * FROM "+dbName;
            ResultSet rs4 = stmt.executeQuery(sql2);
            while (rs4.next()) {
                //5000000000000000000
//                BigInteger exchange=new BigInteger("1000000000000000000");
                try {
                    String ether=rs4.getString("value");
//                    BigInteger value = new BigInteger(rs4.getString("value"));

                    double value = Double.parseDouble(ether.substring(0,3));
                    int blocknum = rs4.getInt("blocknum");
                    String origin = rs4.getString("origin");
                    String callee = rs4.getString("callee");
                    String caller = rs4.getString("caller");
//                    int id = rs4.getInt("id");
                    String input = rs4.getString("input");
                    list_value_all.add(value);
                    list_input_all.add(input);
                    list_callee_all.add(callee);
                    list_caller_all.add(caller);
                    list_origin_all.add(origin);
                    list_blocknum_all.add(blocknum);
//                    list_id.add(id);
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
            //endregion

            System.out.println("已保存所有数据，准备进行整理排序，数据量:" + list_blocknum_all.size());
            int maxblock=list_blocknum.get(list_blocknum.size()-1);
            int minblock=list_blocknum.get(0);
            int blocks=maxblock-minblock;
            int percent=0;
            for (int i = 0; i < list_blocknum.size(); i++) {
                List<Data> list_temp = new ArrayList<>();
                String origin = list_origin.get(i);
                int blocknum = list_blocknum.get(i);
                percent=(blocknum-minblock)*100/blocks;
//                System.out.println(percent+"%");
                for (int j = 0; j < list_blocknum_all.size(); j++) {
                    int blocknumcmp = list_blocknum_all.get(j);
                    String origincmp = list_origin_all.get(j);
                    if (blocknumcmp - blocknum > 2) {
                        break;
                    }
                    //向结果中添加对应的数据，同一个blocknum，同一个origin
                    if (blocknumcmp == blocknum && origincmp.equals(origin)) {
                        if (!list_callee_all.get(j).equals("0x0000000000000000000000000000000000000001")
                                && !list_callee_all.get(j).equals("0x0000000000000000000000000000000000000002")) {
                            list_temp.add(new Data(list_callee_all.get(j), list_caller_all.get(j), origin, blocknum,list_input_all.get(j),list_value_all.get(j)));
                        }
                        //添加一条数据到list中
                    }
                }
                //region 查看相邻的几个调用是否存在重入可能
                for(int j=0;j<list_temp.size();j++){
                    if(j>4) {
                        Data data = list_temp.get(j);
                        Data data_prev = list_temp.get(j-1);
                        Data data_prev2 = list_temp.get(j-2);
                        Data data_prev3 = list_temp.get(j-3);
                        if(
                                  data.getCallee().equals(data_prev2.getCallee())
                                &&data.getCaller().equals(data_prev2.getCaller())
                                &&data.getCallee().equals(data_prev.getCaller())
                                &&data.getCaller().equals(data_prev.getCallee())
                                &&data.getCaller().equals(data_prev3.getCallee())
                                &&data.getCallee().equals(data_prev3.getCaller()))
                        {
                            System.out.println("检测到可能的重入");
                            System.out.println("当前区块："+blocknum+"，origin："+origin);
                            if(data.getInput()!=null&&data_prev2.getInput()!=null) {
                                if (data.getInput().equals(data_prev2.getInput())) {
                                    System.out.println("检测到可能的重入——level2");
                                    //出现循环调用，且相隔的input相同
                                    System.out.println("!!!当前区块：" + blocknum + "，origin：" + origin);
                                    break;
                                }
                            }
                        }

                    }
                }
                //endregion
//                if(list_temp.size()>=6){
//                    for(int j=0;j<list_temp.size()-5;j++){
//                        Data data0=list_temp.get(j);
//                        Data data1=list_temp.get(j+1);
//                        Data data2=list_temp.get(j+2);
//                        if(compareData(data0,data1)||compareData(data1,data2)){
//                            continue;
//                        }
//                        int count=0;
//                        for(int k=j+3;k<list_temp.size()-2;k++){
//                            if(list_temp.get(k)==null
//                                    ||list_temp.get(k+1)==null
//                                    ||list_temp.get(k+2)==null){
//                                break;
//                            }
//                            if(compareData(data0,list_temp.get(k))
//                                    &&compareData(data1,list_temp.get(k+1))
//                                    &&compareData(data2,list_temp.get(k+2))
//                            )
//                            {
//                                count++;
//                            }
//
//                        }
//                        if(count>0){
//                            System.out.println("区块："+blocknum+",交易发起："+origin);
//                            System.out.println("存在重复序列,重复次数："+count);
//                            System.out.println("________________________________");
//
//                        }
//                    }
//
//                }
            }


            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) {
            }// 什么都不做
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        System.out.println("Goodbye!");
    }
}