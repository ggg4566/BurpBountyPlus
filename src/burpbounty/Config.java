package burpbounty;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class Config {
    private static String IgnoreParam = "";
    private static Boolean StopStatus= false;
    private static Integer ThreadNum = 20;

    public static String get_ignore_param() {
        return IgnoreParam;
    }

    public static void set_ignore_param(String Param) {
        Config.IgnoreParam = Param;
    }
    public static void init_ignore_param() {
        String line = System.lineSeparator();
        Config.IgnoreParam = ("submit"+"\n"+"Submit");
    }

    public static void set_run_status(Boolean status) {
        Config.StopStatus =status ;
    }

    public static boolean get_run_status() {
        return Config.StopStatus;
    }


    public static void set_threadpool_threadnum(Integer i) {
        Config.ThreadNum = i;
    }
    public static Integer get_threadpool_threadnum() {
        return Config.ThreadNum;
    }

}
