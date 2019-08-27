package cn.wowspeeder;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class Application {
    private static InternalLogger logger = InternalLoggerFactory.getInstance(Application.class);

    public static void main(String[] args) throws Exception {
//        Options options = new Options();
//        Option option = new Option("s", "server", false, "server listen address");
//        options.addOption(option);
//
//        option = new Option("c", "client", false, "server connect address");
//        options.addOption(option);
//
//        option = new Option("conf", "config", true, "config file path default:conf/config.json");
//        options.addOption(option);
//
//        CommandLineParser parser = new DefaultParser();
//        CommandLine commandLine = parser.parse(options, args);
//
//        String configPath = commandLine.getOptionValue("conf", "conf/config.json");
//
//        logger.info("config path:{}", configPath);
//        if (commandLine.hasOption("s")) {
//            new SSServer().start();
//        } else if (commandLine.hasOption("c")) {
//            new SSLocal().start();
//        } else {
//            logger.error("not found run type");
//        }
//        logger.info("start success!");


        // start 指定数量的 server
    }
}
