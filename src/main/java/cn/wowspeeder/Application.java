package cn.wowspeeder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class Application {

    private static InternalLogger logger = InternalLoggerFactory.getInstance(Application.class);

    public static final String METHOD = "aes-256-cfb";

    public static final int SERVER_START_PORT=20000;
    public static final int CLIENT_START_PORT=30000;

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        Option option = new Option("s", "server", false, "server listen address");
        options.addOption(option);

        option = new Option("c", "client", false, "server connect address");
        options.addOption(option);

        option = new Option("num", "number", true, "100");
        options.addOption(option);

        option = new Option("sip", "server ip", true, "60.251.180.188");
        options.addOption(option);

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(options, args);

        String countValue = commandLine.getOptionValue("num", "1");
        String serverIp = commandLine.getOptionValue("sip", "60.251.180.188");

        String password =  "ygDa4P8JswqlTR7do_%s";

        // start 指定启动的数量
        int count = 1;
        try {
            count = Integer.parseInt(countValue);
        } catch (Exception e) {
            count = 1;
        }

        for (int i = 0; i < count; i++) {
            final int index = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (commandLine.hasOption("s")) {
                            new SSServer().startSingle(SERVER_START_PORT + index, String.format(password,index), METHOD);
                        } else if (commandLine.hasOption("c")) {
                            new SSLocal().startSingle("127.0.0.1", CLIENT_START_PORT + index, serverIp, SERVER_START_PORT
                                    + index, String.format(password,index), METHOD);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        logger.info("start success!");
    }
}
