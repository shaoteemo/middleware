package com.shaoteemo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Create Info: WordCount入口类
 * 配置job作业的相关参数、创建job对象、设置输入输出路径、提交作业
 * <br>Change Info:
 * <br>Create On 9/18/2024 20:54
 *
 * @author XiaoMo
 * @since 0.1-alpha
 */
public class WordCountDriver {
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        // 参数处理
        if (args == null || args.length < 2) {
            System.err.println("Usage:yarn jar xxx.jar com.shaoteemo.WordCountDriver <inpath> <outpath>");
            System.exit(0);
        }
        // 1.创建配置文对象
        Configuration conf = new Configuration();
        // 2.设置本地运行
//        conf.set("mapreduce.framework.name", "local");
        // 3.创建Job
        Job job = Job.getInstance(conf);
        // 4.设置Driver关联类
        job.setJarByClass(WordCountDriver.class);
        // 5.设置mapper相关信息
        job.setMapperClass(WordCountMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        // 6.设置Reducer相关
        job.setReducerClass(WordCountReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        // 7.输入输出路径
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        //提交作业并结束程序
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
