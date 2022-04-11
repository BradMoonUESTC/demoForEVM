package com.XY;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class MySQLDemoReentrancyData {

    private Logger LOG = LoggerFactory.getLogger(MySQLDemoReentrancyData.class);

    public static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    public static final int CORE_POOL_SIZE = Math.max(4, Math.min(CPU_COUNT - 1, 8));

    public static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;

    /** 有向图邻接表 */
    private Map<Integer, Set<Integer>> denseAdjacencyTable;
    /** 点访问状态 */
    private Set<Integer> vertexAccessStatus = new HashSet<Integer>();
    /** 追踪栈 */
    private List<Integer> traceStack = new ArrayList<Integer>();
    /** 环列表 */
    private List<List<Integer>> cycles = new ArrayList<List<Integer>>();

    public ExecutorService threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1000),
            Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());

    public MySQLDemoReentrancyData(Map<Integer, Set<Integer>> denseAdjacencyTable) {
        this.denseAdjacencyTable= denseAdjacencyTable;
    }

    public void findCycle() {
        for (Map.Entry<Integer, Set<Integer>> entry : denseAdjacencyTable.entrySet()) {
            vertexAccessStatus = new HashSet<Integer>();
            findCycle(entry.getKey());
        }
        LOG.debug("filter pre cycles count {}", cycles.size());
        Map<Integer, List<List<Integer>>> lenCycles = new HashMap<Integer, List<List<Integer>>>();
        cycles.stream().forEach(cycle -> {
            int cycleLen = cycle.size();
            if (cycleLen > 10) return;
            List<List<Integer>> currentCycles = lenCycles.get(cycleLen);
            if (null == currentCycles) {
                currentCycles = new ArrayList<List<Integer>>();
                lenCycles.put(cycleLen, currentCycles);
            }
            currentCycles.add(cycle);
        });
        List<List<Integer>> filterCycles = new ArrayList<List<Integer>>();
        lenCycles.values().forEach(value -> {
            Future<List<List<Integer>>> future = threadPool.submit(new Callable<List<List<Integer>>>() {
                @Override
                public List<List<Integer>> call() throws Exception {
                    List<List<Integer>> repeatCycles = new ArrayList<List<Integer>>();
                    Set<Integer> repeatCycleIds = new HashSet<Integer>();
                    for (int x = 0, xLen = value.size(); x < xLen; x++) {
                        if (repeatCycleIds.contains(x)) continue;
                        if(value.size()>100){
                            System.out.println("stop");
                        }
                        System.out.println("repeat_cycle"+value.size());
                        judgeRepeatCycle(value, x, repeatCycleIds);
                        repeatCycles.add(value.get(x));
                    }
                    return repeatCycles;
                }
            });
            while (!future.isDone()) {
            }
            try {
                filterCycles.addAll(future.get());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        cycles = filterCycles;
        LOG.debug("filter post cycles count {}", cycles.size());
    }

    public boolean hasCycle() {
        return cycles.size() > 0;
    }

    public List<List<Integer>> getCycles() {
        return this.cycles;
    }

    private void findCycle(int vertex) {
        if (vertexAccessStatus.contains(vertex)) {
            int j = 0;
            if ((j = traceStack.indexOf(vertex)) != -1) {
                List<Integer> cycle = new ArrayList<Integer>();
                while (j < traceStack.size()) {
                    cycle.add(traceStack.get(j));
                    j++;
                }
                cycles.add(cycle);
                return;
            }
            return;
        }
        vertexAccessStatus.add(vertex);
        traceStack.add(vertex);

        Set<Integer> vertexs = denseAdjacencyTable.get(vertex);
        for (int v : vertexs) {
            if (denseAdjacencyTable.containsKey(v)) {
                findCycle(v);
            }
        }

        traceStack.remove(traceStack.size() - 1);
    }

    private void judgeRepeatCycle(List<List<Integer>> cycles, int currentCycleId, Set<Integer> repeatCycleIds) {
        List<Integer> cycle = cycles.get(currentCycleId);
        int cycleLen = cycle.size();
        String cycleTxt = StringUtils.join(cycle, "-");
        for (int x = 0, xLen = cycles.size(); x < xLen; x++) {
            if (x == currentCycleId || repeatCycleIds.contains(x)) continue;
            List<Integer> ccycle = cycles.get(x);
            for (int y = 0; y < cycleLen; y++) {
                StringBuilder sb = new StringBuilder(50);
                for (int z = y; z < cycleLen + y; z++) {
                    sb.append(ccycle.get(z >= cycleLen ? z - cycleLen : z)).append("-");
                }
                sb.deleteCharAt(sb.length() - 1);
                if (cycleTxt.equals(sb.toString())) {
                    repeatCycleIds.add(currentCycleId);
                    repeatCycleIds.add(x);
                }
            }
        }
    }


    // MySQL 8.0 以下版本 - JDBC 驱动名及数据库 URL
//    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
//    static final String DB_URL = "jdbc:mysql://localhost:3306/RUNOOB";

    // MySQL 8.0 以上版本 - JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/evm_data?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";


    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "root";
    static final String PASS = "Nerbonic123";

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

    //根据value返回key
    public static String getKeyStringFromMap(Map<String,Integer> map,int value) {
        String key="";
        for (Map.Entry<String, Integer> m : map.entrySet()) {
            if (m.getValue()==(value)) {
                key = m.getKey();
            }
        }
        return key;
    }

    public static class Data {
        private String callee;
        private String caller;
        private String origin;
        private int blocknum;

        public Data(String callee, String caller, String origin, int blocknum) {
            this.callee = callee;
            this.caller = caller;
            this.origin = origin;
            this.blocknum = blocknum;
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

            //region 获取排序好的blocknum和origin去重数据并加入list
            String sql;
            sql = "SELECT distinct blocknum,origin FROM evmtest0105_101 where blocknum>6467000 order by blocknum asc";
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
            List<Integer> list_id = new ArrayList<>();
            String sql2 = "SELECT * FROM evmtest0105_101 where blocknum>6467000";
            ResultSet rs4 = stmt.executeQuery(sql2);
            while (rs4.next()) {
                int blocknum = rs4.getInt("blocknum");
                String origin = rs4.getString("origin");
                String callee = rs4.getString("callee");
                String caller = rs4.getString("caller");
                int id = rs4.getInt("id");
                list_callee_all.add(callee);
                list_caller_all.add(caller);
                list_origin_all.add(origin);
                list_blocknum_all.add(blocknum);
                list_id.add(id);
            }
            //endregion

            System.out.println("已保存所有数据，准备进行整理排序，数据量:" + list_blocknum_all.size());

            for (int i = 0; i < list_blocknum.size(); i++) {
                List<Data> list_temp = new ArrayList<>();
                String origin = list_origin.get(i);
                int blocknum = list_blocknum.get(i);
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
                            list_temp.add(new Data(list_callee_all.get(j), list_caller_all.get(j), origin, blocknum));
                        }
                        //添加一条数据到list中
                    }
                }
                //循环之后，所有同一个区块同一个origin的数据保存到list_temp中
                //接下来对list_temp中的数据进行解析，先构造邻接表，然后判断是否有回路
                //list_temp中每个data存放着caller和callee，每个不同的caller和callee是一个顶点
                //region 定义顶点集合和顶点集合数组
                List<String> list_nodes = getNodes(list_temp);//顶点集合
                Map<String,Integer> map=new HashMap<String,Integer>();
                for(int j=0;j<list_nodes.size();j++){
                    map.put(list_nodes.get(j),j);
                }
                //endregion
                Map<Integer, Set<Integer>> adjacencyTable = new HashMap<Integer, Set<Integer>>();

                for(int j=0;j<list_nodes.size();j++){
                    List<Integer> list=new ArrayList<>();//一个地址的所有出度地址
                    for(int k=0;k<list_temp.size();k++){
                        if(list_temp.get(k).getCaller().equals(list_nodes.get(j))){
                            list.add(map.get(list_temp.get(k).getCallee()));
                        }
                    }
                    if(list.size()>10){
//                        System.out.println("出现了出度大于10的地址，可能是关键节点合约");
//                        System.out.println("合约地址："+list_nodes.get(j)+"，调用地址数量："+list.size());
                    }
                    //将调用图作为邻接表填充进map
                    adjacencyTable.put(map.get(list_nodes.get(j)), new HashSet<>(list));
                }
                MySQLDemoReentrancyData dfsCycle = new MySQLDemoReentrancyData(adjacencyTable);
                dfsCycle.findCycle();
                if (!dfsCycle.hasCycle()) {
                    //System.out.println("No Cycle.");
                } else {
                    System.out.println("已收集好blocknum为" + blocknum + ",origin为" + origin + "的数据，准备解析");

                    List<List<Integer>> cycleList = dfsCycle.getCycles();
                    for (int k = 0, len = cycleList.size(); k < len; k++) {
                        List<Integer> list=cycleList.get(k);
                        if(list.size()>3){//循环中如果大于3个地址，标记出来
                            System.out.println("alert");
                        }
                        for(int m=0;m<list.size();m++){
                            String address=getKeyStringFromMap(map,list.get(m));
                            System.out.print(address+" # ");
                        }
                        System.out.println(" ");
                        System.out.println(StringUtils.join(cycleList.get(k), "#"));
                        System.out.println("-----------------------------------------------------");
                    }
                }

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