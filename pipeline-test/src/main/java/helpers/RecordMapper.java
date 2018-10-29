package helpers;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

public class RecordMapper {

    public static String PREVIOUS = "previous";
    public static String CURRENT = "current";

    private Properties properties;

    HashMap<String, RecordSignature> previous = new HashMap<>();
    HashMap<String, RecordSignature> current = new HashMap<>();

    private final Object m_sync = new Object();
    private int chunkSize;
    Compare compare;// = new Compare();
    ArrayList<RecordsToCompare> m_chunk = new ArrayList<>();

    public RecordMapper(Properties properties) {
        this.properties = properties;
        this.chunkSize = Integer.parseInt(properties.getProperty("suite.pusher.defaultParams.chunkSize"));
        this.compare = new Compare(properties);
    }

    private HashMap<String, RecordSignature> getMap(String key) {
        if (PREVIOUS.equalsIgnoreCase(key)) {
            return previous;
        }
        return current;
    }

    private boolean isPrevious(HashMap<String, RecordSignature> map) {
        return map == previous;
    }

    private HashMap<String, RecordSignature> getMapToCompare(String key) {
        if (PREVIOUS.equalsIgnoreCase(key)) {
            return current;
        }
        return previous;
    }

    // Call in the end of the compare
    public void report() {
        synchronized (m_sync) {
            Iterator it = previous.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry pair = (HashMap.Entry) it.next();
                System.out.println(pair.getKey() + " = " + pair.getValue());
                it.remove(); // avoids a ConcurrentModificationException
            }
            // TODO PDT report missing messages
        }
    }

    public static void printMap(HashMap mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }


    public void addEntry(RecordSignature record, String pipelineKey) {
        addEntry(record, getMap(pipelineKey), getMapToCompare(pipelineKey));
    }


    public void addEntry(RecordSignature record, HashMap<String, RecordSignature> mapToAdd, HashMap<String, RecordSignature> mapToCompare) {
        synchronized (m_sync) {
            String key = record.getKey();
            if (mapToCompare.containsKey(key)) {
                System.out.println("Prepare new record for compare [" + key + "] " + record);
                RecordSignature recordToCompare = mapToCompare.remove(key);
                RecordsToCompare pair = isPrevious(mapToAdd)
                        ? new RecordsToCompare(record, recordToCompare)
                        : new RecordsToCompare(recordToCompare, record);
                System.out.println("Compare new pair " + m_chunk.size() + " [" + key + "] " + pair);

                // todo PDT add compare message to Compare class
                m_chunk.add(pair);
                System.out.println("Check chunk criteria...");
                if (m_chunk.size() > chunkSize) {
                    System.out.println("Add new record to map [" + key + "] " + record);
                    ArrayList<RecordsToCompare> chunkToCompare = m_chunk;
                    m_chunk = new ArrayList<>();
                    try {
                        compare.compareChunk(chunkToCompare);
                    } catch (Throwable e) {
                        System.err.println("Failed to compare the chunk" + e.getMessage());
                        e.printStackTrace();
                    }
                }
                System.out.println("Finish to prepare the comparison for [" + key + "] " + pair);
            } else {
                System.out.println("Add new record to map [" + key + "] " + record);
                mapToAdd.put(key, record);
            }
        }
    }


    public void addEntry(String topic, ConsumerRecord<String, String> record) {

        //  addEntry(record,)
    }
}
