package com.example.apptest;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public class LoggerUnit {
    private static final Logger ljhLogger = LogManager.getLogger("FimiLogger");
    public static Logger logger;
    public LoggerUnit() {
        //加载配置
        ConfigureLog4J configureLog4J = new ConfigureLog4J();
        configureLog4J.configure();
        //初始化 log
        logger = Logger.getLogger(this.getClass());
    }

    public static void writeLogger(String logInfo) {
        ljhLogger.log(Priority.DEBUG, "ljh : " + logInfo);
    }
}
