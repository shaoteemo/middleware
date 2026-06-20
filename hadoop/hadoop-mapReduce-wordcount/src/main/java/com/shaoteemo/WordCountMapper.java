package com.shaoteemo;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Create Info:
 * <br>Change Info:
 * <br>Create On 9/17/2024 21:36
 *
 * @author XiaoMo
 * @since 0.1-alpha
 */
/*
 * KEYIN: 输入数据Key类型，默认类型是文本偏移量。
 * VALUEIN: 输入数据Value类型，默认的是文本内容
 * KEYOUT: 输出数据Key类型，
 * VALUEOUT: 输出数据Value类型，
 *  */
public class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    // 输出value对象。出现一次即1个。
    private final IntWritable valueOut = new IntWritable(1);
    // 输出key对象
    private final Text keyOut = new Text();

    // 文本中的每行内容调用一次此方法
    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        if (value != null) {
            // 转化为String处理
            String text = value.toString();
            // 去前后空格后按照空格切片
            String[] words = text.trim().split(" ");
            // 循环统计输出
            for (String word : words) {
                keyOut.set(word);
                context.write(keyOut, valueOut);
            }
        }
    }

}
