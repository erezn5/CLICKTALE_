package com.pipe.pipeapp;

import com.pipe.record.comparator.model.FactoryHelper;
import com.rabbitmq.client.ConnectionFactory;
import helpers.Consumer;
import helpers.RecordMapper;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class ServiceCompare implements Runnable {

    final static String propertiesFile = "/qa.properties";
    private static Properties prop;

    static Logger log = Logger.getLogger(ServiceCompare.class);
    static Logger log1 = Logger.getLogger(ServiceCompare.class);
    //    static org.slf4j.Logger logBack = LoggerFactory.getLogger(ServiceCompare.class);
    private Consumer fetcher;
    private static Thread thread1;
    private static Thread thread2;
    private Object m_sync = new Object();
    public static FactoryHelper factoryHelper;



    public ServiceCompare(Consumer fetcher) {
        this.fetcher = fetcher;
    }

    public void run() {
        try {
            if(log.isDebugEnabled()){
                log.debug("Entering Application.");
            }
            log.info("Thread: " + thread1.getName() + " is running...");
            log.info("Thread: " + thread2.getName() + " is running...");
            log1.info("Sample info message");
//logBack.info("Thread: " +{}+ "is running",thread1.getName() );
            fetcher.consume();

        } catch (ConsumeException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        prop = new Properties();
        prop.load((ServiceCompare.class.getResourceAsStream(propertiesFile)));
//        PropertyConfigurator.configure("C:\\Git\\pipeline-test\\src\\main\\resources\\log4j.properties");

        RecordMapper mapper = new RecordMapper(prop);


        factoryHelper = new FactoryHelper(prop);
        ConnectionFactory factory = factoryHelper.fillPropertiesForRabbitConsumerAndConnect();

        String previousQueue = prop.getProperty("suite.pusher.defaultParams.previousRabbitQueue");
        String currentQueue = prop.getProperty("suite.pusher.defaultParams.currentRabbitQueue");

        int limit = Integer.parseInt(prop.getProperty("suite.pusher.defaultParams.limit"));

        thread1 = new Thread(new ServiceCompare(new RabbitAMQPService(previousQueue, limit, RecordMapper.PREVIOUS, mapper, factory)));
        thread2 = new Thread(new ServiceCompare(new RabbitAMQPService(currentQueue, limit, RecordMapper.CURRENT, mapper, factory)));
//        }


        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
    }


}
