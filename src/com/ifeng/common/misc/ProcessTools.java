package com.ifeng.common.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 有关进程的信息。有些情况下依赖于具体的操作环境
 * @author jinmy
 */
public final class ProcessTools {

    private static final String EMMA_COVERAGE_OUT_FILE = "emma.coverage.out.file";

    /**
     * 系统属性名称，指定ProcessTools在启动Java子进程时，需要拷贝哪些系统属性。
     * 它的值为一个正则表达式，符合表达式的当前系统属性将传递给Java子进程
     * 例如
     * -Dcom.ifeng.common.misc.copyProcessProps=java.*,com\.ifeng\.common.*
     */
    private static final String COPY_PROPS = ProcessTools.class.getPackage().getName()
            + ".copyProcessProps";
    private static final String START_SEQ = ProcessTools.class.getPackage().getName()
            + ".processStartSeq";

    /**
     * 用于区分不同进程的序列标识。目前只是用于EMMA的覆盖率报告文件。
     * 可以在外部指定序列开始值，避免多个测试之间的文件冲突。
     */
    private static int subProcessSeq = 1;
    static {
        String startSeq = System.getProperty(START_SEQ);
        if (startSeq != null) {
            subProcessSeq = Integer.parseInt(startSeq);
        }
    }
    
    private ProcessTools() {
        // utility class
    }
    
    private static synchronized int getNextSubProcessSeq() {
        return ++subProcessSeq;
    }
    /**
     * 在另一个进程中运行一个Java程序，使用当前进程的classPath。
     * 如果需要其它的properties，要在vmArgs中自行传递
     * @param vmArgs 虚拟机参数字符串
     * @param className 要运行的类名
     * @param progArgs 运行的程序的参数
     * @return 进程
     */
    public static Process execJava(String[] vmArgs, String className,
            String[] progArgs) throws IOException {
        return execJava(vmArgs, className, progArgs, null);
    }

    private static final String[] EMPTY_STRING_ARRAY = { };
    
    /**
     * 设置dir当前目录，其它同上。
     * JDK中用整行命令行方式传递的命令行信息不能包含引号和空格等，否则会出问题，
     * 因此在实现中要用String[]方式传递参数。
     */
    public static Process execJava(String[] vmArgs, String className,
            String[] progArgs, File dir) throws IOException {
        String[] properties = getPropertyDefs();
        if (vmArgs == null) {
            vmArgs = EMPTY_STRING_ARRAY;
        }
        if (progArgs == null) {
            progArgs = EMPTY_STRING_ARRAY;
        }
        String[] args = new String[vmArgs.length + progArgs.length
                + properties.length + 4];
        // array 内容：
        // java <vmArgs> -classpath <classpath> <properties> <className> <propArgs>
        args[0] = "java";
        System.arraycopy(vmArgs, 0, args, 1, vmArgs.length);
        args[vmArgs.length + 1] = "-classpath";
        args[vmArgs.length + 2] = System.getProperty("java.class.path");
        System.arraycopy(properties, 0, args, vmArgs.length + 3, properties.length);
        args[vmArgs.length + 3 + properties.length] = className;
        System.arraycopy(progArgs, 0, args, vmArgs.length + 4 + properties.length,
                progArgs.length);
        return Runtime.getRuntime().exec(args, null, dir);
    }
    
    /**
     * 得到字符串，包含需要传递给子Java进程的-D选项。
     * 包括COPY_PROPS系统property指定的
     * 以及在emma的输出文件名
     */
    private static String[] getPropertyDefs() {
        String copyProps = System.getProperty(COPY_PROPS);
        List props = new ArrayList();
        if (copyProps != null) {
            Pattern pattern = Pattern.compile(copyProps);
            for (Iterator it = System.getProperties().entrySet().iterator(); 
                    it.hasNext(); ) {
                Map.Entry entry = (Map.Entry)it.next();
                String name = String.valueOf(entry.getKey());
                String value = String.valueOf(entry.getValue());
                if (pattern.matcher(name).matches()) {
                    props.add("-D" + name + '=' + value); 
                }
            }
        }
        // 由于多个进程并行执行，如果都写到一个文件，会产生冲突
        // 这里让它们写道不同文件，执行完后再合并
        String emmaOutFile = System.getProperty(EMMA_COVERAGE_OUT_FILE);
        if (emmaOutFile != null) {
            // 下面是常量字符串相加，不会消耗性能
            props.add("-D" + EMMA_COVERAGE_OUT_FILE + '=' + emmaOutFile
                    + getNextSubProcessSeq());
        }
        String[] result = new String[props.size()];
        props.toArray(result);
        return result;
    }
    
    static class ErrorReader extends Thread {
        private BufferedReader errReader;
        public ErrorReader(InputStream errStream) {
            super("ProcessErrorReader");
            this.errReader = new BufferedReader(new InputStreamReader(errStream));
        }
        public void run() {
            // read other error out
            try {
                while (true) {
                    String line = this.errReader.readLine();
                    if (line != null) {
                        System.err.println("perr: " + line);
                    } else {
                        break;
                    }
                }
                // 不必管中间发生异常的特殊情况。
                this.errReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 执行一个Process，直到其退出，并返回其输出的信息。
     * 一般用这种方式得到少量返回信息，不适合大量输出信息的情况
     * @param process 已经启动的进程
     * @return 返回的信息。如果没有输出或者出错，返回null
     * 屏蔽了Exception
     */
    public static String execProcess(Process process) {
        try {
            StringBuffer result = new StringBuffer();
            // 在线程中读取error，并原样打印出来
            new ErrorReader(process.getErrorStream()).start();
            // 在当前线程中读取input
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            boolean first = true;
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (first) {
                    result.append(line);
                    first = false;
                } else {
                    result.append('\n').append(line); 
                }
            }
            process.waitFor();
            reader.close();
            return result.length() == 0 ? null : result.toString();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 运行一个命令，返回命令的结果.
     * JDK的Runtime.exec在执行命令行时，如果命令行中有引号、空格
     * 等特殊字符，并不能正确处理，要用 @see ProcessTools#execCommandResult(String[])
     * @param command 命令行。
     * @return 返回命令的结果。如果没有输出或出错返回null。屏蔽了Exception
     */
    public static String execCommandResult(String command) {
        try {
            return execProcess(Runtime.getRuntime().exec(command));
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * 运行一个命令，返回命令的结果.
     * JDK的Runtime.exec在执行命令行时，如果命令行中有引号、空格
     * 等特殊字符，并不能正确处理，要用这个方法，而不用上面的方法
     * @param command 命令行
     * @return 返回命令的结果。如果没有输出或出错返回null。屏蔽了Exception
     */
    public static String execCommandResult(String[] command) {
        try {
            return execProcess(Runtime.getRuntime().exec(command));
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * 同上，先设置当前目录
     */
    public static String execCommandResult(String command, File dir) {
        try {
            return execProcess(Runtime.getRuntime().exec(command, null, dir));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 同上，先设置当前目录
     */
    public static String execCommandResult(String[] command, File dir) {
        try {
            return execProcess(Runtime.getRuntime().exec(command, null, dir));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 同上，传递环境变量
     */
    public static String execCommandResult(String command, String[] env, File dir) {
        try {
            return execProcess(Runtime.getRuntime().exec(command, env, dir));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 同上，传递环境变量
     */
    public static String execCommandResult(String[] command, String[] env, File dir) {
        try {
            return execProcess(Runtime.getRuntime().exec(command, env, dir));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 运行一个Java命令，并返回命令的标准输出结果。
     * 参数参见execJava
     * @return 返回命令的结果。如果没有输出或出错返回null。屏蔽了Exception
     */
    public static String execJavaResult(String[] vmArgs, String className,
            String[] progArgs) {
        try {
            return execProcess(execJava(vmArgs, className, progArgs));
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * 运行一个Java命令，并返回命令的标准输出结果。
     * 参数参见execJava
     * @return 返回命令的结果。如果没有输出或出错返回null。屏蔽了Exception
     */
    public static String execJavaResult(String[] vmArgs, String className,
            String[] progArgs, File dir) {
        try {
            return execProcess(execJava(vmArgs, className, progArgs, dir));
        } catch (IOException e) {
            return null;
        }
    }

}
