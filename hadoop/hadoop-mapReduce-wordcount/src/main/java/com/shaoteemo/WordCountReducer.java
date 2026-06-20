package com.shaoteemo;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Create Info: 程序处理Reducer
 * <br>Change Info:
 * <br>Create On 9/17/2024 22:01
 *
 * @author XiaoMo
 * @since 0.1-alpha
 */
/*
 * KEYIN: 输入数据Key类型。（与对应Mapper输出保持一致）
 * VALUEIN: 输入数据Value类型。（与对应Mapper输出保持一致）
 * KEYOUT: 输出数据Key类型，
 * VALUEOUT: 输出数据Value类型，
 *  */
public class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    // 求和总数
    private int count;
    // 输出求和总数
    private final IntWritable valueOut = new IntWritable();

    // 相同的key为一组。调用一次此方法，此处即相同的文本内容。
    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Reducer<Text, IntWritable, Text, IntWritable>.Context context) throws IOException, InterruptedException {

        //初始化总和为0
        count = 0;
        // 遍历value累加
        values.forEach(item -> count += item.get());
        // 设置封装统计结果并输出
        valueOut.set(count);
        context.write(key, valueOut);
    }
}
