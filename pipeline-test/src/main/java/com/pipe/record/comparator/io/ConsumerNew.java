package com.pipe.record.comparator.io;

import com.pipe.pipeapp.ConsumeException;
import com.pipe.record.comparator.model.Record;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface ConsumerNew {

     Record consume() throws ConsumeException, IOException, TimeoutException, ParseException;
}
