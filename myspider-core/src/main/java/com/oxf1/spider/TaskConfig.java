package com.oxf1.spider;

import com.oxf1.spider.config.ConfigOperator;
import com.oxf1.spider.config.SysDefaultConfig;
import com.oxf1.spider.config.impl.YamlConfigOperator;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by cxu on 2015/6/21.
 */
public class TaskConfig{
    private static String HOST = null;//本机ip
    private final String jvmProcessId;//jvm 进程id
    private final String taskId;//唯一标识
    private final String taskName;
    private final ConfigOperator cfg;
    /**
     * 存放同一个任务需要的多线程共享对象
     */
    private ConcurrentHashMap<String, Object> taskSharedObject;

    static{
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            HOST = "?.?.?.?";
        }
        if(StringUtils.isBlank(HOST)){
            HOST = addr.getHostAddress().toString();
        }
    }

    public TaskConfig(String taskId, String taskName){
        this.taskId = taskId;
        this.taskName = taskName;
        /*初始化jvm进程id*/
        this.jvmProcessId = ManagementFactory.getRuntimeMXBean().getName();

        String taskConfigFile = SysDefaultConfig.DEFAULT_SPIDER_WORK_DIR + getTaskFp() + File.separator + "cfg.yaml";
        this.cfg = new YamlConfigOperator(taskConfigFile);//默认的
        this.taskSharedObject = new ConcurrentHashMap<String, Object>(5);
    }

    public String getTaskId() {
        return this.taskId;
    }

    public String getTaskName(){
        return this.taskName;
    }

    public String loadString(String key) {
        key = this.getTaskKey(key);
        Object value = this.cfg.loadValue(key);
        if(value!=null){
            return (String)value;
        }
        else return null;
    }

    /**
     * 从配置读出一个key,转化为int
     * @param key
     * @return
     */
    public Integer loadInt(String key) {
        key = this.getTaskKey(key);
        Object value =  this.cfg.loadValue(key);
        if(value!=null){
            return (Integer)value;
        }
        return null;
    }

    /**
     * 保存配置
     * @param key
     * @param value
     */
    public void put(String key, Object value) {
        key = this.getTaskKey(key);
        cfg.put(key, value);
    }

    public void addTaskSharedObject(String key, Object obj){
        key = this.getTaskKey(key);
        taskSharedObject.put(key, obj);
    }

    public Object getTaskSharedObject(String key){
        key = this.getTaskKey(key);
        Object o = taskSharedObject.get(key);
        return o;
    }

    /**
     * 本机唯一任务标示
     * @return
     */
    public String getTaskFp(){
        return getTaskKey("");
    }

    /**
     * 加上和Task有关的前缀
     * @param key
     * @return
     */
    private String getTaskKey(String key){
        StringBuffer buf = new StringBuffer(50);
        buf.append(HOST)
                .append("@")
                .append(this.taskName)
                .append("@")
                .append(this.taskId)
                .append("@")
                .append(this.jvmProcessId);//使用jvm进程Id可以在一台机器上模拟分布式

        if(StringUtils.isNotBlank(key)){
            buf.append("@").append(key);
        }

        return buf.toString();
    }
}
