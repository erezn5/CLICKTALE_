package helpers;

import org.joda.time.DateTime;
import org.joda.time.tz.UTCProvider;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class IdentifyProvider {

    long MachineIdOffset = 0 << 5;
    AtomicLong counter = new AtomicLong();
    long EpochUtcMiliseconds = ZonedDateTime.of(2015, 1, 1, 0, 0,
                                                        0, 0, ZoneOffset.UTC).toEpochSecond() * 1000;
    final long UnsignedIntMaxValue = 0xFFFFFFFFL;


    public long generateSessionId() throws InterruptedException {

        if((0x1f & counter.get()) == 0) Thread.sleep(1);

        long utcNowMiliseconds = System.currentTimeMillis();
        long sessionIdTime = utcNowMiliseconds - EpochUtcMiliseconds;

        long sid =  (sessionIdTime << 14 | MachineIdOffset | (0x1f & counter.getAndIncrement()));
        return sid;

    }

    public long generateUserId() throws InterruptedException {
        return generateSessionId();
    }

    public long toUtcTimeMillis(long sessionId){
        return (EpochUtcMiliseconds + (sessionId >>> 14));
    }

    public org.joda.time.DateTime toDate (long sessionId){
        long miliseconds = toUtcTimeMillis(sessionId);
        DateTime dateTime = new DateTime(miliseconds);

        return dateTime;
    }
}
