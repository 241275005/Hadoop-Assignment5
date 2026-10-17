package com.assignment5;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Task1 {

    public static class Task1Mapper
            extends Mapper<LongWritable, Text, Text, Text> {

        private final Text outKey = new Text();
        private final Text outValue = new Text();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString().trim();

            if (line.isEmpty()) {
                return;
            }

            String[] parts = line.split("\\s+");

            if (parts.length < 2) {
                return;
            }

            String indexId = parts[0];
            String stockCode = parts[1];

            String fileName =
                    ((org.apache.hadoop.mapreduce.lib.input.FileSplit)
                            context.getInputSplit())
                            .getPath()
                            .getName();

            outKey.set(indexId + "\t" + stockCode);

            if (fileName.equals("A.txt")) {
                outValue.set("A");
            } else if (fileName.equals("B.txt")) {
                outValue.set("B");
            } else {
                return;
            }

            context.write(outKey, outValue);
        }
    }

    public static class Task1Reducer
            extends Reducer<Text, Text, Text, Text> {

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            boolean inA = false;
            boolean inB = false;

            for (Text value : values) {
                String source = value.toString();

                if (source.equals("A")) {
                    inA = true;
                }

                if (source.equals("B")) {
                    inB = true;
                }
            }

            String relation;

            if (inA && inB) {
                relation = "BOTH";
            } else if (inA) {
                relation = "A_ONLY";
            } else {
                relation = "B_ONLY";
            }

            context.write(key, new Text(relation));
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 3) {
            System.err.println(
                    "Usage: Task1 <A input> <B input> <output>");
            System.exit(1);
        }

        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "Task1 Stock Relation");

        job.setJarByClass(Task1.class);

        job.setMapperClass(Task1Mapper.class);
        job.setReducerClass(Task1Reducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setNumReduceTasks(1);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileInputFormat.addInputPath(job, new Path(args[1]));

        FileOutputFormat.setOutputPath(job, new Path(args[2]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

