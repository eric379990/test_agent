package com.csc.test_agent.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csc.test_agent.exception.TestAgentRuntimeException;

public class ProcessUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessUtils.class);

    /** 
     * Get the process id
     * @param command 
     * @return 
     */
    public static String getPID(String command){
        BufferedReader reader =null;
        try{
            Process process = Runtime.getRuntime().exec("ps -ef");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while((line = reader.readLine())!=null){
                if(line.contains(command)){
                    String[] strs = line.split("\\s+");
                    return strs[1];
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(reader!=null){
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
      
    /** 
     * Closed the process
     * @param Pid process pid
     */  
    public static void killProcess(String Pid){  
        Process process = null;  
        BufferedReader reader =null;  
        try{  
            process = Runtime.getRuntime().exec("kill -9 "+Pid);  
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));  
            String line = null;  
            while((line = reader.readLine())!=null){  
                LOGGER.info("kill PID return info -----> "+line);  
            }
        }catch(Exception e){
            LOGGER.error(e.getMessage());;
        }finally{  
            if(process!=null){
                process.destroy();
            }

            if(reader!=null){
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new TestAgentRuntimeException("Close reader exception", e);
                }
            }
        }
    }
}
