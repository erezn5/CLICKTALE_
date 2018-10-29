package helpers;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.lua.LuaAerospikeLib;
import com.aerospike.client.policy.WritePolicy;
import com.pipe.pipeapp.ConsumeException;

import java.io.IOException;
import java.util.logging.Logger;

public class AeroSpikeMethods {

//    Logger logger;

    public AeroSpikeMethods() throws AerospikeException {
        try {
//            logger = Logger.getLogger(AeroSpikeMethods.class.getName());
//            logger.info("Connecting to aerospike...");
            AerospikeClient client = new AerospikeClient("172.22.3.126", 3000);

//            logger.info("Connected Successfully");
            WritePolicy policy = new WritePolicy();
            Key key = new Key("test", "myset", "mykey");
            Bin bin = new Bin("mybin", "myvalue");
            client.put(policy, key, bin);
        }catch (Exception e ){
            e.printStackTrace();
        }
    }
}
