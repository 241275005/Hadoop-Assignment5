package com.assignment5;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Task2 {

    public static class Task2Mapper
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

            if (parts.length < 3) {
                return;
            }

            String indexId = parts[0];
            String relation = parts[2];

            outKey.set(indexId);
            outValue.set(relation);

            context.write(outKey, outValue);
        }
    }

    public static class Task2Reducer
            extends Reducer<Text, Text, Text, Text> {

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            int both = 0;
            int aOnly = 0;
            int bOnly = 0;

            for (Text value : values) {

                String relation = value.toString();

                if (relation.equals("BOTH")) {
                    both++;
                } else if (relation.equals("A_ONLY")) {
                    aOnly++;
                } else if (relation.equals("B_ONLY")) {
                    bOnly++;
                }
            }

            int union = both + aOnly + bOnly;

            String result =
                    both + "\t"
                    + aOnly + "\t"
                    + bOnly + "\t"
                    + union;

            context.write(key, new Text(result));
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.err.println(
                    "Usage: Task2 <input> <output>");
            System.exit(1);
        }

        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "Task2 Statistics");

        job.setJarByClass(Task2.class);

        job.setMapperClass(Task2Mapper.class);
        job.setReducerClass(Task2Reducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setNumReduceTasks(1);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
