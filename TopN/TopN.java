import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class TopN extends Configured implements Tool {

    public static class TopMapper extends Mapper<Object, Text, Text, IntWritable>{
        private TreeMap<Integer,String> top;
        private int n;

        @Override
        public void setup(Context context){
            top = new TreeMap<>();
            n = Integer.parseInt(context.getConfiguration().get("nValue"));
        }

        @Override
        public void map(Object key, Text value, Context context){
            String term = value.toString().split("\\s")[0];
            String [] indexes = value.toString().split("\\s")[1].split(";");

            int count = 0;
            for(String i: indexes){
                count += Integer.parseInt(i.split(":")[1]);
            }

            top.put(count,term);
            if(top.size() > n){
                top.remove(top.firstKey());
            }
        }

        @Override
        public void cleanup(Context context) throws IOException, InterruptedException {
            for(Map.Entry<Integer,String> e: top.entrySet()){
                context.write(new Text(e.getValue()), new IntWritable(e.getKey()));
            }
        }
    }

    public static class TopReducer extends Reducer<Text,IntWritable,Text,IntWritable>{
        private TreeMap<Integer,String> top;
        private int n;

        @Override
        public void setup(Context context){
            top = new TreeMap<>();
            n = Integer.parseInt(context.getConfiguration().get("nValue"));
        }

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            String term = key.toString();
            Integer count = 0;

            for(IntWritable i: values){
                count = i.get();
            }

            top.put(count,term);
            if(top.size() > n){
                top.remove(top.firstKey());
            }

        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            for(Map.Entry<Integer,String> e: top.entrySet()){
                context.write(new Text(e.getValue()), new IntWritable(e.getKey()));
            }
        }
    }

    public static void main(String []args) throws Exception {
        ToolRunner.run(new Configuration(), new TopN(), args);
    }

    @Override
    public int run(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = this.getConf();
        Job job = Job.getInstance(conf, "Top N");
        job.setJarByClass(TopN.class);
        job.setMapperClass(TopMapper.class);
        job.setReducerClass(TopReducer.class);
        job.setNumReduceTasks(1);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        return job.waitForCompletion(true) ? 0 : 1;
    }
}
